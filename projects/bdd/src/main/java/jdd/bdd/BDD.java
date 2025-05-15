
// TODO: if the number of variables is changed, do we need to clear the sat_cache??
// TODO: why are not we partially cleaning the "relprod_cache"?

package jdd.bdd;

import jdd.util.NodeName;
import jdd.util.Configuration;
import jdd.util.Allocator;
import jdd.util.Test;
import jdd.util.Array;
import jdd.util.math.Prime;

import java.util.BitSet;

/**
 * BDD main class. All BDD code uses this.
 * <p>This is not a "bdd tree" or "bdd node", but the Java objects that handles _ALL_ BDD operations
 * for a given universe.
 *
 * @see NodeTable
 * @see jdd.util.Configuration
 */


public class BDD extends NodeTable {

	/** for the OP cache */
	protected static final int CACHE_AND = 0, CACHE_OR = 1, CACHE_XOR = 2, CACHE_BIIMP = 3;
	protected static final int CACHE_IMP = 4,	CACHE_NAND = 5, CACHE_NOR = 6, CACHE_RESTRICT = 7;

	/** for the quant cache */
	protected static final int CACHE_EXISTS = 0,  CACHE_FORALL = 1;

	// /** yet unused */
	// protected static final int CACHE_SIMPLIFY = 1;


	protected int num_vars, last_sat_vars;
	protected SimpleCache op_cache, relprod_cache, not_cache, ite_cache, quant_cache;
	protected SimpleCache replace_cache;
	protected DoubleCache sat_cache;

	// quantification stuff
	protected boolean [] varset_vec;	// used internally by quant/relprod and some other functions
	protected boolean [] sign_vec;	// used internally by restrict functions for polarity
	protected int [] oneSat_buffer; // used internally by oneSat
	private boolean [] support_buffer; // used by support()
	protected int varset_last, quant_id, quant_cube, restrict_careset;
	protected boolean quant_conj;

	protected NodeName nodeNames = new BDDNames();
	private Permutation firstPermutation; /** permutations are gathered in a linked list. this is to avoid having multiple objects for same permutation. */


	/**
	 * create a BDD manager with initially <tt>nodesize</tt> nodes.
	 */
	public BDD(int nodesize) {
		this(nodesize, Configuration.DEFAULT_BDD_CACHE_SIZE);
	}

	/**
	 * create a BDD manager with initially <tt>nodesize</tt> nodes
	 * and <tt>cache_size</tt> cache elements.
	 */
	public BDD(int nodesize, int cache_size) {
		super(Prime.prevPrime(nodesize));

		// MAY GROW. (bdd1, bdd2, op) => bdd
		op_cache  = new OptimizedCache("OP", cache_size / Configuration.bddOpcacheDiv, 3, 2);

		// MAY GROW. bdd => bdd
		not_cache = new OptimizedCache("NOT", cache_size / Configuration.bddNegcacheDiv, 1, 1);

		// MAY GROW. (bdd1, bdd2, bdd3) => bdd
		ite_cache = new OptimizedCache("ITE", cache_size / Configuration.bddItecacheDiv, 3, 3);

		// MAY GROW. (bdd1, bdd2, quant-type) => bdd. we could join this with op_cache?
		quant_cache = new OptimizedCache("QUANT", cache_size / Configuration.bddQuantcacheDiv, 3, 2);

		// MAY GROW. rel-prod. (bdd1, bdd2, bdd3) => bdd
		relprod_cache = new OptimizedCache("REL-PROD", cache_size / Configuration.bddRelprodcacheDiv, 3, 3);

		// MAY GROW. (bdd1, perm-id) => bdd
		replace_cache = new OptimizedCache("REPLACE", cache_size / Configuration.bddReplacecacheDiv , 2, 1);

		// WONT GROW. BDD => double
		sat_cache = new DoubleCache("SAT", cache_size / Configuration.bddSatcountDiv);

		num_vars = 0;
		last_sat_vars = -1; // not assigned yet
		varset_last = -1; // invalid
		varset_vec = Allocator.allocateBooleanArray(24); // 24 is just a default number
		sign_vec = Allocator.allocateBooleanArray(varset_vec.length); // same length!
		support_buffer = new boolean[24]; // dito

		firstPermutation = null; // nothing yet

		// yes, we know how deep our trees are and will call tree_depth_changed()!
		enableStackMarking();
	}


	// ---------------------------------------------------------------
	/** this function is called at exit, cleans up and frees allocated memory */
	public void cleanup() {
		super.cleanup();
		sign_vec = varset_vec = null;
		oneSat_buffer = null;
		quant_cache = null;
		ite_cache = null;
		not_cache  = null;
		op_cache = null;
		replace_cache = null;
		relprod_cache = null;
		sat_cache = null;
	}

	// ---------------------------------------------------------------


	/** the symbolic ONE */
	public final int getOne() { return 1; }

	/** the symbolic ZERO */
	public final int getZero() { return 0; }

	// ---------------------------------------------------------------

	/** how many BDD variables do we have ?? */
	public int numberOfVariables() { return num_vars; }

	/** create a new BDD variable */
	public int createVar() {
		int var = nstack.push( mk(num_vars, 0, 1) );
		int nvar = mk(num_vars, 1, 0);
		nstack.pop();
		num_vars++;

		saturate(var);
		saturate(nvar);

		not_cache.add(var, nvar);
		not_cache.add(nvar, var);


		// we want to keep the work stack at least so large
		nstack.grow(6 * num_vars + 1);

		if(varset_vec.length < num_vars) {
			varset_vec = Allocator.allocateBooleanArray(num_vars * 3);
			sign_vec = Allocator.allocateBooleanArray(num_vars * 3); // same size!
		}


		if(support_buffer.length < num_vars)
			support_buffer = new boolean[num_vars * 3];

		tree_depth_changed(num_vars); // MUST be called

		// we used to have -1 there, but "num_vars" will make life easier in satCount() etc.
		setAll(0, num_vars, 0, 0);
		setAll(1, num_vars, 1, 1);

		return var;
	}

