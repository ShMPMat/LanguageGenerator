package io.tashtabash.lang.language.morphem.change.substitution

import kotlin.math.max


fun unitePhonemeSubstitutions(
    old: List<PhonemeSubstitution?>,
    new: List<PhonemeSubstitution?>
): List<PhonemeSubstitution> {
    var oldIdx = 0
    var newIdx = 0
    val maxIdx = max(old.size, new.size)
    val result = mutableListOf<PhonemeSubstitution>()

    while (oldIdx < maxIdx || newIdx < maxIdx) {
        val curOld = old.getOrNull(oldIdx)
        val curNew = new.getOrNull(newIdx)

        if (curOld == null)
            result += curNew!!
        else if (curNew == null)
            result += curOld
        else if (curOld is DeletingPhonemeSubstitution) {
            result += DeletingPhonemeSubstitution
            oldIdx++
            continue
        } else
            result += curNew

        oldIdx++
        newIdx++
    }

    return result
}
