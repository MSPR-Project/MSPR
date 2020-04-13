package com.example.mspr;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void test_REQUEST_IMAGE_CAPTURE(){
        int test_REQUEST_IMAGE_CAPTURE = 1;
        int true_value = MainActivity.REQUEST_IMAGE_CAPTURE;
        assertEquals(test_REQUEST_IMAGE_CAPTURE, true_value);
    }
}
