package io.tashtabash.lang.language.morphem.change.substitution


fun unitePhonemeSubstitutions(
    old: List<PhonemeSubstitution?>,
    new: List<PhonemeSubstitution?>
): List<PhonemeSubstitution> {
    var oldIdx = 0
    var newIdx = 0
    val sizeSum = old.size + new.size
    val result = mutableListOf<PhonemeSubstitution>()

    while (oldIdx + newIdx < sizeSum) {
        val curOld = old.getOrNull(oldIdx)
        val curNew = new.getOrNull(newIdx)

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
            continue
        }
        if (curNew == null) {
            result += curOld
            oldIdx++
            continue
        }
        if (curOld is DeletingPhonemeSubstitution) {
            result += DeletingPhonemeSubstitution
            oldIdx++
            continue
        }

        result += curNew * curOld
        oldIdx++
        newIdx++
    }

    return result
}
