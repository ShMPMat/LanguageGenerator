package shmp.lang.language.category.value

import shmp.lang.language.lexis.*


open class AbstractCategoryValue(
    semanticsCore: SemanticsCore,
    override val parentClassName: String,
    override val alias: String
) : CategoryValue {
    override val semanticsCore = semanticsCore.copy(staticCategories = setOf(this))

    constructor(parentClassName: String, meaning: Meaning, shortName: String, speechPart: SpeechPart = SpeechPart.Particle) : this(
        parentClassName,
        meaning,
        shortName,
        speechPart.toDefault()
    )

    constructor(parentClassName: String, meaning: Meaning, shortName: String, speechPart: TypedSpeechPart) : this(
        SemanticsCore(meaning, speechPart),
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

    override fun toString() = this::class.simpleName.let { name ->
        if (name == "AbstractCategoryValue" || name == null)
            alias
        else name
    }
}
