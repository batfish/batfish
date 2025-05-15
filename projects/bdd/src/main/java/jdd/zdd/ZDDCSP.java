
package jdd.zdd;


import jdd.bdd.OptimizedCache;
import jdd.util.Configuration;

/**
 * ZDD operations for CSP problems.
 * based on "On the properties of combination set operations", by Okuno, Minato and Isozaki.
 *
 * <p>I am also thankfull to Paolo Bonzini who suggested
 * replaceing the slow Exclude() with noSubset() :)
 */
public class ZDDCSP  extends ZDD2  {
	protected static final int  CACHE_RESTRICT = 0, CACHE_NOSUPSET = 1;

	protected OptimizedCache csp_cache;

	public ZDDCSP(int nodesize, int cachesize) {
		super(nodesize, cachesize);
		csp_cache = new OptimizedCache("csp", cachesize / Configuration.zddCSPCacheDiv , 3, 2);
	}


	// ---------------------------------------------------------------
	public void cleanup() {
		super.cleanup();
		csp_cache = null;
	}
	// ---------------------------------------------------------------


	protected void post_removal_callbak() {
		super.post_removal_callbak();
		csp_cache.free_or_grow(this);
	}



	public final int restrict(int F, int C) {
		if(F == 0 || C == 0) return 0;
		if(F == C) return F;
		//if(C == 1) return 0; // <=== is this OK ???

		if(csp_cache.lookup(F, C, CACHE_RESTRICT)) return csp_cache.answer;
		int hash = csp_cache.hash_value;

		int ret = 0, v = getVar(F);
		if(v < getVar(C)) { // F0 = F, F1 = 0 ==> restrict(F1,Cn) = 0
			int tmp =  nstack.push( restrict(F,getLow(C)));
			ret = mk(getVar(C), tmp, 0);
			nstack.pop();
		} else if(v > getVar(C)) { // C0 = C, C1 = 0 => restrict(F1,C1) = 0
			int tmp  = nstack.push( restrict( getHigh(F), C));
			int tmp2 = nstack.push( restrict( getLow(F), C));
			ret = mk(v, tmp2, tmp);
			nstack.drop(2);
		} else {
			int tmp1 = nstack.push( restrict(getHigh(F), getHigh(C)));
			int tmp2 = nstack.push( restrict(getHigh(F), getLow(C)));
			tmp1 = union(tmp1, tmp2);
			nstack.drop(2);
			nstack.push( tmp1);
			tmp2 = nstack.push( restrict(getLow(F),getLow(C)));
			ret = mk(v, tmp2, tmp1);
			nstack.drop(2);
		}


		csp_cache.insert(hash, F, C, CACHE_RESTRICT, ret);
		return ret;
	}
	// ---------------------------------------------------------------
    /**
     * slow Exclude conputed using the definition:
     * Exclude(F,C) = F - Restrict(F,C)
     */
	private final int exclude_slow(int F, int C) {
		int tmp = nstack.push( restrict(F,C));
		tmp = diff(F, tmp);
		nstack.pop();
		return tmp;
	}


	// ---------------------------------------------------------------
    /**
     * fast Exclude computed under its other name "noSupset":
	 *
	 * noSupset(F, C) = {f \in F | \lnot \exist c \in C, c \subseteq f }
     *
     * NOTE THAT THE SAME CODE IS ALSO FOUND IN ZDDGraph.noSupset(F,C)
     * We could have used some OOP tricks to reuse that code here, but
     * I decided not to...
     */

    private final int exclude_fast(int F, int C)  {
		if( emptyIn(C)) return 0;
		return noSupset_rec(F, C);
	}


    private final int noSupset_rec(int F, int C) {

		if(F == 0 || C == 1 || F == C) return 0;
        if(F == 1 || C == 0) return F;

		if(csp_cache.lookup(F, C, CACHE_NOSUPSET)) return csp_cache.answer;
		int hash = csp_cache.hash_value;

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
                // special case, because noSupset( getHigh(F), C1) = 0
               tmp1 = nstack.push( 0);
            } else {
                int F1 = getHigh(F);
                tmp1 = nstack.push( noSupset_rec( F1, getLow(C)));
                tmp2 = nstack.push( noSupset_rec( F1, C1));
                tmp1 = intersect(tmp1, tmp2);
                nstack.drop(2);
                nstack.push( tmp1);
            }

			tmp2 = nstack.push( noSupset_rec( getLow(F), getLow(C)));
			ret  = mk(fvar, tmp2, tmp1);
			nstack.drop(2);
		}

		csp_cache.insert(hash, F, C, CACHE_NOSUPSET, ret);
		return ret;
    }


    // ---------------------------------------------------------------
    /**
     * Exclude(F,C), defined as "F - Restrict(F,C)"
     */
	public final int exclude(int F, int C) {
		// return exclude_slow(F,C);
        return exclude_fast(F, C);
	}

	// --- [ debug ] ----------------------------------------------

	public void showStats() {
		super.showStats();
		csp_cache.showStats();
	}

	public long getMemoryUsage() {
		long ret = super.getMemoryUsage();
		if(csp_cache != null) ret += csp_cache.getMemoryUsage();
		return ret;
	}
}

