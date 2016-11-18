package org.activity.objects;

public class Triple<A, B, C> // implements Comparable
{
	private A first;
	private B second; // 'second' is used for comparison
	private C third;
	
	public Triple(A first, B second, C third)
	{
		super();
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	// public Triple()
	// {
	//
	// }
	
	public int hashCode() // not sure
	{
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;
		int hashThird = third != null ? third.hashCode() : 0;
		
		return (hashFirst + hashSecond + hashThird) * hashSecond + hashFirst + hashThird;
	}
	
	/*
	 * public boolean equals(Object other) { if (other instanceof Pair) { Pair otherPair = (Pair) other; return (( this.first == otherPair.first || ( this.first != null &&
	 * otherPair.first != null && this.first.equals(otherPair.first))) && ( this.second == otherPair.second || ( this.second != null && otherPair.second != null &&
	 * this.second.equals(otherPair.second))) ); }
	 * 
	 * return false; }
	 */
	public int compareTo(Triple<A, B, C> tripleTarget)
	{
		// System.out.println("Debug inside Triple's compareTo()");
		// System.out.println(tripleTarget.toString());
		Double thisThird = (Double) this.third;
		Double targetThird = (Double) tripleTarget.getThird();
		
		return thisThird.compareTo(targetThird);
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
		return "(" + first + ", " + second + ", " + third + ")";
	}
	
	public String toStringCSV()
	{
		return first + ", " + second + ", " + third;
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
	
	public C getThird()
	{
		return third;
	}
	
	public void setThird(C third)
	{
		this.third = third;
	}
	
	/*
	 * @Override public int compareTo(Object target) { // TODO Auto-generated method stub Pair<A,B> pairTarget = (Pair<A,B>)target;
	 * 
	 * Double thisSecond= (Double) this.second; Double targetSecond = (Double) pairTarget.getSecond();
	 * 
	 * return thisSecond.compareTo(targetSecond); }
	 */
}
