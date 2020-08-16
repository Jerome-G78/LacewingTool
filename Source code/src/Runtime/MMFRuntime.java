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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import Application.*;
import Application.CRunApp.MenuEntry;
import Extensions.CRunAndroid;
import Objects.CExtension;
import Objects.CObject;
import OpenGL.*;

import RunLoop.CRun;
import Services.CServices;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.view.MenuItem.OnMenuItemClickListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;

import android.widget.RelativeLayout;
import com.google.ads.*;
import tv.ouya.console.api.OuyaController;

public abstract class MMFRuntime extends Activity
{
    public static String version = "Release 1";
    public static int debugLevel = 0;
    public static String appVersion = "";
    public static boolean rooted;

    public String ABI;

    public static MMFRuntime inst;

    public CTouchManager touchManager;

    public void die ()
    {
        Log.Log("MMFRuntime/die");
        System.exit(0);
    }
    
    public boolean enableCrashReporting = false;

    public static void installCrashReporter ()
    {
        if (MMFRuntime.debugLevel == 0) /* not in development mode? */
            Thread.currentThread().
                    setUncaughtExceptionHandler (new CrashReporter ());
    }

    public AdView adView;

    public void setAdMob (final boolean enabled, final boolean displayAtBottom, final boolean displayOverFrame)
    {
        if (adView != null)
        {
            container.removeView (adView);
            adView = null;
        }

        if (!enabled)
            return;

        adView = new AdView (MMFRuntime.inst, AdSize.BANNER, adMobID);

        adView.setId(2);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.FILL_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

        if (displayAtBottom)
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        if (!displayOverFrame)
           ((RelativeLayout.LayoutParams) mainView.getLayoutParams()).addRule
                   (displayAtBottom ? RelativeLayout.ABOVE : RelativeLayout.BELOW, adView.getId());

        container.addView(adView, params);

        AdRequest request = new AdRequest ();

        if (adMobTestDeviceID != "")
        {
            StringTokenizer tokenizer = new StringTokenizer(adMobTestDeviceID, ",");

            while (tokenizer.hasMoreElements())
            {
                String id = tokenizer.nextToken();

                Log.Log ("Adding AdMob test device: " + id);
                request.addTestDevice(id);
            }
        }

        adView.loadAd (request);
    }

    public String adMobID = "";
    public String adMobTestDeviceID = "";

    public MainView mainView = null;
    public RelativeLayout container = null;

    public int orientation = 0;

    public int viewportX, viewportY, viewportWidth, viewportHeight;
    public float scaleX, scaleY;

    public int currentWidth, currentHeight;

    public CRunTimerTask timerTask;

    public static boolean OUYA = false;

    @Override
    public void onConfigurationChanged(Configuration c)
    {
        super.onConfigurationChanged(c);

        orientation = c.orientation;
    }
    
    public boolean batteryReceived;
    
    public int batteryLevel;
    public int batteryStatus;
    public int batteryPlugged;
    public int batteryHealth;
    public int batteryScale;

    public CRunApp app;
    
    public static LinkedList <String> nativeExtensions;
    public static LinkedList <String> otherObjects;
    
    public BroadcastReceiver receiver = new BroadcastReceiver()
    {
		@Override
		public void onReceive(Context context, Intent intent)
		{
			/* 1.5 emulator:
			 
			     status, icon-small, temperature, level, plugged, 
			     scale, present, health, technology, voltage */
			
			batteryLevel = intent.getIntExtra("level", 0);
			batteryStatus = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
			batteryPlugged = intent.getIntExtra("plugged", 0xDEADBEEF);
			batteryHealth = intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
			batteryScale = intent.getIntExtra("scale", 1);
			
			batteryReceived = true;
		}
    };

