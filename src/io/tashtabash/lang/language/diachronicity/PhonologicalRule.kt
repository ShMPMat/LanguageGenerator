package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.morphem.change.substitution.EpenthesisSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.createPhonemeSubstitutions
import io.tashtabash.lang.language.morphem.change.substitution.unitePhonemeSubstitutions
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatchers
import io.tashtabash.lang.language.phonology.matcher.unitePhonemeMatchers
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
        allowSyllableStructureChange: Boolean
    ) : this(
        matchers.take(precedingLength),
        matchers.drop(precedingLength).dropLast(followingLength),
        matchers.takeLast(followingLength),
        substitutions,
        allowSyllableStructureChange
    )

    val matchers: List<PhonemeMatcher>
        get() = precedingMatchers + targetMatchers + followingMatchers

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

        val modifications =  bases.toList().flatMap { base ->
            val leftBorderShifts = -other.matchers.size until 0

            val leftBorderApplications = leftBorderShifts
                .mapNotNull { shift -> base.applyWithShift(other, shift)?.first }
                .distinct()

            (bases + leftBorderApplications).flatMap { leftBorderBase ->
                val rightBorderShifts = leftBorderBase.matchers.size - other.matchers.size until leftBorderBase.matchers.size

                rightBorderShifts.mapNotNull { shift -> leftBorderBase.applyWithShift(other, shift) }
                    .map { (rule, isNarrowed) ->
                        if (isNarrowed)
                            // Add the rule from before the narrowing
                            bases += leftBorderBase
                        rule
                    }
                    .distinct()
            }
        }

        return (modifications.reversed() + bases.reversed()).distinct()
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
            ?: return listOf(this, other)

        for (shift in -other.matchers.size + 1 until resultBase.matchers.size)
            if (shift !in computeInternalShifts(other) && resultBase.applyWithShift(other, shift) != null)
                return listOf(this, other)

        return listOf(resultBase)
    }

    private fun computeInternalShifts(other: PhonologicalRule): IntRange =
        0..matchers.size - other.matchers.size

    /**
     * @return a PhonologicalRule which is identical to consecutive application
     *  of this and other, if the application of other is restricted to the area
     *  matched by this.
     */
    private fun applyInside(other: PhonologicalRule): List<PhonologicalRule> =
        computeInternalShifts(other).fold(listOf(this)) { prev, shift ->
            prev.flatMap { curRule ->
                curRule.applyWithShift(other, shift)
                    ?.let { (rule, isNarrowed) ->
                        listOfNotNull(curRule.takeIf { isNarrowed }, rule)
                    } ?: prev
            }
        }

    /**
     * @return a PhonologicalRule which is identical to application of this and
     *  then application of other, applied at the point shifted by shift.
     *  If other can't be possibly applied with such shift, return null.
     */
    private fun applyWithShift(other: PhonologicalRule, shift: Int): Pair<PhonologicalRule, Boolean>? {
        // Preceding matchers + the matchers of other applied before the matchers of this start
        val thisSubstitutionsShift = precedingMatchers.size + max(0, -shift)
        val otherSubstitutionsShift = other.precedingMatchers.size + max(0, shift)
        val thisShiftedSubstitutions = createNullPadding(precedingMatchers.size) + substitutions

        val (newMatchers, isNarrowed, isChanged) = unitePhonemeMatchers(
            matchers,
            thisShiftedSubstitutions,
            other.matchers,
            shift
        )
        newMatchers ?:
            return null
        if (!isChanged)
            return null

        val newSubstitutions = unitePhonemeSubstitutions(
            createNullPadding(thisSubstitutionsShift - otherSubstitutionsShift) + substitutions,
            createNullPadding(otherSubstitutionsShift - thisSubstitutionsShift) + other.substitutions
        )
        val newSubstitutionsShift = min(thisSubstitutionsShift, otherSubstitutionsShift)
        val directSubstitutionsCount = newSubstitutions.count { it !is EpenthesisSubstitution }

        return PhonologicalRule(
            newMatchers,
            newSubstitutionsShift,
            newMatchers.size - newSubstitutionsShift - directSubstitutionsCount,
            newSubstitutions,
            allowSyllableStructureChange || other.allowSyllableStructureChange
        ) to isNarrowed
    }

    private fun createNullPadding(size: Int): List<Nothing?> = (1..size)
        .map { null }

    override fun toString() = targetMatchers.joinToString("") +
            " -> ${substitutions.joinToString("")}" +
            " / ${precedingMatchers.joinToString("")}" +
            " _ ${followingMatchers.joinToString("")}" +
            if (allowSyllableStructureChange) "!" else ""
}


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
