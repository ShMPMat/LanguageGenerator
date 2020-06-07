package shmp

import shmp.containers.WordBase
import shmp.generator.LanguageGenerator
import shmp.language.Language
import shmp.language.Word
import shmp.language.syntax.Clause
import shmp.language.syntax.Sentence
import shmp.language.syntax.SentenceNode
import shmp.language.syntax.SyntaxRelation
import kotlin.random.Random

fun visualize(language: Language) {
    fun Word.toNode() = SentenceNode(
        this,
        language.sentenceChangeParadigm.wordChangeParadigm.getDefaultState(this).map { it.categoryValue }//TODO filter only Self-Refs
    )

    val subj = language.words.first { it.semanticsCore.word == "mother" }.toNode()
    val verb = language.words.first { it.semanticsCore.word == "have" }.toNode()
    val obj = language.words.first { it.semanticsCore.word == "time" }.toNode()

    subj.relation[SyntaxRelation.Verb] = verb
    verb.relation[SyntaxRelation.Subject] = subj
    verb.relation[SyntaxRelation.Object] = obj
    obj.relation[SyntaxRelation.Verb] = verb

    val testSentence = Sentence(verb)
    println(language.sentenceChangeParadigm.apply(testSentence))
    print("\n\n")

    print(language)
}

fun main() {
    visualize(LanguageGenerator(166).generateLanguage(WordBase().words.size))
}