    public boolean inputStreamToFile (InputStream input, String filename)
    {
    	try
    	{
	    	FileOutputStream output = this.openFileOutput (filename, 0);

	    	byte [] buffer = new byte [1024 * 8];
	    	int total = 0;

	    	for (;;)
	    	{
	    		int length = input.read (buffer);

	    		if (length < 0)
	    			break;

	    		total += length;

	    		output.write(buffer, 0, length);
	    	}

            buffer = null;

	    	Log.Log("inputStreamToFile: " + total + " bytes");

	    	output.close ();
    	}
    	catch(Exception e)
    	{
	    	Log.Log("inputStreamToFile: FAILED: " + e);
            return false;
	    }

        return true;
    }

    private void assetToFile (String asset)
    {
        try
        {
            inputStreamToFile (getResources().getAssets().open("mmf/" + ABI + "/" + asset), asset);
        }
        catch (Exception e)
        {
        }
    }

    Thread mainThread;

    boolean created = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        Log.Log("onCreate, runtime version: " + MMFRuntime.version);


        /* the su binary should only be present on rooted devices */

        try
        {   rooted = Runtime.getRuntime().exec("/system/xbin/which su").exitValue() == 0;
        }
        catch (Throwable e)
        {
        }

        if (!rooted)
            rooted = android.os.Build.TAGS != null && android.os.Build.TAGS.contains("test-keys");

        try
        {
            BufferedReader buildProp = new BufferedReader(new InputStreamReader(new FileInputStream("/system/build.prop")));

            for(;;)
            {
                String line = buildProp.readLine();

                if(line.toLowerCase().indexOf("ouya") != -1)
                {
                    OUYA = true;
                    break;
                }
            }
        }
        catch(Throwable e)
        {
        }

        Log.Log("MMFRuntime/OUYA: " + OUYA);

        CrashReporter.addInfo ("OUYA", Boolean.toString(OUYA));

        mainThread = Thread.currentThread ();

        try
        {
            PackageInfo manager=getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersion = manager.versionName;
        }
        catch (Throwable t)
        {
            Log.Log ("Error retrieving app version: " + t);

            appVersion = "";
        }

        Log.Log ("appVersion: " + appVersion);

        MMFRuntime.installCrashReporter ();

        CrashReporter.addInfo ("Package", this.getClass ().getName ());

        MMFRuntime.inst = this;

        super.onCreate(savedInstanceState);

        viewportX = viewportY = 0;
        scaleX = scaleY = 1.0f;

        Native.init (this.getClass().getPackage().getName(),
                        getApplicationInfo ().dataDir + "/libs");

        ABI = Native.getABI ();
        Log.Log ("ABI is " + ABI);

        nativeExtensions = new LinkedList <String> ();
        otherObjects = new LinkedList <String> ();
        
        try
        {
            String assets [] = getResources().getAssets().list("mmf/" + ABI);

            for (int i = 0; i < assets.length; ++ i)
            {
            	String asset = assets [i].replaceAll(".so", "");
            	
            	if (asset.startsWith("CRun"))
            	{
            		nativeExtensions.add (asset);
            	}
            	else
            	{
            		otherObjects.add (asset);
            	}
            }
            
            Iterator <String> it = otherObjects.iterator ();
            
            while (it.hasNext())
            {
            	String object = it.next() + ".so";
            	
            	assetToFile (object);

            	System.load(this.getFilesDir() + "/" + object);

                this.deleteFile(object);
            }
            
        	it = nativeExtensions.iterator ();
        	
            while (it.hasNext())
            {
            	String extension = it.next();
            	
            	assetToFile (extension + ".so");
            	
            	Native.load(extension,
                        this.getFilesDir() + "/" + extension + ".so");

        	    this.deleteFile(extension + ".so");
            }
        	
    		it = otherObjects.iterator ();
            
            while (it.hasNext())
            {
            	this.deleteFile(it.next() + ".so");          	
            }
        }
        catch(Exception e)
        {
        }

        registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        app = new CRunApp();
        app.load(null);

        container = new RelativeLayout (this);

