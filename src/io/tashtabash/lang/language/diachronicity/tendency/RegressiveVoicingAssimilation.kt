package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatchers
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.withProb


class RegressiveVoicingAssimilation : MatcherConditionPhonologicalChangeTendency() {
    override fun getNewInstance() = RegressiveVoicingAssimilation()

    override fun createMatchers(phonemeContainer: PhonemeContainer): List<List<PhonemeMatcher>> =
        listOf(
            createPhonemeMatchers("(C[+Voiced])(C[-Voiced])", phonemeContainer),
            createPhonemeMatchers("(C[-Voiced])(C[+Voiced])", phonemeContainer)
        )

    override val defaultRetentionChance = 0.8

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return listOf(
                createRule("(C[+Voiced]) -> [-Voiced] / _ (C[-Voiced])") withProb 0.1,
                createRule("(C[-Voiced]) -> [+Voiced] / _ (C[+Voiced])") withProb 0.1
            )
        }
}
