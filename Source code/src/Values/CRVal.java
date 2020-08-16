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
// CRVAL : Alterable values et strings
//
//----------------------------------------------------------------------------------
package Values;

import RunLoop.*;
import OI.*;
import Objects.*;
import Expressions.*;

public class CRVal 
{   
    public static final int VALUES_NUMBEROF_ALTERABLE=26;
    public static final int STRINGS_NUMBEROF_ALTERABLE=10;
    
    public int rvValueFlags;
    public CValue rvValues[];
    public String rvStrings[];
    
    public CRVal() 
    {
    }
    public void init(CObject ho, CObjectCommon ocPtr, CCreateObjectInfo cob)
    {
	// Creation des tableaux
        rvValueFlags=0;
		rvValues=new CValue[VALUES_NUMBEROF_ALTERABLE];
		rvStrings=new String[STRINGS_NUMBEROF_ALTERABLE];
		int n;
		for (n=0; n<VALUES_NUMBEROF_ALTERABLE; n++)
		{
		    rvValues[n]=null;
		}
		for (n=0; n<STRINGS_NUMBEROF_ALTERABLE; n++)
		{
		    rvStrings[n]=null;
		}
	
		// Initialisation des valeurs
		if (ocPtr.ocValues!=null)
		{
		    CValue value;
		    for (n=0; n<ocPtr.ocValues.nValues; n++)
		    {
			value=getValue(n);
			value.forceInt(ocPtr.ocValues.values[n]);
		    }
		}
		if (ocPtr.ocStrings!=null)
		{
		    for (n=0; n<ocPtr.ocStrings.nStrings; n++)
		    {
			rvStrings[n]=ocPtr.ocStrings.strings[n];
		    }
		}
    }
    public void kill(boolean bFast)
    {
		int n;
		for (n=0; n<VALUES_NUMBEROF_ALTERABLE; n++)
		{
		    rvValues[n]=null;
		}
		for (n=0; n<STRINGS_NUMBEROF_ALTERABLE; n++)
		{
		    rvStrings[n]=null;
		}
    }
    public CValue getValue(int n)
    {
		if (rvValues[n]==null)
		{
		    rvValues[n]=new CValue();
		}
		return rvValues[n];
    }
    public String getString(int n)
    {
		if (rvStrings[n]==null)
		{
		    rvStrings[n]=new String("");
		}
		return rvStrings[n];
    }
    public void setString(int n, String s)
    {
		rvStrings[n]=new String(s);
    }
}
