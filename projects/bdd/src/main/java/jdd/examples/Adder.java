
package jdd.examples;


import jdd.bdd.BDD;
import jdd.util.JDDConsole;
import jdd.util.Options;

/**
 * Adder.java, converted from BuDDy:s source:
 * <pre>
 * 	FILE:  adder.cc
 * 	DESCR: BDD implementation of an N bit adder.
 * 	AUTH:  Jorn Lind
 * 	DATE:  feb 1998
 * </pre>
 *
 * This class builds a BDD representation of an N-bit adder.
 *
 * @see ConfigExample
 */


public class Adder
//	extends ProfiledBDD2
	extends BDD
{
	private int N;
	private int [] ainp;
	private int [] binp;
	private int [] not_ainp;
	private int [] not_binp;
	private int [] co;
	private int [] xout;

	/** create an N-bit adder */

	public Adder(int N) {
		super(505 + N,2000);
		this.N = N;

		this.ainp = new int[N];
		this.binp = new int[N];
		this.not_ainp = new int[N];
		this.not_binp = new int[N];
		this.co   = new int[N];
		this.xout = new int[N];


		for(int n = 0; n < N; n++) {
			ainp[n] = createVar();
			binp[n] = createVar();
			not_ainp[n] = ref( not(ainp[n]) );
			not_binp[n] = ref( not(binp[n]) );
		}
		build_adder();
	}

	private void build_adder() {
		for(int n = 0; n < N; n++) {
			if(n > 0) {
				// xout[n] = ainp[n] ^ binp[n] ^ co[n-1];
				// co[n] = ainp[n] & binp[n] | ainp[n] & co[n-1] |binp[n] & co[n-1];

				int tmp1 = ref( xor(ainp[n], binp[n]) );
				xout[n] = ref( xor(tmp1, co[n-1]) );
				deref(tmp1);

				tmp1 = ref( and(ainp[n], binp[n]) );
				int tmp2 = ref( and(ainp[n], co[n-1]) );
				int tmp4 = ref( or(tmp1, tmp2) );
				deref(tmp1);
				deref(tmp2);


				int tmp3 = ref( and(binp[n], co[n-1]) );
				co[n] = ref( or(tmp4, tmp3) );
				deref(tmp3);
				deref(tmp4);
			} else {
				xout[n] = ref( xor(ainp[n], binp[n]) );
				co[n] = ref( and(ainp[n], binp[n]) );
			}
		}
	}

	public void dump() {
		for(int n = 0; n < N; n++) {
			System.out.println("Out[" + n + "]: " + nodeCount(xout[n]) + " nodes");
		}
	}


	// ------------- [ debugging ] --------------------------------------

	/** build a binary encoding of the number "val", use_a indicates if a or b is used */
	private int setval(int val, boolean use_a) {
		int x = 1;
		for(int n = 0; n < N; n++) {
			if( (val & 1) != 0) {
				x = andTo(x, use_a ? ainp[n] : binp[n]);
			} else {
				x = andTo(x, use_a ? not_ainp[n] : not_binp[n]);
			}
			val >>>= 1;
		}
		return x;
	}

	private boolean test_vector(int av, int bv, int a, int b) {
		int res = a + b;
		for(int n = 0; n < N; n++) {
			int resv = ref( and(av, bv));
			resv = andTo(resv, xout[n]);

			boolean fail = ((resv == 0 && (res &1) != 0) || (resv != 0 && (res & 1) == 0));
			deref(resv);

			if(fail) {
				JDDConsole.out.println("resv = " + resv + ", res = " + res);
				return false;
			}
			res >>>= 1;
		}
		return true;
	}

	public boolean  test_adder() {
		int m = 1 << N;
		for(int a = 0; a < m; a++) {
			for(int b = 0; b < m; b++) {
				int av = setval(a, true);
				int bv = setval(b, false);

				boolean ret = test_vector(av,bv,a,b);
				deref(av);
				deref(bv);
				if(!ret) return false;
			}
		}
		return true;
	}

	public static void main(String [] args) {
		// Options.profile_cache = true; // <-- to see cache profiling

		if(args.length >= 1) {
			boolean test = false, dump = false, verbose = false;;
			int n = -1;
			for(int i = 0; i < args.length; i++) {
				if(args[i].equals("-t")) test = true;
				else if(args[i].equals("-d")) dump = true;
				else if(args[i].equals("-v")) verbose = true;
				else n = Integer.parseInt(args[i]);
			}

			Options.verbose = verbose;

			if(n > 0) {
				JDDConsole.out.print("Adder\tN=" + n);
				long c1 = System.currentTimeMillis();
				Adder adder = new Adder(n);
				if(dump) adder.dump();

				if(test) {
					// uncomment these lines to test the adder, beware that it is very slow...
					JDDConsole.out.print("Testing...");
					JDDConsole.out.println(adder.test_adder() ? " PASSED" : "FAILED!");
				}


				long c2 = System.currentTimeMillis();
				JDDConsole.out.println("\ttime=" + (c2-c1));

				if(verbose) adder.showStats();
				adder.cleanup();

				return;
			}
		}

		JDDConsole.out.println("Usage: java jdd.examples.Adder [-t] [-d] [-v] <number of bits>");
		JDDConsole.out.println("\t -t    test adder (slow)");
		JDDConsole.out.println("\t -d    dump BDD size");
		JDDConsole.out.println("\t -v    be verbose");
	}
}