	/** helper function to create multiple variables */
	public int [] createVars(int n) {
		int [] ret = new int[n];
		for(int i = 0; i < n; i++) {
			ret[i] = createVar();
		}
		return ret;
	}

	/**
	 * this function is called after the garbage collector hash changed some internal data.
	 * when we get here, data structures such as caches are instable, so we have to clean up
	 * after the garbage collector.
	 */
	protected void post_removal_callbak() {
		// NOTE: dont do any advanced stuffs here, HashTable is invalid at this point!!

		// NOTE: we cant use partial invalidation for some cahce types since they hold
		//       more than just BDD nodes [I think quant_cache is fixed, however]

		sat_cache.invalidate_cache(); // NO NEED TO GROW THIS ONE ?
		relprod_cache.free_or_grow(this);
		replace_cache.free_or_grow(this);
		quant_cache.free_or_grow(this);
		ite_cache.free_or_grow(this);
		not_cache.free_or_grow(this);
		op_cache.free_or_grow(this);
	}


	/** mk operator as defined in Andersens lecture notes */
	public int mk(int i, int l, int h) {
		if(l == h) return l;
		return add(i,l,h);
	}


	// ---------------------------------------------------------------------
	// note: in contrary to ZDD cubes, these cubes start with LSB variable (x1) first
	// Note 2: i think we just modified cube(), so the statement above wont hold anymore

	/**
	 * create a bdd <i>cube</i>, which is simply a conjunction of a set of variables.
	 * <p>given a list of variables <i>v[]</i>, it vill return <i>v[0] AND v[1] AND ...</i>.
	 * this cube is then used in operations such as exists and forall
	 * @see #exists
	 * @see #forall
	 */
	public final int cube(boolean [] v) {
		int last = 1, len = Math.min(v.length, num_vars);
		for(int i = 0; i < len; i++) {
			int var = len - i - 1;
			nstack.push(last);
			if(v[var]) last = mk(var, 0, last);
			nstack.pop();
		}
		return last;
	}

	/**
	 * a cube over a set of variables represented as a mintem. for example the string "11-1"
	 * over four variables will return the cube <tt>v1 AND v3 AND v4</tt>.
	 * @see #cube
	 */
	public final int cube(String s) {
		int len = s.length(), last = 1;
		for(int i = 0; i < len;i++) {
			int var = len - i - 1;
			nstack.push(last);
			if(s.charAt(var) == '1') last = mk(var, 0, last);
			nstack.pop();
		}
		return last;
	}

	/**
	 * returns an unary minterm based on a vector of boolean assignments.
	 * <p>for example <i>minterm([true, false]) </i> will return <i>NOT v1 and v2</i>.
	 * @see #minterm
	 */
	public final int minterm(boolean [] v) {
		int last = 1, len = Math.min(v.length, num_vars);
		for(int i = 0; i < len; i++) {
			int var = len - i - 1;
			nstack.push(last);
			last = (v[var] ? mk(var, 0, last) : mk(var, last, 0));
			nstack.pop();
		}
		return last;
	}


	/**
	 * <p>returns a unary minterm based on a vector of boolean assignments.
	 * for example <i>minterm([true, false]) </i> will return <i>NOT v1 and v2</i>.
	 * @see #minterm
	 */

	public final int minterm(String s) {
		int len = s.length(), last = 1;
		for(int i = 0; i < len;i++) {
			int var = len - i - 1;
			nstack.push(last);
			last = ((s.charAt(var) == '1') ? mk(var, 0, last) :
				( (s.charAt(var) == '0') ? mk(var, last, 0) : last));
			nstack.pop();
		}
		return last;
	}

	// ---------------------------------------------------------------------

	/**
	 * this is the <tt>If-Then-Else</tt> BDD function.
	 * <p>it can be used to (inefficiently) simulate and binary operation.
	 * (i think it is described in the "Long" paper).
	 *
	 * @return <tt>(f AND then_) OR (NOT f AND else_)</tt>
	 */

	public int ite(int f, int then_, int else_ ) {
		if(f == 0) return else_;
		if(f == 1) return then_;

		if((getLow(f) < 2 && getHigh(f) < 2) && (getVar(f) < getVar(then_)) && (getVar(f) < getVar(else_))) {
			if(getLow(f) == 0) return mk(getVar(f), else_, then_); 	// f is a variable
			if(getLow(f) == 1) mk(getVar(f), then_, else_ ); 	// f is inverse of a variable
			// XXX: even if f.var is larger/equal to then_.var or else_.var, we can
			//      optimize this by calling COFACTOR and then OR...
		}
		return ite_rec(f, then_, else_);
	}

