package shmp.lang.utils

import kotlin.reflect.KClass


fun <T : Any> KClass<T>.values() = sealedSubclasses.mapNotNull { it.objectInstance }
fun <T : Any> KClass<T>.valuesSet() = values().toSet()
