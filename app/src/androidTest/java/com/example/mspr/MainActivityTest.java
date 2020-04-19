package com.example.mspr;

import org.junit.Test;

import static org.junit.Assert.*;

public class MainActivityTest {
    @Test
    public void testgetBitmapFromURL() {
        assertEquals(MainActivity.getBitmapFromURL("fail"), null);
    }

    @Test
    public void testVariable(){
        assertEquals(1, MainActivity.REQUEST_IMAGE_CAPTURE);
    }

    @Test
    public void isInteger() {
        assertEquals(MainActivity.isInteger("1"),true);
    }
}