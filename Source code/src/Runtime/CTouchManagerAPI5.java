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

import android.view.*;
import java.util.*;

public class CTouchManagerAPI5 extends CTouchManager
{
    public CTouchManagerAPI5()
    {
    }

    public void process(MotionEvent event)
    {
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        int pointer = event.getPointerId (action >> MotionEvent.ACTION_POINTER_ID_SHIFT);

        switch(actionCode)
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:

                try
                {   newTouch(pointer, event.getX(pointer), event.getY(pointer));
                }
                catch (Throwable t)
                {
                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:

                endTouch(pointer);
                break;

            case MotionEvent.ACTION_MOVE:

                for(int i = 0; i < event.getPointerCount(); ++ i)
                {
                    try
                    {
                        touchMoved(event.getPointerId(i), event.getX(i), event.getY(i));
                    }
                    catch (Throwable t)
                    {
                    }
                }

                break;
        }
    }
}
