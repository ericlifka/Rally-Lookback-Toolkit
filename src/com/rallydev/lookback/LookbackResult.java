package com.rallydev.lookback;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LookbackResult {

    public String _rallyAPIMajor;
    public String _rallyAPIMinor;
    public List<String> Errors;
    public List<String> Warnings;
    public Map<String, String> ThreadStats;
    public Map<String, Integer> Timings;
    public Map<String, Object> GeneratedQuery;
    public int TotalResultCount;
    public int StartIndex;
    public int PageSize;
    public String ETLDate;
    public List<Map<String, Object>> Results;

    LookbackQuery queryContext;

    LookbackResult validate(LookbackQuery context) {
        if (Errors != null && Errors.size() > 0) {
            throw new LookbackException(Errors.get(0));
        }
        queryContext = context;
        return this;
    }

    public Iterator<Map<String, Object>> getResultsIterator() {
        return Results.iterator();
    }

    public boolean hasWarnings() {
        return Warnings != null && Warnings.size() > 0;
    }

    public boolean hasMorePages() {
        return StartIndex + Results.size() < TotalResultCount;
    }
}
