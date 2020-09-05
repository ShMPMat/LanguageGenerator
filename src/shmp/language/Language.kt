package shmp.language

import shmp.containers.PhonemeContainer
import shmp.language.SpeechPart.*
import shmp.language.category.paradigm.SentenceChangeParadigm
import shmp.language.derivation.DerivationParadigm
import shmp.language.lexis.Meaning
import shmp.language.lexis.Word
import shmp.language.phonology.RestrictionsParadigm
import shmp.language.phonology.prosody.StressType

class Language(
    internal val words: List<Word>,
    internal val phonemeContainer: PhonemeContainer,
    internal val stressType: StressType,
    internal val numeralSystemBase: NumeralSystemBase,
    internal val restrictionsParadigm: RestrictionsParadigm,
    internal val derivationParadigm: DerivationParadigm,
    internal val sentenceChangeParadigm: SentenceChangeParadigm
) {
    fun getWord(meaning: Meaning) = words
        .first { it.semanticsCore.hasMeaning(meaning) }

    override fun toString(): String {
        return """phonemes:
         |${phonemeContainer}
         |Syllable structure: ${words[0].syllableTemplate}
         |Stress patern: $stressType
         |Numeral system base: $numeralSystemBase
         |words:
         |${words.joinToString { it.toString() + " - " + it.semanticsCore }}
         |${words.joinToString { "${sentenceChangeParadigm.wordChangeParadigm.apply(it)} - ${it.semanticsCore}" }}
         |
         |
         |$sentenceChangeParadigm
         |
         |
         |Change paradigms elaborated:
         |
         |Personal pronoun:
         |${getParadigmPrinted(this, words.first { it.semanticsCore.hasMeaning("_personal_pronoun") })}
         |
         |Noun:
         |${getParadigmPrinted(this, words.first { it.semanticsCore.speechPart == Noun })}
         |
         |Verb:
         |${getParadigmPrinted(this, words.first { it.semanticsCore.speechPart == Verb })}
         |
         |
         |$derivationParadigm
         |""".trimMargin()
    }
}