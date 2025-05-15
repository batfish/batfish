
package jdd.examples;


import jdd.bdd.Permutation;
import jdd.bdd.debug.ProfiledBDD2;
import jdd.util.Configuration;
import jdd.util.JDDConsole;
import jdd.util.Options;


/**

This is the solitare.cxx example copied from BuDDy source distribution
convert to Java and JDD:
<p>
<b>Note:</b> you <u>can not</u> run this example with the default JVM parameters
(8 million nodes would require at least 256 MB RAM)!!
<p>
Jorn Lind-Nielsen Included this in BuDDy as  very good pedagogic example on sequential models.
HOWEVER, bear in mind that this is a very INEFFICIENT way of solving a very simple problem.
<p>
from BuDDy README:

<i><pre>
"This example tries to calculate the reachable state space of the
Solitare game. The board is sketched below. At the beginning all
places except number 17 has a pin in them. A pin may be moved by
jumping over another pin into an empty place. The pin in between is
then removed. The goal is to remove all pins execept one - which
should be left in the center.

It took a few hours to run this example on a Alpha machine and it
required some 8000000 BDD nodes."



              ----------------
              | 07 | 14 | 21 |
              ----------------
              | 08 | 15 | 22 |
    ------------------------------------
    | 01 | 04 | 09 | 16 | 23 | 28 | 31 |
    ------------------------------------
    | 02 | 05 | 10 | 17 | 24 | 29 | 32 |
    ------------------------------------
    | 03 | 06 | 11 | 18 | 25 | 30 | 33 |
    ------------------------------------
              | 12 | 19 | 26 |
              ----------------
              | 13 | 20 | 27 |
              ----------------
</pre></i>

*/

public class Solitaire extends ProfiledBDD2 {

	private static final int SIZE = 33;
	private static final int CENTER = 16;

	/** Current state variables */
	private int [] boardC = new int[SIZE];
	private int [] not_boardC = new int[SIZE];

	/** Next state variables */
	private int [] boardN = new int[SIZE];
	private int [] not_boardN = new int[SIZE];

	/** Use to remove the number of states defined by the next-state variables  */
	private double dummyStateNum;


	/** Initial state */
	private int I;

	/** Transition relation */
	private int T;

	/** All current state variables */
	private int currentvar;

	/** Renaming pair */
	Permutation pair;


   /** All the possible moves. Note that the numbering starts from '1' */
	private static final int [][]moves =
	{ {1,4,9}, {1,2,3},
		{2,5,10},
		{3,2,1}, {3,6,11},
		{4,5,6}, {4,9,16},
		{5,10,17},
		{6,5,4}, {6,11,18},
		{7,8,9}, {7,14,21},
		{8,9,10}, {8,15,22},
		{9,8,7}, {9,10,11}, {9,4,1}, {9,16,23},
		{10,9,8}, {10,11,12}, {10,5,2}, {10,17,24},
		{11,10,9}, {11,12,13}, {11,6,3}, {11,18,25},
		{12,11,10}, {12,19,26},
		{13,12,11}, {13,20,27},
		{14,15,16},
		{15,16,17},
		{16,15,14}, {16,17,18}, {16,9,4}, {16,23,28},
		{17,16,15}, {17,18,19}, {17,10,5}, {17,24,29},
		{18,17,16}, {18,19,20}, {18,11,6}, {18,25,30},
		{19,18,17},
		{20,19,18},
		{21,22,23}, {21,14,7},
		{22,23,24}, {22,15,8},
		{23,22,21}, {23,24,25}, {23,16,9}, {23,28,31},
		{24,23,22}, {24,25,26}, {24,17,10}, {24,29,32},
		{25,24,23}, {25,26,27}, {25,18,11}, {25,30,33},
		{26,25,24}, {26,19,12},
		{27,26,25}, {27,20,13},
		{28,29,30}, {28,23,16},
		{29,24,17},
		{30,29,28}, {30,25,18},
		{31,32,33}, {31,28,23},
		{32,29,24},
		{33,32,31}, {33,30,25},
	};



	// -----------------------------------------------------------

	public Solitaire() {
		// super(100000,1000);	// <-- these are the original values

		super(8300000,63000);	// we know what we need, why waste memory?
		Configuration.minFreeNodesProcent = 1; // we are cheating, of course
	}




	// -----------------------------------------------------------
	/** setup the model before using i */
	public void setup() {
		// bdd_setcacheratio(64);
		// bdd_setmaxincrease(500000);

		dummyStateNum = Math.pow(2.0, SIZE);

		make_board();
		make_transition_relation();
		make_initial_state();
	}

