package Services;

import java.util.ArrayList;

public class CArrayList<T> extends ArrayList<T>
{
	@Override
	public T get(int index)
	{
		try
		{
			return super.get(index);
		}
		catch(IndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	public void setAtGrow(int index, T value)
	{
        ensureCapacity(index + 1);
        
        while(size() <= index)
        	add(null);
		
        set(index, value);
	}
}
