package shmp.lang.generator.util

open class GeneratorException(message: String) : Exception(message)

class DataConsistencyException(message: String) : GeneratorException(message)

