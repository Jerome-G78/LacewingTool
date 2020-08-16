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

package OpenGL;

import Services.*;
import Application.CRunApp;
import Banks.*;
import Runtime.*;

import android.graphics.*;
import android.text.StaticLayout;
import android.text.TextPaint;


public class CTextSurface
{
	CRunApp app;
	
	String prevText;
	short prevFlags;
	int prevColor;
	CFontInfo prevFont;

	public int width;
	public int height;

	int effect;
	int effectParam;

    int drawOffset;

	public Bitmap textBitmap;
    public Canvas textCanvas;
    public TextPaint textPaint;

    class CTextTexture extends CImage
    {
        public CTextTexture ()
        {
            allocNative2 ((MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0,
                    (short) -1, CServices.getBitmapPixels (textBitmap), 0, 0, 0,
                        0, textBitmap.getWidth(), textBitmap.getHeight ());
        }

        @Override
        public void onDestroy ()
        {
            textTexture = null;
        }
    }

    CTextTexture textTexture;

    public CTextSurface(CRunApp app, int width, int height)
    {
    	this.app = app;
    	
        this.width = width;
        this.height = height;

        int bmpWidth = 1;
        int bmpHeight = 1;

        while(bmpWidth < width)
            bmpWidth *= 2;

        while(bmpHeight < height)
            bmpHeight *= 2;

        textBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        textTexture = null;

        textCanvas = new Canvas(textBitmap);
        textPaint = new TextPaint();

        textPaint.setAntiAlias(true);
        textPaint.setSubpixelText(true);

        prevText = "";
        prevFlags = 0;
        prevFont = null;
    	prevColor = 0;
    }

    public void resize(int width, int height, boolean backingOnly)
    {
        if (!backingOnly)
        {
            this.width = width;
            this.height = height;
        }

        if(textBitmap.getWidth() >= width && textBitmap.getHeight() >= height)
            return;

        int bmpWidth = 1;
        int bmpHeight = 1;

        while(bmpWidth < width)
            bmpWidth *= 2;

        while(bmpHeight < height)
            bmpHeight *= 2;

        textBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        textCanvas = new Canvas(textBitmap);
        textTexture = null;
    }

    public void measureText (String s, short flags, CFontInfo font, CRect rect)
    {
        textPaint.setTypeface(font.createFont());

        StaticLayout layout = new StaticLayout
                (s, textPaint, width, CServices.textAlignment(flags), 1.0f, 0.0f, false);

        int height = layout.getHeight ();

        if ((rect.top + height) > rect.bottom)
            height = rect.bottom - rect.top;

        rect.bottom = rect.top + height;
        rect.left = rect.left + layout.getWidth ();
    }

    public void setText(String s, short flags, int color, CFontInfo font, boolean dynamic)
    {
        if(s.equals(prevText) && color == prevColor && flags == prevFlags && font.equals(prevFont))
            return;

        textBitmap.eraseColor(0);

        prevFont = font;
        prevText = s;
        prevColor = color;
        prevFlags = flags;

        CRect rect = new CRect ();
        rect.left = 0;
        rect.right = width;
        rect.top = 0;
        rect.bottom = height;

        manualDrawText(s, flags, rect, color, font, dynamic);
        updateTexture();
    }

    public void manualDrawText(String s, short flags, CRect rect, int color, CFontInfo font, boolean dynamic)
    {
        int rectWidth = rect.right - rect.left;
        int rectHeight = rect.bottom - rect.top;

        textPaint.setColor(0xFF000000|color);
        textPaint.setTypeface(font.createFont());
        textPaint.setTextSize(font.lfHeight);

        StaticLayout layout = new StaticLayout
                (s, textPaint, rectWidth, CServices.textAlignment(flags), 1.0f, 0.0f, false);

        if (dynamic && height < layout.getHeight ())
        {
            resize (width, layout.getHeight (), true);

            layout.draw (textCanvas);

            if ((flags & CServices.DT_BOTTOM) != 0)
                drawOffset = - (layout.getHeight () - rectHeight);
            else if ((flags & CServices.DT_VCENTER) != 0)
                drawOffset = - ((layout.getHeight () - rectHeight) / 2);
        }
        else
        {
            drawOffset = 0;

            textCanvas.save();

            if ((flags & CServices.DT_BOTTOM) != 0)
                textCanvas.translate (rect.left, rect.bottom - layout.getHeight());
            else if ((flags & CServices.DT_VCENTER) != 0)
                textCanvas.translate (rect.left, rect.top + rectHeight / 2 - layout.getHeight() / 2);
            else
                textCanvas.translate (rect.left, rect.top);

           // textCanvas.clipRect (rect.left, rect.top, rect.right, rect.bottom);

            layout.draw (textCanvas);

            textCanvas.restore();
        }
    }

    public void updateTexture()
    {
        if(textTexture != null)
        	textTexture.updateWith(textBitmap);
        else
        	textTexture = new CTextTexture ();
    }
    
    public void manualClear(int color)
    {
        textBitmap.eraseColor(color & 0x00FFFFFF);
    }
    
    
    public void draw(int x, int y, int effect, int effectParam)
    {
        if (textTexture == null)
            updateTexture();

        GLRenderer.inst.renderImage
            (textTexture, x, y + drawOffset, -1, -1, effect, effectParam);
    }

    public void recycle ()
    {
        if (textBitmap != null)
        {
            textBitmap.recycle();
            textBitmap = null;
        }

        if (textTexture != null)
            textTexture.destroy (); /* will also set textTexture to null */
    }
}
