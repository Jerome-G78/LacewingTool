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
// CRUNEXTENSION: Classe abstraite run extension
//
//----------------------------------------------------------------------------------
package Extensions;

import Objects.*;
import RunLoop.*;
import Expressions.*;
import Services.*;
import Sprites.*;
import Conditions.*;
import Actions.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public abstract class CRunExtension 
{   
    public final static int REFLAG_DISPLAY=1;
    public final static int REFLAG_ONESHOT=2;
    public CExtension ho;
    public CRun rh;
    
    public CRunExtension() 
    {
    }

    public void init(CExtension hoPtr)
    {
		ho=hoPtr;
		rh=hoPtr.hoAdRunHeader;
    }
    
    public int getNumberOfConditions()
    {
    	return 0;
    }
    
    public boolean createRunObject(CBinaryFile file, CCreateObjectInfo cob, int version)
    {
    	return false;
    }
    
    public int handleRunObject()
    {
    	return REFLAG_ONESHOT;
    }
    
    public void displayRunObject()
    {
    }

    public void reinitDisplay ()
    {
    }

    public void destroyRunObject(boolean bFast)
    {
    }
    
    public void pauseRunObject()
    {
    }
    
    public void continueRunObject()
    {
    }

    public void getZoneInfos()
    {
    }
    
    public boolean condition(int num, CCndExtension cnd)
    {
    	return false;
    }
    
    public void action(int num, CActExtension act)
    {
    }
    
    public CValue expression(int num)
    {
    	return new CValue(0);
    }
    
    public CMask getRunObjectCollisionMask(int flags)
    {
    	return null;
    }
    
    public CFontInfo getRunObjectFont()
    {
    	return null;
    }
    
    public void setRunObjectFont(CFontInfo fi, CRect rc)
    {
    }
    
    public int getRunObjectTextColor()
    {
    	return 0;
    }
    
    public void setRunObjectTextColor(int rgb)  
    {
    }

    public boolean onCreateOptionsMenu (Menu menu)
    {
        return false;
    }

    public boolean onPrepareOptionsMenu (Menu menu)
    {
        return false;
    }
}
