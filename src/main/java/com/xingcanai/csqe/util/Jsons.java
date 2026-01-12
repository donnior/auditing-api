package com.xingcanai.csqe.util;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

public class Jsons {
    public static <T> T safeRead(String json, String path) {
        if (json == null) {
            return null;
        }
        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        return JsonPath.using(conf).parse(json).read(path);
    }

    public static <T> T safeRead(Object object, String path) {
        if (object == null) {
            return null;
        }
        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        return JsonPath.using(conf).parse(object).read(path);
    }

    @SuppressWarnings("unchecked")
    public static <T> T safeRead(Map<String, Object> json, String path) {
        if (json == null) {
            return null;
        }
        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        return (T) JsonPath.using(conf).parse(json).read(path);
    }

    public static String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
