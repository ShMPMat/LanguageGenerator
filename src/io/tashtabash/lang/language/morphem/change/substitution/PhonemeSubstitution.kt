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
    addModifierRegex.matches(substitution) -> ModifierPhonemeSubstitution(
        substitution.drop(2)
            .dropLast(1)
            .split(",")
            .map { PhonemeModifier.valueOf(it) }
            .toSet(),
        setOf(),
        phonemeContainer
    )
    removeModifierRegex.matches(substitution) -> ModifierPhonemeSubstitution(
        setOf(),
        substitution.drop(2)
            .dropLast(1)
            .split(",")
            .map { PhonemeModifier.valueOf(it) }
            .toSet(),
        phonemeContainer
    )
    epenthesisRegex.matches(substitution) -> EpenthesisSubstitution(
        phonemeContainer.getPhoneme(substitution.removePrefix("(").removeSuffix(")"))
    )
    else -> ExactPhonemeSubstitution(phonemeContainer.getPhoneme(substitution))
}

private val addModifierRegex = "\\[\\+.*]".toRegex()
private val removeModifierRegex = "\\[-.*]".toRegex()
private val epenthesisRegex = "\\(.\\)".toRegex()
