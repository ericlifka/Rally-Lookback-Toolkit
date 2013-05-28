package com.rallydev.lookback;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class LookbackQueryTests {

    LookbackApi api;

    @Before
    public void setUp() {
        api = new LookbackApi();
    }

    @Test(expected = LookbackException.class)
    public void cannotSetFieldsTrueAndRequireFields() {
        api.newSnapshotQuery()
                .setFieldsTrue()
                .requireFields("TestField")
                .execute();
    }

    @Test
    public void requireFieldIsAtomic() {
        LookbackQuery query = api.newSnapshotQuery()
                .requireFields("TestField", "TestField");

        assert (1 == query.fields.size());
    }

    @Test
    public void requireFieldsAddsAllParameters() {
        Set<String> fields = api.newSnapshotQuery().requireFields("1", "2", "3").fields;

        assert (3 == fields.size());
        assert (fields.contains("1"));
        assert (fields.contains("2"));
        assert (fields.contains("3"));
    }

    @Test
    public void sortByIsAtomic() {
        LookbackQuery query = api.newSnapshotQuery()
                .sortBy("TestField")
                .sortBy("TestField");

        assert (1 == query.sort.size());
    }

    @Test
    public void hydrateFieldIsAtomic() {
        LookbackQuery query = api.newSnapshotQuery()
                .hydrateFields("TestField", "TestField");

        assert (1 == query.hydrate.size());
    }

    @Test
    public void setPropertIsAtomic() {
        LookbackQuery query = api.newSnapshotQuery()
                .addProperty("compress", "true")
                .addProperty("compress", "true");

        assert (1 == query.properties.size());
    }

    @Test
    public void addFindClauseIsAtomic() {
        LookbackQuery query = api.newSnapshotQuery()
                .addFindClause("Project", "123")
                .addProperty("Project", "123");

        assert (1 == query.find.size());
    }

    @Test(expected = LookbackException.class)
    public void cannotExecuteQueryWithNoFind() {
        api.newSnapshotQuery()
                .execute();
    }

    @Test
    public void encodedJsonContainsFields() {
        String json = api.newSnapshotQuery()
                .requireFields("field1", "field2")
                .getRequestJson();

        Map requestMap = new Gson().fromJson(json, Map.class);
        List fields = (List) requestMap.get("fields");

        assert (2 == fields.size());
        assert (fields.contains("field1"));
        assert (fields.contains("field2"));
    }

    @Test
    public void encodedJsonContainsHydrate() {
        String json = api.newSnapshotQuery()
                .hydrateFields("field1", "field2")
                .getRequestJson();

        Map requestMap = new Gson().fromJson(json, Map.class);
        List hydrate = (List) requestMap.get("hydrate");

        assert (2 == hydrate.size());
        assert (hydrate.contains("field1"));
        assert (hydrate.contains("field2"));
    }

    @Test
    public void encodedJsonContainsSort() {
        String json = api.newSnapshotQuery()
                .sortBy("field1")
                .sortBy("field2", -1)
                .getRequestJson();

        Map requestMap = new Gson().fromJson(json, Map.class);
        Map sort = (Map) requestMap.get("sort");

        assert (2 == sort.size());
        assert (1.0 == (Double) sort.get("field1"));
        assert (-1.0 == (Double) sort.get("field2"));
    }

    @Test
    public void encodedJsonContainsPagesizeAndStart() {
        String json = api.newSnapshotQuery()
                .getRequestJson();

        Map requestMap = new Gson().fromJson(json, Map.class);
        double start = (Double) requestMap.get("start");
        double pagesize = (Double) requestMap.get("pagesize");

        assert (0 == start);
        assert (20000 == pagesize);
    }

    @Test
    public void encodedJsonContainsProperties() {
        String json = api.newSnapshotQuery()
                .addProperty("compress", true)
                .getRequestJson();

        Map requestMap = new Gson().fromJson(json, Map.class);
        Boolean compress = (Boolean) requestMap.get("compress");

        assert (compress);
    }

    @Test
    public void encodedJsonContainsFind() {
        String json = api.newSnapshotQuery()
                .addFindClause("Project", 1234)
                .addFindClause("__At", "Current")
                .getRequestJson();

        Map requestMap = new Gson().fromJson(json, Map.class);
        Map find = (Map) requestMap.get("find");

        assert (2 == find.size());
        assert (1234 == (Double) find.get("Project"));
        assert ("Current".equals(find.get("__At")));
    }

    @Test
    public void encodedJsonContainsComplexFind() {
        Map story = new HashMap();
        story.put("_TypeHierarchy", "HierarchicalRequirement");

        Map defect = new HashMap();
        defect.put("_TypeHierarchy", "Defect");

        List<Map> clauses = new ArrayList<Map>(2);
        clauses.add(story);
        clauses.add(defect);

        String json = api.newSnapshotQuery()
                .addFindClause("$or", clauses)
                .getRequestJson();

        Map requestMap = new Gson().fromJson(json, Map.class);
        Map find = (Map) requestMap.get("find");

        assert (1 == find.size());
        List orClauses = (List) find.get("$or");
        assert (2 == orClauses.size());
        Map storyClause = (Map) orClauses.get(0);
        Map defectClause = (Map) orClauses.get(1);
        assert ("HierarchicalRequirement".equals(storyClause.get("_TypeHierarchy")));
        assert ("Defect".equals(defectClause.get("_TypeHierarchy")));
    }
}
