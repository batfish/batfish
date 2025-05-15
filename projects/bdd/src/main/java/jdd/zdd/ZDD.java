
package jdd.zdd;

import jdd.bdd.BDD;
import jdd.bdd.NodeTable;
import jdd.bdd.OptimizedCache;
import jdd.bdd.debug.BDDDebuger;
import jdd.util.Array;
import jdd.util.Configuration;
import jdd.util.NodeName;
import jdd.util.Options;

import java.util.Collection;
import java.util.StringTokenizer;

/**
 * Base implementation for Zero-Suppressed Binary Decision Diagrams (Z-BDDs).
 * Z-BDDs are a special type of BDDs that are more suited for sparse sets.
 * For example, if you are working with a set that is most of the time empty,
 * you might want to use Z-BDDs instead of BDDs.
 * <p>
 * The base implementation will give you the core Z-BDD operation such as
 * change, diff and union. To get more operators, see the sub-classes.
 *
 * @see BDD
 * @see ZDD2
 * @see ZDDCSP
 */

// NOTE FOR DEVELOPERS:
// Zero-suppressed BDDs use INVERSE variable order: v_last at top, v_0 at
//      bottom just above 0 and 1.
//
//	  To make implementation easier,
//	  v_0 has t_var of 0 while 0 and 1 are assigned t_var of -1.

public class ZDD extends NodeTable {
	private static final int CACHE_SUBSET0 = 0, CACHE_SUBSET1 = 1, CACHE_CHANGE = 2,
							CACHE_UNION = 3, CACHE_INTERSECT = 4, CACHE_DIFF = 5;

	protected int num_vars; /** number of Z-BDD variables */
	private int node_count_int; /** internal variable used by nodeCount */
	private OptimizedCache unary_cache;		// for unary stuff. (ZDD, variable, op) => ZDD
	private OptimizedCache binary_cache;	// for binary operations. (ZDD, ZDD, op) => ZDD
	protected NodeName nodeNames = new ZDDNames();

	/**
	 * create a Z-BDD manager
	 * @param nodesize is the number of nodes initially allocated in the node-table
	 */
	public ZDD(int nodesize) {
		this(nodesize, Configuration.DEFAULT_ZDD_CACHE_SIZE);
	}

	/**
	 * create a Z-BDD manager
	 * @param nodesize is the number of nodes initially allocated in the node-table
	 * @param cachesize is the suggested cache size.
	 */

	public ZDD(int nodesize, int cachesize) {
		super(nodesize);


		unary_cache = new OptimizedCache("unary", cachesize / Configuration.zddUnaryCacheDiv, 3, 1);
		binary_cache = new OptimizedCache("binary", cachesize / Configuration.zddBinaryCacheDiv, 3, 2);


		// FIXME:
		// we can do this, but it wont include the base-class (ZDD2, ZDDCSP etc) caches:
		if(Options.profile_cache)  new jdd.bdd.debug.BDDDebugFrame(this);

		// yes, we know how deep our trees are and will call tree_depth_changed()!
		enableStackMarking();
	}


	// ---------------------------------------------------------------
	/** cleanup this ZDD, to release its memory and help GC */
	public void cleanup() {
		super.cleanup();
		binary_cache = null;
		unary_cache = null;
	}
	// ---------------------------------------------------------------
	// Debugging stuff
	public Collection addDebugger(BDDDebuger d) {
		Collection v = super.addDebugger( d );
		v.add( unary_cache );
		v.add( binary_cache );
		return v;
	}
	// ---------------------------------------------------------------

	protected void post_removal_callbak() {
		binary_cache.free_or_grow(this);
		unary_cache.free_or_grow(this);
	}

	/** Zero-suppressed MK operator */
	protected final int mk(int i, int l, int h) {
		if(h == 0) return l; /* ZDD node elimination */
		return add(i,l,h);
	}



