package org.activity.stats;

import java.util.List;
import java.util.Locale;

import com.google.uzaygezen.core.BitVector;
import com.google.uzaygezen.core.BitVectorFactories;
import com.google.uzaygezen.core.CompactHilbertCurve;

/**
 * This utilises classes from the project at https://code.google.com/p/uzaygezen/ Note: we are using 0.1 version of the
 * project as 0.2 sources files are not properly packaged in their download.
 * 
 * @author gunjan
 *
 */
public class HilbertCurveUtils
{

	static final int precisionInBits = 70; // 2^31 = 2147483648

	/**
	 * Computes a one dimensional index for a given two dimensional coordinate using Compact Hilbert Space Filled Curve.
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public static long getCompactHilbertCurveIndex(long lat, long lon)
	{
		BitVector chi;

		// System.out.println("Inside getCompactHilbertCurveIndex");
		CompactHilbertCurve chc = new CompactHilbertCurve(new int[] { precisionInBits, precisionInBits });

		List<Integer> bitsPerDimension = chc.getSpec().getBitsPerDimension();
		// System.out.println("Bits per dimension: = " + bitsPerDimension.toString());

		BitVector[] p = new BitVector[bitsPerDimension.size()];// coordinates of a point in a multi-dimensional space
																// are represented as an array of type BitVector[]

		// creating emptry bitvector
		for (int i = p.length; --i >= 0;)
		{
			p[i] = BitVectorFactories.OPTIMAL.apply(bitsPerDimension.get(i));// creating emptry bitvector of
																				// bitsPerDimension.get(i), i,e, num of
																				// bits for that dimension as
																				// precision
		}

		p[0].copyFrom(lat);// (0b10);//initialising
		p[1].copyFrom(lon);// (0b11);

		chi = BitVectorFactories.OPTIMAL.apply(chc.getSpec().sumBitsPerDimension()); // empty preallocated output
																						// variable

		chc.index(p, 0, chi);

		return chi.toLong();
	}

	/**
	 * for test and experiments
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("Ajooba");
		BitVector chi;

		CompactHilbertCurve chc = new CompactHilbertCurve(new int[] { precisionInBits, precisionInBits });

		List<Integer> bitsPerDimension = chc.getSpec().getBitsPerDimension();
		System.out.println("Bits per dimension: = " + bitsPerDimension.toString());

		BitVector[] p = new BitVector[bitsPerDimension.size()];// coordinates of a point in a multi-dimensional space
																// are represented as an array of type BitVector[]

		// creating emptry bitvector
		for (int i = p.length; --i >= 0;)
		{
			p[i] = BitVectorFactories.OPTIMAL.apply(bitsPerDimension.get(i));// creating emptry bitvector of
																				// bitsPerDimension.get(i), i,e, num of
																				// bits for that dimension as
																				// precision
			System.out.println("p[" + i + "] = " + p[i]);
		}

		System.out.println("======================");

		p[0].copyFrom(12);// (0b10);//initialising
		p[1].copyFrom(55);// (0b11);

		chi = BitVectorFactories.OPTIMAL.apply(chc.getSpec().sumBitsPerDimension()); // empty preallocated output
																						// variable

		chc.index(p, 0, chi);

		System.out.println(String.format(Locale.ROOT, "index([%s, %s])=%s", p[0].toExactLong(), p[1].toExactLong(),
				chi.toExactLong()));
		// System.out.println(String.format(Locale.ROOT, "index([0b%s, 0b%s])=0b%s", p[0], p[1], chi));

		// Next line overwrites whatever is already written in p.
		chc.indexInverse(chi, p);
		System.out.println(String.format(Locale.ROOT, "indexInverse(%s)=[%s, %s]", chi.toExactLong(),
				p[0].toExactLong(), p[1].toExactLong()));
		// System.out.println(String.format(Locale.ROOT, "indexInverse(0b%s)=[0b%s, 0b%s]", chi, p[0], p[1]));

		System.out.println("======================");

		p[0].copyFrom(12);// (0b10);//initialising
		p[1].copyFrom(57);// (0b11);

		chi = BitVectorFactories.OPTIMAL.apply(chc.getSpec().sumBitsPerDimension()); // empty preallocated output
																						// variable

		chc.index(p, 0, chi);

		System.out.println(String.format(Locale.ROOT, "index([%s, %s])=%s", p[0].toExactLong(), p[1].toExactLong(),
				chi.toExactLong()));
		// System.out.println(String.format(Locale.ROOT, "index([0b%s, 0b%s])=0b%s", p[0], p[1], chi));

		// Next line overwrites whatever is already written in p.
		chc.indexInverse(chi, p);
		System.out.println(String.format(Locale.ROOT, "indexInverse(%s)=[%s, %s]", chi.toExactLong(),
				p[0].toExactLong(), p[1].toExactLong()));
		// System.out.println(String.format(Locale.ROOT, "indexInverse(0b%s)=[0b%s, 0b%s]", chi, p[0], p[1]));

		System.out.println("======================");

		p[0].copyFrom(12);// (0b10);//initialising
		p[1].copyFrom(55);// (0b11);

		chi = BitVectorFactories.OPTIMAL.apply(chc.getSpec().sumBitsPerDimension()); // empty preallocated output
																						// variable

		chc.index(p, 0, chi);

		System.out.println(String.format(Locale.ROOT, "index([%s, %s])=%s", p[0].toExactLong(), p[1].toExactLong(),
				chi.toExactLong()));
		// System.out.println(String.format(Locale.ROOT, "index([0b%s, 0b%s])=0b%s", p[0], p[1], chi));

		// Next line overwrites whatever is already written in p.
		chc.indexInverse(chi, p);
		System.out.println(String.format(Locale.ROOT, "indexInverse(%s)=[%s, %s]", chi.toExactLong(),
				p[0].toExactLong(), p[1].toExactLong()));
		// System.out.println(String.format(Locale.ROOT, "indexInverse(0b%s)=[0b%s, 0b%s]", chi, p[0], p[1]));

		System.out.println("======================");

		p[0].copyFrom(0);// (0b10);//initialising
		p[1].copyFrom(5);// (0b11);

		chi = BitVectorFactories.OPTIMAL.apply(chc.getSpec().sumBitsPerDimension()); // empty preallocated output
																						// variable

		chc.index(p, 0, chi);

		System.out.println(String.format(Locale.ROOT, "index([%s, %s])=%s", p[0].toExactLong(), p[1].toExactLong(),
				chi.toExactLong()));
		// System.out.println(String.format(Locale.ROOT, "index([0b%s, 0b%s])=0b%s", p[0], p[1], chi));

		// Next line overwrites whatever is already written in p.
		chc.indexInverse(chi, p);
		System.out.println(String.format(Locale.ROOT, "indexInverse(%s)=[%s, %s]", chi.toExactLong(),
				p[0].toExactLong(), p[1].toExactLong()));
		// System.out.println(String.format(Locale.ROOT, "indexInverse(0b%s)=[0b%s, 0b%s]", chi, p[0], p[1]));

		System.out.println("======================");

		System.out.println("======================");

		p[0].copyFrom(122222222);// (0b10);//initialising
		p[1].copyFrom(5);// (0b11);

		chi = BitVectorFactories.OPTIMAL.apply(chc.getSpec().sumBitsPerDimension()); // empty preallocated output
																						// variable

		chc.index(p, 0, chi);

		System.out.println(String.format(Locale.ROOT, "index([%s, %s])=%s", p[0].toExactLong(), p[1].toExactLong(),
				chi.toExactLong()));
		// System.out.println(String.format(Locale.ROOT, "index([0b%s, 0b%s])=0b%s", p[0], p[1], chi));

		// Next line overwrites whatever is already written in p.
		chc.indexInverse(chi, p);
		System.out.println(String.format(Locale.ROOT, "indexInverse(%s)=[%s, %s]", chi.toExactLong(),
				p[0].toExactLong(), p[1].toExactLong()));
		// System.out.println(String.format(Locale.ROOT, "indexInverse(0b%s)=[0b%s, 0b%s]", chi, p[0], p[1]));
		System.out.println("======================");

		p[1].copyFrom(122222222);// (0b10);//initialising
		p[0].copyFrom(8);// (0b11);

		chi = BitVectorFactories.OPTIMAL.apply(chc.getSpec().sumBitsPerDimension()); // empty preallocated output
																						// variable

		chc.index(p, 0, chi);

		System.out.println(String.format(Locale.ROOT, "index([%s, %s])=%s", p[0].toExactLong(), p[1].toExactLong(),
				chi.toExactLong()));
		// System.out.println(String.format(Locale.ROOT, "index([0b%s, 0b%s])=0b%s", p[0], p[1], chi));

		// Next line overwrites whatever is already written in p.
		chc.indexInverse(chi, p);
		System.out.println(String.format(Locale.ROOT, "indexInverse(%s)=[%s, %s]", chi.toExactLong(),
				p[0].toExactLong(), p[1].toExactLong()));
		// System.out.println(String.format(Locale.ROOT, "indexInverse(0b%s)=[0b%s, 0b%s]", chi, p[0], p[1]));

		System.out.println("======================\n my function:");

		System.out
				.println(String.format(Locale.ROOT, "index([%s, %s])=%s", 12, 55, getCompactHilbertCurveIndex(12, 55)));

		System.out
				.println(String.format(Locale.ROOT, "index([%s, %s])=%s", 12, 57, getCompactHilbertCurveIndex(12, 57)));
	}

}
