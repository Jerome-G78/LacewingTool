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
package Application;

import Runtime.ITouchAware;
import Banks.*;
import OpenGL.*;
import Runtime.*;

public class CJoystick implements ITouchAware
{
	public static final int KEY_JOYSTICK = 0;
	public static final int KEY_FIRE1 = 1;
	public static final int KEY_FIRE2 = 2;
	public static final int KEY_NONE = -1;
	
	public static final int MAX_TOUCHES = 3;
	
	public static final int JFLAG_JOYSTICK = 0x0001;
	public static final int JFLAG_FIRE1 = 0x0002;
	public static final int JFLAG_FIRE2 = 0x0004;
	public static final int JFLAG_LEFTHANDED = 0x0008;
	
	public static final int JPOS_NOTDEFINED = 0x80000000;
	
	public CRunApp app;

    class CJoystickImage extends CImage
    {
        CJoystickImage (String name)
        {
            super (name);
        }

        @Override
        public void onDestroy ()
        {
            joyBack = null;
            joyFront = null;
            fire1U = null;
            fire2U = null;
            fire1D = null;
            fire2D = null;
        }
    }

	public CJoystickImage joyBack;
    public CJoystickImage joyFront;
	public CJoystickImage fire1U;
	public CJoystickImage fire2U;
	public CJoystickImage fire1D;
	public CJoystickImage fire2D;

	/* To avoid getWidth/getHeight JNI overhead */

    public int joyBack_width, joyBack_height;
    public int joyFront_width, joyFront_height;
    public int fire1U_width, fire1U_height;
    public int fire2U_width, fire2U_height;
    public int fire1D_width, fire1D_height;
    public int fire2D_width, fire2D_height;
	
	public boolean bLandScape;
	public int [] imagesX = new int[3];
	public int [] imagesY = new int[3];
	public int joystickX;
	public int joystickY;
	public int joystick;
	public int flags;

	boolean staticPosition = false;
	
	public int [] touches = new int[MAX_TOUCHES];

    public void loadImages ()
    {
        if (joyBack != null)
            return; /* still loaded */

        joyBack = new CJoystickImage("drawable/joyback");
        joyFront = new CJoystickImage("drawable/joyfront");
        fire1U = new CJoystickImage("drawable/fire1u");
        fire2U = new CJoystickImage("drawable/fire2u");
        fire1D = new CJoystickImage("drawable/fire1d");
        fire2D = new CJoystickImage("drawable/fire2d");

        joyBack_width = joyBack.getWidth ();
        joyBack_height = joyBack.getHeight ();

        joyFront_width = joyFront.getWidth ();
        joyFront_height = joyFront.getHeight ();

        fire1U_width = fire1U.getWidth ();
        fire1U_height = fire1U.getHeight ();

        fire2U_width = fire2U.getWidth ();
        fire2U_height = fire2U.getHeight ();

        fire1D_width = fire1D.getWidth ();
        fire1D_height = fire1D.getHeight ();

        fire2D_width = fire2D.getWidth ();
        fire2D_height = fire2D.getHeight ();
    }

	public CJoystick(CRunApp app, int flags)
	{
        Log.Log("Init joystick with flags " + flags);

        loadImages ();

		this.app = app;
		this.flags = flags;

		joystickX = 0;
		joystickY = 0;
		
		imagesX[KEY_JOYSTICK]=JPOS_NOTDEFINED;
		imagesY[KEY_JOYSTICK]=JPOS_NOTDEFINED;
		imagesX[KEY_FIRE1]=JPOS_NOTDEFINED;
		imagesY[KEY_FIRE1]=JPOS_NOTDEFINED;
		imagesX[KEY_FIRE2]=JPOS_NOTDEFINED;
		imagesY[KEY_FIRE2]=JPOS_NOTDEFINED;

        for (int i = 0; i < touches.length; ++ i)
            touches [i] = -1;
	}
	
