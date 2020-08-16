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
// ON LOOP
//
//----------------------------------------------------------------------------------
package Conditions;

import RunLoop.*;
import Objects.*;
import Params.*;
import Expressions.*;

public class CND_ONLOOP extends CCnd
{
    public boolean eva1(CRun rhPtr, CObject hoPtr)
    {
        String pName=rhPtr.get_EventExpressionStringLowercase((CParamExpression) evtParams[0]);
        if (rhPtr.rh4CurrentFastLoop.equals (pName) == false)
            return false;
        rhPtr.rhEvtProg.rh2ActionOn=false;
        return true;
    }
    public boolean eva2(CRun rhPtr)
    {
        return false;
    }
    
}
