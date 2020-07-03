package org.activity.objects;

import java.io.Serializable;

/**
 * 
 * @author gunjan
 * @since 22 July 2018
 */
public class FastFlippableIntPair implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final int lower;
	private final int higher;

	/**
	 * Constructs a <code>FastFlippableIntPair</code> with the specified a and b.
	 *
	 * @param a
	 * 
	 * @param b
	 * 
	 */
	public FastFlippableIntPair(int a, int b)
	{
		if (a <= b)
		{
			this.lower = a;
			this.higher = b;
		}
		else// a>b
		{
			this.lower = b;
			this.higher = a;
		}
	}

	/**
	 * Cache the hash code to make computing hashes faster.
	 */
	private int hash = 0; // inspired from javafx.geometry.Dimension2D.hash

	@Override
	public int hashCode()
	{
		if (hash == 0)
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + higher;
			result = prime * result + lower;

			hash = result;
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FastFlippableIntPair other = (FastFlippableIntPair) obj;
		if (higher != other.higher) return false;
		if (lower != other.lower) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "[" + lower + "," + higher + "]";
	}

	public int getLower()
	{
		return lower;
	}

	public int getHigher()
	{
		return higher;
	}

}
// /**
// * Indicates whether some other object is "equal to" this one.
// *
// * @param obj
// * the reference object with which to compare
// * @return true if this Dimension2D instance is the same as the obj argument; false otherwise
// */
// @Override
// public boolean equals(Object obj)
// {
// if (obj == this) return true;
// if (obj instanceof FastFlippableIntPair)
// {
// FastFlippableIntPair other = (FastFlippableIntPair) obj;
// return getWidth() == other.getWidth() && getHeight() == other.getHeight();
// }
// else
// return false;
// }

// @Override
// public int hashCode()
// {
// if (hash == 0)
// {
// long bits = 7L;
// bits = 31L * bits + Double.doubleToLongBits(getWidth());
// bits = 31L * bits + Double.doubleToLongBits(getHeight());
// hash = (int) (bits ^ (bits >> 32));
// }
// return hash;
// }

/**
 * Returns a string representation of this {@code Dimension2D}. This method is intended to be used only for
 * informational purposes. The content and format of the returned string might vary between implementations. The
 * returned string might be empty but cannot be {@code null}.
 */
// @Override
// public String toString()
// {
// return "Dimension2D [width = " + getWidth() + ", height = " + getHeight() + "]";
// }
