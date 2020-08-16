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

import android.widget.*;
import android.content.*;
import android.view.*;

import android.text.*;
import android.text.method.*;

import Actions.*;
import Conditions.*;
import Expressions.*;
import Params.*;
import RunLoop.*;
import Services.*;
import Runtime.*;

@SuppressWarnings("deprecation")
public class CRunkcedit extends CRunViewExtension
{
    public int getNumberOfConditions()
    {   return 7;
    }

    private boolean modified;
    private CFontInfo font;

    @Override
    public void createRunView(final CBinaryFile file, CCreateObjectInfo cob, int version)
    {
        final EditText field = new EditText(ho.getControlsContext())
        {
        	@Override
        	public boolean onKeyPreIme(int keyCode, KeyEvent event)
        	{
        		switch (event.getAction())
        		{
        			case KeyEvent.ACTION_DOWN:
        				
            			MMFRuntime.inst.onKeyDown (keyCode, event);
            			break;

        			case KeyEvent.ACTION_UP:
        				
            			MMFRuntime.inst.onKeyUp (keyCode, event);
            			break;
        		}
        		
                return super.onKeyPreIme(keyCode, event);
            }
        };

        field.setGravity (Gravity.LEFT | Gravity.TOP);

        ho.hoImgWidth = file.readShort();
        ho.hoImgHeight = file.readShort();

        if (ho.hoImgHeight < 45)
            ho.hoImgHeight = 45;

        font = file.readLogFont16();

        file.skipBytes(4 * 16); // Custom colors
        file.skipBytes(8); // Foreground/background colors

        file.skipBytes(40); // Text style

        int flags = file.readInt();

        if ((flags & 0x0010) != 0) // Read only
            field.setEnabled(false);

        if ((flags & 0x0020) == 0) // Not multiline
            field.setTransformationMethod(new SingleLineTransformationMethod());

        if ((flags & 0x0040) != 0) // Password
            field.setTransformationMethod(new PasswordTransformationMethod());

        if ((flags & 0x0100) != 0) // Hide on start
            field.setVisibility(EditText.INVISIBLE);

        if ((flags & 0x4000) != 0) // Transparent
            field.setBackgroundDrawable(null);

        if ((flags & 0x00010000) != 0) // Align center
            field.setGravity(Gravity.CENTER_HORIZONTAL);

        if ((flags & 0x00020000) != 0) // Align right
            field.setGravity(Gravity.RIGHT);

        field.addTextChangedListener(new TextWatcher()
        {
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                modified = true;
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void afterTextChanged(Editable s)
            {
            }
        });

        setView (field);
        updateFont (font);
    }

    public CFontInfo getRunObjectFont()
    {
        return font;
    }

    public void setRunObjectFont(CFontInfo font, CRect rc)
    {
        this.font = font;

        updateFont(font);
    }

    public boolean condition(int num, CCndExtension cnd)
    {
        switch (num)
        {
            case 0: // Is visible?

                return view.getVisibility() == EditText.VISIBLE;

            case 1: // Is enabled?

                return view.isEnabled();

            case 2: // Can undo?

                return false;

            case 3: // Just been modified?

                // Doesn't make sense, but this is how flash does it..

                boolean temp = modified;
                modified = false;
                return temp;

            case 4: // Has focus?

                return view.isFocused();

            case 5: // Is number?

                String text = ((EditText) view).getText().toString();

                if (text.length() == 0)
                    return false;

                int index = 0;
                char first = text.charAt(index);

                for (; first == ' ' || first == '	'; first = text.charAt(index))
                {
                }

                return (first >= '0' && first <= '9');

            case 6: // Is selected?

                return ((EditText) view).getSelectionStart() != -1;

        };
        
        return false;
    }

    private void replaceSelection(String replacement)
    {
        final EditText field = (EditText) view;

        String text = field.getText().toString();

        int selectionStart = field.getSelectionStart();
        int selectionEnd = field.getSelectionEnd();

        final String newText = text.substring(0, selectionStart) + replacement
                + text.substring(selectionEnd, text.length());

        field.setText(newText);
    }