        container.addView (mainView = new MainView (this),
                new RelativeLayout.LayoutParams
                        (RelativeLayout.LayoutParams.FILL_PARENT,
                                RelativeLayout.LayoutParams.FILL_PARENT));

        mainView.setId (1);

        app.createControlView ();

        app.updateWindowDimensions(app.widthSetting, app.heightSetting);

        if((app.hdr2Options & CRunApp.AH2OPT_KEYBOVERAPPWINDOW) != 0)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        else
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        if((app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) == 0)
        {
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        app.startApplication();

        setContentView(container);

        if((app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) == 0)
        {
            try
            {
                Class p [] = new Class[1];
                p [0] = Integer.TYPE;

                View v = getWindow().getDecorView ();

                Method m = v.getClass().getMethod ("setSystemUiVisibility", p);

                Object args [] = new Object [1];
                args [0] = new Integer (1 | 2);

                try
                {
                    m.invoke (v, args);

                    Log.Log ("setSystemUiVisibility() called successfully");
                }
                catch (IllegalAccessException e)
                {   Log.Log ("setSystemUiVisibility() : " + e);
                }
                catch (InvocationTargetException e)
                {   Log.Log ("setSystemUiVisibility() : " + e);
                }
            }
            catch (NoSuchMethodException e)
            {
                Log.Log ("setSystemUiVisibility() not found");
            }
        }

        created = true;
    }

    public void setFrameRate (int frameRate)
    {
        Log.Log ("setFrameRate to " + frameRate);

        long interval = frameRate != 0 ? (long) (1000 / frameRate) : 0;

        if (timerTask != null)
        {
            if ((!timerTask.dead) && timerTask.interval == interval)
                return;

            synchronized (timerTask)
            {
                timerTask.dead = true;
                timerTask = null;
            }
        }

        if (interval != 0)
        {
            Handler handler = new Handler ();
            timerTask = new CRunTimerTask(handler, interval);

            handler.post(timerTask);
        }
    }

    public boolean initialUpdateDone = false;

