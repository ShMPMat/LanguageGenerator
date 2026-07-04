package io.tashtabash.lang.language.derivation

import io.tashtabash.lang.containers.SemanticsCoreTemplate
import io.tashtabash.lang.containers.toSemanticsCore
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.morphem.Affix
import io.tashtabash.random.randomUnwrappedElement
import kotlin.random.Random


data class Derivation(
    val affix: Affix,
    val derivationClass: DerivationClass,
    val resultSpeechPart: TypedSpeechPart,
    val strength: Double,
    private val categoriesChanger: CategoryChanger
) {
    fun derive(word: Word, lexis: AbstractLexis, random: Random, find: (Meaning) -> SemanticsCoreTemplate): Word? {
        if (word.semanticsCore.changeHistory?.computeAppliedDerivations(lexis)?.contains(this) == true)
            return null

        val applicableTypes = derivationClass.possibilities
            .filter { word.semanticsCore.derivationCluster.typeToCore.containsKey(it.value) }
        val chosenType = randomUnwrappedElement(applicableTypes + makeNoType(1.0 / strength), random)
            ?: return null

        val chosenMeaning = randomUnwrappedElement(
            word.semanticsCore.derivationCluster.typeToCore.getValue(chosenType) + DerivationLink(null, 1.0 / strength),
            random
        ) ?: return null
        val core = find(chosenMeaning)

        return derive(word, core)
    }

    private fun derive(originalWord: Word, derivedCore: SemanticsCoreTemplate): Word? {
        val derivedWord = affix.change(originalWord, listOf(), listOf(derivationClass))
        val newStaticCategories = categoriesChanger.makeStaticCategories(
            listOf(originalWord.semanticsCore),
            resultSpeechPart
        ) ?: return null
        try {
            val newCore = derivedCore.toSemanticsCore(newStaticCategories).let {
                it.copy(
                    speechPart = resultSpeechPart,
                    tags = it.tags + SemanticsTag(derivationClass.name),
                    changeHistory = DerivationHistory(this, SimpleWordPointer(originalWord))
                )
            }
            return derivedWord.copy(semanticsCore = newCore)
        } catch (e: LanguageException) {
            return null // Return null if the resulting word doesn't make sense (i.e. a Perception verb w/ trans tags)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (javaClass != other?.javaClass)
            return false

        other as Derivation

        if (affix.toString() != other.affix.toString())
            return false
        if (derivationClass != other.derivationClass)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = affix.toString().hashCode()
        result = 31 * result + derivationClass.hashCode()
        return result
    }

    override fun toString() = "Class - $derivationClass; $affix; $categoriesChanger; strength = %.2f".format(strength)
}