	// -------------------------------------------
    /** create a new ZDD variable */
	public int createVar() {
		int ret = num_vars++;
		// we want to keep the work stack at least so large
		nstack.grow(5 * num_vars + 3);

		tree_depth_changed(num_vars); // MUST be called
		return ret;
	}
	// --------------------------------------------------------

    /** returns {} */
	public final int empty() { return 0; }

    /** returns {{}} */
	public final int base() { return 1; }

    /** create a tree of a variable. single(vx) = { vx } */
	public final int single(int var) { return mk(var, 0, 1); }

	public final int universe() {
		int last = 1;
		for(int i = 0; i < num_vars; i++) {
			nstack.push(last);
			last = mk(i, last, last);
			nstack.pop();
		}
		return last;
	}

    /** cube of a single variable v */
	public final int cube(int v) { return mk(v, 0, 1) ; }

    /** cube of a selection of variables */
	public final int cube(boolean [] v) {
		int last = 1;
		for(int i = 0; i < v.length; i++)
			if( v[i] ) {
				nstack.push(last);
				last = mk(i, 0, last);
				nstack.pop();
			}
		return last;
	}

    /**
     * cube of a selection of variables, represented as
     * a string, e.g. "11001"
     */
	public final int cube(String s) {
		int len = s.length();
		int last = 1;
		for(int i = 0; i < len; i++)
			if( s.charAt(len - i - 1) == '1') {
				nstack.push(last);
				last = mk(i, 0, last);
				nstack.pop();
			}
		return last;
	}
    /**
     * Union of cubes, each represented by a string token.
     *
     * This function was added to ease fast creation of
     * sets of sets in tests.
     *
     * E.g. "0001 1000 1101" => { v1, v4, v4v3v1 }
     */
    public int cubes_union(String s) {
        return do_cubes_op(s, true);
    }

    /**
     * Intersection of cubes. same as
     * cubes_unon() but uses intersect instead of union
     */
    public int cubes_intersect(String s) {
        return do_cubes_op(s, false);
    }

    private int do_cubes_op(String s, boolean do_union) {
        StringTokenizer st = new StringTokenizer(s," \t\n,;");
        int ret = -1;

        while(st.hasMoreTokens()) {
            String str = st.nextToken();
            int c = cube(str);

            if(ret == -1) ret = c;
            else {
                ref(ret);
                ref(c);
                int tmp1 = do_union ? union(ret,c) : intersect(ret,c);
                deref(ret);
                deref(c);
                ret = tmp1;
            }
        }
        return ret;
    }

	public int subsets(boolean [] v) {
		int last = 1;
		for(int i = 0; i < v.length; i++)
			if( v[i] ) {
				nstack.push(last);
				last = mk(i, last, last);
				nstack.pop();
			}
		return last;
	}

	//-----------------------------------------------
	/** var is a variable, NOT a tree */
	public final int subset1(int zdd, int var) {
		if(var < 0 || var >= num_vars) return -1; // INVALID VARIABLE!

		if(getVar(zdd) < var) return 0;
		if(getVar(zdd) == var) return getHigh(zdd);

		// ... else if(getVar(zdd) > var)

		if(unary_cache.lookup(zdd, var, CACHE_SUBSET1)) return unary_cache.answer;
		int hash = unary_cache.hash_value;

		int l = nstack.push( subset1( getLow(zdd), var));
		int h = nstack.push( subset1( getHigh(zdd), var));
		l = mk( getVar(zdd), l, h);
		nstack.drop(2);

		unary_cache.insert(hash, zdd, var, CACHE_SUBSET1, l);
		return l;
	}

	/** var is a variable, NOT a tree */
	public final int subset0(int zdd, int var) {
		if(var < 0 || var >= num_vars) return -1; // INVALID VARIABLE!

		if(getVar(zdd) < var) return zdd;
		if(getVar(zdd) == var) return getLow(zdd);

		// ... else if(getVar(zdd) > var)
		if(unary_cache.lookup(zdd, var, CACHE_SUBSET0)) return unary_cache.answer;
		int hash = unary_cache.hash_value;

		int l = nstack.push( subset0( getLow(zdd), var));
		int h = nstack.push( subset0( getHigh(zdd), var));
		l = mk( getVar(zdd), l, h);
		nstack.drop(2);
		unary_cache.insert(hash, zdd, var, CACHE_SUBSET0, l);
		return l;
	}

