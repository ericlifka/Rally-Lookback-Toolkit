package com.rallydev.lookback;

import com.rits.cloning.Cloner;

import java.io.IOException;
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

    LookbackQuery(LookbackResult previousPage, LookbackApi parentApi) {
        this.parentApi = parentApi;
        cloneFields(previousPage.queryContext);
        updateToNextPage();
    }

    void cloneFields(LookbackQuery previousQuery) {
        Cloner cloner = new Cloner();

        find = cloner.deepClone(previousQuery.find);
        sort = cloner.deepClone(previousQuery.sort);
        fields = cloner.deepClone(previousQuery.fields);
        hydrate = cloner.deepClone(previousQuery.hydrate);
        properties = cloner.deepClone(previousQuery.properties);

        isFieldsTrue = previousQuery.isFieldsTrue;
        pagesize = previousQuery.pagesize;
        start = previousQuery.start;
    }

    void updateToNextPage() {
        start += pagesize;
    }

    public LookbackResult execute() {
        try {
            return validateAndRun();
        }
        catch (IOException exception) {
            throw new LookbackException(exception);
        }
    }

    LookbackResult validateAndRun() throws IOException {
        validateQuery();
        return parentApi.executeQuery(this);
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
        QueryBuilder query = new QueryBuilder();
        addParametersToQuery(query);
        return query.getQueryJson();
    }

    void addParametersToQuery(QueryBuilder query) {
        query.addField("find", find);
        query.addField("start", start);
        query.addField("pagesize", pagesize);
        query.addField("fields", fields);
        query.addField("hydrate", hydrate);
        query.addField("sort", sort);
        query.mergeProperties(properties);
    }
}
