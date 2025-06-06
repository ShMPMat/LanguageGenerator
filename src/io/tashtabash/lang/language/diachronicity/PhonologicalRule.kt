package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.morphem.change.substitution.*
import io.tashtabash.lang.language.phonology.matcher.*
import kotlin.math.max
import kotlin.math.min


data class PhonologicalRule(
    val precedingMatchers: List<PhonemeMatcher>,
    val targetMatchers: List<PhonemeMatcher>,
    val followingMatchers: List<PhonemeMatcher>,
    val substitutions: List<PhonemeSubstitution>,
    val allowSyllableStructureChange: Boolean = false
) {
    init {
        if (targetMatchers.size != substitutions.count { it !is EpenthesisSubstitution })
            throw LanguageException("The number of targetMatchers and non-epenthesis substitutions must be equal")
    }

    constructor(
        matchers: List<PhonemeMatcher>,
        precedingLength: Int,
        followingLength: Int,
        substitutions: List<PhonemeSubstitution>,
        allowSyllableStructureChange: Boolean = false
    ) : this(
        matchers.take(precedingLength),
        matchers.drop(precedingLength).dropLast(followingLength),
        matchers.takeLast(followingLength),
        substitutions,
        allowSyllableStructureChange
    )

    val matchers: List<PhonemeMatcher> by lazy {
        precedingMatchers + targetMatchers + followingMatchers
    }

    val substitutionPairs: List<Pair<PhonemeMatcher?, PhonemeSubstitution>> by lazy {
        var matcherIdx = 0

        substitutions.map { s ->
            if (s is EpenthesisSubstitution)
                null to s
            else
                targetMatchers[matcherIdx++] to s
        }
    }

    private val spreadMatchers: List<PhonemeMatcher?>
        get() = precedingMatchers + substitutionPairs.map { it.first } + followingMatchers

    private val lastIdx = spreadMatchers.size - substitutions.count { it == DeletingPhonemeSubstitution }

    fun mirror() = PhonologicalRule(
        followingMatchers.reversed(),
        targetMatchers.reversed(),
        precedingMatchers.reversed(),
        substitutions.reversed()
    )

    /**
     * @return such a list of PhonologicalRules that the first matched member will
     *  have the same result as the consecutive application of this and other.
     *  This works for any given sequence of phonemes assuming that this matches
     *  it in the first place.
     */
    operator fun plus(other: PhonologicalRule): List<PhonologicalRule> {
        val resultRules = mutableListOf<Pair<PhonologicalRule, IntRange?>>(this to null)
        var i = 0

        while (i < resultRules.size) {
            // Larger by 1 than the first possible index
            var shift = -other.spreadMatchers.size

            while (true) {
                val (currentBase, currentRange) = resultRules[i]
                // Don't apply shifts before the range: they are either unavailable or handled already
                shift = max(shift + 1, (currentRange?.last ?: Int.MIN_VALUE) + 1)

                if (shift >= currentBase.lastIdx)
                    break
                val result = currentBase.applyWithShift(other, shift)
                    ?: continue

                if (result.isNarrowed) {
                    // Exclude endless loops when other's beginning matches its end
                    val applicationRange = if (shift + other.spreadMatchers.size > currentBase.spreadMatchers.size)
                        result.rule.spreadMatchers.indices
                    else
                        result.applicationRange

                    resultRules += result.rule to applicationRange
                } else
                    resultRules[i] = result.rule to result.applicationRange
            }
            i++
        }

        return resultRules.map { it.first }.reversed()
    }

    /**
     * @return the shortest possible list of PhonologicalRules, which, applied
     *  consecutively, will have the same result as the consecutive application
     *  of this and other.
     */
    operator fun times(other: PhonologicalRule): List<PhonologicalRule> {
        var resultRule = this

        for (shift in -other.matchers.size + 1 until resultRule.lastIdx) {
            val result = resultRule.applyWithShift(other, shift)
                ?: continue
            if (result.isNarrowed)
                return listOf(this, other)
            resultRule = result.rule
        }

        return listOf(resultRule)
    }

    /**
     * @return a PhonologicalRule which is identical to application of this and
     *  then application of other, applied at the point shifted by shift.
     *  If other can't be possibly applied with such shift, return null.
     */
    private fun applyWithShift(other: PhonologicalRule, shift: Int): ShiftedApplicationResult? {
        if (shouldSkipApplication(other, shift))
            return null

        // Preceding matchers + the matchers of other applied before the matchers of this start
        val thisSubstitutionsShift = precedingMatchers.size + max(0, -shift)
        val otherSubstitutionsShift = other.precedingMatchers.size + max(0, shift)
        val thisShiftedSubstitutions = createNullPadding(precedingMatchers.size) + substitutions

        val (newMatchers, isNarrowed, isChanged, applicationRange) = unitePhonemeMatchers(
            spreadMatchers,
            thisShiftedSubstitutions,
            other.matchers,
            shift
        ) ?: return null
        if (!isChanged)
            return null

        val newSubstitutions = unitePhonemeSubstitutions(
            createNullPadding(thisSubstitutionsShift - otherSubstitutionsShift) + substitutions,
            createNullPadding(otherSubstitutionsShift - thisSubstitutionsShift) + other.substitutions
        ) ?: return null
        val newSubstitutionsShift = min(thisSubstitutionsShift, otherSubstitutionsShift)
        val directSubstitutionsCount = newSubstitutions.count { it !is EpenthesisSubstitution }

        return ShiftedApplicationResult(
            PhonologicalRule(
                newMatchers,
                newSubstitutionsShift,
                newMatchers.size - newSubstitutionsShift - directSubstitutionsCount,
                newSubstitutions,
                allowSyllableStructureChange || other.allowSyllableStructureChange
            ),
            isNarrowed,
            applicationRange.first..
                    applicationRange.last
                    // Account for length inflation which epenthesis causes
                    + other.substitutions.count { it is EpenthesisSubstitution }
                    // Account for the fact that a glottal stop added between all vowels should do "oao -> o'a'o"
                    - other.followingMatchers.size
        )
    }

    private fun createNullPadding(size: Int): List<Nothing?> = (1..size)
        .map { null }

    private fun shouldSkipApplication(other: PhonologicalRule, shift: Int): Boolean {
        // Skip if the other matches the non-targetMatchers of this rule
        // and the corresponding matchers are wider that the ones in the other
        val matchedWindow = spreadMatchers.drop(max(0, shift))
            .take(other.matchers.size)
        if (shift + other.matchers.size <= precedingMatchers.size) {
            val matchedOther = other.spreadMatchers.takeLast(matchedWindow.size)
            if (matchedWindow.zip(matchedOther).map { it.second?.times(it.first) }.all { it == null || !it.second })
                return true
        } else if (shift >= spreadMatchers.size - followingMatchers.size) {
            val matchedOther = other.spreadMatchers.take(matchedWindow.size)
            if (matchedWindow.zip(matchedOther).map { it.second?.times(it.first) }.all { it == null || !it.second })
                return true
        }

        return false
    }

    override fun toString() = targetMatchers.joinToString("") +
            " -> ${substitutions.joinToString("")}" +
            " / ${precedingMatchers.joinToString("")}" +
            " _ ${followingMatchers.joinToString("")}" +
            if (allowSyllableStructureChange) "!" else ""

    /**
     * Move matchers on the borders of targetMatchers to precedingMatchers and followingMatchers
     * if the substitutions are passing
     */
    fun trim(): PhonologicalRule {
        var curRule = this

        while (curRule.substitutions.lastOrNull() == PassingPhonemeSubstitution)
            curRule = curRule.copy(
                substitutions = curRule.substitutions.dropLast(1),
                targetMatchers = curRule.targetMatchers.dropLast(1),
                followingMatchers = curRule.targetMatchers.takeLast(1) + curRule.followingMatchers
            )
        while (curRule.substitutions.firstOrNull() == PassingPhonemeSubstitution)
            curRule = curRule.copy(
                substitutions = curRule.substitutions.drop(1),
                targetMatchers = curRule.targetMatchers.drop(1),
                precedingMatchers = curRule.precedingMatchers + curRule.targetMatchers.take(1)
            )

        return curRule
    }
}


private data class ShiftedApplicationResult(
    val rule: PhonologicalRule,
    val isNarrowed: Boolean,
    val applicationRange: IntRange
)
