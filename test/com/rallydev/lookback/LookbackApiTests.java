package com.rallydev.lookback;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Iterator;

public class LookbackApiTests {

    LookbackApi api;
    String username = "username";
    String password = "password";
    String workspace = "41529001";

    @Before
    public void setUp() {
        api = new LookbackApi()
                .setCredentials(username, password)
                .setWorkspace(workspace);
    }

    @Test
    public void makeQuery() {
        LookbackResult result = api.newSnapshotQuery()
                .addFindClause("_TypeHierarchy", -51038)
                .addFindClause("Children", null)
                .addFindClause("_ItemHierarchy", new BigInteger("5103028089"))
                .execute();

        Gson gson = new Gson();
        Iterator iterator = result.getResultsIterator();
        while (iterator.hasNext()) {
            System.out.println(gson.toJson(iterator.next()));
        }
    }

    @Test
    public void makePagedQuery() {
        LookbackResult result = api.newSnapshotQuery()
                .addFindClause("_TypeHierarchy", -51038)
                .addFindClause("Children", null)
                .addFindClause("_ItemHierarchy", new BigInteger("5103028089"))
                .setPagesize(200)
                .execute();

        int totalResults = result.TotalResultCount;
        int resultCount = result.Results.size();
        int queryCount = 1;

        while (result.hasMorePages()) {
            result = api.getQueryForNextPage(result).execute();
            resultCount += result.Results.size();
            queryCount++;
        }

        System.out.println("TotalResultCount: " + totalResults);
        System.out.println("Accumulated Results: " + resultCount);
        System.out.println("Queries Made: " + queryCount);
    }

    @Test
    public void makeInlineQuery() {
        Iterator resultIterator = new LookbackApi()
                .setCredentials(username, password)
                .setWorkspace(workspace)
                .newSnapshotQuery()
                .addFindClause("_TypeHierarchy", -51038)
                .addFindClause("Children", null)
                .addFindClause("_ItemHierarchy", new BigInteger("5103028089"))
                .execute()
                .getResultsIterator();
    }
}
