package shmp.language.syntax.clause.realization

import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.category.PersonValue
import shmp.language.syntax.ChangeParadigm
import shmp.language.lexis.Word
import shmp.language.syntax.SyntaxException
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.clause.translation.SentenceNode
import shmp.language.syntax.orderer.UndefinedOrderer
import kotlin.random.Random


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

    override fun toNode(changeParadigm: ChangeParadigm, random: Random): SentenceNode {
        val node = verb.wordToNode(changeParadigm, UndefinedOrderer)
        val obj = objectClause.toNode(changeParadigm, random).addThirdPerson()
        val subj = subjectClause.toNode(changeParadigm, random).addThirdPerson()

        subj.setRelation(SyntaxRelation.Verb, node, false)
        obj.setRelation(SyntaxRelation.Verb, node, false)

        node.setRelation(SyntaxRelation.Subject, subj, true)
        node.setRelation(SyntaxRelation.Object, obj, true)

        return node
    }
}


private fun SentenceNode.addThirdPerson() =
    if (this.categoryValues.none { it.parentClassName == "Person" })
        this.withCategoryValue(PersonValue.Third)
    else this.copy()
