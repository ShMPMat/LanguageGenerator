package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.category.realization.AffixCategoryApplicator
import io.tashtabash.lang.language.category.realization.CategoryApplicator
import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.morphem.Affix
import io.tashtabash.lang.language.morphem.Prefix
import io.tashtabash.lang.language.morphem.Suffix


fun Language.removeUnusedRules(): Language {
    val trackableLanguage = injectChangeTrackers(this)
    for (word in trackableLanguage.lexis.words)
        trackableLanguage.changeParadigm.wordChangeParadigm.getAllWordForms(word, true)

    return removeUnusedChanges(trackableLanguage)
}

fun injectChangeTrackers(language: Language): Language =
    language.copy(
        changeParadigm = language.changeParadigm.mapApplicators(::injectChangeTrackers)
    )

fun injectChangeTrackers(categoryApplicator: CategoryApplicator): CategoryApplicator =
    when (categoryApplicator) {
        is AffixCategoryApplicator -> AffixCategoryApplicator(
            injectChangeTrackers(categoryApplicator.affix),
            categoryApplicator.type
        )
        else -> categoryApplicator
    }

fun injectChangeTrackers(affix: Affix): Affix = when (affix) {
    is Prefix -> {
        Prefix(injectChangeTrackers(affix.templateChange))
    }
    is Suffix -> {
        Suffix(injectChangeTrackers(affix.templateChange))
    }
    else -> throw LanguageException("Unknown affix '$affix'")
}

fun injectChangeTrackers(templateChange: TemplateChange): TemplateChange =
    when (templateChange) {
        is TemplateSequenceChange -> TrackableTemplateSequenceChange(templateChange.changes)
        else -> templateChange
    }

fun removeUnusedChanges(language: Language): Language =
    language.copy(
        changeParadigm = language.changeParadigm.mapApplicators(::removeUnusedChanges)
    )

fun removeUnusedChanges(categoryApplicator: CategoryApplicator): CategoryApplicator =
    when (categoryApplicator) {
        is AffixCategoryApplicator -> {
            val clearedChange = removeUnusedChanges(categoryApplicator.affix.templateChange)

            if (clearedChange is TemplateSequenceChange && clearedChange.changes.isEmpty())
                PassingCategoryApplicator
            else {
                val changedAffix = when (categoryApplicator.affix) {
                    is Prefix -> Prefix(clearedChange)
                    is Suffix -> Suffix(clearedChange)
                    else -> throw LanguageException("Unknown affix '${categoryApplicator.affix}'")
                }
                AffixCategoryApplicator(
                    changedAffix,
                    categoryApplicator.type
                )
            }
        }
        else -> categoryApplicator
    }

fun removeUnusedChanges(templateChange: TemplateChange): TemplateChange =
    when (templateChange) {
        is TrackableTemplateSequenceChange -> {
            val changes = templateChange.changes
                .filterIndexed { i, _ -> templateChange.usageStats[i] > 0 }

            if (changes.size == 1)
                changes[0]
            else
                TemplateSequenceChange(changes)
        }
        else -> templateChange
    }