	/**
	  * internal ITE recursive function.
	  * note that are ITE terminal cases differs from BuDDy since our 0/1 constant variables differs
	  */
	private final int ite_rec(int f, int g, int h) {
		if(f == 1) return g;
		if(f == 0) return h;
		if(g == h) return g;
		if(g == 1 && h == 0) return f;
		if(g == 0 && h == 1) return not_rec(f);

		if(g == 1) return or_rec(f,h);
		if(g == 0) {
			int tmp = nstack.push(not_rec(h));
			tmp = nor_rec(f,tmp);
			nstack.pop();
			return tmp;
		}

		if(h == 0) return and_rec(f,g);
		if(h == 1) {
			int tmp = nstack.push( not_rec(g) );
			tmp = nand_rec(f,tmp);
			nstack.pop();
			return tmp;
		}

		if(ite_cache.lookup(f,g,h)) return ite_cache.answer;
		int hash = ite_cache.hash_value;

		int v = Math.min(getVar(f), Math.min(getVar(g), getVar(h)));
		int l = nstack.push( ite_rec(
				(v == getVar(f)) ? getLow(f) : f, (v == getVar(g)) ? getLow(g) : g, (v == getVar(h)) ? getLow(h) : h));

		int H = nstack.push(ite_rec(
				(v == getVar(f)) ? getHigh(f) : f, (v == getVar(g)) ? getHigh(g) : g, (v == getVar(h)) ? getHigh(h) : h) );

		l = mk(v,l,H);
		nstack.drop(2);

		ite_cache.insert(hash, f,g,h, l);
		return l;
	}

	// ---------------------------------------------------------------------
	/**
	 * binary AND.
	 * @see #or
	 * @return u1 AND u2
	 */
	public int and(int u1, int u2) {
		nstack.push(u1);
		nstack.push(u2);
		int ret = and_rec(u1,u2);
		nstack.drop(2);
		return ret;

	}
	private final int and_rec(int u1, int u2) {
		// terminal cases
		if(u1 == u2 || u2 == 1) return u1;
		if(u1 == 0 || u2 == 0) return 0;
		if(u1 == 1) return u2;

		int l, h, v = getVar(u1);
		if(v > getVar(u2)) {v = u1; u1 = u2; u2 = v; v = getVar(u1);	}

		if(op_cache.lookup(u1, u2, CACHE_AND)) return op_cache.answer;
		int hash = op_cache.hash_value;

		if( v == getVar(u2)) {
			l = nstack.push(and_rec(getLow(u1), getLow(u2)));
			h = nstack.push(and_rec(getHigh(u1), getHigh(u2)));
		} else { // v < getVar(u2)
			l = nstack.push( and_rec(getLow(u1), u2));
			h = nstack.push( and_rec(getHigh(u1), u2));
		}

		if(l != h) l = mk(v,l,h);
		nstack.drop(2);

		op_cache.insert(hash, u1, u2, CACHE_AND, l);
		return l;
	}


	// ---------------------------------------------------------------------
	/**
	 * binary NAND.
	 * @see #nor
	 * @return u1 NAND u2
	 */
	public int nand(int u1, int u2) {
		nstack.push( u1);
		nstack.push( u2);
		int ret = nand_rec(u1,u2);
		nstack.drop(2);
		return ret;
	}

	private final int nand_rec(int u1, int u2) {
		if(u1 == 0 || u2 == 0) return 1;
		if(u1 == 1 || u1 == u2) return not_rec(u2); // arash: early term
		if(u2 == 1) return not_rec(u1);

		int l, h, v = getVar(u1);
		if(v > getVar(u2)) {v = u1; u1 = u2; u2 = v; v = getVar(u1);}

		if( op_cache.lookup(u1, u2, CACHE_NAND) ) return op_cache.answer;
		int hash = op_cache.hash_value;

		if( v == getVar(u2)) {
			l = nstack.push( nand_rec(getLow(u1), getLow(u2)));
			h = nstack.push( nand_rec(getHigh(u1), getHigh(u2)));
		} else { // v < getVar(u2)
			l = nstack.push( nand_rec(getLow(u1), u2) );
			h = nstack.push( nand_rec(getHigh(u1), u2));
		}

		if(l != h) l = mk(v,l,h);
		op_cache.insert(hash, u1, u2, CACHE_NAND, l);
		nstack.drop(2);
		return l;

	}

	// ---------------------------------------------------------------------
	/**
	 * binary OR.
	 * @see #and
	 * @return u1 OR u2
	 */
	public int or(int u1, int u2) {
		nstack.push( u1);
		nstack.push( u2);
		int ret = or_rec(u1,u2);
		nstack.drop(2);
		return ret;

	}
	private final int or_rec(int u1, int u2) {
		if(u1 == 1 || u2 == 1) return 1;
		if(u1 == 0 || u1 == u2) return u2;
		if(u2 == 0) return u1;


		int l, h, v = getVar(u1);
		if(v > getVar(u2)) {v = u1; u1 = u2; u2 = v; v = getVar(u1);}

		if( op_cache.lookup(u1, u2, CACHE_OR)) return op_cache.answer;
		int hash = op_cache.hash_value;

		if( v == getVar(u2)) {
			l = nstack.push( or_rec(getLow(u1), getLow(u2)));
			h = nstack.push( or_rec(getHigh(u1), getHigh(u2)));
		} else { // v < getVar(u2)
			l = nstack.push( or_rec(getLow(u1), u2));
			h = nstack.push( or_rec(getHigh(u1), u2));
		}

		if(l != h) l = mk(v,l,h);
		op_cache.insert(hash, u1, u2, CACHE_OR, l);
		nstack.drop(2);
		return l;

	}
	// ---------------------------------------------------------------------
	/**
	 * binary NOR.
	 * @see #nand
	 * @return u1 NOR u2
	 */
	public int nor(int u1, int u2) {
		nstack.push( u1);
		nstack.push( u2);
		int ret = nor_rec(u1,u2);
		nstack.drop(2);
		return ret;

	}
	private final int nor_rec(int u1, int u2) {
		// terminal cases
		if(u1 == 1 || u2 == 1) return 0;
		if(u1 == 0 || u1 == u2) return not_rec(u2);	 // ARASH: testing early termination if u1 = u2
		if(u2 == 0) return not_rec(u1);

		int l, h, v = getVar(u1);
		if(v > getVar(u2)) {v = u1; u1 = u2; u2 = v; v = getVar(u1);}


		if(op_cache.lookup(u1, u2, CACHE_NOR) ) return op_cache.answer;
		int hash = op_cache.hash_value;

		if( v == getVar(u2)) {
			l = nstack.push( nor_rec(getLow(u1), getLow(u2)));
			h = nstack.push( nor_rec(getHigh(u1), getHigh(u2)));
		} else { // v < getVar(u2)
			l = nstack.push( nor_rec(getLow(u1), u2));
			h = nstack.push( nor_rec(getHigh(u1), u2));
		}

		if(l != h) l = mk(v,l,h);
		op_cache.insert(hash, u1, u2, CACHE_NOR, l);
		nstack.drop(2);

		return l;
	}


