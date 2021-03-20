package shmp.lang.language.syntax.context


data class Context(val explicitContext: ContextValues = setOf(), val implicitContext: ContextValues = setOf()) {
    val allContext = implicitContext + explicitContext
}
