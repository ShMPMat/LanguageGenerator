package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.PhonemeContainer
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
        val bases = applyInside(other).toMutableList()

        val modifications = bases.toList().flatMap { (base, range) ->
            val leftBorderShifts = -other.spreadMatchers.size + 1 until 0

            bases += leftBorderShifts
                .filter { range == null || it + other.spreadMatchers.size < range.first }
                .mapNotNull { shift ->
                    base.applyWithShift(other, shift)
                        ?.let { (rule, _, newRange) ->
                            val lengthDiff = rule.spreadMatchers.size - base.spreadMatchers.size
                            val shiftedOldRangeEnd = range?.last
                                ?.let { it + lengthDiff }
                            rule to newRange.first..(shiftedOldRangeEnd ?: newRange.last)
                        }
                }
                .distinct()

            bases.toList().flatMap { (leftBorderBase, leftBorderRange) ->
                // The first shift on which the matchers of other will cross the right boundary
                val start = leftBorderBase.spreadMatchers.size - other.spreadMatchers.size + 1
                // The last shift on which the matchers of other still overlap with this
                val end = leftBorderBase.spreadMatchers.size - leftBorderBase.substitutions.count { it == DeletingPhonemeSubstitution }
                val rightBorderShifts = start until end

                rightBorderShifts
                    .filter { leftBorderRange == null || it > leftBorderRange.last }
                    .mapNotNull { shift -> leftBorderBase.applyWithShift(other, shift)?.rule }
                    .distinct()
            }
        }

        return (modifications.reversed() + bases.map { it.first }.reversed()).distinct()
    }

    /**
     * @return the shortest possible list of PhonologicalRules, which, applied
     *  consecutively, will have the same result as the consecutive application
     *  of this and other.
     */
    operator fun times(other: PhonologicalRule): List<PhonologicalRule> {
        val resultBase = applyInside(other)
            .takeIf { it.size == 1 }
            ?.get(0)
            ?.first
            ?: return listOf(this, other)

        for (shift in -other.matchers.size + 1 until resultBase.matchers.size)
            if (shift !in computeInternalShifts(other) && resultBase.applyWithShift(other, shift) != null)
                return listOf(this, other)

        return listOf(resultBase)
    }

    private fun computeInternalShifts(other: PhonologicalRule): IntRange =
        0..spreadMatchers.size - other.matchers.size - substitutions.count { it is DeletingPhonemeSubstitution }

    /**
     * @return a PhonologicalRule which is identical to consecutive application
     *  of this and other, if the application of other is restricted to the area
     *  matched by this.
     */
    private fun applyInside(other: PhonologicalRule): List<Pair<PhonologicalRule, IntRange?>> =
        computeInternalShifts(other)
            .fold(listOf(this to null as IntRange?)) { prev, shift ->
                prev.flatMap { (curRule, curRange ) ->
                    if (curRange != null && shift <= curRange.last)
                        listOf(curRule to curRange)
                    else curRule.applyWithShift(other, shift)
                        ?.let { (rule, isNarrowed, newRange) ->
                            listOfNotNull(
                                (curRule to curRange).takeIf { isNarrowed },
                                rule to (curRange?.first ?: newRange.first)..newRange.last
                            )
                        } ?: listOf(curRule to curRange)
                }
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
            applicationRange
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


fun createPhonologicalRule(rule: String, phonemeContainer: PhonemeContainer): PhonologicalRule {
    val allowSyllableStructureChange = rule.last() == '!'
    val clearedRule = rule.dropLastWhile { it == '!' }

    return PhonologicalRule(
        clearedRule.dropWhile { it != '/' }
            .drop(1)
            .dropLastWhile { it != '_' }
            .dropLast(1)
            .replace(" ", "")
            .let { createPhonemeMatchers(it, phonemeContainer) },
        clearedRule.dropLastWhile { it != '>' }
            .dropLast(2)
            .replace(" ", "")
            .let { createPhonemeMatchers(it, phonemeContainer) },
        clearedRule.dropWhile { it != '/' }
            .dropWhile { it != '_' }
            .drop(1)
            .replace(" ", "")
            .let { createPhonemeMatchers(it, phonemeContainer) },
        clearedRule.dropWhile { it != '>' }
            .drop(1)
            .dropLastWhile { it != '/' }
            .dropLast(1)
            .replace(" ", "")
            .let { createPhonemeSubstitutions(it, phonemeContainer) },
        allowSyllableStructureChange
    )
}
