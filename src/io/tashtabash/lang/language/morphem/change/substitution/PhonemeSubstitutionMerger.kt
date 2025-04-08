package io.tashtabash.lang.language.morphem.change.substitution


fun unitePhonemeSubstitutions(
    old: List<PhonemeSubstitution?>,
    new: List<PhonemeSubstitution?>
): List<PhonemeSubstitution>? {
    var oldIdx = 0
    var newIdx = 0
    val sizeSum = old.size + new.size
    val result = mutableListOf<PhonemeSubstitution>()

    while (true) {
        val curOld = old.getOrNull(oldIdx)
        val curNew = new.getOrNull(newIdx)
        if (oldIdx + newIdx >= sizeSum && curOld == null && curNew == null)
            break

        if (curOld == null && curNew == null) {
            result += PassingPhonemeSubstitution
            if (oldIdx < old.size)
                oldIdx++
            if (newIdx < new.size)
                newIdx++
            continue
        }
        if (curOld == null) {
            result += curNew!!
            newIdx++
            oldIdx++
            continue
        }
        if (curNew == null) {
            result += curOld
            newIdx++
            oldIdx++
            continue
        }
        if (curOld is DeletingPhonemeSubstitution) {
            result += DeletingPhonemeSubstitution
            oldIdx++
            continue
        }
        if (curNew is EpenthesisSubstitution) {
            result += curNew
            newIdx++
            continue
        }
        if (curOld is EpenthesisSubstitution && curNew is DeletingPhonemeSubstitution) {
            oldIdx++
            newIdx++
            continue
        }

        result += (curNew * curOld)
            ?: return null
        oldIdx++
        newIdx++
    }

    return result
}
