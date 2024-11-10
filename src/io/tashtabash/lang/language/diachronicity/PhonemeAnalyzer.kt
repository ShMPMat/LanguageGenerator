package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.category.realization.*
import io.tashtabash.lang.language.derivation.DerivationParadigm
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
    phonemes += derivationParadigm.derivations
        .flatMap { analyzePhonemes(it.affix.templateChange) }
    phonemes += derivationParadigm.compounds
        .flatMap { it.infix.phonemes }
    phonemes += changeParadigm.wordChangeParadigm
        .speechPartChangeParadigms
        .values
        .flatMap { speechPartChangeParadigm ->
            speechPartChangeParadigm.applicators.values.flatMap { applicators ->
                applicators.values.flatMap { analyzePhoneme(it) }
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
    is ModifierPhonemeSubstitution, PassingPhonemeSubstitution, DeletingPhonemeSubstitution -> null
    else -> throw LanguageException("Unknown phoneme substitution '$phonemeSubstitution'")
}


fun analyzePhoneme(categoryApplicator: CategoryApplicator): List<Phoneme> = when (categoryApplicator) {
    is AffixCategoryApplicator -> analyzePhonemes(categoryApplicator.affix.templateChange)
    is ConsecutiveApplicator -> categoryApplicator.applicators.flatMap { analyzePhoneme(it) }
    is FilterApplicator -> categoryApplicator.applicators.flatMap { (applicator, words) ->
            analyzePhoneme(applicator) + words.flatMap { it.toPhonemes() }
        }
    is WordCategoryApplicator -> categoryApplicator.word.toPhonemes()
    is WordReduplicationCategoryApplicator, PassingCategoryApplicator -> listOf()
    else -> throw LanguageException("Unknown category applicator '$categoryApplicator'")
}
