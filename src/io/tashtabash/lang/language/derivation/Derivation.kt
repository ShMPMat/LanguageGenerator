package io.tashtabash.lang.language.derivation

import io.tashtabash.lang.containers.SemanticsCoreTemplate
import io.tashtabash.lang.containers.toSemanticsCore
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
    fun deriveRandom(word: Word, random: Random, resolver: (Meaning) -> SemanticsCoreTemplate): Word? {
        if (word.semanticsCore.appliedDerivations.contains(this))
            return null

        val applicableTypes = derivationClass.possibilities
            .filter { word.semanticsCore.derivationCluster.typeToCore.containsKey(it.type) }

        val chosenType = randomUnwrappedElement(applicableTypes + makeNoType(1.0 / strength), random)
            ?: return null

        val chosenMeaning = randomUnwrappedElement(
            word.semanticsCore.derivationCluster.typeToCore.getValue(chosenType) + noDerivationLink,
            random
        ) ?: return null
        val core = resolver(chosenMeaning)

        return derive(word, core)
    }

    private fun derive(originalWord: Word, derivedCore: SemanticsCoreTemplate,): Word? {
        val derivedWord = affix.change(originalWord, listOf(), listOf(derivationClass))
        val newDerivations = originalWord.semanticsCore.appliedDerivations + this
        val newStaticCategories = categoriesChanger.makeStaticCategories(
            listOf(originalWord.semanticsCore),
            resultSpeechPart
        ) ?: return null
        val newCore = derivedCore.toSemanticsCore(newStaticCategories).let {
            it.copy(
                tags = it.tags + SemanticsTag(derivationClass.name),
                appliedDerivations = newDerivations,
                changeHistory = DerivationHistory(this, SimpleWordPointer(originalWord))
            )
        }

        return derivedWord.copy(semanticsCore = newCore)
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
