package io.tashtabash.lang.language.morphem.change


abstract class TemplateChange : WordChange {
    abstract fun mirror(): TemplateChange
}
