package shmp.generator

open class GeneratorException(message: String) : Exception(message)

class DataConsistencyException(message: String) : GeneratorException(message)