	// ---------------------------------------------------------------------
	/**
	 * binary XOR.
	 * @see #biimp
	 * @return u1 XOR u2
	 */
	public int xor(int u1, int u2) {
		nstack.push( u1);
		nstack.push( u2);
		int ret = xor_rec(u1,u2);
		nstack.drop(2);
		return ret;

	}
	private final int xor_rec(int u1, int u2) {
		// terminal cases

		if(u1 == u2) return 0;
		if(u1 == 0) return u2;
		if(u2 == 0) return u1;
		if(u1 == 1) return not_rec(u2);
		if(u2 == 1) return not_rec(u1);


		int l, h, v = getVar(u1);
		if(v > getVar(u2)) {v = u1; u1 = u2; u2 = v; v = getVar(u1);}

		if(op_cache.lookup(u1, u2, CACHE_XOR)) return op_cache.answer;
		int hash = op_cache.hash_value;

		if( v == getVar(u2)) {
			l = nstack.push( xor_rec(getLow(u1), getLow(u2)));
			h = nstack.push( xor_rec(getHigh(u1), getHigh(u2)));
		} else { // v < getVar(u2)
			l = nstack.push( xor_rec(getLow(u1), u2));
			h = nstack.push( xor_rec(getHigh(u1), u2));
		}

		if(l != h) l = mk(v,l,h);
		op_cache.insert(hash, u1, u2, CACHE_XOR, l);
		nstack.drop(2);
		return l;
	}

	// ---------------------------------------------------------------------
	/**
	 * binary BI-IMPLICATION (double implication, equivalence, whatever...).
	 * @see #xor
	 * @return (u1 <--> u2)
	 */
	public int biimp(int u1, int u2) {
		nstack.push( u1);
		nstack.push( u2);
		int ret = biimp_rec(u1,u2);
		nstack.drop(2);
		return ret;
	}

	private final int biimp_rec(int u1, int u2) {
		// terminal cases
		if(u1 == u2) return 1;
		if(u1 == 0) return not_rec(u2);
		if(u1 == 1) return u2;
		if(u2 == 0) return not_rec(u1);
		if(u2 == 1) return u1;


		int l, h, v = getVar(u1);
		if(v > getVar(u2)) {v = u1; u1 = u2; u2 = v; v = getVar(u1);}

		if(op_cache.lookup(u1, u2, CACHE_BIIMP)) return op_cache.answer;
		int hash = op_cache.hash_value;

		if( v == getVar(u2)) {
			l = nstack.push( biimp_rec(getLow(u1), getLow(u2)));
			h = nstack.push( biimp_rec(getHigh(u1), getHigh(u2)));
		} else { // v < getVar(u2)
			l = nstack.push( biimp_rec(getLow(u1), u2));
			h = nstack.push( biimp_rec(getHigh(u1), u2));
		}

		if(l != h) l = mk(v,l,h);
		op_cache.insert(hash, u1, u2, CACHE_BIIMP, l);
		nstack.drop(2);
		return l;
	}

	// ---------------------------------------------------------------------
	/**
	 * binary implication.
	 * @return u1 -> u2
	 */
	public int imp(int u1, int u2) {
		nstack.push( u1);
		nstack.push( u2);
		int ret = imp_rec(u1,u2);
		nstack.drop(2);
		return ret;

	}

	private final int imp_rec(int u1, int u2) {
		// terminal cases
		if(u1 == 0 || u2 == 1 || u1 == u2) return 1;
		if(u1 == 1) return u2;
		if(u2 == 0) return not_rec(u1);


		if(op_cache.lookup(u1, u2, CACHE_IMP)) return op_cache.answer;
		int hash = op_cache.hash_value;

		int l, h, v = getVar(u1);
		if( getVar(u1) == getVar(u2)) {
			l = nstack.push( imp_rec(getLow(u1), getLow(u2)));
			h = nstack.push( imp_rec(getHigh(u1), getHigh(u2)));
		} else if (/* (u2 == 0) || */ getVar(u1) < getVar(u2)) {
			l = nstack.push( imp_rec(getLow(u1), u2));
			h = nstack.push( imp_rec(getHigh(u1), u2));
		} else  {
			l = nstack.push( imp_rec(u1, getLow(u2)));
			h = nstack.push( imp_rec(u1, getHigh(u2)));
			v = getVar(u2);
		}
		if(l != h) l = mk(v,l,h);
		op_cache.insert(hash, u1, u2, CACHE_IMP, l);
		nstack.drop(2);

		return l;
	}

