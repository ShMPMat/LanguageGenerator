package shmp.language.syntax.clause.realization

import shmp.language.SpeechPart
import shmp.language.lexis.Word
import shmp.language.syntax.ChangeParadigm
import shmp.language.syntax.SyntaxException
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.clause.translation.SentenceNode
import shmp.language.syntax.features.WordSyntaxRole
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
            throw SyntaxException("$copula is not a verb")
    }

    override fun toNode(changeParadigm: ChangeParadigm, random: Random): SentenceNode {
        val node = copula.copy(syntaxRole = WordSyntaxRole.Copula)
            .wordToNode(changeParadigm, UndefinedOrderer, SyntaxRelation.Verb)
        val obj = complement.toNode(changeParadigm, random).addThirdPerson()
        val subj = subject.toNode(changeParadigm, random).addThirdPerson()

        node.setRelationChild(SyntaxRelation.Subject, subj)
        node.setRelationChild(SyntaxRelation.SubjectCompliment, obj)

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

        obj.setRelationChild(SyntaxRelation.Subject, subj)
        subj.setRelationChild(SyntaxRelation.SubjectCompliment, obj)

        return subj
    }
}
