package com.rallydev.lookback;

import com.google.gson.Gson;

import java.util.*;

public class LookbackQuery {

    LookbackApi parentApi;

    Map<String, Object> find;
    Map<String, Integer> sort;
    List<String> fields;
    List<String> hydrate;

    Map<String, Object> properties;

    boolean isFieldsTrue = false;
    int pagesize = 20000;
    int start = 0;

    LookbackQuery(LookbackApi parentApi) {
        this.parentApi = parentApi;
    }

    public void execute() {
        validateQuery();
        String requestJson = buildRequestJson();
        parentApi.executeQuery(requestJson);
    }

    public LookbackQuery setPagesize(int pagesize) {
        this.pagesize = pagesize;
        return this;
    }

    public LookbackQuery setStart(int start) {
        this.start = start;
        return this;
    }

    public LookbackQuery setFieldsTrue() {
        this.isFieldsTrue = true;
        return this;
    }

    public LookbackQuery requireFields(String... requiredFields) {
        if (fields == null) {
            fields = new ArrayList<String>();
        }

        for (String f : requiredFields) {
            if (!fields.contains(f)) {
                fields.add(f);
            }
        }

        return this;
    }

    public LookbackQuery sortBy(String field) {
        return sortBy(field, 1);
    }

    public LookbackQuery sortBy(String field, int direction) {
        if (direction != 1 && direction != -1) {
            throw new LookbackException("Sort only supports values of 1 or -1");
        }

        if (sort == null) {
            sort = new HashMap<String, Integer>();
        }

        sort.put(field, direction);
        return this;
    }


    public LookbackQuery hydrateFields(String... fields) {
        if (hydrate == null) {
            hydrate = new ArrayList<String>();
        }

        for (String f : fields) {
            if (!hydrate.contains(f)) {
                hydrate.add(f);
            }
        }

        return this;
    }

    public LookbackQuery addProperty(String field, Object property) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }

        properties.put(field, property);
        return this;
    }

    public LookbackQuery addFindClause(String field, Object value) {
        if (find == null) {
            find = new HashMap<String, Object>();
        }

        find.put(field, value);
        return this;
    }

    void validateQuery() {
        checkFieldsValid();
        checkFindValid();
    }

    void checkFieldsValid() {
        if (isFieldsTrue && fields != null) {
            throw new LookbackException("Cannot set fields=true and pass required fields");
        }
    }

    void checkFindValid() {
        if (find == null) {
            throw new LookbackException("Cannot execute query without find");
        }
    }

    String buildRequestJson() {
        Map<String, Object> request = new HashMap<String, Object>();

        request.put("find", find);
        request.put("start", start);
        request.put("pagesize", pagesize);
        request.put("fields", fields);
        request.put("hydrate", hydrate);
        request.put("sort", sort);

        if (properties != null) {
            request.putAll(properties);
        }

        return new Gson().toJson(request);
    }
}
