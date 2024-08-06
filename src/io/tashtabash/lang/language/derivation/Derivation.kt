package io.tashtabash.lang.language.derivation

import io.tashtabash.lang.containers.WordBase
import io.tashtabash.lang.containers.toSemanticsCore
import io.tashtabash.lang.language.lexis.SemanticsTag
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.lexis.noDerivationLink
import io.tashtabash.lang.language.morphem.Affix
import io.tashtabash.random.randomUnwrappedElement
import kotlin.random.Random


class Derivation(
    private val affix: Affix,
    val dClass: DerivationClass,
    val resultSpeechPart: TypedSpeechPart,
    val strength: Double,
    private val categoriesChanger: CategoryChanger
) {
    fun derive(word: Word, allWords: WordBase, random: Random): Word? {
        if (word.semanticsCore.appliedDerivations.contains(this))
            return null

        val applicableTypes = dClass.possibilities
            .filter { word.semanticsCore.derivationCluster.typeToCore.containsKey(it.type) }

        val chosenType = randomUnwrappedElement(applicableTypes + makeNoType(1.0 / strength), random)
            ?: return null

        val chosenMeaning = randomUnwrappedElement(
            word.semanticsCore.derivationCluster.typeToCore.getValue(chosenType) + noDerivationLink,
            random
        ) ?: return null

        val derivedWord = affix.change(word, listOf())
        val newDerivations = word.semanticsCore.appliedDerivations + this
        val newStaticCategories = categoriesChanger.makeStaticCategories(
            listOf(word.semanticsCore),
            resultSpeechPart
        ) ?: return null
        val newCore = allWords.allWords.first { it.word == chosenMeaning }
            .toSemanticsCore(newStaticCategories).let {
                it.copy(
                    tags = it.tags + SemanticsTag(dClass.name),
                    appliedDerivations = newDerivations,
                    changeHistory = DerivationHistory(this, word)
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
        if (dClass != other.dClass)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = affix.toString().hashCode()
        result = 31 * result + dClass.hashCode()
        return result
    }

    override fun toString() = "Class - $dClass; $affix; $categoriesChanger; strength = %.2f".format(strength)
}