    public void updateViewport ()
    {
        if (!created)
            return;

        if (SurfaceView.inst != null)
            SurfaceView.inst.makeCurrent();

        Log.Log("Thread " + Thread.currentThread() + " updating viewport");

        currentWidth = mainView.currentWidth;
        currentHeight = mainView.currentHeight;

        Log.Log("Android.MMFRuntime updating viewport - width "
                + currentWidth + ", height " + currentHeight);

        boolean stretchWindowToViewport = false;

    	app.gaCxWin = app.widthSetting;
    	app.gaCyWin = app.heightSetting;
    	
    	scaleX = scaleY = 1.0f;
    	
        float appAspect = ((float) app.gaCxWin) / ((float) app.heightSetting);
        float screenAspect = ((float) currentWidth) / ((float) currentHeight);
        
        switch(app.viewMode)
        {
        /* resize window to screen size (no scaling) */
        case CRunApp.VIEWMODE_ADJUSTWINDOW:

        	stretchWindowToViewport = false;
        	
            viewportWidth = currentWidth;
            viewportHeight = currentHeight;

            if (app.frame != null)
            {
                if(viewportWidth > app.frame.leWidth)
                    viewportWidth = app.frame.leWidth;

                if(viewportHeight > app.frame.leHeight)
                    viewportHeight = app.frame.leHeight;
            }

            viewportX = (currentWidth / 2) - (viewportWidth / 2);
            viewportY = (currentHeight / 2) - (viewportHeight / 2);

            app.gaCxWin = viewportWidth;
            app.gaCyWin = viewportHeight;

            app.updateWindowDimensions(viewportWidth, viewportHeight);
            
        	break;
        	
    	/* center unscaled app in screen */
        case CRunApp.VIEWMODE_CENTER: 

        	stretchWindowToViewport = false;
        	
        	viewportWidth = app.gaCxWin;
        	viewportHeight = app.gaCyWin;

            viewportX = (currentWidth / 2) - (viewportWidth / 2);
            viewportY = (currentHeight / 2) - (viewportHeight / 2);

            app.updateWindowDimensions(app.gaCxWin, app.gaCyWin);        	
            
        	break;
        
    	/* scale, keep aspect ratio, add borders to match screen ratio */
        case CRunApp.VIEWMODE_FITINSIDE_BORDERS:

        /* grow window to match screen ratio, scale to screen size */
        case CRunApp.VIEWMODE_FITINSIDE_ADJUSTWINDOW:

        	stretchWindowToViewport = true;

            scaleX = scaleY = Math.min (((float) currentWidth) / app.gaCxWin,
            							((float) currentHeight) / app.gaCyWin);

            viewportWidth = (int) (app.gaCxWin * scaleX);
            viewportHeight = (int) (app.gaCyWin * scaleY);

            if(app.viewMode == CRunApp.VIEWMODE_FITINSIDE_ADJUSTWINDOW)
            {
                if(viewportWidth < currentWidth)
                    app.gaCxWin = Math.round(currentWidth / scaleX);

                if(viewportHeight < currentHeight)
                    app.gaCyWin = Math.round(currentHeight / scaleY);

                viewportWidth = (int) (app.gaCxWin * scaleX);
                viewportHeight = (int) (app.gaCyWin * scaleY);
            }

            viewportX = (currentWidth / 2) - (viewportWidth / 2);
            viewportY = (currentHeight / 2) - (viewportHeight / 2);

            app.updateWindowDimensions(app.gaCxWin, app.gaCyWin);
            
        	break;
        	
    	/* scale, keep aspect ratio, allow chopping off to fill screen */
        case CRunApp.VIEWMODE_FITOUTSIDE:

            if(appAspect < screenAspect)
                app.gaCyWin = (int) (app.gaCxWin / screenAspect);
            else
                app.gaCxWin = (int) (app.gaCyWin * screenAspect);

            scaleX = scaleY = Math.min (((float) currentWidth) / app.gaCxWin,
            							((float) currentHeight) / app.gaCyWin);

        	stretchWindowToViewport = true;
        	
            viewportWidth = (int) (app.gaCxWin * scaleX);
            viewportHeight = (int) (app.gaCyWin * scaleY);

            viewportX = (currentWidth / 2) - (viewportWidth / 2);
            viewportY = (currentHeight / 2) - (viewportHeight / 2);

            app.updateWindowDimensions(app.gaCxWin, app.gaCyWin);
            
        	break;

    	/* stretch game window, ignore aspect ratio */
        case CRunApp.VIEWMODE_STRETCH:

        	stretchWindowToViewport = true;

            scaleX = ((float) currentWidth) / app.gaCxWin;
            scaleY = ((float) currentHeight) / app.gaCyWin;
            
            viewportWidth = (int) (app.gaCxWin * scaleX);
            viewportHeight = (int) (app.gaCyWin * scaleY);

            viewportX = (currentWidth / 2) - (viewportWidth / 2);
            viewportY = (currentHeight / 2) - (viewportHeight / 2);

            app.updateWindowDimensions(app.gaCxWin, app.gaCyWin);
            
        	break;
        };

        Log.Log ("uV: initialUpdateDone " + initialUpdateDone + ", GLRenderer is " + GLRenderer.inst);

        if (!initialUpdateDone)
        {
            initialUpdateDone = true;

            /* nb. onSurfaceCreated calls setFrameRate, which starts the timer */

            app.setSurfaceEnabled (true); /* for now */
        }
        else
        {
            if (GLRenderer.inst != null)
            {
            	Log.Log("Setting renderer limits...");
            	
            	GLRenderer.limitX = MMFRuntime.inst.viewportX + MMFRuntime.inst.app.gaCxWin;
            	GLRenderer.inst.setLimitX(GLRenderer.limitX);

            	GLRenderer.limitY = MMFRuntime.inst.viewportY + MMFRuntime.inst.app.gaCyWin;
            	GLRenderer.inst.setLimitY(GLRenderer.limitY);

            	GLRenderer.inst.updateViewport(stretchWindowToViewport);
            }

            if(app != null)
            {
                /* Update the frame size */

                if(app.frame != null)
                    app.frame.updateSize();

                /* Redraw everything (some objects may now be visible etc.) */

                if(app.run != null)
                    app.run.redrawLevel(CRun.DLF_DRAWOBJECTS|CRun.DLF_REDRAWLAYER);
            }
        }
    }

