
package jdd.examples;

import jdd.util.Array;
import jdd.util.JDDConsole;
import jdd.util.Options;
import jdd.util.math.Digits;
import jdd.zdd.ZDD;
import jdd.zdd.ZDDCSP;

/**
 * N Queen with Z-BDDs and the CSP procedures.
 * <p>The implementation comes directly from a famous Z-BDD paper [Okuno's ].
 *
 * <p>It will probably go much faster if we can figure out how to make the exclude-operator
 * native [current version makes calls to restrict and diff]...
 *
 * @see ZDD
 * @see ZDDCSP
 */

public class ZDDCSPQueens extends ZDDCSP implements Queens {
	private int n, sols;
	private int [] x, xv;
	private int get(int i, int j) { return x[ i + j * n]; }
	private int getVar(int i, int j) { return xv[ i + j * n]; }
	private boolean [] solvec;
	private long time, memory;

	public ZDDCSPQueens(int n) {
		super(1+Math.max(1000, (int) (Math.pow(2, n-5))*800), 10000);

		time = System.currentTimeMillis() ;
		this.n = n;
		x = new int[ n * n];
		xv = new int[ n * n];
		boolean[] mark = new boolean[n * n];
		for(int i = 0; i < n * n; i++) {
			xv[i] = createVar();
			x[i] = ref( change(1, xv[i]) );
		}



		// compute G1
		int G1 = 0;
		for(int i = 0; i < n; i++) G1 = unionWith(G1, get(0, i) );

		// compute the rest
		int last_G = G1;
		for(int i = 1; i < n; i++) {
			int F = 0;
			for(int j = 0; j < n; j++)  {
				int bld = build(i, j, last_G, mark);
				F = unionWith( F, bld );
				deref(bld);
			}
			deref( last_G );
			last_G = F;

		}

		solvec = satOne(last_G, null);

		sols = count(last_G);
		deref(last_G);
		time = System.currentTimeMillis() - time;
		if(Options.verbose) showStats();
		memory = getMemoryUsage();
		cleanup();
	}

	// --- [Queens interface ] ---------------------------------------------
	public int getN() { return n; }
	public double numberOfSolutions() { return sols; }
	public long getTime() { return time; }
	public long getMemory() { return memory; }
	public boolean [] getOneSolution() { return solvec; }

	// --- [ internal stuff ] --------------------------------------------------
	private boolean valid(int a, int b) { return (a >= 0 && a < n) && (b >= 0 && b < n); }

	private int build(int i, int j, int G, boolean []mark) {
		Array.set(mark, false);

		for(int k = 0; k < i; k++)  mark[ k + n * j] = true;
		for(int k = 1; k <= i; k++)  {
			int a = j - k, b = i - k;
			if(valid(b, a)) mark[b + n * a] = true;
			a = j + k;
			if(valid(b, a)) mark[b + n * a] = true;
		}


		int C = 0;
		for(int k = 0; k < n * n; k++) {
			if(mark[k]) {
				int a = k / n, b = k % n;
				C = unionWith(C, get(b, a) );
			}
		}

		int tmp = ref( exclude(G,C) );
		deref(C);
		int ret = ref( mul( tmp, get(i,j)) );
		deref(tmp);
		return ret;
	}

	// -------------------------------------------------------------
	private int unionWith(int a, int b) {
		int tmp = ref( union(a,b) );
		deref(a);
		return tmp;
	}

	public static void main(String [] args) {
		for(String str :args) {
			final int n = Integer.parseInt(str );

			ZDDCSPQueens q = new ZDDCSPQueens( n );
			JDDConsole.out.printf("ZDDCSP-Queen\tSolutions=%.0f\tN=%d\tmem=%s\ttime=%d\n",
				q.numberOfSolutions(), n, Digits.prettify1024((long) q.getMemory()), q.getTime());
		}
	}
}
