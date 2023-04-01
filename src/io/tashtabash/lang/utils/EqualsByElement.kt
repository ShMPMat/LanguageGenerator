package io.tashtabash.lang.utils


infix fun <E> List<E>.equalsByElement(other: List<E>) = size == other.size && containsAll(other)

infix fun <E> List<E>.notEqualsByElement(other: List<E>) = !(this equalsByElement other)
