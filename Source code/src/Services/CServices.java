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
// CSERVICES : Routines utiles diverses
//
//----------------------------------------------------------------------------------
package Services;

import android.net.Uri;
import Banks.*;
import Application.*;
import OpenGL.*;
import Runtime.MMFRuntime;

import android.graphics.*;
import android.provider.Settings;
import android.text.Layout;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class CServices
{
    public static short DT_LEFT = 0x0000;
    public static short DT_TOP = 0x0000;
    public static short DT_CENTER = 0x0001;
    public static short DT_RIGHT = 0x0002;
    public static short DT_BOTTOM = 0x0008;
    public static short DT_VCENTER = 0x0004;
    public static short DT_SINGLELINE = 0x0020;
    public static short DT_CALCRECT = 0x0400;
    public static short DT_VALIGN = 0x0800;
    public static short CPTDISPFLAG_INTNDIGITS = 0x000F;
    public static short CPTDISPFLAG_FLOATNDIGITS = 0x00F0;
    public static short CPTDISPFLAG_FLOATNDIGITS_SHIFT = 4;
    public static short CPTDISPFLAG_FLOATNDECIMALS = (short) 0xF000;
    public static short CPTDISPFLAG_FLOATNDECIMALS_SHIFT = 12;
    public static short CPTDISPFLAG_FLOAT_FORMAT = 0x0200;
    public static short CPTDISPFLAG_FLOAT_USENDECIMALS = 0x0400;
    public static short CPTDISPFLAG_FLOAT_PADD = 0x0800;

    public static Layout.Alignment textAlignment (int flags)
    {
        if ((flags & DT_CENTER) != 0)
            return Layout.Alignment.ALIGN_CENTER;

        if ((flags & DT_RIGHT) != 0)
            return Layout.Alignment.ALIGN_OPPOSITE;

        return Layout.Alignment.ALIGN_NORMAL;
    }

    public CServices()
    {
    }

    public static String loadFile (String filename)
    {
        String s = "";

        try
        {
            FileInputStream stream = new FileInputStream(filename);

            BufferedReader reader = new BufferedReader
                    (new InputStreamReader
                            (stream, Charset.forName(CFile.charset)));

            StringBuilder builder = new StringBuilder ();
            char [] buffer = new char [1024 * 16];

            int cpt;

            while ((cpt = reader.read(buffer, 0, buffer.length)) > 0)
                builder.append(buffer, 0, cpt);

            s = builder.toString();
        }
        catch (Throwable t)
        {
        }

        return s;
    }

    public static void saveFile (String filename, String s)
    {
        try
        {
            FileOutputStream file = new FileOutputStream (filename, false);
            file.write (s.getBytes(CFile.charset));
            file.close ();
        }
        catch (Throwable t)
        {
        }
    }

    public static int parseInt (String s)
    {
        try
        {
            return Integer.parseInt (s);
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    public static Uri filenameToURI (String filename)
    {
        filename = filename.toLowerCase ();

        File file = new File (filename);

        if (file == null)
            return null;

        Uri uri = Uri.fromFile (file);

        if (uri == null)
            return null;

        return uri;
    }

    public static int HIWORD(int ul)
    {
        return ul >> 16;
    }

    public static int LOWORD(int ul)
    {
        return ul & 0x0000FFFF;
    }

    public static int MAKELONG(int lo, int hi)
    {
        return (hi << 16) | (lo & 0xFFFF);
    }

    public static int getRValueJava(int rgb)
    {
        return (rgb >>> 16) & 0xFF;
    }

    public static int getGValueJava(int rgb)
    {
        return (rgb >>> 8) & 0xFF;
    }

    public static int getBValueJava(int rgb)
    {
        return rgb & 0xFF;
    }

    public static int RGBJava(int r, int g, int b)
    {
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public static int swapRGB(int rgb)
    {
        int r = (rgb >>> 16) & 0xFF;
        int g = (rgb >>> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (b & 0xFF) << 16 | (g & 0xFF) << 8 | (r & 0xFF);
    }

    public static String getWord(String s, int start)
    {
        int n;
        char c = ' ';
        if (s.charAt(start) == '"')
        {
            c = '"';
            start++;
        }
        for (n = start; n < s.length(); n++)
        {
            if (s.charAt(n) == c)
            {
                break;
            }
        }
        return s.substring(start, n);
    }

    public static Bitmap replaceColor(Bitmap imgSource, int oldColor, int newColor)
    {
        // Copie l'image source
        int width = imgSource.getWidth();
        int height = imgSource.getHeight();
        int pixels[] = new int[width * height];
        imgSource.getPixels(pixels, 0, width, 0, 0, width, height);

        int x, y;
        for (y = 0; y < height; y++)
        {
            for (x = 0; x < width; x++)
            {
                if ((pixels[y * width + x] & 0xFFFFFF) == oldColor)
                {
                    pixels[y * width + x] = (pixels[y * width + x] & 0xFF000000) | newColor;
                }
            }
        }
        Bitmap imgDest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        imgDest.setPixels(pixels, 0, width, 0, 0, width, height);
        return imgDest;
    }

    public static String intToString(int value, int displayFlags)
    {
        String s = Integer.toString(value);
        if ((displayFlags & CPTDISPFLAG_INTNDIGITS) != 0)
        {
            int nDigits = displayFlags & CPTDISPFLAG_INTNDIGITS;
            if (s.length() > nDigits)
            {
                s = s.substring(0, nDigits);
            }
            else
            {
                while (s.length() < nDigits)
                {
                    s = "0" + s;
                }
            }
        }
        return s;
    }
    public static String doubleToString(double value, int displayFlags)
    {
        String s;
        if ( (displayFlags & CPTDISPFLAG_FLOAT_FORMAT) == 0 )
        {
            s=Double.toString(value);
        }
        else
        {
            boolean bRemoveTrailingZeros = false;
            int nDigits = ((displayFlags & CPTDISPFLAG_FLOATNDIGITS) >> CPTDISPFLAG_FLOATNDIGITS_SHIFT) + 1;
            int nDecimals = -1;
            if ( (displayFlags & CPTDISPFLAG_FLOAT_USENDECIMALS) != 0 )
                nDecimals = ((displayFlags & CPTDISPFLAG_FLOATNDECIMALS) >> CPTDISPFLAG_FLOATNDECIMALS_SHIFT);
            else if ( value!=0.0 && value > -1.0 && value < 1.0 )
            {
                nDecimals = nDigits;
                bRemoveTrailingZeros = true;
            }

            if (nDecimals<0)
            {
                // TODO (uses toPrecision in flash)
            }
            /*else
            { */
                double magnitude = Math.pow(10,
                        nDecimals - (int) Math.ceil (Math.log10(value < 0 ? - value: value)));

                s = Double.toString (Math.round(value * magnitude) / magnitude);
            /* } */
            int l, n;
            String ss;
            if ((displayFlags & CPTDISPFLAG_FLOAT_PADD)!=0)
            {
                l=0;
                for (n=0; n<s.length(); n++)
                {
                    ss=s.substring(n, n + 1);
                    if (ss!="." && ss!="+" && ss!="-" && ss!="e" && ss!="E")
                    {
                        l++;
                    }
                }
                boolean bFlag=false;
                if (s.substring(0, 1)=="-")
                {
                    bFlag=true;
                    s=s.substring(1);
                }
                while(l<nDigits)
                {
                    s="0"+s;
                    l++;
                }
                if (bFlag)
                {
                    s="-"+s;
                }
            }
/*				if (bRemoveTrailingZeros)
            {
                l=s.length;
                var ps:int = s.length-1;
                while (s.charAt(ps)=="0")
                {
                    l=ps;
                }
                s=s.substr(0, l);
            }
*/			}
        return s;
    }
    public static void drawRegion(Canvas g, Paint p, Bitmap source, int sourceX, int sourceY, int sx, int sy, int destX, int destY)
    {
        int width = source.getWidth();
        int height = source.getHeight();
        if (sourceX < 0)
        {
            destX -= sourceX;
            sx += sourceX;
            sourceX = 0;
        }
        if (sourceX + sx > width)
        {
            sx = width - sourceX;
        }
        if (sourceY < 0)
        {
            destY -= sourceY;
            sy += sourceY;
            sourceY = 0;
        }
        if (sourceY + sy > height)
        {
            sy = height - sourceY;
        }
        if (sx > 0 && sy > 0)
        {
        	Rect srcRect=new Rect(sourceX, sourceY, sourceX+sx, sourceY+sy);
        	Rect dstRect=new Rect(destX, destY, destX+sx, destY+sy);
        	g.drawBitmap(source, srcRect, dstRect, p);
        }
    }

    public static int paintTextHeight(Paint p)
    {
    	return (int) (Math.ceil(-p.ascent()) + Math.ceil(p.descent()));
    }

    public static String getAndroidID ()
    {
        try
        {
            return Settings.Secure.getString (MMFRuntime.inst.getContentResolver (),
                                             Settings.Secure.ANDROID_ID);
        }
        catch (Throwable e)
        {
            return "";
        }
    }

    public static int [] getBitmapPixels (Bitmap img)
    {
        int [] pixels = new int [img.getWidth () * img.getHeight ()];
        img.getPixels (pixels, 0, img.getWidth (), 0, 0, img.getWidth (), img.getHeight ());
        return pixels;
    }

}