	public void setPositions()
	{
	    if (staticPosition)
	        return;

		int sx, sy;
		sx=MMFRuntime.inst.currentWidth;
		sy=MMFRuntime.inst.currentHeight;
		if ((flags&JFLAG_LEFTHANDED)==0)
		{
			if ((flags&JFLAG_JOYSTICK)!=0)
			{
				imagesX[KEY_JOYSTICK]=16+joyBack_width/2;
				imagesY[KEY_JOYSTICK]=sy-16-joyBack_height/2;
			}
			if ((flags&JFLAG_FIRE1)!=0 && (flags&JFLAG_FIRE2)!=0)
			{
				imagesX[KEY_FIRE1]=sx-fire1U_width/2-32;
				imagesY[KEY_FIRE1]=sy-fire1U_height/2-16;
				imagesX[KEY_FIRE2]=sx-fire2U_width/2-16;
				imagesY[KEY_FIRE2]=sy-fire2U_height/2-fire1U_height-24;
			}
			else if ((flags&JFLAG_FIRE1)!=0)
			{
				imagesX[KEY_FIRE1]=sx-fire1U_width/2-16;
				imagesY[KEY_FIRE1]=sy-fire1U_height/2-16;
			}
			else if ((flags&JFLAG_FIRE2)!=0)
			{
				imagesX[KEY_FIRE2]=sx-fire2U_width/2-16;
				imagesY[KEY_FIRE2]=sy-fire2U_height/2-16;
			}
		}
		else
		{
			if ((flags&JFLAG_JOYSTICK)!=0)
			{
				imagesX[KEY_JOYSTICK]=sx-16-joyBack_width/2;
				imagesY[KEY_JOYSTICK]=sy-16-joyBack_height/2;
			}
			if ((flags&JFLAG_FIRE1)!=0 && (flags&JFLAG_FIRE2)!=0)
			{
				imagesX[KEY_FIRE1]=fire1U_width/2+16+fire2U_width*2/3;
				imagesY[KEY_FIRE1]=sy-fire1U_height/2-16;
				imagesX[KEY_FIRE2]=fire2U_width/2+16;
				imagesY[KEY_FIRE2]=sy-fire2U_height/2-fire1U_height-24;
			}
			else if ((flags&JFLAG_FIRE1)!=0)
			{
				imagesX[KEY_FIRE1]=fire1U_width/2+16;
				imagesY[KEY_FIRE1]=sy-fire1U_height/2-16;
			}
			else if ((flags&JFLAG_FIRE2)!=0)
			{
				imagesX[KEY_FIRE2]=fire2U_width/2+16;
				imagesY[KEY_FIRE2]=sy-fire2U_height/2-16;
			}
		}
	}
	
	public void setXPosition(int f, int p)
	{
        staticPosition = true;

        if ((f&JFLAG_JOYSTICK)!=0)
		{
			imagesX[KEY_JOYSTICK]=p;
		}
		else if ((f&JFLAG_FIRE1)!=0)
		{
			imagesX[KEY_FIRE1]=p;
		}
		else if ((f&JFLAG_FIRE2)!=0)
		{
			imagesX[KEY_FIRE2]=p;
		}
	}
	
	public void setYPosition(int f, int p)
	{
	    staticPosition = true;

		if ((f&JFLAG_JOYSTICK)!=0)
		{
			imagesY[KEY_JOYSTICK]=p;
		}
		else if ((f&JFLAG_FIRE1)!=0)
		{
			imagesY[KEY_FIRE1]=p;
		}
		else if ((f&JFLAG_FIRE2)!=0)
		{
			imagesY[KEY_FIRE2]=p;
		}
	}
	
	public void draw()
	{
		GLRenderer renderer = GLRenderer.inst;

        renderer.beginWholeScreenDraw ();

		if ((flags&JFLAG_JOYSTICK)!=0)
		{		
			renderer.renderImage(joyBack, imagesX[KEY_JOYSTICK]-joyBack_width/2, imagesY[KEY_JOYSTICK]-joyBack_height/2, joyBack_width, joyBack_height, 0, 0);
			renderer.renderImage(joyFront, imagesX[KEY_JOYSTICK]+joystickX-joyFront_width/2, imagesY[KEY_JOYSTICK]+joystickY-joyFront_height/2, joyFront_width, joyFront_height, 0, 0);
		}
		if ((flags&JFLAG_FIRE1)!=0)
		{
			CImage tex;
			int tw, th;

		    if ((joystick&0x10)==0)
            {
                tex = fire1U;

                tw = fire1U_width;
                th = fire1U_height;
            }
            else
            {
                tex = fire1D;

                tw = fire1D_width;
                th = fire1D_height;
            }

			renderer.renderImage(tex, imagesX[KEY_FIRE1]-tw/2, imagesY[KEY_FIRE1]-th/2, tw, th, 0, 0);
		}
		if ((flags&JFLAG_FIRE2)!=0)
		{
		    CImage tex;
		    int tw, th;

            if ((joystick&0x20)==0)
            {
                tex = fire2U;

                tw = fire2U_width;
                th = fire2U_height;
            }
            else
            {
                tex = fire2D;

                tw = fire2D_width;
                th = fire2D_height;
            }

			renderer.renderImage(tex, imagesX[KEY_FIRE2]-tw/2, imagesY[KEY_FIRE2]-th/2, tw, th, 0, 0);
		}

        renderer.endWholeScreenDraw ();
	}
	
