package shmp.language.derivation

import shmp.containers.toSemanticsCore
import shmp.language.lexis.DerivationHistory
import shmp.language.lexis.SemanticsTag
import shmp.language.lexis.Word
import shmp.language.lexis.noDerivationLink
import shmp.language.morphem.Affix
import shmp.random.randomUnwrappedElement
import kotlin.random.Random


class Derivation(private val affix: Affix, val dClass: DerivationClass, private val categoriesChanger: CategoryChanger) {
    fun derive(word: Word, random: Random): Word? {
        if (word.semanticsCore.appliedDerivations.contains(this))
            return null

        val applicableTypes = dClass.possibilities
            .filter { word.semanticsCore.derivationCluster.typeToCore.containsKey(it.type) }

        val chosenType = randomUnwrappedElement(applicableTypes + noType, random)
            ?: return null

        val chosenSemantics = randomUnwrappedElement(
            word.semanticsCore.derivationCluster.typeToCore.getValue(chosenType) + noDerivationLink,
            random
        )
            ?: return null

        val derivedWord = affix.change(word)
        val newTags = derivedWord.semanticsCore.tags + listOf(SemanticsTag(dClass.name))
        val newDerivations = word.semanticsCore.appliedDerivations + listOf(this)
        val newStaticCategories = categoriesChanger.getNewStaticCategories(listOf(word.semanticsCore))
        val newCore = chosenSemantics.toSemanticsCore(newStaticCategories, random)
            .copy(
                tags = newTags,
                appliedDerivations = newDerivations,
                derivationHistory = DerivationHistory(this, word)
            )

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

    override fun toString() = "Class - $dClass; $affix; $categoriesChanger"
}
