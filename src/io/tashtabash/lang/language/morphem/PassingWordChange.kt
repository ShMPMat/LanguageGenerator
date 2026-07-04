package io.tashtabash.lang.language.morphem

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.derivation.DerivationClass
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.change.WordChange


object PassingWordChange : WordChange {
    override val position = null

    override fun change(
        word: Word,
        categoryValues: SourcedCategoryValues,
        derivationValues: List<DerivationClass>
    ): Word {
        val newCategoryValues = word.categoryValues + categoryValues
        // Inject the values in the roots instead of creating a mock Ø morpheme
        val newMorphemes = word.morphemes.map { morpheme ->
            if (morpheme.isRoot)
                // Filter the original values and not vice versa, there's a good chance there are none
                morpheme.copy(
                    categoryValues = categoryValues + morpheme.categoryValues.filter { v -> v !in categoryValues },
                    derivationValues = derivationValues + morpheme.derivationValues
                )
            else
                morpheme
        }

        return word.copy(categoryValues = newCategoryValues, morphemes = newMorphemes)
    }

    override fun toString() =
        "Conversion"
}
