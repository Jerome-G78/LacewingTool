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
// CMOVESTATIC : Mouvement statique
//
//----------------------------------------------------------------------------------
package Movements;

import Objects.*;
import Sprites.*;
import Animations.*;

public class CMoveBullet extends CMove
{
    public boolean MBul_Wait=false;
    public CObject MBul_ShootObject=null;
    
    public void init(CObject ho, CMoveDef mvPtr)
    {
        hoPtr=ho;
	if (hoPtr.roc.rcSprite!=null)						// Est-il active?
	{
	    hoPtr.roc.rcSprite.setSpriteColFlag(0);		//; Pas dans les collisions
	}
	if ( hoPtr.ros!=null )
	{
	    hoPtr.ros.rsFlags&=~CRSpr.RSFLAG_VISIBLE;
	    hoPtr.ros.obHide();									//; Cache pour le moment
	}
	MBul_Wait=true;
	hoPtr.hoCalculX=0;
	hoPtr.hoCalculY=0;
	if (hoPtr.roa!=null)
	{
	    hoPtr.roa.init_Animation(CAnim.ANIMID_WALK);
	}
	hoPtr.roc.rcSpeed=0;
	hoPtr.roc.rcCheckCollides=true;			//; Force la detection de collision
	hoPtr.roc.rcChanged=true;
    }
    public void init2(CObject parent)
    {
	hoPtr.roc.rcMaxSpeed=hoPtr.roc.rcSpeed;
	hoPtr.roc.rcMinSpeed=hoPtr.roc.rcSpeed;				
	MBul_ShootObject=parent;			// Met l'objet source	
    }
    public void kill()
    {
        
    }
    public void move()
    {
	if (MBul_Wait)
	{
	    // Attend la fin du mouvement d'origine
	    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	    if (MBul_ShootObject.roa!=null)
	    {
		if (MBul_ShootObject.roa.raAnimOn==CAnim.ANIMID_SHOOT) 
		    return;
	    }
	    startBullet();
	}

	// Fait fonctionner la balle
	// ~~~~~~~~~~~~~~~~~~~~~~~~~
        if (hoPtr.roa!=null)
        {
            hoPtr.roa.animate();
        }
	newMake_Move(hoPtr.roc.rcSpeed, hoPtr.roc.rcDir);

	// Verifie que la balle ne sort pas du terrain (assez loin des bords!)
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	if (hoPtr.hoX<-64 || hoPtr.hoX>hoPtr.hoAdRunHeader.rhLevelSx+64 || hoPtr.hoY<-64 || hoPtr.hoY>hoPtr.hoAdRunHeader.rhLevelSy+64)
	{
	    // Detruit la balle, sans explosion!
	    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	    hoPtr.hoCallRoutine=false;
	    hoPtr.hoAdRunHeader.destroy_Add(hoPtr.hoNumber);
	}	
	if (hoPtr.roc.rcCheckCollides)			//; Faut-il tester les collisions?
	{
            hoPtr.roc.rcCheckCollides=false;		//; Va tester une fois!
            hoPtr.hoAdRunHeader.newHandle_Collisions(hoPtr);
	}        
    }
    public void startBullet()
    {
	// Fait demarrer la balle
	// ~~~~~~~~~~~~~~~~~~~~~~
	if (hoPtr.roc.rcSprite!=null)				//; Est-il active?
	{
	    hoPtr.roc.rcSprite.setSpriteColFlag(CSprite.SF_RAMBO);
	}
	if ( hoPtr.ros!=null )
	{
	    hoPtr.ros.rsFlags|=CRSpr.RSFLAG_VISIBLE;
	    hoPtr.ros.obShow();					//; Plus cache
	}
	MBul_Wait=false; 					//; On y va!
	MBul_ShootObject=null;
    }

    public void stop()
    {
        
    }
    public void start()
    {
        
    }
    public void bounce()
    {
        
    }
    public void setSpeed(int speed)
    {
        
    }
    public void setMaxSpeed(int speed)
    {
        
    }
    public void reverse()
    {        
    }
    public void setXPosition(int x)
    {        
	if (hoPtr.hoX!=x)
	{
	    hoPtr.hoX=x;
	    hoPtr.rom.rmMoveFlag=true;
	    hoPtr.roc.rcChanged=true;
	    hoPtr.roc.rcCheckCollides=true;					//; Force la detection de collision
	}
    }
    public void setYPosition(int y)
    {
	if (hoPtr.hoY!=y)
	{
	    hoPtr.hoY=y;
	    hoPtr.rom.rmMoveFlag=true;
	    hoPtr.roc.rcChanged=true;
	    hoPtr.roc.rcCheckCollides=true;					//; Force la detection de collision
	}
    }
    public void setDir(int dir)
    {
    }
    
}
