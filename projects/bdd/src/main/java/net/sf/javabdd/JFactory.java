/*
 * Note: We obtained permission from the author of Javabdd, John Whaley, to use
 * the library with Batfish under the MIT license. The email exchange is included
 * in LICENSE.email file.
 *
 * MIT License
 *
 * Copyright (c) 2013-2017 John Whaley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package net.sf.javabdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntSet;
import com.carrotsearch.hppc.IntStack;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is a 100% Java implementation of the BDD factory.
 *
 * <p>It was originally authored by John Whaley, and has since been heavily modified and improved by
 * the Batfish Authors.
 */
public class JFactory extends BDDFactory implements Serializable {
  private static final Logger LOGGER = LogManager.getLogger(JFactory.class);

  /** Whether to maintain (and in some cases print) statistics about the cache use. */
  private static final boolean CACHESTATS = false;

  /** A cache of BDDImpls that have been freed and may now be reused. */
  private transient BDDImpl[] _bddReuse;

  /** Number of valid entries in {@link #_bddReuse}. */
  private transient int _bddReuseSize;

  /** The limit on the size of {@link #_bddReuse}. */
  private static final int BDD_REUSE_LIMIT = 1024;

  /** The number of BDDImpl objects reused since the last garbage collection. */
  private transient long reusedBDDs;

  /**
   * Whether to flush (clear completely) the cache when live BDD nodes are garbage collected. If
   * {@code false}, the cache will be attempted to be cleaned and maintain existing valid cache
   * entries.
   */
  private static final boolean FLUSH_CACHE_ON_GC = false;

  /**
   * If set, assertions will be made on BDD internal computations. Used in developing the factory.
   */
  private static final boolean VERIFY_ASSERTIONS = false;

  protected JFactory() {
    supportSet = new int[0];
    bddrefstack = new int[4096];
    bddrefstackTop = 0;
    _bddReuse = new BDDImpl[BDD_REUSE_LIMIT];
    _bddReuseSize = 0;
  }

  @Serial
  private void readObject(java.io.ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    supportSet = new int[0];
    bddrefstack = new int[4096];
    bddrefstackTop = 0;
    _bddReuse = new BDDImpl[BDD_REUSE_LIMIT];
    _bddReuseSize = 0;
    quantvarset = new int[bddvarnum];
  }

  public static BDDFactory init(int nodenum, int cachesize) {
    BDDFactory f = new JFactory();
    f.initialize(nodenum, cachesize);
    return f;
  }

  @Override
  public long runGC() {
    long nodenum = getNodeNum();
    bdd_gbc();
    return nodenum - getNodeNum();
  }

  /** The total number of BDDs ever created. */
  private long madeBDDs;

  /** The total number of BDDs ever freed. */
  private long freedBDDs;

  @Override
  public long numOutstandingBDDs() {
    return madeBDDs - freedBDDs;
  }

  /** Private helper function to create BDD objects. */
  private BDDImpl makeBDD(int id) {
    madeBDDs++;
    if (_bddReuseSize > 0) {
      BDDImpl ret = _bddReuse[--_bddReuseSize];
      reusedBDDs++;
      ret._index = id;
      bdd_addref(id);
      return ret;
    }
    return new BDDImpl(id);
  }

  /** Wrapper for the BDD index number used internally in the representation. */
  protected class BDDImpl extends BDD {
    int _index;

    BDDImpl(int index) {
      _index = index;
      bdd_addref(_index);
    }

    @Override
    public BDDFactory getFactory() {
      return JFactory.this;
    }

    @Override
    public boolean isConstant() {
      return ISCONST(_index);
    }

    @Override
    public boolean isZero() {
      return _index == BDDZERO;
    }

    @Override
    public boolean isOne() {
      return _index == BDDONE;
    }

    @Override
    public boolean isAnd() {
      int index = _index;
      while (!ISCONST(index)) {
        if (LOW(index) != BDDZERO) {
          return false;
        }
        index = HIGH(index);
      }
      return index == BDDONE;
    }

    @Override
    public boolean isNor() {
      int index = _index;
      while (!ISCONST(index)) {
        if (HIGH(index) != BDDZERO) {
          return false;
        }
        index = LOW(index);
      }
      return index == BDDONE;
    }

    @Override
    public boolean isVar() {
      return !ISCONST(_index) && LOW(_index) == BDDZERO && HIGH(_index) == BDDONE;
    }

    @Override
    public boolean isAssignment() {
      return bdd_isAssignment(_index);
    }

    @Override
    public int var() {
      return bdd_var(_index);
    }

    @Override
    public BDD high() {
      return makeBDD(HIGH(_index));
    }

    @Override
    public BDD low() {
      return makeBDD(LOW(_index));
    }

    @Override
    public BDD id() {
      return makeBDD(_index);
    }

    @Override
    public BDD not() {
      return makeBDD(bdd_not(_index));
    }

    @Override
    public BDD notEq() {
      int result = bdd_not(_index);
      bdd_delref(_index);
      bdd_addref(result);
      _index = result;
      return this;
    }

    @Override
    public BDD ite(BDD thenBDD, BDD elseBDD) {
      int x = _index;
      int y = ((BDDImpl) thenBDD)._index;
      int z = ((BDDImpl) elseBDD)._index;
      return makeBDD(bdd_ite(x, y, z));
    }

    @Override
    public BDD iteWith(BDD thenBDD, BDD elseBDD) {
      int x = _index;
      int y = ((BDDImpl) thenBDD)._index;
      int z = ((BDDImpl) elseBDD)._index;
      int result = bdd_ite(x, y, z);
      // Update this BDD to point to the result
      bdd_delref(_index);
      // Free the then and else BDDs, avoiding double-free if they're the same object
      if (this != thenBDD) {
        thenBDD.free();
      }
      if (this != elseBDD && thenBDD != elseBDD) {
        elseBDD.free();
      }
      bdd_addref(result);
      _index = result;
      return this;
    }

    @Override
    public BDD relprod(BDD that, BDD var) {
      int x = _index;
      int y = ((BDDImpl) that)._index;
      int z = ((BDDImpl) var)._index;
      return makeBDD(bdd_relprod(x, y, z));
    }

    @Override
    public BDD compose(BDD g, int var) {
      int x = _index;
      int y = ((BDDImpl) g)._index;
      return makeBDD(bdd_compose(x, y, var));
    }

    @Override
    public BDD veccompose(BDDPairing pair) {
      int x = _index;
      return makeBDD(bdd_veccompose(x, (bddPair) pair));
    }

    @Override
    public BDD constrain(BDD that) {
      int x = _index;
      int y = ((BDDImpl) that)._index;
      return makeBDD(bdd_constrain(x, y));
    }

    /**
     * Given the index of the result of an operation, either changes {@code this} {@link BDD} (when
     * {@code makeNew} is false) or creates a new BDD ({@code makeNew} is true).
     */
    private BDD eqOrNew(int result, boolean makeNew) {
      if (makeNew) {
        return makeBDD(result);
      }
      if (_index != result) {
        // Swap both the index and the reference to the new value.
        // This would be a no-op in the else branch.
        bdd_delref(_index);
        bdd_addref(result);
        _index = result;
      }
      return this;
    }

    @Override
    BDD exist(BDD var, boolean makeNew) {
      int x = _index;
      int y = ((BDDImpl) var)._index;
      return eqOrNew(bdd_exist(x, y), makeNew);
    }

    @Override
    public boolean testsVars(BDD var) {
      int x = _index;
      int y = ((BDDImpl) var)._index;
      return bdd_testsVars(x, y);
    }

    @Override
    public BDD project(BDD var) {
      int x = _index;
      int y = ((BDDImpl) var)._index;
      return makeBDD(bdd_project(x, y));
    }

    @Override
    public BDD forAll(BDD var) {
      int x = _index;
      int y = ((BDDImpl) var)._index;
      return makeBDD(bdd_forall(x, y));
    }

    @Override
    public BDD unique(BDD var) {
      int x = _index;
      int y = ((BDDImpl) var)._index;
      return makeBDD(bdd_unique(x, y));
    }

    @Override
    public BDD restrict(BDD var) {
      int x = _index;
      int y = ((BDDImpl) var)._index;
      return makeBDD(bdd_restrict(x, y));
    }

    @Override
    public BDD restrictWith(BDD that) {
      int x = _index;
      int y = ((BDDImpl) that)._index;
      int a = bdd_restrict(x, y);
      bdd_delref(x);
      if (this != that) {
        that.free();
      }
      bdd_addref(a);
      _index = a;
      return this;
    }

    @Override
    public BDD simplify(BDD d) {
      int x = _index;
      int y = ((BDDImpl) d)._index;
      return makeBDD(bdd_simplify(x, y));
    }

    @Override
    public BDD support() {
      int x = _index;
      return makeBDD(bdd_support(x));
    }

    @Override
    public boolean andSat(BDD that) {
      if (applycache == null) {
        applycache = FlatCacheI_init(cachesize);
      }
      return andsat_rec(_index, ((BDDImpl) that)._index);
    }

    @Override
    public boolean diffSat(BDD that) {
      if (applycache == null) {
        applycache = FlatCacheI_init(cachesize);
      }
      return diffsat_rec(_index, ((BDDImpl) that)._index);
    }

    @Override
    BDD apply(BDD that, BDDOp opr, boolean makeNew) {
      int x = _index;
      int y = ((BDDImpl) that)._index;
      int z = opr.id;
      return eqOrNew(bdd_apply(x, y, z), makeNew);
    }

    @Override
    public BDD applyWith(BDD that, BDDOp opr) {
      int x = _index;
      int y = ((BDDImpl) that)._index;
      int z = opr.id;
      int a = bdd_apply(x, y, z);
      bdd_delref(x);
      if (this != that) {
        that.free();
      }
      bdd_addref(a);
      _index = a;
      return this;
    }

    @Override
    public BDD applyAll(BDD that, BDDOp opr, BDD var) {
      int x = _index;
      int y = ((BDDImpl) that)._index;
      int z = opr.id;
      int a = ((BDDImpl) var)._index;
      return makeBDD(bdd_appall(x, y, z, a));
    }

    @Override
    public BDD applyEx(BDD that, BDDOp opr, BDD var) {
      int x = _index;
      int y = ((BDDImpl) that)._index;
      int z = opr.id;
      int a = ((BDDImpl) var)._index;
      return makeBDD(bdd_appex(x, y, z, a));
    }

    @Override
    public BDD transform(BDD rel, BDDPairing pair) {
      int x = _index;
      int y = ((BDDImpl) rel)._index;

      return makeBDD(bdd_transform(x, y, (bddPair) pair));
    }

    @Override
    public BDD applyUni(BDD that, BDDOp opr, BDD var) {
      int x = _index;
      int y = ((BDDImpl) that)._index;
      int z = opr.id;
      int a = ((BDDImpl) var)._index;
      return makeBDD(bdd_appuni(x, y, z, a));
    }

    @Override
    public BDD satOne() {
      int x = _index;
      return makeBDD(bdd_satone(x));
    }

    @Override
    public BDD fullSatOne() {
      int x = _index;
      return makeBDD(bdd_fullsatone(x));
    }

    @Override
    public BitSet minAssignmentBits() {
      return bdd_minassignmentbits(_index);
    }

    @Override
    public BDD randomFullSatOne(int seed) {
      int x = _index;
      return makeBDD(bdd_randomfullsatone(x, seed));
    }

    @Override
    public BDD satOne(BDD var, boolean pol) {
      int x = _index;
      int y = ((BDDImpl) var)._index;
      int z = pol ? 1 : 0;
      return makeBDD(bdd_satoneset(x, y, z));
    }

    @Override
    public BDD replace(BDDPairing pair) {
      int x = _index;
      return makeBDD(bdd_replace(x, (bddPair) pair));
    }

    @Override
    public BDD replaceWith(BDDPairing pair) {
      int x = _index;
      int y = bdd_replace(x, (bddPair) pair);
      bdd_delref(x);
      bdd_addref(y);
      _index = y;
      return this;
    }

    @Override
    public int nodeCount() {
      return bdd_nodecount(_index);
    }

    @Override
    public double pathCount() {
      return bdd_pathcount(_index);
    }

    @Override
    public double satCount() {
      return bdd_satcount(_index).doubleValue();
    }

    @Override
    public int[] varProfile() {
      int x = _index;
      return bdd_varprofile(x);
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (!(o instanceof BDDImpl)) {
        return false;
      }
      BDDImpl that = (BDDImpl) o;
      return _index == that._index;
    }

    @Override
    public int hashCode() {
      return _index;
    }

    @Override
    public void free() {
      bdd_delref(_index);
      _index = INVALID_BDD;
      ++freedBDDs;
      if (_bddReuseSize < BDD_REUSE_LIMIT) {
        _bddReuse[_bddReuseSize++] = this;
      }
    }
  }

  private static final int REF_MASK = 0xFFC00000;
  private static final int MARK_MASK = 0x00200000;
  private static final int LEV_MASK = 0x001FFFFF;
  private static final int MAXVAR = LEV_MASK;
  private static final int INVALID_BDD = -1;

  private static final int REF_INC = 0x00400000;

  private static final int offset__refcou_and_level = 0;
  private static final int offset__low = 1;
  private static final int offset__high = 2;
  private static final int offset__next = 3;
  private static final int __node_size = 4;

  /**
   * The maximum number of BDD nodes that can {@link #bddnodesize} can ever be measured is the
   * largest array that can be allocated.
   */
  private static final int MAX_NODESIZE = Integer.highestOneBit(Integer.MAX_VALUE / __node_size);

  private boolean HASREF(int node) {
    boolean r = (bddnodes[node * __node_size + offset__refcou_and_level] & REF_MASK) != 0;
    return r;
  }

  private void SETMAXREF(int node) {
    bddnodes[node * __node_size + offset__refcou_and_level] |= REF_MASK;
  }

  private void CLEARREF(int node) {
    bddnodes[node * __node_size + offset__refcou_and_level] &= ~REF_MASK;
  }

  private void INCREF(int node) {
    if ((bddnodes[node * __node_size + offset__refcou_and_level] & REF_MASK) != REF_MASK) {
      bddnodes[node * __node_size + offset__refcou_and_level] += REF_INC;
    }
  }

  private void DECREF(int node) {
    int rc = bddnodes[node * __node_size + offset__refcou_and_level] & REF_MASK;
    if (rc != REF_MASK && rc != 0) {
      bddnodes[node * __node_size + offset__refcou_and_level] -= REF_INC;
    }
  }

  private int GETREF(int node) {
    return bddnodes[node * __node_size + offset__refcou_and_level] >>> 22;
  }

  private int LEVEL(int node) {
    return bddnodes[node * __node_size + offset__refcou_and_level] & LEV_MASK;
  }

  private int LEVELANDMARK(int node) {
    return bddnodes[node * __node_size + offset__refcou_and_level] & (LEV_MASK | MARK_MASK);
  }

  private void SETLEVEL(int node, int val) {
    if (VERIFY_ASSERTIONS) {
      _assert(val == (val & LEV_MASK));
    }
    bddnodes[node * __node_size + offset__refcou_and_level] &= ~LEV_MASK;
    bddnodes[node * __node_size + offset__refcou_and_level] |= val;
  }

  private void SETLEVELANDMARK(int node, int val) {
    if (VERIFY_ASSERTIONS) {
      _assert(val == (val & (LEV_MASK | MARK_MASK)));
    }
    bddnodes[node * __node_size + offset__refcou_and_level] &= ~(LEV_MASK | MARK_MASK);
    bddnodes[node * __node_size + offset__refcou_and_level] |= val;
  }

  private void SETMARK(int n) {
    bddnodes[n * __node_size + offset__refcou_and_level] |= MARK_MASK;
  }

  private void UNMARK(int n) {
    bddnodes[n * __node_size + offset__refcou_and_level] &= ~MARK_MASK;
  }

  private boolean MARKED(int n) {
    return (bddnodes[n * __node_size + offset__refcou_and_level] & MARK_MASK) != 0;
  }

  private int LOW(int r) {
    return bddnodes[r * __node_size + offset__low];
  }

  private void SETLOW(int r, int v) {
    bddnodes[r * __node_size + offset__low] = v;
  }

  private int HIGH(int r) {
    return bddnodes[r * __node_size + offset__high];
  }

  private void SETHIGH(int r, int v) {
    bddnodes[r * __node_size + offset__high] = v;
  }

  private int HASH(int r) {
    return bddhash[r];
  }

  private void SETHASH(int r, int v) {
    bddhash[r] = v;
  }

  private int NEXT(int r) {
    return bddnodes[r * __node_size + offset__next];
  }

  private void SETNEXT(int r, int v) {
    bddnodes[r * __node_size + offset__next] = v;
  }

  private int VARr(int n) {
    return LEVELANDMARK(n);
  }

  private void SETVARr(int n, int val) {
    SETLEVELANDMARK(n, val);
  }

  private static void _assert(boolean b) {
    if (!b) {
      throw new InternalError();
    }
  }

  private abstract static class BddCacheData {
    protected BddCacheData() {
      a = -1;
    }

    int a, b, c;
    int hash;
  }

  private static class BddCacheDataI extends BddCacheData {
    int res;
  }

  // a = index, c = operator, value = value.
  private static class BigIntegerBddCacheData extends BddCacheData {
    BigInteger value;
  }

  // a = operator, b = result, c = unused
  private static class MultiOpBddCacheData extends BddCacheData {
    int[] operands;
  }

  /**
   * Flat int[]-based cache specialized for ITE operations. Each entry stores [f, g, h, res] in 4
   * consecutive ints (the operation code is always bddop_ite, so it's not stored).
   */
  private static final class FlatCacheIte implements Serializable {
    static final int ENTRY_SHIFT = 2; // log2(4)
    int[] data;
    int mask;

    FlatCacheIte(int numEntries) {
      mask = numEntries - 1;
      data = new int[numEntries << ENTRY_SHIFT];
      for (int i = 0; i < data.length; i += 4) {
        data[i] = -1; // f = -1 means unused
      }
    }

    /** Returns the cached result for key (f, g, h), or -1 if not found. */
    int lookup(int hash, int f, int g, int h, CacheStats stats) {
      int eBase = (hash & mask) << ENTRY_SHIFT;
      if (data[eBase] == f && data[eBase + 1] == g && data[eBase + 2] == h) {
        if (CACHESTATS) {
          stats.opHit++;
        }
        return data[eBase + 3];
      }
      if (CACHESTATS) {
        stats.opMiss++;
      }
      return -1;
    }

    /** Stores a result for key (f, g, h). */
    void store(int hash, int f, int g, int h, int res, CacheStats stats) {
      int eBase = (hash & mask) << ENTRY_SHIFT;
      if (CACHESTATS && data[eBase] != -1) {
        stats.opOverwrite++;
      }
      data[eBase] = f;
      data[eBase + 1] = g;
      data[eBase + 2] = h;
      data[eBase + 3] = res;
    }
  }

  /**
   * Flat int[]-based cache for int-result operations. Eliminates pointer dereferences by storing
   * entries as contiguous ints: [a, b, c, res] tuples.
   */
  private static final class FlatCacheI implements Serializable {
    static final int ENTRY_SHIFT = 2; // log2(4) - 4 ints per entry
    int[] data;
    int mask;

    FlatCacheI(int numEntries) {
      mask = numEntries - 1;
      data = new int[numEntries << ENTRY_SHIFT];
      // Initialize all entries as unused (a = -1)
      for (int i = 0; i < data.length; i += 4) {
        data[i] = -1;
      }
    }

    /** Returns the cached result for key (a, b, c), or -1 if not found. */
    int lookup(int hash, int a, int b, int c, CacheStats stats) {
      int eBase = (hash & mask) << ENTRY_SHIFT;
      if (data[eBase] == a && data[eBase + 1] == b && data[eBase + 2] == c) {
        if (CACHESTATS) {
          stats.opHit++;
        }
        return data[eBase + 3];
      }
      if (CACHESTATS) {
        stats.opMiss++;
      }
      return -1;
    }

    /** Stores a result for key (a, b, c). */
    void store(int hash, int a, int b, int c, int res, CacheStats stats) {
      int eBase = (hash & mask) << ENTRY_SHIFT;
      if (CACHESTATS && data[eBase] != -1) {
        stats.opOverwrite++;
      }
      data[eBase] = a;
      data[eBase + 1] = b;
      data[eBase + 2] = c;
      data[eBase + 3] = res;
    }
  }

  private static class BddCache {
    BddCacheData[] table;
    int tablesize;
    int tablemask; // tablesize - 1, for power-of-2 sized tables

    /**
     * Returns the number of used entries in this cache.
     *
     * <p>Slow. Should only be used in debugging contexts.
     */
    private int used() {
      // Array lengths in Java must be representable by a signed int.
      return (int) Arrays.stream(table).parallel().filter(e -> e.a != -1).count();
    }
  }

  private static class JavaBDDException extends BDDException {
    /** Version ID for serialization. */
    JavaBDDException(int x) {
      super(errorstrings[-x]);
    }
  }

  private static final int BDDONE = 1;
  private static final int BDDZERO = 0;

  private boolean bddrunning; /* Flag - package initialized */
  private int bdderrorcond; /* Some error condition */
  private int bddnodesize; /* Number of allocated nodes (power of 2) */
  private int bddnodemask; /* bddnodesize - 1, for fast hash masking */
  private int[] bddnodes; /* All of the bdd nodes */
  private int[] bddhash; /* Hash table buckets (separate from nodes for cache locality) */
  private int bddfreepos; /* First free node */
  private int bddfreenum; /* Number of free nodes */
  private int bddproduced; /* Number of new nodes ever produced */
  private int bddvarnum; /* Number of defined BDD variables */
  private transient int[] bddrefstack; /* BDDs referenced during the current computation. */
  private transient int bddrefstackTop; /* Next free position in bddrefstack. */
  private int[] bddvar2level; /* Variable -> level table */
  private int[] bddlevel2var; /* Level -> variable table */
  private boolean bddresized; /* Flag indicating a resize of the nodetable */

