package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.morphem.change.ChangeException
import io.tashtabash.lang.language.syntax.ChangeParadigm


fun areChangesValid(lexis: Lexis, changeParadigm: ChangeParadigm): Result<Unit> {
    try {
        for (word in lexis.words)
            changeParadigm.wordChangeParadigm.getAllWordForms(word, true)
    } catch (e: ChangeException) {
        return Result.failure(e)
    }
    return Result.success(Unit)
}
