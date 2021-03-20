package shmp.lang.language

import shmp.lang.containers.PhonemeContainer
import shmp.lang.language.SpeechPart.*
import shmp.lang.language.syntax.ChangeParadigm
import shmp.lang.language.derivation.DerivationParadigm
import shmp.lang.language.lexis.Lexis
import shmp.lang.language.phonology.RestrictionsParadigm
import shmp.lang.language.phonology.prosody.StressType


class Language(
    internal val lexis: Lexis,
    internal val phonemeContainer: PhonemeContainer,
    internal val stressType: StressType,
    internal val numeralSystemBase: NumeralSystemBase,
    internal val restrictionsParadigm: RestrictionsParadigm,
    internal val derivationParadigm: DerivationParadigm,
    internal val changeParadigm: ChangeParadigm
) {
    override fun toString(): String {
        return """phonemes:
         |${phonemeContainer}
         |Syllable structure: ${lexis.words[0].syllableTemplate}
         |Stress patern: $stressType
         |Numeral system base: $numeralSystemBase
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
         |${getParadigmPrinted(this, lexis.words.first { it.semanticsCore.hasMeaning("_personal_pronoun") })}
         |
         |Noun:
         |${getParadigmPrinted(this, lexis.words.first { it.semanticsCore.speechPart == Noun })}
         |
         |Verb:
         |${getParadigmPrinted(this, lexis.words.first { it.semanticsCore.speechPart == Verb })}
         |
         |
         |$derivationParadigm
         |""".trimMargin()
    }
}
