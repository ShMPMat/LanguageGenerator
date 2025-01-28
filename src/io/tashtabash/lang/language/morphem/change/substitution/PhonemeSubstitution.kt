package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeModifier


interface PhonemeSubstitution {
    fun substitute(phoneme: Phoneme?): List<Phoneme>

    val isOriginalPhonemeChanged: Boolean
        get() = true

    // Create a new PhonemeSubstitution which is equal in effect to
    // the sequential application of this instance after the other
    operator fun times(other: PhonemeSubstitution): PhonemeSubstitution
}


fun createPhonemeSubstitutions(substitutions: String, phonemeContainer: PhonemeContainer): List<PhonemeSubstitution> {
    var currentPostfix = substitutions
    val resultSubstitutions = mutableListOf<PhonemeSubstitution>()

    while (currentPostfix.isNotEmpty()) {
        val token = if (currentPostfix[0] == '[')
            currentPostfix.takeWhile { it != ']' } + ']'
        else if (currentPostfix[0] == '(')
            currentPostfix.takeWhile { it != ')' } + ')'
        else if (currentPostfix[0] == '|')
             // Take a multi-char
            '|' + currentPostfix.drop(1).takeWhile { it != '|' } + '|'
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
    modifierRegex.matches(substitution) -> {
        val modifierTokens = substitution.drop(1)
            .dropLast(1)
            .split(",")

        ModifierPhonemeSubstitution(
            modifierTokens.filter { it[0] == '+' }
                .map { PhonemeModifier.valueOf(it.drop(1)) }
                .toSet(),
            modifierTokens.filter { it[0] == '-' }
                .map { PhonemeModifier.valueOf(it.drop(1)) }
                .toSet(),
            phonemeContainer
        )
    }
    epenthesisRegex.matches(substitution) -> EpenthesisSubstitution(
        phonemeContainer.getPhoneme(substitution.removePrefix("(").removeSuffix(")"))
    )
    else -> ExactPhonemeSubstitution(phonemeContainer.getPhoneme(substitution.trim('|')))
}

private val modifierRegex = "\\[.*]".toRegex()
private val epenthesisRegex = "\\(.\\)".toRegex()
