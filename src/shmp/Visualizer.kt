package shmp

import shmp.containers.WordBase
import shmp.generator.LanguageGenerator
import shmp.language.CategoryValue
import shmp.language.Language
import shmp.language.category.CategorySource
import shmp.language.category.NumbersValue
import shmp.language.getClauseAndInfoPrinted
import shmp.language.lexis.Word
import shmp.language.syntax.Sentence
import shmp.language.syntax.SentenceNode
import shmp.language.syntax.SentenceType
import shmp.language.syntax.SyntaxRelation
import kotlin.random.Random

fun visualize(language: Language) {
    fun Word.toNode(presetCategories: List<CategoryValue> = listOf()): SentenceNode {
        val classNames = presetCategories
            .map { it.parentClassName }

        return SentenceNode(
            this,
            language.sentenceChangeParadigm.wordChangeParadigm
                .getDefaultState(this)
                .filter { it.source == CategorySource.SelfStated }
                .map { it.categoryValue }
                .filter { it.parentClassName !in classNames }
                    + presetCategories
        )
    }

    val subjAdj = language.words.first { it.semanticsCore.word == "new" }.toNode()
    val subj = language.words.first { it.semanticsCore.word == "mother" }.toNode(listOf(NumbersValue.Plural))
    val verb = language.words.first { it.semanticsCore.word == "have" }.toNode()
    val objAdj = language.words.first { it.semanticsCore.word == "new" }.toNode()
    val obj = language.words.first { it.semanticsCore.word == "time" }.toNode()

    subjAdj.setRelation(SyntaxRelation.Subject, subj)
    subj.setRelation(SyntaxRelation.Verb, verb)
    subj.setRelation(SyntaxRelation.Definition, subjAdj)
    verb.setRelation(SyntaxRelation.Subject, subj)
    verb.setRelation(SyntaxRelation.Object, obj)
    objAdj.setRelation(SyntaxRelation.Subject, obj)
    obj.setRelation(SyntaxRelation.Verb, verb)
    obj.setRelation(SyntaxRelation.Definition, objAdj)

    val testSentenceMain = Sentence(verb, SentenceType.MainClause)
    val testSentenceQuestion = Sentence(verb, SentenceType.Question)
    println("Main:")
    println(getClauseAndInfoPrinted(language.sentenceChangeParadigm.apply(testSentenceMain, Random(10))))
    println("General question:")
    println(getClauseAndInfoPrinted(language.sentenceChangeParadigm.apply(testSentenceQuestion, Random(10))))
    print("\n\n")

    print(language)
}

fun main() {
    val generator = LanguageGenerator("SupplementFiles", 190)
    val wordAmount = WordBase("SupplementFiles").baseWords.size

    visualize(generator.generateLanguage(wordAmount))
}