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
        val applicableTypes = derivationClass.possibilities
            .filter { word.semanticsCore.derivationCluster.typeToCore.containsKey(it.type) }

        if (applicableTypes.isEmpty()) return null

        val chosenType = randomElement(applicableTypes + noType, random).type
        if (chosenType == Passing)
            return null
        val derivedWord = affix.change(word)
        val newTags = derivedWord.semanticsCore.tags + listOf(SemanticsTag(derivationClass.name))
        var newCore = word.semanticsCore.derivationCluster.typeToCore.getValue(chosenType)
            .template.toSemanticsCore(word.semanticsCore.staticCategories, random)
        newCore = newCore.copy(tags = newTags)

        return derivedWord.copy(semanticsCore = newCore)
    }
}

enum class DerivationClass(val possibilities: List<Box>, val speechPart: SpeechPart) {
    Diminutive(listOf(Box(Smallness, 1.0), Box(Child, 1.0)), SpeechPart.Noun)
}

enum class DerivationType() {
    Smallness,
    Child,

    Passing
}

data class Box(val type: DerivationType, override val probability: Double): SampleSpaceObject

val noType = listOf(Box(Passing, 1.0))