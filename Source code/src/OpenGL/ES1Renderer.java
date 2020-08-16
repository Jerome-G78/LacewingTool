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
import Services.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL11;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.opengl.GLDebugHelper;
import android.opengl.GLSurfaceView;
import android.view.View;
import android.widget.PopupWindow;

import java.nio.*;
import java.util.*;

public class ES1Renderer extends GLRenderer
{
    public GL11 gl;

    public ES1Renderer ()
    {
        System.loadLibrary ("ES1Renderer");

        allocNative ();
    }

    private native void allocNative ();

    boolean didStretch;
    
    int oldLimitX;
    int oldLimitY;
    
    public void beginWholeScreenDraw ()
    {
        gl.glMatrixMode(GL11.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glMatrixMode(GL11.GL_PROJECTION);
        gl.glLoadIdentity();

        MMFRuntime runtime =  MMFRuntime.inst;

        gl.glViewport(0, 0, runtime.currentWidth, runtime.currentHeight);
        gl.glOrthof(0, runtime.currentWidth, runtime.currentHeight, 0, 1.0f, -1.0f);

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

        MMFRuntime runtime = MMFRuntime.inst;

        gl.glMatrixMode (GL11.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glMatrixMode(GL11.GL_PROJECTION);
        gl.glLoadIdentity();

        if(stretchWindowToViewport)
        {
            gl.glViewport(runtime.viewportX, runtime.viewportY,
                    runtime.viewportWidth, runtime.viewportHeight);

            gl.glOrthof(0, MMFRuntime.inst.app.gaCxWin, MMFRuntime.inst.app.gaCyWin, 0, 1.0f, -1.0f);
        }
        else
        {
            gl.glViewport(runtime.viewportX, runtime.viewportY,
                    runtime.viewportWidth, runtime.viewportHeight);

            gl.glOrthof(0, runtime.viewportWidth, runtime.viewportHeight, 0, 1.0f, -1.0f);
        }
    }

    public void onSurfaceCreated(GL11 gl, EGLConfig config)
    {
        this.gl = gl;

        Log.Log ("ES1Renderer/onSurfaceCreated");

        boolean isFirstCreate = (inst == null);

        inst = this;

        MMFRuntime.installCrashReporter ();

        if (MMFRuntime.debugLevel >= 2)
        {
            this.gl = (GL11) GLDebugHelper.wrap(gl, GLDebugHelper.CONFIG_CHECK_GL_ERROR |
                    GLDebugHelper.CONFIG_LOG_ARGUMENT_NAMES, debugWriter);
        }

        gpu = gl.glGetString(GL11.GL_RENDERER);
        gpuVendor = gl.glGetString(GL11.GL_VENDOR);
        glVersion = gl.glGetString(GL11.GL_VERSION);

        Log.Log("ES1Renderer : Started - GPU " + gpu + ", vendor " + gpuVendor);

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


        gl.glEnable(GL11.GL_BLEND);
        gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        gl.glEnable(GL11.GL_TEXTURE_2D);

        gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL11.GL_COLOR_ARRAY);

        gl.glDisable (GL11.GL_CULL_FACE);

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

    public native void clear (int color);
    public native void fillZone (int x, int y, int w, int h, int color, int inkEffect, int inkEffectParam);

    public void fillZone (int x, int y, int w, int h, int color)
    {
        fillZone (x, y, w, h, color, 0, 0);
    }

    protected void scissor (boolean enabled)
    {
        if (enabled)
            gl.glEnable (GL11.GL_SCISSOR_TEST);
        else
            gl.glDisable (GL11.GL_SCISSOR_TEST);
    }

    protected void scissor (int x, int y, int w, int h)
    {
        gl.glScissor (x, y, w, h);
    }

    protected native void setBase (int x, int y);

    protected native int getBaseX ();
    protected native int getBaseY ();

    public native void setLimitX (int limit);
    public native void setLimitY (int limit);

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

    public native void renderRect
            (int x, int y, int w, int h, int color, int thickness);

    /* ES 2.0 only */

    public void renderGradientEllipse
            (int x, int y, int w, int h, int color1, int color2, boolean vertical, int inkEffect, int inkEffectParam)
    {
        renderGradient (x, y, w, h, color1, color2, vertical, inkEffect, inkEffectParam);
    }

    public void renderPatternEllipse
            (ITexture image, int x, int y, int w, int h, int inkEffect, int inkEffectParam)
    {
        renderPattern (image, x, y, w, h, inkEffect, inkEffectParam);
    }

}



