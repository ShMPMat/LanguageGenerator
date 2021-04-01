package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.Language
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.lexis.toUnspecified
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

        val node = verb.wordToNode(changeParadigm, UndefinedArranger, SyntaxRelation.Verb)
        val obj = objectClause.toNode(language, random).addThirdPerson()
        val subj = subjectClause.toNode(language, random).addThirdPerson()

        node.setRelationChild(SyntaxRelation.Subject, subj)
        node.setRelationChild(SyntaxRelation.Object, obj)

        return node
    }
}


internal fun SentenceNode.addThirdPerson() =
    if (this.categoryValues.none { it.parentClassName == "Person" })
        this.apply { insertCategoryValue(shmp.lang.language.category.PersonValue.Third) }
    else this
