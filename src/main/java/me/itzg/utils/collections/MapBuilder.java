package me.itzg.utils.collections;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Geoff Bourne
 * @since 3/8/2015
 */
public class MapBuilder<K,V> {

    private Map<K,V> content;

    protected MapBuilder(Map<K, V> content) {
        this.content = content;
    }

    public MapBuilder<K, V> put(K key, V value) {
        content.put(key, value);
        return this;
    }

    public Map<K,V> build() {
        return content;
    }

    public static <k,v> MapBuilder<k,v> startMap() {
        return new MapBuilder<k,v>(new LinkedHashMap<k,v>());
    }
}
