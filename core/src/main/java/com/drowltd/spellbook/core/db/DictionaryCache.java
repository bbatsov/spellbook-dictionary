package com.drowltd.spellbook.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author iivalchev
 */
public class DictionaryCache {

    private int cacheSize = -1;
    private final Map<SDictionary, Map<String, Integer>> dictCache = new LinkedHashMap<SDictionary, Map<String, Integer>>();
    private final Map<SDictionary, Map<String, Integer>> ratingsCache = new LinkedHashMap<SDictionary, Map<String, Integer>>();

    public DictionaryCache() {
    }

    public DictionaryCache(int cacheSize) {
        setCacheSize(cacheSize);
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public final void setCacheSize(int cacheSize) {
        if (cacheSize < -1) {
            this.cacheSize = -1;
        } else {
            this.cacheSize = cacheSize;
        }
        ensureCacheSize(dictCache);
        ensureCacheSize(ratingsCache);
    }

    private void ensureCacheSize(Map<SDictionary, Map<String, Integer>> cache) {
        assert cache != null : "cache is null";

        if (cache.size() <= cacheSize || cacheSize == -1) {
            return;
        }

        final Iterator<SDictionary> iterator = cache.keySet().iterator();


        for (int size = cache.size(); size > cacheSize && iterator.hasNext(); --size) {
            iterator.next();
            iterator.remove();
        }
    }

    private boolean canAddToCache(Map<SDictionary, Map<String, Integer>> cache) {
        assert cache != null : "cache is null";

        if (cacheSize == -1) {
            return true;
        }

        return cache.size() < cacheSize;
    }

    public void invalidateDictCache(SDictionary dictionary) {
        invalidateCache(dictionary, dictCache);
    }

    public void invalidateRatingsCache(SDictionary dictionary) {
        invalidateCache(dictionary, ratingsCache);
    }

    private void invalidateCache(SDictionary dictionary, Map<SDictionary, Map<String, Integer>> cache) {
        assert cache != null : "cache is null";

        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }

        cache.remove(dictionary);
    }

    public List<String> getWordsList(SDictionary dictionary) {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }
        final Map<String, Integer> wordsMap = dictCache.get(dictionary);

        if (wordsMap == null) {
            return null;
        }

        return new ArrayList<String>(wordsMap.keySet());
    }

    public Map<String, Integer> getWordsMap(SDictionary dictionary) {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }

        return dictCache.get(dictionary);

    }

    public Map<String, Integer> getRatingMap(SDictionary dictionary) {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }

        Map<String, Integer> dictMap = dictCache.get(dictionary);
        Map<String, Integer> ratingsMap = ratingsCache.get(dictionary);

        int size = 0;

        if (dictMap != null) {
            size += dictMap.size();
        }
        if (ratingsMap != null) {
            size += ratingsMap.size();
        }


        if (size == 0) {
            return null;
        }

        Map<String, Integer> map = new HashMap<String, Integer>(size);

        if (dictMap != null) {
            map.putAll(dictMap);
        }
        if (ratingsMap != null) {
            map.putAll(ratingsMap);
        }

        return map;
    }

    public void addDictionary(SDictionary dictionary, Map<String, Integer> words) {
        addToCache(dictionary, words, dictCache);
    }

    public void addRatings(SDictionary dictionary, Map<String, Integer> words) {
        addToCache(dictionary, words, ratingsCache);
    }

    private void addToCache(SDictionary dictionary, Map<String, Integer> words, Map<SDictionary, Map<String, Integer>> cache) {
        assert cache != null : "cache is null";

        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }

        if (words == null || words.isEmpty()) {
            throw new IllegalArgumentException("words is empty or null");
        }

        if (canAddToCache(cache) || cache.containsKey(dictionary)) {
            cache.put(dictionary, words);
        }
    }
}
