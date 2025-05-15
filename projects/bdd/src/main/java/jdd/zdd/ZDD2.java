
package jdd.zdd;


import jdd.bdd.OptimizedCache;
import jdd.util.Configuration;

/**
 * ZDD2 extends ZDD with some additional operations for unate cube set algebra
 * @see ZDD
 */
public class ZDD2  extends ZDD  {

	private static final int CACHE_MUL = 0, CACHE_DIV = 1, CACHE_MOD = 2;
	protected OptimizedCache unate_cache;


	public ZDD2(int nodesize) {
		this(nodesize, Configuration.DEFAULT_ZDD_CACHE_SIZE);
	}

	public ZDD2(int nodesize, int cachesize) {
		super(nodesize, cachesize);
		unate_cache = new OptimizedCache("unate", cachesize / Configuration.zddUnateCacheDiv , 3, 2);
	}



	// ---------------------------------------------------------------
	public void cleanup() {
		super.cleanup();
		unate_cache = null;
	}

	// ---------------------------------------------------------------
	protected void post_removal_callbak() {
		super.post_removal_callbak();
		unate_cache.free_or_grow(this);
	}


	// ----------------------------------------------------------------------
    public final int mul(int p, int q) {

        if(p == 0 || q == 0) return 0;
        if(p == 1) return q;
        if(q == 1) return p;


        int pvar = getVar(p);
        int qvar = getVar(q);

        // maybe we should compare p and q?
        if(pvar > qvar) {
            int tmp = p; p = q; q = tmp;
            tmp = pvar; pvar = qvar; qvar = tmp;
        }

        if(unate_cache.lookup(p, q, CACHE_MUL)) return unate_cache.answer;
		int hash = unate_cache.hash_value;

        int tmp1, tmp2, ret;
        if(pvar < qvar) {
            tmp1 =  nstack.push( mul( p, getHigh(q)));
            tmp2 =  nstack.push( mul( p, getLow(q)));
            ret = mk(qvar, tmp2, tmp1);
            nstack.drop(2);
        } else { // pvar == qvar
            tmp1 = nstack.push( mul( getHigh(p), getHigh(q)));
            tmp2 = nstack.push( mul( getHigh(p), getLow(q)));

            ret = union(tmp1, tmp2);
            nstack.drop(2);
            nstack.push( ret);

            tmp1 = nstack.push( mul(getLow(p), getHigh(q)));

            tmp1 = union(ret, tmp1);
            nstack.drop(2);
            nstack.push( tmp1);

            tmp2 = nstack.push( mul(getLow(p), getLow(q)));
            ret  = mk(pvar, tmp2, tmp1);
            nstack.drop(2);
        }


		unate_cache.insert(hash, p, q, CACHE_MUL, ret);
		return ret;
	}

	// ----------------------------------------------------------------
	/**
     * if q contains a single literal, this equals subset1(p, getVar(q))
     */
    public final int div(int p, int q) {
        if(p < 2) return 0;
        if(p == q) return 1;
        if(q == 1) return p;

        int pvar = getVar(p);
        int qvar = getVar(q);

        if(pvar < qvar) return 0;


		if(unate_cache.lookup(p, q, CACHE_DIV)) return unate_cache.answer;
		int hash = unate_cache.hash_value;

        int tmp1, tmp2, ret;
        if(pvar > qvar) {
            tmp1 = nstack.push( div(getLow(p), q));
            tmp2 = nstack.push( div(getHigh(p), q));
            ret  = mk(pvar, tmp1, tmp2);
            nstack.drop(2);
        } else {
            ret = div( getHigh(p), getHigh(q));

            tmp1 = getLow(q);
            if(tmp1 != 0 && ret != 0) {
                nstack.push( ret); // save it
                tmp1 = nstack.push( div( getLow(p), tmp1));
                ret = intersect(tmp1, ret);
                nstack.drop(2);
            }
        }

        unate_cache.insert(hash, p, q, CACHE_DIV, ret);
		return ret;
    }

	/** if q contains a single literal, this equals subset0(p, getVar(q)) */
	public final int mod(int p, int q) { // P % Q = P - Q * (P / Q)


		if(unate_cache.lookup(p, q, CACHE_MOD)) return unate_cache.answer;
		int hash = unate_cache.hash_value;

		int tmp = nstack.push( div(p, q));
		tmp = nstack.push( mul(q, tmp));
		tmp = diff(p, tmp);
		nstack.drop(2);

		unate_cache.insert(hash, p, q, CACHE_MOD, tmp);
		return tmp;

	}
	// --- [ debug ] ----------------------------------------------

	public void showStats() {
		super.showStats();
		unate_cache.showStats();
	}

	/** return the amount of internally allocated memory in bytes */
	public long getMemoryUsage() {
		long ret = super.getMemoryUsage();
		if(unate_cache != null) ret += unate_cache.getMemoryUsage();
		return ret;
	}
}

