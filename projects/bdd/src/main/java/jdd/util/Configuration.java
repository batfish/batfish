
package jdd.util;


/**
 * This class contains the various configuration parameters used in the package
 *
 */

public class Configuration {

	// ---- [ dont change the default values here ] --------------------------------

	/**
	 * number of garbage collection we will wait before giving up an unused partial cache.
	 * @see jdd.bdd.OptimizedCache
	 */
	public static final int MAX_KEEP_UNUSED_PARTIAL_CACHE = 5;


	/**
	 * the smallest node-table we will allow
	 * @see jdd.bdd.NodeTable
	 */
	public static final int MIN_NODETABLE_SIZE = 100;


	/**
	 * the smallest we will allow
	 * @see jdd.bdd.SimpleCache
	 */
	public static final int MIN_CACHE_SIZE = 32;

	// --- [ default values for tweakable parameters ] ------------------------

	// note-table
	public static int DEFAULT_NODETABLE_SIMPLE_DEADCOUNT_THRESHOLD = 20000;	/** see #nodetableSimpleDeadcountThreshold */
	public static final int DEFAULT_NODETABLE_SMALL_SIZE = 200000; /** @see #nodetableSmallSize*/
	public static final int DEFAULT_NODETABLE_LARGE_SIZE = 4000000; /** @see #nodetableLargeSize  */
	public static final int DEFAULT_NODETABLE_GROW_MIN = 50000; /** @see #nodetableGrowMin  */
	public static final int DEFAULT_NODETABLE_GROW_MAX = 300000; /** @see #nodetableGrowMax  */


	// bdd
	public final static int DEFAULT_BDD_OPCACHE_DIV = 1;	/** see #bddOpcacheDiv */
	public final static int DEFAULT_BDD_NEGCACHE_DIV = 2;	/** see #bddNegcacheDiv */
	public final static int DEFAULT_BDD_ITECACHE_DIV = 4;	/** see #bddItecacheDiv */
	public final static int DEFAULT_BDD_QUANTCACHE_DIV = 3;	/** see #bddOpcacheDiv */
	public final static int DEFAULT_BDD_RELPRODCACHE_DIV = 2; /** see #bddRelprodcacheDiv */
	public final static int DEFAULT_BDD_REPLACECACHE_DIV = 3;	/** see #bddReplacecheDiv */
	public final static int DEFAULT_BDD_SATCOUNT_DIV = 8;	/** see #bddSatcountDiv */



	public final static int DEFAULT_MAX_NODE_INCREASE = 100000;	/** see #maxNodeIncrease */
	public final static int DEFAULT_MIN_FREE_NODES_PROCENT = 20;	/** see #minFreeNodesProcent */
	public final static int DEFAULT_MAX_NODE_FREE = DEFAULT_MAX_NODE_INCREASE;	/** see #maxNodeFree */


	public final static int DEFAULT_MAX_SIMPLECACHE_GROWS = 5; /** see #maxSimplecacheGrows */
	public final static int DEFAULT_MIN_SIMPLECACHE_HITRATE_TO_GROW = 40; /** see #minSimplecacheHitrateToGrow */
	public final static int DEFAULT_MIN_SIMPLECACHE_ACCESS_TO_GROW = 15;	/** see #minSimplecacheAccessToGrow */


	public final static byte DEFAULT_CACHEENTRY_STICKY_HITS = 16;	/** see #cacheentryStickyHits */

	public final static int DEFAULT_MAX_CACHE_GROWS = 3;	/** see #maxCacheGrows */
	public final static int DEFAULT_MIN_CACHE_LOADFACTOR_TO_GROW = 95; /** see #minCacheLoadfactorToGrow */



	// -------- node table

	/** the threshold below which a simpler deadnode counter is used */
	public static int nodetableSimpleDeadcountThreshold = DEFAULT_NODETABLE_SIMPLE_DEADCOUNT_THRESHOLD;

	/** if node-table is smaller than this, we grow table with nodetableGrowMax nodes at a time */
	public static int nodetableSmallSize = DEFAULT_NODETABLE_SMALL_SIZE;

	/** if node-table is larger than this, we grow table with nodetableGrowMin nodes at a time */
	public static int nodetableLargeSize = DEFAULT_NODETABLE_LARGE_SIZE;

	/** the smallest node-table grow we recommend */
	public static int nodetableGrowMin = DEFAULT_NODETABLE_GROW_MIN;

	/** the largest node-table grow we allow */
	public static int nodetableGrowMax = DEFAULT_NODETABLE_GROW_MAX;

	// --- BDD node/hashtbale



	// -- BDD.java
	/** default size of the BDD cache range: 100-10k */
	public final static int DEFAULT_BDD_CACHE_SIZE = 1000;

