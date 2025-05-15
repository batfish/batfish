
package jdd.zdd;


import jdd.bdd.OptimizedCache;
import jdd.util.Configuration;

/**
 * ZDD with graph algorithms from Coudert's paper.
 *
 */

public class ZDDGraph  extends ZDD  {
    protected  static final int CACHE_NOSUBSET = 0, CACHE_NOSUPSET = 1;
	protected OptimizedCache graph_cache;

	public ZDDGraph(int nodesize, int cachesize) {
		super(nodesize, cachesize);
        graph_cache = new OptimizedCache("graph", cachesize / Configuration.zddGraphCacheDiv , 3, 2);
	}

    // ---------------------------------------------------------------
	public void cleanup() {
		super.cleanup();
		graph_cache = null;
	}
	// ---------------------------------------------------------------


	protected void post_removal_callbak() {
		super.post_removal_callbak();
		graph_cache.free_or_grow(this);
	}

	// ---------------------------------------------------------------

	/**
	 * all pairs, that is, all possible edges in a fully connected graph.
	 * if V = { x_from, ..., x_to} then  allEdge(V) = { (v1,v2) | v1,v2 \in V. v1 != v2 }
	 */
	public int allEdge() { return allEdge(0, num_vars-1); }
	public int allEdge(int from, int to) {
		if(to < from) return 0;

		int left = 0, right = mk(from, 0,1);
		nstack.push( left); // place holders
		nstack.push( right);
		for(int i = from+1; i < to; i++) {
			int tmp1 = nstack.push( mk(i,left, right));
			int tmp2 = nstack.push( mk(i,right, 1));
			nstack.drop(4);
			left = nstack.push( tmp1);
			right = nstack.push( tmp2);
		}
		int ret = mk(to, left, right);
		nstack.drop(2);
		return ret;
	}

	// ------------------------------------------------------------------------

    /**
     * noSubset(F, C) = {f \in F | \lnot \exist c \in C. f \subseteq c }
     */

    public final int noSubset(int F, int C) {
        if(F == C || F == 1 || F == 0) return 0;
        if(C == 0) return F;
        if(C == 1) return diff(F, 1);

        if(graph_cache.lookup(F, C, CACHE_NOSUBSET)) return graph_cache.answer;
		int hash = graph_cache.hash_value;

        int ret;
        int fvar = getVar(F);
        int cvar = getVar(C);
        if(fvar > cvar) {
            int tmp1 = nstack.push( noSubset(getLow(F), C));
            ret = mk (fvar, tmp1, getHigh(F));
            nstack.pop();
        } else if(fvar < cvar) {
            int tmp1 = nstack.push( noSubset(F, getLow(C)));
            int tmp2 = nstack.push( noSubset(F, getHigh(C)));
            ret = intersect( tmp1, tmp2);
            nstack.drop(2);
        } else {
            int tmp1 = nstack.push( noSubset(getLow(F), getLow(C)));
            int tmp2 = nstack.push( noSubset(getLow(F), getHigh(C)));

            tmp1 = intersect( tmp1, tmp2);
            nstack.drop(2);
            nstack.push( tmp1);

            tmp2 = nstack.push( noSubset(getHigh(F), getHigh(C)));
            ret = mk( fvar, tmp1, tmp2);
            nstack.drop(2);
        }

        graph_cache.insert(hash, F, C, CACHE_NOSUBSET, ret);

        return ret;
    }


    /**
	 * noSupset is used to compute exclude.
	 *
	 * noSupset(F, C) = {f \in F | \lnot \exist c \in C. c \subseteq f }
     */

    public int noSupset(int F, int C)  {
		if( emptyIn(C)) return 0;
		return noSupset_rec(F, C);
	}


    private final int noSupset_rec(int F, int C) {

		if(F == 0 || C == 1 || F == C) return 0;
        if(F == 1 || C == 0) return F;

		if(graph_cache.lookup(F, C, CACHE_NOSUPSET)) return graph_cache.answer;
		int hash = graph_cache.hash_value;

		int ret;
		int fvar = getVar(F);
		int cvar = getVar(C);

		if (fvar < cvar) {
			ret = noSupset_rec(F, getLow(C));
		} else if (fvar > cvar) {
			int tmp1 = nstack.push( noSupset_rec(getHigh(F), C));
			int tmp2 = nstack.push( noSupset_rec(getLow(F) , C));
			ret = mk(fvar, tmp2, tmp1);
			nstack.drop(2);
		} else {

            int tmp1, tmp2;
            int C1 = getHigh(C);

            if( emptyIn(C1)) {
                // special case, beause  noSupset( getHigh(F), C1) = 0
                tmp1 = nstack.push(0);
            } else {
                tmp1 = nstack.push( noSupset_rec( getHigh(F), getLow(C)));
                tmp2 = nstack.push( noSupset_rec( getHigh(F), C1));
                tmp1 = intersect(tmp1, tmp2);
                nstack.drop(2);
                nstack.push( tmp1);
            }


			tmp2 = nstack.push( noSupset_rec( getLow(F), getLow(C)));
			ret = mk(fvar, tmp2, tmp1);
			nstack.drop(2);
		}

		graph_cache.insert(hash, F, C, CACHE_NOSUPSET, ret);
		return ret;
    }



	// ------------------------------------------------------------------------

	/**
	 * MaxSet(X) = { x \in X | \forall x' \in X. x  \subseteq x' ==> x = x' }
	 * that is:
	 * MaxSet(X) = { x \in X | \lnot \exist x' \in X - x. x  \subseteq x' }
	 */
	 public int maxSet(int X) {
		if(X < 2) return X;
		int T0 = nstack.push( maxSet( getLow(X)));
		int T1 = nstack.push( maxSet( getHigh(X)));
		int T2 = nstack.push( noSubset(T0, T1));

		T0 = mk( getVar(X), T2, T1);
		nstack.drop(3);
		return T0;
	 }
}

