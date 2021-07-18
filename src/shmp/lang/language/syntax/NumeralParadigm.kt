package shmp.lang.language.syntax

import shmp.lang.language.NumeralSystemBase
import shmp.lang.language.lexis.Lexis
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.syntax.clause.realization.wordToNode
import shmp.lang.language.syntax.clause.translation.SentenceNode


data class NumeralParadigm(val base: NumeralSystemBase) {
    private val manyMeaning: Meaning = "Many"

    fun constructNumeral(n: Int, lexis: Lexis): SentenceNode {
        if (n < 1)
            throw SyntaxException("No numeral for $n")

        return when (base) {
            NumeralSystemBase.Restricted3 -> extractRestricted(n, lexis, 3)
            NumeralSystemBase.Restricted5 -> extractRestricted(n, lexis, 5)
            NumeralSystemBase.Restricted20 -> extractRestricted(n, lexis, 20)
        }
    }

    private fun extractRestricted(n: Int, lexis: Lexis, max: Int) =
        if (n > max) lexis.getWord(manyMeaning).wordToNode(SyntaxRelation.AdNumeral)
        else lexis.getWord(n.toString()).wordToNode(SyntaxRelation.AdNumeral)

    override fun toString() = """
         |Numeral system base: $base
         |""".trimMargin()
}
