package jdd.bdd;

import jdd.util.Allocator;
import jdd.util.Array;
import jdd.util.Configuration;
import jdd.util.JDDConsole;
import jdd.util.math.Digits;

/** Cache for int->double. based on simple cache */

// XXX: todo: we need a more soft MAX_HITRATE, that is, if the load-rate is very high,
//            then we should allow even a hitrate about 30-35% to trigger a grow!

// XXX: our good_hash() is not that good :(

public final class DoubleCache extends CacheBase {
  private int[] in;
  private double[] out;

  public int hash_value;
  public double answer;

  private int cache_bits, shift_bits, cache_size, cache_mask;
  private int possible_bins_count, num_clears, num_partial_clears, num_grows;
  private long num_access, partial_count, partial_kept;
  private long hit,
      miss,
      last_hit,
      last_access; // cache hits and misses, hit/access-count since last grow

  /**
   * the arguments are: (size of elements, number of members. number of members that also are BDD
   * nodes)
   */
  public DoubleCache(String name, int size) {
    super(name);

    this.cache_bits = (size < 32) ? 5 : Digits.closest_log2(size); // min size 32
    this.shift_bits = 32 - this.cache_bits; // w-n, where w is the machine word size..
    this.cache_size = (1 << cache_bits);
    this.cache_mask = cache_size - 1;

    num_grows = 0;
    num_access = 0;
    hit = miss = last_hit = last_access = 0;
    partial_count = partial_kept = 0;

    this.possible_bins_count = 0;
    this.num_clears = num_partial_clears = 0;

    in = Allocator.allocateIntArray(cache_size);
    out = Allocator.allocateDoubleArray(cache_size);
    Array.set(in, -1);
  }

  /** the _real_ size of the cache. it is probably higher than what the user requested */
  public int getSize() {
    return cache_size;
  }

  /* return the amount of internally allocated memory in bytes */
  public long getMemoryUsage() {
    long ret = 0;
    if (in != null) ret += in.length * 4;
    if (out != null) ret += out.length * 4;
    return ret;
  }

  /**
   * see if we are allowed to grow this cache. We grow the cache if (num_grows <
   * MAX_SIMPLECACHE_GROWS) and the hit-rate since the last grow is larger than
   * MIN_SIMPLECACHE_HITRATE_TO_GROW.
   */
  private boolean may_grow() {
    if (num_grows < Configuration.maxSimplecacheGrows) {
      long acs = (num_access - last_access);

      // only when we have "MIN_SIMPLECACHE_ACCESS_TO_GROW %" or more access', we have enough
      // information to decide
      // whether we can grow cache or not (beside, if acs == 0, we will get a div by 0 below :)
      if ((acs * 100) < cache_size * Configuration.minSimplecacheAccessToGrow) return false;

      // compute hitrate (in procent) since the LAST grow, not the overall hitrate
      int rate = (int) (((hit - last_hit) * 100.0) / acs);

      if (rate > Configuration.minSimplecacheHitrateToGrow) {
        // store information needed to compute the next after-last-grow-hitrate
        last_hit = hit;
        last_access = num_access;

        // register a grow and return true
        num_grows++;
        return true;
      }
    }
    return false;
  }

  // ---[ these operations just clean the cache ] ---------------------------------

  /** just wipe the cache */
  public void invalidate_cache() {
    Array.set(in, -1);
    possible_bins_count = 0;
    num_clears++;
  }

  /** try to grow the cache. if unable, it will just wipe the cache */
  public void free_or_grow() {
    if (may_grow()) grow_and_invalidate_cache();
    else invalidate_cache();
  }

  /** grow the cache and invalidate everything [since the hash function hash chagned] */
  private void grow_and_invalidate_cache() {
    cache_bits++;
    shift_bits--;
    cache_size = 1 << cache_bits;
    cache_mask = cache_size - 1;

    in = null;
    in = Allocator.allocateIntArray(cache_size);
    out = null;
    out = Allocator.allocateDoubleArray(cache_size);
    Array.set(in, -1);
    possible_bins_count = 0;
    num_clears++;
  }

  // ---[ these operations clean only invalid nodes ] ----------------------

  /**
   * either _partially_ wipe the cache or try to grow it.
   *
   * <p>XXX: at the moment, if cache is grown all current data is lost
   *
   * @see #free_or_grow
   */
  public void free_or_grow(NodeTable nt) {
    if (may_grow())
      grow_and_invalidate_cache(); // no way to partially invalidate, as the size and thus the
                                   // hashes chagnes
    else invalidate_cache(nt);
  }

  /**
   * removes the elements that are garbage collected. this is where the "bdds" variable in
   * constructor is used.
   */
  public void invalidate_cache(NodeTable nt) {
    if (possible_bins_count == 0) return;
    num_partial_clears++;

    int ok = 0;

    for (int i = 0; i < cache_size; i++)
      if (in[i] == -1 || !nt.isValid(in[i])) in[i] = -1;
      else ok++;

    partial_count += cache_size; // for showStats
    partial_kept += ok; // for showStats
    possible_bins_count = ok; // at this point ok = exact current bin-count
  }

  // -----------------------------------------------------------------------------

  /** this is the _correct_ way to insert something into the cache. format: (key1->value) */
  public void insert(int hash, int key1, double value) {
    possible_bins_count++;
    in[hash] = key1;
    out[hash] = value;
  }

  // -----------------------------------------------------------------------------

  /**
   * lookup the element associated with (a) returns true if element found (stored in
   * SimpleCache.answer) returns false if element not found. user should copy the hash value from
   * SimpleCache.hash_value before doing any more cache-operations!
   */
  public final boolean lookup(int a) {
    num_access++;
    int hash = a & cache_mask;
    if (in[hash] == a) {
      hit++;
      answer = out[hash];
      return true;
    } else {
      miss++;
      hash_value = hash;
      return false;
    }
  }

  // -----[ HASH functions  ] -------------------------

  private final int good_hash(int i) {
    return i & cache_mask; // cant get much better ?
  }

  // -----------------------------------------------------------------------------

  public double computeLoadFactor() { // just see howmany buckts are in use
    int bins = 0;
    for (int i = 0; i < cache_size; i++) if (in[i] != -1) bins++;
    return ((int) (bins * 10000) / cache_size) / 100.0;
  }

  public double computeHitRate() { // hit-rate since the last clear
    if (num_access == 0) return 0;
    return ((int) ((hit * 10000) / (num_access))) / 100.0;
  }

  public long getAccessCount() {
    return num_access;
  }

  public int getCacheSize() {
    return cache_size;
  }

  public int getNumberOfClears() {
    return num_clears;
  }

  public int getNumberOfPartialClears() {
    return num_partial_clears;
  }

  public int getNumberOfGrows() {
    return num_grows;
  }

  // --------------------------------------------------------------

  public void showStats() {
    if (num_access != 0) {
      JDDConsole.out.printf(
          "%s-cache: ld=%.2f%% sz=%s acces=%s clrs=%d/%d ",
          getName(),
          computeLoadFactor(),
          Digits.prettify(cache_size),
          Digits.prettify(num_access),
          num_clears,
          num_partial_clears);

      if (partial_count > 0) {
        double pck = ((int) (10000.0 * partial_kept / partial_count)) / 100.0;
        JDDConsole.out.printf("pclr=%.2f%% ", pck);
      }

      JDDConsole.out.printf("hitr=%.2f%% #grow=%d\n", computeHitRate(), num_grows);
    }
  }
}
