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

import Actions.CActExtension;
import Conditions.CCndExtension;
import Expressions.CValue;
import RunLoop.CCreateObjectInfo;
import Runtime.MMFRuntime;
import Services.CBinaryFile;
import android.view.KeyEvent;
import tv.ouya.console.api.OuyaController;

public class CRunOUYA extends CRunExtension
{
    @Override public int getNumberOfConditions()
    {
        return 46;
    }

    OuyaController currentController = null;

    @Override
    public boolean createRunObject(CBinaryFile file, CCreateObjectInfo cob, int version)
    {
        ho.getApplication().ouyaObjects.add(this);

        return true;
    }

    @Override
    public void destroyRunObject(boolean bFast)
    {
        ho.getApplication().ouyaObjects.remove(this);
    }

    @Override
    public int handleRunObject()
    {

        return 0;
    }

    public int getKeyIndex(int keyCode)
    {
        switch(keyCode)
        {
            case OuyaController.BUTTON_O:
                return 0;
            case OuyaController.BUTTON_U:
                return 1;
            case OuyaController.BUTTON_Y:
                return 2;
            case OuyaController.BUTTON_A:
                return 3;
            case OuyaController.BUTTON_L1:
                return 4;
            case OuyaController.BUTTON_L2:
                return 5;
            case OuyaController.BUTTON_R1:
                return 6;
            case OuyaController.BUTTON_R2:
                return 7;
            case OuyaController.BUTTON_SYSTEM:
                return 8;
            case OuyaController.BUTTON_DPAD_UP:
                return 9;
            case OuyaController.BUTTON_DPAD_DOWN:
                return 10;
            case OuyaController.BUTTON_DPAD_LEFT:
                return 11;
            case OuyaController.BUTTON_DPAD_RIGHT:
                return 12;
            case OuyaController.BUTTON_R3:
                return 13;
            case OuyaController.BUTTON_L3:
                return 14;
            default:
                return -1;
        }
    }

    public void keyDown(OuyaController controller, int keyCode, KeyEvent msg)
    {
        int keyIndex = getKeyIndex(keyCode);

        if(keyIndex == -1)
            return;

        currentController = controller;

        buttonPressEvents[keyIndex] = ho.getEventCount();
        ho.generateEvent(15 + keyIndex, 0);
    }

    public void keyUp(OuyaController controller, int keyCode, KeyEvent msg)
    {
        int keyIndex = getKeyIndex(keyCode);

        if(keyIndex == -1)
            return;

        currentController = controller;

        buttonReleaseEvents[keyIndex] = ho.getEventCount();
        ho.generateEvent(31 + keyIndex, 0);
    }

    int[] buttonPressEvents = new int[15];
    int[] buttonReleaseEvents = new int[15];

    @Override
    public boolean condition(int num, CCndExtension cnd)
    {
        if(num >= 0 && num <= 14)
        {
            /* Is button pressed for player N? */

            OuyaController controller = OuyaController.getControllerByPlayer(cnd.getParamExpression(rh, 0));

            if(controller == null)
                return false;

            switch(num)
            {
                case 0:
                    return controller.getButton(OuyaController.BUTTON_O);
                case 1:
                    return controller.getButton(OuyaController.BUTTON_U);
                case 2:
                    return controller.getButton(OuyaController.BUTTON_Y);
                case 3:
                    return controller.getButton(OuyaController.BUTTON_A);
                case 4:
                    return controller.getButton(OuyaController.BUTTON_L1);
                case 5:
                    return controller.getButton(OuyaController.BUTTON_L2);
                case 6:
                    return controller.getButton(OuyaController.BUTTON_R1);
                case 7:
                    return controller.getButton(OuyaController.BUTTON_R2);
                case 8:
                    return controller.getButton(OuyaController.BUTTON_SYSTEM);
                case 9:
                    return controller.getButton(OuyaController.BUTTON_DPAD_UP);
                case 10:
                    return controller.getButton(OuyaController.BUTTON_DPAD_DOWN);
                case 11:
                    return controller.getButton(OuyaController.BUTTON_DPAD_LEFT);
                case 12:
                    return controller.getButton(OuyaController.BUTTON_DPAD_RIGHT);
                case 13:
                    return controller.getButton(OuyaController.BUTTON_R3);
                case 14:
                    return controller.getButton(OuyaController.BUTTON_L3);
                default:
                    return false;
            }
        }

        if(num >= 15 && num <= 29)
        {
            /* On button pressed */

            return ho.getEventCount() == buttonPressEvents[num - 15];
        }

        if(num >= 31 && num <= 45)
        {
            /* On button released */

            return ho.getEventCount() == buttonReleaseEvents[num - 31];
        }

        switch(num)
        {
            case 30:  /* Is device an OUYA? */
                return MMFRuntime.OUYA;
        };

        return false;
    }