    public void action(int num, CActExtension act)
    {
        final EditText field = (EditText) view;

        switch (num)
        {
            case 0: // Load text from file
            {
                field.setText(CServices.loadFile (act.getParamFilename (rh, 0)));
                break;
            }

            case 1: // Load text from file via a file selector

                break;

            case 2: // Save text to file
            {
                CServices.saveFile (act.getParamFilename (rh, 0), field.getText().toString());
                break;
            }

            case 3: // Save text to file via a file selector

                break;

            case 4: // Set text
            {
                field.setText(act.getParamExpString(rh, 0));
                break;
            }

            case 5: // Replace selection

                replaceSelection(act.getParamExpString(rh, 0));
                break;

            case 6: // Cut
            {
                rh.rhApp.clipboard
                        (field.getText().toString().substring(field.getSelectionStart(), field.getSelectionEnd()));

                replaceSelection("");

                break;
            }

            case 7: // Copy
            {
                rh.rhApp.clipboard
                        (field.getText().toString().substring(field.getSelectionStart(), field.getSelectionEnd()));

                break;
            }

            case 8: // Paste
            {
                replaceSelection(rh.rhApp.clipboard ());

                break;
            }

            case 9: // Clear

                field.setText ("");
                break;

            case 10: // Undo

                break;

            case 11: // Clear undo buffer

                break;

            case 12: // Show

                field.setVisibility(EditText.VISIBLE);
                break;

            case 13: // Hide

                field.setVisibility(EditText.INVISIBLE);
                break;

            case 14: // Set font via font selector

                break;

            case 15: // Set font color via color selector

                break;

            case 16: // Activate

                field.requestFocus();
                break;

            case 20: // Set read only off
            case 17: // Enable

                field.setEnabled(true);
                break;

            case 19: // Set read only on
            case 18: // Disable

                field.setEnabled(false);
                break;

            case 21: // Force text modified on

                modified = true;
                break;

            case 22: // Force text modified off

                modified = false;
                break;

            case 23: // Limit text size

                int length = act.getParamExpression (rh, 0);

                InputFilter[] filters = new InputFilter [1];
                filters [0] = new InputFilter.LengthFilter (length);

                ((TextView) view).setFilters (filters);

                break;

            case 24: // Set position

                CPositionInfo position = act.getParamPosition(rh, 0);

                ho.hoX = position.x;
                ho.hoY = position.y;

                break;

            case 25: // Set X position

                ho.hoX = act.getParamExpression(rh, 0);
                break;

            case 26: // Set Y position

                ho.hoY = act.getParamExpression(rh, 0);
                break;

            case 27: // Set size

                ho.setWidth (act.getParamExpression(rh, 0));
                ho.setHeight(act.getParamExpression(rh, 1));

                break;

            case 28: // Set X size

                ho.setWidth(act.getParamExpression(rh, 0));
                break;

            case 29: // Set Y size

                ho.setHeight(act.getParamExpression(rh, 0));
                break;

            case 30: // Deactivate

                view.clearFocus ();
                break;

            case 31: // Scroll to top

                break;

            case 32: // Scroll to line

                break;

            case 33: // Scroll to end

                break;

            case 34: // Set color

                break;

            case 35: // Set background color

                break;
        }
        ;
    }

    public CValue expression(int num)
    {
        final EditText field = (EditText) view;

        switch (num)
        {
            case 0: // Get text

                return new CValue(field.getText().toString());

            case 1: // Get selection

                return new CValue(field.getText().toString().substring(field.getSelectionStart(), field.getSelectionEnd()));

            case 2: // Get X position

                return new CValue(field.getLeft());

            case 3: // Get Y position

                return new CValue(field.getTop());

            case 4: // Get X size

                return new CValue(field.getWidth());

            case 5: // Get Y size

                return new CValue(field.getHeight());

            case 6: // Get value

                return new CValue(CServices.parseInt(field.getText().toString()));

            case 7: // Get first line

                String text = field.getText().toString();
                return new CValue(text.substring(0, Math.min(text.indexOf('\r'), text.indexOf('\n'))));

            case 8: // Get line count

                return new CValue(field.getLineCount());

            case 9: // Get color

                return new CValue(0);

            case 10: // Get background color

                return new CValue(0);
        }
        ;

        return new CValue(0);
    }
}
