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
// CVALUE : classe de calcul et de stockage de valeurs
//
//----------------------------------------------------------------------------------
package Expressions;


public class CValue 
{
    public static final byte TYPE_INT=0;
    public static final byte TYPE_DOUBLE=1;
    public static final byte TYPE_STRING=2;

    public byte type;
    public int intValue;
    public double doubleValue;
    public String stringValue;
   
    public CValue() 
    {
		type=TYPE_INT;
		intValue=0;
    }
    public CValue(CValue value) 
    {
		switch(value.type)
		{
		    case 0:
				intValue=value.intValue;
				break;
		    case 1:
				doubleValue=value.doubleValue;
				break;
		    case 2:
				stringValue=new String(value.stringValue);
				break;
		}
		type=value.type;
    }
    public CValue(int i) 
    {
		type=TYPE_INT;
		intValue=i;
    }
    public CValue(double d) 
    {
		type=TYPE_DOUBLE;
		doubleValue=d;
    }
    public CValue(String s) 
    {
		type=TYPE_STRING;
		stringValue=s;
    }
    public byte getType()
    {
    	return type;
    }
    public int getInt()
    {
		switch(type)
		{
		    case 0:
				return intValue;
		    case 1:
				return (int)doubleValue;
		}
		return 0;
    }
    public double getDouble()
    {
		switch(type)
		{
		    case 0:
				return (double)intValue;
		    case 1:
				return doubleValue;
		}
		return 0;
    }
    public String getString()
    {
		if (type==TYPE_STRING)
		    return stringValue;
		return "";
    }
    public void forceInt(int value)
    {
		type=TYPE_INT;
		intValue=value;
    }
    public void forceDouble(double value)
    {
		type=TYPE_DOUBLE;
		doubleValue=value;
    }
    public void forceString(String value)
    {
		type=TYPE_STRING;
		stringValue=new String(value);
    }
    public void forceValue(CValue value)
    {
		type=value.type;
		switch (type)
		{
		    case 0:
				intValue=value.intValue;
				break;
		    case 1:
				doubleValue=value.doubleValue;
				break;
		    case 2:
				stringValue=new String(value.stringValue);
				break;
		}
    }
    public void setValue(CValue value)
    {
		switch (type)
		{
		    case 0:
				intValue=value.getInt();
				break;
		    case 1:
				doubleValue=value.getDouble();
				break;
		    case 2:
				stringValue=new String(value.stringValue);
				break;
		}
    }
    public void getCompatibleTypes(CValue value)
    {
		if (type==TYPE_INT && value.type==TYPE_DOUBLE)
		    convertToDouble();
		else if (type==TYPE_DOUBLE && value.type==TYPE_INT)
		    value.convertToDouble();
    }
    public void convertToDouble()
    {
		if (type==TYPE_INT)
		{
		    doubleValue=(double)intValue;
		    type=TYPE_DOUBLE;
		}
    }
    public void convertToInt()
    {
		if (type==TYPE_DOUBLE)
		{
		    intValue=(int)doubleValue;
		    type=TYPE_INT;
		}
    }
    public void add(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:	// TYPE_INT:
				intValue+=value.intValue;
				break;
		    case 1:	// TYPE_DOUBLE:
				doubleValue+=value.doubleValue;
				break;
		    case 2:	// TYPE_STRING:
				stringValue=new String(stringValue+value.stringValue);
				break;
		}
    }
    public void sub(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:	// TYPE_INT:
				intValue-=value.intValue;
				break;
		    case 1:	// TYPE_DOUBLE:
				doubleValue-=value.doubleValue;
				break;
		}
    }
    public void negate()
    {
		switch (type)
		{
		    case 0:
				intValue=-intValue;
				break;
		    case 1:
				doubleValue=-doubleValue;
				break;
		}
    }
    public void mul(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				intValue*=value.intValue;
				break;
		    case 1:
				doubleValue*=value.doubleValue;
				break;
		}
    }
    public void div(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				if ( value.intValue != 0 )
				    intValue/=value.intValue;
				else
				    intValue=0;
				break;
		    case 1:
				if ( value.doubleValue != 0.0 )
				    doubleValue/=value.doubleValue;
				else
				    doubleValue=0.0;
				break;
		}
    }
    public void pow(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				intValue = (int) Math.pow(intValue, value.intValue);
				break;
		    case 1:
				doubleValue = Math.pow(doubleValue, value.doubleValue);
				break;
		}
    }
    public void mod(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				if (value.intValue==0)
				    intValue=0;
				else
				    intValue%=value.intValue;
				break;
		    case 1:
				if (value.doubleValue==0.0)
				    doubleValue=0.0;
				else
				    doubleValue%=value.doubleValue;
				break;
		}
    }
    public void andLog(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				intValue&=value.intValue;
				break;
		    case 1:
				forceInt(getInt()&value.getInt());
				break;
		}
    }
    public void orLog(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				intValue|=value.intValue;
				break;
		    case 1:
				forceInt(getInt()|value.getInt());
				break;
		}
    }
    public void xorLog(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				intValue^=value.intValue;
				break;
		    case 1:
				forceInt(getInt()^value.getInt());
				break;
		}
    }
    public boolean equal(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				return (intValue==value.intValue);
		    case 1:
				return (doubleValue==value.doubleValue);
		    case 2:
		    	return stringValue.equals(value.stringValue);
		}
		return false;
    }
    public boolean greater(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				return (intValue>=value.intValue);
		    case 1:
				return (doubleValue>=value.doubleValue);
		    case 2:
				return stringValue.compareTo(value.stringValue)>=0;
		}
		return false;
    }
    public boolean lower(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				return (intValue<=value.intValue);
		    case 1:
				return (doubleValue<=value.doubleValue);
		    case 2:
				return stringValue.compareTo(value.stringValue)<=0;
		}
		return false;
    }
    public boolean greaterThan(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				return (intValue>value.intValue);
		    case 1:
				return (doubleValue>value.doubleValue);
		    case 2:
				return stringValue.compareTo(value.stringValue)>0;
		}
		return false;
    }
    public boolean lowerThan(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				return (intValue<value.intValue);
		    case 1:
				return (doubleValue<value.doubleValue);
		    case 2:
				return stringValue.compareTo(value.stringValue)<0;
		}
		return false;
    }
    public boolean notEqual(CValue value)
    {
		if (type!=value.type)
		{
		    getCompatibleTypes(value);
		}
	
		switch (type)
		{
		    case 0:
				return (intValue!=value.intValue);
		    case 1:
				return (doubleValue!=value.doubleValue);
		    case 2:
				return stringValue.compareTo(value.stringValue)!=0;
		}
		return false;
    }
}
