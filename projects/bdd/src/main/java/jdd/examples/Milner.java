
package jdd.examples;


import jdd.bdd.Permutation;
import jdd.bdd.debug.ProfiledBDD2;
import jdd.util.JDDConsole;

/**
 * BDD simulation of N milner cyclers.
 * <p>This program computes the reachable state subset of the Milner scheduler.
 * Check out Andersens lecture notes, where it is explained in detail.
 * <p>
 * The original code from BuDDy also includes a deadlock detection part which
 * I haven't figured out yet (it looks kinda incomplete to me...).
 */
public class Milner extends ProfiledBDD2 {
	private int N; // number of cyclers
	private int [] normvar;
	private int [] primvar;
	private int []c, cp, t, tp, h, hp;
	private int I, T;

	private int normvarset;
	private Permutation pairs;

	// -------------------------------------------
	public Milner(int N) {
		super(100000, 30000);

		this.N = N;
		this.normvar = new int[N * 3];
		this.primvar = new int[N * 3];
		this.c = new int[N];
		this.cp= new int[N];
		this.h = new int[N];
		this.hp= new int[N];
		this.t = new int[N];
		this.tp= new int[N];

		for(int n = 0; n < N * 3; n++) {
			normvar[n] = createVar();
			primvar[n] = createVar();
		}
		pairs = createPermutation(primvar, normvar);
		// pairs = createPermutation(normvar, primvar);

		normvarset = 1;
		for(int i = 0; i < normvar.length; i++) normvarset = andTo(normvarset, normvar[i] );

		for(int n = 0; n < N; n++) {
			c[n] = normvar[n * 3];
			t[n] = normvar[n * 3 + 1];
			h[n] = normvar[n * 3 + 2];

			cp[n] = primvar[n * 3];
			tp[n] = primvar[n * 3 + 1];
			hp[n] = primvar[n * 3 + 2];
		}

		I = initial_state(t,h,c);
		T = transitions(t,tp,h,hp,c,cp);
 }



	// ---------------------------------------------------

	private int andA(int res, int []x, int []y, int z) {
		for(int i = 0; i < N; i++) {
			if(i != z) {
				int tmp1 = ref( biimp(x[i], y[i]) );
				res = andTo(res, tmp1);
				deref(tmp1);
			}
		}
		return res;
	}

	private int diff(int bdd1, int bdd2) {
		int tmp = ref( not(bdd2) );
		int ret = and(tmp, bdd1) ;
		deref(tmp);
		return ret;
	}

	private int transitions( int []t, int []tp, int []h, int []hp, int []c, int []cp) {
		int tran = 0;
		for(int i = 0; i < N; i++) {

			// P = ((c[i]>cp[i]) & (tp[i]>t[i]) & hp[i] & A(c,cp,i) & A(t,tp,i) & A(h,hp,i))
			// 	| ((h[i]>hp[i]) & cp[(i+1)%N] & A(c,cp,(i+1)%N) & A(h,hp,i)& A(t,tp,N));
			int tmp1 = ref( diff(c[i], cp[i]) );
			int tmp2 = ref( diff(tp[i], t[i]) );
			tmp1 = andTo(tmp1, tmp2);
			deref(tmp2);

			tmp1 = andTo(tmp1, hp[i]);
			tmp1 = andA(tmp1, c, cp, i);
			tmp1 = andA(tmp1, t, tp, i);
			tmp1 = andA(tmp1, h, hp, i);
			int P = tmp1;

			tmp1 = ref( diff(h[i], hp[i]) );
			tmp1 = andTo(tmp1, cp[(i+1) % N]);
			tmp1 = andA(tmp1, c, cp, (i+1) % N);
			tmp1 = andA(tmp1, h, hp, i);
			tmp1 = andA(tmp1, t, tp, N);

			P = orTo(P, tmp1);
			deref(tmp1);


			// E = t[i] & !tp[i] & A(t,tp,i) & A(h,hp,N) & A(c,cp,N);
			tmp1 = ref( not(tp[i]) );
			tmp1 = andTo(tmp1, t[i]);
			tmp1 = andA(tmp1, t, tp, i);
			tmp1 = andA(tmp1, h, hp, N);
			int E = andA(tmp1, c, cp, N);

			// T |= P | E;
			tmp2 = ref( or(P,E) );
			deref(P);
			deref(E);

			tran = orTo(tran, tmp2);
			deref(tmp2);
		}

		return tran;
	}


	private int initial_state(int []t, int []h, int []c) {
		// bdd I = c[0] & !h[0] & !t[0];
		// for(i=1; i<N; i++) I &= !c[i] & !h[i] & !t[i];

		I = 1;
		for(int i = 0; i < N; i++) {
			int tmp1 = ref( (i == 0) ? c[i] : not( c[i] ) );
			tmp1 = andTo( tmp1, not( h[i] ) );
			tmp1 = andTo( tmp1, not( t[i] ) );

			I = andTo(I, tmp1);
			deref(tmp1);
		}
		return I;
	}

	// -----------------------------------------------------

	public int reachable_states() {
		int by, bx = 0;
		do {
			by = bx;
			int tmp1 = ref( relProd(bx, T, normvarset) );
			deref(bx);

			int C = ref( replace(tmp1, pairs) );
			deref(tmp1);

			bx = orTo( C, I );
		} while(by != bx);

		return bx;
	}

	public static void main(String [] args) {
		// Options.verbose = true; // <-- if you like insane garbage collection messages...
		// Options.profile_cache = true; // <-- to see cache profiling

		if(args.length >= 1) {
			// parse command line
			int n = -1;
			boolean verbose = false;
			for(int i = 0; i < args.length; i++){
				if(args[i].equals("-v")) verbose = true;
				else n = Integer.parseInt(args[i]);
			}


			if(n > 0) {
				long c1 = System.currentTimeMillis();
				Milner milner = new Milner(n);
				int R = milner.reachable_states();
				long c2 = System.currentTimeMillis();

				if(verbose) {
					milner.showStats();
					JDDConsole.out.println("Simulation of " + n + " milner cyclers");
					JDDConsole.out.println("SatCount(R) = " + milner.satCount(R) );
					JDDConsole.out.println("Calc        = " + (n * Math.pow(2, 1 + n) * Math.pow(2, 3 * n)) );
					JDDConsole.out.println("Time: " + (c2-c1) + " [ms]");
				} else {
					JDDConsole.out.println("Milner\tN=" + n + "\ttime="+ (c2-c1));
				}

				milner.cleanup();
				return;
			}
		}

		JDDConsole.out.println("Usage: java jdd.examples.Milner [-v] <number of cyclers>");
	}
}
