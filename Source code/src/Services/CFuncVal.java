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
// CFUNCVAL petite classe pour parser une chaine
//
//----------------------------------------------------------------------------------
package Services;

/** A little class to parse a number in a string.
 */
public class CFuncVal
{
    public int intValue;
    public double doubleValue;

    public int parse(String s)
    {
        // Un nombre hexadecimal?
        try
        {
            String ss;
            if (s.length() >= 3)
            {
                if (s.charAt(0) == '0' && (s.charAt(1) == 'x' || s.charAt(2) == 'X'))
                {
                    ss = s.substring(2);
                    intValue = Integer.parseInt(ss, 16);
                    return 0;
                }
                if (s.charAt(0) == '0' && (s.charAt(1) == 'b' || s.charAt(2) == 'B'))
                {
                    ss = s.substring(2);
                    intValue = Integer.parseInt(ss, 2);
                    return 0;
                }
            }
            double d = Double.valueOf(s).doubleValue();
            int frac = (int) d;
            if (d - frac != 0)
            {
                doubleValue = d;
                return 1;
            }
            intValue = (int) d;
            return 0;
        }
        catch (NumberFormatException e)
        {
        }
        intValue = 0;
        return 0;
    }
}
