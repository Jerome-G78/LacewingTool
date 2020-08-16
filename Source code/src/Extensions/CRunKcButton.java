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
// CRUNKCBUTTON: extension object
//
//----------------------------------------------------------------------------------
package Extensions;

import Runtime.ITouchAware;
import android.widget.*;
import android.graphics.*;
import android.view.*;

import Params.*;
import Services.*;
import RunLoop.*;
import Expressions.*;
import Sprites.*;
import Conditions.*;
import Actions.*;
import Banks.*;
import Objects.*;
import OpenGL.*;
import Runtime.*;

public class CRunKcButton extends CRunViewExtension implements ITouchAware
{
    public static final int CND_BOXCHECK = 0;
    public static final int CND_CLICKED = 1;
    public static final int CND_BOXUNCHECK = 2;
    public static final int CND_VISIBLE = 3;
    public static final int CND_ISENABLED = 4;
    public static final int CND_ISRADIOENABLED = 5;
    public static final int CND_LAST = 6;
    public static final int ACT_CHANGETEXT = 0;
    public static final int ACT_SHOW = 1;
    public static final int ACT_HIDE = 2;
    public static final int ACT_ENABLE = 3;
    public static final int ACT_DISABLE = 4;
    public static final int ACT_SETPOSITION = 5;
    public static final int ACT_SETXSIZE = 6;
    public static final int ACT_SETYSIZE = 7;
    public static final int ACT_CHGRADIOTEXT = 8;
    public static final int ACT_RADIOENABLE = 9;
    public static final int ACT_RADIODISABLE = 10;
    public static final int ACT_SELECTRADIO = 11;
    public static final int ACT_SETXPOSITION = 12;
    public static final int ACT_SETYPOSITION = 13;
    public static final int ACT_CHECK = 14;
    public static final int ACT_UNCHECK = 15;
    public static final int ACT_SETCMDID = 16;
    public static final int ACT_SETTOOLTIP = 17;
    public static final int ACT_LAST = 18;
    public static final int EXP_GETXSIZE = 0;
    public static final int EXP_GETYSIZE = 1;
    public static final int EXP_GETX = 2;
    public static final int EXP_GETY = 3;
    public static final int EXP_GETSELECT = 4;
    public static final int EXP_GETTEXT = 5;
    public static final int EXP_GETTOOLTIP = 6;
    public static final int EXP_LAST = 7;
    private static final int BTNTYPE_PUSHTEXT = 0;
    private static final int BTNTYPE_CHECKBOX = 1;
    private static final int BTNTYPE_RADIOBTN = 2;
    private static final int BTNTYPE_PUSHBITMAP = 3;
    private static final int BTNTYPE_PUSHTEXTBITMAP = 4;
    private static final int ALIGN_ONELINELEFT = 0;
    private static final int ALIGN_CENTER = 1;
    private static final int ALIGN_CENTERINVERSE = 2;
    private static final int ALIGN_ONELINERIGHT = 3;
    private static final int BTN_HIDEONSTART = 0x0001;
    private static final int BTN_DISABLEONSTART = 0x0002;
    private static final int BTN_TEXTONLEFT = 0x0004;
    private static final int BTN_TRANSP_BKD = 0x0008;
    private static final int BTN_SYSCOLOR = 0x0010;
    private static final int SX_TEXTIMAGE=6;
    private static final int SY_TEXTIMAGE=4;
    
    short buttonImages[];
    short buttonType;
    short buttonCount;
    int flags;
    short alignImageText;
    String tooltipText = "";
    CFontInfo font;
    int foreColour;
    int backColour;
    int clickedEvent = 0;
    int touchID = -1;
    boolean bEnabled=true;
    boolean bVisible=true;
    boolean bBitmapButtonPressed=false;
    String strings[]=null;
    int zone=-1;
    int oldZone=-1;
	int selected;
	int oldSelected;
	int oldKey;
	boolean radioEnabled[];
	int syButton;

    Button button;
    RadioGroup radioGroup;
    CheckBox checkBox;

