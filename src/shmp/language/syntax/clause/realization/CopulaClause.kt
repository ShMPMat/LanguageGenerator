package shmp.language.syntax.clause.realization

import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.lexis.Word
import shmp.language.syntax.ChangeParadigm
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.clause.translation.SentenceNode
import shmp.language.syntax.orderer.UndefinedOrderer
import kotlin.random.Random


abstract class CopulaClause(val topType: SyntaxRelation): SyntaxClause


class VerbalCopulaClause(
    val copula: Word,
    val subject: NominalClause,
    val complement: NominalClause
) : CopulaClause(SyntaxRelation.Verb) {
    init {
        if (copula.semanticsCore.speechPart != SpeechPart.Verb)
            throw LanguageException("$copula is not a verb")
    }

    override fun toNode(changeParadigm: ChangeParadigm, random: Random): SentenceNode {
        val node = copula.wordToNode(changeParadigm, UndefinedOrderer)
        val obj = complement.toNode(changeParadigm, random).addThirdPerson()
        val subj = subject.toNode(changeParadigm, random).addThirdPerson()

        subj.setRelation(SyntaxRelation.Verb, node, false)
        obj.setRelation(SyntaxRelation.Verb, node, false)

        node.setRelation(SyntaxRelation.Subject, subj, true)
        node.setRelation(SyntaxRelation.SubjectCompliment, obj, true)

        return node
    }
}


class NullCopulaClause(
    val subject: NominalClause,
    val complement: NominalClause
) : CopulaClause(SyntaxRelation.Subject) {
    override fun toNode(changeParadigm: ChangeParadigm, random: Random): SentenceNode {
        val obj = complement.toNode(changeParadigm, random).addThirdPerson()
        val subj = subject.toNode(changeParadigm, random).addThirdPerson()

        obj.setRelation(SyntaxRelation.Subject, subj, true)
        subj.setRelation(SyntaxRelation.SubjectCompliment, obj, true)

        return subj
    }
}
