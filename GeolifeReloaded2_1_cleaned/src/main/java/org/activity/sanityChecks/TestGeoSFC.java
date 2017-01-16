package org.activity.sanityChecks;

import java.util.List;
import java.util.Locale;

import com.google.uzaygezen.core.BitVector;
import com.google.uzaygezen.core.BitVectorFactories;
import com.google.uzaygezen.core.CompactHilbertCurve;

public class TestGeoSFC
{

	public static void main(String args[])
	{
		CompactHilbertCurve chc = new CompactHilbertCurve(new int[] { 2, 2 });
		List<Integer> bitsPerDimension = chc.getSpec().getBitsPerDimension();
		BitVector[] p = new BitVector[bitsPerDimension.size()];
		for (int i = p.length; --i >= 0;)
		{
			p[i] = BitVectorFactories.OPTIMAL.apply(bitsPerDimension.get(i));
		}

		p[0].copyFrom(0b10);
		p[1].copyFrom(0b11);
		BitVector chi = BitVectorFactories.OPTIMAL.apply(chc.getSpec().sumBitsPerDimension());
		chc.index(p, 0, chi);
		System.out.println(String.format(Locale.ROOT, "index([0b%s, 0b%s])=0b%s", p[0], p[1], chi));
		// Next line overwrites whatever is already written in p.
		chc.indexInverse(chi, p);
		System.out.println(String.format(Locale.ROOT, "indexInverse(0b%s)=[0b%s, 0b%s]", chi, p[0], p[1]));
	}
}