	/** Setup the variables needed for the board */
	private void make_board() {
		for(int n = 0; n < SIZE; n++) {
			boardC[n] = createVar();
			not_boardC[n] = ref( not(boardC[n]));
      boardN[n] = createVar();
      not_boardN[n] = ref( not(boardN[n]));
		}
	}


	/** Make the initial state predicate */
	private void make_initial_state() {
		I = 1;
		for(int n = 0; n < SIZE; n++)
			I = andTo(I, (n == CENTER) ? not_boardC[n] : boardC[n] );
		// printSet(I);
	}





	/** Make sure all other places does nothing when there's a move from 'src' to 'dst' over 'tmp' */
	private int all_other_idle(int src, int tmp, int dst) {
		int idle = 1;
		for(int n = 0; n < SIZE; n++) {
   		if(n != src && n != tmp && n != dst) {
				int tmp2 = ref( biimp(boardC[n], boardN[n]) );
				idle = andTo(idle, tmp2);
				deref(tmp2);
			}
		}
   	return idle;
	}



	/** Encode one move from 'src' to 'dst' over 'tmp' */
	private int make_move(int src, int tmp, int dst) {

		// bdd move = boardC[src] & boardC[tmp] & !boardC[dst] & !boardN[src] & !boardN[tmp] & boardN[dst];
		// move &= all_other_idle(src, tmp, dst);

		int tmp1 = ref( and( boardC[src], not_boardN[src]) );
		int tmp2 = ref( and( boardC[tmp], not_boardN[tmp]) );
		int tmp5 = ref( and(tmp1, tmp2) );
		deref(tmp1);
		deref(tmp2);


		int tmp3 = ref( and( boardN[dst], not_boardC[dst]) );
		int tmp4 = all_other_idle(src, tmp, dst);
		int tmp6 = ref( and(tmp3, tmp4) );
		deref(tmp3);
		deref(tmp4);

		int move = ref( and(tmp5, tmp6) );
		deref(tmp5);
		deref(tmp6);

		return move;
	}


	private void make_transition_relation() {

		T = 0;
		for(int n = 0; n < moves.length; n++) {
			int tmp = make_move(moves[n][0]-1, moves[n][1]-1, moves[n][2]-1);
			T = orTo(T, tmp);
			deref(tmp);
		}

		JDDConsole.out.println("Transition relation: " + nodeCount(T) + " nodes, " +
			satCount(T) +" distinct transitions."); // XXX: this differs from orifiganl solitare.cxx!
	}




	/** Make renaming pair and current state variables */
	private void make_itedata() {
		pair = createPermutation(boardN, boardC);

		currentvar = 1;
		for(int n = 0; n < SIZE; n++)  currentvar = andTo(currentvar, boardC[n]);
	}



	/** Do the forward iteration */
	private void iterate() {
		int tmp;
		int reachable = I;
		int cou = 1;

		make_itedata();

	do {
		tmp = reachable;
		int next = ref( relProd(reachable, T, currentvar) );
		int tmp2 = ref( replace(next, pair) );
		deref(next);

		reachable = orTo(reachable, tmp2);
		deref(tmp2);



		JDDConsole.out.println("" + cou + ": " + nodeCount(reachable) + " nodes, " +
					(satCount(reachable) /dummyStateNum) + " states.");
		// showStats(); if(cou == 10) break;
		cou++;

	} while(tmp != reachable);
}

/*
void iterate_front(void)
{
   bdd tmp;
   bdd reachable = I;
   bdd front = reachable;
   int cou = 1;

   make_itedata();

   do
   {
      tmp = reachable;
      bdd next = bdd_appex(front, T, bddop_and, currentvar);
      next = bdd_replace(next, pair);
      front = next - reachable;
      reachable |= front;

      cout << cou << ": " << bdd_nodecount(reachable)
	   << " , " << bdd_satcount(reachable)/dummyStateNum << endl;
      cout << cou << ": " << bdd_nodecount(front)
	   << " , " << bdd_satcount(front)/dummyStateNum << endl;
      cou++;
   }
   while (tmp != reachable);
}
*/

	public static void main(String [] args) {
		Options.verbose = true; // see GC calls??



		long c1 = System.currentTimeMillis();
		Solitaire s = new Solitaire();

		s.setup();
		s.iterate();

		s.showStats();
		long c2 = System.currentTimeMillis();
		JDDConsole.out.println("Time: " + (c2-c1) + " [ms]");
	}
}
