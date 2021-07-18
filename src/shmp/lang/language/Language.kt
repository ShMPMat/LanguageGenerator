package shmp.lang.language

import shmp.lang.containers.PhonemeContainer
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.syntax.ChangeParadigm
import shmp.lang.language.derivation.DerivationParadigm
import shmp.lang.language.lexis.Lexis
import shmp.lang.language.phonology.RestrictionsParadigm
import shmp.lang.language.phonology.prosody.StressType


class Language(
    val lexis: Lexis,
    val phonemeContainer: PhonemeContainer,
    val stressType: StressType,
    val restrictionsParadigm: RestrictionsParadigm,
    val derivationParadigm: DerivationParadigm,
    val changeParadigm: ChangeParadigm
) {
    override fun toString() = """phonemes:
     |${phonemeContainer}
     |Syllable structure: ${lexis.words[0].syllableTemplate}
     |Stress pattern: $stressType
     |$lexis
     |words:
     |${lexis.words.joinToString { "${changeParadigm.wordChangeParadigm.apply(it)} - ${it.semanticsCore}" }}
     |
     |$changeParadigm
     |
     |
     |Change paradigms elaborated:
     |
     |Personal pronoun:
     |${getParadigmPrinted(lexis.words.first { it.semanticsCore.hasMeaning("_personal_pronoun") })}
     |
     |Deixis pronoun:
     |${getParadigmPrinted(lexis.words.first { it.semanticsCore.hasMeaning("_deixis_pronoun") })}
     |
     |Noun:
     |${getParadigmPrinted(lexis.words.first { it.semanticsCore.speechPart.type == Noun })}
     |
     |Verb:
     |${getParadigmPrinted(lexis.words.first { it.semanticsCore.speechPart.type == Verb })}
     |
     |Numerals:
     |${getNumeralsPrinted()}
     |
     |$derivationParadigm
     |""".trimMargin()
}
