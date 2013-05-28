package com.rallydev.lookback;

import com.rits.cloning.Cloner;

import java.io.IOException;
import java.util.*;

/**
 * LookbackQuery objects present an interface for configuring a query before executing it.
 * Request a query LookbackQuery object from your LookbackApi object, and configure it
 * as necessary, then use the execute method to run the query. If more pages of data exist,
 * they can be queried for as well with LookbackApi's getQueryForNextPage method.
 *
 *      LookbackQuery query = api.newSnapshotQuery()
 *                              .addFindClause("_TypeHierarchy", "Defect")
 *                              .setStart(1000)
 *                              .setPagesize(1000)
 *                              .sortBy("_ValidFrom")
 *                              .requireFields("ObjectID", "ScheduleState", "_ValidFrom", "_ValidTo")
 *                              .hydrateFields("ScheduleState");
 *
 *      LookbackResult resultSet = query.execute();
 *
 *      if (resultSet.hasMorePages()) {
 *          LookbackQuery nextQuery = api.getQueryForNextPage(resultSet);
 *      }
 */
public class LookbackQuery {

    LookbackApi parentApi;

    Map<String, Object> find;
    Map<String, Integer> sort;
    Set<String> fields;
    Set<String> hydrate;

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

    /**
     * Execute this LookbackQuery as it is configured.
     * @return LookbackResult - a representation of the returned data.
     */
    public LookbackResult execute() {
        try {
            return validateAndRun();
        } catch (IOException exception) {
            throw new LookbackException(exception);
        }
    }

    /**
     * Sets the page size for the query.
     * @param pagesize
     * @return LookbackQuery - Enables method chaining
     */
    public LookbackQuery setPagesize(int pagesize) {
        this.pagesize = pagesize;
        return this;
    }

    /**
     * Sets the start index for the query.
     * @param start
     * @return LookbackQuery - Enables method chaining
     */
    public LookbackQuery setStart(int start) {
        this.start = start;
        return this;
    }

    /**
     * Configures the query to request all fields available for Snapshots.
     * Warning: This is intended for development, pagesize is limited by the LookbackApi
     * when fields is set to True. When transitioning to production please configure queries
     * to request only the fields they need.
     * @return LookbackQuery - Enables method chaining
     */
    public LookbackQuery setFieldsTrue() {
        this.isFieldsTrue = true;
        return this;
    }

    /**
     * Adds fields to the 'fields' parameter of the query. These fields will be included
     * on any Snapshots that have them. This is incompatible with setFieldsTrue, only one can be utilized.
     * A LookbackException will be thrown if both are configured for the same LookbackQuery object.
     * @param requiredFields
     * @return LookbackQuery - Enables method chaining
     */
    public LookbackQuery requireFields(String... requiredFields) {
        if (fields == null) {
            fields = new HashSet<String>();
        }

        for (String f : requiredFields) {
            fields.add(f);
        }

        return this;
    }

    /**
     * Adds a field to the 'sort' parameter of the query as an ascending sort.
     * @param field
     * @return LookbackQuery - Enables method chaining
     */
    public LookbackQuery sortBy(String field) {
        return sortBy(field, 1);
    }

    /**
     * Adds a field to the 'sort' parameter of the query in the specified direction.
     * @param field
     * @param direction - 1 for ascending, -1 for descending
     * @return LookbackQuery - Enables method chaining
     */
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

    /**
     * Adds fields to be hydrated. Hydration replaces rally OID values with human
     * readable values where possible. Hydration is not necessary for querying, but
     * is useful for interpretting snapshot results.
     * @param fields
     * @return LookbackQuery - Enables method chaining
     */
    public LookbackQuery hydrateFields(String... fields) {
        if (hydrate == null) {
            hydrate = new HashSet<String>();
        }

        for (String f : fields) {
            hydrate.add(f);
        }

        return this;
    }

    /**
     * Add a clause to the find parameter. Clauses can be either simple string values
     * or more complex objects:
     *
     *      // Simple clause, represents {"_TypeHierarchy": "HierarchicalRequirement"}
     *      query.addFindClause("_TypeHierarchy", "HierarchicalRequirement");
     *
     *      // More complex clause, represents
     *      // {
     *      //     "$or": [
     *      //         {"_TypeHierarchy": "hierarchicalRequirement"},
     *      //         {"_TypeHierarchy": "Defect"}
     *      //     ]
     *      // }
     *      Map defect = new HashMap();
     *      Map story = new HashMap();
     *      defect.put("_TypeHierarchy", "Defect");
     *      story.put("_TypeHierarchy", "HierarchicalRequirement");
     *
     *      List orClause = new ArrayList(2);
     *      orClause.add(defect);
     *      orClause.add(story);
     *
     *      query.addFindClause("$or", orClause);
     *
     * @param field
     * @param value
     * @return LookbackQuery - Enables method chaining
     */
    public LookbackQuery addFindClause(String field, Object value) {
        if (find == null) {
            find = new HashMap<String, Object>();
        }

        find.put(field, value);
        return this;
    }

    /**
     * Add a query parameter not specified otherwise. This allows for future changes
     * to the lookback api that may require new parameters.
     * @param parameter - parameter name
     * @param value - parameter value
     * @return LookbackQuery - Enables method chaining
     */
    public LookbackQuery addProperty(String parameter, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }

        properties.put(parameter, value);
        return this;
    }

    String getRequestJson() {
        QueryBuilder query = new QueryBuilder();
        addParametersToQuery(query);
        return query.getQueryJson();
    }

    private void cloneFields(LookbackQuery previousQuery) {
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

    private void updateToNextPage() {
        start += pagesize;
    }

    private LookbackResult validateAndRun() throws IOException {
        validateQuery();
        return parentApi.executeQuery(this);
    }

    private void validateQuery() {
        checkFieldsValid();
        checkFindValid();
    }

    private void checkFieldsValid() {
        if (isFieldsTrue && fields != null) {
            throw new LookbackException("Cannot set fields=true and pass required fields");
        }
    }

    private void checkFindValid() {
        if (find == null) {
            throw new LookbackException("Cannot execute query without find");
        }
    }

    private void addParametersToQuery(QueryBuilder query) {
        query.addField("find", find);
        query.addField("start", start);
        query.addField("pagesize", pagesize);
        query.addField("fields", fields);
        query.addField("hydrate", hydrate);
        query.addField("sort", sort);
        query.mergeProperties(properties);
    }
}