	public void newTouch(int id, float x, float y)
	{
        x *= MMFRuntime.inst.scaleX;
        y *= MMFRuntime.inst.scaleY;

        x += MMFRuntime.inst.viewportX;
        y += MMFRuntime.inst.viewportY;

		int key = getKey((int) Math.floor(x), (int) Math.floor(y));

		if(key != KEY_NONE)
		{
			touches[key]=id;
			if (key==KEY_JOYSTICK)
			{
				joystick&=0xF0;
			}		
			else if (key==KEY_FIRE1)
			{
				joystick|=0x10;
			}
			else if (key==KEY_FIRE2)
			{
				joystick|=0x20;
			}
		}
	}
	
	public void touchMoved(int id, float _x, float _y)
	{
        _x *= MMFRuntime.inst.scaleX;
        _y *= MMFRuntime.inst.scaleX;

        _x += MMFRuntime.inst.viewportX;
        _y += MMFRuntime.inst.viewportY;

        int x = (int) Math.round(_x);
        int y = (int) Math.round(_y);

		int key = getKey(x, y);
		
		if (key==KEY_JOYSTICK)
		{
			touches[KEY_JOYSTICK]=id;
		}
		if (id==touches[KEY_JOYSTICK])
		{
			joystickX=x-imagesX[KEY_JOYSTICK];
			joystickY=y-imagesY[KEY_JOYSTICK];
			if (joystickX<-joyBack_width/4)
			{
				joystickX=-joyBack_width/4;
			}
			if (joystickX>joyBack_width/4)
			{
				joystickX=joyBack_width/4;
			}
			if (joystickY<-joyBack_height/4)
			{
				joystickY=-joyBack_height/4;
			}
			if (joystickY>joyBack_height/4)
			{
				joystickY=joyBack_height/4;
			}

			joystick&=0xF0;
			double h=Math.sqrt(joystickX*joystickX+joystickY*joystickY);
			if (h>=joyBack_width/4)
			{
				double angle=Math.atan2(-joystickY, joystickX);
				int j=0;
				if (angle>=0.0)
				{
					if (angle<Math.PI/8)
						j=8;
					else if (angle<(Math.PI/8)*3)
						j=9;
					else if (angle<(Math.PI/8)*5)
						j=1;
					else if (angle<(Math.PI/8)*7)
						j=5;
					else 
						j=4;					
				}
				else
				{
					if (angle>-Math.PI/8)
						j=8;
					else if (angle>-(Math.PI/8)*3)
						j=0xA;
					else if (angle>-(Math.PI/8)*5)
						j=2;
					else if (angle>-(Math.PI/8)*7)
						j=6;
					else
						j=4;
				}
				joystick|=j;
			}
		}
	}

	public void endTouch(int id)
	{
		int n;
		for (n=0; n<MAX_TOUCHES; n++)
		{
			if (touches[n]==id)
			{
				touches[n] = -1;
				switch (n)
				{
					case KEY_JOYSTICK:
						joystickX=0;
						joystickY=0;
						joystick&=0xF0;
						break;
					case KEY_FIRE1:
						joystick&=~0x10;
						break;
					case KEY_FIRE2:
						joystick&=~0x20;
						break;
				}
				break;
			}
		}	
	}
	
	public int getKey(int x, int y)
	{	
		if ((flags&JFLAG_JOYSTICK) != 0)
		{
			if (x>=imagesX[KEY_JOYSTICK]-joyBack_width/2 && x<imagesX[KEY_JOYSTICK]+joyBack_width/2)
			{
				if (y>imagesY[KEY_JOYSTICK]-joyBack_height/2 && y<imagesY[KEY_JOYSTICK]+joyBack_height/2)
				{
					return KEY_JOYSTICK;
				}
			}
		}
		if ((flags&JFLAG_FIRE1) != 0)
		{
			if (x>=imagesX[KEY_FIRE1]-fire1U_width/2 && x<imagesX[KEY_FIRE1]+fire1U_width/2)
			{
				if (y>imagesY[KEY_FIRE1]-fire1U_height/2 && y<imagesY[KEY_FIRE1]+fire1U_height/2)
				{
					return KEY_FIRE1;
				}
			}
		}
		if ((flags&JFLAG_FIRE2) != 0)
		{
			if (x>=imagesX[KEY_FIRE2]-fire2U_width/2 && x<imagesX[KEY_FIRE2]+fire2U_width/2)
			{
				if (y>imagesY[KEY_FIRE2]-fire2U_height/2 && y<imagesY[KEY_FIRE2]+fire2U_height/2)
				{
					return KEY_FIRE2;
				}
			}
		}
		return KEY_NONE;
	}

	public void reset(int f)
    {
        flags=f;
        setPositions();
    }

}
