/* Copyright (c) 1996-2013 Clickteam
 *
 * This source code is part of the Android exporter for Clickteam Multimedia Fusion 2.
 * 
 * Permission is hereby granted to any person obtaining a legal copy 
 * of Clickteam Multimedia Fusion 2 to use or modify this source code for 
 * debugging, optimizing, or customizing applications created with 
 * Clickteam Multimedia Fusion 2.  Any other use of this source code is prohibited.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
//----------------------------------------------------------------------------------
//
// CIMAGEBANK : Stockage des images
//
//----------------------------------------------------------------------------------
package Banks;

import Runtime.MMFRuntime;
import Application.*;
import Services.*;
import Sprites.*;
import OpenGL.*;
import Runtime.*;

import android.content.res.Resources;
import android.graphics.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.nio.*;
import java.lang.System;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* This class is abstract because anyone using CImage MUST handle onDestroy */

public abstract class CImage extends ITexture
{
    public static Set <CImage> images = new HashSet<CImage>();

    protected native void allocNative (boolean resample);

    protected native void allocNative2
        (boolean resample, short handle, int [] img, int xSpot, int ySpot,
            int xAP, int yAP, int width, int height);

    protected native void allocNative4
        (boolean resample, CFile file);

    private native void freeNative ();

    public CImage ()
    {
        allocNative ((MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0);

        images.add (this);
    }

    /* For CImageBank */

    public CImage
        (short handle, Bitmap img, int xSpot, int ySpot,
            int xAP, int yAP, int useCount, int width, int height)
    {
        allocNative2
            ((MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0,
                handle, CServices.getBitmapPixels (img), xSpot, ySpot,
                xAP, yAP, width, height);

        images.add (this);
    }

    /* For the joystick images */

    public CImage (String resource)
    {
        Bitmap img = null;

        try
        {
            img = BitmapFactory.decodeResource (MMFRuntime.inst.getResources (),
                                    MMFRuntime.inst.getResourceID (resource));
        }
        catch (Throwable e)
        {
        }

        if (img == null)
            throw new RuntimeException ("Bad image resource : " + resource);

        allocNative2 ((MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0,
                        (short) -1, CServices.getBitmapPixels (img), 0, 0, 0,
                            0, img.getWidth (), img.getHeight ());

        images.add (this);
    }

    /* For the Active Picture object (to load a file) */

    public CImage (InputStream input)
    {
        Bitmap img = null;

        try
        {
            img = BitmapFactory.decodeStream (input);
        }
        catch (Throwable e)
        {
        }

        if (img == null)
            throw new RuntimeException ("Bad image [stream]");

        allocNative2 ((MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0,
                        (short) -1, CServices.getBitmapPixels (img), 0, 0, 0,
                            0, img.getWidth (), img.getHeight ());

        images.add (this);
    }

    public long ptr;

    public native CMask getMask(int nFlags, int angle, double scaleX, double scaleY);

    public native int getXSpot ();
    public native int getYSpot ();

    public native void setXSpot (int x);
    public native void setYSpot (int y);

    public native int getXAP ();
    public native int getYAP ();

    public native int setXAP (int x);
    public native int setYAP (int y);

    public native int getWidth();
    public native int getHeight();

    public native int getPixel (int x, int y);

    public native int texture ();

    public native void deuploadNative ();

    public void destroy ()
    {
        if (ptr == 0)
            return;

        images.remove (this);

        onDestroy ();

        deuploadNative ();
        freeNative ();

        ptr = 0;
    }

    public abstract void onDestroy ();

    public native void updateWith (int [] pixels, int width, int height);

    public void updateWith (Bitmap b)
    {
        updateWith (CServices.getBitmapPixels (b), b.getWidth (), b.getHeight ());
    }
    
    public native short getHandle ();

    public native void getInfo (CImageInfo dest, int nAngle, float fScaleX, float fScaleY);

    public native void flipHorizontally ();
    public native void flipVertically ();

}
