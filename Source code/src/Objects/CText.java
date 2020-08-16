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
// CText : Objet string
//
//----------------------------------------------------------------------------------
package Objects;

import Banks.*;
import Sprites.*;
import OI.*;
import RunLoop.*;
import Services.*;
import OpenGL.*;

public class CText extends CObject
{
    public short rsFlag;
    public int rsBoxCx;
    public int rsBoxCy;
    public int rsMaxi;
    public int rsMini;
    public byte rsHidden;
    public String rsTextBuffer;
    public short rsFont;
    public int rsTextColor;
    public CTextSurface textSurface;
    public int deltaY;
    
    public CText()
    {
    }

    public void init(CObjectCommon ocPtr, CCreateObjectInfo cob)
    {
        rsFlag = 0;										// ???? adlo->loFlags;
        CDefTexts txt = (CDefTexts) ocPtr.ocObject;
        hoImgWidth = txt.otCx;
        hoImgHeight = txt.otCy;

        textSurface = new CTextSurface(hoAdRunHeader.rhApp, hoImgWidth, hoImgHeight);

        rsBoxCx = txt.otCx;
        rsBoxCy = txt.otCy;

        // Recuperer la couleur et le nombre de phrases
        rsMaxi = txt.otNumberOfText;
        rsTextColor=0;
        if (txt.otTexts.length>0)
        {
            rsTextColor = txt.otTexts[0].tsColor;
        }
        rsHidden = (byte) cob.cobFlags;					// A Toujours?
        rsTextBuffer = null;
        rsFont = -1;
        rsMini = 0;
        if ((rsHidden & CRun.COF_FIRSTTEXT) != 0)
        {
            if (txt.otTexts.length>0)
            {
                rsTextBuffer = txt.otTexts[0].tsText;
            }
            else
            {
                rsTextBuffer = "";
            }
        }
    }

    public void handle()
    {
        ros.handle();
        if (roc.rcChanged)
        {
            roc.rcChanged = false;
            modif();
        }
    }

    public void modif()
    {
        ros.modifRoutine();
    }

    public void display()
    {
        ros.displayRoutine();
    }

    public void kill(boolean bFast)
    {
        if (textSurface != null)
            textSurface.recycle();
    }

    public void getZoneInfos()
    {
        CDefTexts txt = (CDefTexts) hoCommon.ocObject;
        short flags = txt.otTexts[0].tsFlags;

        // Rectangle
        CRect rc = new CRect();
        rc.left = hoX - hoAdRunHeader.rhWindowX;
        rc.top = hoY - hoAdRunHeader.rhWindowY;
        rc.right = rc.left + rsBoxCx;
        rc.bottom = rc.top + rsBoxCy;
        hoImgWidth = (short) (rc.right - rc.left);
        hoImgHeight = (short) (rc.bottom - rc.top);
        hoImgXSpot = 0;
        hoImgYSpot = 0;

        // Get font
        short nFont = rsFont;
        if (nFont == -1)
        {
            if (txt.otTexts.length>0)
            {
                nFont = txt.otTexts[0].tsFont;
            }
        }
        CFont font = hoAdRunHeader.rhApp.fontBank.getFontFromHandle(nFont);

        // Calcul dimensions exacte zone
        String s;
        if (rsMini >= 0)
        {
            s = txt.otTexts[rsMini].tsText;
        }
        else
        {
            s = rsTextBuffer;
            if (s == null)
            {
                s = "";
            }
        }

    }

    public void draw()
    {
        int effect = ros.rsEffect;
        int effectParam = ros.rsEffectParam;
        CDefTexts txt = (CDefTexts) hoCommon.ocObject;
        short flags = txt.otTexts[0].tsFlags;

        // Get font
        short nFont = rsFont;
        if (nFont == -1)
        {
            if (txt.otTexts.length>0)
            {
                nFont = txt.otTexts[0].tsFont;
            }
        }
        CFont font = hoAdRunHeader.rhApp.fontBank.getFontFromHandle(nFont);

        // Affichage
        String s = null;
        if (rsMini >= 0)
        {
            s = txt.otTexts[rsMini].tsText;
        }
        else
        {
            s = rsTextBuffer;
            if (s == null)
            {
                s = "";
            }
        }

        // Allow only the following flags
        short dtflags = (short) (flags & (CServices.DT_LEFT | CServices.DT_CENTER | CServices.DT_RIGHT |
                CServices.DT_TOP | CServices.DT_BOTTOM | CServices.DT_VCENTER |
                CServices.DT_SINGLELINE));

        textSurface.setText(s, dtflags, rsTextColor, font.getFontInfo(), true);
        textSurface.draw(hoX - hoAdRunHeader.rhWindowX, (hoY - hoAdRunHeader.rhWindowY) + deltaY,
        						effect, effectParam);
    }

    public CMask getCollisionMask(int flags)
    {
        return null;
    }

    public CFontInfo getFont()
    {
        short nFont = rsFont;
        if (nFont == -1)
        {
            CDefTexts txt = (CDefTexts) hoCommon.ocObject;
            nFont = txt.otTexts[0].tsFont;
        }
        return hoAdRunHeader.rhApp.fontBank.getFontInfoFromHandle(nFont);
    }

    public void setFont(CFontInfo info, CRect pRc)
    {
        rsFont = hoAdRunHeader.rhApp.fontBank.addFont(info);
        if (pRc != null)
        {
            hoImgWidth = rsBoxCx = pRc.right - pRc.left;
            hoImgHeight = rsBoxCy = pRc.bottom - pRc.top;
        }
        modif();
        roc.rcChanged = true;
    }

    public int getFontColor()
    {
        return rsTextColor;
    }

    public void setFontColor(int rgb)
    {
        rsTextColor = rgb;
        modif();
        roc.rcChanged = true;
    }

    public boolean txtChange(int num)
    {
        if (num < -1)
        {
            num = -1;							// -1==chaine stockee...
        }
        if (num >= rsMaxi)
        {
            num = rsMaxi - 1;
        }
        if (num == rsMini)
        {
            return false;
        }

        rsMini = num;

        // -------------------------------
        // Recopie le texte dans la chaine
        // -------------------------------
        if (num >= 0)
        {
            CDefTexts txt = (CDefTexts) hoCommon.ocObject;
            txtSetString(txt.otTexts[rsMini].tsText);
        }

        // Reafficher ou pas?
        // ------------------
        if ((ros.rsFlags & CRSpr.RSFLAG_HIDDEN) != 0)
        {
            return false;
        }
        return true;
    }

    public void txtSetString(String s)
    {
        rsTextBuffer = s;
    }

    public void spriteDraw(CSprite spr, CImageBank bank, int x, int y)
    {
        draw();
    }

    public CMask spriteGetMask()
    {
        return null;
    }

    public void spriteKill (CSprite spr)
    {
        spr.sprExtraInfo = null;
    }
}
