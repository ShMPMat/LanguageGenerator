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

    subjAdj.relation[SyntaxRelation.Subject] = subj
    subj.relation[SyntaxRelation.Verb] = verb
    subj.relation[SyntaxRelation.Definition] = subjAdj
    verb.relation[SyntaxRelation.Subject] = subj
    verb.relation[SyntaxRelation.Object] = obj
    objAdj.relation[SyntaxRelation.Subject] = obj
    obj.relation[SyntaxRelation.Verb] = verb
    obj.relation[SyntaxRelation.Definition] = objAdj

    val testSentence = Sentence(verb)
    val sentenceClause = language.sentenceChangeParadigm.apply(testSentence, Random(1))
    println(getClauseAndInfoPrinted(sentenceClause))
    print("\n\n")

    print(language)
}

fun main() {
    visualize(LanguageGenerator(189).generateLanguage(WordBase().words.size))
}