package shmp.language.derivation

import shmp.containers.toSemanticsCore
import shmp.language.SpeechPart
import shmp.language.derivation.DerivationType.*
import shmp.language.lexis.*
import shmp.language.morphem.Affix
import shmp.random.SampleSpaceObject
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
        val derivedWord = affix.change(word)
        val newTags = derivedWord.semanticsCore.tags + listOf(SemanticsTag(derivationClass.name))
        val newDerivations = word.semanticsCore.appliedDerivations + listOf(this)
        var newCore = word.semanticsCore.derivationCluster.typeToCore.getValue(chosenType)
            .template.toSemanticsCore(word.semanticsCore.staticCategories, random)
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

enum class DerivationClass(val possibilities: List<Box>, val speechPart: SpeechPart) {
    Diminutive(listOf(Box(Smallness, 1.0), Box(Young, 1.0)), SpeechPart.Noun),
    Augmentative(listOf(Box(Big, 3.0), Box(Old, 1.0)), SpeechPart.Noun)
}

enum class DerivationType {
    Smallness,
    Young,

    Big,
    Old,

    Passing
}

data class Box(val type: DerivationType, override val probability: Double): SampleSpaceObject

val noType = listOf(Box(Passing, 1.0))