package io.tashtabash.lang.language.lexis

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.derivation.CompoundHistory
import io.tashtabash.lang.language.derivation.DerivationHistory
import io.tashtabash.lang.language.syntax.features.CopulaType
import io.tashtabash.lang.language.syntax.features.QuestionMarker


data class Lexis(
    override val words: List<Word>,
    private val copula: WordMap<CopulaType>,
    val questionMarker: WordMap<QuestionMarker>
): AbstractLexis() {
    val size: Int
        get() = words.size

    fun getWord(meaning: Meaning) = getWordOrNull(meaning)
        ?: throw LanguageException("No word with meaning '$meaning' in Language")

    fun getBySpeechPart(speechPart: TypedSpeechPart) = words
        .filter { it.semanticsCore.speechPart == speechPart }

    fun getCopulaWord(type: CopulaType) = copula[type]
        ?: throw LanguageException("No copula in Language for type $type")

    fun getQuestionMarkerWord(type: QuestionMarker) = questionMarker[type]
        ?: throw LanguageException("No question marker in Language for type $type")

    fun getWordOrNull(meaning: Meaning) = words
        .firstOrNull { it.semanticsCore.hasMeaning(meaning) }

    // Assumes that the word order is the same
    fun shift(newWords: List<Word>): Lexis {
        if (newWords.size != words.size)
            throw LanguageException("Can't shift words: the size has been changed: ${words.size} to ${newWords.size}")

        val newCopulas = copula.mapValues { (_, word) ->
            val i = words.indexOf(word)
            newWords[i]
        }
        val newQuestionMarkers = questionMarker.mapValues { (_, word) ->
            val i = words.indexOf(word)
            newWords[i]
        }

        return Lexis(newWords, newCopulas, newQuestionMarkers)
    }

    // Turn all Word pointers into IndexWordPointer, allowing quick
    // shift(newWords) when the order of the words hasn't been changed
    fun reifyPointers(): Lexis =
        copy(words = words.map { reifyPointers(it) })

    private fun reifyPointers(word: Word): Word {
        val newChangeHistory = when (val oldChangeHistory = word.semanticsCore.changeHistory) {
            is CompoundHistory -> oldChangeHistory.copy(previous = oldChangeHistory.previous.map { reifyPointer(it) })
            is DerivationHistory -> oldChangeHistory.copy(previous = reifyPointer(oldChangeHistory.previous) )
            null -> null
            else -> throw LanguageException("Cannot reify WordPointers for $oldChangeHistory")
        }

        return word.copy(semanticsCore = word.semanticsCore.copy(changeHistory = newChangeHistory))
    }

    private fun reifyPointer(pointer: WordPointer): IndexWordPointer = when (pointer) {
        is IndexWordPointer -> pointer
        is SimpleWordPointer -> {
            val i = words.indexOf(pointer.word)
            if (i == -1) {
                throw LanguageException("Cannot reify WordPointer $pointer: no word '${pointer.word}' found")
            }

            IndexWordPointer(i)
        }
        else -> throw LanguageException("Cannot reify WordPointer $pointer")
    }

    fun computeHistory(word: Word): String {
        return word.semanticsCore.changeHistory?.printHistory(word, this)
            ?: "No derivations"
    }

    override fun toString() = """
        |copula: $copula
        |question marker: $questionMarker
        |word roots:
        |${words.joinToString { it.toString() + " - " + it.semanticsCore }}
    """.trimMargin()
}

typealias WordMap<E> = Map<E, Word>
