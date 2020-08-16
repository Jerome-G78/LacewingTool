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
// CRMVT : Donnees de base d'un mouvement
//
//----------------------------------------------------------------------------------
package Movements;

import RunLoop.*;
import OI.*;
import Objects.*;

public class CRMvt
{
    public int rmMvtNum;					// Number of the current movement
    public CMove rmMovement = null;
    public byte rmWrapping;					// For CHECK POSITION
    public boolean rmMoveFlag = false;					// Messages/movements
    public int rmReverse = 0;					// Ahaid or reverse?
    public boolean rmBouncing = false;					// Bouncing?
    public short rmEventFlags = 0;				// To accelerate events
    public static final short EF_GOESINPLAYFIELD = 0x0001;
    public static final short EF_GOESOUTPLAYFIELD = 0x0002;
    public static final short EF_WRAP = 0x0004;

    public CRMvt()
    {
    }

    public void init(int nMove, CObject hoPtr, CObjectCommon ocPtr, CCreateObjectInfo cob, int forcedType)
    {
        // Effacement du mouvement precedent
        if (rmMovement != null)
        {
            rmMovement.kill();
        }

        // Copie les donnees de base
        // -------------------------
        if (cob != null)
        {
            hoPtr.roc.rcDir = cob.cobDir;					//; Directions
        }
        rmWrapping = hoPtr.hoOiList.oilWrap;				//; Flag pour wrap

        // Initialise les mouvements
        // -------------------------
        CMoveDef mvPtr = null;
        hoPtr.roc.rcMovementType = -1;
        if (ocPtr.ocMovements != null)
        {
            if (nMove < ocPtr.ocMovements.nMovements)
            {
                mvPtr = ocPtr.ocMovements.moveList[nMove];
                rmMvtNum = nMove;
                if (forcedType == -1)
                {
                    forcedType = mvPtr.mvType;
                }
                hoPtr.roc.rcMovementType = forcedType;					//; Le type
                switch (forcedType)
                {
                    // MVTYPE_STATIC
                    case 0:
                    case 1:
                        rmMovement = new CMoveStatic();
                        break;
                    // MVTYPE_RACE
                    case 2:
                        rmMovement = new CMoveRace();
                        break;
                    // MVTYPE_GENERIC
                    case 3:
                        rmMovement = new CMoveGeneric();
                        break;
                    // MVTYPE_BALL
                    case 4:
                        rmMovement = new CMoveBall();
                        break;
                    // MVTYPE_TAPED
                    case 5:
                        rmMovement = new CMovePath();
                        break;
                    // MVTYPE_PLATFORM
                    case 9:
                        rmMovement = new CMovePlatform();
                        break;
                    // MVTYPE_EXT				
                    case 14:
                        rmMovement = loadMvtExtension(hoPtr, (CMoveDefExtension) mvPtr);
                        if (rmMovement == null)
                        {
                            rmMovement = new CMoveStatic();
                        }
                        break;
                }
                hoPtr.roc.rcDir = dirAtStart(hoPtr, mvPtr.mvDirAtStart, hoPtr.roc.rcDir);			//; La direction par defaut
                rmMovement.init(hoPtr, mvPtr);                              //; Init des mouvements
            }
        }

        if (hoPtr.roc.rcMovementType == -1)
        {
            hoPtr.roc.rcMovementType = 0;
            rmMovement = new CMoveStatic();
            rmMovement.init(hoPtr, null);
            hoPtr.roc.rcDir = 0;
        }
    }

    public CMove loadMvtExtension(CObject hoPtr, CMoveDefExtension mvDef)
    {
        String extName=mvDef.moduleName;
        extName=extName.toLowerCase();
        int index = extName.indexOf('-');
        while (index > 0)
        {
            extName = extName.substring(0, index) + '_' + extName.substring(index + 1);
            index = extName.indexOf('-');
        }

        CRunMvtExtension object=null;
        
		// 

		if (object!=null)
		{
			object.init(hoPtr);
			CMoveExtension mvExt=new CMoveExtension(object);
			return mvExt;
		}		
        return null;
    }

    public void initSimple(CObject hoPtr, int forcedType, boolean bRestore)
    {
        if (rmMovement != null)
        {
            rmMovement.kill();
        }
        hoPtr.roc.rcMovementType = forcedType;					//; Le type
        switch (forcedType)
        {
            // MVTYPE_DISAPPEAR
            case 11:
                rmMovement = new CMoveDisappear();
                break;
            // MVTYPE_BULLET
            case 13:
                rmMovement = new CMoveBullet();
                break;
        }
        rmMovement.hoPtr = hoPtr;
        if (bRestore == false)
        {
            rmMovement.init(hoPtr, null);                              //; Init des mouvements
        }
    }

    public void kill(boolean bFast)
    {
        rmMovement.kill();
    }

    public void move()
    {
        rmMovement.move();
    }

    public void nextMovement(CObject hoPtr)
    {
        CObjectCommon ocPtr = hoPtr.hoCommon;
        if (ocPtr.ocMovements != null)
        {
            if (rmMvtNum + 1 < ocPtr.ocMovements.nMovements)
            {
                kill(false);
                init(rmMvtNum + 1, hoPtr, ocPtr, null, -1);
            }
        }
    }

    public void previousMovement(CObject hoPtr)
    {
        CObjectCommon ocPtr = hoPtr.hoCommon;
        if (ocPtr.ocMovements != null)
        {
            if (rmMvtNum - 1 >= 0)
            {
                kill(false);
                init(rmMvtNum - 1, hoPtr, ocPtr, null, -1);
            }
        }
    }

    public void selectMovement(CObject hoPtr, int mvt)
    {
        CObjectCommon ocPtr = hoPtr.hoCommon;
        if (ocPtr.ocMovements != null)
        {
            if (mvt >= 0 && mvt < ocPtr.ocMovements.nMovements)
            {
                kill(false);
                init(mvt, hoPtr, ocPtr, null, -1);
            }
        }
    }

    public int dirAtStart(CObject hoPtr, int dirAtStart, int dir)
    {
        if (dir < 0 || dir >= 32)
        {
            // Compte le nombre de directions demandees
            int cpt = 0;
            int das = dirAtStart;
            int das2;
            for (int n = 0; n < 32; n++)
            {
                das2 = das;
                das >>= 1;
                if ((das2 & 1) != 0)
                {
                    cpt++;
                }
            }

            // Une ou zero direction?
            if (cpt == 0)
            {
                dir = 0;
            }
            else
            {
                // Appelle le hasard pour trouver le bit
                cpt = hoPtr.hoAdRunHeader.random((short) cpt);
                das = dirAtStart;
                for (dir = 0;; dir++)
                {
                    das2 = das;
                    das >>= 1;
                    if ((das2 & 1) != 0)
                    {
                        cpt--;
                        if (cpt < 0)
                        {
                            break;
                        }
                    }
                }
            }
        }
        // Direction trouvee, OUF
        return dir;
    }
}
