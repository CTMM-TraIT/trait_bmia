/**
 * MFNodeByKindAndQNameFilter.java
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
import javax.xml.namespace.QName;

public class MFNodeByKindAndQNameFilter implements IEnumerable 
{
	public static class Enumerator implements IEnumerator 
	{
		IEnumerator baseEnum;
		int nodeKind;
		QName qname;
		int pos=0;
				
		public Enumerator(IEnumerator baseEnum, int nodeKind, QName qname)
		{
			this.baseEnum = baseEnum;
			this.nodeKind = nodeKind;
			this.qname = qname;
		}
		
		public boolean moveNext() throws Exception
		{
			while (baseEnum.moveNext())
			{
				Object o = baseEnum.current();
				if (o instanceof IMFNode)
				{
					IMFNode node = (IMFNode) o;
					if ((node.getNodeKind() & nodeKind) != 0 && node.getQName().equals(qname))
					{	
						pos++;
						return true;
					}
				}
			}
			return false;
		}
		
		public Object current() 
		{
			return baseEnum.current();
		}
		
		public int position() {return pos;}
		
		public void close() {baseEnum.close();}
	}
	
	private IEnumerable baseSet;
	private int nodeKind;
	private QName qname;
	
	public MFNodeByKindAndQNameFilter(IEnumerable baseSet, int nodeKind, QName qname)
	{
		this.baseSet = baseSet;
		this.nodeKind = nodeKind;
		this.qname = qname;
	}
	
	public IEnumerator enumerator() throws Exception
	{
		return new Enumerator(baseSet.enumerator(), nodeKind, qname);
	}
}
