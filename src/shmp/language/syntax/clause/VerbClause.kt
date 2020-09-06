package shmp.language.syntax.clause

import shmp.language.Language
import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.lexis.Word
import shmp.language.syntax.SentenceNode
import shmp.language.syntax.SyntaxException
import shmp.language.syntax.SyntaxRelation


class TransitiveVerbClause(
    val verb: Word,
    val subjectClause: NominalClause,
    val objectClause: NominalClause
): SyntaxClause {
    init {
        if (verb.semanticsCore.speechPart != SpeechPart.Verb)
            throw LanguageException("$verb is not a verb")
        if (verb.semanticsCore.tags.any { it.name == "intrans" })
            throw SyntaxException("$verb in the transitive clause is intransitive")
    }

    override fun toNode(language: Language): SentenceNode {
        val node = verb.toNode(language)
        val obj = objectClause.toNode(language)
        val subj = subjectClause.toNode(language)

        subj.setRelation(SyntaxRelation.Verb, node)
        obj.setRelation(SyntaxRelation.Verb, node)

        node.setRelation(SyntaxRelation.Subject, subj)
        node.setRelation(SyntaxRelation.Object, obj)

        return node
    }
}
