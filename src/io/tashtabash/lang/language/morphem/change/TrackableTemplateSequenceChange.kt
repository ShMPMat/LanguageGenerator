package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.derivation.DerivationClass
import io.tashtabash.lang.language.lexis.Word


class TrackableTemplateSequenceChange(changes: List<TemplateChange>): TemplateSequenceChange(changes) {
    val usageStats: MutableList<Int> =
        changes.map { 0 }
            .toMutableList()

    override fun change(word: Word, categoryValues: SourcedCategoryValues, derivationValues: List<DerivationClass>): Word {
        findAppliedChangeIndex(word, categoryValues, derivationValues)
            ?.let { usageStats[it]++ }

        return super.change(word, categoryValues, derivationValues)
    }
}