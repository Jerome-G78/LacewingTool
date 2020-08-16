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

import RunLoop.*;
import java.util.*;

public class CRunKcBoxACFrameData extends CExtStorage 
{	
    public static enum type
    {
        object,
        container,
        button
    }
    public static long FLAG_CONTAINED = 0x00002000;

    // Global list of objects
    Vector<CRunKcBoxA>	pObjects;

    // List of containers for action "attach"
    Vector<CRunKcBoxA>	pContainers;
    
    Vector<CRunKcBoxA>	pButtons;
    
    // Clicked button
    //HCURSOR		gOldCurs;
    int			gClickedButton;
    int			gHighlightedButton;
    //BOOL		bTimer;
  
    public boolean ImEmpty()
    {
        if (pObjects != null)
        {
            for (int i = 0; i < pObjects.size(); i++)
            {
                if (pObjects.get(i) != null)
                {
                    return false;
                }
            }
        }
        if (pContainers != null)
        {
            for (int i = 0; i < pContainers.size(); i++)
            {
                if (pContainers.get(i) != null)
                {
                    return false;
                }
            }
        }
        return true;
    }
    private int AddObjAddr(type t, CRunKcBoxA reObject)
    {
        if (t == type.object)
        {
           // 1st allocation
            if (pObjects == null)
            {
                pObjects = new Vector<CRunKcBoxA>(0);
                pObjects.addElement(reObject);
                //pObjects.set(0, reObject);
                return 0;
            }
            // Search for free place
            for (int i=0; i < pObjects.size(); i++)
            {
                if (pObjects.get(i) == null )
                {
                    pObjects.setElementAt(reObject,i);
                    return i;
                }
            }
            // Reallocation
            pObjects.addElement(reObject);
            return pObjects.size() - 1;
        }
        if (t == type.container)
        {
            if (pContainers == null)
            {
                pContainers = new Vector<CRunKcBoxA>(0);
                pContainers.addElement(reObject);
                return 0;
            }
            // Search for free place
            for (int i=0; i < pContainers.size(); i++)
            {
                if (pContainers.get(i) == null )
                {
                    pContainers.setElementAt( reObject,i);
                    return i;
                }
            }
            // Reallocation
            pContainers.addElement(reObject);
            return pContainers.size() - 1;
        }
        if (t == type.button)
        {
            if (pButtons == null)
            {
                pButtons = new Vector<CRunKcBoxA>(0);
                pButtons.addElement(reObject);
                return 0;
            }
            // Search for free place
            for (int i=0; i < pButtons.size(); i++)
            {
                if (pButtons.get(i) == null )
                {
                    pButtons.setElementAt( reObject,i);
                    return i;
                }
            }
            // Reallocation
            pButtons.addElement(reObject);
            return pButtons.size() - 1;
        }
        return 0; //won't happen
    }
//    private int FindObjAddr(type t, CRunKcBoxA reObject)
//    {
//        if (t == type.object)
//        {
//            if (pObjects != null)
//            {
//                return pObjects.indexOf(reObject);
//            }  
//        }
//        if (t == type.container)
//        {
//            if (pContainers != null)
//            {
//                return pContainers.indexOf(reObject);
//            } 
//        } 
//        if (t == type.button)
//        {
//            if (pButtons != null)
//            {
//                return pButtons.indexOf(reObject);
//            } 
//        }
//        return -1;
//    }
    // Remove object from list
    private void RemoveObjAddr(type t,  CRunKcBoxA reObject)
    {
        if (t == type.object)
        {
            if (pObjects != null)
            {
                int i = pObjects.indexOf(reObject);
                if (i != -1)
                {
                    pObjects.setElementAt(null, i);
                }                
            }
        }
        if (t == type.container)
        {
            if (pContainers != null)
            {
                int i = pContainers.indexOf(reObject);
                if (i != -1)
                {
                    pContainers.setElementAt(null, i);
                } 
            }
        }
        if (t == type.button)
        {
            if (pButtons != null)
            {
                int i = pButtons.indexOf(reObject);
                if (i != -1)
                {
                    pButtons.setElementAt(null, i);
                } 
            }
        }
      
     }
    
