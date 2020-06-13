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
            .filter { it.type.matcher.match(word) }

        if (applicableTypes.isEmpty()) return null

        val chosenType = randomElement(applicableTypes, random).type
        val derivedWord = affix.change(word)

        return derivedWord
    }
}

enum class DerivationClass(val possibilities: List<Box>) {
    Diminutive(listOf(Box(Smallness, 1.0), Box(Child, 1.0)))
}

enum class DerivationType(val matcher: WordMatcher) {
    Smallness(ConcatMatcher(SpeechPartMatcher(SpeechPart.Noun))),
    Child(ConcatMatcher(SpeechPartMatcher(SpeechPart.Noun), TagMatcher(SemanticsTag("species"))))
}

data class Box(val type: DerivationType, override val probability: Double): SampleSpaceObject