    @Override
    protected void onDestroy() 
    {
    	app.endApplication();

    	unregisterReceiver(receiver);
    	
    	super.onDestroy();
    }

    @Override
    protected void onPause() 
    {
        if(app != null && app.run != null)
            app.run.pause();

        super.onPause();
    }

    @Override
    protected void onResume()
    {
        if(app != null && app.run != null)
            app.run.resume();

    	super.onResume();
    }

    public int getResourceID (String name)
    {
        String p = getClass().getPackage().getName();

        int id = getResources().getIdentifier(name, null, p);

        Log.Log ("getResourceID for " + name + ", package " + p + ": " + id);

        assert (id > 0);

        return id;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        final int keyCode = event.getKeyCode ();

        if (app != null)
        {
            if (keyCode == KeyEvent.KEYCODE_BACK &&
            		(app.hdr2Options & CRunApp.AH2OPT_DISABLEBACKBUTTON) != 0)
            {
                runOnRuntimeThread (new Runnable()
                {
                    public void run()
                    {
                        app.keyDown(keyCode);
                    }
                });

                return true;
            }
        }

        return super.dispatchKeyEvent (event);
    }

    public Queue <Runnable> toRun = new LinkedList <Runnable> ();

    public void runOnRuntimeThread (final Runnable r)
    {
        synchronized (toRun)
        {
            MMFRuntime.inst.toRun.add (r);
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        super.onCreateOptionsMenu (menu);

        boolean display = false;

        CRun run = app.run;

        if (run == null)
            return false;

        if (run.rhNObjects != 0)
        {
            int cptObject = run.rhNObjects;
            int count = 0;

            do
            {
                while (run.rhObjectList[count] == null)
                    ++ count;

                CObject object = run.rhObjectList[count ++];

                if (object instanceof CExtension
                        && ((CExtension) object).ext.onCreateOptionsMenu (menu) == true
                                && display == false)
                {
                    display = true;
                }

                -- cptObject;

            } while (cptObject != 0);
        }
        
        if (!display)
        {
        	/* Not handled by an extension */

        	if (app.androidMenu != null && app.androidMenu.length > 0)
        	{
        		Log.Log("Options menu not handled by an extension - using app menu (" + app.androidMenu.length + " options)");
        		
        		for (int i = 0; i < app.androidMenu.length; ++ i)
        		{
        			MenuEntry entry = app.androidMenu[i];
        			
        			entry.item = menu.add(entry.title);
        			
        			String resource = String.format("drawable/optmenu%03d", i);
        			int resID = getResourceID(resource);

        			Log.Log("Menu icon resource " + resource + " -> res ID " + resID);
        			
        			if (resID != -1)
        				entry.item.setIcon(resID);
        			
        			if (entry.disabled)
        				entry.item.setEnabled(false);
        		}
        		
        		return true;
        	}
        }
        
        return display;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	for(MenuEntry entry : app.androidMenu)
    	{
    		if(entry.item == item)
    		{
    			for(CRunAndroid obj : app.androidObjects)
    			{
    				obj.menuItem = entry.id;

    				obj.menuItemEvent = obj.ho.getEventCount();
    				obj.ho.generateEvent(20, 0);

    				obj.anyMenuItemEvent = obj.ho.getEventCount();
    				obj.ho.generateEvent(21, 0);
    			}
    		}
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
    
 }

