# Rally Lookback API Toolkit #

This toolkit provides an interface for interacting with Rally's Lookback API. Documentation for Rally's Lookback API can be found [here](https://rally1.rallydev.com/analytics/doc)

Maven support coming soon. To use this toolkit in your project, download the jar [here](https://github.com/ericlifka/Rally-Lookback-Toolkit/blob/master/out/artifacts/lbapi_rest_toolkit_jar/lbapi-rest-toolkit.jar?raw=true) and add it to your project. All dependencies are included.

To get started, create an instance of LookbackApi and configure it with your Rally credentials and workspace information:

    LookbackApi lookbackApi = new LookbackApi();
    lookbackApi.setCredentials("myusername", "mypassword");
    lookbackApi.setWorkspace("myworkspace");

Next, ask the LookbackApi object for a new query:

    LookbackQuery query = lookbackApi.newSnapshotQuery();

The LookbackQuery object provides functions for setting up the parameters to your query.

Add query clauses with `query.addFindClause(field, value)`. To query for Defects in a project, add two clauses to the query:

    query.addFindClause("_TypeHierarchy", "Defect");
    query.addFindClause("Project", 1234); // replace 1234 with your project OID.

Find clauses can also be complex, simply build up representational objects and then add them as clauses:

    Map greaterThanInProgress = new HashMap();
    greaterThanInProgress.put("$gt", "In-Progress");
    query.addFindClause("ScheduleState", greaterThanInProgress);

LookbackQuery also provides for modifying all of the other aspects of a query, such as pagesize, start, fields, sort, and hydration. All of the objects in the toolkit support chaining for easier specification:

    query.setPagesize(200)                      // set pagesize to 200 instead of the default 20k
            .setStart(200)                      // ask for the second page of data
            .requireFields("ScheduleState",     // A useful set of fields for defects, add any others you may want
                           "ObjectID",
                           "PlanEstimate",
                           "_ValidFrom",
                           "_ValidTo")
            .sortBy("_ValidFrom")               // _ValidFrom is a useful way to order snapshots, it's also the default, so this is unnecessary
            .hydrateFields("ScheduleState");    // ScheduleState will come back as an OID if it doesn't get hydrated



Once the query is configured it can be executed via `query.execute()` which returns a LookbackResult containing the snapshot data:

    LookbackResult resultSet = query.execute();

If anything goes wrong with executing the query, such as an authentication exception, a LookbackException will be raised, which is a runtime exception. Any errors returned by the Lookback API will also be raised as runtime exceptions. The Lookback API will return warnings for certain issues that don't stop the request. The LookbackResult contains these warnings, and they can be checked for:

    if (resultSet.hasWarnings()) {
        // check warnings
    }

The data in a LookbackResult can be accessed directly via it's fields, or via an iterator:

    int resultCount = resultSet.Results.size();
    Map firstSnapshot = resultSet.Results.get(0);

    Iterator iterator = resultSet.getResultsIterator();
    while (iterator.hasNext()) {
        Map snapshot = iterator.next();
    }

A LookbackResult can also tell if there are more pages of data available and the LookbackApi object can automatically generate queries for the next page of a result set:

    while (resultSet.hasMorePages()) {
        LookbackQuery nextQuery = lookbackApi.getQueryForNextPage(resultSet);
        LookbackResult moreResults = nextQuery.execute();
        doSomethingWithSnapshots(moreResults);
    }

Due to the chained nature of the api, one off queries can be made all in one go:

    Iterator resultIterator =
        new LookbackApi()
            .setCredentials(username, password)
            .setWorkspace(workspace)
            .newSnapshotQuery()
                .addFindClause("_TypeHierarchy", -51038)
                .addFindClause("Children", null)
                .addFindClause("_ItemHierarchy", new BigInteger("5103028089"))
                .execute()
                    .getResultsIterator();

One quirk in dealing with Rally data from Java is dealing with OIDs, which are integers, but mcuh larger than Java's max size for integers. The BigInteger class as illustrated in the above example is an easy way to work around this issue.


## MIT License ##

Copyright (c) 2013 Rally Software

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
IN THE SOFTWARE.