	// -----------------------------------------------------
	/**
	 * binary NOT.
	 * @return NOT u1
	 */

	public int not(int u1) {
		nstack.push( u1);
		int ret = not_rec(u1);
		nstack.pop();
		return ret;
	}

	private final int not_rec(int bdd) {
		if(bdd < 2) return (bdd ^ 1);// 1-->0 and 0-->1

		if( not_cache.lookup(bdd)) return not_cache.answer;
		int hash = not_cache.hash_value;

		int l = nstack.push( not_rec(getLow(bdd)));
		int h = nstack.push( not_rec(getHigh(bdd)));
		if(l != h)  l = mk( getVar(bdd), l, h);
		nstack.drop(2);

		not_cache.insert(hash, bdd, l);
		return l;
	}


	// ---- [ sets of variables represented as a BDD tree ] -----------------------------------------

	/** this prepares the varset_vec s.t. if it is true, then it is quantified */
	private void varset(int bdd) {
		Test.check(bdd > 1, "BAD varset");

		// for(int i = 0; i < num_vars; i++) varset_vec[i] = false;
		for(int i = num_vars; i != 0; ) varset_vec[--i] = false;

		while( bdd > 1) {
			// this assume that BDD variables grow from root. otherwise, varset_last will be incorrect
			varset_vec[ varset_last = getVar(bdd) ] = true;
			bdd = getHigh(bdd);
		}
	}

	/** same as varset, but this time bdd is not a positive cube. eg: v1 & ~v2, sogn_vec hold the sign */
	private void varset_signed(int bdd) {
		Test.check(bdd > 1, "BAD varset");

		for(int i = 0; i < num_vars; i++) varset_vec[i] = false;
		while( bdd > 1) {
			// XXX: this assumes correct order on varset_last, does not apply to Z-BDDs for example!
			varset_last = getVar(bdd);
			varset_vec[varset_last] = true;
			sign_vec[varset_last] = (getLow(bdd) == 0);
			bdd = getHigh(bdd);

		}
   }

	// ---- [ quantification ] -------------------------------------------------------
	/**
	 * binary EXISTS, existential quantification.
	 * <p>Let <i>exists(bdd, variable)</i> to be
	 * <i>bdd(variable) OR bdd(NOT variable)</i>. Then <i>exists(bdd, cube)</i>
	 * does this for every (combination of) variable in the cube.
	 * <p>Note that all references of <i>variable</i> and the variables in <i>cube</i>
	 * will be removed from the resulting bdd.
	 *
	 * @see #forall
	 */

	public int exists(int bdd, int cube) {
		if(cube == 1) return bdd;
		Test.check(cube != 0, "Empty cube");
		quant_conj = false;
		quant_id = CACHE_EXISTS;
		quant_cube = cube;

		varset(cube);
		return quant_rec(bdd);
	}
	// ----------------------------------------------------------------
	/**
	 * binary FOR-ALL, universal quantification.
	 * <p>Let <i>forall(bdd, variable)</i> to be
	 * <i>bdd(variable) AND bdd(NOT variable)</i>. Then <i>foral(bdd, cube)</i>
	 * does this for every (combination of) variable in the cube.
	 * <p>Note that all references of <i>variable</i> and the variables in <i>cube</i>
	 * will be removed from the resulting bdd.
	 *
	 * @see #exists
	 */
	public int forall(int bdd, int cube) {
		if(cube == 1) return bdd;
		Test.check(cube != 0, "Empty cube");
		quant_conj = true;
		quant_id = CACHE_FORALL;
		quant_cube = cube;

		varset(cube);
		return quant_rec(bdd);
	}


	/** internal quantificator, optimized and unreadable :) */
	private final int quant_rec(int bdd) {
		int var = getVar(bdd);
		if(bdd < 2 || var > varset_last) return bdd;

		if(quant_cache.lookup(bdd, quant_cube, quant_id)) return quant_cache.answer;
		int hash = quant_cache.hash_value;

		int l = 0;
		if(varset_vec[ var ])  {

			l = getLow(bdd);
			int h = getHigh(bdd);



			// we want the one closes to a terminal as l, so we can get an terminal answer earlier:
			if(getVar(h)  > getVar(l)) { l = h; h = getLow(bdd); } // SWAP l and h

			l = quant_rec( l);

			// check for early termination
			if((quant_conj == true && l == 0) || (quant_conj == false && l == 1)) {

				// yes!
				// "l" is our result, no need to compute more
				// 0 AND x = 0, 1 OR x = 1
			} else {
				nstack.push( l); // SAVE l
				h = nstack.push( quant_rec( h));
				l = quant_conj ? and_rec(l,h) : or_rec(l,h);
				nstack.drop(2);
			}
		} else {
			l = nstack.push( quant_rec( getLow(bdd) ));
			int h = nstack.push( quant_rec( getHigh(bdd) ));
			l = mk( var, l, h);
			nstack.drop(2);
		}

		quant_cache.insert(hash, bdd, quant_cube, quant_id, l );
		return l;
	}

	// ----[ relational product ] ------------------------------------
	/**
	 * This is the relational-product of <i>Clarke et al.</i>.
	 *
	 * <p>It combines a conjunction and existential quantification that is often
	 * seen in image computation.
	 * Use this operation for better runtime performance!
	 */
	public int relProd(int u1, int u2, int c) {

		/* this code should equal to:
		int tmp = ref( and(u1,u2));
		int ret = exists(tmp, c);
		deref(tmp);
		return ret;
		*/

		if(c < 2) return and_rec(u1, u2);

		varset(c);

		// for the internal quant_rec call
		quant_conj = false;
		quant_id = CACHE_EXISTS;	// for cache lookup in quant_rec()
		quant_cube = c;
		return relProd_rec(u1, u2);
	}

