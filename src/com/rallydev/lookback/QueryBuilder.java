package com.rallydev.lookback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

class QueryBuilder {

    Map<String, Object> query;

    QueryBuilder() {
        query = new HashMap<String, Object>();
    }

    void addField(String field, Object value) {
        if (value != null) {
            query.put(field, value);
        }
    }

    void mergeProperties(Map<String, Object> properties) {
        if (properties != null) {
            query.putAll(properties);
        }
    }

    String getQueryJson() {
        return getSerializer()
                .toJson(query);
    }

    private Gson getSerializer() {
        return new GsonBuilder()
                .serializeNulls()
                .create();
    }
}
