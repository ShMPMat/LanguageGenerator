package shmp.lang.language

import shmp.lang.language.lexis.Meaning
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.toUnspecified
import shmp.random.SampleSpaceObject


enum class VowelQualityAmount(val amount: Int, override val probability: Double) : SampleSpaceObject {
    Two(2, 4.0),
    Three(3, 30.0),//No actual data
    Four(4, 49.0),//No actual data
    Five(5, 188.0),
    Six(6, 100.0),
    Seven(7, 60.0),//No actual data
    Eight(8, 31.0),//No actual data
    Nine(9, 20.0),//No actual data
    Ten(10, 10.0),//No actual data
    Eleven(11, 6.0),//No actual data
    Twenty(12, 4.0),//No actual data
    Thirteen(13, 2.0)
}

enum class NumeralSystemBase(override val probability: Double) : SampleSpaceObject {
    Decimal(125.0),
    Vigesimal(20.0),
    VigesimalTill100(22.0),
    SixtyBased(5.0),
    ExtendedBodyPartSystem(4.0),
    Restricted3(6.0),
    Restricted5(6.0),
    Restricted20(6.0)
}

enum class CategoryRealization {
    NewWord,
    PrefixSeparateWord,
    SuffixSeparateWord,
    Prefix,
    Suffix,
    Reduplication,
    Passing
}

interface CategoryValue {
    val semanticsCore: SemanticsCore
    val parentClassName: String
    val alias: String
}

open class AbstractCategoryValue(
    semanticsCore: SemanticsCore,
    override val parentClassName: String,
    override val alias: String
) : CategoryValue {
    override val semanticsCore = semanticsCore.copy(staticCategories = setOf(this))

    constructor(parentClassName: String, speechPart: SpeechPart, meaning: Meaning, shortName: String) : this(
        SemanticsCore(meaning, speechPart.toUnspecified()),
        parentClassName,
        shortName
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractCategoryValue

        if (parentClassName != other.parentClassName) return false
        if (semanticsCore.meaningCluster != other.semanticsCore.meaningCluster) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parentClassName.hashCode()
        result = 31 * result + semanticsCore.meaningCluster.hashCode()
        return result
    }
}

typealias CategoryValues = List<CategoryValue>