	private final int relProd_rec(int u1, int u2) {
		if(u1 == 0 || u2 == 0) return 0;
		if(u1 == u2 || u2 == 1) return quant_rec(u1);
		if(u1 == 1) return quant_rec(u2);

		if(getVar(u1) > varset_last && getVar(u2) > varset_last)  {
			return and_rec(u1, u2);
		}

		if(getVar(u2) < getVar(u1)) { int tmp = u1; u1 = u2; u2 = tmp; } // SWAP

		if(relprod_cache.lookup(u1, u2, quant_cube)) return relprod_cache.answer;
		int hash = relprod_cache.hash_value;

		int l,h, v = getVar(u1);
		l = relProd_rec(getLow(u1), (v == getVar(u2)) ? getLow(u2)  : u2);


		// early termination, see Yangs thesis...
		if(varset_vec[ v ]) {
			if(l == 1) return l;
			if(l == getHigh(u1)) return l;
			if(l == getHigh(u2) && getVar(u2) == v) return l;
		}

		nstack.push(l);
		h = nstack.push( relProd_rec( getHigh(u1), (v == getVar(u2)) ? getHigh(u2) : u2));

		if(l != h) {
			if(varset_vec[ v ]) l = or_rec(l, h);
			else l = mk(v, l ,h);
		}

		relprod_cache.insert(hash, u1, u2, quant_cube, l );
		nstack.drop(2);
		return l;
	}

	// ----- [ permutaion ] ----------------------
	private int [] perm_vec;
	private int perm_last, perm_var, perm_id;

	/**
	 * create a Permutation vector for a given variable permutation.
	 * <p>A permutation is used by <tt>replace</tt> to re-label nodes in a BDD.
	 *
	 * <p><b>NOTE:</b> the from and to-cubes must not overlap!
	 * @see Permutation
	 * @see #replace
	 */
	public Permutation createPermutation( int [] cube_from, int [] cube_to) {
		// since the unique IDs is used in the cache, we don't want same permutation to be
		// represented by different Permutation objects and hence different IDs.

		Permutation perm = Permutation.findPermutation(firstPermutation,cube_from, cube_to);
		if(perm != null) {
			// the permutation already exists. use it!
		} else {
			// create a new one
			perm = new Permutation( cube_from, cube_to, this);

			// insert it into the permutation list
			perm.next = firstPermutation;
			firstPermutation = perm;
		}
		return perm;
	}

	/**
	 * Given a permutation, this function will re-label (some) nodes in a BDD
	 * to associate with a new sets of variables.
	 *
	 * @see Permutation
	 * @see #createPermutation
	 */
	public int replace(int bdd, Permutation perm) {
		perm_vec = perm.perm;
		perm_last = perm.last;
		perm_id   = perm.id;
		int ret = replace_rec( bdd );
		perm_vec = null; // help GC ?
		return ret;
	}

	/**
	 * recursive part of replace.
	 * <p> uses the variables perm_vec, perm_id and perm_last and sets perm_var
	 */
	private final int replace_rec(int bdd) {
		if(bdd < 2 || getVar(bdd) > perm_last)	return bdd;


		if(replace_cache.lookup(bdd, perm_id)) return replace_cache.answer;
		int hash = replace_cache.hash_value;

		int l = nstack.push( replace_rec( getLow(bdd)));
		int h = nstack.push( replace_rec( getHigh(bdd)));

		perm_var = perm_vec[ getVar(bdd) ];
		l = mkAndOrder(l,h);

		nstack.drop(2);
		replace_cache.insert(hash, bdd, perm_id, l );
		return l;
	}

	/** mk but with possible bad order between var and l or h. uses the variable perm_var */
	private final int mkAndOrder(int l, int h) {

		// XXX: do we need a cache here??
		// XXX: probably not if we use a S->S' permutation, but otherwise....

		int vl = getVar(l);
		int vh = getVar(h);
		if(perm_var < vl && perm_var < vh) return mk(perm_var, l, h);

		Test.check(perm_var != vl && perm_var != vh, "Replacing to a variable already in the BDD");


		int x, y, v = vl;
		if( vl == vh) {
			x = nstack.push( mkAndOrder( getLow(l), getLow(h) ));
			y = nstack.push( mkAndOrder( getHigh(l), getHigh(h) ));
		} else if( vl < vh) {
			x = nstack.push( mkAndOrder( getLow(l), h ));
			y = nstack.push( mkAndOrder( getHigh(l), h ));
		} else { // if( getVar(l) > getVar(h))
			x = nstack.push( mkAndOrder( l, getLow(h) ));
			y = nstack.push( mkAndOrder( l, getHigh(h) ));
			v = vh;
		}
		x = mk( v, x, y);
		nstack.drop(2);
		return x;
	}

	// ----[ restrict ] -----------------------------
	/**
	 * The BDD restrict operation. Tries to minimize the size of a BDD by using dont-cares.
	 * For more details, check out <i>"A unified framework for the formal verification of circuits</i>,
	 * by Coudert and Madre.
	 * @see #simplify
	 */
	public int restrict(int u, int v) {
		if(v == 1) return u; // XXX: not sure about his one

		varset_signed(v);
		restrict_careset = v;
		return restrict_rec(u);
	}

