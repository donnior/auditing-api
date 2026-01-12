package com.xingcanai.csqe.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Lists {

    public static <T> void whenNotEmpty(List<T> list, Consumer<List<T>> consumer) {
        if (list == null || list.isEmpty()) return;
        consumer.accept(list);
    }

    public static boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    @Deprecated
    public static <T> List<T> merge(List<T> one, List<T> two) {
        if (one == null) return two;
        if (two == null) return one;
        one.addAll(two);
        return one;
    }

    public static <T> List<T> concat(List<T> one, List<T> two) {
        var list = new ArrayList<T>();
        if (one != null) {
            list.addAll(one);
        }
        if (two != null) {
            list.addAll(two);
        }
        return list;
    }

    public static <T> List<T> concat(T head, List<T> tail) {
        var list = new ArrayList<T>();
        if (head != null) {
            list.add(head);
        }
        if (tail != null) {
            list.addAll(tail);
        }
        return list;
    }

    public static <T> List<T> concat(List<T> head, T tail) {
        var list = new ArrayList<T>();
        if (head != null) {
            list.addAll(head);
        }
        if (tail != null) {
            list.add(tail);
        }
        return list;
    }

}