  private int minfreenodes = 20;

  /*=== PRIVATE KERNEL VARIABLES =========================================*/

  private int[] bddvarset; /* Set of defined BDD variables */
  private int gbcollectnum; /* Number of garbage collections */
  private int cachesize; /* Size of the operator caches */
  private long gbcclock; /* Clock ticks used in GBC */

  /** Total millis used in resizing */
  private long sumResizeTime;

  private static final int BDD_MEMORY = -1; /* Out of memory */
  private static final int BDD_VAR = -2; /* Unknown variable */
  private static final int BDD_RANGE = -3;
  /* Variable value out of range (not in domain) */
  private static final int BDD_DEREF = -4;
  /* Removing external reference to unknown node */
  private static final int BDD_RUNNING = -5;
  /* Called bdd_init() twice whithout bdd_done() */
  private static final int BDD_FILE = -6; /* Some file operation failed */
  private static final int BDD_FORMAT = -7; /* Incorrect file format */
  private static final int BDD_ORDER = -8;
  /* Vars. not in order for vector based functions */
  private static final int BDD_BREAK = -9; /* User called break */
  private static final int BDD_VARNUM = -10;
  /* Tried to set max. number of nodes to be fewer */
  /* than there already has been allocated */
  private static final int BDD_OP = -12; /* Unknown operator */
  private static final int BDD_VARSET = -13; /* Illegal variable set */
  private static final int BDD_VARBLK = -14; /* Bad variable block operation */
  private static final int BDD_DECVNUM = -15;
  /* Trying to decrease the number of variables */
  private static final int BDD_REPLACE = -16;
  /* Replacing to already existing variables */
  private static final int BDD_NODENUM = -17;
  /* Number of nodes reached user defined maximum */
  private static final int BDD_ILLBDD = -18; /* Illegal bdd argument */

  private static final int BVEC_SIZE = -20; /* Mismatch in bitvector size */
  private static final int BVEC_SHIFT = -21;
  /* Illegal shift-left/right parameter */
  private static final int BVEC_DIVZERO = -22; /* Division by zero */

  private static final int BDD_ERRNUM = 24;

  /* Strings for all error mesages */
  private static final String[] errorstrings = {
    "",
    "Out of memory",
    "Unknown variable",
    "Value out of range",
    "Unknown BDD root dereferenced",
    "bdd_init() called twice",
    "File operation failed",
    "Incorrect file format",
    "Variables not in ascending order",
    "User called break",
    "Mismatch in size of variable sets",
    "Cannot allocate fewer nodes than already in use",
    "Unknown operator",
    "Illegal variable set",
    "Bad variable block operation",
    "Trying to decrease the number of variables",
    "Trying to replace with variables already in the bdd",
    "Number of nodes reached user defined maximum",
    "Unknown BDD - was not in node table",
    "Bad size argument",
    "Mismatch in bitvector size",
    "Illegal shift-left/right parameter",
    "Division by zero"
  };

  /*=== OTHER INTERNAL DEFINITIONS =======================================*/

  /**
   * Multiplicative hash combining two ints. Uses Knuth's constant and a secondary mixing constant
   * with a finalization XOR shift for good distribution with power-of-2 table sizes.
   */
  private static int PAIR(int a, int b) {
    int h = a * 0x9e3779b9 + b * 0x517cc1b7;
    return h ^ (h >>> 16);
  }

  /** Multiplicative hash combining three ints. See {@link #PAIR(int, int)}. */
  private static int TRIPLE(int a, int b, int c) {
    int h = a * 0x9e3779b9 + b * 0x517cc1b7 + c * 0x6c62272e;
    return h ^ (h >>> 16);
  }

  private int NODEHASH(int lvl, int l, int h) {
    int hash = lvl + l * 1183477 + h * 1296043;
    hash ^= (hash >>> 16);
    hash *= 0x45d9f3b;
    hash ^= (hash >>> 16);
    return hash & bddnodemask;
  }

  @Override
  public BDD zero() {
    return makeBDD(BDDZERO);
  }

  @Override
  public BDD one() {
    return makeBDD(BDDONE);
  }

  private int bdd_ithvar(int var) {
    if (var < 0 || var >= bddvarnum) {
      bdd_error(BDD_VAR);
      return BDDZERO;
    }

    return bddvarset[var * 2];
  }

  private int bdd_nithvar(int var) {
    if (var < 0 || var >= bddvarnum) {
      bdd_error(BDD_VAR);
      return BDDZERO;
    }

    return bddvarset[var * 2 + 1];
  }

  private int bdd_varnum() {
    return bddvarnum;
  }

  private static int bdd_error(int v) {
    throw new JavaBDDException(v);
  }

  private static boolean ISZERO(int r) {
    return r == BDDZERO;
  }

  private static boolean ISONE(int r) {
    return r == BDDONE;
  }

  private static boolean ISCONST(int r) {
    // return r == BDDZERO || r == BDDONE;
    return r < 2;
  }

  private void CHECK(int r) {
    if (!bddrunning) {
      bdd_error(BDD_RUNNING);
    } else if (r < 0 || r >= bddnodesize) {
      bdd_error(BDD_ILLBDD);
    } else if (VERIFY_ASSERTIONS && r >= 2 && LOW(r) == INVALID_BDD) {
      bdd_error(BDD_ILLBDD);
    }
  }

  private int bdd_var(int root) {
    CHECK(root);
    if (root < 2) {
      bdd_error(BDD_ILLBDD);
    }

    return bddlevel2var[LEVEL(root)];
  }

  private void checkresize() {
    if (bddresized) {
      bdd_operator_noderesize();
    }
    bddresized = false;
  }

  private static int NOTHASH(int r) {
    return r; // BDD node IDs are sequential; no mixing needed for power-of-2 tables
  }

  private static int APPLYHASH(int l, int r, int op) {
    return TRIPLE(l, r, op);
  }

  private static int MULTIOPHASH(int[] operands, int op) {
    int result = op;
    for (int operand : operands) {
      result = PAIR(result, operand);
    }
    return result;
  }

  private static int RESTRHASH(int r, int var) {
    return PAIR(r, var);
  }

  private static int CONSTRAINHASH(int f, int c) {
    return PAIR(f, c);
  }

  private static int QUANTHASH(int r, int quantid) {
    return PAIR(r, quantid);
  }

  private static int TRANSFORMHASH(int cacheid, int l, int r) {
    return TRIPLE(cacheid, l, r);
  }

  private static int REPLACEHASH(int cacheid, int r) {
    return PAIR(cacheid, r);
  }

  private static int CORRECTIFYHASH(int level, int l, int r) {
    return TRIPLE(level, l, r);
  }

  private static int VECCOMPOSEHASH(int cacheid, int f) {
    return PAIR(cacheid, f);
  }

  private static int COMPOSEHASH(int cacheid, int f, int g) {
    return TRIPLE(cacheid, f, g);
  }

  private static int SATCOUHASH(int r, int miscid) {
    return PAIR(r, miscid);
  }

  private static int PATHCOUHASH(int r, int miscid) {
    return PAIR(r, miscid);
  }

  private static int APPEXHASH(int l, int r, int appexid) {
    return TRIPLE(l, r, appexid);
  }

  private boolean INVARSET(int a) {
    return quantvarset[a] == quantvarsetID; /* unsigned check */
  }

  private boolean INSVARSET(int a) {
    return Math.abs(quantvarset[a]) == quantvarsetID; /* signed check */
  }

  private static final int bddop_and = 0;
  private static final int bddop_xor = 1;
  private static final int bddop_or = 2;
  private static final int bddop_nand = 3;
  private static final int bddop_nor = 4;
  private static final int bddop_imp = 5;
  private static final int bddop_biimp = 6;
  private static final int bddop_diff = 7;
  private static final int bddop_less = 8;
  private static final int bddop_invimp = 9;

  /* Should *not* be used in bdd_apply calls !!! */
  private static final int bddop_not = 10;
  private static final int bddop_simplify = 11;
  private static final int bddop_andsat = 12;
  private static final int bddop_diffsat = 13;
  private static final int bddop_ite = 14;

  @Override
  public BDD onehot(BDD... bdds) {
    if (bdds.length == 0) {
      return makeBDD(BDDZERO);
    }

    // Extract node indices and sort by decreasing level using packed long[] for
    // O(n log n) primitive sort without autoboxing.
    int len = bdds.length;
    long[] keyed = new long[len];
    for (int i = 0; i < len; i++) {
      int id = ((BDDImpl) bdds[i])._index;
      int lvl = LEVEL(id);
      keyed[i] = ((long) (Integer.MAX_VALUE - lvl) << 32) | (id & 0xFFFFFFFFL);
    }
    Arrays.sort(keyed);
    int[] ids = new int[len];
    for (int i = 0; i < len; i++) {
      ids[i] = (int) keyed[i];
    }

    // Build the onehot BDD bottom-up using direct int-level operations,
    // avoiding BDDImpl wrapper creation/free per intermediate step.
    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (itecache == null) {
      int iteSize = Integer.highestOneBit(cachesize - 1) << 1;
      if (iteSize < 16) iteSize = 16;
      itecache = new FlatCacheIte(iteSize);
    }
    if (multiopcache == null) {
      multiopcache = BddCacheMultiOp_init(cachesize);
    }

    INITREF();
    int onehot = BDDZERO;
    int allFalse = BDDONE;
    PUSHREF(onehot);
    PUSHREF(allFalse);

    for (int i = 0; i < len; i++) {
      int bdd = ids[i];
      int newOnehot = ite_rec(bdd, allFalse, onehot);
      SETREF(0, newOnehot);
      onehot = newOnehot;
      // allFalse = allFalse AND NOT bdd = allFalse DIFF bdd
      applyop = bddop_diff;
      int newAllFalse = apply_rec(allFalse, bdd);
      SETREF(1, newAllFalse);
      allFalse = newAllFalse;
    }

    checkresize();
    return makeBDD(onehot);
  }

  @Override
  public BDD andLiterals(BDD... literals) {
    if (literals.length == 0) {
      return makeBDD(BDDONE);
    }
    if (literals.length == 1) {
      return literals[0].id();
    }

    int[] ids = new int[literals.length];
    for (int i = 0; i < literals.length; i++) {
      int id = ((BDDImpl) literals[i])._index;
      if (ISCONST(id)) {
        throw new IllegalArgumentException("Illegal constant " + id);
      }
      ids[i] = id;
    }

    BDDImpl bdd = makeBDD(bdd_andLiterals(ids));
    return bdd;
  }

  private int bdd_andLiterals(int[] literals) {
    assert literals.length > 0; // empty array handled in caller
    INITREF();

    /* build bottom-up, skipping the operator cache at each level since this construction is very cheap (and we don't
     * want to evict a more valuable cache entry. We could consider using the multip cache to cache the entire
     * andLiterals operation.
     */
    int last = -1;
    int lastLevel = -1;
    for (int i = literals.length - 1; i >= 0; i--) {
      int n = literals[i];
      int level = LEVEL(n);
      int var = bddlevel2var[level];
      boolean positive = n == bddvarset[var * 2];
      if (!(positive || n == bddvarset[var * 2 + 1])) {
        throw new IllegalArgumentException(String.format("argument %s is not a literal", i));
      }

      if (last == -1) {
        last = n;
      } else {
        if (level >= lastLevel) {
          throw new IllegalArgumentException("Levels are not strictly increasing");
        }
        PUSHREF(last);
        if (positive) {
          last = bdd_makenode(level, BDDZERO, last);
        } else {
          last = bdd_makenode(level, last, BDDZERO);
        }
        POPREF(1);
      }
      lastLevel = level;
    }
    return last;
  }

  @Override
  public BDD onehotVars(BDD... variables) {
    if (variables.length == 0) {
      return makeBDD(BDDZERO);
    }
    // This function skips the operator cache, since it's so cheap

    // Construct the result bottom-up. Given variable j, we keep track of two formulas:
    //
    //   1. allFalse[j] is the negation of all variables [k>=j] at this level or lower.
    //   2. onehot[j] is the onehot encoding for all variables [k>=j] at this level or lower.
    //
    // Then for the variable i immediately above j in the tree, we can construct
    //
    //     onehot[i] = (i ^ allFalse[j]) v (!i ^ onehot[j])
    //     allFalse[i] = !i ^ allFalse[j]
    //
    int onehot = BDDZERO;
    int allFalse = BDDONE;

    // We only have two live refs: highest computed onehot and allFalse. Just allocate 2 refs
    // and overwrite, rather than push/pop.
    INITREF();
    PUSHREF(onehot);
    PUSHREF(allFalse);
    int lastLevel = Integer.MAX_VALUE;

    for (int i = variables.length - 1; i >= 0; i--) {
      BDD var = variables[i];
      checkArgument(var.isVar(), "Variable %s is not a variable: %s", i, var);
      int id = ((BDDImpl) var)._index;
      int level = LEVEL(id);
      checkArgument(
          level < lastLevel,
          "Levels are not strictly increasing: index %s (level %s) is >= index %s (level %s)",
          i,
          level,
          i + 1,
          lastLevel);
      lastLevel = level;
      onehot = bdd_makenode(level, onehot, allFalse);
      SETREF(0, onehot);
      allFalse = bdd_makenode(level, allFalse, BDDZERO);
      SETREF(1, allFalse);
    }

    return makeBDD(onehot);
  }

  /**
   * Extracts the indices from the input bddOperands as an array. If {@code shortCircuit} is found,
   * the returned array will contain {@code shortCircuit} is the first value. If all values are
   * {@code identity}, the returned array will contain {@code identity} as the first value.
   * Otherwise the returned array will not contain either {@code shortCircuit} or {@code identity},
   * and will be sorted and deduped.
   */
  @VisibleForTesting
  static int[] toIntOperands(Collection<BDD> bddOperands, int identity, int shortCircuit) {
    int[] operands = new int[bddOperands.size()];
    int i = 0;
    for (BDD bdd : bddOperands) {
      int id = ((BDDImpl) bdd)._index;
      if (id == shortCircuit) {
        operands[0] = shortCircuit;
        return operands;
      }
      if (id == identity) {
        continue;
      }
      operands[i++] = id;
    }
    if (i == 0) {
      // all operands were identity
      operands[0] = identity;
      return operands;
    }
    Arrays.sort(operands, 0, i);
    return dedupSorted(operands, i);
  }

  @Override
  public BDD andAll(Collection<BDD> bddOperands, boolean free) {
    if (bddOperands.isEmpty()) {
      return makeBDD(BDDONE);
    }
    if (bddOperands.size() == 1) {
      BDD bdd = bddOperands.iterator().next();
      return free ? bdd : bdd.id();
    }
    if (bddOperands.size() == 2) {
      Iterator<BDD> iter = bddOperands.iterator();
      BDD bdd1 = iter.next();
      BDD bdd2 = iter.next();
      return free ? bdd1.andWith(bdd2) : bdd1.and(bdd2);
    }
    int[] operands = toIntOperands(bddOperands, BDDONE, BDDZERO);
    int ret = ISCONST(operands[0]) ? operands[0] : bdd_andAll(operands);
    if (free) {
      bddOperands.forEach(BDD::free);
    }
    return makeBDD(ret);
  }

  private int bdd_andAll(int[] operands) {
    if (operands.length == 0) {
      return BDDONE;
    } else if (ISZERO(operands[0])) {
      return BDDZERO;
    }
    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (multiopcache == null) {
      multiopcache = BddCacheMultiOp_init(cachesize);
    }

    INITREF();
    int res = andAll_rec(operands);
    checkresize();

    return res;
  }

  @Override
  protected BDD orAll(Collection<BDD> bddOperands, boolean free) {
    if (bddOperands.isEmpty()) {
      return makeBDD(BDDZERO);
    }
    if (bddOperands.size() == 1) {
      BDD bdd = bddOperands.iterator().next();
      return free ? bdd : bdd.id();
    }
    if (bddOperands.size() == 2) {
      Iterator<BDD> iter = bddOperands.iterator();
      BDD bdd1 = iter.next();
      BDD bdd2 = iter.next();
      return free ? bdd1.orWith(bdd2) : bdd1.or(bdd2);
    }
    int[] operands = toIntOperands(bddOperands, BDDZERO, BDDONE);
    int ret = ISCONST(operands[0]) ? operands[0] : bdd_orAll(operands);
    if (free) {
      bddOperands.forEach(BDD::free);
    }
    return makeBDD(ret);
  }

  private int bdd_orAll(int[] operands) {
    if (operands.length == 0) {
      return BDDZERO;
    } else if (ISONE(operands[0])) {
      return BDDONE;
    }
    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (multiopcache == null) {
      multiopcache = BddCacheMultiOp_init(cachesize);
    }

    INITREF();
    int res = orAll_rec(operands);
    checkresize();

    return res;
  }

  private boolean bdd_isAssignment(int r) {
    CHECK(r);
    if (r == BDDONE) {
      return true;
    } else if (r == BDDZERO) {
      return false;
    }

    // If this node is an assignment, exactly one child will be zero at every level except the last,
    // which will be one. If there are no zero children, there are two satisfying paths.
    if (LOW(r) == BDDZERO) {
      return bdd_isAssignment(HIGH(r));
    } else if (HIGH(r) == BDDZERO) {
      return bdd_isAssignment(LOW(r));
    }

    return false;
  }

  private int bdd_not(int r) {
    CHECK(r);

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }

    INITREF();
    int res = not_rec(r);
    checkresize();