	private int restrict_rec(int u) {
		if(u < 2 || getVar(u) > varset_last) return u;


		if(op_cache.lookup(u, restrict_careset, CACHE_RESTRICT)) return op_cache.answer;
		int hash = op_cache.hash_value;
		int ret = 0;


		if(varset_vec[ getVar(u)] ) {
				ret = restrict_rec( sign_vec[getVar(u)] ? getHigh(u) : getLow(u) );
		} else {
			int l = nstack.push( restrict_rec( getLow(u) ));
			int h = nstack.push( restrict_rec( getHigh(u) ));
			ret = mk( getVar(u), l, h);
			nstack.drop(2);
		}

		op_cache.insert(hash, u, restrict_careset, CACHE_RESTRICT, ret );
		return ret;
	}

	// ----[ simplify ] -----------------------------
	/**
	 * The BDD simplification operation. Work like the restrict operation.
	 * <p>Check out Andersens BDD lecture notes for detailed information on this one.
	 *
	 * XXX: we have no cache for it yet!
	 *
	 * @see #restrict
	 */
	public int simplify(int d, int u) {
		if(d == 0) return 0;
		if(u <  2) return u;

		if(d == 1) {
			int l = nstack.push( simplify(d, getLow(u) ));
			int h = nstack.push( simplify(d, getHigh(u) ));
			h = mk( getVar(u), l, h);
			nstack.drop(2);
			return h;
		} else if( getVar(d) == getVar(u)) {
			if(getLow(d)  == 0) return simplify(getHigh(d), getHigh(u));
			if(getHigh(d) == 0) return simplify( getLow(d),  getLow(u));

			int l = nstack.push( simplify( getLow(d),  getLow(u) ));
			int h = nstack.push( simplify(getHigh(d), getHigh(u) ));

			h = mk( getVar(u), l, h);
			nstack.drop(2);
			return h;
		} else if( getVar(d) < getVar(u)) {
			int l = nstack.push( simplify( getLow(d), u ));
			int h = nstack.push( simplify(getHigh(d), u ));
			h = mk( getVar(d), l, h);
			nstack.drop(2);
			return h;
		} else {
			int l = nstack.push( simplify( d,  getLow(u) ));
			int h = nstack.push( simplify( d, getHigh(u) ));

			h = mk( getVar(u), l, h);
			nstack.drop(2);
			return h;
		}
	}

	// ----[ SAT stuff ] ------------------------
	/**
	 * Is this BDD tree a BDD Variable?
	 *
	 * @return true if the BDD is representing a variable.
	 */
	public boolean isVariable(int bdd) {
		if(bdd < 2 || bdd > table_size || !isValid(bdd)) return false;
		return (getLow(bdd) == 0 && getHigh(bdd) == 1);
	}


	/**
	 * returns the number of satisfying assignments for this BDD.
	 * beware of possible floating-point overflow here!
	 *
	 * <p>
	 * Important note:
	 * this works because getVar() returns "numbers of variables plus one" for the
	 * terminal nodes!
	 */
	public double satCount(int bdd) {

		// if the number of variables has changed since the last time, the cache is invalid!
		if(last_sat_vars != -1 && last_sat_vars != num_vars) sat_cache.invalidate_cache();
		last_sat_vars = num_vars;

		return Math.pow(2, getVar(bdd)) * satCount_rec(bdd);
	}

	protected double satCount_rec(int bdd) {
		if(bdd < 2) return bdd;

		if(sat_cache.lookup(bdd)) return sat_cache.answer;
		int hash = sat_cache.hash_value;

		int low = getLow(bdd);
		int high = getHigh(bdd);

		double ret = (satCount_rec(low) * Math.pow(2, getVar(low)  - getVar(bdd)  -1)) +
				(satCount_rec(high) * Math.pow(2, getVar(high) - getVar(bdd)  -1));

		sat_cache.insert(hash, bdd, ret);
		return ret;
	}


	// ----[ node count ] ----------------------------
	private int node_count_int;
	/** compute the number of nodes in the tree (this function is currently somewhat slow)*/
	public int nodeCount(int bdd) {
		node_count_int = 0;
		nodeCount_mark(bdd);
		unmark_tree(bdd);
		return node_count_int;
	}

	/** recursively mark and count nodes, used by nodeCount*/
	private final void nodeCount_mark(int bdd) {
		if(bdd < 2) return;

		if( isNodeMarked(bdd)) return;
		mark_node(bdd);
		node_count_int++;
		nodeCount_mark( getLow(bdd) );
		nodeCount_mark( getHigh(bdd) );
	}


	/** faster nodeCount, but doesn't take the shared child-trees into account */
	public final int quasiReducedNodeCount(int bdd) {
		if(bdd < 2) return 0;
		return 1 + quasiReducedNodeCount(getLow(bdd)) + quasiReducedNodeCount(getHigh(bdd));
	}

	public BitSet minAssignment(int bdd) {
		BitSet set = new BitSet(num_vars);
		minAssignment_rec(set, bdd);
		return set;
	}

	private void minAssignment_rec(BitSet set, int bdd) {
		if (bdd < 2) return;
		int lo = getLow(bdd);
		int hi = getHigh(bdd);
		boolean useHi = lo == 0;
		if (useHi) {
			set.set(getVar(bdd));
			minAssignment_rec(set, hi);
		} else {
			minAssignment_rec(set, lo);
		}
	}

