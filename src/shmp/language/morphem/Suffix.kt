package shmp.language.morphem

class Suffix(override val templateChange: TemplateWordChange) : Affix {
    override val position = Position.End

    init {
        if (templateChange.position != position)
            throw ExceptionInInitializerError("Suffix can change other parts of words")
    }

    override fun toString(): String {
        return "Suffixes: $templateChange"
    }


}