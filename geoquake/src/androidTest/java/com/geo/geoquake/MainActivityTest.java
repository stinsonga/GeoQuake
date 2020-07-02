package com.geo.geoquake;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Test
    public void getUrlFragTest() {
        Context context = InstrumentationRegistry
                .getInstrumentation().getTargetContext();
        assertEquals(Utils.getURLFrag(0, 0, 1, context), "significant_day.geojson");
    }

}
