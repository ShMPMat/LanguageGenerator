package shmp.language

import shmp.containers.PhonemeContainer
import shmp.language.SpeechPart.*
import shmp.language.category.ChangeParadigm
import shmp.language.phonology.RestrictionsParadigm
import shmp.language.phonology.prosody.StressType

class Language(
    internal val words: List<Word>,
    internal val phonemeContainer: PhonemeContainer,
    internal val stressType: StressType,
    internal val sovOrder: SovOrder,
    internal val numeralSystemBase: NumeralSystemBase,
    internal val restrictionsParadigm: RestrictionsParadigm,
    internal val changeParadigm: ChangeParadigm
) {
    override fun toString(): String {
        return """phonemes:
                 |${phonemeContainer}
                 |Syllable structure: ${words[0].syllableTemplate}
                 |Stress patern: $stressType
                 |Numeral system base: $numeralSystemBase
                 |words:
                 |${words.joinToString { it.toString() + " - " + it.syntaxCore.word }}
                 |${words.joinToString { "${changeParadigm.apply(it)} - ${it.syntaxCore.word}" }}
                 |SOV order: $sovOrder
                 |${changeParadigm}
                 |
                 |
                 |Paradigms elaborated:
                 |
                 |Personal pronoun:
                 |${getParadigmPrinted(this, words.first { it.syntaxCore.word == "_personal_pronoun" })}
                 |
                 |Noun:
                 |${getParadigmPrinted(this, words.first { it.syntaxCore.speechPart == Noun })}
                 |
                 |Verb:
                 |${getParadigmPrinted(this, words.first { it.syntaxCore.speechPart == Verb })}
                 |""".trimMargin()
    }
}