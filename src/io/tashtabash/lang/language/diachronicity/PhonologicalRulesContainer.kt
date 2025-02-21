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
        "s -> h / _ \$",
        "s -> h / \$ _ ",
        "p -> f / _ ",
        "p -> f / _ \$",
        "p -> f / \$ _ ",
        "[+Stop] -> ʔ / _ ",
        "[+Stop] -> ʔ / _ $",
        "[+Stop] -> ʔ / $ _ ",
        "[+Stop] -> ʔ / $ _ V",
        "[+Stop] -> ʔ / V _ $",
        "[+Stop] -> ʔ / V _ V",

        // Vowel quality transfers
        "(V[-Long])h -> [+Long]- / _ ",
        "(V[-Long])x -> [+Long]- / _ ",
        "(V[-Long])ə -> [+Long]- / _ ",
        // Vowel quality transfers: Nasalization
        "(V[-Nasalized])(C[+Nasal]) -> [+Nasalized]- / _ ",
        "(V[-Nasalized])(C[+Nasal]) -> [+Nasalized]- / _ $",
        "(V[-Nasalized])(C[+Nasal]) -> [+Nasalized]- / $ _ ",
        "(C[+Nasal])(V[-Nasalized]) -> -[+Nasalized] / _ ",
        "(C[+Nasal])(V[-Nasalized]) -> -[+Nasalized] / _ $",
        "(C[+Nasal])(V[-Nasalized]) -> -[+Nasalized] / $ _ ",
        "(V[-Nasalized])(C[+Nasal]) -> [+Nasalized]_ / _ ",
        "(V[-Nasalized])(C[+Nasal]) -> [+Nasalized]_ / _ $",
        "(V[-Nasalized])(C[+Nasal]) -> [+Nasalized]_ / $ _ ",
        "(C[+Nasal])(V[-Nasalized]) -> _[+Nasalized] / _ ",
        "(C[+Nasal])(V[-Nasalized]) -> _[+Nasalized] / _ $",
        "(C[+Nasal])(V[-Nasalized]) -> _[+Nasalized] / $ _ ",
        // Consonant quality transfers
        "[-Voiced] -> [+Voiced] / _ V",
        "[-Voiced] -> [+Voiced] / $ _ V",
        "[-Voiced] -> [+Voiced] / V _ V",
        "[-Voiced] -> [+Voiced] / V _ $",
        "[-Voiced] -> [+Voiced] / V _ ",
        "[+Voiced] -> [-Voiced] / _ $",
        "[+Voiced] -> [-Voiced] / $ _ ",
    ).map { createPhonologicalRule(it, phonemeContainer) }

    val possibleSyllableStructureChangeRules = listOf(
        // Vowel deletion
        "V -> - / \$C _ CV",
        "V -> - / VC _ C\$",
        "V -> - / C _ \$",
        // Consonant deletion
        "C -> - / _ \$",
        "CC -> _- / _ \$",
        "VC -> _- / _ \$",
        "CC -> -_ / \$ _ ",
        "CV -> -_ / \$ _ ",
        // Vowel epenthesis
        "C -> _(ə) / _ C ",
        "C -> _(a) / _ C ",
        "C -> _(i) / _ C ",
        // Consonant epenthesis
        "V -> _(ʔ) / _ V ",
        "V -> _(ɦ) / _ V ",
        "V -> _(h) / _ V ",
    ).map { createPhonologicalRule(it, phonemeContainer) }
        .flatMap { listOf(it, it.copy(allowSyllableStructureChange = true)) }

    return PhonologicalRulesContainer(
        noSyllableStructureChangeRules + possibleSyllableStructureChangeRules
    )
}
