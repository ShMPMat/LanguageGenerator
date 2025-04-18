package io.tashtabash.lang.language

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.lang.language.syntax.ChangeParadigm
import io.tashtabash.lang.language.derivation.DerivationParadigm
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.phonology.RestrictionsParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType


data class Language(
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
     |${lexis.words.joinToString { "${changeParadigm.wordChangeParadigm.apply(it).unfold()} - ${it.semanticsCore}" }}
     |
     |$changeParadigm
     |
     |
     |Change paradigms elaborated:
     |
     |Personal pronoun:
     |${printParadigm(lexis.words.first { it.semanticsCore.hasMeaning("_personal_pronoun") })}
     |
     |Deixis pronoun:
     |${printParadigm(lexis.words.first { it.semanticsCore.hasMeaning("_deixis_pronoun") })}
     |
     |Noun:
     |${printParadigm(Noun)}
     |
     |Verb:
     |${printParadigm(Verb)}
     |
     |Numerals:
     |${getNumeralsPrinted()}
     |
     |$derivationParadigm
     |""".trimMargin()
}
