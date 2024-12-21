package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.PhonemeContainer


data class PhonologicalRulesContainer(val phonologicalRules: List<PhonologicalRule>)


fun createDefaultRules(phonemeContainer: PhonemeContainer): PhonologicalRulesContainer {
    val noSyllableStructureChangeRules = listOf(
        // Vowel shifts
        "a -> o / _ ",
        "o -> a / _ ",
        "ə -> o / _ ",
        "ɨ -> o / _ ",
        // Vowel reduction
        "V -> ə / _ ",
        "V -> ə / _ $",
        "V -> ə / $ _ ",
        "(V{-Stress}) -> ə / _ ",
        "(V{-Stress}) -> ə / _ $",
        "(V{-Stress}) -> ə / $ _ ",
        // Consonant shifts
        "d -> g / _ n",
        "s -> h / _ ",

        // Vowel quality transfers
        "(V[-Long])h -> [+Long]- / _ ",
        "(V[-Long])x -> [+Long]- / _ ",
        "(V[-Long])ə -> [+Long]- / _ ",
        // Consonant quality transfers
        "[-Voiced] -> [+Voiced] / _ V",
        "[-Voiced] -> [+Voiced] / $ _ V",
        "[-Voiced] -> [+Voiced] / V _ V",
        "[-Voiced] -> [+Voiced] / V _ $",
        "[-Voiced] -> [+Voiced] / V _ ",
        "[+Voiced] -> [-Voiced] / _ $",
    ).map { createPhonologicalRule(it, phonemeContainer) }

    val possibleSyllableStructureChangeRules = listOf(
        // Vowel deletion
        "V -> - / \$C _ CV",
        "V -> - / VC _ C\$",
        "V -> - / C _ \$",
        // Consonant deletion
        "C -> - / _ \$",
        "C -> - / C _ \$",
        "C -> - / V _ \$",
    ).map { createPhonologicalRule(it, phonemeContainer) }
        .flatMap { listOf(it, it.copy(allowSyllableStructureChange = true)) }

    return PhonologicalRulesContainer(
        noSyllableStructureChangeRules + possibleSyllableStructureChangeRules
    )
}
