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

package Extensions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import Application.CRunApp;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.Vibrator;
import android.telephony.*;
import android.net.*;
import android.view.*;
import android.os.BatteryManager;
import android.os.Environment;

import Services.*;
import RunLoop.*;
import Conditions.*;
import Actions.*;
import Application.CRunApp.MenuEntry;
import Expressions.*;
import Objects.*;
import Extensions.*;
import OpenGL.*;
import Runtime.*;
import android.view.inputmethod.InputMethodManager;

public class CRunAndroid extends CRunExtension
{
	String deviceID;
	String logTag;
	
	Intent intentOut;
	
	Map<String, BroadcastReceiver> intentsIn;
	Intent intentIn;
	
	public TelephonyManager getTelephonyManager()
	{
		return (TelephonyManager) MMFRuntime.inst.getSystemService(Context.TELEPHONY_SERVICE);
	}
	
	public ConnectivityManager getConnectivityManager()
	{
		return (ConnectivityManager) MMFRuntime.inst.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

    public Vibrator getVibrator ()
    {
        return (Vibrator) MMFRuntime.inst.getSystemService(Context.VIBRATOR_SERVICE);
    }

	public NetworkInfo getActiveNetworkInfo()
	{
		return getConnectivityManager().getActiveNetworkInfo();
	}
	
    @Override public int getNumberOfConditions()
    {
        return 22;
    }

    @Override
    public boolean createRunObject(CBinaryFile file, CCreateObjectInfo cob, int version)
    {
    	intentOut = new Intent();
    	intentsIn = new HashMap<String, BroadcastReceiver>();
        
    	logTag = ho.getApplication().appName;

        ho.getApplication().androidObjects.add (this);

        return true;
    }

    @Override
    public void destroyRunObject(boolean bFast)
    {
        ho.getApplication().androidObjects.remove (this);
        
        for(BroadcastReceiver receiver : intentsIn.values())
        	MMFRuntime.inst.unregisterReceiver (receiver);

        intentsIn.clear();
    }

    @Override
    public int handleRunObject()
    {

        return 0;
    }

    public int menuButtonEvent = -1;
    public int backButtonEvent = -1;
    public int intentEvent = -1;
    public int anyIntentEvent = -1;
    
    public int menuItemEvent = -1;
    public int anyMenuItemEvent = -1;
    public String menuItem;

    public int asyncLoadCompleteEvent = -1;
    public int asyncLoadFailedEvent = -1;

    @Override
    public boolean condition(int num, CCndExtension cnd)
    {
        switch (num)
        {
            case 0: /* Device has a GPU? */
                return GLRenderer.inst.gpuVendor.compareTo("Android") != 0;
                
            case 1: /* User is roaming? */
            {
            	TelephonyManager tm = getTelephonyManager();
            	
            	if(tm == null)
            		return false;
            	
                return tm.isNetworkRoaming();
            }
            
            case 2: /* On extension exception */
            	return false;
            	
            case 3: /* Network is connected? */

                try
                {
            	    return getActiveNetworkInfo().isConnected();
                }
                catch (Throwable t)
                {
                    return false;
                }

            case 4: /* Device is plugged in? */
            	
            	if(!MMFRuntime.inst.batteryReceived)
            		return false;
            	
            	return MMFRuntime.inst.batteryPlugged == BatteryManager.BATTERY_PLUGGED_AC ||
            	MMFRuntime.inst.batteryPlugged == BatteryManager.BATTERY_PLUGGED_USB;
            	
            case 5: /* Device is plugged in to an AC adapter? */

            	if(!MMFRuntime.inst.batteryReceived)
            		return false;
            	
            	return MMFRuntime.inst.batteryPlugged == BatteryManager.BATTERY_PLUGGED_AC;
            	
            case 6: /* Device is plugged in to a USB port? */

            	if(!MMFRuntime.inst.batteryReceived)
            		return false;
            	
            	return MMFRuntime.inst.batteryPlugged == BatteryManager.BATTERY_PLUGGED_USB;
            	
            case 7: /* Battery is charging? */

            	if(!MMFRuntime.inst.batteryReceived)
            		return false;
            	
            	return MMFRuntime.inst.batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING;
        	
            case 8: /* Battery is discharging? */

            	if(!MMFRuntime.inst.batteryReceived)
            		return false;
            	
            	return MMFRuntime.inst.batteryStatus == BatteryManager.BATTERY_STATUS_DISCHARGING;

            case 9: /* Battery is full? */

            	if(!MMFRuntime.inst.batteryReceived)
            		return false;
            	
            	return MMFRuntime.inst.batteryStatus == BatteryManager.BATTERY_STATUS_FULL;

            case 10: /* On back button pressed */

                return ho.getEventCount() == backButtonEvent;

            case 11: /* On home button pressed */

                return false;

            case 12: /* On menu button pressed */

                return ho.getEventCount() == menuButtonEvent;

            case 13: /* Device is rooted? */

                return MMFRuntime.rooted;

            case 14: /* Bluetooth enabled? */
            {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

                if (adapter == null || !adapter.isEnabled ())
                    return false;

                return true;
            }

            case 15: /* Back button down? */

                return rh.rhApp.keyBuffer[KeyEvent.KEYCODE_BACK] != 0;

            case 16: /* Menu button down? */

                return rh.rhApp.keyBuffer[KeyEvent.KEYCODE_MENU] != 0;

            case 17: /* Button %0 down? */

                int keyCode = cnd.getParamExpression(rh, 0);

                if (keyCode < 0 || keyCode >= rh.rhApp.keyBuffer.length)
                    return false;

                return rh.rhApp.keyBuffer[keyCode] != 0;

            case 18: /* On incoming intent with action %0 */

            	return intentIn != null && intentIn.getAction() == cnd.getParamExpString(rh, 0)
            				&& ho.getEventCount() == intentEvent;
            	
            case 19: /* On any incoming intent */

            	return intentIn != null && ho.getEventCount() == anyIntentEvent;

            case 20: /* On options menu item selected */

            	return ho.getEventCount() == menuItemEvent &&
            				menuItem.compareToIgnoreCase(cnd.getParamExpString(rh, 0)) == 0;

            case 21: /* On any options menu item selected */

            	return ho.getEventCount() == anyMenuItemEvent;

            case 22: /* On async load complete */

                return ho.getEventCount() == asyncLoadCompleteEvent;

            case 23: /* On async load failed */

                return ho.getEventCount() == asyncLoadFailedEvent;
        }
        
        return false;
    }

    @Override
    public void action(int num, CActExtension act)
    {
        switch (num)
        {
        	/* Log actions */
        
            case 0:    	
            	android.util.Log.d(logTag, act.getParamExpString(rh, 0));
                break;
            case 1:    	
            	android.util.Log.e(logTag, act.getParamExpString(rh, 0));
                break;
            case 2:    	
            	android.util.Log.i(logTag, act.getParamExpString(rh, 0));
                break;
            case 3:    	
            	android.util.Log.v(logTag, act.getParamExpString(rh, 0));
                break;
            case 4:    	
            	android.util.Log.w(logTag, act.getParamExpString(rh, 0));
                break;

            case 5: /* Set log tag */	
            	logTag = act.getParamExpString(rh, 0);
                break;

            case 6: /* Start sleep prevention */

                MMFRuntime.inst.runOnUiThread (new Runnable ()
                {   public void run ()
                    {   MMFRuntime.inst.getWindow().addFlags
                                (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                });

                break;

            case 7: /* Stop sleep prevention */

                MMFRuntime.inst.runOnUiThread (new Runnable ()
                {   public void run ()
                    {   MMFRuntime.inst.getWindow().clearFlags
                                (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                });

                break;

            case 8: /* Hide status bar */

                break;

            case 9: /* Show status bar */
            	
                break;
                
            case 10: /* Open URL */
            {
                try
                {
                    String url = act.getParamExpString(rh, 0);

                    if(url.indexOf("://") == -1)
                        url = "http://" + url;

                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    MMFRuntime.inst.startActivity(intent);
                }
                catch (Throwable e)
                {
                }

                break;
            }

            case 11: /* Start intent */
            {
                try
                {
                	intentOut.setAction(act.getParamExpString(rh, 0));
                	intentOut.setData(Uri.parse (act.getParamExpString(rh, 1)));
                	
                    MMFRuntime.inst.startActivity(intentOut);
                }
                catch (Throwable e)
                {
                    android.util.Log.e("MMFRuntime", "Error starting intent: " + e.toString());
                }

                intentOut = new Intent();
                
                break;
            }

            case 12: /* Vibrate */

                getVibrator ().vibrate (act.getParamExpression (rh, 0));

                break;

            case 13: /* Show keyboard */

                MMFRuntime.inst.getWindow().setSoftInputMode
                        (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                break;

            case 14: /* Hide keyboard */

                MMFRuntime.inst.getWindow().setSoftInputMode
                        (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                /*InputMethodManager imm = (InputMethodManager) MMFRuntime.inst.getSystemService(
                        Context.INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);*/

                break;
                
            case 15: /* Add intent category */
            	
            	intentOut.addCategory (act.getParamExpString(rh, 0));
            	break;

            case 16: /* Add intent data (string) */
            	
            	intentOut.putExtra (act.getParamExpString(rh, 0), act.getParamExpString(rh, 1));
            	break;

            case 17: /* Add intent data (boolean) */
            	
            	intentOut.putExtra(act.getParamExpString(rh, 0), act.getParamExpression(rh, 1) != 0);
            	break;

            case 18: /* Add intent data (long) */
            	
            	intentOut.putExtra(act.getParamExpString(rh, 0), (long) act.getParamExpression(rh, 1));
            	break;
            	
            case 19: /* Subscribe to action */
            	
            	try
            	{
	            	if (intentsIn.get(act.getParamExpString(rh,  0)) != null)
	            		break;
	            	
	            	BroadcastReceiver receiver = new BroadcastReceiver()
	                {
	            		@Override
	            		public void onReceive(Context context, Intent intent)
	            		{
	            			intentIn = intent;
	            			
	            		  	intentEvent = ho.getEventCount();
	            		  	ho.generateEvent(18, 0);
	
	            			anyIntentEvent = ho.getEventCount();
	            			ho.generateEvent(19, 0);   
	            			
	            			intentIn = null;
	            		}
	                };
	                
	                MMFRuntime.inst.registerReceiver
	                	(receiver, new IntentFilter(act.getParamExpString(rh, 1)));
	                
	                intentsIn.put(act.getParamExpString(rh, 0), receiver);
            	}
            	catch(Exception e)
            	{
            	}
	                
            	break;

            case 20: /* Unsubscribe from action */
            	
            	BroadcastReceiver removed = intentsIn.remove(act.getParamExpString(rh, 0));
            	
            	if (removed != null)
            		MMFRuntime.inst.unregisterReceiver(removed);

            	break;     

            case 21: /* Start intent with chooser */
            {
                try
                {
                	intentOut.setAction(act.getParamExpString(rh, 0));
                	intentOut.setData(Uri.parse (act.getParamExpString(rh, 1)));
                	
                    MMFRuntime.inst.startActivity
                    	(Intent.createChooser (intentOut, act.getParamExpString(rh, 2)));
                }
                catch (Throwable e)
                {
                    android.util.Log.e("MMFRuntime", "Error starting intent: " + e.toString());
                }

                intentOut = new Intent();
                
                break;
            }
                
            case 22: /* Enable options menu item */
            {
            	String id = act.getParamExpString(rh, 0);
            	
            	if(rh.rhApp.androidMenu == null)
            		break;
            	
            	for(MenuEntry item : rh.rhApp.androidMenu)
            	{
            		if(item.id.equalsIgnoreCase(id))
            		{
            			item.disabled = false;
            			break;
            		}
            	}
            	
            	break;
            }
            
            case 23: /* Disable options menu item */
            {
            	String id = act.getParamExpString(rh, 0);
            	
            	if(rh.rhApp.androidMenu == null)
            		break;
            	
            	for(MenuEntry item : rh.rhApp.androidMenu)
            	{
            		if(item.id.equalsIgnoreCase(id))
            		{
            			item.disabled = true;
            			break;
            		}
            	}
            	
            	break;
            }
            
            case 24: /* Load image async */

                /*String url = act.getParamExpString(rh, 0);
            	CObject object = act.getParamObject(rh, 1);

            	if(! (object instanceof CExtension))
            	    return;

            	if (((CExtension) object).ext instanceof CRunkcpica)
                {
                    final CRunkcpica ext = (CRunkcpica) ((CExtension) object).ext;

                    ho.retrieveHFile(url, new CRunApp.FileRetrievedHandler()
                    {
                        @Override
                        public void onRetrieved(CRunApp.HFile file, java.io.InputStream stream)
                        {
                            try
                            {
                                Log.Log("Android object: Image retrieved, " + stream.available() + " bytes available");
                            }
                            catch(IOException e)
                            {
                            }

                            ext.load(stream);

                            asyncLoadCompleteEvent = ho.getEventCount();
                            ho.generateEvent(22, 0);
                        }

                        @Override
                        public void onFailure()
                        {
                            Log.Log("Android object: Failure w/ async image download");

                            asyncLoadFailedEvent = ho.getEventCount();
                            ho.generateEvent(23, 0);
                        }
                    });
                }                   */

            	break;
        }
    }

    @Override
    public CValue expression(int num)
    {
    	String key;
    	
        switch (num)
        {
            case 0: /* GPU_Name$ */
                return new CValue(GLRenderer.inst.gpu);

            case 1: /* GPU_Vendor$ */
                return new CValue(GLRenderer.inst.gpuVendor);

            case 2: /* DeviceID$ */
            {
                TelephonyManager tm = getTelephonyManager();

                if(tm == null)
                    return new CValue("");

                String id = tm.getDeviceId();

                return new CValue(id != null ? id : "");
            }

            case 3: /* Operator$ */
            {
            	TelephonyManager tm = getTelephonyManager();
            	
            	if(tm == null)
            		return new CValue("");
            	
                return new CValue(tm.getNetworkOperatorName().trim());
            }
            
            case 4: /* StackTrace$ */

                return new CValue("");
            
            case 5: /* AppTitle$() */
            	return new CValue(ho.getApplication().appName);
            	
            case 6: /* BatteryPercentage() */

            	if(!MMFRuntime.inst.batteryReceived)
            		return new CValue(-1);
            	
            	return new CValue(100 * ((float) MMFRuntime.inst.batteryLevel / (float) MMFRuntime.inst.batteryScale));
        
            case 7:
            	return new CValue(ho.getApplication().gaCxWin);

            case 8:
            	return new CValue(ho.getApplication().gaCyWin);

            case 9:

                return new CValue (MMFRuntime.inst.getFilesDir ().toString ());

            case 10:

                 return new CValue (rh.getTempPath ());

            case 11:

                 return new CValue (CServices.getAndroidID ());

             case 12:

                return new CValue (MMFRuntime.inst.getClass ().getName ());

            case 13:

                return new CValue (Environment.getExternalStorageDirectory ().getAbsolutePath());

            case 14:

                return new CValue (MMFRuntime.appVersion);

            case 15:

                return new CValue (MMFRuntime.version);

            case 16:

                if (MMFRuntime.inst.adView == null)
                    return new CValue (0);

                return new CValue (MMFRuntime.inst.adView.getHeight ());

            case 17:

                return new CValue (GLRenderer.inst.glVersion);
                
            case 18: /* IntentAction$ */
            	
            	if (intentIn == null)
            		return new CValue("");
            	
            	return new CValue(intentIn.getAction());

            case 19: /* IntentData$ */
            	
            	if (intentIn == null)
            		return new CValue("");
            	
            	return new CValue(intentIn.getDataString());

            case 20: /* IntentExtra_String$ */
            	
            	key = ho.getExpParam().getString();
            	
            	if (intentIn == null)
            		return new CValue("");
            	
            	String extra = intentIn.getStringExtra(key);
            	
            	return new CValue(extra == null ? "" : extra);

            case 21: /* IntentExtra_Boolean */
            	
            	key = ho.getExpParam().getString();
            	
            	if (intentIn == null)
            		return new CValue("");
            	
            	return new CValue(intentIn.getBooleanExtra(key, false) ? 1 : 0);

            case 22: /* IntentExtra_Long */
            	
            	key = ho.getExpParam().getString();
            	
            	if (intentIn == null)
            		return new CValue("");
            	
            	return new CValue(intentIn.getLongExtra(key, 0));
            	
            case 23: /* MenuItem$ */
            	
            	return new CValue(menuItem);
        }

        return new CValue(0);
    }
    
}