	public final int change(int zdd, int var) {
		if(var < 0 || var >= num_vars) return -1; // INVALID VARIABLE!


		if(getVar(zdd) < var) return mk(var, 0, zdd);
		// XXX: FIX THIS: if(getVar(zdd) == var) return mk(var, getVar(zdd), getVar(zdd));
		if(getVar(zdd) == var) return mk(var, getHigh(zdd), getLow(zdd));

		// else if(v > var)
		if(unary_cache.lookup(zdd, var, CACHE_CHANGE)) return unary_cache.answer;
		int hash = unary_cache.hash_value;


		int l = nstack.push( change( getLow(zdd), var));
		int h = nstack.push( change( getHigh(zdd), var));
		l = mk( getVar(zdd), l, h);
		nstack.drop(2);

		unary_cache.insert(hash, zdd, var, CACHE_CHANGE, l);
		return l;
	}


	public final int union(int p, int q) {
		if(getVar(p) > getVar(q)) return union(q,p);
		if(p == 0) return q;
		if(q == 0 || q == p) return p;
		// NOT USEFUL HERE: if(p == 1) 	return insert_base(q);


		if(binary_cache.lookup(p, q, CACHE_UNION)) return binary_cache.answer;
		int hash = binary_cache.hash_value;

		int l;
		if(getVar(p) < getVar(q)) {
			l = nstack.push( union(p, getLow(q)));
			l = mk(getVar(q), l, getHigh(q));
			nstack.pop();
		} else {
			l = nstack.push( union( getLow(p), getLow(q)));
			int h = nstack.push( union(getHigh(p), getHigh(q)));
			l = mk( getVar(p), l, h);
			nstack.drop(2);
		}

		binary_cache.insert(hash, p, q, CACHE_UNION, l);
		return l;
	}


	public final int intersect(int p, int q) {
		if(p == 0 || q == 0) return 0;
		if(q == p) return p;
		if(p == 1) return follow_low(q);	// ADDED BY ARASH, NOT TESTED VERY MUCH YET, DONT NOW IF IT MAKES THINGS FASTER OR SLOWER!!
		if(q == 1) return follow_low(p);	//  (not in the original Minato paper)



		if(binary_cache.lookup(p, q, CACHE_INTERSECT)) return binary_cache.answer;
		int hash = binary_cache.hash_value;

		int l = 0;
		if(getVar(p) > getVar(q)) l =  intersect( getLow(p), q);
		else if(getVar(p) < getVar(q)) l =  intersect( p, getLow(q));
		else {
		// else if getVar(p) == getVar(q)
			l = nstack.push( intersect( getLow(p), getLow(q)));
			int h = nstack.push( intersect(getHigh(p), getHigh(q)));
			l = mk( getVar(p), l, h);
			nstack.drop(2);
		}

		binary_cache.insert(hash, p, q, CACHE_INTERSECT, l);
		return l;
	}

	public final int diff(int p, int q) {
		if(p == 0 || p == q ) return 0;
		if(q == 0) return p;

		if(binary_cache.lookup(p, q, CACHE_DIFF)) return binary_cache.answer;
		int hash = binary_cache.hash_value;

		int l = 0;
		if(getVar(p) < getVar(q)) l =  diff( p, getLow(q));
		else if(getVar(p) > getVar(q)) {
			l = nstack.push( diff( getLow(p), q));
			l = mk( getVar(p), l, getHigh(p));
			nstack.pop();
		} else { // getVar(p) == getVar(q);
			l = nstack.push( diff( getLow(p), getLow(q)));
			int h = nstack.push( diff(getHigh(p), getHigh(q)));
			l = mk( getVar(p), l, h);
			nstack.drop(2);
		}

		binary_cache.insert(hash, p, q, CACHE_DIFF, l);
		return l;
	}


