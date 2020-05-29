package shmp.language.category.realization

import shmp.language.CategoryRealization
import shmp.language.Word

abstract class WordCategoryApplicator(
    val applicatorWord: Word,
    type: CategoryRealization
) : AbstractCategoryApplicator(type)