    public CRunKcButton()
    {
    }

    public int getNumberOfConditions()
    {
        return CND_LAST;
    }

    public void createRunView(CBinaryFile file, CCreateObjectInfo cob, int version)
    {
        // Read in edPtr values
        ho.hoImgWidth = file.readShort();
        ho.hoImgHeight = file.readShort();
        buttonType = file.readShort();
        buttonCount = file.readShort();
        flags = file.readInt();
        font = file.readLogFont();
        foreColour = file.readColor();
//    file.skipBytes(1);  // padding
        backColour = file.readColor();
//    file.skipBytes(1);  // padding
        buttonImages = new short[3];
        int i;
        for (i = 0; i < 3; i++)
        {
            buttonImages[i] = file.readShort();
        }
        if ((buttonType == BTNTYPE_PUSHBITMAP) || (buttonType == BTNTYPE_PUSHTEXTBITMAP))
        {
            ho.loadImageList(buttonImages);
        }
		if (buttonType==BTNTYPE_PUSHBITMAP)
		{		
			ho.hoImgWidth = 1;
			ho.hoImgHeight = 1;
			for (i = 0; i < 3; i++)
			{
				if (buttonImages[i]!=-1)
				{
					CImage image=ho.hoAdRunHeader.rhApp.imageBank.getImageFromHandle(buttonImages[i]);
					ho.hoImgWidth = Math.max(ho.hoImgWidth, image.getWidth());
					ho.hoImgHeight = Math.max(ho.hoImgHeight, image.getHeight());
				}
			}
		}
        file.readShort(); // fourth word in img array
        file.readInt(); // ebtnSecu
        alignImageText = file.readShort();

		selected=-1;
		oldSelected=-1;
		oldZone=-1;
		oldKey=-1;
		bEnabled=true;
		if ((flags&BTN_DISABLEONSTART)!=0)
		{
			bEnabled=false;
		}			
		bVisible=true;
		if ((flags&BTN_HIDEONSTART)!=0)
		{
			bVisible=false;
		}				
        
        // In the file an array of strings follows. The first string is the button text,
        // the second is the tooltip. For a radio button, there are buttonCount strings
        // for each radio button in the group, and no tooltip.
        if (buttonType != BTNTYPE_RADIOBTN)
        {
            strings = new String[1];
            strings[0] = file.readString();
            tooltipText = file.readString();

            if(buttonType == BTNTYPE_CHECKBOX)
            {
                view = checkBox = new CheckBox(ho.getControlsContext());
                checkBox.setText (strings [0]);
            }
        }
        else
        {
            strings = new String[buttonCount];
            radioEnabled=new boolean[buttonCount];
			syButton=ho.hoImgHeight/buttonCount;
            
            view = radioGroup = new RadioGroup (ho.getControlsContext());

            for (i = 0; i < buttonCount; i++)
            {
                strings[i] = file.readString();
                radioEnabled[i]=true;
            
                RadioButton radioButton = new RadioButton (ho.getControlsContext());

                radioButton.setText(strings[i]);
                radioGroup.addView(radioButton);
            }
        }

        if(buttonType == BTNTYPE_PUSHTEXT)
        {
            view = button = new Button (ho.getControlsContext());

            button.setText(strings[0]);
        }

        if(view != null)
        {
            if(!bEnabled)
                view.setEnabled(false);

            if(!bVisible)
                view.setVisibility(View.INVISIBLE);

            view.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View view)
                {
                    ho.pushEvent(CND_CLICKED, 0);
                }
            });

            setView (view);
        }
        else
        {
            MMFRuntime.inst.touchManager.addTouchAware(this);
        }
        
        updateFont();
    }

    public void destroyRunObject(boolean bFast)
    {
        if (view == null)
            MMFRuntime.inst.touchManager.removeTouchAware(this);
        else
            setView(null);
    }

    public void newTouch (int id, float x, float y)
    {
		if (buttonType==BTNTYPE_PUSHBITMAP)
		{
			zone = getZone((int) x, (int) y);
			
			if (zone == 0)
			{
				if (bBitmapButtonPressed)
					return;
			
				bBitmapButtonPressed = true;
		        touchID = id;
		        
		        clickedEvent = rh.rh4EventCount;
		        ho.generateEvent(CND_CLICKED, 0);
			}
		}
    }
    
    public void endTouch (int id)
    {
    	if(bBitmapButtonPressed && id == touchID)
    	{
    		bBitmapButtonPressed = false;
    	}
    }
    
    public void touchMoved (int id, float x, float y)
    {
    }
    
    public int handleRunObject()
    {
    	super.handleRunObject ();

        if (view == null)
        	return REFLAG_DISPLAY;
        else
            return 0;
    }
    
	int getZone(int xMouse, int yMouse)
	{
		if (xMouse>=ho.hoX && xMouse<ho.hoX+ho.hoImgWidth)
		{
			if (yMouse>=ho.hoY && yMouse<ho.hoY+ho.hoImgHeight)
			{
				return 0;
			}
		}

		return -1; 
	}

    public void displayRunObject()
    {
    	if (bVisible==false || view != null)
    	{
    		return;    		
    	}
    	
        CImage image;
        CRect crc=new CRect();
        crc.left=ho.hoX;
        crc.top=ho.hoY;
        crc.right=ho.hoX+ho.hoImgWidth;
        crc.bottom=ho.hoY+ho.hoImgHeight;
    	switch (buttonType)
    	{
    	case BTNTYPE_PUSHBITMAP:
    		if (bEnabled==false)
    		{
    			image=ho.hoAdRunHeader.rhApp.imageBank.getImageFromHandle(buttonImages[2]);
    		}
    		else
    		{
        		if (bBitmapButtonPressed)
        		{
        			image=ho.hoAdRunHeader.rhApp.imageBank.getImageFromHandle(buttonImages[1]);
        		}
        		else
        		{
        			image=ho.hoAdRunHeader.rhApp.imageBank.getImageFromHandle(buttonImages[0]);
        		}
    		}
    		if (image != null)
                GLRenderer.inst.renderImage
                    (image, ho.hoX, ho.hoY, image.getWidth(), image.getHeight(), 0, 0);
			break;
    	}
    }

    public CFontInfo getRunObjectFont()
    {
        return font;
    }

    public void setRunObjectFont(CFontInfo fi, CRect rc)
    {
        font = fi;

        updateFont();
    }

    private void updateFont()
    {
        if(view != null)
        {
            if(radioGroup != null)
            {
                for(int i = 0; i < radioGroup.getChildCount(); ++ i)
                {
                    updateFont((TextView) radioGroup.getChildAt(i), font);
                }
            }
            else
                updateFont((TextView) view, font);
        }
    }

    public int getRunObjectTextColor()
    {
        return foreColour;
    }

    public void setRunObjectTextColor(int rgb)
    {
        foreColour = rgb;
    }

    public CMask getRunObjectCollisionMask(int flags)
    {
        return null;
    }

    public Bitmap getRunObjectSurface()
    {
        return null;
    }

    public void getZoneInfos()
    {
    }

    // Conditions
    // --------------------------------------------------
    public boolean condition(int num, CCndExtension cnd)
    {
        switch (num)
        {
            case CND_BOXCHECK:
                return cndBOXCHECK(cnd);
            case CND_CLICKED:
                return cndCLICKED(cnd);
            case CND_BOXUNCHECK:
                return cndBOXUNCHECK(cnd);
            case CND_VISIBLE:
                return cndVISIBLE(cnd);
            case CND_ISENABLED:
                return cndISENABLED(cnd);
            case CND_ISRADIOENABLED:
                return cndISRADIOENABLED(cnd);
        }
        return false;
    }

    private boolean cndBOXCHECK(CCndExtension cnd)
    {
        if (buttonType == BTNTYPE_CHECKBOX)
        {
            return ((CheckBox) view).isChecked();
        }
        return false;
    }

    private boolean cndCLICKED(CCndExtension cnd)
    {
        // If this condition is first, then always true
        if ((ho.hoFlags & CObject.HOF_TRUEEVENT) != 0)
        {
            return true;
        }

        // If condition second, check event number matches
        if (rh.rh4EventCount == clickedEvent)
        {
            return true;
        }

        return false;
    }

    private boolean cndBOXUNCHECK(CCndExtension cnd)
    {
        if (buttonType == BTNTYPE_CHECKBOX)
        {
            return !((CheckBox) view).isChecked();
        }
        return false;
    }

    private boolean cndVISIBLE(CCndExtension cnd)
    {
        return bVisible;
    }

    private boolean cndISENABLED(CCndExtension cnd)
    {
        return bEnabled;
    }

    private boolean cndISRADIOENABLED(CCndExtension cnd)
    {
        if(radioEnabled == null)
            return false;

        int index = cnd.getParamExpression(rh, 0);
    	if ((index >= 0) && (index < buttonCount))
    	{
    		return radioEnabled[index];
    	}
    	return false;
    }

    // Actions
    // -------------------------------------------------
    public void action(int num, CActExtension act)
    {
        switch (num)
        {
            case ACT_CHANGETEXT:
                actCHANGETEXT(act);
                break;
            case ACT_SHOW:
                actSHOW(act);
                break;
            case ACT_HIDE:
                actHIDE(act);
                break;
            case ACT_ENABLE:
                actENABLE(act);
                break;
            case ACT_DISABLE:
                actDISABLE(act);
                break;
            case ACT_SETPOSITION:
                actSETPOSITION(act);
                break;
            case ACT_SETXSIZE:
                actSETXSIZE(act);
                break;
            case ACT_SETYSIZE:
                actSETYSIZE(act);
                break;
            case ACT_CHGRADIOTEXT:
                actCHGRADIOTEXT(act);
                break;
            case ACT_RADIOENABLE:
                actRADIOENABLE(act);
                break;
            case ACT_RADIODISABLE:
                actRADIODISABLE(act);
                break;
            case ACT_SELECTRADIO:
                actSELECTRADIO(act);
                break;
            case ACT_SETXPOSITION:
                actSETXPOSITION(act);
                break;
            case ACT_SETYPOSITION:
                actSETYPOSITION(act);
                break;
            case ACT_CHECK:
                actCHECK(act);
                break;
            case ACT_UNCHECK:
                actUNCHECK(act);
                break;
            case ACT_SETCMDID:
                actSETCMDID(act);
                break;
            case ACT_SETTOOLTIP:
                actSETTOOLTIP(act);
                break;
        }
    }

    private void actCHANGETEXT(CActExtension act)
    {
        final String text = act.getParamExpString(rh, 0);
        
        strings[0]=text;
    	ho.redraw();

        if(view != null && radioGroup == null)
        {
            ((TextView) view).setText(text);
        }
    }

    private void actSHOW(CActExtension act)
    {
    	bVisible=true;
    	ho.redraw();

        if(view != null)
            view.setVisibility(View.VISIBLE);
    }

    private void actHIDE(CActExtension act)
    {
    	bVisible=false;
    	ho.redraw();

        if(view != null)
            view.setVisibility(View.INVISIBLE);
    }

    private void actENABLE(CActExtension act)
    {
    	bEnabled=true;
    	ho.redraw();
        
        if(view != null)
            view.setEnabled(true);
    }

    private void actDISABLE(CActExtension act)
    {
    	bEnabled=false;
    	ho.redraw();

        if(view != null)
            view.setEnabled(false);
    }

    private void actSETPOSITION(CActExtension act)
    {
        CPositionInfo pos = act.getParamPosition(rh, 0);
        ho.setPosition(pos.x, pos.y);
        ho.redraw();
    }

    private void actSETXSIZE(CActExtension act)
    {
        ho.setWidth(act.getParamExpression(rh, 0));
        ho.redraw();
    }

    private void actSETYSIZE(CActExtension act)
    {
        ho.setHeight(act.getParamExpression(rh, 0));
        ho.redraw();
    }

    private void actCHGRADIOTEXT(CActExtension act)
    {
        int index = act.getParamExpression(rh, 0);
        String newText = act.getParamExpString(rh, 1);
        if ((index >= 0) && (index < buttonCount))
        {
            strings[index]=newText;
            ho.redraw();
        }
    }

    private void actRADIOENABLE(CActExtension act)
    {
        if(radioEnabled == null)
            return;

        int index = act.getParamExpression(rh, 0);
        if ((index >= 0) && (index < buttonCount))
        {
        	if (radioEnabled[index]==false)
        	{
        		radioEnabled[index]=true;
        		ho.redraw();
        	}
        }
    }

    private void actRADIODISABLE(CActExtension act)
    {
        if(radioEnabled == null)
            return;

        int index = act.getParamExpression(rh, 0);
        if ((index >= 0) && (index < buttonCount))
        {
        	if (radioEnabled[index]==true)
        	{
        		radioEnabled[index]=false;
        		ho.redraw();
        	}
        }
    }

    private void actSELECTRADIO(CActExtension act)
    {
        if(radioEnabled == null)
            return;

        int index = act.getParamExpression(rh, 0);
        if (selected!=index)
        {
        	if (radioEnabled[index])
        	{
        		selected=index;
        		ho.redraw();
        	}
        }
    }

    private void actSETXPOSITION(CActExtension act)
    {
        ho.setPosition(act.getParamExpression(rh, 0), ho.hoY);
        ho.redraw();
    }

    private void actSETYPOSITION(CActExtension act)
    {
        ho.setPosition(ho.hoX, act.getParamExpression(rh, 0));
        ho.redraw();
    }

    private void actCHECK(CActExtension act)
    {
        if (buttonType == BTNTYPE_CHECKBOX)
        {
        	if (selected==-1)
        	{
        		selected=0;
        		ho.redraw();
        	}
        }
    }

    private void actUNCHECK(CActExtension act)
    {
        if (buttonType == BTNTYPE_CHECKBOX)
        {
        	if (selected==0)
        	{
        		selected=-1;
        		ho.redraw();
        	}
        }
    }

    private void actSETCMDID(CActExtension act)
    {
        // TODO set menu item
    }

    private void actSETTOOLTIP(CActExtension act)
    {
        tooltipText = act.getParamExpString(rh, 0);
    }

    // Expressions
    // --------------------------------------------
    public CValue expression(int num)
    {
        switch (num)
        {
            case EXP_GETXSIZE:
                return expGETXSIZE();
            case EXP_GETYSIZE:
                return expGETYSIZE();
            case EXP_GETX:
                return expGETX();
            case EXP_GETY:
                return expGETY();
            case EXP_GETSELECT:
                return expGETSELECT();
            case EXP_GETTEXT:
                return expGETTEXT();
            case EXP_GETTOOLTIP:
                return expGETTOOLTIP();
        }
        return null;
    }

    private CValue expGETXSIZE()
    {
        return new CValue(ho.getWidth());
    }

    private CValue expGETYSIZE()
    {
        return new CValue(ho.getHeight());
    }

    private CValue expGETX()
    {
        return new CValue(ho.getX());
    }

    private CValue expGETY()
    {
        return new CValue(ho.getY());
    }

    private CValue expGETSELECT()
    {
        return new CValue(radioGroup.getCheckedRadioButtonId());
    }

    private CValue expGETTEXT()
    {
        int index = ho.getExpParam().getInt();
    	CValue ret=new CValue("");
    	
        if (buttonType == BTNTYPE_RADIOBTN)
        {
            if ((index < 0) || (index >= buttonCount))
            {
                return ret;
            }
        }
        else
        {
            index = 0;
        }
		ret.forceString(strings[index]);
		return ret;
    }

    private CValue expGETTOOLTIP()
    {
        return new CValue(tooltipText);
    }
}
