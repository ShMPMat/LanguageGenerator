package io.tashtabash.lang.utils


data class MapWithDefault<K, V>(val default: V, val map: Map<K, V> = mapOf()): LinkedHashMap<K, V>() {
    override fun get(key: K): V =
        getOrDefault(key, default)

    override fun toString() = "Default: $default" +
            if (map.isNotEmpty())
                ", $map"
            else ""
}