    @Override
    public void action(int num, CActExtension act)
    {
    }

    @Override
    public CValue expression(int num)
    {
        OuyaController controller;
        Float value;

        switch(num)
        {
            case 0:  /* Current player */

                if(currentController == null)
                    return new CValue(0);

                return new CValue(currentController.getPlayerNum());

            case 1:  /* Left axis X (current player) */

                if(currentController == null)
                    return new CValue(0.0f);

                value = currentController.getAxisValue(OuyaController.AXIS_LS_X);

                if(value == null)
                    return new CValue(0.0f);

                return new CValue(value.floatValue());

            case 2:  /* Left axis Y (current player) */

                if(currentController == null)
                    return new CValue(0.0f);

                value = currentController.getAxisValue(OuyaController.AXIS_LS_Y);

                if(value == null)
                    return new CValue(0.0f);

                return new CValue(value.floatValue());

            case 3:  /* Right axis X (current player) */

                if(currentController == null)
                    return new CValue(0.0f);

                value = currentController.getAxisValue(OuyaController.AXIS_RS_X);

                if(value == null)
                    return new CValue(0.0f);

                return new CValue(value.floatValue());

            case 4:  /* Right axis Y (current player) */

                if(currentController == null)
                    return new CValue(0.0f);

                value = currentController.getAxisValue(OuyaController.AXIS_RS_Y);

                if(value == null)
                    return new CValue(0.0f);

                return new CValue(value.floatValue());

            case 5:  /* Left axis X (player ID) */

                controller = OuyaController.getControllerByPlayer(ho.getExpParam().getInt());

                if(controller == null)
                    return new CValue(0.0f);

                value = controller.getAxisValue(OuyaController.AXIS_LS_X);

                if(value == null)
                    return new CValue(0.0f);

                return new CValue(value.floatValue());

            case 6:  /* Left axis Y (player ID) */

                controller = OuyaController.getControllerByPlayer(ho.getExpParam().getInt());

                if(controller == null)
                    return new CValue(0.0f);

                value = controller.getAxisValue(OuyaController.AXIS_LS_Y);

                if(value == null)
                    return new CValue(0.0f);

                return new CValue(value.floatValue());

            case 7:  /* Right axis X (player ID) */

                controller = OuyaController.getControllerByPlayer(ho.getExpParam().getInt());

                if(controller == null)
                    return new CValue(0.0f);

                value = controller.getAxisValue(OuyaController.AXIS_RS_X);

                if(value == null)
                    return new CValue(0.0f);

                return new CValue(value.floatValue());

            case 8:  /* Right axis Y (player ID) */

                controller = OuyaController.getControllerByPlayer(ho.getExpParam().getInt());

                if(controller == null)
                    return new CValue(0.0f);

                value = controller.getAxisValue(OuyaController.AXIS_RS_Y);

                if(value == null)
                    return new CValue(0.0f);

                return new CValue(value.floatValue());

        }
        return new CValue(0);
    }
    
}
