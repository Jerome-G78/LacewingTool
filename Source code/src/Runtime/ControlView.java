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
package Runtime;

import android.graphics.Canvas;
import android.view.*;

import Application.*;

public class ControlView extends ViewGroup
{
    CRunApp app;

    public ControlView(CRunApp app)
    {
        super(MMFRuntime.inst);

        this.app = app;

        setClipChildren(true);
        setClipToPadding(true);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        float scaleX = MMFRuntime.inst.scaleX;
        float scaleY = MMFRuntime.inst.scaleY;

        int count = getChildCount();

        int maxHeight = getSuggestedMinimumHeight();
        int maxWidth = getSuggestedMinimumWidth();

        for (int i = 0; i < count; ++ i)
        {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            ControlView.LayoutParams layoutParams =
                    (ControlView.LayoutParams) child.getLayoutParams();

            int drawWidth = Math.round(layoutParams.width * scaleX);
            int drawHeight = Math.round(layoutParams.height * scaleY);

            child.measure(MeasureSpec.makeMeasureSpec(drawWidth, MeasureSpec.EXACTLY),
                          MeasureSpec.makeMeasureSpec(drawHeight, MeasureSpec.EXACTLY));

            int drawX = (int) (layoutParams.x * scaleX);
            int drawY = (int) (layoutParams.y * scaleY);

            if ((drawX + drawWidth) > maxWidth)
                maxWidth = drawX + drawWidth;

            if ((drawY + drawHeight) > maxHeight)
                maxHeight = drawY + drawHeight;
        }

        /* setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
                resolveSize(maxHeight, heightMeasureSpec)); */

           setMeasuredDimension(resolveSize((int) (app.gaCxWin * MMFRuntime.inst.scaleX), widthMeasureSpec),
                resolveSize((int) (app.gaCyWin + MMFRuntime.inst.scaleY), heightMeasureSpec));
    }

    protected ViewGroup.LayoutParams generateDefaultLayoutParams()
    {
        return new LayoutParams(LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT, 0, 0);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        float scaleX = MMFRuntime.inst.scaleX;
        float scaleY = MMFRuntime.inst.scaleY;

        int count = getChildCount();

        for (int i = 0; i < count; ++ i)
        {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            ControlView.LayoutParams layoutParams =
                    (ControlView.LayoutParams) child.getLayoutParams();

            int drawX = (int) (layoutParams.x * scaleX);
            int drawY = (int) (layoutParams.y * scaleY);

            int drawWidth = child.getMeasuredWidth();
            int drawHeight = child.getMeasuredHeight();

            child.layout(drawX, drawY, drawX + drawWidth, drawY + drawHeight);

        }
    }

    protected boolean checkLayoutParams(ViewGroup.LayoutParams p)
    {
        return p instanceof ControlView.LayoutParams;
    }

    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p)
    {
        return new LayoutParams(p);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams
    {
        public int x;
        public int y;

        public LayoutParams(int width, int height, int x, int y)
        {
            super (width, height);

            this.x = x;
            this.y = y;
        }

        public LayoutParams(ViewGroup.LayoutParams source)
        {
            super(source);
        }
    }
}
