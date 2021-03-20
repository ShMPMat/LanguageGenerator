package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization
import shmp.lang.language.lexis.Word

abstract class WordCategoryApplicator(
    val applicatorWord: Word,
    type: CategoryRealization
) : AbstractCategoryApplicator(type)