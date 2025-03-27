package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.morphem.change.substitution.PassingPhonemeSubstitution
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.matcher.BorderPhonemeMatcher
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
    val phonemeTypeNumber = PhonemeType.values().size
    val resultTemplateChanges = templateChanges.toMutableList()

    for (i in 0..resultTemplateChanges.size - phonemeTypeNumber) {
        var window = templateChanges.drop(i)
            .take(phonemeTypeNumber)
            .filterIsInstance<TemplateSingleChange>()
        var isMirrored = false

        if (window[0].position == Position.Beginning) {
            window = window.map { it.mirror() }
            isMirrored = true
        }

        if (window.size != phonemeTypeNumber)
            continue
        if (!window.all { it.position == window[0].position })
            continue
        if (!window.all { it.rule.matchers.size == window[0].rule.matchers.size })
            continue
        if (window[0].rule.matchers in listOf(listOf(BorderPhonemeMatcher), listOf(PassingPhonemeMatcher, BorderPhonemeMatcher)))
            continue

        val matcherPhonemeTypes = window.map { it.rule.matchers[0] }
            .filterIsInstance<TypePhonemeMatcher>()
            .map { it.phonemeType }
        if (!PhonemeType.values().all { it in matcherPhonemeTypes })
            continue

        val croppedTemplateChanges = window.map {
            val croppedRule = PhonologicalRule(
                listOf(PassingPhonemeMatcher) + it.rule.matchers.drop(1),
                it.rule.precedingMatchers.size,
                it.rule.followingMatchers.size,
                it.rule.substitutions,
                it.rule.allowSyllableStructureChange
            )
            it.copy(rule = croppedRule)
        }
        // Check if the cropped changes are the same
        if (!croppedTemplateChanges.all { it == croppedTemplateChanges[0] })
            continue

        var newChange = croppedTemplateChanges[0]
        if (isMirrored)
            newChange = newChange.mirror()
        resultTemplateChanges[i] = newChange
        for (j in 1 until phonemeTypeNumber)
            resultTemplateChanges.removeAt(i + 1)
    }

    return resultTemplateChanges
}

/**
 * Obsolete because passing Rules are semantically significant, they check the presence of a phoneme
 */
private fun simplifyPassingRules(templateChanges: List<TemplateChange>): List<TemplateChange> =
    templateChanges.map { simplifyPassingRules(it) }

private fun simplifyPassingRules(templateChange: TemplateChange): TemplateChange {
    if (templateChange !is TemplateSingleChange)
        return templateChange

    return templateChange.applyAsEnd {
        var resultTemplateChange = it

        while (true) {
            if (resultTemplateChange.rule.precedingMatchers.getOrNull(0) == PassingPhonemeMatcher) {
                val newMatchers = resultTemplateChange.rule
                    .precedingMatchers
                    .drop(1)
                resultTemplateChange = resultTemplateChange.copy(
                    rule = resultTemplateChange.rule.copy(precedingMatchers = newMatchers)
                )
                continue
            } else if (
                resultTemplateChange.rule.targetMatchers.getOrNull(0) == PassingPhonemeMatcher
                && resultTemplateChange.rule.targetMatchers.size > 1 //Don't simplify the target matchers into nothing
                && resultTemplateChange.rule.substitutions.getOrNull(0) == PassingPhonemeSubstitution
            ) {
                val newMatchers = resultTemplateChange.rule
                    .targetMatchers
                    .drop(1)
                val newSubstitutions = resultTemplateChange.rule
                    .substitutions
                    .drop(1)
                resultTemplateChange = resultTemplateChange.copy(
                    rule = resultTemplateChange.rule.copy(
                        targetMatchers = newMatchers,
                        substitutions = newSubstitutions
                    )
                )
                continue
            }

            break

        }

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
