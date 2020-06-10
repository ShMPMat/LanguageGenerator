package shmp

import shmp.containers.WordBase
import shmp.generator.LanguageGenerator
import shmp.language.Language
import shmp.language.getClauseAndInfoPrinted
import shmp.language.getClauseInfoPrinted
import shmp.language.lexis.Word
import shmp.language.syntax.Sentence
import shmp.language.syntax.SentenceNode
import shmp.language.syntax.SyntaxRelation

fun visualize(language: Language) {
    fun Word.toNode() = SentenceNode(
        this,
        language.sentenceChangeParadigm.wordChangeParadigm.getDefaultState(this).map { it.categoryValue }//TODO filter only Self-Refs
    )

    val subjAdj = language.words.first { it.semanticsCore.word == "new" }.toNode()
    val subj = language.words.first { it.semanticsCore.word == "mother" }.toNode()
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
    val sentenceClause = language.sentenceChangeParadigm.apply(testSentence)
    println(getClauseAndInfoPrinted(sentenceClause))
    print("\n\n")

    print(language)
}

fun main() {
    visualize(LanguageGenerator(183).generateLanguage(WordBase().words.size))
}