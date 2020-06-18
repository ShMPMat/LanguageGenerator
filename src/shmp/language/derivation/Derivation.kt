package shmp.language.derivation

import shmp.containers.toSemanticsCore
import shmp.language.derivation.DerivationType.Passing
import shmp.language.lexis.DerivationLink
import shmp.language.lexis.SemanticsTag
import shmp.language.lexis.Word
import shmp.language.lexis.noDerivationLink
import shmp.language.morphem.Affix
import shmp.random.randomElement
import kotlin.random.Random

class Derivation(private val affix: Affix, private val derivationClass: DerivationClass) {
    fun derive(word: Word, random: Random): Word? {

        if (word.semanticsCore.appliedDerivations.contains(this))
            return null

        val applicableTypes = derivationClass.possibilities
            .filter { word.semanticsCore.derivationCluster.typeToCore.containsKey(it.type) }

        if (applicableTypes.isEmpty()) return null

        val chosenType = randomElement(applicableTypes + noType, random).type
        if (chosenType == Passing)
            return null
        val chosenSemantics = randomElement(
            word.semanticsCore.derivationCluster.typeToCore.getValue(chosenType) + noDerivationLink,
            random
        )
        if (chosenSemantics.template == null)
            return null

        val derivedWord = affix.change(word)
        val newTags = derivedWord.semanticsCore.tags + listOf(SemanticsTag(derivationClass.name))
        val newDerivations = word.semanticsCore.appliedDerivations + listOf(this)
        var newCore = chosenSemantics.template.toSemanticsCore(word.semanticsCore.staticCategories, random)
        newCore = newCore.copy(tags = newTags, appliedDerivations = newDerivations)

        return derivedWord.copy(semanticsCore = newCore)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Derivation

        if (affix.toString() != other.affix.toString()) return false
        if (derivationClass != other.derivationClass) return false

        return true
    }

    override fun hashCode(): Int {
        var result = affix.toString().hashCode()
        result = 31 * result + derivationClass.hashCode()
        return result
    }

    override fun toString() = "Class - $derivationClass; $affix"
}