   // Add objects
    public int AddContainer(CRunKcBoxA re)
    {
        return AddObjAddr(type.container, re);
    }
    public int AddObject(CRunKcBoxA re)
    {
        return AddObjAddr(type.object, re);
    }
    public int AddButton(CRunKcBoxA re)
    {
        return AddObjAddr(type.button, re);
    }
    // Find objects
//    public int FindContainer(CRunKcBoxA re)
//    {
//        return FindObjAddr(type.container, re);
//    }
//    public int FindObject(CRunKcBoxA re)
//    {
//        return FindObjAddr(type.object, re);
    //}
//    public int FindButton(CRunKcBoxA re)
//    {
//        return FindObjAddr(type.button, re);
//    }
    public void RemoveContainer(CRunKcBoxA re)
    {
        RemoveObjAddr(type.container, re);
    }
    public void RemoveObjectFromList(CRunKcBoxA re)
    {
        RemoveObjAddr(type.object, re);
    }
    public void RemoveButton(CRunKcBoxA re)
    {
        RemoveObjAddr(type.button, re);
    }
    // Get objects
    public int GetContainer(CRunKcBoxA re)
    {
        int left = re.ho.getX();
        int top = re.ho.getY();
        int right = re.ho.getX() + re.ho.getWidth();
        int bottom = re.ho.getY() + re.ho.getHeight();

        if (this.pContainers != null)
        {
            for (int i=0; i < this.pContainers.size(); i++)
            {
                if ((this.pContainers.get(i) != null) && (this.pContainers.get(i) != re))
                {
                    CRunKcBoxA reThisOne = this.pContainers.get(i);
                    if ((left >= reThisOne.ho.getX()) && 
                            (right <= reThisOne.ho.getX() + reThisOne.ho.getWidth()) && 
                            (top >= reThisOne.ho.getY()) && 
                            (bottom <= reThisOne.ho.getY() + reThisOne.ho.getHeight()))
                    {
                        return i;
                    }
                }
            } 
        }
        return -1;
    }
    public int GetObjectFromList(int x, int y)
    {
        int r = -1;
        if (this.pObjects != null)
        {
            for (int i = this.pObjects.size() - 1; i >= 0; i--)
            {
                if (this.pObjects.get(i) != null)
                {
                    CRunKcBoxA reThisOne = this.pObjects.get(i);
                    CRun rhPtr = reThisOne.ho.hoAdRunHeader;
                    if ((x >= reThisOne.ho.getX() - rhPtr.rhWindowX) &&
                             (x <= (reThisOne.ho.getX() - rhPtr.rhWindowX + reThisOne.ho.getWidth())) &&
                             (y >= (reThisOne.ho.getY() - rhPtr.rhWindowY)) &&
                             (y <= (reThisOne.ho.getY() - rhPtr.rhWindowY + reThisOne.ho.getHeight())))
                    {
                        r = i;
                        break;
                    }
                }
            }
        }
	return r;
    }
//    public int GetButton(int x, int y)
//    {
//        int r = -1;
//        if (this.pButtons != null)
//        {
//            for (int i = this.pButtons.size() - 1; i >= 0; i--)
//            {
//                if (this.pButtons.get(i) != null)
//                {
//                    CRunKcBoxA reThisOne = this.pButtons.get(i);
//                    CRun rhPtr = reThisOne.ho.hoAdRunHeader;
//                    if ((x >= reThisOne.ho.getX() - rhPtr.rhWindowX) &&
//                             (x <= (reThisOne.ho.getX() - rhPtr.rhWindowX + reThisOne.ho.getWidth())) &&
//                             (y >= (reThisOne.ho.getY() - rhPtr.rhWindowY)) &&
//                             (y <= (reThisOne.ho.getY() - rhPtr.rhWindowY + reThisOne.ho.getHeight())))
//                    {
//                        r = i;
//                        break;
//                    }
//                }
//            }
//        }
//	return r;
//    }
    // Update position of contained objects
    public void UpdateContainedPos()//CRunKcBoxA re)
    {
	if (this.pObjects != null)
        {
            for (int i=0; i < this.pObjects.size(); i++)
            {
                if (this.pObjects.get(i) != null)
                {
                    CRunKcBoxA reThisOne = this.pObjects.get(i);
                    // Contained ? must update coordinates
                    if ((reThisOne.rData.dwFlags & FLAG_CONTAINED) != 0)
                    {
                        // Not yet a container? search Medor, search!
                        if (reThisOne.rContNum == -1 )
                        {
                            reThisOne.rContNum = GetContainer(reThisOne);
                            if (reThisOne.rContNum != -1 )
                            {
                                CRunKcBoxA rdPtrCont = this.pContainers.get(reThisOne.rContNum);
                                reThisOne.rContDx = (short)(reThisOne.ho.getX() - rdPtrCont.ho.getX());
                                reThisOne.rContDy = (short)(reThisOne.ho.getY() - rdPtrCont.ho.getY());
                            }
                        }

                        if ((reThisOne.rContNum != -1) && (reThisOne.rContNum < this.pContainers.size() ))
                        {
                            CRunKcBoxA rdPtrCont = this.pContainers.get(reThisOne.rContNum);
                            if (rdPtrCont != null )
                            {
                                int newX = rdPtrCont.ho.getX() + reThisOne.rContDx;
                                int newY = rdPtrCont.ho.getY() + reThisOne.rContDy;
                                if ((newX != reThisOne.ho.getX()) || (newY != reThisOne.ho.getY()))
                                {
                                    reThisOne.ho.setX(newX);
                                    reThisOne.ho.setY(newY);
                                    // Update tooltip position
                                    //UpdateToolTipRect(reThisOne);
                                    reThisOne.ho.redraw();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
     
}


