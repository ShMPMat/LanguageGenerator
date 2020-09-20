package shmp.language.syntax.clause.realization

import shmp.language.SpeechPart
import shmp.language.lexis.Word
import shmp.language.syntax.ChangeParadigm
import shmp.language.syntax.SyntaxException
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.clause.translation.SentenceNode
import shmp.language.syntax.features.CopulaType
import shmp.language.syntax.features.WordSyntaxRole
import shmp.language.syntax.arranger.UndefinedArranger
import kotlin.random.Random


abstract class CopulaClause(val topType: SyntaxRelation, val copulaType: CopulaType) : SyntaxClause


class VerbalCopulaClause(
    val copula: Word,
    val subject: NominalClause,
    val complement: NominalClause
) : CopulaClause(SyntaxRelation.Verb, CopulaType.Verb) {
    init {
        if (copula.semanticsCore.speechPart != SpeechPart.Verb)
            throw SyntaxException("$copula is not a verb")
    }

    override fun toNode(changeParadigm: ChangeParadigm, random: Random): SentenceNode {
        val node = copula.copy(syntaxRole = WordSyntaxRole.Copula)
            .wordToNode(changeParadigm, UndefinedArranger, SyntaxRelation.Verb)
        val obj = complement.toNode(changeParadigm, random).addThirdPerson()
        val subj = subject.toNode(changeParadigm, random).addThirdPerson()

        node.setRelationChild(SyntaxRelation.Subject, subj)
        node.setRelationChild(SyntaxRelation.SubjectCompliment, obj)

        return node
    }
}


class ParticleCopulaClause(
    val copula: Word,
    val subject: NominalClause,
    val complement: NominalClause
) : CopulaClause(TODO(), CopulaType.Particle) {
    init {
        if (copula.semanticsCore.speechPart != SpeechPart.Particle)
            throw SyntaxException("$copula is not a particle")
    }

    override fun toNode(changeParadigm: ChangeParadigm, random: Random): SentenceNode {
        TODO()
//        val node = copula.copy(syntaxRole = WordSyntaxRole.Copula).wordToNode(
//            changeParadigm,
//            changeParadigm.wordOrder.optionalOrders.getValue(),
//            SyntaxRelation.Verb
//        )
//        val obj = complement.toNode(changeParadigm, random).addThirdPerson()
//        val subj = subject.toNode(changeParadigm, random).addThirdPerson()
//
//        node.setRelationChild(SyntaxRelation.Subject, subj)
//        node.setRelationChild(SyntaxRelation.SubjectCompliment, obj)
//
//        return node
    }
}


class NullCopulaClause(
    val subject: NominalClause,
    val complement: NominalClause
) : CopulaClause(SyntaxRelation.Subject, CopulaType.None) {
    override fun toNode(changeParadigm: ChangeParadigm, random: Random): SentenceNode {
        val obj = complement.toNode(changeParadigm, random).addThirdPerson()
        val subj = subject.toNode(changeParadigm, random).addThirdPerson()

        subj.setRelationChild(SyntaxRelation.SubjectCompliment, obj)

        return subj
    }
}
