package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.arranger.PassingSingletonArranger
import io.tashtabash.lang.language.syntax.clause.translation.SentenceNode
import io.tashtabash.lang.language.syntax.features.CopulaType
import io.tashtabash.lang.language.syntax.arranger.UndefinedArranger
import kotlin.random.Random


abstract class CopulaClause(val copulaType: CopulaType) : SyntaxClause


class VerbalCopulaClause(
    val copula: Word,
    val additionalCategories: SourcedCategoryValues,
    val subject: NominalClause,
    val complement: NominalClause
) : CopulaClause(CopulaType.Verb) {
    init {
        if (copula.semanticsCore.speechPart.type != SpeechPart.Verb)
            throw SyntaxException("$copula is not a verb")
    }

    override fun toNode(language: Language, random: Random): SentenceNode {
        val node = copula.toNode(SyntaxRelation.Verb, additionalCategories.map { it.categoryValue }, UndefinedArranger)
        val obj = complement.toNode(language, random).addThirdPerson().apply {
            addCategoryValues(
                language.changeParadigm.syntaxLogic.resolveCopulaCase(
                    CopulaType.Verb,
                    SyntaxRelation.Agent,
                    word.semanticsCore.speechPart
                )
            )
        }
        val subj = subject.toNode(language, random).addThirdPerson().apply {
            addCategoryValues(
                language.changeParadigm.syntaxLogic.resolveCopulaCase(
                    CopulaType.Verb,
                    SyntaxRelation.SubjectCompliment,
                    word.semanticsCore.speechPart
                )
            )
        }

        node.setRelationChild(SyntaxRelation.Agent, subj)
        node.setRelationChild(SyntaxRelation.Patient, obj)

        return node
    }
}


class ParticleCopulaClause(
    val copula: Word,
    val subject: NominalClause,
    val complement: NominalClause
) : CopulaClause(CopulaType.Particle) {
    init {
        if (copula.semanticsCore.speechPart.type != SpeechPart.Particle)
            throw SyntaxException("$copula is not a particle")
    }

    override fun toNode(language: Language, random: Random): SentenceNode {
        val obj = complement.toNode(language, random).addThirdPerson().apply {
            addCategoryValues(
                language.changeParadigm.syntaxLogic.resolveCopulaCase(
                    CopulaType.Particle,
                    SyntaxRelation.Agent,
                    word.semanticsCore.speechPart
                )
            )
        }
        val subj = subject.toNode(language, random).addThirdPerson().apply {
            addCategoryValues(
                language.changeParadigm.syntaxLogic.resolveCopulaCase(
                    CopulaType.Particle,
                    SyntaxRelation.SubjectCompliment,
                    word.semanticsCore.speechPart
                )
            )
        }
        val particle = copula.toNode(SyntaxRelation.CopulaParticle, listOf(), PassingSingletonArranger)

        particle.setRelationChild(SyntaxRelation.Agent, subj)
        particle.setRelationChild(SyntaxRelation.SubjectCompliment, obj)

        return particle
    }
}


class NullCopulaClause(val subject: NominalClause, val complement: NominalClause) : CopulaClause(CopulaType.None) {
    override fun toNode(language: Language, random: Random): SentenceNode {
        val obj = complement.toNode(language, random).addThirdPerson().apply {
            addCategoryValues(
                language.changeParadigm.syntaxLogic.resolveCopulaCase(
                    CopulaType.None,
                    SyntaxRelation.SubjectCompliment,
                    word.semanticsCore.speechPart
                )
            )
        }
        val subj = subject.toNode(language, random).addThirdPerson().apply {
            addCategoryValues(
                language.changeParadigm.syntaxLogic.resolveCopulaCase(
                    CopulaType.None,
                    SyntaxRelation.Agent,
                    word.semanticsCore.speechPart
                )
            )
        }

        subj.setRelationChild(SyntaxRelation.SubjectCompliment, obj)

        return subj
    }
}
