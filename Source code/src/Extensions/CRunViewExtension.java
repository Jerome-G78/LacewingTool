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

import Application.CRunApp;
import RunLoop.CCreateObjectInfo;
import Runtime.ControlView;
import Services.CBinaryFile;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.*;
import android.widget.TextView;
import Services.CFontInfo;

public abstract class CRunViewExtension extends CRunExtension
{
    View view;

    public abstract void createRunView (final CBinaryFile file, final CCreateObjectInfo cob, final int version);

    public void setView (final View view)
    {
        final ControlView controls = ho.hoAdRunHeader.rhApp.controlView;

        if (view != null)
        {
            this.view = view;

            view.setLayoutParams (new ControlView.LayoutParams
                            		(ho.hoImgWidth, ho.hoImgHeight,
                            			ho.hoX - rh.rhWindowX, ho.hoY - rh.rhWindowY));

            controls.addView(view);
        }
        else
        {
            if (this.view == null)
                return;

            final View toRemove = this.view;
            this.view = null;

            controls.removeView(toRemove);
        }
    }

    public void updateFont (final TextView view, CFontInfo font)
    {
    	if ((rh.rhApp.hdr2Options & CRunApp.AH2OPT_SYSTEMFONT) != 0)
    		return;
    	
        final Typeface typeface = font.createFont ();
        final int size = font.lfHeight;

        view.post(new Runnable()
        {
            public void run()
            {
                view.setTypeface (typeface);                
                view.setTextSize (size);
            }
        });
    }

    public void updateFont (CFontInfo font)
    {
        updateFont ((TextView) view, font);
    }

    @Override
    public boolean createRunObject (final CBinaryFile file, final CCreateObjectInfo cob, final int version)
    {
        createRunView(file, cob, version);
        return false;
    }

    @Override
    public int handleRunObject ()
    {
    	updateLayout ();
        return 0;
    }
    
    public void updateLayout()
    {    
    	if (view == null)
    		return;
		
		final int x = ho.hoX - rh.rhWindowX;
		final int y = ho.hoY - rh.rhWindowY;
		
		final int width = ho.hoImgWidth;
		final int height = ho.hoImgHeight;
		
		final ControlView.LayoutParams layoutParams =
		        (ControlView.LayoutParams) view.getLayoutParams();
		
		if (layoutParams.x == x && layoutParams.y == y &&
		        layoutParams.width == width
		        && layoutParams.height == height)
		{
		    return;
		}
		  
		layoutParams.x = x;
		layoutParams.y = y;
		
		layoutParams.width = width;
		layoutParams.height = height;
		
		view.requestLayout ();
    }

    public void setViewWidth (int width)
    {
        ho.hoImgWidth = width;

        updateLayout ();
    }

    public void setViewHeight (int height)
    {
        ho.hoImgHeight = height;

        updateLayout ();
    }

    public void setViewSize (int width, int height)
    {
        ho.hoImgWidth = width;
        ho.hoImgHeight = height;

        updateLayout ();
    }

    public void setViewX (int x)
    {
        ho.hoX = x;

        updateLayout ();
    }

    public void setViewY (int y)
    {
        ho.hoY = y;

        updateLayout ();
    }

    public void setViewPosition (int x, int y)
    {
        ho.hoX = x;
        ho.hoY = y;
        
        updateLayout ();
    }

    @Override
    public void destroyRunObject(boolean bFast)
    {
        setView (null);
    }

}