	// ---- [oneSat ] -----------------------------------
	/**
	 * return a satisfying assignment for this BDD as a unary cube.
	 * bdd may not be the constant ONE or ZERO.
	 */
	public int oneSat(int bdd) {
		if( bdd < 2) return bdd;

		if(getLow(bdd) == 0) {
			int high = nstack.push( oneSat(getHigh(bdd)));
			int u = mk( getVar(bdd), 0, high);
			nstack.pop();
			return u;
		} else {
			int low = nstack.push( oneSat(getLow(bdd)));
			int u = mk( getVar(bdd), low, 0);
			nstack.pop();
			return u;
		}
	}
	// ---- [oneSat, vector version] -----------------------------------
	/**
	 * oneSat(bdd, buffer) returns an int vector
	 * x where x[i] is 0/1 or -1 for neg cofactor/pos cofactor and dont care
	 * <p>if buffer is null, a new vector is created otherwise  buffer is used an returned
	 */
	public int [] oneSat(int bdd, int [] buffer) {
		if(buffer == null) buffer = new int[num_vars];

		oneSat_buffer = buffer;
		Array.set(buffer, -1);
		oneSat_rec(bdd);
		oneSat_buffer = null; // help gc :(
		return buffer;
	}

	protected void oneSat_rec(int bdd) {
		if( bdd < 2) return;

		if(getLow(bdd) == 0) {
			oneSat_buffer[ getVar(bdd) ] = 1;
			oneSat_rec(getHigh(bdd));
		} else {
			oneSat_buffer[ getVar(bdd) ] = 0;
			oneSat_rec(getLow(bdd));
		}
	}
	// ---- [ support ] ---------------------------------------------
	/**
	 * returns the support set of a variable, that is a cube C where each of its variables
	 * are used somewhere in the ibpu bdd.
	 */
	public int support(int bdd) {
		Array.set(support_buffer, false);

		support_rec(bdd);
		unmark_tree(bdd);
		int ret = cube(support_buffer);
		return ret;
	}
	private final void support_rec(int bdd) {
		if(bdd < 2) return;

		if( isNodeMarked(bdd) ) return;
		support_buffer[ getVar(bdd) ] = true;
		mark_node(bdd);

		support_rec( getLow(bdd) );
		support_rec( getHigh(bdd) );
	}

	// --- [ member ] -------------------------------------------------------------
	/**
	 * returns true if the given minterm is a included in the bdd.
	 *
	 * <p><b>Note:</b> This method is far far more efficient than computing the "minterm" as BDD,
	 * then OR it to "bdd" and see if the result is not equal to "bdd"!
	 */
	public boolean member(int bdd, boolean [] minterm ) {

		while(bdd >= 2)
			bdd = (minterm[getVar(bdd)]) ? getHigh(bdd) : getLow(bdd);
		return (bdd == 0) ? false : true;
	}

	// ---------------------- common BDD operation shortcuts ----------------------
	/**
	 * the operation bdd1 |= bdd2;  is equal to bdd1 = orTo(bdd1, bdd2);
	 * <p>this operation also handles the ref-counting
	 */
	public int orTo(int bdd1, int bdd2) {
		int tmp = ref( or(bdd1, bdd2) );
		deref(bdd1);
		return tmp;
	}

	/**
	 * the operation bdd1 &= bdd2;  is equal to bdd1 = andTo(bdd1, bdd2);
	 * <p>this operation also handles the ref-counting
	 */
	public int andTo(int bdd1, int bdd2) {
		int tmp = ref( and(bdd1, bdd2) );
		deref(bdd1);
		return tmp;
	}

	// --- [ debug ] ----------------------
	public void showStats() {
		super.showStats();
		op_cache.showStats();
		not_cache.showStats();
		quant_cache.showStats();
		replace_cache.showStats();
		ite_cache.showStats();
		relprod_cache.showStats();
		sat_cache.showStats();
	}


	/* return the amount of internally allocated memory in bytes */
	public long getMemoryUsage() {
		long ret = super.getMemoryUsage();

		// small buffers, but still
		if(varset_vec != null) ret += varset_vec.length * 4;
		if(oneSat_buffer != null) ret += oneSat_buffer.length * 4;
		if(support_buffer != null) ret += support_buffer.length * 1;		 // we assume one byte per boolean :(

		// caches eat a lot of memory
		if(op_cache != null) ret += op_cache.getMemoryUsage();
		if(relprod_cache != null) ret += relprod_cache.getMemoryUsage();
		if(not_cache != null) ret += not_cache.getMemoryUsage();
		if(ite_cache != null) ret += ite_cache.getMemoryUsage();
		if(quant_cache != null) ret += quant_cache.getMemoryUsage();
		if(sat_cache != null) ret += sat_cache.getMemoryUsage();
		if(replace_cache != null) ret += replace_cache.getMemoryUsage();

		// permutations also use some memory
		Permutation tmp = firstPermutation ;
		while(tmp != null) {
			ret += tmp.getMemoryUsage();
			tmp = tmp.next;
		}


		return ret;
	}

	// --------------------------------------------------------
	/** printthis BDD to console (prints internal structure) */
	public void print(int bdd) {BDDPrinter.print(bdd, this);	}

	/** creates a DOT file for this BDD and convert to an image using DOT */
	public void printDot(String fil, int bdd) {	BDDPrinter.printDot(fil, bdd, this, nodeNames);	}

	/**  possibly using dont-cares, print satisfying assignment od this BDD */
	public void printSet(int bdd) {	BDDPrinter.printSet(bdd, num_vars, this, null);	}

	/** print the cubes (true assignment) for the minterms in this BDD */
	public void printCubes(int bdd) {	BDDPrinter.printSet(bdd, num_vars, this, nodeNames);	}

	/**
	 * Change the default node-naming procedure, for advanced users only!
	 * @see NodeName
	 */
	public void setNodeNames(NodeName nn) { nodeNames = nn; }

}
