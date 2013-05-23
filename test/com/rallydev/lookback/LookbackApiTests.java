package com.rallydev.lookback;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class LookbackApiTests {

    LookbackApi api;

    @Before
    public void setUp() {
        String username = "username";
        String password = "password";

        api = new LookbackApi()
                .setCredentials(username, password)
                .setWorkspace("1234");
    }

//    @Test
//    public void testRunQuery() {
//        LookbackQuery query = api.newSnapshotQuery()
//                .setPagesize(20000)
//                .setStart(0);
//    }

}
