package shmp.lang.language.syntax.arranger

import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import kotlin.random.Random


object UndefinedArranger : Arranger {
    override fun <E> order(pairs: List<Pair<SyntaxRelation, E>>, random: Random) =
        throw SyntaxException("The order is undefined")
}
