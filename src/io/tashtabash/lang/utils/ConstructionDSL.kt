package io.tashtabash.lang.utils


infix fun <E> Boolean.thenTake(expr: () -> E): E? =
    if (this)
        expr()
    else null
