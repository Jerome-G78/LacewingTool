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
package Runtime;

import Application.CRunApp;
import Banks.CImage;
import android.content.Context;
import android.view.*;
import Extensions.*;
import OpenGL.*;
import tv.ouya.console.api.OuyaController;
import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.GL11;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SurfaceView extends android.view.SurfaceView implements SurfaceHolder.Callback
{
    public static SurfaceView inst;

    public static boolean hasSurface = false;
    public static boolean ES2 = false;

    public GLRenderer renderer;

    public EGL10 egl;

    public EGLDisplay eglDisplay;
    public EGLContext eglContext;
    public EGLSurface eglSurface;
    public EGLConfig eglConfig;

    public SurfaceView(Context context)
    {
        super(context);

        inst = this;

        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
    }

    public void swapBuffers ()
    {
        /* Does nothing if the surface hasn't yet been created */

        if (egl != null)
            egl.eglSwapBuffers (eglDisplay, eglSurface);

    }
    
    boolean reinit = false;

    public void surfaceCreated (SurfaceHolder holder)
    {
        Log.Log("Thread " + Thread.currentThread() + " creating surface w/ holder " + holder);

        egl = (EGL10) EGLContext.getEGL();

        eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        egl.eglInitialize(eglDisplay, new int [] { 2, 0 });

        Log.Log ("eglInitialize called");

        eglConfig = new SimpleEGLConfigChooser
                            (ES2 ? 4 /* EGL_OPENGL_ES2_BIT */ :
                                   1 /* EGL_OPENGL_ES_BIT */,  false).chooseConfig(egl, eglDisplay);

        Log.Log ("Got eglConfig : " + eglConfig);

        eglContext = egl.eglCreateContext(eglDisplay, eglConfig,
                    EGL10.EGL_NO_CONTEXT, new int []
                            { 0x3098 /* EGL_CONTEXT_CLIENT_VERSION */, ES2 ? 2 : 1, EGL10.EGL_NONE });

        eglSurface = egl.eglCreateWindowSurface (eglDisplay, eglConfig, holder, null);

        Log.Log ("createSurface: Making current...");

        makeCurrent();

        hasSurface = true;

        if (SurfaceView.ES2)
        {
            renderer = new ES2Renderer ();
            ((ES2Renderer) renderer).onSurfaceCreated (eglConfig);
        }
        else
        {
            renderer = new ES1Renderer ();
            ((ES1Renderer) renderer).onSurfaceCreated ((GL11) eglContext.getGL(), eglConfig);
        }
        
        if (reinit)
        	MMFRuntime.inst.updateViewport();
        
    	reinit = true;
    }

    public void makeCurrent ()
    {
        Log.Log ("makeCurrent - display: " + eglDisplay
            + ", surface: " + eglSurface + ", context: " + eglContext + " from thread " + Thread.currentThread ());

        if (eglDisplay != null && eglSurface != null && eglContext != null)
        {
            if (!egl.eglMakeCurrent (eglDisplay, eglSurface, eglSurface, eglContext))
            {
                Log.Log ("!!! eglMakeCurrent FAILED : " + egl.eglGetError());
            }

            Log.Log ("eglMakeCurrent: " + egl.eglGetError ());

            GLRenderer.inst = renderer;
        }
        else
        {
            GLRenderer.inst = null;

            Log.Log ("Can't make current");
        }
    }

    public void surfaceChanged (android.view.SurfaceHolder holder, int format, int w, int h)
    {
        /* Assuming something higher up will already have called updateViewport */
    }

    public void surfaceDestroyed (SurfaceHolder holder)
    {
        shutdown ();
    }

    public void shutdown ()
    {
        /* Make sure old texture IDs won't stick around when the surface is recreated */

        Set <CImage> images = new HashSet<CImage>();
        images.addAll (CImage.images);

        Iterator <CImage> iterator = images.iterator ();

        while (iterator.hasNext ())
            iterator.next ().destroy();

        CImage.images.clear();

        egl.eglMakeCurrent (eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(eglDisplay, eglSurface);
        egl.eglDestroyContext(eglDisplay, eglContext);
        egl.eglTerminate(eglDisplay);

        hasSurface = false;

        if (MMFRuntime.inst.app != null
        		&& (MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_AUTOEND) != 0)
        {
            MMFRuntime.inst.die();
        }
    }

    byte getPlayerBits(int keyCode)
    {
        switch(keyCode)
        {
            case OuyaController.BUTTON_DPAD_LEFT:
                return 0x04;

            case OuyaController.BUTTON_DPAD_RIGHT:
                return 0x08;

            case OuyaController.BUTTON_A:
                return 16;
        };

        return 0x00;
    }

    @Override
    public boolean onKeyDown(final int keyCode, KeyEvent msg)
    {
        if(MMFRuntime.inst.OUYA)
        {
            OuyaController.onKeyDown(keyCode, msg);

            OuyaController controller = OuyaController.getControllerByDeviceId(msg.getDeviceId());

            if(controller != null)
            {
                byte[] rhPlayer = MMFRuntime.inst.app.run.rhPlayer;

                rhPlayer[controller.getPlayerNum()] |= getPlayerBits(keyCode);

                for (Iterator <CRunOUYA> it =
                        MMFRuntime.inst.app.ouyaObjects.iterator (); it.hasNext (); )
                {
                    ((CRunOUYA) it.next()).keyDown(controller, keyCode, msg);
                }
            }
        }

        if (MMFRuntime.inst.app != null)
            MMFRuntime.inst.app.keyDown(msg.getKeyCode());

        return false;
    }

    @Override
    public boolean onKeyUp(final int keyCode, KeyEvent msg)
    {
        if(MMFRuntime.inst.OUYA)
        {
            OuyaController.onKeyUp(keyCode, msg);

            OuyaController controller = OuyaController.getControllerByDeviceId(msg.getDeviceId());

            if(controller != null)
            {
                byte[] rhPlayer = MMFRuntime.inst.app.run.rhPlayer;

                rhPlayer[controller.getPlayerNum()] &= ~ getPlayerBits(keyCode);

                for (Iterator <CRunOUYA> it =
                        MMFRuntime.inst.app.ouyaObjects.iterator (); it.hasNext (); )
                {
                    ((CRunOUYA) it.next()).keyUp(controller, keyCode, msg);
                }
            }
        }

        if (MMFRuntime.inst.app != null)
            MMFRuntime.inst.app.keyUp(msg.getKeyCode());

        return false;
    }

    @Override
    public boolean onGenericMotionEvent(final MotionEvent event)
    {
        OuyaController.onGenericMotionEvent(event);

        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onTrackballEvent(final MotionEvent event)
    {
        MMFRuntime.inst.app.trackballMove((int) event.getX(), (int) event.getY());
        MMFRuntime.inst.touchManager.process(event);

        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus)
    {
        if (!hasWindowFocus)
        {
            // Pause
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event)
    {
        if (MMFRuntime.inst.app != null)
            MMFRuntime.inst.touchManager.process (event);

        return true;
    }


    /**** From GLSurfaceView source ****/

    public interface EGLConfigChooser {
        EGLConfig chooseConfig(EGL10 egl, EGLDisplay display);
    }

    private static abstract class BaseConfigChooser
            implements EGLConfigChooser {
        public BaseConfigChooser(int[] configSpec) {
            mConfigSpec = configSpec;
        }
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config);

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException(
                        "No configs match configSpec");
            }

            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
                    num_config);
            EGLConfig config = chooseConfig(egl, display, configs);
            if (config == null) {
                throw new IllegalArgumentException("No config chosen");
            }
            return config;
        }

        abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                        EGLConfig[] configs);

        protected int[] mConfigSpec;
    }

    private static class ComponentSizeChooser extends BaseConfigChooser {

        int type;

        public ComponentSizeChooser(int type, int redSize, int greenSize, int blueSize,
                                    int alphaSize, int depthSize, int stencilSize) {

            super(new int[] {
                    EGL10.EGL_RENDERABLE_TYPE, type,
                    EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,

                    EGL10.EGL_RED_SIZE, redSize,
                    EGL10.EGL_GREEN_SIZE, greenSize,
                    EGL10.EGL_BLUE_SIZE, blueSize,
                    EGL10.EGL_ALPHA_SIZE, alphaSize,
                    EGL10.EGL_DEPTH_SIZE, depthSize,
                    EGL10.EGL_STENCIL_SIZE, stencilSize,
                    EGL10.EGL_NONE});

            this.type = type;

            mValue = new int[1];
            mRedSize = redSize;
            mGreenSize = greenSize;
            mBlueSize = blueSize;
            mAlphaSize = alphaSize;
            mDepthSize = depthSize;
            mStencilSize = stencilSize;
        }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                      EGLConfig[] configs) {

            Log.Log (configs.length + " possible configurations returned");

            /* TODO : depth size 24 may be fastest? */

            EGLConfig closestConfig = null;
            int closestDistance = 1000;
            for(EGLConfig config : configs) {

                if (type == 4)
                {
                    if ((findConfigAttrib(egl, display, config,
                            EGL10.EGL_RENDERABLE_TYPE, 0) & 4) == 0)
                    {
                        Log.Log ("Config not ES 2.0, skipping...");
                        continue;
                    }
                }

                if ((findConfigAttrib(egl, display, config,
                        EGL10.EGL_SURFACE_TYPE, 0) & EGL10.EGL_WINDOW_BIT) == 0)
                {
                    Log.Log ("Config does not have window bit set, skipping...");
                    continue;
                }

                int r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config,
                        EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config,
                        EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0);
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);

                int distance = Math.abs(r - mRedSize)
                        + Math.abs(g - mGreenSize)
                        + Math.abs(b - mBlueSize) + Math.abs(a - mAlphaSize)
                        + Math.abs(d - mDepthSize) + Math.abs(s - mStencilSize);
                if (distance < closestDistance) {

                    Log.Log ("New closest config: " + config);

                    closestDistance = distance;
                    closestConfig = config;
                }
            }
            return closestConfig;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                     EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }

        private int[] mValue;
        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
    }

    private static class SimpleEGLConfigChooser extends ComponentSizeChooser {
        public SimpleEGLConfigChooser(int type, boolean withDepthBuffer) {
            super(type, 4, 4, 4, 0, withDepthBuffer ? 24 : 0, 0);
            mRedSize = 5;
            mGreenSize = 6;
            mBlueSize = 5;
        }
    }


}
