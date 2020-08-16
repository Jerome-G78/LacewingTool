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

import Runtime.*;
import Application.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

public abstract class GLRenderer
{
    public static final int BOP_COPY = 0;
    public static final int BOP_BLEND = 1;
    public static final int BOP_INVERT = 2;
    public static final int BOP_XOR = 3;
    public static final int BOP_AND = 4;
    public static final int BOP_OR = 5;
    public static final int BOP_BLEND_REPLEACETRANSP = 6;
    public static final int BOP_DWROP = 7;
    public static final int BOP_ANDNOT = 8;
    public static final int BOP_ADD = 9;
    public static final int BOP_MONO = 10;
    public static final int BOP_SUB = 11;
    public static final int BOP_BLEND_DONTREPLACECOLOR = 12;
    public static final int BOP_EFFECTEX = 13;
    public static final int BOP_MAX = 14;

    public static final int BOP_MASK = 0xFFF;
    public static final int BOP_RGBAFILTER = 0x1000;
    public static final int EFFECTFLAG_TRANSPARENT = 0x10000000;
    public static final int EFFECTFLAG_ANTIALIAS = 0x20000000;
    public static final int EFFECT_MASK = 0xFFFF;

    public static GLRenderer inst;
    
    public static int limitX;
    public static int limitY;

    private long ptr;

    protected Set<ITexture> textures;

    public Log debugWriter;

    public GLRenderer ()
    {
    	textures = new HashSet<ITexture>();
        debugWriter = new Log();
    }

    public String gpu, gpuVendor, glVersion;

    public abstract void updateViewport (boolean stretchWindowToViewport);
    public abstract void clear(int color);

    protected abstract void setBase (int x, int y);

    protected abstract int getBaseX ();
    protected abstract int getBaseY ();

    public abstract void setLimitX (int limit);
    public abstract void setLimitY (int limit);
    
    public abstract void fillZone
        (int x, int y, int w, int h, int color, int inkEffect, int inkEffectParam);

    public abstract void fillZone (int x, int y, int w, int h, int color);

    public void drawJoystick()
    {
        if(MMFRuntime.inst.app.joystick != null
                && MMFRuntime.inst.app.appRunningState == CRunApp.SL_FRAMELOOP)
        {
            MMFRuntime.inst.app.joystick.draw();
        }
    }

    protected abstract void scissor (boolean enabled);
    protected abstract void scissor (int x, int y, int w, int h);

    ArrayList<int []> clips = new ArrayList<int []>();

    public void pushClip(int x, int y, int w, int h)
    {
        x *= MMFRuntime.inst.scaleX;
        y *= MMFRuntime.inst.scaleY;

        w *= MMFRuntime.inst.scaleX;
        h *= MMFRuntime.inst.scaleY;

        x += MMFRuntime.inst.viewportX + getBaseX ();
        y += MMFRuntime.inst.viewportY + getBaseY ();

        if(clips.isEmpty())
            scissor (true);

        int [] clip = { x, MMFRuntime.inst.currentHeight - y - h, w, h };
        clips.add(clip);

        scissor (clip[0], clip[1], clip[2], clip[3]);
    }

    public void popClip()
    {
        clips.remove(clips.size() - 1);

        if(clips.isEmpty())
        {
            scissor (false);
        }
        else
        {
            int [] clip = clips.get(clips.size() - 1);
            scissor (clip[0], clip[1], clip[2], clip[3]);
        }
    }

    ArrayList<int []> bases = new ArrayList<int []>();

    public void pushClipAndBase(int x, int y, int w, int h)
    {
        pushClip(x, y, w, h);

        x += getBaseX ();
        y += getBaseY ();

        int [] base = { x, y };
        bases.add(base);

        setBase (x, y);
    }

    public void popClipAndBase()
    {
        popClip();

        bases.remove(bases.size() - 1);

        if(bases.isEmpty())
        {
            setBase (0, 0);
        }
        else
        {
            int [] base = bases.get(bases.size() - 1);
            setBase (base [0], base [1]);
        }
    }

    public void pushBase(int x, int y, int w, int h)
    {
        x += getBaseX ();
        y += getBaseY ();

        int [] base = { x, y };
        bases.add(base);

        setBase (x, y);
    }

    public void popBase()
    {
        bases.remove(bases.size() - 1);

        if(bases.isEmpty())
        {
            setBase (0, 0);
        }
        else
        {
            int [] base = bases.get(bases.size() - 1);
            setBase (base [0], base [1]);
        }
    }

    public abstract void renderPoint
        (ITexture image, int x, int y, int inkEffect, int inkEffectParam);

    public abstract void renderGradient
        (int x, int y, int w, int h, int color1, int color2, boolean vertical, int inkEffect, int inkEffectParam);

    public abstract void setInkEffect
        (int effect, int effectParam);

    public abstract void renderImage
        (ITexture image, int x, int y, int w, int h, int inkEffect, int inkEffectParam);

    public abstract void renderScaledRotatedImage
        (ITexture image, float angle, float sX, float sY, int hX, int hY,
            int x, int y, int w, int h, int inkEffect, int inkEffectParam);

    public abstract void renderPattern
        (ITexture image, int x, int y, int w, int h, int inkEffect, int inkEffectParam);

    public abstract void renderLine
        (int xA, int yA, int xB, int yB, int color, int thickness);

    public abstract void renderRect
        (int x, int y, int w, int h, int color, int thickness);

    public abstract void renderGradientEllipse
            (int x, int y, int w, int h, int color1, int color2,
              boolean vertical, int inkEffect, int inkEffectParam);

    public abstract void renderPatternEllipse
        (ITexture image, int x, int y, int w, int h, int inkEffect, int inkEffectParam);

    public abstract void beginWholeScreenDraw ();

    public abstract void endWholeScreenDraw ();
}
