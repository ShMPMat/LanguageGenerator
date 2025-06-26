package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatchers
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.allWithProb
import io.tashtabash.random.withProb


class EndConsonantDrop : MatcherConditionPhonologicalChangeTendency() {
    override fun getNewInstance() = EndConsonantDrop()

    override fun createMatchers(phonemeContainer: PhonemeContainer): List<List<PhonemeMatcher>> =
        listOf(
            createPhonemeMatchers("C$", phonemeContainer)
        )

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            allowSyllableStructureChange = true

            return (createRules("<C> -> - / _ $") allWithProb 3.0) +
                    (createRule("C -> - / _ $") withProb 3.0)
        }
}
