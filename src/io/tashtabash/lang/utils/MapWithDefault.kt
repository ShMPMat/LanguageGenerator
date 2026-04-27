package io.tashtabash.lang.utils


class MapWithDefault<K, V>(val default: V, map: Map<K, V> = mapOf()): LinkedHashMap<K, V>(map) {
    override fun get(key: K): V =
        getOrDefault(key, default)

    override fun toString() = "Default: $default" +
            if (isNotEmpty())
                ", ${this.entries}"
            else ""
}