    return res;
  }

  private int not_rec(int r) {
    int res;

    if (ISZERO(r)) {
      return BDDONE;
    } else if (ISONE(r)) {
      return BDDZERO;
    }

    int hash = NOTHASH(r);
    int cached = applycache.lookup(hash, r, 0, bddop_not, cachestats);
    if (cached >= 0) return cached;

    PUSHREF(not_rec(LOW(r)));
    PUSHREF(not_rec(HIGH(r)));
    res = bdd_makenode(LEVEL(r), READREF(2), READREF(1));
    POPREF(2);

    applycache.store(hash, r, 0, bddop_not, res, cachestats);

    return res;
  }

  private int bdd_ite(int f, int g, int h) {
    CHECK(f);
    CHECK(g);
    CHECK(h);

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (itecache == null) {
      int iteSize = Integer.highestOneBit(cachesize - 1) << 1;
      if (iteSize < 16) iteSize = 16;
      itecache = new FlatCacheIte(iteSize);
    }
    if (multiopcache == null) {
      multiopcache = BddCacheMultiOp_init(cachesize);
    }

    INITREF();
    int res = ite_rec(f, g, h);
    checkresize();

    return res;
  }

  private int ite_rec(int f, int g, int h) {
    int res;

    if (ISONE(f)) {
      return g;
    } else if (ISZERO(f)) {
      return h;
    } else if (HIGH(f) == BDDONE
        && LOW(f) == BDDZERO
        && LEVEL(f) < LEVEL(g)
        && LEVEL(f) < LEVEL(h)) {
      // f is a single variable BDD, and its level is lower than g's and h's.
      // this is a common case: we're building a BDD bottom-up
      // skip the operator cache
      return bdd_makenode(LEVEL(f), h /* low/else */, g /* high/then */);
    } else if (g == h) {
      return g;
    } else if (ISZERO(g)) {
      applyop = bddop_less;
      return apply_rec(f, h);
    } else if (ISONE(g)) {
      return or_rec(f, h);
    } else if (ISZERO(h)) {
      return and_rec(f, g);
    } else if (ISONE(h)) {
      applyop = bddop_imp;
      return apply_rec(f, g);
    }

    // ITE uses a dedicated flat cache for best performance
    int hash = TRIPLE(f, g, h);
    int cached = itecache.lookup(hash, f, g, h, cachestats);
    if (cached >= 0) return cached;

    if (LEVEL(f) == LEVEL(g)) {
      if (LEVEL(f) == LEVEL(h)) {
        PUSHREF(ite_rec(LOW(f), LOW(g), LOW(h)));
        PUSHREF(ite_rec(HIGH(f), HIGH(g), HIGH(h)));
        res = bdd_makenode(LEVEL(f), READREF(2), READREF(1));
      } else if (LEVEL(f) < LEVEL(h)) {
        PUSHREF(ite_rec(LOW(f), LOW(g), h));
        PUSHREF(ite_rec(HIGH(f), HIGH(g), h));
        res = bdd_makenode(LEVEL(f), READREF(2), READREF(1));
      } else /* f > h */ {
        PUSHREF(ite_rec(f, g, LOW(h)));
        PUSHREF(ite_rec(f, g, HIGH(h)));
        res = bdd_makenode(LEVEL(h), READREF(2), READREF(1));
      }
    } else if (LEVEL(f) < LEVEL(g)) {
      if (LEVEL(f) == LEVEL(h)) {
        PUSHREF(ite_rec(LOW(f), g, LOW(h)));
        PUSHREF(ite_rec(HIGH(f), g, HIGH(h)));
        res = bdd_makenode(LEVEL(f), READREF(2), READREF(1));
      } else if (LEVEL(f) < LEVEL(h)) {
        PUSHREF(ite_rec(LOW(f), g, h));
        PUSHREF(ite_rec(HIGH(f), g, h));
        res = bdd_makenode(LEVEL(f), READREF(2), READREF(1));
      } else /* f > h */ {
        PUSHREF(ite_rec(f, g, LOW(h)));
        PUSHREF(ite_rec(f, g, HIGH(h)));
        res = bdd_makenode(LEVEL(h), READREF(2), READREF(1));
      }
    } else /* f > g */ {
      if (LEVEL(g) == LEVEL(h)) {
        PUSHREF(ite_rec(f, LOW(g), LOW(h)));
        PUSHREF(ite_rec(f, HIGH(g), HIGH(h)));
        res = bdd_makenode(LEVEL(g), READREF(2), READREF(1));
      } else if (LEVEL(g) < LEVEL(h)) {
        PUSHREF(ite_rec(f, LOW(g), h));
        PUSHREF(ite_rec(f, HIGH(g), h));
        res = bdd_makenode(LEVEL(g), READREF(2), READREF(1));
      } else /* g > h */ {
        PUSHREF(ite_rec(f, g, LOW(h)));
        PUSHREF(ite_rec(f, g, HIGH(h)));
        res = bdd_makenode(LEVEL(h), READREF(2), READREF(1));
      }
    }

    POPREF(2);

    itecache.store(hash, f, g, h, res, cachestats);

    return res;
  }

  private int bdd_replace(int r, bddPair pair) {
    CHECK(r);

    if (replacecache == null) {
      replacecache = FlatCacheI_init(cachesize);
    }
    replacepair = pair.result;
    replacelast = pair.last;
    replaceid = (pair.id << 3) | CACHEID_REPLACE;

    INITREF();
    int res = replace_rec(r);
    checkresize();

    return res;
  }

  private int replace_rec(int r) {
    int res;

    if (ISCONST(r) || LEVEL(r) > replacelast) {
      return r;
    }

    int hash = REPLACEHASH(replaceid, r);
    int cached = replacecache.lookup(hash, r, 0, replaceid, cachestats);
    if (cached >= 0) return cached;

    PUSHREF(replace_rec(LOW(r)));
    PUSHREF(replace_rec(HIGH(r)));

    /* Replace the root variable with the new one. Replacements at the root or in the subbdds can
     * cause the new root to be out of order. bdd_correctify builds the bdd correctly by branching
     * on the new root at the correct level of the bdd.
     */
    {
      int level = LEVEL(replacepair[LEVEL(r)]);

      /* bdd_correctify calls are cached separately from replace_rec calls. Set the cacheid for
       * the bdd_correctify calls and restore when it returns.
       */
      int tmp = replaceid;
      replaceid = (level << 3) | CACHEID_CORRECTIFY;
      res = bdd_correctify(level, READREF(2), READREF(1));
      replaceid = tmp;
    }
    POPREF(2);

    replacecache.store(hash, r, 0, replaceid, res, cachestats);

    return res;
  }

  /**
   * This is similar to {@link JFactory#bdd_makenode} -- it returns a BDD that branches at the input
   * level with the input low and high nodes. The difference between this and bdd_makenode is that
   * bdd_makenode requires level to be strictly less than LEVEL(l) and LEVEL(r), where this does
   * not. The base case of bdd_correctify is when that is true -- then it simply delegates to
   * bdd_makenode.
   *
   * @param level The level to branch on.
   * @param l The low branch.
   * @param r The high branch.
   */
  private int bdd_correctify(int level, int l, int r) {
    int res;

    if (level < LEVEL(l) && level < LEVEL(r)) {
      return bdd_makenode(level, l, r);
    }

    if (level == LEVEL(l) || level == LEVEL(r)) {
      bdd_error(BDD_REPLACE);
      return 0;
    }

    int hash = CORRECTIFYHASH(replaceid, l, r);
    int cached = replacecache.lookup(hash, l, r, replaceid, cachestats);
    if (cached >= 0) return cached;

    if (LEVEL(l) == LEVEL(r)) {
      PUSHREF(bdd_correctify(level, LOW(l), LOW(r)));
      PUSHREF(bdd_correctify(level, HIGH(l), HIGH(r)));
      res = bdd_makenode(LEVEL(l), READREF(2), READREF(1));
    } else if (LEVEL(l) < LEVEL(r)) {
      PUSHREF(bdd_correctify(level, LOW(l), r));
      PUSHREF(bdd_correctify(level, HIGH(l), r));
      res = bdd_makenode(LEVEL(l), READREF(2), READREF(1));
    } else {
      PUSHREF(bdd_correctify(level, l, LOW(r)));
      PUSHREF(bdd_correctify(level, l, HIGH(r)));
      res = bdd_makenode(LEVEL(r), READREF(2), READREF(1));
    }
    POPREF(2);

    replacecache.store(hash, l, r, replaceid, res, cachestats);

    return res;
  }

  private int bdd_apply(int l, int r, int op) {
    CHECK(l);
    CHECK(r);

    if (op < 0 || op > bddop_invimp) {
      bdd_error(BDD_OP);
      return BDDZERO;
    }

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    applyop = op;

    INITREF();
    int res;
    switch (op) {
      case bddop_and:
        res = and_rec(l, r);
        break;
      case bddop_or:
        res = or_rec(l, r);
        break;
      default:
        res = apply_rec(l, r);
        break;
    }
    checkresize();

    return res;
  }

  private int apply_rec(int l, int r) {
    int res;

    if (VERIFY_ASSERTIONS) {
      _assert(applyop != bddop_and && applyop != bddop_or);
    }

    if (ISCONST(l) && ISCONST(r)) {
      return oprres[applyop][l << 1 | r];
    }

    switch (applyop) {
      // case bddop_and: is handled elsehwere
      case bddop_xor:
        if (l == r) {
          return BDDZERO;
        } else if (ISZERO(l)) {
          return r;
        } else if (ISZERO(r)) {
          return l;
        } else if (ISONE(l)) {
          return not_rec(r);
        } else if (ISONE(r)) {
          return not_rec(l);
        } else if (l > r) {
          // Since XOR is symmetric, maximize caching by ensuring l < r (== handled above).
          int t = l;
          l = r;
          r = t;
        }
        break;
      // case bddop_or: is handled elsehwere
      case bddop_nand:
        if (l == r) {
          return not_rec(l);
        } else if (ISZERO(l) || ISZERO(r)) {
          return BDDONE;
        } else if (ISONE(l)) {
          return not_rec(r);
        } else if (ISONE(r)) {
          return not_rec(l);
        } else if (l > r) {
          // Since NAND is symmetric, maximize caching by ensuring l < r (== handled above).
          int t = l;
          l = r;
          r = t;
        }
        break;
      case bddop_nor:
        if (l == r) {
          return not_rec(l);
        } else if (ISONE(l) || ISONE(r)) {
          return BDDZERO;
        } else if (ISZERO(l)) {
          return not_rec(r);
        } else if (ISZERO(r)) {
          return not_rec(l);
        } else if (l > r) {
          // Since NOR is symmetric, maximize caching by ensuring l < r (== handled above).
          int t = l;
          l = r;
          r = t;
        }
        break;
      case bddop_imp:
        if (l == r) {
          return BDDONE;
        } else if (ISZERO(l)) {
          return BDDONE;
        } else if (ISONE(l)) {
          return r;
        } else if (ISZERO(r)) {
          return not_rec(l);
        } else if (ISONE(r)) {
          return BDDONE;
        }
        break;
      case bddop_biimp:
        if (l == r) {
          return BDDONE;
        } else if (ISZERO(l)) {
          return not_rec(r);
        } else if (ISZERO(r)) {
          return not_rec(l);
        } else if (ISONE(l)) {
          return r;
        } else if (ISONE(r)) {
          return l;
        } else if (l > r) {
          // Since BIIMP is symmetric, maximize caching by ensuring l < r (== handled above).
          int t = l;
          l = r;
          r = t;
        }
        break;
      case bddop_diff:
        if (l == r) {
          return BDDZERO;
        } else if (ISZERO(l)) {
          return BDDZERO;
        } else if (ISONE(r)) {
          return BDDZERO;
        } else if (ISONE(l)) {
          return not_rec(r);
        } else if (ISZERO(r)) {
          return l;
        }
        break;
      case bddop_less:
        if (l == r) {
          return BDDZERO;
        } else if (ISONE(l)) {
          return BDDZERO;
        } else if (ISZERO(r)) {
          return BDDZERO;
        } else if (ISZERO(l)) {
          return r;
        } else if (ISONE(r)) {
          return not_rec(l);
        } else {
          // Rewrite as equivalent diff to improve caching.
          applyop = bddop_diff;
          int t = l;
          l = r;
          r = t;
        }
        break;
      case bddop_invimp:
        if (l == r) {
          return BDDONE;
        } else if (ISONE(l)) {
          return BDDONE;
        } else if (ISZERO(r)) {
          return BDDONE;
        } else if (ISONE(r)) {
          return l;
        } else if (ISZERO(l)) {
          return not_rec(r);
        } else {
          // Rewrite as equivalent imp to improve caching.
          applyop = bddop_imp;
          int t = l;
          l = r;
          r = t;
        }
        break;
    }

    int hash = APPLYHASH(l, r, applyop);
    int cached = applycache.lookup(hash, l, r, applyop, cachestats);
    if (cached >= 0) return cached;

    int LEVEL_l = LEVEL(l);
    int LEVEL_r = LEVEL(r);
    if (LEVEL_l == LEVEL_r) {
      PUSHREF(apply_rec(LOW(l), LOW(r)));
      PUSHREF(apply_rec(HIGH(l), HIGH(r)));
      res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
    } else if (LEVEL_l < LEVEL_r) {
      PUSHREF(apply_rec(LOW(l), r));
      PUSHREF(apply_rec(HIGH(l), r));
      res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
    } else {
      PUSHREF(apply_rec(l, LOW(r)));
      PUSHREF(apply_rec(l, HIGH(r)));
      res = bdd_makenode(LEVEL_r, READREF(2), READREF(1));
    }

    POPREF(2);

    applycache.store(hash, l, r, applyop, res, cachestats);

    return res;
  }

  private int and_rec(int l, int r) {
    int res;

    if (l == r) {
      return l;
    } else if (ISZERO(l) || ISZERO(r)) {
      return BDDZERO;
    } else if (ISONE(l)) {
      return r;
    } else if (ISONE(r)) {
      return l;
    } else if (l > r) {
      // Since AND is symmetric, maximize caching by ensuring l < r (== handled above).
      int t = l;
      l = r;
      r = t;
    }
    int hash = APPLYHASH(l, r, bddop_and);
    int cached = applycache.lookup(hash, l, r, bddop_and, cachestats);
    if (cached >= 0) return cached;

    int LEVEL_l = LEVEL(l);
    int LEVEL_r = LEVEL(r);
    if (LEVEL_l == LEVEL_r) {
      PUSHREF(and_rec(LOW(l), LOW(r)));
      PUSHREF(and_rec(HIGH(l), HIGH(r)));
      res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
    } else if (LEVEL_l < LEVEL_r) {
      PUSHREF(and_rec(LOW(l), r));
      PUSHREF(and_rec(HIGH(l), r));
      res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
    } else {
      PUSHREF(and_rec(l, LOW(r)));
      PUSHREF(and_rec(l, HIGH(r)));
      res = bdd_makenode(LEVEL_r, READREF(2), READREF(1));
    }

    POPREF(2);

    applycache.store(hash, l, r, bddop_and, res, cachestats);

    return res;
  }

  private boolean diffsat_rec(int l, int r) {
    if (ISZERO(l) || ISONE(r)) {
      return false;
    } else if (ISONE(l) || ISZERO(r)) {
      return true;
    } else if (l == r) {
      return false;
    }

    // TODO: should we also check for diff? For now, don't since diff_sat should be real fast.
    int hash = APPLYHASH(l, r, bddop_diffsat);
    int cached = applycache.lookup(hash, l, r, bddop_diffsat, cachestats);
    if (cached >= 0) return cached == 1;

    boolean res;
    if (LEVEL(l) == LEVEL(r)) {
      res = diffsat_rec(LOW(l), LOW(r)) || diffsat_rec(HIGH(l), HIGH(r));
    } else if (LEVEL(l) < LEVEL(r)) {
      res = diffsat_rec(LOW(l), r) || diffsat_rec(HIGH(l), r);
    } else {
      res = diffsat_rec(l, LOW(r)) || diffsat_rec(l, HIGH(r));
    }

    applycache.store(hash, l, r, bddop_diffsat, res ? 1 : 0, cachestats);

    return res;
  }

  private boolean andsat_rec(int l, int r) {
    if (ISZERO(l) || ISZERO(r)) {
      return false;
    } else if (ISONE(l) || ISONE(r)) {
      return true;
    } else if (l == r) {
      return true;
    } else if (l > r) {
      // Since AND is symmetric, maximize caching by ensuring l < r (== handled above).
      int t = l;
      l = r;
      r = t;
    }

    // TODO: should we also check for and? For now, don't since and_sat should be real fast.
    int hash = APPLYHASH(l, r, bddop_andsat);
    int cached = applycache.lookup(hash, l, r, bddop_andsat, cachestats);
    if (cached >= 0) return cached == 1;

    boolean res;
    if (LEVEL(l) == LEVEL(r)) {
      res = andsat_rec(LOW(l), LOW(r)) || andsat_rec(HIGH(l), HIGH(r));
    } else if (LEVEL(l) < LEVEL(r)) {
      res = andsat_rec(LOW(l), r) || andsat_rec(HIGH(l), r);
    } else {
      res = andsat_rec(l, LOW(r)) || andsat_rec(l, HIGH(r));
    }

    applycache.store(hash, l, r, bddop_andsat, res ? 1 : 0, cachestats);

    return res;
  }

  /**
   * Dedup a sorted array. Returns the input array if it contains no duplicates. Mutates the array
   * if there are duplicates.
   */
  static int[] dedupSorted(int[] values, int len) {
    assert len <= values.length;
    if (values.length < 2 && len == values.length) {
      return values;
    }
    int i = 0; // index last written to
    int j = 1; // index to read from next
    while (j < len) {
      if (values[i] != values[j]) {
        values[++i] = values[j++];
      } else {
        j++;
      }
    }

    int dedupLen = i + 1;
    if (dedupLen < values.length) {
      return Arrays.copyOf(values, dedupLen);
    } else {
      return values;
    }
  }

  private int andAll_rec(int[] operands) {
    if (operands.length == 0) {
      return BDDONE;
    } else if (operands.length == 1) {
      return operands[0];
    } else if (operands.length == 2) {
      return and_rec(operands[0], operands[1]);
    }

    // sort and dedup the operands to optimize caching
    Arrays.sort(operands);
    operands = dedupSorted(operands, operands.length);

    int hash = MULTIOPHASH(operands, bddop_and);
    MultiOpBddCacheData entry = BddCache_lookupMultiOp(multiopcache, hash);
    if (entry.a == bddop_and && Arrays.equals(operands, entry.operands)) {
      if (CACHESTATS) {
        cachestats.opHit++;
      }
      return entry.b;
    }
    if (CACHESTATS) {
      cachestats.opMiss++;
    }

    /* Compute the result in a way that generalizes and_rec. Identify the variable to branch on, and
     * make two recursive calls (for when that variable is high or low).
     *
     * In a single pass over operands:
     * 1. Find the level of the variable the result BDD should branch on. This is the minimum level
     *    branched on at the roots of the current operand BDDs.
     * 2. Compute the size needed for the operand arrays of the two recursive calls. This is equal
     *    to the number of operands whose root level are greater than the minimum, plus the number
     *    of operands whose root level is equal to the minimum and whose child (low or high,
     *    corresponding to if the recursive call is computing the low or high child of the result)
     *    is not the one BDD.
     * 3. Whether either recursive call can be short-circuited because one of the operands is the
     *    zero BDD. This can only happen when the one is a child a BDD whose root level is the
     *    minimum.
     */

    int minLevel = LEVEL(operands[0]);
    int nodesWithMinLevel = 0;
    int nodesWithMinLevelLowNonOne = 0;
    int nodesWithMinLevelHighNonOne = 0;
    boolean nodeWithMinLevelHasLowZero = false;
    boolean nodeWithMinLevelHasHighZero = false;
    for (int n : operands) {
      int level = LEVEL(n);
      if (level < minLevel) {
        minLevel = level;
        nodesWithMinLevel = 0;
        nodesWithMinLevelHighNonOne = 0;
        nodesWithMinLevelLowNonOne = 0;
        nodeWithMinLevelHasHighZero = false;
        nodeWithMinLevelHasLowZero = false;
      } else if (level > minLevel) {
        continue;
      }

      // level == minLevel
      nodesWithMinLevel++;

      int high = HIGH(n);
      nodeWithMinLevelHasHighZero |= ISZERO(high);
      nodesWithMinLevelHighNonOne += ISONE(high) ? 0 : 1;

      int low = LOW(n);
      nodeWithMinLevelHasLowZero |= ISZERO(low);
      nodesWithMinLevelLowNonOne += ISONE(low) ? 0 : 1;
    }

    int nodesWithoutMinLevel = operands.length - nodesWithMinLevel;

    int low;
    if (!nodeWithMinLevelHasLowZero) {
      /* Make the recursive call for the low branch. None of the operands are 0, so we can't
       * short-circuit to 0. Allocate and build the array of operands, then make the call and push
       * the result onto the stack.
       */
      int[] lowOperands = new int[nodesWithMinLevelLowNonOne + nodesWithoutMinLevel];
      int i = 0;
      for (int operand : operands) {
        if (LEVEL(operand) == minLevel) {
          int l = LOW(operand);
          if (!ISONE(l)) {
            assert !ISCONST(l);
            lowOperands[i++] = l;
          }
        } else {
          assert !ISCONST(operand);
          lowOperands[i++] = operand;
        }
      }
      assert i == lowOperands.length;
      low = andAll_rec(lowOperands);
      PUSHREF(low); // make sure low isn't garbage collected.
    } else {
      low = BDDZERO;
    }

    int high;
    if (!nodeWithMinLevelHasHighZero) {
      /* Make the recursive call for the high branch. None of the operands are 0, so we can't
       * short-circuit to 0. Allocate and build the array of operands, then make the call and push
       * the result onto the stack.
       */
      int[] highOperands = new int[nodesWithMinLevelHighNonOne + nodesWithoutMinLevel];
      int i = 0;
      for (int operand : operands) {
        if (LEVEL(operand) == minLevel) {
          int h = HIGH(operand);
          if (!ISONE(h)) {
            assert !ISCONST(h);
            highOperands[i++] = h;
          }
        } else {
          assert !ISCONST(operand);
          highOperands[i++] = operand;
        }
      }
      assert i == highOperands.length;
      high = andAll_rec(highOperands);
      PUSHREF(high); // make sure high isn't garbage collected.
    } else {
      high = BDDZERO;
    }

    int res = bdd_makenode(minLevel, low, high);

    if (!nodeWithMinLevelHasHighZero) {
      POPREF(1);
    }
    if (!nodeWithMinLevelHasLowZero) {
      POPREF(1);
    }

    if (CACHESTATS && entry.a != -1) {
      cachestats.opOverwrite++;
    }
    entry.a = bddop_and;
    entry.b = res;
    entry.operands = operands;
    entry.hash = hash;
    return res;
  }

  private int orAll_rec(int[] operands) {
    if (operands.length == 0) {
      return BDDZERO;
    } else if (operands.length == 1) {
      return operands[0];
    } else if (operands.length == 2) {
      return or_rec(operands[0], operands[1]);
    }

    // sort and dedup the operands to optimize caching
    Arrays.sort(operands);
    operands = dedupSorted(operands, operands.length);

    int hash = MULTIOPHASH(operands, bddop_or);
    MultiOpBddCacheData entry = BddCache_lookupMultiOp(multiopcache, hash);
    if (entry.a == bddop_or && Arrays.equals(operands, entry.operands)) {
      if (CACHESTATS) {
        cachestats.opHit++;
      }
      return entry.b;
    }
    if (CACHESTATS) {
      cachestats.opMiss++;
    }

    /* Compute the result in a way that generalizes or_rec. Identify the variable to branch on, and
     * make two recursive calls (for when that variable is high or low).
     *
     * In a single pass over operands:
     * 1. Find the level of the variable the result BDD should branch on. This is the minimum level
     *    branched on at the roots of the current operand BDDs.
     * 2. Compute the size needed for the operand arrays of the two recursive calls. This is equal
     *    to the number of operands whose root level are greater than the minimum, plus the number
     *    of operands whose root level is equal to the minimum and whose child (low or high,
     *    corresponding to if the recursive call is computing the low or high child of the result)
     *    is not the zero BDD.
     * 3. Whether either recursive call can be short-circuited because one of the operands is the
     *    one BDD. This can only happen when the one is a child a BDD whose root level is the
     *    minimum.
     */

    int minLevel = LEVEL(operands[0]);
    int nodesWithMinLevel = 0;
    int nodesWithMinLevelLowNonZero = 0;
    int nodesWithMinLevelHighNonZero = 0;
    boolean nodeWithMinLevelHasLowOne = false;
    boolean nodeWithMinLevelHasHighOne = false;
    for (int n : operands) {
      int level = LEVEL(n);
      if (level < minLevel) {
        minLevel = level;
        nodesWithMinLevel = 0;
        nodesWithMinLevelHighNonZero = 0;
        nodesWithMinLevelLowNonZero = 0;
        nodeWithMinLevelHasHighOne = false;
        nodeWithMinLevelHasLowOne = false;
      } else if (level > minLevel) {
        continue;
      }

      // level == minLevel
      nodesWithMinLevel++;

      int high = HIGH(n);
      nodeWithMinLevelHasHighOne |= ISONE(high);
      nodesWithMinLevelHighNonZero += ISZERO(high) ? 0 : 1;

      int low = LOW(n);
      nodeWithMinLevelHasLowOne |= ISONE(low);
      nodesWithMinLevelLowNonZero += ISZERO(low) ? 0 : 1;
    }

    int nodesWithoutMinLevel = operands.length - nodesWithMinLevel;

    int low;
    if (!nodeWithMinLevelHasLowOne) {
      /* Make the recursive call for the low branch. None of the operands are 1, so we can't
       * short-circuit to 1. Allocate and build the array of operands, then make the call and push
       * the result onto the stack.
       */
      int[] lowOperands = new int[nodesWithMinLevelLowNonZero + nodesWithoutMinLevel];
      int i = 0;
      for (int operand : operands) {
        if (LEVEL(operand) == minLevel) {
          int l = LOW(operand);
          if (!ISZERO(l)) {
            assert !ISCONST(l);
            lowOperands[i++] = l;
          }
        } else {
          assert !ISCONST(operand);
          lowOperands[i++] = operand;
        }
      }
      assert i == lowOperands.length;
      low = orAll_rec(lowOperands);
      PUSHREF(low); // make sure low isn't garbage collected.
    } else {
      low = BDDONE;
    }

    int high;
    if (!nodeWithMinLevelHasHighOne) {
      /* Make the recursive call for the high branch. None of the operands are 1, so we can't
       * short-circuit to 1. Allocate and build the array of operands, then make the call and push
       * the result onto the stack.
       */
      int[] highOperands = new int[nodesWithMinLevelHighNonZero + nodesWithoutMinLevel];
      int i = 0;
      for (int operand : operands) {
        if (LEVEL(operand) == minLevel) {
          int h = HIGH(operand);
          if (!ISZERO(h)) {
            assert !ISCONST(h);
            highOperands[i++] = h;
          }
        } else {
          assert !ISCONST(operand);
          highOperands[i++] = operand;
        }
      }
      assert i == highOperands.length;
      high = orAll_rec(highOperands);
      PUSHREF(high); // make sure high isn't garbage collected.
    } else {
      high = BDDONE;
    }

    int res = bdd_makenode(minLevel, low, high);

    if (!nodeWithMinLevelHasHighOne) {
      POPREF(1);
    }
    if (!nodeWithMinLevelHasLowOne) {
      POPREF(1);
    }

    if (CACHESTATS && entry.a != -1) {
      cachestats.opOverwrite++;
    }
    entry.a = bddop_or;
    entry.b = res;
    entry.operands = operands;
    entry.hash = hash;
    return res;
  }

  private int or_rec(int l, int r) {
    int res;

    if (l == r) {
      return l;
    } else if (ISONE(l) || ISONE(r)) {
      return BDDONE;
    } else if (ISZERO(l)) {
      return r;
    } else if (ISZERO(r)) {
      return l;
    } else if (l > r) {
      // Since OR is symmetric, maximize caching by ensuring l < r (== handled above).
      int t = l;
      l = r;
      r = t;
    }
    int hash = APPLYHASH(l, r, bddop_or);
    int cached = applycache.lookup(hash, l, r, bddop_or, cachestats);
    if (cached >= 0) return cached;

    int LEVEL_l = LEVEL(l);
    int LEVEL_r = LEVEL(r);
    if (LEVEL_l == LEVEL_r) {
      PUSHREF(or_rec(LOW(l), LOW(r)));
      PUSHREF(or_rec(HIGH(l), HIGH(r)));
      res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
    } else if (LEVEL_l < LEVEL_r) {
      PUSHREF(or_rec(LOW(l), r));
      PUSHREF(or_rec(HIGH(l), r));
      res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
    } else {
      PUSHREF(or_rec(l, LOW(r)));
      PUSHREF(or_rec(l, HIGH(r)));
      res = bdd_makenode(LEVEL_r, READREF(2), READREF(1));
    }

    POPREF(2);

    applycache.store(hash, l, r, bddop_or, res, cachestats);

    return res;
  }

  private int relprod_rec(int l, int r) {
    int res;

    if (l == BDDZERO || r == BDDZERO) {
      return BDDZERO;
    } else if (l == r) {
      return exist_rec(l);
    } else if (l == BDDONE) {
      return exist_rec(r);
    } else if (r == BDDONE) {
      return exist_rec(l);
    }

    int LEVEL_l = LEVEL(l);
    int LEVEL_r = LEVEL(r);
    if (LEVEL_l > quantlast && LEVEL_r > quantlast) {
      applyop = bddop_and;
      res = and_rec(l, r);
      applyop = bddop_or;
    } else {
      int hash = APPEXHASH(l, r, appexid);
      int cached = appexcache.lookup(hash, l, r, appexid, cachestats);
      if (cached >= 0) return cached;

      if (LEVEL_l == LEVEL_r) {
        PUSHREF(relprod_rec(LOW(l), LOW(r)));
        PUSHREF(relprod_rec(HIGH(l), HIGH(r)));
        if (INVARSET(LEVEL_l)) {
          res = or_rec(READREF(2), READREF(1));
        } else {
          res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
        }
      } else if (LEVEL_l < LEVEL_r) {
        PUSHREF(relprod_rec(LOW(l), r));
        PUSHREF(relprod_rec(HIGH(l), r));
        if (INVARSET(LEVEL_l)) {
          res = or_rec(READREF(2), READREF(1));
        } else {
          res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
        }
      } else {
        PUSHREF(relprod_rec(l, LOW(r)));
        PUSHREF(relprod_rec(l, HIGH(r)));
        if (INVARSET(LEVEL_r)) {
          res = or_rec(READREF(2), READREF(1));
        } else {
          res = bdd_makenode(LEVEL_r, READREF(2), READREF(1));
        }
      }

      POPREF(2);

      appexcache.store(hash, l, r, appexid, res, cachestats);
    }

    return res;
  }

  private int bdd_relprod(int a, int b, int var) {
    return bdd_appex(a, b, bddop_and, var);
  }

  private int bdd_appex(int l, int r, int opr, int var) {
    CHECK(l);
    CHECK(r);
    CHECK(var);

    if (opr < 0 || opr > bddop_invimp) {
      bdd_error(BDD_OP);
      return BDDZERO;
    }

    if (var < 2) /* Empty set */ {
      return bdd_apply(l, r, opr);
    }
    if (varset2vartable(var) < 0) {
      return BDDZERO;
    }

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (appexcache == null) {
      appexcache = FlatCacheI_init(cachesize);
    }
    if (quantcache == null) {
      quantcache = FlatCacheI_init(cachesize);
    }
    applyop = bddop_or;
    appexop = opr;
    appexid = (var << 5) | (appexop << 1); /* FIXME: range! */
    quantid = (appexid << 3) | CACHEID_APPEX;

    INITREF();
    int res = opr == bddop_and ? relprod_rec(l, r) : appquant_rec(l, r);
    checkresize();

    return res;
  }

  private int bdd_transform(int l, int r, bddPair pair) {
    CHECK(l);
    CHECK(r);

    if (!_validPairIdsForTransform.contains(pair.id)) {
      checkArgument(pair.isValidForTransform(), "Input BDDPairing is not valid for transform");
      _validPairIdsForTransform.add(pair.id);
    }

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (replacecache == null) {
      replacecache = FlatCacheI_init(cachesize);
    }
    replacepair = pair.result;
    replacelast = pair.last;
    replaceid = (pair.id << 3) | CACHEID_TRANSFORM;

    INITREF();
    int res = transform_rec(l, r);
    checkresize();

    return res;
  }

  private int transform_rec(int l, int r) {
    int res;

    if (ISZERO(l) || ISZERO(r)) {
      return BDDZERO;
    }
    if (LEVEL(l) > replacelast && LEVEL(r) > replacelast) {
      return and_rec(l, r);
    }
    int hash = TRANSFORMHASH(replaceid, l, r);
    int cached = replacecache.lookup(hash, l, r, replaceid, cachestats);
    if (cached >= 0) return cached;

    int level;
    if (LEVEL(l) == LEVEL(r)) {
      level = LEVEL(l);
      PUSHREF(transform_rec(LOW(l), LOW(r)));
      PUSHREF(transform_rec(HIGH(l), HIGH(r)));
    } else if (LEVEL(l) < LEVEL(r)) {
      level = LEVEL(l);
      PUSHREF(transform_rec(LOW(l), r));
      PUSHREF(transform_rec(HIGH(l), r));
    } else {
      level = LEVEL(r);
      PUSHREF(transform_rec(l, LOW(r)));
      PUSHREF(transform_rec(l, HIGH(r)));
    }

    int lo = READREF(2);
    int hi = READREF(1);

    assert LEVEL(lo) >= level && LEVEL(hi) >= level : "Cannot transform up more than one level";

    // check if this level is replaced, i.e. if it's in the codomain of the replacepair. If it is,
    // it must be mapped from the next level.
    int nextLevel = level + 1;
    if (nextLevel < replacepair.length && LEVEL(replacepair[nextLevel]) == level) {
      // this level has been replaced, i.e. existentially quantified. OR hi and lo
      res = or_rec(lo, hi);
    } else {
      // two cases: replace at this level or keep the level. If replaced, it must be mapped to the
      // previous level.
      int codomLevel = LEVEL(replacepair[level]);
      assert codomLevel == level || codomLevel == level - 1
          : "Transform can only replace variables to the previous level";
      res = bdd_makenode(codomLevel, lo, hi);
    }
    POPREF(2);

    replacecache.store(hash, l, r, replaceid, res, cachestats);

    return res;
  }

  private int varset2vartable(int r) {
    if (r < 2) {
      return bdd_error(BDD_VARSET);
    }

    quantvarsetID++;

    if (quantvarsetID == INT_MAX) {
      for (int i = 0; i < bddvarnum; ++i) {
        quantvarset[i] = 0;
      }
      quantvarsetID = 1;
    }

    quantlast = -1;
    for (int n = r; n > 1; n = HIGH(n)) {
      quantvarset[LEVEL(n)] = quantvarsetID;
      if (VERIFY_ASSERTIONS) {
        _assert(quantlast < LEVEL(n));
      }
      quantlast = LEVEL(n);
    }

    return 0;
  }

  private static final int INT_MAX = Integer.MAX_VALUE;

  private int varset2svartable(int r) {
    if (r < 2) {
      return bdd_error(BDD_VARSET);
    }

    quantvarsetID++;

    if (quantvarsetID == INT_MAX / 2) {
      for (int i = 0; i < bddvarnum; ++i) {
        quantvarset[i] = 0;
      }
      quantvarsetID = 1;
    }

    quantlast = 0;
    for (int n = r; !ISCONST(n); ) {
      if (ISZERO(LOW(n))) {
        quantvarset[LEVEL(n)] = quantvarsetID;
        n = HIGH(n);
      } else {
        quantvarset[LEVEL(n)] = -quantvarsetID;
        n = LOW(n);
      }
      if (VERIFY_ASSERTIONS) {
        _assert(quantlast < LEVEL(n));
      }
      quantlast = LEVEL(n);
    }

    return 0;
  }

  private int appquant_rec(int l, int r) {
    int res;

    if (VERIFY_ASSERTIONS) {
      _assert(appexop != bddop_and);
    }

    switch (appexop) {
      case bddop_or:
        if (l == BDDONE || r == BDDONE) {
          return BDDONE;
        } else if (l == r) {
          return exist_rec(l);
        } else if (l == BDDZERO) {
          return exist_rec(r);
        } else if (r == BDDZERO) {
          return exist_rec(l);
        }
        break;
      case bddop_xor:
        if (l == r) {
          return BDDZERO;
        } else if (l == BDDZERO) {
          return quant_rec(r);
        } else if (r == BDDZERO) {
          return quant_rec(l);
        }
        break;
      case bddop_nand:
        if (l == BDDZERO || r == BDDZERO) {
          return BDDONE;
        }
        break;
      case bddop_nor:
        if (l == BDDONE || r == BDDONE) {
          return BDDZERO;
        }
        break;
    }

    if (ISCONST(l) && ISCONST(r)) {
      res = oprres[appexop][(l << 1) | r];
    } else if (LEVEL(l) > quantlast && LEVEL(r) > quantlast) {
      int oldop = applyop;
      applyop = appexop;
      switch (applyop) {
        case bddop_and:
          res = and_rec(l, r);
          break;
        case bddop_or:
          res = or_rec(l, r);
          break;
        default:
          res = apply_rec(l, r);
          break;
      }
      applyop = oldop;
    } else {
      int hash = APPEXHASH(l, r, appexid);
      int cached = appexcache.lookup(hash, l, r, appexid, cachestats);
      if (cached >= 0) return cached;

      int lev;
      if (LEVEL(l) == LEVEL(r)) {
        PUSHREF(appquant_rec(LOW(l), LOW(r)));
        PUSHREF(appquant_rec(HIGH(l), HIGH(r)));
        lev = LEVEL(l);
      } else if (LEVEL(l) < LEVEL(r)) {
        PUSHREF(appquant_rec(LOW(l), r));
        PUSHREF(appquant_rec(HIGH(l), r));
        lev = LEVEL(l);
      } else {
        PUSHREF(appquant_rec(l, LOW(r)));
        PUSHREF(appquant_rec(l, HIGH(r)));
        lev = LEVEL(r);
      }
      if (INVARSET(lev)) {
        int r2 = READREF(2), r1 = READREF(1);
        switch (applyop) {
          case bddop_and:
            res = and_rec(r2, r1);
            break;
          case bddop_or:
            res = or_rec(r2, r1);
            break;
          default:
            res = apply_rec(r2, r1);
            break;
        }
      } else {
        res = bdd_makenode(lev, READREF(2), READREF(1));
      }

      POPREF(2);

      appexcache.store(hash, l, r, appexid, res, cachestats);
    }

    return res;
  }

  private int appuni_rec(int l, int r, int var) {
    int res;

    int LEVEL_l, LEVEL_r, LEVEL_var;
    LEVEL_l = LEVEL(l);
    LEVEL_r = LEVEL(r);
    LEVEL_var = LEVEL(var);

    if (LEVEL_l > LEVEL_var && LEVEL_r > LEVEL_var) {
      // Skipped a quantified node, answer is zero.
      return BDDZERO;
    }

    if (ISCONST(l) && ISCONST(r)) {
      res = oprres[appexop][(l << 1) | r];
    } else if (ISCONST(var)) {
      int oldop = applyop;
      applyop = appexop;
      switch (applyop) {
        case bddop_and:
          res = and_rec(l, r);
          break;
        case bddop_or:
          res = or_rec(l, r);
          break;
        default:
          res = apply_rec(l, r);
          break;
      }
      applyop = oldop;
    } else {
      int hash = APPEXHASH(l, r, appexid);
      int cached = appexcache.lookup(hash, l, r, appexid, cachestats);
      if (cached >= 0) return cached;

      int lev;
      if (LEVEL_l == LEVEL_r) {
        if (LEVEL_l == LEVEL_var) {
          lev = -1;
          var = HIGH(var);
        } else {
          lev = LEVEL_l;
        }
        PUSHREF(appuni_rec(LOW(l), LOW(r), var));
        PUSHREF(appuni_rec(HIGH(l), HIGH(r), var));
        lev = LEVEL_l;
      } else if (LEVEL_l < LEVEL_r) {
        if (LEVEL_l == LEVEL_var) {
          lev = -1;
          var = HIGH(var);
        } else {
          lev = LEVEL_l;
        }
        PUSHREF(appuni_rec(LOW(l), r, var));
        PUSHREF(appuni_rec(HIGH(l), r, var));
      } else {
        if (LEVEL_r == LEVEL_var) {
          lev = -1;
          var = HIGH(var);
        } else {
          lev = LEVEL_r;
        }
        PUSHREF(appuni_rec(l, LOW(r), var));
        PUSHREF(appuni_rec(l, HIGH(r), var));
      }
      if (lev == -1) {
        int r2 = READREF(2), r1 = READREF(1);
        switch (applyop) {
          case bddop_and:
            res = and_rec(r2, r1);
            break;
          case bddop_or:
            res = or_rec(r2, r1);
            break;
          default:
            res = apply_rec(r2, r1);
            break;
        }
      } else {
        res = bdd_makenode(lev, READREF(2), READREF(1));
      }

      POPREF(2);

      appexcache.store(hash, l, r, appexid, res, cachestats);
    }

    return res;
  }

  private int unique_rec(int r, int q) {
    int res;
    int LEVEL_r, LEVEL_q;

    LEVEL_r = LEVEL(r);
    LEVEL_q = LEVEL(q);
    if (LEVEL_r > LEVEL_q) {
      // Skipped a quantified node, answer is zero.
      return BDDZERO;
    }

    if (r < 2 || q < 2) {
      return r;
    }

    int hash = QUANTHASH(r, quantid);
    int cached = quantcache.lookup(hash, r, 0, quantid, cachestats);
    if (cached >= 0) return cached;

    if (LEVEL_r == LEVEL_q) {
      PUSHREF(unique_rec(LOW(r), HIGH(q)));
      PUSHREF(unique_rec(HIGH(r), HIGH(q)));
      res = apply_rec(READREF(2), READREF(1));
    } else {
      PUSHREF(unique_rec(LOW(r), q));
      PUSHREF(unique_rec(HIGH(r), q));
      res = bdd_makenode(LEVEL(r), READREF(2), READREF(1));
    }

    POPREF(2);

    quantcache.store(hash, r, 0, quantid, res, cachestats);

    return res;
  }

  /**
   * Variant of {@link #quant_rec(int)} specialized for when {@link #applyop} is {@link #bddop_or},
   * and using {@link #orAll_rec(int[])}.
   */
  private int exist_rec(int r) {
    if (r < 2 || LEVEL(r) > quantlast) {
      return r;
    }

    int hash = QUANTHASH(r, quantid);
    int cached = quantcache.lookup(hash, r, 0, quantid, cachestats);
    if (cached >= 0) return cached;

    int res = -1; // indicates that it has not been set.

    if (INVARSET(LEVEL(r))) {
      // The root node of this BDD is meant to be erased. Collect all its non-erased children
      // and combine them. We can short-circuit any time we find an erased child that is BDDONE.
      int pushedRefs = 0;
      IntStack toProcess = new IntStack(); // all have roots that will be erased
      IntSet processed = new IntHashSet(); // nothing gets in toProcess if not new here
      IntSet toOr = new IntHashSet(); // will be orAll'd to get the final result.
      processed.add(r);
      toProcess.push(r);
      while (!toProcess.isEmpty()) {
        int bdd = toProcess.pop();
        int left = LOW(bdd);
        int right = HIGH(bdd);
        if (left == BDDONE || right == BDDONE) {
          // Short-circuit to true.
          res = BDDONE;
          break;
        }
        if (left == BDDZERO) {
          // do nothing
        } else if (INVARSET(LEVEL(left))) {
          if (processed.add(left)) {
            // No need to push a ref, since bdd is referenced.
            toProcess.push(left);
          }
        } else {
          int quantLeft = exist_rec(left);
          if (quantLeft == BDDONE) {
            res = BDDONE;
            break;
          }
          PUSHREF(quantLeft);
          toOr.add(quantLeft);
          ++pushedRefs;
        }
        if (right == BDDZERO) {
          // do nothing
        } else if (INVARSET(LEVEL(right))) {
          if (processed.add(right)) {
            // No need to push a ref, since bdd is referenced.
            toProcess.push(right);
          }
        } else {
          int quantRight = exist_rec(right);
          if (quantRight == BDDONE) {
            res = BDDONE;
            break;
          }
          PUSHREF(quantRight);
          toOr.add(quantRight);
          ++pushedRefs;
        }
      }
      if (res == -1) {
        res = orAll_rec(toOr.toArray());
      } // else it was set above and we can skip the orAll
      POPREF(pushedRefs);
    } else {
      PUSHREF(exist_rec(LOW(r)));
      PUSHREF(exist_rec(HIGH(r)));
      res = bdd_makenode(LEVEL(r), READREF(2), READREF(1));
      POPREF(2);
    }

    quantcache.store(hash, r, 0, quantid, res, cachestats);

    return res;
  }

  private int quant_rec(int r) {
    int res;

    if (r < 2 || LEVEL(r) > quantlast) {
      return r;
    }

    int hash = QUANTHASH(r, quantid);
    int cached = quantcache.lookup(hash, r, 0, quantid, cachestats);
    if (cached >= 0) return cached;

    PUSHREF(quant_rec(LOW(r)));
    PUSHREF(quant_rec(HIGH(r)));

    if (INVARSET(LEVEL(r))) {
      int r2 = READREF(2), r1 = READREF(1);
      switch (applyop) {
        case bddop_and:
          res = and_rec(r2, r1);
          break;
        case bddop_or:
          res = or_rec(r2, r1);
          break;
        default:
          res = apply_rec(r2, r1);
          break;
      }
    } else {
      res = bdd_makenode(LEVEL(r), READREF(2), READREF(1));
    }

    POPREF(2);

    quantcache.store(hash, r, 0, quantid, res, cachestats);

    return res;
  }

  private boolean testsVars_rec(int r) {

    if (r < 2 || LEVEL(r) > quantlast) {
      return false;
    } else if (INVARSET(LEVEL(r))) {
      return true;
    }

    int hash = QUANTHASH(r, quantid);
    int cached = quantcache.lookup(hash, r, 0, quantid, cachestats);
    if (cached >= 0) return cached == 1;

    boolean res = testsVars_rec(LOW(r)) || testsVars_rec(HIGH(r));

    quantcache.store(hash, r, 0, quantid, res ? 1 : 0, cachestats);

    return res;
  }

  private int project_rec(int r) {
    int res;

    if (r < 2) {
      return r;
    }

    int level = LEVEL(r);
    if (level > quantlast) {
      // existentially quantify all remaining variables
      return BDDONE;
    }

    int hash = QUANTHASH(r, quantid);
    int cached = quantcache.lookup(hash, r, 0, quantid, cachestats);
    if (cached >= 0) return cached;

    int low = PUSHREF(project_rec(LOW(r)));
    int high = PUSHREF(project_rec(HIGH(r)));

    if (INVARSET(level)) {
      res = bdd_makenode(level, low, high);
    } else {
      // existentially quantify
      res = or_rec(low, high);
    }

    POPREF(2);

    quantcache.store(hash, r, 0, quantid, res, cachestats);

    return res;
  }

  private int bdd_constrain(int f, int c) {
    CHECK(f);
    CHECK(c);

    if (misccache == null) {
      misccache = FlatCacheI_init(cachesize);
    }
    miscid = CACHEID_CONSTRAIN;

    INITREF();
    int res = constrain_rec(f, c);
    checkresize();

    return res;
  }

  private int constrain_rec(int f, int c) {
    int res;

    if (ISONE(c)) {
      return f;
    } else if (ISCONST(f)) {
      return f;
    } else if (c == f) {
      return BDDONE;
    } else if (ISZERO(c)) {
      return BDDZERO;
    }

    int hash = CONSTRAINHASH(f, c);
    int cached = misccache.lookup(hash, f, c, miscid, cachestats);
    if (cached >= 0) return cached;

    if (LEVEL(f) == LEVEL(c)) {
      if (ISZERO(LOW(c))) {
        res = constrain_rec(HIGH(f), HIGH(c));
      } else if (ISZERO(HIGH(c))) {
        res = constrain_rec(LOW(f), LOW(c));
      } else {
        PUSHREF(constrain_rec(LOW(f), LOW(c)));
        PUSHREF(constrain_rec(HIGH(f), HIGH(c)));
        res = bdd_makenode(LEVEL(f), READREF(2), READREF(1));
        POPREF(2);
      }
    } else if (LEVEL(f) < LEVEL(c)) {
      PUSHREF(constrain_rec(LOW(f), c));
      PUSHREF(constrain_rec(HIGH(f), c));
      res = bdd_makenode(LEVEL(f), READREF(2), READREF(1));
      POPREF(2);
    } else {
      if (ISZERO(LOW(c))) {
        res = constrain_rec(f, HIGH(c));
      } else if (ISZERO(HIGH(c))) {
        res = constrain_rec(f, LOW(c));
      } else {
        PUSHREF(constrain_rec(f, LOW(c)));
        PUSHREF(constrain_rec(f, HIGH(c)));
        res = bdd_makenode(LEVEL(c), READREF(2), READREF(1));
        POPREF(2);
      }
    }

    misccache.store(hash, f, c, miscid, res, cachestats);

    return res;
  }

  private int bdd_compose(int f, int g, int var) {
    CHECK(f);
    CHECK(g);
    if (var < 0 || var >= bddvarnum) {
      bdd_error(BDD_VAR);
      return BDDZERO;
    }

    if (replacecache == null) {
      // compose_rec uses replacecache
      replacecache = FlatCacheI_init(cachesize);
    }
    if (applycache == null) {
      // compose_rec can call ite_rec, which uses applycache
      applycache = FlatCacheI_init(cachesize);
    }
    composelevel = bddvar2level[var];
    replaceid = (composelevel << 3) | CACHEID_COMPOSE;

    INITREF();
    int res = compose_rec(f, g);
    checkresize();
    return res;
  }

  private int compose_rec(int f, int g) {
    int res;

    if (LEVEL(f) > composelevel) {
      return f;
    }

    int hash = COMPOSEHASH(replaceid, f, g);
    int cached = replacecache.lookup(hash, f, g, replaceid, cachestats);
    if (cached >= 0) return cached;

    if (LEVEL(f) < composelevel) {
      if (LEVEL(f) == LEVEL(g)) {
        PUSHREF(compose_rec(LOW(f), LOW(g)));
        PUSHREF(compose_rec(HIGH(f), HIGH(g)));
        res = bdd_makenode(LEVEL(f), READREF(2), READREF(1));
      } else if (LEVEL(f) < LEVEL(g)) {
        PUSHREF(compose_rec(LOW(f), g));
        PUSHREF(compose_rec(HIGH(f), g));
        res = bdd_makenode(LEVEL(f), READREF(2), READREF(1));
      } else {
        PUSHREF(compose_rec(f, LOW(g)));
        PUSHREF(compose_rec(f, HIGH(g)));
        res = bdd_makenode(LEVEL(g), READREF(2), READREF(1));
      }
      POPREF(2);
    } else
    /*if (LEVEL(f) == composelevel) changed 2-nov-98 */ {
      res = ite_rec(g, HIGH(f), LOW(f));
    }

    replacecache.store(hash, f, g, replaceid, res, cachestats);

    return res;
  }

  private int bdd_veccompose(int f, bddPair pair) {
    CHECK(f);

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (replacecache == null) {
      replacecache = FlatCacheI_init(cachesize);
    }
    replacepair = pair.result;
    replaceid = (pair.id << 3) | CACHEID_VECCOMPOSE;
    replacelast = pair.last;

    INITREF();
    int res = veccompose_rec(f);
    checkresize();

    return res;
  }

  private int veccompose_rec(int f) {
    int res;

    if (LEVEL(f) > replacelast) {
      return f;
    }

    int hash = VECCOMPOSEHASH(replaceid, f);
    int cached = replacecache.lookup(hash, f, 0, replaceid, cachestats);
    if (cached >= 0) return cached;

    PUSHREF(veccompose_rec(LOW(f)));
    PUSHREF(veccompose_rec(HIGH(f)));
    res = ite_rec(replacepair[LEVEL(f)], READREF(1), READREF(2));
    POPREF(2);

    replacecache.store(hash, f, 0, replaceid, res, cachestats);

    return res;
  }

  private int bdd_exist(int r, int var) {
    CHECK(r);
    CHECK(var);

    if (var < 2) /* Empty set */ {
      return r;
    }
    if (varset2vartable(var) < 0) {
      return BDDZERO;
    }

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (quantcache == null) {
      quantcache = FlatCacheI_init(cachesize);
    }
    applyop = bddop_or;
    quantid = (var << 3) | CACHEID_EXIST; /* FIXME: range */

    INITREF();
    int res = exist_rec(r);
    checkresize();

    return res;
  }

  private boolean bdd_testsVars(int r, int var) {
    CHECK(r);
    CHECK(var);

    if (var < 2) {
      // bdd.exist(var) is equal to bdd if var is zero or one
      return false;
    }
    if (varset2vartable(var) < 0) {
      return false; // error converting var to vartable
    }

    if (quantcache == null) {
      quantcache = FlatCacheI_init(cachesize);
    }
    quantid = (var << 3) | CACHEID_TESTS_CONSTRAINT; /* FIXME: range */

    return testsVars_rec(r);
  }

  private int bdd_project(int r, int var) {
    CHECK(r);
    CHECK(var);

    if (var < 2) /* Empty set */ {
      // projecting onto an empty set of variables means existentially
      // quantifying all variables.
      return r == BDDZERO ? BDDZERO : BDDONE;
    }
    if (varset2vartable(var) < 0) {
      return BDDZERO;
    }

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (quantcache == null) {
      quantcache = FlatCacheI_init(cachesize);
    }
    quantid = (var << 3) | CACHEID_PROJECT;

    INITREF();
    int res = project_rec(r);
    checkresize();

    return res;
  }

  private int bdd_forall(int r, int var) {
    CHECK(r);
    CHECK(var);

    if (var < 2) /* Empty set */ {
      return r;
    }
    if (varset2vartable(var) < 0) {
      return BDDZERO;
    }

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (quantcache == null) {
      quantcache = FlatCacheI_init(cachesize);
    }
    quantid = (var << 3) | CACHEID_FORALL;
    applyop = bddop_and;

    INITREF();
    int res = quant_rec(r);
    checkresize();

    return res;
  }

  private int bdd_unique(int r, int var) {
    CHECK(r);
    CHECK(var);

    if (var < 2) /* Empty set */ {
      return r;
    }

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (quantcache == null) {
      quantcache = FlatCacheI_init(cachesize);
    }
    quantid = (var << 3) | CACHEID_UNIQUE;
    applyop = bddop_xor;

    INITREF();
    int res = unique_rec(r, var);
    checkresize();

    return res;
  }

  private int bdd_restrict(int r, int var) {
    CHECK(r);
    CHECK(var);

    if (var < 2) /* Empty set */ {
      return r;
    }
    if (varset2svartable(var) < 0) {
      return BDDZERO;
    }

    if (misccache == null) {
      misccache = FlatCacheI_init(cachesize);
    }
    miscid = (var << 3) | CACHEID_RESTRICT;

    INITREF();
    int res = restrict_rec(r);
    checkresize();

    return res;
  }

  private int restrict_rec(int r) {
    int res;

    if (ISCONST(r) || LEVEL(r) > quantlast) {
      return r;
    }

    int hash = RESTRHASH(r, miscid);
    int cached = misccache.lookup(hash, r, 0, miscid, cachestats);
    if (cached >= 0) return cached;

    if (INSVARSET(LEVEL(r))) {
      if (quantvarset[LEVEL(r)] > 0) {
        res = restrict_rec(HIGH(r));
      } else {
        res = restrict_rec(LOW(r));
      }
    } else {
      PUSHREF(restrict_rec(LOW(r)));
      PUSHREF(restrict_rec(HIGH(r)));
      res = bdd_makenode(LEVEL(r), READREF(2), READREF(1));
      POPREF(2);
    }

    misccache.store(hash, r, 0, miscid, res, cachestats);

    return res;
  }

  private int bdd_simplify(int f, int d) {
    CHECK(f);
    CHECK(d);

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    applyop = bddop_or;

    INITREF();
    int res = simplify_rec(f, d);
    checkresize();

    return res;
  }

  private int simplify_rec(int f, int d) {
    int res;

    if (ISONE(d) || ISCONST(f)) {
      return f;
    } else if (d == f) {
      return BDDONE;
    } else if (ISZERO(d)) {
      return BDDZERO;
    }

    int hash = APPLYHASH(f, d, bddop_simplify);
    int cached = applycache.lookup(hash, f, d, bddop_simplify, cachestats);
    if (cached >= 0) return cached;

    if (LEVEL(f) == LEVEL(d)) {
      if (ISZERO(LOW(d))) {
        res = simplify_rec(HIGH(f), HIGH(d));
      } else if (ISZERO(HIGH(d))) {
        res = simplify_rec(LOW(f), LOW(d));
      } else {
        PUSHREF(simplify_rec(LOW(f), LOW(d)));
        PUSHREF(simplify_rec(HIGH(f), HIGH(d)));
        res = bdd_makenode(LEVEL(f), READREF(2), READREF(1));
        POPREF(2);
      }
    } else if (LEVEL(f) < LEVEL(d)) {
      PUSHREF(simplify_rec(LOW(f), d));
      PUSHREF(simplify_rec(HIGH(f), d));
      res = bdd_makenode(LEVEL(f), READREF(2), READREF(1));
      POPREF(2);
    } else /* LEVEL(d) < LEVEL(f) */ {
      PUSHREF(or_rec(LOW(d), HIGH(d))); /* Exist quant */
      res = simplify_rec(f, READREF(1));
      POPREF(1);
    }

    applycache.store(hash, f, d, bddop_simplify, res, cachestats);

    return res;
  }

  private int bdd_support(int r) {
    int res = 1;

    CHECK(r);

    if (r < 2) {
      return BDDONE;
    }

    /* On-demand allocation of support set */
    if (supportSet.length < bddvarnum) {
      supportSet = new int[bddvarnum];
      supportID = 0;
    }

    /* Update global variables used to speed up bdd_support()
     * - instead of always memsetting support to zero, we use
     *   a change counter.
     * - and instead of reading the whole array afterwards, we just
     *   look from 'min' to 'max' used BDD variables.
     */
    if (supportID == 0x0FFFFFFF) {
      /* We probably don't get here -- but let's just be sure */
      for (int i = 0; i < bddvarnum; ++i) {
        supportSet[i] = 0;
      }
      supportID = 0;
    }
    ++supportID;
    supportMin = LEVEL(r);
    supportMax = supportMin;

    support_rec(r, supportSet);
    bdd_unmark(r);

    for (int n = supportMax; n >= supportMin; --n) {
      if (supportSet[n] == supportID) {
        int tmp;
        bdd_addref(res);
        tmp = bdd_makenode(n, BDDZERO, res);
        bdd_delref(res);
        res = tmp;
      }
    }

    return res;
  }

  private void support_rec(int r, int[] support) {

    if (r < 2) {
      return;
    }

    if (MARKED(r) || LOW(r) == INVALID_BDD) {
      return;
    }

    support[LEVEL(r)] = supportID;

    if (LEVEL(r) > supportMax) {
      supportMax = LEVEL(r);
    }

    SETMARK(r);

    support_rec(LOW(r), support);
    support_rec(HIGH(r), support);
  }

  private int bdd_appall(int l, int r, int opr, int var) {
    CHECK(l);
    CHECK(r);
    CHECK(var);

    if (opr < 0 || opr > bddop_invimp) {
      bdd_error(BDD_OP);
      return BDDZERO;
    }

    if (var < 2) /* Empty set */ {
      return bdd_apply(l, r, opr);
    }
    if (varset2vartable(var) < 0) {
      return BDDZERO;
    }

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (appexcache == null) {
      appexcache = FlatCacheI_init(cachesize);
    }
    if (quantcache == null) {
      quantcache = FlatCacheI_init(cachesize);
    }
    applyop = bddop_and;
    appexop = opr;
    appexid = (var << 5) | (appexop << 1) | 1; /* FIXME: range! */
    quantid = (appexid << 3) | CACHEID_APPAL;

    INITREF();
    int res = appquant_rec(l, r);
    checkresize();

    return res;
  }

  private int bdd_appuni(int l, int r, int opr, int var) {
    CHECK(l);
    CHECK(r);
    CHECK(var);

    if (opr < 0 || opr > bddop_invimp) {
      bdd_error(BDD_OP);
      return BDDZERO;
    }

    if (var < 2) /* Empty set */ {
      return bdd_apply(l, r, opr);
    }

    if (applycache == null) {
      applycache = FlatCacheI_init(cachesize);
    }
    if (appexcache == null) {
      appexcache = FlatCacheI_init(cachesize);
    }
    if (quantcache == null) {
      quantcache = FlatCacheI_init(cachesize);
    }
    applyop = bddop_xor;
    appexop = opr;
    appexid = (var << 5) | (appexop << 1) | 1; /* FIXME: range! */
    quantid = (appexid << 3) | CACHEID_APPUN;

    INITREF();
    int res = appuni_rec(l, r, var);
    checkresize();

    return res;
  }

  private int bdd_satone(int r) {
    int res;

    CHECK(r);
    if (r < 2) {
      return r;
    }

    INITREF();
    res = satone_rec(r);

    checkresize();
    return res;
  }

  private int satone_rec(int r) {
    if (ISCONST(r)) {
      return r;
    }

    int lo = LOW(r);
    int hi = HIGH(r);
    boolean useHi = ISZERO(lo);
    return bdd_makesatnode(LEVEL(r), satone_rec(useHi ? hi : lo), !useHi);
  }

  private int bdd_satoneset(int r, int var, int pol) {
    int res;

    CHECK(r);
    if (ISZERO(r)) {
      return r;
    }
    if (!ISCONST(pol)) {
      bdd_error(BDD_ILLBDD);
      return BDDZERO;
    }

    INITREF();
    satPolarity = pol;
    res = satoneset_rec(r, var);

    checkresize();
    return res;
  }

  private int satoneset_rec(int r, int var) {
    if (ISCONST(r) && ISCONST(var)) {
      return r;
    }

    if (LEVEL(r) < LEVEL(var)) {
      int lo = LOW(r);
      int hi = HIGH(r);
      boolean useHi = ISZERO(lo);
      return bdd_makesatnode(LEVEL(r), satoneset_rec(useHi ? hi : lo, var), !useHi);
    } else if (LEVEL(var) < LEVEL(r)) {
      return bdd_makesatnode(LEVEL(var), satoneset_rec(r, HIGH(var)), satPolarity != BDDONE);
    } else /* LEVEL(r) == LEVEL(var) */ {
      int lo = LOW(r);
      int hi = HIGH(r);
      boolean useHi = ISZERO(lo);
      return bdd_makesatnode(LEVEL(r), satoneset_rec(useHi ? hi : lo, HIGH(var)), !useHi);
    }
  }

  private int bdd_fullsatone(int r) {
    int res;

    CHECK(r);
    if (r == BDDZERO) {
      return 0;
    }

    INITREF();
    res = fullsatone_rec(r);

    for (int v = LEVEL(r) - 1; v >= 0; v--) {
      res = bdd_makesatnode(v, res, true);
    }

    checkresize();
    return res;
  }

  private int fullsatone_rec(int r) {
    if (r < 2) {
      return r;
    }

    int lo = LOW(r);
    int hi = HIGH(r);
    boolean useLo = lo != BDDZERO;
    int child = fullsatone_rec(useLo ? lo : hi);
    for (int v = LEVEL(child) - 1; v > LEVEL(r); v--) {
      child = bdd_makesatnode(v, child, true);
    }
    return bdd_makesatnode(LEVEL(r), child, useLo);
  }

  private BitSet bdd_minassignmentbits(int r) {
    CHECK(r);
    BitSet set = new BitSet(bddvarnum);
    minassignmentbits_rec(set, r);
    return set;
  }

  private void minassignmentbits_rec(BitSet set, int r) {
    if (r < 2) {
      return;
    }

    int lo = LOW(r);
    int hi = HIGH(r);
    boolean useHi = lo == BDDZERO;
    if (useHi) {
      set.set(LEVEL(r));
      minassignmentbits_rec(set, hi);
    } else {
      minassignmentbits_rec(set, lo);
    }
  }

  private int bdd_randomfullsatone(int r, int seed) {
    int res;

    CHECK(r);
    if (r == BDDZERO) {
      return BDDZERO;
    }

    INITREF();
    res = randomfullsatone_rec(r, 0, seed);

    checkresize();
    return res;
  }

  // Makes a node for the purposes of a satisfying assignment. The resulting node tests the given
  // variable, has the given child at the branch indicated by {@code useLow}, and has the other
  // branch false.
  private int bdd_makesatnode(int variable, int child, boolean useLow) {
    assert LEVEL(child) > variable; // or the BDD is out of order.

    PUSHREF(child);
    int ret = bdd_makenode(variable, useLow ? child : BDDZERO, useLow ? BDDZERO : child);
    POPREF(1);
    return ret;
  }

  // Recursively builds a full satisfying assignment for the BDD corresponding to r, using all
  // variables from level..bddvarnum.
  //
  // Invariants:
  // * r can be anything, including BDDZERO or BDDONE.
  // * level <= LEVEL(r)
  // * seed is a deterministic function of the branches taken in the eventual parent BDD
  //   (levels 0..level-1) and the original seed.
  //
  // The returned BDD tests all variables from level..bddvarnum.
  private int randomfullsatone_rec(int r, int level, int seed) {
    if (level == bddvarnum) {
      // Reached past the last variable aka, r is zero or one.
      assert r == BDDZERO || r == BDDONE; // sanity check.
      return r;
    }

    // To be deterministic, we cannot use the BDD ID (r). Indeed, the only thing we can really use
    // is LEVEL(r) [aka, which variable is tested in this node] as well as the path we take through
    // the BDD. This is a lot like netconan, but no need for cryptographic security.
    int newSeed = seed * 31 + level;
    boolean preferLo = (newSeed & 65536) == 0;
    if (level < LEVEL(r)) {
      // The BDD r is the same no matter which branch at the current level is taken. Pick one
      // randomly.
      if (preferLo) {
        // Change newSeed for recursive cases based on path.
        newSeed = newSeed * 23;
      }
      int next = randomfullsatone_rec(r, level + 1, newSeed);
      return bdd_makesatnode(level, next, preferLo);
    }

    assert level == LEVEL(r); // sanity check

    int lo = LOW(r);
    int hi = HIGH(r);
    // Even though we prefer low branch randomly, we can't use it if the low branch is BDDZERO.
    // Similarly, even if we prefer the high branch we must take low branch if hi is BDDZERO.
    boolean useLo = (lo != BDDZERO && preferLo || hi == BDDZERO);
    if (useLo) {
      // Change newSeed for recursive cases based on path.
      newSeed *= 23;
    }
    int next = randomfullsatone_rec(useLo ? lo : hi, level + 1, newSeed);
    return bdd_makesatnode(level, next, useLo);
  }

  private void bdd_gbc_rehash() {
    bddfreepos = 0;
    bddfreenum = 0;

    for (int n = bddnodesize - 1; n >= 2; n--) {
      if (LOW(n) != INVALID_BDD) {
        int hash2;

        hash2 = NODEHASH(LEVEL(n), LOW(n), HIGH(n));
        SETNEXT(n, HASH(hash2));
        SETHASH(hash2, n);
      } else {
        SETNEXT(n, bddfreepos);
        bddfreepos = n;
        bddfreenum++;
      }
    }
  }

  private void INITREF() {
    bddrefstackTop = 0;
  }

  private int PUSHREF(int a) {
    if (bddrefstackTop >= bddrefstack.length) {
      bddrefstack = Arrays.copyOf(bddrefstack, bddrefstack.length * 2);
    }
    bddrefstack[bddrefstackTop++] = a;
    return a;
  }

  private int READREF(int a) {
    return bddrefstack[bddrefstackTop - a];
  }

  private void POPREF(int a) {
    bddrefstackTop -= a;
  }

  private void SETREF(int index, int a) {
    bddrefstack[index] = a;
  }

  private int bdd_nodecount(int r) {
    int[] num = new int[1];

    CHECK(r);

    bdd_markcount(r, num);
    bdd_unmark(r);

    return num[0];
  }

  private int bdd_anodecount(int[] r) {
    int[] cou = new int[1];

    for (int i : r) {
      bdd_markcount(i, cou);
    }

    for (int i : r) {
      bdd_unmark(i);
    }

    return cou[0];
  }

  private int[] bdd_varprofile(int r) {
    CHECK(r);

    int[] varprofile = new int[bddvarnum];

    varprofile_rec(r, varprofile);
    bdd_unmark(r);
    return varprofile;
  }

  private void varprofile_rec(int r, int[] varprofile) {

    if (r < 2) {
      return;
    }

    if (MARKED(r)) {
      return;
    }

    varprofile[bddlevel2var[LEVEL(r)]]++;
    SETMARK(r);

    varprofile_rec(LOW(r), varprofile);
    varprofile_rec(HIGH(r), varprofile);
  }

  private double bdd_pathcount(int r) {
    CHECK(r);

    miscid = CACHEID_PATHCOU;

    if (countcache == null) {
      countcache = BddCacheBigInteger_init(cachesize);
    }

    return bdd_pathcount_rec(r).doubleValue();
  }

  private BigInteger bdd_pathcount_rec(int r) {
    if (ISZERO(r)) {
      return BigInteger.ZERO;
    } else if (ISONE(r)) {
      return BigInteger.ONE;
    }

    int hash = PATHCOUHASH(r, miscid);
    BigIntegerBddCacheData entry = BddCache_lookupBigInteger(countcache, hash);
    if (entry.a == r && entry.c == miscid) {
      if (CACHESTATS) {
        cachestats.opHit++;
      }
      return entry.value;
    }

    if (CACHESTATS) {
      cachestats.opMiss++;
    }
    BigInteger size = bdd_pathcount_rec(LOW(r)).add(bdd_pathcount_rec(HIGH(r)));

    if (CACHESTATS && entry.a != -1) {
      cachestats.opOverwrite++;
    }
    entry.a = r;
    entry.c = miscid;
    entry.value = size;
    entry.hash = hash;

    return size;
  }

  private BigInteger bdd_satcount(int r) {
    CHECK(r);

    if (countcache == null) {
      countcache = BddCacheBigInteger_init(cachesize);
    }

    miscid = CACHEID_SATCOU;
    return satcount_rec(r).shiftLeft(LEVEL(r));
  }

  private BigInteger satcount_rec(int root) {
    if (ISZERO(root)) {
      return BigInteger.ZERO;
    } else if (ISONE(root)) {
      return BigInteger.ONE;
    }

    int hash = SATCOUHASH(root, miscid);
    BigIntegerBddCacheData entry = BddCache_lookupBigInteger(countcache, hash);
    if (entry.a == root && entry.c == miscid) {
      if (CACHESTATS) {
        cachestats.opHit++;
      }
      return entry.value;
    }

    if (CACHESTATS) {
      cachestats.opMiss++;
    }

    int low = LOW(root);
    int high = HIGH(root);
    BigInteger size =
        satcount_rec(low)
            .shiftLeft(LEVEL(low) - LEVEL(root) - 1)
            .add(satcount_rec(high).shiftLeft(LEVEL(high) - LEVEL(root) - 1));

    if (CACHESTATS && entry.a != -1) {
      cachestats.opOverwrite++;
    }
    entry.a = root;
    entry.c = miscid;
    entry.value = size;
    entry.hash = hash;

    return size;
  }

  void bdd_gbc() {
    long c2, c1 = System.currentTimeMillis();

    // if (gbc_handler != NULL)
    {
      gcstats.nodes = bddnodesize;
      gcstats.freenodes = bddfreenum;
      gcstats.time = 0;
      gcstats.sumtime = gbcclock;
      gcstats.num = gbcollectnum;
      gbc_handler(true, gcstats);
    }

    for (int i = 0; i < bddrefstackTop; i++) {
      bdd_mark(bddrefstack[i]);
    }

    for (int n = 0; n < bddnodesize; n++) {
      if (HASREF(n)) {
        bdd_mark(n);
      }
    }
    Arrays.fill(bddhash, 0);

    bddfreepos = 0;
    bddfreenum = 0;

    for (int n = bddnodesize - 1; n >= 2; n--) {

      if (MARKED(n) && LOW(n) != INVALID_BDD) {
        int hash2;

        UNMARK(n);
        hash2 = NODEHASH(LEVEL(n), LOW(n), HIGH(n));
        SETNEXT(n, HASH(hash2));
        SETHASH(hash2, n);
      } else {
        SETLOW(n, INVALID_BDD);
        SETNEXT(n, bddfreepos);
        bddfreepos = n;
        bddfreenum++;
      }
    }

    if (bddfreenum > 0) {
      // Don't reset or clean caches if we didn't free any nodes.

      if (FLUSH_CACHE_ON_GC) {
        bdd_operator_reset();
      } else {
        bdd_operator_clean();
      }
    }

    c2 = System.currentTimeMillis();
    gbcclock += c2 - c1;
    gbcollectnum++;

    // if (gbc_handler != NULL)
    {
      gcstats.nodes = bddnodesize;
      gcstats.freenodes = bddfreenum;
      gcstats.reusednodes = reusedBDDs;
      reusedBDDs = 0;
      gcstats.time = c2 - c1;
      gcstats.sumtime = gbcclock;
      gcstats.num = gbcollectnum;
      gbc_handler(false, gcstats);
    }

    // validate_all();
  }

  private int bdd_addref(int root) {
    if (root == INVALID_BDD) {
      bdd_error(BDD_BREAK); /* distinctive */
    }
    if (root < 2 || !bddrunning) {
      return root;
    }
    if (VERIFY_ASSERTIONS) {
      if (root >= bddnodesize) {
        return bdd_error(BDD_ILLBDD);
      }
      if (LOW(root) == INVALID_BDD) {
        return bdd_error(BDD_ILLBDD);
      }
    }

    INCREF(root);
    return root;
  }

  private int bdd_delref(int root) {
    if (root == INVALID_BDD) {
      bdd_error(BDD_BREAK); /* distinctive */
    }
    if (root < 2 || !bddrunning) {
      return root;
    }
    if (VERIFY_ASSERTIONS) {
      if (root >= bddnodesize) {
        return bdd_error(BDD_ILLBDD);
      }
      if (LOW(root) == INVALID_BDD) {
        return bdd_error(BDD_ILLBDD);
      }
      /* if the following line is present, fails there much earlier */
      if (!HASREF(root)) {
        bdd_error(BDD_BREAK); /* distinctive */
      }
    }

    DECREF(root);
    return root;
  }

  private void bdd_mark(int i) {

    if (i < 2) {
      return;
    }

    if (MARKED(i) || LOW(i) == INVALID_BDD) {
      return;
    }

    SETMARK(i);

    bdd_mark(LOW(i));
    bdd_mark(HIGH(i));
  }

  private void bdd_markcount(int i, int[] cou) {

    if (i < 2) {
      return;
    }

    if (MARKED(i) || LOW(i) == INVALID_BDD) {
      return;
    }

    SETMARK(i);
    cou[0] += 1;

    bdd_markcount(LOW(i), cou);
    bdd_markcount(HIGH(i), cou);
  }

  private void bdd_unmark(int i) {

    if (i < 2) {
      return;
    }

    if (!MARKED(i) || LOW(i) == INVALID_BDD) {
      return;
    }
    UNMARK(i);

    bdd_unmark(LOW(i));
    bdd_unmark(HIGH(i));
  }

  private int bdd_makenode(int level, int low, int high) {
    assert (ISCONST(low) || level < LEVEL(low)) && (ISCONST(high) || level < LEVEL(high));

    /* check whether childs are equal */
    if (low == high) {
      if (CACHESTATS) {
        cachestats.uniqueTrivial++;
      }
      return low;
    }

    if (CACHESTATS) {
      cachestats.uniqueAccess++;
    }

    /* Try to find an existing node of this kind */
    int hash2 = NODEHASH(level, low, high);
    int res = bddhash[hash2];

    while (res != 0) {
      int base = res * __node_size;
      if ((bddnodes[base] & LEV_MASK) == level
          && bddnodes[base + 1] == low
          && bddnodes[base + 2] == high) {
        if (CACHESTATS) {
          cachestats.uniqueHit++;
        }
        return res;
      }

      res = bddnodes[base + 3]; // NEXT
      if (CACHESTATS) {
        cachestats.uniqueChain++;
      }
    }

    /* No existing node => build one */
    if (CACHESTATS) {
      cachestats.uniqueMiss++;
    }

    /* Any free nodes to use ? */
    if (bddfreepos == 0) {
      if (bdderrorcond != 0) {
        return 0;
      }

      /* Try to allocate more nodes */
      bdd_gbc();

      if ((bddfreenum * 100L) / bddnodesize <= minfreenodes) {
        bdd_noderesize(true);
        hash2 = NODEHASH(level, low, high);
      }

      /* Panic if that is not possible */
      if (bddfreepos == 0) {
        bdd_error(BDD_NODENUM);
        bdderrorcond = Math.abs(BDD_NODENUM);
        return 0;
      }
    }

    /* Build new node */
    res = bddfreepos;
    int resBase = res * __node_size;
    bddfreepos = bddnodes[resBase + 3]; // NEXT(bddfreepos)
    bddfreenum--;
    bddproduced++;
    newNodeIndex(res);

    bddnodes[resBase] = level; // SETLEVELANDMARK - mark is 0 for new nodes
    bddnodes[resBase + 1] = low; // SETLOW
    bddnodes[resBase + 2] = high; // SETHIGH

    /* Insert node */
    bddnodes[resBase + 3] = bddhash[hash2]; // SETNEXT(res, HASH(hash2))
    bddhash[hash2] = res; // SETHASH(hash2, res)

    return res;
  }

  /** Called whenever a new BDD node is created, with the given (previously free) index. */
  protected void newNodeIndex(int index) {}

  private void bdd_noderesize(boolean doRehash) {
    int oldsize = bddnodesize;
    int newsize = bddnodesize;

    if (increasefactor > 0) {
      newsize += (int) (newsize * increasefactor);
    } else {
      newsize = newsize << 1;
    }

    if (newsize < 0 || newsize > MAX_NODESIZE) {
      // prevent integer overflow
      newsize = MAX_NODESIZE;
    }

    if (newsize <= oldsize) {
      return;
    }

    doResize(doRehash, oldsize, newsize);
  }

  @Override
  public int setNodeTableSize(int size) {
    int old = bddnodesize;
    doResize(true, old, size);
    return old;
  }

  private void doResize(boolean doRehash, int oldsize, int newsize) {
    newsize = Integer.highestOneBit(newsize - 1) << 1; // round up to power of 2
    if (newsize < 16) newsize = 16;
    if (newsize <= oldsize) {
      return;
    }

    long resizeStartTime = System.currentTimeMillis();

    bddnodes = Arrays.copyOf(bddnodes, newsize * __node_size);
    bddhash = new int[newsize]; // fresh allocation is zeroed (all hash buckets empty)
    bddnodesize = newsize;
    bddnodemask = newsize - 1;

    // Initialize the new nodes in parallel
    IntStream.range(oldsize, newsize)
        .parallel()
        .forEach(
            n -> {
              SETLOW(n, INVALID_BDD);
              SETNEXT(n, n + 1);
            });

    SETNEXT(bddnodesize - 1, bddfreepos);
    bddfreepos = oldsize;
    bddfreenum += bddnodesize - oldsize;

    if (doRehash) {
      bdd_gbc_rehash();
    }
    bddresized = true;
    long resizeTime = System.currentTimeMillis() - resizeStartTime;
    sumResizeTime += resizeTime;
    LOGGER.info(
        "Resized node table from {} to {} in {}s / {}s total",
        oldsize,
        newsize,
        resizeTime / 1000.0,
        sumResizeTime / 1000.0);
  }

  @Override
  protected void initialize(int initnodesize, int cs) {
    if (bddrunning) {
      bdd_error(BDD_RUNNING);
    }

    bddnodesize = Integer.highestOneBit(initnodesize - 1) << 1; // round up to power of 2
    if (bddnodesize < 16) bddnodesize = 16;
    bddnodemask = bddnodesize - 1;

    bddnodes = new int[bddnodesize * __node_size];
    bddhash = new int[bddnodesize];

    bddresized = false;

    for (int n = 0; n < bddnodesize; n++) {
      SETLOW(n, INVALID_BDD);
      // SETREFCOU(n, 0);
      // SETHASH(n, 0);
      // SETLEVEL(n, 0);
      SETNEXT(n, n + 1);
    }
    SETNEXT(bddnodesize - 1, 0);

    SETMAXREF(0);
    SETMAXREF(1);
    SETLOW(0, 0);
    SETHIGH(0, 0);
    SETLOW(1, 1);
    SETHIGH(1, 1);

    bdd_operator_init();

    bddfreepos = 2;
    bddfreenum = bddnodesize - 2;
    bddrunning = true;
    bddvarnum = 0;
    gbcollectnum = 0;
    gbcclock = 0;
    cachesize = cs;

    bdderrorcond = 0;

    // bdd_gbc_hook(bdd_default_gbchandler);
    // bdd_error_hook(bdd_default_errhandler);
    // bdd_resize_hook(NULL);
    bdd_pairs_init();
  }

  /* Hash value modifiers to distinguish between entries in misccache */
  private static final int CACHEID_CONSTRAIN = 0x0;
  private static final int CACHEID_RESTRICT = 0x1;
  private static final int CACHEID_SATCOU = 0x2;
  private static final int CACHEID_SATCOULN = 0x3;
  private static final int CACHEID_PATHCOU = 0x4;

  /* Hash value modifiers for replace/compose. Max 8 values */
  private static final int CACHEID_REPLACE = 0x0;
  private static final int CACHEID_COMPOSE = 0x1;
  private static final int CACHEID_VECCOMPOSE = 0x2;
  private static final int CACHEID_CORRECTIFY = 0x3;
  private static final int CACHEID_TRANSFORM = 0x4;

  /* Hash value modifiers for quantification. Max 8 values */
  private static final int CACHEID_EXIST = 0x0;
  private static final int CACHEID_FORALL = 0x1;
  private static final int CACHEID_UNIQUE = 0x2;
  private static final int CACHEID_APPEX = 0x3;
  private static final int CACHEID_APPAL = 0x4;
  private static final int CACHEID_APPUN = 0x5;
  private static final int CACHEID_PROJECT = 0x6;
  private static final int CACHEID_TESTS_CONSTRAINT = 0x7;

  /* Number of boolean operators */
  static final int OPERATOR_NUM = 11;

  /* Operator results - entry = left<<1 | right  (left,right in {0,1}) */
  private static final int[][] oprres = {
    {0, 0, 0, 1}, /* and                       ( & )         */
    {0, 1, 1, 0}, /* xor                       ( ^ )         */
    {0, 1, 1, 1}, /* or                        ( | )         */
    {1, 1, 1, 0}, /* nand                                    */
    {1, 0, 0, 0}, /* nor                                     */
    {1, 1, 0, 1}, /* implication               ( >> )        */
    {1, 0, 0, 1}, /* bi-implication                          */
    {0, 0, 1, 0}, /* difference /greater than  ( - ) ( > )   */
    {0, 1, 0, 0}, /* less than                 ( < )         */
    {1, 0, 1, 1}, /* inverse implication       ( << )        */
    {1, 1, 0, 0}, /* not                       ( ! )         */
  };

  private transient int applyop; /* Current operator for apply */
  private transient int appexop; /* Current operator for appex */
  private transient int appexid; /* Current cache id for appex */
  private transient int quantid; /* Current cache id for quantifications */
  private transient int[] quantvarset; /* Current variable set for quant. */
  private transient int quantvarsetID; /* Current id used in quantvarset */
  private transient int quantlast; /* Current last variable to be quant. */
  private transient int replaceid; /* Current cache id for replace */
  private transient int[] replacepair; /* Current replace pair */
  private transient int replacelast; /* Current last var. level to replace */
  private transient int composelevel; /* Current variable used for compose */
  private transient int miscid; /* Current cache id for other results */
  private transient int supportID; /* Current ID (true value) for support */
  private transient int supportMin; /* Min. used level in support calc. */
  private transient int supportMax; /* Max. used level in support calc. */
  private @Nonnull transient int[] supportSet; /* The found support set */
  private transient FlatCacheI applycache; /* Cache for apply results */
  private transient FlatCacheI quantcache; /* Cache for exist/forall results */
  private transient FlatCacheI appexcache; /* Cache for appex/appall results */
  private transient FlatCacheI replacecache; /* Cache for replace results */
  private transient FlatCacheI misccache; /* Cache for other results */
  private transient FlatCacheIte itecache; /* Cache for ITE results */
  private transient BddCache multiopcache; /* Cache for varargs operators */
  private transient BddCache countcache; /* Cache for count results */
  private int cacheratio;
  private transient int satPolarity;

  /* Used instead of local variable in order
  to avoid compiler warning about 'first'
  being clobbered by setjmp */

  private void bdd_operator_init() {
    quantvarsetID = 0;
    quantvarset = null;
    cacheratio = 0;
    supportSet = new int[0];
  }

  private void bdd_operator_reset() {
    FlatCacheI_reset(applycache);
    FlatCacheIte_reset(itecache);
    FlatCacheI_reset(quantcache);
    FlatCacheI_reset(appexcache);
    FlatCacheI_reset(replacecache);
    FlatCacheI_reset(misccache);
    BddCache_reset(multiopcache);
    BddCache_reset(countcache);
  }

  private void bdd_operator_clean() {
    Stream.<Runnable>of(
            () -> FlatCacheI_clean_ab(applycache),
            () -> FlatCacheIte_clean(itecache),
            () -> FlatCacheI_clean_a(quantcache),
            () -> FlatCacheI_clean_ab(appexcache),
            () -> FlatCacheI_clean_ab(replacecache),
            () -> FlatCacheI_clean_ab(misccache),
            () -> BddCache_clean_multiop(multiopcache),
            () -> BddCache_clean_d(countcache))
        .parallel()
        .forEach(Runnable::run);
  }

  private void bdd_operator_varresize() {
    quantvarset = new int[bddvarnum];

    // memset(quantvarset, 0, sizeof(int)*bddvarnum);
    quantvarsetID = 0;

    BddCache_reset(countcache);
  }

  @Override
  public int setCacheSize(int newcachesize) {
    int old = cachesize;
    FlatCacheI_resize_apply(newcachesize);
    FlatCacheIte_resize(newcachesize);
    quantcache = FlatCacheI_resize(quantcache, newcachesize);
    appexcache = FlatCacheI_resize(appexcache, newcachesize);
    replacecache = FlatCacheI_resize(replacecache, newcachesize);
    misccache = FlatCacheI_resize(misccache, newcachesize);
    BddCache_resize(multiopcache, newcachesize);
    BddCache_resize(countcache, newcachesize);
    return old;
  }

  private void bdd_operator_noderesize() {
    if (cacheratio > 0) {
      int newcachesize = bddnodesize / cacheratio;

      FlatCacheI_resize_apply(newcachesize);
      FlatCacheIte_resize(newcachesize);
      quantcache = FlatCacheI_resize(quantcache, newcachesize);
      appexcache = FlatCacheI_resize(appexcache, newcachesize);
      replacecache = FlatCacheI_resize(replacecache, newcachesize);
      misccache = FlatCacheI_resize(misccache, newcachesize);
      BddCache_resize(multiopcache, newcachesize);
      BddCache_resize(countcache, newcachesize);

      cachesize = newcachesize;
    }
  }

  private FlatCacheI FlatCacheI_init(int size) {
    size = Integer.highestOneBit(size - 1) << 1;
    if (size < 16) size = 16;
    return new FlatCacheI(size);
  }

  private BddCache BddCacheI_init(int size) {
    size = Integer.highestOneBit(size - 1) << 1; // round up to power of 2
    if (size < 16) size = 16;

    BddCache cache = new BddCache();
    cache.table = new BddCacheDataI[size];
    Arrays.parallelSetAll(cache.table, i -> new BddCacheDataI());
    cache.tablesize = size;
    cache.tablemask = size - 1;

    return cache;
  }

  private BddCache BddCacheMultiOp_init(int size) {
    size = Integer.highestOneBit(size - 1) << 1; // round up to power of 2
    if (size < 16) size = 16;

    BddCache cache = new BddCache();
    cache.table = new MultiOpBddCacheData[size];
    Arrays.parallelSetAll(cache.table, i -> new MultiOpBddCacheData());
    cache.tablesize = size;
    cache.tablemask = size - 1;

    return cache;
  }

  private BddCache BddCacheBigInteger_init(int size) {
    size = Integer.highestOneBit(size - 1) << 1; // round up to power of 2
    if (size < 16) size = 16;

    BddCache cache = new BddCache();
    cache.table = new BigIntegerBddCacheData[size];
    Arrays.parallelSetAll(cache.table, i -> new BigIntegerBddCacheData());
    cache.tablesize = size;
    cache.tablemask = size - 1;

    return cache;
  }

  private static void BddCache_done(BddCache cache) {
    if (cache == null) {
      return;
    }

    cache.table = null;
    cache.tablesize = 0;
  }

  /**
   * Returns the name of the type of {@link BddCache} that {@code cache} represents.
   *
   * <p>Slow. Should only be used in debugging contexts.
   */
  private String getCacheName(BddCache cache) {
    if (cache == countcache) {
      return "count";
    } else if (cache == multiopcache) {
      return "multiop";
    } else {
      return "unknown";
    }
  }

  private static <T extends BddCacheData> T[] reallocateAndResize(
      T[] oldTable, int newsize, IntFunction<T[]> newTable, Supplier<T> constructor) {
    int mask = newsize - 1;
    T[] ret = newTable.apply(newsize);
    Arrays.stream(oldTable).parallel().forEach(entry -> ret[entry.hash & mask] = entry);
    IntStream.range(0, newsize)
        .parallel()
        .forEach(
            i -> {
              if (ret[i] != null) {
                return;
              }
              ret[i] = constructor.get();
            });
    return ret;
  }

  private int BddCache_resize(BddCache cache, int newsize) {
    if (cache == null) {
      return 0;
    }

    if (CACHESTATS) {
      LOGGER.info(
          "Cache {} resize: {}/{} slots used",
          getCacheName(cache),
          cache.used(),
          cache.table.length);
    }

    newsize = Integer.highestOneBit(newsize - 1) << 1; // round up to power of 2
    if (newsize < 16) newsize = 16;

    if (cache.table instanceof BddCacheDataI[]) {
      cache.table =
          reallocateAndResize(cache.table, newsize, BddCacheDataI[]::new, BddCacheDataI::new);
    } else if (cache.table instanceof BigIntegerBddCacheData[]) {
      cache.table =
          reallocateAndResize(
              cache.table, newsize, BigIntegerBddCacheData[]::new, BigIntegerBddCacheData::new);
    } else if (cache.table instanceof MultiOpBddCacheData[]) {
      cache.table =
          reallocateAndResize(
              cache.table, newsize, MultiOpBddCacheData[]::new, MultiOpBddCacheData::new);
    } else {
      throw new IllegalStateException("unknown cache table type");
    }

    cache.tablesize = newsize;
    cache.tablemask = newsize - 1;

    return 0;
  }

  private static BddCacheDataI BddCache_lookupI(BddCache cache, int hash) {
    return (BddCacheDataI) cache.table[hash & cache.tablemask];
  }

  private static void FlatCacheI_reset(FlatCacheI cache) {
    if (cache == null) return;
    for (int i = 0; i < cache.data.length; i += 4) {
      cache.data[i] = -1;
    }
  }

  private void FlatCacheI_clean_ab(FlatCacheI cache) {
    if (cache == null) return;
    int[] data = cache.data;
    for (int i = 0; i < data.length; i += 4) {
      int a = data[i];
      if (a < 0) continue;
      if (LOW(a) == INVALID_BDD
          || (data[i + 1] != 0 && LOW(data[i + 1]) == INVALID_BDD)
          || LOW(data[i + 3]) == INVALID_BDD) {
        data[i] = -1;
      }
    }
  }

  private static FlatCacheI FlatCacheI_resize(FlatCacheI cache, int newsize) {
    if (cache == null) return null;
    newsize = Integer.highestOneBit(newsize - 1) << 1;
    if (newsize < 16) newsize = 16;
    return new FlatCacheI(newsize);
  }

  private void FlatCacheI_clean_a(FlatCacheI cache) {
    if (cache == null) return;
    int[] data = cache.data;
    for (int i = 0; i < data.length; i += 4) {
      int a = data[i];
      if (a < 0) continue;
      if (LOW(a) == INVALID_BDD || LOW(data[i + 3]) == INVALID_BDD) {
        data[i] = -1;
      }
    }
  }

  private void FlatCacheI_resize_apply(int newsize) {
    if (applycache == null) return;
    newsize = Integer.highestOneBit(newsize - 1) << 1;
    if (newsize < 16) newsize = 16;
    applycache = new FlatCacheI(newsize);
  }

  private static void FlatCacheIte_reset(FlatCacheIte cache) {
    if (cache == null) return;
    for (int i = 0; i < cache.data.length; i += 4) {
      cache.data[i] = -1;
    }
  }

  private void FlatCacheIte_clean(FlatCacheIte cache) {
    if (cache == null) return;
    int[] data = cache.data;
    for (int i = 0; i < data.length; i += 4) {
      int f = data[i];
      if (f < 0) continue;
      if (LOW(f) == INVALID_BDD
          || LOW(data[i + 1]) == INVALID_BDD
          || LOW(data[i + 2]) == INVALID_BDD
          || LOW(data[i + 3]) == INVALID_BDD) {
        data[i] = -1;
      }
    }
  }

  private void FlatCacheIte_resize(int newsize) {
    if (itecache == null) return;
    newsize = Integer.highestOneBit(newsize - 1) << 1;
    if (newsize < 16) newsize = 16;
    itecache = new FlatCacheIte(newsize);
  }

  private static BigIntegerBddCacheData BddCache_lookupBigInteger(BddCache cache, int hash) {
    return (BigIntegerBddCacheData) cache.table[hash & cache.tablemask];
  }

  private static MultiOpBddCacheData BddCache_lookupMultiOp(BddCache cache, int hash) {
    return (MultiOpBddCacheData) cache.table[hash & cache.tablemask];
  }

  private void BddCache_reset(BddCache cache) {
    if (cache == null) {
      return;
    }
    if (CACHESTATS) {
      LOGGER.info(
          "Cache {} reset: {}/{} slots used",
          getCacheName(cache),
          cache.used(),
          cache.table.length);
    }

    Arrays.stream(cache.table).parallel().forEach(e -> e.a = -1);
  }

  private void BddCache_clean_d(BddCache cache) {
    if (cache == null) {
      return;
    }
    Arrays.stream(cache.table)
        .parallel()
        .forEach(
            entry -> {
              int a = entry.a;
              if (a >= 0 && LOW(a) == INVALID_BDD) {
                entry.a = -1;
              }
            });
  }

  private void BddCache_clean_a(BddCache cache) {
    if (cache == null) {
      return;
    }
    Arrays.stream(cache.table)
        .parallel()
        .forEach(
            entry -> {
              int a = entry.a;
              if (a < 0) {
                return;
              }
              if (LOW(a) == INVALID_BDD || LOW(((BddCacheDataI) entry).res) == INVALID_BDD) {
                entry.a = -1;
              }
            });
  }

  private void BddCache_clean_ab(BddCache cache) {
    if (cache == null) {
      return;
    }
    Arrays.stream(cache.table)
        .parallel()
        .forEach(
            entry -> {
              int a = entry.a;
              if (a < 0) {
                return;
              }
              if (LOW(a) == INVALID_BDD
                  || (entry.b != 0 && LOW(entry.b) == INVALID_BDD)
                  || LOW(((BddCacheDataI) entry).res) == INVALID_BDD) {
                entry.a = -1;
              }
            });
  }

  private boolean invalidEntry(MultiOpBddCacheData entry) {
    if (entry.a == -1) {
      // unused entry
      return false;
    }
    if (LOW(entry.b) == INVALID_BDD) {
      // invalid result
      return true;
    }
    for (int i = 0; i < entry.operands.length; i++) {
      if (LOW(entry.operands[i]) == INVALID_BDD) {
        // invalid operand
        return true;
      }
    }
    // all valid
    return false;
  }

  private void BddCache_clean_multiop(BddCache cache) {
    if (cache == null) {
      return;
    }
    Arrays.stream(cache.table)
        .parallel()
        .forEach(
            e -> {
              MultiOpBddCacheData entry = (MultiOpBddCacheData) e;
              if (invalidEntry(entry)) {
                entry.a = -1;
                entry.operands = null;
              }
            });
  }

  private void bdd_setpair(bddPair pair, int oldvar, int newvar) {
    if (pair == null) {
      return;
    }

    if (oldvar < 0 || oldvar > bddvarnum - 1) {
      bdd_error(BDD_VAR);
    }
    if (newvar < 0 || newvar > bddvarnum - 1) {
      bdd_error(BDD_VAR);
    }

    bdd_delref(pair.result[bddvar2level[oldvar]]);
    pair.result[bddvar2level[oldvar]] = bdd_ithvar(newvar);
    pair.id = update_pairsid();

    if (bddvar2level[oldvar] > pair.last) {
      pair.last = bddvar2level[oldvar];
    }
  }

  private void bdd_setbddpair(bddPair pair, int oldvar, int newvar) {
    int oldlevel;

    if (pair == null) {
      return;
    }

    CHECK(newvar);
    if (oldvar < 0 || oldvar >= bddvarnum) {
      bdd_error(BDD_VAR);
    }
    oldlevel = bddvar2level[oldvar];

    bdd_delref(pair.result[oldlevel]);
    pair.result[oldlevel] = bdd_addref(newvar);
    pair.id = update_pairsid();

    if (oldlevel > pair.last) {
      pair.last = oldlevel;
    }
  }

  private void bdd_resetpair(bddPair p) {
    for (int n = 0; n < bddvarnum; n++) {
      p.result[n] = bdd_ithvar(bddlevel2var[n]);
    }
    p.last = 0;
  }

  class bddPair extends BDDPairing implements Serializable {
    int[] result;
    int last;
    int id;
    bddPair next;

    @Override
    public void set(int oldvar, int newvar) {
      bdd_setpair(this, oldvar, newvar);
    }

    @Override
    public void set(int oldvar, BDD newvar) {
      bdd_setbddpair(this, oldvar, ((BDDImpl) newvar)._index);
    }

    @Override
    public void reset() {
      bdd_resetpair(this);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append('{');
      boolean any = false;
      for (int i = 0; i < result.length; ++i) {
        if (result[i] != bdd_ithvar(bddlevel2var[i])) {
          if (any) {
            sb.append(", ");
          }
          any = true;
          sb.append(bddlevel2var[i]);
          sb.append('=');
          BDDImpl b = new BDDImpl(result[i]);
          sb.append(b);
          b.free();
        }
      }
      sb.append('}');
      return sb.toString();
    }

    /**
     * Test whether this pairing is compatible with {@link BDD#transform(BDD, BDDPairing)}: the
     * pairing must map each variable to itself (i.e. noop) or the variable at the previous level.
     */
    private boolean isValidForTransform() {
      for (int i = 0; i < result.length; i++) {
        int level = LEVEL(result[i]);
        if (!(level == i || level == i - 1)) {
          return false;
        }
      }
      return true;
    }
  }

  private bddPair pairs; /* List of all replacement pairs in use */
  private int pairsid; /* Pair identifier */
  private Set<Integer> _validPairIdsForTransform; /* Set of pairs that can be used with transform */

  /**
   * ***********************************************************************
   * ***********************************************************************
   */
  private void bdd_pairs_init() {
    pairsid = 0;
    pairs = null;
    _validPairIdsForTransform = new HashSet<>();
  }

  private int update_pairsid() {
    pairsid++;

    int numIds = INT_MAX >> 3;

    if (pairsid == numIds) {
      // we use more pair IDs than there are pair objects -- each time we mutate a pair we give it a
      // new ID. so if we run out, search for an unused one. have to clear the cache too
      pairsid = 0;
      for (bddPair p = pairs; p != null; p = p.next) {
        p.id = pairsid++;
      }
      // bdd_operator_reset();
      FlatCacheI_reset(replacecache);
      _validPairIdsForTransform.clear();
    }

    if (pairsid >= numIds) {
      throw new IllegalStateException("too many pairings");
    }

    return pairsid;
  }

  private void bdd_register_pair(bddPair p) {
    p.next = pairs;
    pairs = p;
  }

  private void bdd_pairs_vardown(int level) {
    for (bddPair p = pairs; p != null; p = p.next) {
      int tmp;

      tmp = p.result[level];
      p.result[level] = p.result[level + 1];
      p.result[level + 1] = tmp;

      if (p.last == level) {
        p.last++;
      }
    }
  }

  private int bdd_pairs_resize(int oldsize, int newsize) {
    for (bddPair p = pairs; p != null; p = p.next) {
      p.result = Arrays.copyOf(p.result, newsize);

      for (int n = oldsize; n < newsize; n++) {
        p.result[n] = bdd_ithvar(bddlevel2var[n]);
      }
    }

    return 0;
  }

  @Override
  public boolean isInitialized() {
    return bddrunning;
  }

  @Override
  public double setMinFreeNodes(double x) {
    return bdd_setminfreenodes((int) (x * 100.)) / 100.;
  }

  private int bdd_setminfreenodes(int mf) {
    int old = minfreenodes;

    if (mf < 0 || mf > 100) {
      return bdd_error(BDD_RANGE);
    }

    minfreenodes = mf;
    return old;
  }

  private double increasefactor;

  @Override
  public double setIncreaseFactor(double x) {
    if (x < 0) {
      return bdd_error(BDD_RANGE);
    }
    double old = increasefactor;
    increasefactor = x;
    return old;
  }

  @Override
  public int setCacheRatio(int r) {
    return bdd_setcacheratio(r);
  }

  private int bdd_setcacheratio(int r) {
    int old = cacheratio;

    if (r <= 0) {
      return bdd_error(BDD_RANGE);
    }
    if (bddnodesize == 0) {
      return old;
    }

    cacheratio = r;
    bdd_operator_noderesize();
    return old;
  }

  @Override
  public int varNum() {
    return bdd_varnum();
  }

  @Override
  public int setVarNum(int num) {
    return bdd_setvarnum(num);
  }

  @Override
  public int duplicateVar(int var) {
    if (var < 0 || var >= bddvarnum) {
      bdd_error(BDD_VAR);
      return BDDZERO;
    }

    int newVar = bddvarnum;
    int lev = bddvar2level[var];
    // Increase the size of the various data structures.
    bdd_setvarnum(bddvarnum + 1);
    // Actually duplicate the var in all BDDs.
    insert_level(lev);
    dup_level(lev, 0);
    // Fix up bddvar2level
    for (int i = 0; i < bddvarnum; ++i) {
      if (bddvar2level[i] > lev && bddvar2level[i] < bddvarnum) {
        ++bddvar2level[i];
      }
    }
    bddvar2level[newVar] = lev + 1;
    // Fix up bddlevel2var
    for (int i = bddvarnum - 2; i > lev; --i) {
      bddlevel2var[i + 1] = bddlevel2var[i];
    }
    bddlevel2var[lev + 1] = newVar;
    // Fix up bddvarset
    for (int bdv = 0; bdv < bddvarnum; bdv++) {
      bddvarset[bdv * 2] = PUSHREF(bdd_makenode(bddvar2level[bdv], BDDZERO, BDDONE));
      bddvarset[bdv * 2 + 1] = bdd_makenode(bddvar2level[bdv], BDDONE, BDDZERO);
      POPREF(1);

      SETMAXREF(bddvarset[bdv * 2]);
      SETMAXREF(bddvarset[bdv * 2 + 1]);
    }
    // Fix up pairs
    for (bddPair pair = pairs; pair != null; pair = pair.next) {
      bdd_delref(pair.result[bddvarnum - 1]);
      for (int i = bddvarnum - 1; i > lev + 1; --i) {
        pair.result[i] = pair.result[i - 1];
        if (i != LEVEL(pair.result[i]) && i > pair.last) {
          pair.last = i;
        }
      }
      pair.result[lev + 1] = bdd_ithvar(newVar);
    }

    return newVar;
  }

  private int bdd_setvarnum(int num) {
    int bdv;
    int oldbddvarnum = bddvarnum;

    if (num < 1 || num > MAXVAR) {
      bdd_error(BDD_RANGE);
      return BDDZERO;
    }

    if (num < bddvarnum) {
      return bdd_error(BDD_DECVNUM);
    }
    if (num == bddvarnum) {
      return 0;
    }

    if (bddvarset == null) {
      bddvarset = new int[num * 2];
      bddlevel2var = new int[num + 1];
      bddvar2level = new int[num + 1];
    } else {
      bddvarset = Arrays.copyOf(bddvarset, num * 2);
      bddlevel2var = Arrays.copyOf(bddlevel2var, num + 1);
      bddvar2level = Arrays.copyOf(bddvar2level, num + 1);
    }

    for (bdv = bddvarnum; bddvarnum < num; bddvarnum++) {
      bddvarset[bddvarnum * 2] = PUSHREF(bdd_makenode(bddvarnum, BDDZERO, BDDONE));
      bddvarset[bddvarnum * 2 + 1] = bdd_makenode(bddvarnum, BDDONE, BDDZERO);
      POPREF(1);

      if (bdderrorcond != 0) {
        bddvarnum = bdv;
        return -bdderrorcond;
      }

      SETMAXREF(bddvarset[bddvarnum * 2]);
      SETMAXREF(bddvarset[bddvarnum * 2 + 1]);
      bddlevel2var[bddvarnum] = bddvarnum;
      bddvar2level[bddvarnum] = bddvarnum;
    }

    SETLEVELANDMARK(0, num);
    SETLEVELANDMARK(1, num);
    bddvar2level[num] = num;
    bddlevel2var[num] = num;

    bdd_pairs_resize(oldbddvarnum, bddvarnum);
    bdd_operator_varresize();

    assert bddvarnum == LEVEL(BDDZERO);
    assert bddvarnum == LEVEL(BDDONE);

    return 0;
  }

  @Override
  public BDD ithVar(int var) {
    return makeBDD(bdd_ithvar(var));
  }

  @Override
  public BDD nithVar(int var) {
    return makeBDD(bdd_nithvar(var));
  }

  @Override
  public void printAll() {
    bdd_fprintall(System.out);
  }

  @Override
  public void printTable(BDD b) {
    int x = ((BDDImpl) b)._index;
    bdd_fprinttable(System.out, x);
  }

  @Override
  public int level2Var(int level) {
    return bddlevel2var[level];
  }

  @Override
  public int var2Level(int var) {
    return bddvar2level[var];
  }

  @Override
  public void setVarOrder(int[] neworder) {
    bdd_setvarorder(neworder);
  }

  private transient int[] extroots;
  private transient int extrootsize;

  private transient levelData[] levels; /* Indexed by variable! */

  static class levelData {
    int start; /* Start of this sub-table (entry in "bddnodes") */
    int size; /* Size of this sub-table */
    int maxsize; /* Max. allowed size of sub-table */
    int nodenum; /* Number of nodes in this level */
  }

  static class imatrix {
    byte[][] rows;
    int size;
  }

  /* Interaction matrix */
  private transient imatrix iactmtx;

  private int bdd_getnodenum() {
    return bddnodesize - bddfreenum;
  }

  private void bdd_setvarorder(int[] neworder) {
    reorder_init();

    for (int level = 0; level < bddvarnum; level++) {
      int lowvar = neworder[level];

      while (bddvar2level[lowvar] > level) {
        reorder_varup(lowvar);
      }
    }

    reorder_done();
  }

  private int reorder_varup(int var) {
    if (var < 0 || var >= bddvarnum) {
      return bdd_error(BDD_VAR);
    }
    if (bddvar2level[var] == 0) {
      return 0;
    }
    return reorder_vardown(bddlevel2var[bddvar2level[var] - 1]);
  }

  private int reorder_vardown(int var) {
    int n, level;

    if (var < 0 || var >= bddvarnum) {
      return bdd_error(BDD_VAR);
    }
    if ((level = bddvar2level[var]) >= bddvarnum - 1) {
      return 0;
    }

    resizedInMakenode = false;

    if (imatrixDepends(iactmtx, var, bddlevel2var[level + 1])) {
      int toBeProcessed = reorder_downSimple(var);
      levelData l = levels[var];

      if (l.nodenum < l.size / 3 || l.nodenum >= (l.size * 3) / 2 && l.size < l.maxsize) {
        reorder_swapResize(toBeProcessed, var);
        reorder_localGbcResize(toBeProcessed, var);
      } else {
        reorder_swap(toBeProcessed, var);
        reorder_localGbc(var);
      }
    }

    /* Swap the var<->level tables */
    n = bddlevel2var[level];
    bddlevel2var[level] = bddlevel2var[level + 1];
    bddlevel2var[level + 1] = n;

    n = bddvar2level[var];
    bddvar2level[var] = bddvar2level[bddlevel2var[level]];
    bddvar2level[bddlevel2var[level]] = n;

    /* Update all rename pairs */
    bdd_pairs_vardown(level);

    if (resizedInMakenode) {
      reorder_rehashAll();
    }

    return 0;
  }

  private static boolean imatrixDepends(imatrix mtx, int a, int b) {
    return (mtx.rows[a][b / 8] & (1 << (b % 8))) != 0;
  }

  private void reorder_setLevellookup() {
    for (int n = 0; n < bddvarnum; n++) {
      levels[n].maxsize = bddnodesize / bddvarnum;
      levels[n].start = n * levels[n].maxsize;
      levels[n].size = Math.min(levels[n].maxsize, (levels[n].nodenum * 5) / 4);

      if (levels[n].size >= 4) {
        levels[n].size = bdd_prime_lte(levels[n].size);
      }
    }
  }

  private void reorder_rehashAll() {
    reorder_setLevellookup();
    bddfreepos = 0;

    Arrays.fill(bddhash, 0);

    for (int n = bddnodesize - 1; n >= 2; n--) {
      if (HASREF(n)) {
        int hash2 = NODEHASH2(VARr(n), LOW(n), HIGH(n));
        SETNEXT(n, HASH(hash2));
        SETHASH(hash2, n);
      } else {
        SETNEXT(n, bddfreepos);
        bddfreepos = n;
      }
    }
  }

  private void reorder_localGbc(int var0) {
    int var1 = bddlevel2var[bddvar2level[var0] + 1];
    int vl1 = levels[var1].start;
    int size1 = levels[var1].size;

    for (int n = 0; n < size1; n++) {
      int hash = n + vl1;
      int r = HASH(hash);
      SETHASH(hash, 0);

      while (r != 0) {
        int next = NEXT(r);

        if (HASREF(r)) {
          SETNEXT(r, HASH(hash));
          SETHASH(hash, r);
        } else {
          DECREF(LOW(r));
          DECREF(HIGH(r));

          SETLOW(r, INVALID_BDD);
          SETNEXT(r, bddfreepos);
          bddfreepos = r;
          levels[var1].nodenum--;
          bddfreenum++;
        }

        r = next;
      }
    }
  }

  private int reorder_downSimple(int var0) {
    int toBeProcessed = 0;
    int var1 = bddlevel2var[bddvar2level[var0] + 1];
    int vl0 = levels[var0].start;
    int size0 = levels[var0].size;

    levels[var0].nodenum = 0;

    for (int n = 0; n < size0; n++) {
      int r;

      r = HASH(n + vl0);
      SETHASH(n + vl0, 0);

      while (r != 0) {
        int next = NEXT(r);

        if (VARr(LOW(r)) != var1 && VARr(HIGH(r)) != var1) {
          /* Node does not depend on next var, let it stay in the chain */
          SETNEXT(r, HASH(n + vl0));
          SETHASH(n + vl0, r);
          levels[var0].nodenum++;
        } else {
          /* Node depends on next var - save it for later procesing */
          SETNEXT(r, toBeProcessed);
          toBeProcessed = r;
          if (CACHESTATS) {
            cachestats.swapCount++;
          }
        }

        r = next;
      }
    }

    return toBeProcessed;
  }

  private void reorder_swapResize(int toBeProcessed, int var0) {
    int var1 = bddlevel2var[bddvar2level[var0] + 1];

    while (toBeProcessed != 0) {
      int next = NEXT(toBeProcessed);
      int f0 = LOW(toBeProcessed);
      int f1 = HIGH(toBeProcessed);
      int f00, f01, f10, f11;

      /* Find the cofactors for the new nodes */
      if (VARr(f0) == var1) {
        f00 = LOW(f0);
        f01 = HIGH(f0);
      } else {
        f00 = f01 = f0;
      }

      if (VARr(f1) == var1) {
        f10 = LOW(f1);
        f11 = HIGH(f1);
      } else {
        f10 = f11 = f1;
      }

      /* Note: makenode does refcou. */
      f0 = reorder_makenode(var0, f00, f10);
      f1 = reorder_makenode(var0, f01, f11);
      // node = bddnodes[toBeProcessed]; /* Might change in makenode */

      /* We know that the refcou of the grandchilds of this node
       * is greater than one (these are f00...f11), so there is
       * no need to do a recursive refcou decrease. It is also
       * possible for the node.low/high nodes to come alive again,
       * so deref. of the childs is delayed until the local GBC. */

      DECREF(LOW(toBeProcessed));
      DECREF(HIGH(toBeProcessed));

      /* Update in-place */
      SETVARr(toBeProcessed, var1);
      SETLOW(toBeProcessed, f0);
      SETHIGH(toBeProcessed, f1);

      levels[var1].nodenum++;

      /* Do not rehash yet since we are going to resize the hash table */

      toBeProcessed = next;
    }
  }

  private static int MIN(int a, int b) {
    return Math.min(a, b);
  }

  private void reorder_localGbcResize(int toBeProcessed, int var0) {
    int var1 = bddlevel2var[bddvar2level[var0] + 1];
    int vl1 = levels[var1].start;
    int size1 = levels[var1].size;

    for (int n = 0; n < size1; n++) {
      int hash = n + vl1;
      int r = HASH(hash);
      SETHASH(hash, 0);

      while (r != 0) {
        int next = NEXT(r);

        if (HASREF(r)) {
          SETNEXT(r, toBeProcessed);
          toBeProcessed = r;
        } else {
          DECREF(LOW(r));
          DECREF(HIGH(r));

          SETLOW(r, INVALID_BDD);
          SETNEXT(r, bddfreepos);
          bddfreepos = r;
          levels[var1].nodenum--;
          bddfreenum++;
        }

        r = next;
      }
    }

    /* Resize */
    if (levels[var1].nodenum < levels[var1].size) {
      levels[var1].size = MIN(levels[var1].maxsize, levels[var1].size / 2);
    } else {
      levels[var1].size = MIN(levels[var1].maxsize, levels[var1].size * 2);
    }

    if (levels[var1].size >= 4) {
      levels[var1].size = bdd_prime_lte(levels[var1].size);
    }

    /* Rehash the remaining live nodes */
    while (toBeProcessed != 0) {
      int next = NEXT(toBeProcessed);
      int hash = NODEHASH2(VARr(toBeProcessed), LOW(toBeProcessed), HIGH(toBeProcessed));

      SETNEXT(toBeProcessed, HASH(hash));
      SETHASH(hash, toBeProcessed);

      toBeProcessed = next;
    }
  }

  private void reorder_swap(int toBeProcessed, int var0) {
    int var1 = bddlevel2var[bddvar2level[var0] + 1];

    while (toBeProcessed != 0) {
      int next = NEXT(toBeProcessed);
      int f0 = LOW(toBeProcessed);
      int f1 = HIGH(toBeProcessed);
      int f00, f01, f10, f11, hash;

      /* Find the cofactors for the new nodes */
      if (VARr(f0) == var1) {
        f00 = LOW(f0);
        f01 = HIGH(f0);
      } else {
        f00 = f01 = f0;
      }

      if (VARr(f1) == var1) {
        f10 = LOW(f1);
        f11 = HIGH(f1);
      } else {
        f10 = f11 = f1;
      }

      /* Note: makenode does refcou. */
      f0 = reorder_makenode(var0, f00, f10);
      f1 = reorder_makenode(var0, f01, f11);
      // node = bddnodes[toBeProcessed]; /* Might change in makenode */

      /* We know that the refcou of the grandchilds of this node
       * is greater than one (these are f00...f11), so there is
       * no need to do a recursive refcou decrease. It is also
       * possible for the node.low/high nodes to come alive again,
       * so deref. of the childs is delayed until the local GBC. */

      DECREF(LOW(toBeProcessed));
      DECREF(HIGH(toBeProcessed));

      /* Update in-place */
      SETVARr(toBeProcessed, var1);
      SETLOW(toBeProcessed, f0);
      SETHIGH(toBeProcessed, f1);

      levels[var1].nodenum++;

      /* Rehash the node since it got new childs */
      hash = NODEHASH2(VARr(toBeProcessed), LOW(toBeProcessed), HIGH(toBeProcessed));
      SETNEXT(toBeProcessed, HASH(hash));
      SETHASH(hash, toBeProcessed);

      toBeProcessed = next;
    }
  }

  private int NODEHASH2(int var, int l, int h) {
    return Math.floorMod(PAIR(l, h), levels[var].size) + levels[var].start;
  }

  private boolean resizedInMakenode;

  private int reorder_makenode(int var, int low, int high) {
    /* check whether childs are equal */
    if (low == high) {
      /* Note: We know that low,high has a refcou greater than zero, so
      there is no need to add reference *recursively* */
      INCREF(low);
      if (CACHESTATS) {
        cachestats.uniqueTrivial++;
      }
      return low;
    }

    if (CACHESTATS) {
      cachestats.uniqueAccess++;
    }

    /* Try to find an existing node of this kind */
    int hash = NODEHASH2(var, low, high);
    int res = HASH(hash);

    while (res != 0) {
      if (LOW(res) == low && HIGH(res) == high) {
        if (CACHESTATS) {
          cachestats.uniqueHit++;
        }
        INCREF(res);
        return res;
      }
      res = NEXT(res);

      if (CACHESTATS) {
        cachestats.uniqueChain++;
      }
    }

    /* No existing node -> build one */
    if (CACHESTATS) {
      cachestats.uniqueMiss++;
    }

    /* Any free nodes to use ? */
    if (bddfreepos == 0) {
      if (bdderrorcond != 0) {
        return 0;
      }

      /* Try to allocate more nodes - call noderesize without
       * enabling rehashing.
       * Note: if ever rehashing is allowed here, then remember to
       * update local variable "hash" */
      bdd_noderesize(false);
      resizedInMakenode = true;

      /* Panic if that is not possible */
      if (bddfreepos == 0) {
        bdd_error(BDD_NODENUM);
        bdderrorcond = Math.abs(BDD_NODENUM);
        return 0;
      }
    }

    /* Build new node */
    res = bddfreepos;
    bddfreepos = NEXT(bddfreepos);
    levels[var].nodenum++;
    bddproduced++;
    bddfreenum--;

    SETVARr(res, var);
    SETLOW(res, low);
    SETHIGH(res, high);

    /* Insert node in hash chain */
    SETNEXT(res, HASH(hash));
    SETHASH(hash, res);

    /* Make sure it is reference counted */
    CLEARREF(res);
    INCREF(res);
    INCREF(LOW(res));
    INCREF(HIGH(res));

    return res;
  }

  private int reorder_init() {
    reorder_handler(true, reorderstats);

    levels = new levelData[bddvarnum];

    for (int n = 0; n < bddvarnum; n++) {
      levels[n] = new levelData();
      levels[n].start = -1;
      levels[n].size = 0;
      levels[n].nodenum = 0;
    }

    /* First mark and recursive refcou. all roots and childs. Also do some
     * setup here for both setLevellookup and reorder_gbc */
    if (mark_roots() < 0) {
      return -1;
    }

    /* Initialize the hash tables */
    reorder_setLevellookup();

    /* Garbage collect and rehash to new scheme */
    reorder_gbc();

    return 0;
  }

  private void insert_level(int levToInsert) {
    for (int n = 2; n < bddnodesize; n++) {
      if (LOW(n) == INVALID_BDD) {
        continue;
      }
      int lev = LEVEL(n);
      if (lev <= levToInsert || lev == bddvarnum - 1) {
        // Stays the same.
        continue;
      }
      int lo, hi, newLev;
      lo = LOW(n);
      hi = HIGH(n);
      // Need to increase level by one.
      newLev = lev + 1;

      // Find this node in its hash chain.
      int hash = NODEHASH(lev, lo, hi);
      int r = HASH(hash), r2 = 0;
      while (r != n && r != 0) {
        r2 = r;
        r = NEXT(r);
      }
      if (r == 0) {
        // Cannot find node in the hash chain ?!
        throw new InternalError();
      }
      // Remove from this hash chain.
      int NEXT_r = NEXT(r);
      if (r2 == 0) {
        SETHASH(hash, NEXT_r);
      } else {
        SETNEXT(r2, NEXT_r);
      }
      // Set level of this node.
      SETLEVEL(n, newLev);
      lo = LOW(n);
      hi = HIGH(n);
      // Add to new hash chain.
      hash = NODEHASH(newLev, lo, hi);
      r = HASH(hash);
      SETHASH(hash, n);
      SETNEXT(n, r);
    }
  }

  private void dup_level(int levToInsert, int val) {
    for (int n = 2; n < bddnodesize; n++) {
      if (LOW(n) == INVALID_BDD) {
        continue;
      }
      int lev = LEVEL(n);
      if (lev != levToInsert || lev == bddvarnum - 1) {
        // Stays the same.
        continue;
      }
      int lo, hi, newLev;
      lo = LOW(n);
      hi = HIGH(n);
      // Duplicate this node.
      _assert(LEVEL(lo) > levToInsert + 1);
      _assert(LEVEL(hi) > levToInsert + 1);
      int n_low, n_high;
      bdd_addref(n);
      // 0 = var is zero, 1 = var is one, -1 = var equals other
      n_low = bdd_makenode(levToInsert + 1, val <= 0 ? lo : 0, val <= 0 ? 0 : lo);
      n_high = bdd_makenode(levToInsert + 1, val == 0 ? hi : 0, val == 0 ? 0 : hi);
      bdd_delref(n);
      newLev = lev;
      SETLOW(n, n_low);
      SETHIGH(n, n_high);

      // Find this node in its hash chain.
      int hash = NODEHASH(lev, lo, hi);
      int r = HASH(hash), r2 = 0;
      while (r != n && r != 0) {
        r2 = r;
        r = NEXT(r);
      }
      if (r == 0) {
        // Cannot find node in the hash chain ?!
        throw new InternalError();
      }
      // Remove from this hash chain.
      int NEXT_r = NEXT(r);
      if (r2 == 0) {
        SETHASH(hash, NEXT_r);
      } else {
        SETNEXT(r2, NEXT_r);
      }
      // Set level of this node.
      SETLEVEL(n, newLev);
      lo = LOW(n);
      hi = HIGH(n);
      // Add to new hash chain.
      hash = NODEHASH(newLev, lo, hi);
      r = HASH(hash);
      SETHASH(hash, n);
      SETNEXT(n, r);
    }
  }

  private int mark_roots() {
    boolean[] dep = new boolean[bddvarnum];

    extrootsize = 0;
    for (int n = 2; n < bddnodesize; n++) {
      /* This is where we go from .level to .var!
       * - Do NOT use the LEVEL macro here. */
      SETLEVELANDMARK(n, bddlevel2var[LEVELANDMARK(n)]);

      if (HASREF(n)) {
        SETMARK(n);
        extrootsize++;
      }
    }

    extroots = new int[extrootsize];

    iactmtx = imatrixNew(bddvarnum);

    extrootsize = 0;
    for (int n = 2; n < bddnodesize; n++) {

      if (MARKED(n)) {
        UNMARK(n);
        extroots[extrootsize++] = n;

        for (int i = 0; i < bddvarnum; ++i) {
          dep[i] = false;
        }
        dep[VARr(n)] = true;
        levels[VARr(n)].nodenum++;

        addref_rec(LOW(n), dep);
        addref_rec(HIGH(n), dep);

        addDependencies(dep);
      }

      /* Make sure the hash field is empty. This saves a loop in the
      initial GBC */
      SETHASH(n, 0);
    }

    SETHASH(0, 0);
    SETHASH(1, 0);

    return 0;
  }

  private static imatrix imatrixNew(int size) {
    imatrix mtx = new imatrix();

    mtx.rows = new byte[size][];

    for (int n = 0; n < size; n++) {
      mtx.rows[n] = new byte[size / 8 + 1];
    }

    mtx.size = size;

    return mtx;
  }

  private void addref_rec(int r, boolean[] dep) {
    if (r < 2) {
      return;
    }

    if (!HASREF(r) || MARKED(r)) {
      bddfreenum--;

      /* Detect variable dependencies for the interaction matrix */
      dep[VARr(r) & ~MARK_MASK] = true;

      /* Make sure the nodenum field is updated. Used in the initial GBC */
      levels[VARr(r) & ~MARK_MASK].nodenum++;

      addref_rec(LOW(r), dep);
      addref_rec(HIGH(r), dep);
    } else {

      /* Update (from previously found) variable dependencies
       * for the interaction matrix */
      for (int n = 0; n < bddvarnum; n++) {
        dep[n] |= imatrixDepends(iactmtx, VARr(r) & ~MARK_MASK, n);
      }
    }

    INCREF(r);
  }

  private void addDependencies(boolean[] dep) {
    for (int n = 0; n < bddvarnum; n++) {
      for (int m = n; m < bddvarnum; m++) {
        if (dep[n] && dep[m]) {
          imatrixSet(iactmtx, n, m);
          imatrixSet(iactmtx, m, n);
        }
      }
    }
  }

  private static void imatrixSet(imatrix mtx, int a, int b) {
    mtx.rows[a][b / 8] |= 1 << (b % 8);
  }

  private void reorder_gbc() {
    bddfreepos = 0;
    bddfreenum = 0;

    /* No need to zero all hash fields - this is done in mark_roots */

    for (int n = bddnodesize - 1; n >= 2; n--) {

      if (HASREF(n)) {
        int hash;

        hash = NODEHASH2(VARr(n), LOW(n), HIGH(n));
        SETNEXT(n, HASH(hash));
        SETHASH(hash, n);

      } else {
        SETLOW(n, INVALID_BDD);
        SETNEXT(n, bddfreepos);
        bddfreepos = n;
        bddfreenum++;
      }
    }
  }

  private void reorder_done() {
    for (int n = 0; n < extrootsize; n++) {
      SETMARK(extroots[n]);
    }
    for (int n = 2; n < bddnodesize; n++) {
      if (MARKED(n)) {
        UNMARK(n);
      } else {
        CLEARREF(n);
      }

      /* This is where we go from .var to .level again!
       * - Do NOT use the LEVEL macro here. */
      SETLEVELANDMARK(n, bddvar2level[LEVELANDMARK(n)]);
    }

    imatrixDelete(iactmtx);
    bdd_gbc();

    reorder_handler(false, reorderstats);
  }

  private static void imatrixDelete(imatrix mtx) {
    for (int n = 0; n < mtx.size; n++) {
      mtx.rows[n] = null;
    }
    mtx.rows = null;
  }

  @Override
  public int nodeCount(Collection<BDD> r) {
    int[] a = new int[r.size()];
    int j = 0;
    for (Object o : r) {
      BDDImpl b = (BDDImpl) o;
      a[j++] = b._index;
    }
    return bdd_anodecount(a);
  }

  @Override
  public int getNodeTableSize() {
    return bdd_getallocnum();
  }

  private int bdd_getallocnum() {
    return bddnodesize;
  }

  @Override
  public int getNodeNum() {
    return bdd_getnodenum();
  }

  @Override
  public int getCacheSize() {
    return cachesize;
  }

  @Override
  public void printStat() {
    bdd_fprintstat(System.out);
  }

  @Override
  public BDDPairing makePair() {
    bddPair p = new bddPair();
    p.result = new int[bddvarnum];
    for (int n = 0; n < bddvarnum; n++) {
      p.result[n] = bdd_ithvar(bddlevel2var[n]);
    }

    p.id = update_pairsid();
    p.last = -1;

    bdd_register_pair(p);
    return p;
  }

  private void bdd_fprintall(PrintStream out) {
    for (int n = 0; n < bddnodesize; n++) {
      if (LOW(n) != INVALID_BDD) {
        out.print("[" + right(n, 5) + " - " + right(GETREF(n), 2) + "] ");
        // TODO: labelling of vars
        out.print(right(bddlevel2var[LEVEL(n)], 3));

        out.print(": " + right(LOW(n), 3));
        out.println(" " + right(HIGH(n), 3));
      }
    }
  }

  private void bdd_fprinttable(PrintStream out, int r) {
    out.println("ROOT: " + r);
    if (r < 2) {
      return;
    }

    bdd_mark(r);

    for (int n = 0; n < bddnodesize; n++) {
      if (MARKED(n)) {
        UNMARK(n);

        out.print("[" + right(n, 5) + "] ");
        // TODO: labelling of vars
        out.print(right(bddlevel2var[LEVEL(n)], 3));

        out.print(": " + right(LOW(n), 3));
        out.println(" " + right(HIGH(n), 3));
      }
    }
  }

  private static String right(int x, int w) {
    return right(Integer.toString(x), w);
  }

  private static String right(String s, int w) {
    int n = s.length();
    // if (w < n) return s.substring(n - w);
    StringBuilder b = new StringBuilder(w);
    for (int i = n; i < w; ++i) {
      b.append(' ');
    }
    b.append(s);
    return b.toString();
  }

  private void bdd_fprintstat(PrintStream out) {
    CacheStats s = cachestats;
    out.print(s.toString());
  }

  @Override
  protected BDDDomain createDomain(int a, BigInteger b) {
    return new bddDomain(a, b);
  }

  private class bddDomain extends BDDDomain {

    bddDomain(int a, BigInteger b) {
      super(a, b);
    }

    @Override
    public BDDFactory getFactory() {
      return JFactory.this;
    }
  }

  @Override
  protected BDDBitVector createBitVector(int a) {
    return new bvec(a);
  }

  private class bvec extends BDDBitVector {

    bvec(int bitnum) {
      super(bitnum);
    }

    @Override
    public BDDFactory getFactory() {
      return JFactory.this;
    }
  }

  //// Prime stuff below.

  private final Random rng = new Random();

  private int Random(int i) {
    return rng.nextInt(i) + 1;
  }

  private static boolean isEven(int src) {
    return (src & 0x1) == 0;
  }

  private static boolean hasFactor(int src, int n) {
    return (src != n) && (src % n == 0);
  }

  private static boolean BitIsSet(int src, int b) {
    return (src & (1 << b)) != 0;
  }

  private static final int CHECKTIMES = 20;

  private static int u64_mulmod(int a, int b, int c) {
    return (int) (((long) a * (long) b) % (long) c);
  }

  /**
   * *********************************************************************** Miller Rabin check
   * ***********************************************************************
   */
  private static int numberOfBits(int src) {
    if (src == 0) {
      return 0;
    }

    for (int b = 31; b > 0; --b) {
      if (BitIsSet(src, b)) {
        return b + 1;
      }
    }

    return 1;
  }

  private static boolean isWitness(int witness, int src) {
    int bitNum = numberOfBits(src - 1) - 1;
    int d = 1;

    for (int i = bitNum; i >= 0; --i) {
      int x = d;

      d = u64_mulmod(d, d, src);

      if (d == 1 && x != 1 && x != src - 1) {
        return true;
      }

      if (BitIsSet(src - 1, i)) {
        d = u64_mulmod(d, witness, src);
      }
    }

    return d != 1;
  }

  private boolean isMillerRabinPrime(int src) {
    for (int n = 0; n < CHECKTIMES; ++n) {
      int witness = Random(src - 1);

      if (isWitness(witness, src)) {
        return false;
      }
    }

    return true;
  }

  /**
   * *********************************************************************** Basic prime searching
   * stuff ***********************************************************************
   */
  private static boolean hasEasyFactors(int src) {
    return hasFactor(src, 3)
        || hasFactor(src, 5)
        || hasFactor(src, 7)
        || hasFactor(src, 11)
        || hasFactor(src, 13);
  }

  private boolean isPrime(int src) {
    if (hasEasyFactors(src)) {
      return false;
    }

    return isMillerRabinPrime(src);
  }

  /**
   * *********************************************************************** External interface
   * ***********************************************************************
   */
  private int bdd_prime_gte(int src) {
    if (isEven(src)) {
      ++src;
    }

    while (!isPrime(src)) {
      src += 2;
    }

    return src;
  }

  private int bdd_prime_lte(int src) {
    if (isEven(src)) {
      --src;
    }

    while (!isPrime(src)) {
      src -= 2;
    }

    return src;
  }
}
