package jdd.examples;


import jdd.bdd.BDD;
import jdd.util.JDDConsole;
import jdd.util.Options;
import jdd.util.math.Digits;

/**
 * The N Queens on a N * N chessboard...
 * <p> we have borrowed this from JavaBDD, to see how much of the [lack of] speed depends
 * on Java...
 *
 * <p> Note that this problem can be solved far faster with a more intelligent use of BDDs.
 */

public class BDDQueens extends BDD implements Queens
{
	private int [] bdds, nbdds;
	private int N, queen;
	private double sols, memory_usage;
	private long time;
	private boolean [] solvec;

	private int X(int x, int y)  { return bdds[ y + x * N]; }
	private int nX(int x, int y)  { return nbdds[ y + x * N]; }


	public BDDQueens(int N) {
		// super(10000, N * N * 64);
		super(1+Math.max(1000, (int) (Math.pow(4.4, N-6))*1000), 10000);

		this.N = N;

		time = System.currentTimeMillis() ;

		int all = N * N;
		bdds = new int[all];
		nbdds = new int[all];
		for(int i = 0; i < all; i++) {
			bdds[i] = createVar();
			nbdds[i] = ref(not(bdds[i]));
		}

		queen = 1;

		for (int i=0 ; i<N ; i++) {
			int e = 0;
			for(int j = 0; j < N; j++)
				e = orTo(e, X(i,j) );
		    queen = andTo(queen, e);
		    deref(e);
		}

		for (int i=0 ; i<N ; i++)
			for(int j = 0; j < N; j++) {
				build(i,j);
				// Test.check(nstack.getTOS() == 0, "in QUEENS: workset stack should be empty");
			}

		sols = satCount(queen);
		time = System.currentTimeMillis()  -time;
		memory_usage = getMemoryUsage();
		if(queen == 0) solvec =  null; // no solutions

		int [] tmp = oneSat(queen, null);
		solvec = new boolean[ tmp.length];
		for(int x = 0; x < solvec.length; x++) solvec[x] = (tmp[x] == 1);
		deref(queen);
		if(Options.verbose) showStats();

		cleanup();
	}

	private void build(int i, int j) {
		int a, b, c, d;
		a = b = c = d = 1;

   		int k,l;

		  /* No one in the same column */
	   for (l=0 ; l<N ; l++)
		 	if (l != j) {
				int mp = ref( imp(X(i,j), nX(i,l)));
				a = andTo(a, mp);
				deref(mp);
			}

		  /* No one in the same row */
		for (k=0 ; k<N ; k++)
			if (k != i) {
				int mp = ref( imp(X(i,j), nX(k,j) ) );
				b = andTo(b, mp);
				deref(mp);
			}

		 /* No one in the same up-right diagonal */
		for (k=0 ; k<N ; k++){
			int ll = k-i+j;
			if (ll>=0 && ll<N)
				if (k != i) {
					int mp = ref( imp(X(i,j), nX(k,ll)) );
					c = andTo(c, mp);
					deref(mp);
				}
		}

		  /* No one in the same down-right diagonal */
		for (k=0 ; k<N ; k++) {
			int ll = i+j-k;
			if (ll>=0 && ll<N)
				if (k != i) {
					int mp = ref( imp(X(i,j), nX(k,ll)) );
					d = andTo(d, mp);
					deref(mp);
				}
		}


        c = andTo(c, d);
        deref(d);
        b = andTo(b,c);
        deref(c);
        a = andTo(a,b);
        deref(b);
		queen = andTo(queen, a);
		deref(a);
	}

	public void showOneSolution() {
		if(solvec == null) return; // no solutions
		for(int x = 0; x < solvec.length; x++) {
			if( (x % N) == 0) JDDConsole.out.printf("\n");
			JDDConsole.out.printf( "%c|", solvec[x] ? '*' : '_');
		}
		JDDConsole.out.printf("\n");
	}

	// ---------------------------------------
	public int getN() { return N; }
	public double numberOfSolutions() { return sols; }
	public long getTime() { return time; }
	public double getMemory() { return memory_usage; }
	public boolean [] getOneSolution() { return solvec;	}

	// -------------------------------------------
	public static void main(String [] args) {
		// Options.verbose = true;
		for(String str :args) {
			final int n = Integer.parseInt(str );
			BDDQueens q = new BDDQueens( n );
			// q.showOneSolution();

			JDDConsole.out.printf("BDD-Queen\tSolutions=%.0f\tN=%d\tmem=%s\ttime=%d\n",
				q.numberOfSolutions(), n, Digits.prettify1024((long) q.getMemory()), q.getTime());
		}
	}
}
