package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.Language
import shmp.lang.language.category.CaseValue
import shmp.lang.language.category.TenseValue
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.clause.translation.SentenceNode
import shmp.lang.language.syntax.arranger.UndefinedArranger
import kotlin.random.Random


class TransitiveVerbClause(
    val verb: Word,
    val subjectClause: NominalClause,
    val objectClause: NominalClause
): SyntaxClause {
    init {
        if (verb.semanticsCore.speechPart.type != SpeechPart.Verb)
            throw SyntaxException("$verb is not a verb")
        if (verb.semanticsCore.tags.any { it.name == "intrans" })
            throw SyntaxException("$verb in the transitive clause is intransitive")
    }

    override fun toNode(language: Language, random: Random): SentenceNode {
        val changeParadigm = language.changeParadigm

        val node = verb.wordToNode(UndefinedArranger, SyntaxRelation.Verb)
        val agent = subjectClause.toNode(language, random).addThirdPerson()
        val patient = objectClause.toNode(language, random).addThirdPerson()

        val caseRelevantCategories = verb.categoryValues
            .map { it.categoryValue }
            .filterIsInstance<TenseValue>()
            .toSet()
        agent.categoryValues.removeIf { it is CaseValue }
        agent.categoryValues.addAll(language.changeParadigm.syntaxLogic.resolveVerbCase(
            verb.semanticsCore.speechPart,
            SyntaxRelation.Agent,
            caseRelevantCategories
        ))
        patient.categoryValues.removeIf { it is CaseValue }
        patient.categoryValues.addAll(language.changeParadigm.syntaxLogic.resolveVerbCase(
            verb.semanticsCore.speechPart,
            SyntaxRelation.Patient,
            caseRelevantCategories
        ))

        node.setRelationChild(SyntaxRelation.Agent, agent)
        node.setRelationChild(SyntaxRelation.Patient, patient)

        return node
    }
}


internal fun SentenceNode.addThirdPerson() =
    if (this.categoryValues.none { it.parentClassName == "Person" })
        this.apply { insertCategoryValue(shmp.lang.language.category.PersonValue.Third) }
    else this
