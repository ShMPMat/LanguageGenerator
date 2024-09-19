package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.morphem.change.substitution.PassingPhonemeSubstitution
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.matcher.PassingPhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.TypePhonemeMatcher


fun createSimplifiedTemplateChange(templateChanges: List<TemplateChange>): TemplateChange =
    simplifyTemplateChange(TemplateSequenceChange(templateChanges))

fun simplifyTemplateChange(templateChange: TemplateChange): TemplateChange = when (templateChange) {
    is TemplateSingleChange -> templateChange.copy()
    is TemplateSequenceChange -> {
        var resultChanges = templateChange.changes

        resultChanges = flattenHierarchy(resultChanges)
        resultChanges = simplifyTypePairsInRules(resultChanges)
        resultChanges = simplifyPassingRules(resultChanges)
        resultChanges = simplifyEqualRules(resultChanges)

        unwrapSingletonSequence(resultChanges)
    }
    else -> throw LanguageException("Unknown template change '$templateChange'")
}

private fun flattenHierarchy(templateChanges: List<TemplateChange>): List<TemplateChange> =
    templateChanges.flatMap {
        when (it) {
            is TemplateSequenceChange -> it.changes
            else -> listOf(it)
        }
    }

private fun simplifyTypePairsInRules(templateChanges: List<TemplateChange>): List<TemplateChange> {
    val windowSize = PhonemeType.values().size
    val resultTemplateChanges = templateChanges.toMutableList()

    for (i in 0..resultTemplateChanges.size - windowSize) {
        var window = templateChanges.drop(i)
            .take(windowSize)
            .filterIsInstance<TemplateSingleChange>()
        var isMirrored = false

        if (window.size != windowSize)
            continue
        if (!window.all { it.position == window[0].position })
            continue
        if (!window.all { it.phonemeMatchers.size == window[0].phonemeMatchers.size })
            continue
        if (window[0].phonemeMatchers.isEmpty())
            continue

        if (window[0].position == Position.Beginning) {
            window = window.map { it.mirror() }
            isMirrored = true
        }

        val matcherPhonemeTypes = window.map { it.phonemeMatchers[0] }
            .filterIsInstance<TypePhonemeMatcher>()
            .map { it.phonemeType }
        if (!PhonemeType.values().all { it in matcherPhonemeTypes })
            continue

        val croppedTemplateChanges = window.map {
            it.copy(phonemeMatchers = listOf(PassingPhonemeMatcher) + it.phonemeMatchers.drop(1))
        }
        if (!croppedTemplateChanges.all { it == croppedTemplateChanges[0] })
            continue

        var newChange = croppedTemplateChanges[0]
        if (isMirrored)
            newChange = newChange.mirror()
        resultTemplateChanges[i] = newChange
        for (j in 1 until windowSize)
            resultTemplateChanges.removeAt(i + 1)
    }

    return resultTemplateChanges
}

private fun simplifyPassingRules(templateChanges: List<TemplateChange>): List<TemplateChange> =
    templateChanges.map { simplifyPassingRules(it) }

private fun simplifyPassingRules(templateChange: TemplateChange): TemplateChange {
    if (templateChange !is TemplateSingleChange)
        return templateChange

    return templateChange.applyAsEnd {
        var resultTemplateChange = it

        while (
            resultTemplateChange.phonemeMatchers.getOrNull(0) == PassingPhonemeMatcher &&
            resultTemplateChange.matchedPhonemesSubstitution.getOrNull(0) == PassingPhonemeSubstitution
        )
            resultTemplateChange = resultTemplateChange.copy(
                phonemeMatchers = resultTemplateChange.phonemeMatchers.drop(1),
                matchedPhonemesSubstitution = resultTemplateChange.matchedPhonemesSubstitution.drop(1),
            )

        resultTemplateChange
    }
}

private fun simplifyEqualRules(templateChanges: List<TemplateChange>): List<TemplateChange> {
    val resultTemplateChanges = templateChanges.toMutableList()
    var i = 0

    while (i < resultTemplateChanges.size - 1) {
        if (resultTemplateChanges[i] == resultTemplateChanges[i + 1])
            resultTemplateChanges.removeAt(i)
        else
            i++
    }

    return resultTemplateChanges
}

private fun unwrapSingletonSequence(templateChanges: List<TemplateChange>): TemplateChange =
    if (templateChanges.size == 1)
        templateChanges[0]
    else
        TemplateSequenceChange(templateChanges)

// If TemplateSingleChange is End, apples the lambda.
// If TemplateSingleChange is Beginning, mirrors it, applies the lambda, mirrors it back
private fun TemplateSingleChange.applyAsEnd(lmb: (TemplateSingleChange) -> TemplateSingleChange): TemplateSingleChange {
    var change = this
    var isMirrored = false
    if (position == Position.Beginning) {
        change = change.mirror()
        isMirrored = true
    }

    change = lmb(change)

    if (isMirrored)
        change = change.mirror()

    return change
}
