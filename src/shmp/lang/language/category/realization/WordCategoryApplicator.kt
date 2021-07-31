package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.sequence.LatchType


abstract class WordCategoryApplicator(
    val word: Word,
    val latch: LatchType,
    type: CategoryRealization
) : AbstractCategoryApplicator(type)