	// ----[ misc, mostly early-termination stuff ]------------------------------------

	/** returns the terminal value along the all-low path */
	public final int follow_low(int zdd) {
		while( zdd >= 2) zdd = getLow(zdd);
		return zdd;
	}

	/** returns the terminal value along the all-high path */
	public final int follow_high(int zdd) {
		while( zdd >= 2) zdd = getHigh(zdd);
		return zdd;
	}
	/** returns true if base {{}} is in X */
	public final boolean emptyIn(int X) {
		return (follow_low(X) == 1);
	}

	/** computes set U {base} */
	private final int insert_base(int set) {
		if(set < 2) return 1; // <-- the magic happens here
		int l = nstack.push( insert_base(getLow(set)));
		l = (getLow(set) == l) ? set : mk(getVar(set), l, getHigh(set));
		nstack.pop();
		return l;
	}

	// --- [ satOne ] ----------------------------------------------
	/** Returns a satisfying boolean assignment, null if none<br>XXX: this code is still untested! */
	public boolean [] satOne(int zdd, boolean [] vec) {
		if(zdd == 0) return null;
		if(vec == null) vec = new boolean[num_vars];
		Array.set(vec, false);
		if(zdd != 1) satOne_rec(zdd, vec);
		return vec;
	}
	private void satOne_rec(int zdd, boolean [] vec) {
		if(zdd < 2) return;
		int next = getLow(zdd);
		if(next == 0 ){
			vec[ getVar(zdd) ] = true;
			next = getHigh(zdd);
		}
		satOne_rec(next, vec);
	}
	// --- [ misc] ----------------------------------------------
	public final int count(int zdd) {
		if(zdd < 2) return zdd;
		return count(getLow(zdd)) +  count(getHigh(zdd));
	}

	// ----[ node count ] ----------------------------
	/** compute the number of nodes in the tree (this function is currently somewhat slow)*/
	public int nodeCount(int zdd) {
		node_count_int = 0;
		nodeCount_mark(zdd);
		unmark_tree(zdd);
		return node_count_int;
	}

	/** recursively mark and count nodes, used by nodeCount*/
	private final void nodeCount_mark(int zdd) {
		if(zdd < 2) return;

		if( isNodeMarked(zdd)) return;
		mark_node(zdd);
		node_count_int++;
		nodeCount_mark( getLow(zdd) );
		nodeCount_mark( getHigh(zdd) );
	}

	// --- [ helper stuff ]---------------------------------------------
	/** helper function to compute set' = set UNION add, derefs the old set! */
	public int unionTo(int set, int add) {
		int tmp = ref( union(set, add) );
		deref(set);
		return tmp;
	}
	/** helper function to compute set' = set - add, derefs the old set! */
	public int diffTo(int set, int add) {
		int tmp = ref( diff(set, add) );
		deref(set);
		return tmp;
}
	// --- [ debug ] ----------------------------------------------
	/** show ZDD statistics */
	public void showStats() {
		super.showStats();
		unary_cache.showStats();
		binary_cache.showStats();
	}

	/** return the amount of internally allocated memory in bytes */
	public long getMemoryUsage() {
		long ret = super.getMemoryUsage();
		if(unary_cache != null) ret += unary_cache.getMemoryUsage();
		if(binary_cache != null) ret += binary_cache.getMemoryUsage();
		return ret;
	}

	public void setNodeNames(NodeName nn) {
		nodeNames = nn;
	}

	public void print(int zdd) { ZDDPrinter.print(zdd, this, nodeNames);	}
	public void printDot(String fil, int bdd) {	ZDDPrinter.printDot(fil, bdd, this, nodeNames);	}
	public void printSet(int bdd) {	ZDDPrinter.printSet(bdd,  this, nodeNames);	}
	public void printCubes(int bdd) {	ZDDPrinter.printSet(bdd, this, null);	}

}
