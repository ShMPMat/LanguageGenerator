package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.arranger.PassingSingletonArranger
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode
import io.tashtabash.lang.language.syntax.arranger.UndefinedArranger
import io.tashtabash.lang.language.syntax.clause.construction.CopulaConstruction
import kotlin.random.Random


abstract class CopulaClause(val copulaType: CopulaConstruction) : SyntaxClause


class VerbalCopulaClause(
    val copula: Word,
    val additionalCategories: SourcedCategoryValues,
    val subject: NominalClause,
    val complement: NominalClause
) : CopulaClause(CopulaConstruction.Verb) {
    init {
        if (copula.semanticsCore.speechPart.type != SpeechPart.Verb)
            throw SyntaxException("$copula is not a verb")
    }

    override fun toNode(language: Language, random: Random): SyntaxNode {
        val node = copula.toNode(Predicate, additionalCategories.map { it.categoryValue }, UndefinedArranger)
        val obj = complement.toNode(language, random).addThirdPerson().apply {
            categoryValues += language.changeParadigm.syntaxLogic.resolveCopulaCase(
                CopulaConstruction.Verb,
                Agent,
                word.semanticsCore.speechPart
            )
        }
        val subj = subject.toNode(language, random).addThirdPerson().apply {
            categoryValues += language.changeParadigm.syntaxLogic.resolveCopulaCase(
                CopulaConstruction.Verb,
                SubjectCompliment,
                word.semanticsCore.speechPart
            )
        }

        node.setRelationChild(Agent, subj)
        node.setRelationChild(Patient, obj)

        return node
    }
}


class ParticleCopulaClause(
    val copula: Word,
    val subject: NominalClause,
    val complement: NominalClause
) : CopulaClause(CopulaConstruction.Particle) {
    init {
        if (copula.semanticsCore.speechPart.type != SpeechPart.Particle)
            throw SyntaxException("$copula is not a particle")
    }

    override fun toNode(language: Language, random: Random): SyntaxNode {
        val obj = complement.toNode(language, random).addThirdPerson().apply {
            categoryValues += language.changeParadigm.syntaxLogic.resolveCopulaCase(
                CopulaConstruction.Particle,
                Agent,
                word.semanticsCore.speechPart
            )
        }
        val subj = subject.toNode(language, random).addThirdPerson().apply {
            categoryValues += language.changeParadigm.syntaxLogic.resolveCopulaCase(
                CopulaConstruction.Particle,
                SubjectCompliment,
                word.semanticsCore.speechPart
            )
        }
        val particle = copula.toNode(CopulaParticle, listOf(), PassingSingletonArranger)

        particle.setRelationChild(Agent, subj)
        particle.setRelationChild(SubjectCompliment, obj)

        return particle
    }
}


class NullCopulaClause(val subject: NominalClause, val complement: NominalClause) : CopulaClause(CopulaConstruction.None) {
    override fun toNode(language: Language, random: Random): SyntaxNode {
        val obj = complement.toNode(language, random).addThirdPerson().apply {
            categoryValues += language.changeParadigm.syntaxLogic.resolveCopulaCase(
                CopulaConstruction.None,
                SubjectCompliment,
                word.semanticsCore.speechPart
            )
        }
        val subj = subject.toNode(language, random).addThirdPerson().apply {
            categoryValues += language.changeParadigm.syntaxLogic.resolveCopulaCase(
                CopulaConstruction.None,
                Agent,
                word.semanticsCore.speechPart
            )
        }

        subj.setRelationChild(SubjectCompliment, obj)

        return subj
    }
}
