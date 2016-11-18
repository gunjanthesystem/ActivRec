/* ref: http://stackoverflow.com/questions/156275/what-is-the-equivalent-of-the-c-pairl-r-in-java
 */
package org.activity.objects;

public class Pair<A, B> // implements Comparable
{
	private A first;
	private B second; // 'second' is used for comparison
	
	public Pair(A first, B second)
	{
		super();
		this.first = first;
		this.second = second;
	}
	
	public Pair()
	{
		super();
		
	}
	
	public int hashCode()
	{
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;
		
		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}
	
	public boolean equals(Object other)
	{
		if (other instanceof Pair)
		{
			Pair otherPair = (Pair) other;
			return ((this.first == otherPair.first || (this.first != null && otherPair.first != null && this.first.equals(otherPair.first))) && (this.second == otherPair.second || (this.second != null
					&& otherPair.second != null && this.second.equals(otherPair.second))));
		}
		
		return false;
	}
	
	/**
	 * ignore order of first and second element, check if the two items in given pair is equal to the two items in this pair
	 * 
	 * @param other
	 * @return
	 */
	public boolean equalsIgnoreOrder(Object other)
	{
		if (other instanceof Pair)
		{
			if (!this.getFirst().getClass().equals(this.getSecond().getClass()))
			{
				new Exception("Exception in org.activity.util.Pair.equalsIgnoreOrder(Object): first and seconds elements of current Pair are different datatypes.\n For Pair "
						+ this.toString() + " First:" + this.getFirst().getClass() + "  and second:" + this.getSecond().getClass()).printStackTrace();
				return false;
			}
			else if (!((Pair) other).getFirst().getClass().equals(((Pair) other).getSecond().getClass()))
			{
				new Exception("Exception in org.activity.util.Pair.equalsIgnoreOrder(Object): first and seconds elements of given Pair are different datatypes.\n For Pair "
						+ other.toString() + " First:" + ((Pair) other).getFirst().getClass() + "  and second:" + ((Pair) other).getSecond().getClass()).printStackTrace();
				return false;
			}
			
			else
			// only if for the each pair compared, first and second elements are of comparable data types
			{
				if (this.equals(other)) // check for equality without reversing order
				{
					return true;
				}
				
				else
				// check for equality reversing the order of first and second elements
				{
					Pair reversedPair = new Pair(((Pair) other).getSecond(), ((Pair) other).getFirst());
					return this.equals(reversedPair);
				}
			}
		}
		return false;
	}
	
	public int compareTo(Pair<A, B> pairTarget)
	{
		Double thisSecond = (Double) this.second;
		Double targetSecond = (Double) pairTarget.getSecond();
		
		return thisSecond.compareTo(targetSecond);
		/*
		 * if(this.second < pairTarget.getSecond()) return -1;
		 * 
		 * else if(this.second == pairTarget.getSecond()) return 0;
		 * 
		 * else //this.second>pairTarget.getSecond return 1;
		 */
	}
	
	public String toString()
	{
		return "(" + first + ", " + second + ")";
	}
	
	public A getFirst()
	{
		return first;
	}
	
	public void setFirst(A first)
	{
		this.first = first;
	}
	
	public B getSecond()
	{
		return second;
	}
	
	public void setSecond(B second)
	{
		this.second = second;
	}
	
	/*
	 * @Override public int compareTo(Object target) { // TODO Auto-generated method stub Pair<A,B> pairTarget = (Pair<A,B>)target;
	 * 
	 * Double thisSecond= (Double) this.second; Double targetSecond = (Double) pairTarget.getSecond();
	 * 
	 * return thisSecond.compareTo(targetSecond); }
	 */
}
