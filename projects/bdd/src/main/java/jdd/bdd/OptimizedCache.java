
// XXX: there is probably a bug here somewhere.
//      when partially cleaning the cache, stupid things will happen



package jdd.bdd;

import jdd.util.Configuration;
import jdd.util.JDDConsole;
import jdd.util.Test;
import jdd.util.math.Digits;


/**
 * SimpleCache + some optimization
 *
 * @see SimpleCache
 * @see DoubleCache
 */

public final class OptimizedCache extends SimpleCache {

	/**
	 * possible_bins_count is the number of possible (at most) entries in the table.
	 * if that number is zero, we wont need to clean the cache
	 */
	protected int possible_bins_count;

	/** we take note every time the cache is partially cleaned, for statistics... */
	protected int num_partial_clears;

	/** more statistics... */
	protected long partial_count, partial_kept, partial_given_up;

	/** the number of access when the last GC was done */
	private long access_last_gc;

	/** the number of garbage collection we have seen without the cache being used */
	private int cache_not_used_count;



	public OptimizedCache(String name, int size, int members, int bdds) {
		super(name, size, members, bdds);

		Test.check(bdds <= 3, "BDD members cannot be more than 3 for this type of cache!");

		partial_count = partial_kept= 0;
		possible_bins_count = 0;
		num_partial_clears = 0;

		access_last_gc = 0;
		cache_not_used_count = 0;
		partial_given_up = 0;
	}


	/**
	 * If this function returns true, we should wipe the cache entirely instead
	 * of a partial clean
	 */
	protected boolean shouldWipeUnusedCache() {
		// here is how it works: if the cache havent been accessed in the last
		// "Configuration.MAX_KEEP_UNUSED_PARTIAL_CACHE" garbage collection, then we
		// will wipe its contecnt since partial clean costs too much

		if(access_last_gc == num_access)  cache_not_used_count++;
		else cache_not_used_count = 0;

		access_last_gc = num_access;

		return (cache_not_used_count > Configuration.MAX_KEEP_UNUSED_PARTIAL_CACHE);
	}

	// ---[ these operations just clean the cache ] ---------------------------------

	public void invalidate_cache() {
		if(possible_bins_count != 0) {
			super.invalidate_cache();
			possible_bins_count = 0;
		}
	}


	protected void grow_and_invalidate_cache() {
		super.grow_and_invalidate_cache();
		possible_bins_count = 0;
	}

	// ---[ these operations clean only invalid nodes ] ----------------------



	/**
	 * removes the elements that are garbage collected.
	 * this is where the "bdds" variable in constructor is used.
	 */
	public void invalidate_cache(NodeTable nt) {

		// sanity check
		if(bdds < 1 ) {
			Test.check(false, "Cannot partiall clean a non-bdd cache!");
		}


		// if it is empty, no need to invalidate it?
		if(possible_bins_count == 0) return;


		// is itreally so smart to do a partial clear??
		if( shouldWipeUnusedCache() ) {
			partial_given_up++;
			invalidate_cache();
			return;
		}


		// yes, do a partial cache clear
		int ok = 0; // "ok" is the number of valid cache entries
		if(bdds == 3)  ok = partial_clean3(nt);
		else if(bdds == 2)  ok = partial_clean2(nt);
		else if(bdds == 1)  ok = partial_clean1(nt);

		num_partial_clears++;
		partial_count += cache_size;	// for showStats
		partial_kept  += ok;			// for showStats
		possible_bins_count = ok;		// at this point ok = exact current bin-count
	}


	// -----------------------------------------------------------------------------
	// the partial clean stuff is divided into smaller function to help JVM
	private final int partial_clean3(NodeTable nt) {
		int ok = 0;
		for(int i = cache_size; i != 0; ) {
				i--;
			if( !isValid(i) || !nt.isValid( getIn(i,1) ) || !nt.isValid( getIn(i,2) ) || !nt.isValid( getIn(i,3) ) || !nt.isValid( getOut(i) ) )
				invalidate(i);
			else ok++;
		}
		return ok;
	}

	private final int partial_clean2(NodeTable nt) {
		int ok = 0;
		for(int i = cache_size; i != 0; ) {
			i--;
			if( !isValid(i) || !nt.isValid( getIn(i,1) ) || !nt.isValid( getIn(i,2) ) || !nt.isValid( getOut(i) ) )
				invalidate(i);
			else ok++;
		}
		return ok;
	}

	private final int partial_clean1(NodeTable nt) {
		int ok = 0;
		for(int i = cache_size; i != 0; ) {
			i--;
			if( !isValid(i) || !nt.isValid( getIn(i,1) ) || !nt.isValid( getOut(i) ) )
				invalidate(i);
			else ok++;
		}
		return ok;
	}

	// -----------------------------------------------------------------------------

	public void insert(int hash, int key1, int value) {
		super.insert(hash, key1, value);
		possible_bins_count++;
	}

	public void insert(int hash, int key1, int key2, int value) {
		super.insert(hash, key1, key2, value);
		possible_bins_count++;
	}

	public void insert(int hash, int key1, int key2, int key3, int value) {
		super.insert(hash, key1, key2, key3, value);
		possible_bins_count++;
	}

	// --------------------------------------------------------------
	public int getNumberOfPartialClears() {
		return num_partial_clears;
	}

	// --------------------------------------------------------------

	public void showStats() {
		if(num_access != 0) {
			JDDConsole.out.printf(
				"%s-cache: ld=%.2f%% sz=%s acces=%s clrs=%d/%d ",
				getName(), computeLoadFactor(), Digits.prettify(cache_size),
				Digits.prettify(num_access), num_clears, num_partial_clears);

			if(partial_count > 0) {
				double pck = ((int)(10000.0 * partial_kept / partial_count)) / 100.0;
				JDDConsole.out.printf("pclr=%.2f%% ", pck);
			}

			if(partial_given_up > 0) {
				JDDConsole.out.printf("giveup=%d ", partial_given_up);
			}
			JDDConsole.out.printf("hitr=%.2f%% #grow=%d\n", computeHitRate(), num_grows);
		}
	}
}
