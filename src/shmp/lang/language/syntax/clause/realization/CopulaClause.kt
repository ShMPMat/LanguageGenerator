package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.Language
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.arranger.PassingSingletonArranger
import shmp.lang.language.syntax.clause.translation.SentenceNode
import shmp.lang.language.syntax.features.CopulaType
import shmp.lang.language.syntax.features.WordSyntaxRole
import shmp.lang.language.syntax.arranger.UndefinedArranger
import kotlin.random.Random


abstract class CopulaClause(val topType: SyntaxRelation, val copulaType: CopulaType) : SyntaxClause


class VerbalCopulaClause(
    val copula: Word,
    val subject: NominalClause,
    val complement: NominalClause
) : CopulaClause(SyntaxRelation.Verb, CopulaType.Verb) {
    init {
        if (copula.semanticsCore.speechPart.type != SpeechPart.Verb)
            throw SyntaxException("$copula is not a verb")
    }

    override fun toNode(language: Language, random: Random): SentenceNode {
        val node = copula.copy(syntaxRole = WordSyntaxRole.Copula)
            .wordToNode(language.changeParadigm, UndefinedArranger, SyntaxRelation.Verb)
        val obj = complement.toNode(language, random).addThirdPerson()
        val subj = subject.toNode(language, random).addThirdPerson()

        node.setRelationChild(SyntaxRelation.Agent, subj)
        node.setRelationChild(SyntaxRelation.Patient, obj)

        return node
    }
}


class ParticleCopulaClause(
    val copula: Word,
    val subject: NominalClause,
    val complement: NominalClause
) : CopulaClause(SyntaxRelation.Agent, CopulaType.Particle) {
    init {
        if (copula.semanticsCore.speechPart.type != SpeechPart.Particle)
            throw SyntaxException("$copula is not a particle")
    }

    override fun toNode(language: Language, random: Random): SentenceNode {
        val obj = complement.toNode(language, random).addThirdPerson()
        val subj = subject.toNode(language, random).addThirdPerson()
        val particle = copula.copy(syntaxRole = WordSyntaxRole.Copula).wordToNode(
            language.changeParadigm,
            PassingSingletonArranger,
            SyntaxRelation.CopulaParticle
        )

        subj.setRelationChild(SyntaxRelation.CopulaParticle, particle)
        subj.setRelationChild(SyntaxRelation.SubjectCompliment, obj)

        return subj
    }
}


class NullCopulaClause(
    val subject: NominalClause,
    val complement: NominalClause
) : CopulaClause(SyntaxRelation.Agent, CopulaType.None) {
    override fun toNode(language: Language, random: Random): SentenceNode {
        val obj = complement.toNode(language, random).addThirdPerson()
        val subj = subject.toNode(language, random).addThirdPerson()

        subj.setRelationChild(SyntaxRelation.SubjectCompliment, obj)

        return subj
    }
}
