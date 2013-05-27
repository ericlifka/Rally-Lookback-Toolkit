package com.rallydev.lookback;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * LookbackResult objects represent a result set of data from the Lookback API.
 */
public class LookbackResult {

    /**
     * The major version of the Lookback API used to make this query.
     */
    public String _rallyAPIMajor;

    /**
     * The minor version of the Lookback API used to make this query.
     */
    public String _rallyAPIMinor;

    /**
     * List of Errors returned by the Lookback API.
     */
    public List<String> Errors;

    /**
     * List of Warnings returned by the Lookback API.
     */
    public List<String> Warnings;

    /**
     * Statistics returned by the API about the query's execution.
     */
    public Map<String, String> ThreadStats;

    /**
     * Timing information returned by the API about the query's execution.
     */
    public Map<String, Integer> Timings;

    /**
     * Information about the query Lookback API generated from the query specification.
     */
    public Map<String, Object> GeneratedQuery;

    /**
     * Total Results found by Lookback API for the specified query.
     */
    public int TotalResultCount;

    /**
     * The index into the total results or this result set, as
     * specified in the query configuration.
     */
    public int StartIndex;

    /**
     * The number of Snapshots as requested by the query configuration.
     */
    public int PageSize;

    /**
     * The ETLDate at time of query execution.
     */
    public String ETLDate;

    /**
     * The list of snapshot objects, as represented by map objects.
     */
    public List<Map<String, Object>> Results;

    /**
     * Get an iterator for the Lookback Snapshots contained in this result set.
     * @return Iterator
     */
    public Iterator<Map<String, Object>> getResultsIterator() {
        return Results.iterator();
    }

    /**
     * Checks for any warnings returned from the Lookback API.
     * @return boolean
     */
    public boolean hasWarnings() {
        return Warnings != null && Warnings.size() > 0;
    }

    /**
     * Checks if there are any more pages of data following this result set.
     * @return boolean
     */
    public boolean hasMorePages() {
        return StartIndex + PageSize < TotalResultCount;
    }

    LookbackQuery queryContext;

    LookbackResult validate(LookbackQuery context) {
        if (Errors != null && Errors.size() > 0) {
            throw new LookbackException(Errors);
        }
        queryContext = context;
        return this;
    }
}
