package io.tashtabash.lang.language.analyzer

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.category.realization.*
import io.tashtabash.lang.language.derivation.DerivationParadigm
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.morphem.change.TemplateChange
import io.tashtabash.lang.language.morphem.change.TemplateSequenceChange
import io.tashtabash.lang.language.morphem.change.TemplateSingleChange
import io.tashtabash.lang.language.morphem.change.substitution.*
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.syntax.ChangeParadigm


// Returns Phonemes actually used by a lang
fun analyzePhonemes(language: Language): PhonemeContainer =
    analyzePhonemes(language.lexis, language.derivationParadigm, language.changeParadigm)

fun analyzePhonemes(
    lexis: Lexis,
    derivationParadigm: DerivationParadigm,
    changeParadigm: ChangeParadigm
): PhonemeContainer {
    val phonemes = mutableSetOf<Phoneme>()

    phonemes += lexis.words
        .flatMap { it.toPhonemes() }
    phonemes += derivationParadigm.compounds
        .flatMap { it.infix.phonemes }
    phonemes += derivationParadigm.derivations
        .flatMap { analyzePhonemes(it.affix.templateChange) }
    phonemes += changeParadigm.wordChangeParadigm
        .speechPartChangeParadigms
        .values
        .flatMap { speechPartChangeParadigm ->
            speechPartChangeParadigm.applicators.values.flatMap { applicators ->
                applicators.values.flatMap { analyzePhoneme(it, ImmutablePhonemeContainer(phonemes.toList())) }
            }
        }

    return ImmutablePhonemeContainer(phonemes.toList())
}


private fun analyzePhonemes(templateChange: TemplateChange): List<Phoneme> = when (templateChange) {
    is TemplateSingleChange -> templateChange.affix.map { it.exactPhoneme } +
            templateChange.matchedPhonemesSubstitution.mapNotNull { analyzePhoneme(it) }
    is TemplateSequenceChange -> templateChange.changes.flatMap { analyzePhonemes(it) }
    else -> throw LanguageException("Unknown template change '$templateChange'")
}

private fun analyzePhoneme(phonemeSubstitution: PhonemeSubstitution): Phoneme? = when (phonemeSubstitution) {
    is ExactPhonemeSubstitution -> phonemeSubstitution.exactPhoneme
    is EpenthesisSubstitution -> phonemeSubstitution.epenthesisPhoneme
    is ModifierPhonemeSubstitution, PassingPhonemeSubstitution, DeletingPhonemeSubstitution -> null
    else -> throw LanguageException("Unknown phoneme substitution '$phonemeSubstitution'")
}

fun analyzePhonemes(phonologicalRule: PhonologicalRule, possiblePhonemes: PhonemeContainer): List<Phoneme> =
    phonologicalRule.substitutionPairs.flatMap { (m, s) ->
        possiblePhonemes.phonemes.flatMap {
            if (m?.match(it) != false)
                if (s is ModifierPhonemeSubstitution)
                    listOfNotNull(s.substituteOrNull(it))
                else
                    s.substitute(it)
            else
                listOf()
        }
    }

fun analyzePhoneme(categoryApplicator: CategoryApplicator, phonemeContainer: PhonemeContainer): List<Phoneme> = when (categoryApplicator) {
    is AffixCategoryApplicator -> analyzePhonemes(categoryApplicator.affix.templateChange)
    is ConsecutiveApplicator -> categoryApplicator.applicators.flatMap { analyzePhoneme(it, phonemeContainer) }
    is FilterApplicator -> categoryApplicator.applicators.flatMap { (applicator, words) ->
            analyzePhoneme(applicator, phonemeContainer) + words.flatMap { it.toPhonemes() }
        }
    is WordCategoryApplicator -> categoryApplicator.word.toPhonemes()
    is WordReduplicationCategoryApplicator, PassingCategoryApplicator -> listOf()
    else -> throw LanguageException("Unknown category applicator '$categoryApplicator'")
}
