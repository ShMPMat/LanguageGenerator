package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeModifier


interface PhonemeSubstitution {
    fun substitute(phoneme: Phoneme?): Phoneme?
    fun getSubstitutePhoneme(): Phoneme?
}


fun createPhonemeSubstitutions(substitutions: String, phonemeContainer: PhonemeContainer): List<PhonemeSubstitution> {
    var currentPostfix = substitutions
    val resultSubstitutions = mutableListOf<PhonemeSubstitution>()

    while (currentPostfix.isNotEmpty()) {
        val token = if (currentPostfix[0] == '[')
            currentPostfix.takeWhile { it != ']' } + ']'
        else
            currentPostfix.take(1)

        resultSubstitutions += createPhonemeSubstitution(token, phonemeContainer)

        currentPostfix = currentPostfix.drop(token.length)
    }

    return resultSubstitutions
}

fun createPhonemeSubstitution(substitution: String, phonemeContainer: PhonemeContainer) = when {
    substitution == "-" -> DeletingPhonemeSubstitution
    substitution == "_" -> PassingPhonemeSubstitution
    addModifierRegex.matches(substitution) -> AddModifierPhonemeSubstitution(
        substitution.drop(2)
            .dropLast(1)
            .split(",")
            .map { PhonemeModifier.valueOf(it) }
            .toSet(),
        phonemeContainer
    )
    else -> ExactPhonemeSubstitution(phonemeContainer.getPhoneme(substitution))
}

private val addModifierRegex = "\\[\\+.*]".toRegex()
