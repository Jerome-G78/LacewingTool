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
// CEVENTGROUP: Groupe d'evenements
//
//----------------------------------------------------------------------------------
package Events;

import Actions.CAct;
import Conditions.CCnd;
import Application.*;

public class CEventGroup 
{
    public byte evgNCond;
    public byte evgNAct;
    public short evgFlags;
    public short evgInhibit;
    public short evgInhibitCpt;
    public short evgIdentifier;
    public CEvent evgEvents[]=null;

    // Internal flags of eventgroups
    public static final short EVGFLAGS_ONCE=0x0001;
    public static final short EVGFLAGS_NOTALWAYS=0x0002;
    public static final short EVGFLAGS_REPEAT=0x0004;
    public static final short EVGFLAGS_NOMORE=0x0008;
    public static final short EVGFLAGS_SHUFFLE=0x0010;
    public static final short EVGFLAGS_EDITORMARK=0x0020;
    public static final short EVGFLAGS_UNDOMARK=0x0040;
    public static final short EVGFLAGS_COMPLEXGROUP=0x0080;
    public static final short EVGFLAGS_BREAKPOINT=0x0100;
    public static final short EVGFLAGS_ALWAYSCLEAN=0x0200;
    public static final short EVGFLAGS_ORINGROUP=0x0400;
    public static final short EVGFLAGS_STOPINGROUP=0x0800;
    public static final short EVGFLAGS_ORLOGICAL=0x1000;
    public static final short EVGFLAGS_GROUPED=0x2000;
    public static final short EVGFLAGS_INACTIVE=0x4000;
    public static final short EVGFLAGS_NOGOOD=(short)0x8000;
    public static final short EVGFLAGS_LIMITED=(short)(EVGFLAGS_SHUFFLE+EVGFLAGS_NOTALWAYS+EVGFLAGS_REPEAT+EVGFLAGS_NOMORE);
    public static final short EVGFLAGS_DEFAULTMASK=(short)(EVGFLAGS_BREAKPOINT+EVGFLAGS_GROUPED);
    
    public CEventGroup() 
    {
    }
    public static CEventGroup create(CRunApp app) 
    {
        long debut=app.file.getFilePointer();
        
        short size=app.file.readAShort();          // evgSize
        CEventGroup evg=new CEventGroup();
        
        evg.evgNCond=(byte)app.file.readByte();
        evg.evgNAct=(byte)app.file.readByte();
        evg.evgFlags=app.file.readAShort();
        evg.evgInhibit=app.file.readAShort();
        evg.evgInhibitCpt=app.file.readAShort();
        evg.evgIdentifier=app.file.readAShort();
        app.file.skipBytes(2);          // evgUndo
        
        evg.evgEvents=new CEvent[evg.evgNCond+evg.evgNAct];
        int n;
        int count=0;
        for (n=0; n<evg.evgNCond; n++)
        {
            evg.evgEvents[count++]=CCnd.create(app);
        }

        for (n=0; n<evg.evgNAct; n++)
        {
            evg.evgEvents[count++]=CAct.create(app);
        }
        
        // Positionne en fin de groupe
        app.file.seek(debut-size);
        
        return evg;
    }
}
