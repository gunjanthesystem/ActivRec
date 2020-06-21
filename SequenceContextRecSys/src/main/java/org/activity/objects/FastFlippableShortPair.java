package org.activity.objects;

import java.io.Serializable;

import org.activity.ui.PopUps;

/**
 * 
 * @author gunjan
 * @since 22 July 2018
 */
public class FastFlippableShortPair implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final short lower;
	private final short higher;

	/**
	 * Constructs a <code>FastFlippableIntPair</code> with the specified a and b.
	 *
	 * @param a
	 * 
	 * @param b
	 * 
	 */
	public FastFlippableShortPair(int a, int b)
	{
		if (Math.abs(a) > 32767 || Math.abs(b) > 32767)
		{
			PopUps.showError(
					"Error in org.activity.objects.FastFlippableShortPair.FastFlippableShortPair(int, int): values out of short range: a= "
							+ a + " b=" + b);
			System.exit(-1);
		}
		if (a <= b)
		{
			this.lower = (short) a;
			this.higher = (short) b;
		}
		else// a>b
		{
			this.lower = (short) b;
			this.higher = (short) a;
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
		FastFlippableShortPair other = (FastFlippableShortPair) obj;
		if (higher != other.higher) return false;
		if (lower != other.lower) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "[" + lower + "," + higher + "]";
	}

	public short getLower()
	{
		return lower;
	}

	public short getHigher()
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
