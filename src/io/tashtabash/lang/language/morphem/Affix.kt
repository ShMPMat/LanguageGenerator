package io.tashtabash.lang.language.morphem

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.derivation.DerivationClass
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.change.TemplateChange
import io.tashtabash.lang.language.morphem.change.WordChange


interface Affix : WordChange {
    val templateChange: TemplateChange

    override fun change(
        word: Word,
        categoryValues: SourcedCategoryValues,
        derivationValues: List<DerivationClass>
    ): Word =
        templateChange.change(word, categoryValues, derivationValues)
}
