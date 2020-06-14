package shmp.language.derivation

import shmp.language.SpeechPart
import shmp.language.derivation.DerivationType.*
import shmp.language.lexis.*
import shmp.language.morphem.Affix
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import kotlin.random.Random

class Derivation(private val affix: Affix, val derivationClass: DerivationClass) {
    fun derive(word: Word, random: Random): Word? {
        val applicableTypes = derivationClass.possibilities
            .filter { word.semanticsCore.derivationCluster.typeToCore.containsKey(it.type) }

        if (applicableTypes.isEmpty()) return null

        val chosenType = randomElement(applicableTypes, random).type
        val derivedWord = affix.change(word)
        val newTags = derivedWord.semanticsCore.tags + listOf(SemanticsTag(derivationClass.name))
        val newCore = derivedWord.semanticsCore.copy(tags = newTags)
        val newLexis =

        return derivedWord.copy(semanticsCore = newCore)
    }
}

enum class DerivationClass(val possibilities: List<Box>) {
    Diminutive(listOf(Box(Smallness, 1.0), Box(Child, 1.0)))
}

fun DerivationClass.types() = this.possibilities.map { it.type }

enum class DerivationType(val matcher: WordMatcher) {
    Smallness(ConcatMatcher(SpeechPartMatcher(SpeechPart.Noun))),
    Child(ConcatMatcher(SpeechPartMatcher(SpeechPart.Noun), TagMatcher(SemanticsTag("species"))))
}

data class Box(val type: DerivationType, override val probability: Double): SampleSpaceObject