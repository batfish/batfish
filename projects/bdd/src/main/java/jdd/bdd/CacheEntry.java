package jdd.bdd;

import jdd.util.Configuration;

/**
 * A cache entry used by Cache, but not SimpleCache. we try cache-entry saturation by using hit()
 * and save(), but this doesnt seem to work ...
 *
 * @see SimpleCache
 */
public final class CacheEntry {

  public int op1, op2, ret, found, overwrite;
  // public byte type, hits;
  public int type;
  public byte hits;

  public CacheEntry() {
    op1 = -1;
    hits = 0;
    found = 0;
  } // empty position

  public final boolean invalid() {
    return op1 == -1;
  }

  public final void clear() {
    op1 = -1;
    hits = 0;
  }

  /** register a hit */
  public final int hit() {
    found++;
    if (hits < 127) hits++;
    return ret;
  }

  /** returns true if this position is available, false if it should not be overwritten */
  public final boolean save() {
    if (op1 != -1) overwrite++;
    if (hits > Configuration.cacheentryStickyHits) return false;
    hits = 0;
    return true;
  }

  /** saturate the counter immediately */
  public final void saturate() {
    hits = Configuration.cacheentryStickyHits;
  }

  // public final int  hit() {found++; return ret; }
  // public final boolean save() { return true;}
}