	/** how little of the BDD cache is used for operations ?, range: 1-8 */
	public static int bddOpcacheDiv = DEFAULT_BDD_OPCACHE_DIV;

	/** how little of the BDD cache is used for negation?, range: 1-8 */
	public static int bddNegcacheDiv = DEFAULT_BDD_NEGCACHE_DIV;

	/** how little of the BDD cache is used for ITE?, range: 1-8 */
	public static int bddItecacheDiv = DEFAULT_BDD_ITECACHE_DIV;

	/** how little of the BDD cache is used for quantification?, range: 1-8 */
	public static int bddQuantcacheDiv = DEFAULT_BDD_QUANTCACHE_DIV;

	/** cache for relProd */
	public final static int bddRelprodcacheDiv = DEFAULT_BDD_RELPRODCACHE_DIV;

	/** how little the replace cache is, range: 2-8 */
	public final static int bddReplacecacheDiv = DEFAULT_BDD_REPLACECACHE_DIV;

	/** how little the satcount cache is, range: 4-32 */
	public final static int bddSatcountDiv = DEFAULT_BDD_SATCOUNT_DIV;




	// -- ZDD.java
	/**
	 * default size of the ZDD cache range:
	 * @see #DEFAULT_BDD_CACHE_SIZE
	 */
	public final static int DEFAULT_ZDD_CACHE_SIZE = DEFAULT_BDD_CACHE_SIZE;
	public final static int ZDD_UNARY_CACHE_DIV = 2; /** see #zddUnaryCacheDiv */
	public final static int ZDD_BINARY_CACHE_DIV = 2; /** see #zddBinaryCacheDiv */
	public final static int ZDD_UNATE_CACHE_DIV = 2; /** see #zddUnateCacheDiv */
	public final static int ZDD_CSP_CACHE_DIV = 2; /** see #zddCSPCacheDiv */
    public final static int ZDD_GRAPH_CACHE_DIV = 2; /** see #zddGraphCacheDiv */




	/** how small the unary (subset/change)  cache is, range: 2-8 */
	public static int zddUnaryCacheDiv = ZDD_UNARY_CACHE_DIV;

	/** how small the binary (set operations)  cache is, range: 2-8 */
	public static int zddBinaryCacheDiv = ZDD_BINARY_CACHE_DIV;

	/** how small the unate (mul/div)  cache is, range: 2-8 */
	public static int zddUnateCacheDiv = ZDD_UNATE_CACHE_DIV;

	/** how small the CSP (restrict/exclude) cache is, range: 2-8 */
	public static int zddCSPCacheDiv = ZDD_CSP_CACHE_DIV;


	/** how small the Graph cache is, range: 2-8 */
	public static int zddGraphCacheDiv = ZDD_GRAPH_CACHE_DIV;




	// --------- NodeTable.java
	/** we wont grow the table with more than this amount, range: 10k-200k */
	public static int maxNodeIncrease = DEFAULT_MAX_NODE_INCREASE;



	/**
	 * if less than this precent nodes are free, we will garbage collect,
	 * or grow the nodetable, range: 5-40. [this constant is tested against MAX_NODE_FREE ]
	 */
	public static int minFreeNodesProcent = DEFAULT_MIN_FREE_NODES_PROCENT;



	/** maximum number of nodes we that must be free, range: same as MAX_NODE_INCREASE?? */
	public static int maxNodeFree = DEFAULT_MAX_NODE_FREE;






	// -- SimpleCache.java
	/** max number of cache grows allowed, rang: 0-15 */
	public static int maxSimplecacheGrows = DEFAULT_MAX_SIMPLECACHE_GROWS;

	/** min hitrate needed to grow cache (in %), range: 10-99 */
	public static int minSimplecacheHitrateToGrow = DEFAULT_MIN_SIMPLECACHE_HITRATE_TO_GROW;

	/** min access load before we consider the hitrate above valid, range: 1-99 */
	public static int minSimplecacheAccessToGrow = DEFAULT_MIN_SIMPLECACHE_ACCESS_TO_GROW;





	// -- Cache.java
	/** max number of grows for the Cache, range: 0-10 */
	public final static int maxCacheGrows = DEFAULT_MAX_CACHE_GROWS;

	/** loadfactor required for Cache to grow, rang: 50-99 */
	public final static int minCacheLoadfactorToGrow = DEFAULT_MIN_CACHE_LOADFACTOR_TO_GROW;


	// -- CacheEntry.java

	/** above this number of hits, a cached value cannot be overwritten, range: 8-32 */
	public static byte cacheentryStickyHits = DEFAULT_CACHEENTRY_STICKY_HITS;

}
