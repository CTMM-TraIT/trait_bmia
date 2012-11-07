/**
 * SequenceJoin.java
 *
 * This file was generated by MapForce 2011r2sp1.
 *
 * YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
 * OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
 *
 * Refer to the MapForce Documentation for further details.
 * http://www.altova.com/mapforce
 */

package com.altova.mapforce;

public class SequenceJoin implements IEnumerable 
{
	private IEnumerable first;
	private IEnumerable second;
	
	public static class Enumerator implements IEnumerator 
	{
		private IEnumerator first = null;
		private IEnumerator second = null;
		int pos = 0;
				
		public Enumerator (IEnumerator first, IEnumerator second)
		{
			this.first = first;
			this.second = second;
		}
		
		public Object current() 
		{
			return (first != null) ? first.current() : second.current(); 
		}
		
		public int position() {return pos;}

		public boolean moveNext() throws Exception 
		{
			if (first != null)
			{
				boolean b = first.moveNext();
				if (b)
				{
					pos++;
					return true;
				}
				first = null;
			}
			if(second.moveNext())
			{
				pos++;
				return true;
			}
			return false;
		}
		
		public void close() {first.close(); second.close();}
	}
	
	public SequenceJoin(IEnumerable a, IEnumerable b)
	{
		first = a;
		second = b;
	}
	
	public IEnumerator enumerator() throws Exception 
	{
		return new Enumerator(first.enumerator(), second.enumerator());
	}
}
