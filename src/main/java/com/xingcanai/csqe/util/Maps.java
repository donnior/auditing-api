package com.xingcanai.csqe.util;

import java.util.HashMap;
import java.util.Map;

public class Maps {
    public static <K, V> Map<K, V> merge(Map<K, V> one, Map<K, V> two){
        Map<K, V> mergedMap = new HashMap<>();
        mergedMap.putAll(one);
        mergedMap.putAll(two);
        return mergedMap;
    }
}
