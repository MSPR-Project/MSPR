package com.example.mspr;

import android.content.Context;
import android.graphics.Bitmap;

import org.junit.Test;

import static org.junit.Assert.*;

public class MainActivityTest {
    MainActivity i = new MainActivity();
    @Test
    public void test_callable_function(){
        Boolean flag=false;
        try{
            i.runFaceDetection();
            Context inContext = null;
            Bitmap inImage = null;
            i.getImageUri(inContext, inImage);
            flag = true;
        }catch (Exception e){
            flag = false;
        }
        assertEquals(flag, true);
    }
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