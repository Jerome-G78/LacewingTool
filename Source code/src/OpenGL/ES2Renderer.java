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

import javax.microedition.khronos.egl.EGLConfig;

import android.opengl.GLES20;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import java.io.InputStream;
import java.util.*;


public class ES2Renderer extends GLRenderer
{
    public static ES2Renderer inst;

    private long ptr;

    public ES2Renderer()
    {
        System.loadLibrary ("ES2Renderer");

        allocNative ();
    }

    private native void allocNative ();

    protected native void setBase (int x, int y);

    protected native int getBaseX ();
    protected native int getBaseY ();

    public native void setLimitX (int limit);
    public native void setLimitY (int limit);

    boolean didStretch;
    
    int oldLimitX;
    int oldLimitY;
    
    public void beginWholeScreenDraw ()
    {
        MMFRuntime runtime =  MMFRuntime.inst;

        GLES20.glViewport(0, 0, runtime.currentWidth, runtime.currentHeight);
        setProjectionMatrix (0, 0,  runtime.currentWidth, runtime.currentHeight);

        oldLimitX = GLRenderer.limitX;
        GLRenderer.limitX = runtime.currentWidth;
        setLimitX(GLRenderer.limitX);

        oldLimitY = GLRenderer.limitY;
        GLRenderer.limitY = runtime.currentHeight;
        setLimitY(GLRenderer.limitY);
    }

    public void endWholeScreenDraw ()
    {
        updateViewport (didStretch);

        GLRenderer.limitX = oldLimitX;
        setLimitX(oldLimitX);

        GLRenderer.limitY = oldLimitY;
        setLimitY(oldLimitY);
    }
    
    @Override
    public void updateViewport (boolean stretchWindowToViewport)
    {
    	didStretch = stretchWindowToViewport;
    	
        /* Be sure not to leave logging information in here, as it can potentially be
         * called every frame by endWholeScreenDraw.
         */

        if(MMFRuntime.inst.app.joystick != null)
            MMFRuntime.inst.app.joystick.setPositions();

        GLES20.glViewport (MMFRuntime.inst.viewportX, MMFRuntime.inst.viewportY,
                MMFRuntime.inst.viewportWidth, MMFRuntime.inst.viewportHeight);

        if(stretchWindowToViewport)
        {
            setProjectionMatrix (0, 0, MMFRuntime.inst.app.gaCxWin, MMFRuntime.inst.app.gaCyWin);
        }
        else
        {
            setProjectionMatrix (0, 0, MMFRuntime.inst.viewportWidth, MMFRuntime.inst.viewportHeight);
        }
    }

    protected void scissor (boolean enabled)
    {
        if (enabled)
            GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        else
            GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    protected void scissor (int x, int y, int w, int h)
    {
        GLES20.glScissor (x, y, w, h);
    }

    public void onSurfaceCreated(EGLConfig config)
    {
        Log.Log ("ES2Renderer/onSurfaceCreated");

        boolean isFirstCreate = (inst == null);

        inst = this;

        MMFRuntime.installCrashReporter ();

        gpu = GLES20.glGetString(GLES20.GL_RENDERER);
        gpuVendor = GLES20.glGetString(GLES20.GL_VENDOR);
        glVersion = GLES20.glGetString(GLES20.GL_VERSION);

        Log.Log ("GL version: " + GLES20.glGetString(GLES20.GL_VERSION));

        Log.Log("ES2Renderer : Started - GPU " + gpu + ", vendor " + gpuVendor);

        CrashReporter.addInfo ("GPU", gpu);
        CrashReporter.addInfo ("GPU Vendor", gpuVendor);

        if ((MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_REQUIREGPU) != 0)
        {
            if (gpu.indexOf ("PixelFlinger") != -1)
            {
                MMFRuntime.inst.app.hdr2Options &= ~ CRunApp.AH2OPT_AUTOEND;
                MMFRuntime.inst.mainView.setVisibility(View.INVISIBLE);

                AlertDialog alertDialog;

                alertDialog = new AlertDialog.Builder(MMFRuntime.inst).create();
                alertDialog.setTitle("No GPU detected");

                alertDialog.setButton (DialogInterface.BUTTON_NEUTRAL, "Close", Message.obtain(new Handler (), new Runnable ()
                {   public void run ()
                    {   System.exit (0);
                    }
                }));

                alertDialog.setMessage("This application requires a GPU, but none was detected.");

                alertDialog.setOnDismissListener (new DialogInterface.OnDismissListener()
                {   public void onDismiss (DialogInterface d)
                    {   System.exit (0);
                    }
                });

                alertDialog.show();

                return;
            }
        }


        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glDisable (GLES20.GL_CULL_FACE);

        // Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        if (!isFirstCreate)
        {
            if (MMFRuntime.inst.app != null
                    && MMFRuntime.inst.app.run != null)
            {
                MMFRuntime.inst.app.run.reinitDisplay ();
                MMFRuntime.inst.app.run.resume();
            }
        }

        updateViewport(didStretch);
        MMFRuntime.inst.setFrameRate(MMFRuntime.inst.app.gaFrameRate);
    }

    public void fillZone (int x, int y, int w, int h, int color)
    {
        renderGradient (x, y, w, h, color, color, true, 0, 0);
    }

    public native void setProjectionMatrix (int x, int y, int w, int h);

    public native void clear (int color);

    public void fillZone (int x, int y, int w, int h, int color, int inkEffect, int inkEffectParam)
    {
        renderGradient (x, y, w, h, color, color, true, inkEffect, inkEffectParam);
    }

    public native void renderPoint
        (ITexture image, int x, int y, int inkEffect, int inkEffectParam);

    public native void renderGradient
        (int x, int y, int w, int h, int color1, int color2, boolean vertical, int inkEffect, int inkEffectParam);

    public native void setInkEffect
        (int effect, int effectParam);

    public native void renderImage
        (ITexture image, int x, int y, int w, int h, int inkEffect, int inkEffectParam);

    public native void renderScaledRotatedImage
        (ITexture image, float angle, float sX, float sY, int hX, int hY,
            int x, int y, int w, int h, int inkEffect, int inkEffectParam);

    public native void renderPattern
        (ITexture image, int x, int y, int w, int h, int inkEffect, int inkEffectParam);

    public native void renderLine
        (int xA, int yA, int xB, int yB, int color, int thickness);

    public void renderRect
            (int x, int y, int w, int h, int color, int thickness)
    {
        /* TODO for ES 2.0 (iOS doesn't support it yet either) */
    }

    /* ES 2.0 additions (b31) */

    public native void renderGradientEllipse
            (int x, int y, int w, int h, int color1, int color2, boolean vertical, int inkEffect, int inkEffectParam);

    public native void renderPatternEllipse
            (ITexture image, int x, int y, int w, int h, int inkEffect, int inkEffectParam);

    /* Called by native code */

    public String loadShader (String name)
    {
        try
        {
            InputStream stream =
                    (MMFRuntime.inst.getApplicationContext().getResources().openRawResource
                        (MMFRuntime.inst.getResourceID ("raw/" + name)));

            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            return new String(bytes);
        }
        catch (Throwable e)
        {
            return "";
        }
    }
}
