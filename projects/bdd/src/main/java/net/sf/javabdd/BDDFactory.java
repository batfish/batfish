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

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Interface for the creation and manipulation of BDDs.
 *
 * @see net.sf.javabdd.BDD
 * @author John Whaley
 * @version $Id: BDDFactory.java,v 1.18 2005/10/12 10:27:08 joewhaley Exp $
 */
public abstract class BDDFactory {
  private static final Logger LOGGER = LogManager.getLogger(BDDFactory.class);

  public static final String getProperty(String key, String def) {
    try {
      return System.getProperty(key, def);
    } catch (AccessControlException ignored) {
      return def;
    }
  }

  /**
   * Initializes a BDD factory with the given initial node table size and operation cache size.
   * Tries to use the "buddy" native library; if it fails, it falls back to the "java" library.
   *
   * @param nodenum initial node table size
   * @param cachesize operation cache size
   * @return BDD factory object
   */
  public static BDDFactory init(int nodenum, int cachesize) {
    String bddpackage = getProperty("bdd", "buddy");
    return init(bddpackage, nodenum, cachesize);
  }

  /**
   * Initializes a BDD factory of the given type with the given initial node table size and
   * operation cache size. The type is a string that can be "buddy", "cudd", "cal", "j", "java",
   * "jdd", "test", "typed", or a name of a class that has an init() method that returns a
   * BDDFactory. If it fails, it falls back to the "java" factory.
   *
   * @param bddpackage BDD package string identifier
   * @param nodenum initial node table size
   * @param cachesize operation cache size
   * @return BDD factory object
   */
  public static BDDFactory init(String bddpackage, int nodenum, int cachesize) {
    try {
      if (bddpackage.equals("j") || bddpackage.equals("java")) {
        return JFactory.init(nodenum, cachesize);
      }
    } catch (LinkageError e) {
      LOGGER.info("Could not load BDD package {}: {}", bddpackage, e.getLocalizedMessage());
    }
    try {
      Class<?> c = Class.forName(bddpackage);
      Method m = c.getMethod("init", int.class, int.class);
      return (BDDFactory) m.invoke(null, new Object[] {nodenum, cachesize});
    } catch (ClassNotFoundException
        | NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException ignored) {
      // Do nothing.
    }
    // falling back to default java implementation.
    return JFactory.init(nodenum, cachesize);
  }

  /** Logical 'and'. */
  public static final BDDOp and = new BDDOp(0, "and");

  /** Logical 'xor'. */
  public static final BDDOp xor = new BDDOp(1, "xor");

  /** Logical 'or'. */
  public static final BDDOp or = new BDDOp(2, "or");

  /** Logical 'nand'. */
  public static final BDDOp nand = new BDDOp(3, "nand");

  /** Logical 'nor'. */
  public static final BDDOp nor = new BDDOp(4, "nor");

  /** Logical 'implication'. */
  public static final BDDOp imp = new BDDOp(5, "imp");

  /** Logical 'bi-implication'. */
  public static final BDDOp biimp = new BDDOp(6, "biimp");

  /** Set difference. */
  public static final BDDOp diff = new BDDOp(7, "diff");

  /** Less than. */
  public static final BDDOp less = new BDDOp(8, "less");

  /** Inverse implication. */
  public static final BDDOp invimp = new BDDOp(9, "invimp");

  /**
   * Enumeration class for binary operations on BDDs. Use the static fields in BDDFactory to access
   * the different binary operations.
   */
  public static class BDDOp {
    final int id;
    final String name;

    private BDDOp(int id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Get the constant false BDD.
   *
   * <p>Compare to bdd_false.
   */
  public abstract BDD zero();

  /**
   * Get the constant true BDD.
   *
   * <p>Compare to bdd_true.
   */
  public abstract BDD one();

  /**
   * Build a cube from an array of variables.
   *
   * <p>Compare to bdd_buildcube.
   */
  public BDD buildCube(int value, List<BDD> variables) {
    BDD result = one();
    for (BDD var : variables) {
      if ((value & 0x1) != 0) {
        var = var.id();
      } else {
        var = var.not();
      }
      result.andWith(var);
      value >>= 1;
    }
    return result;
  }

  /**
   * Build a cube from an array of variables.
   *
   * <p>Compare to bdd_ibuildcube./p>
   */
  public BDD buildCube(int value, int[] variables) {
    BDD result = one();
    for (int z = 0; z < variables.length; z++, value >>= 1) {
      BDD v;
      if ((value & 0x1) != 0) {
        v = ithVar(variables[variables.length - z - 1]);
      } else {
        v = nithVar(variables[variables.length - z - 1]);
      }
      result.andWith(v);
    }
    return result;
  }

  /**
   * Builds a BDD variable set from an integer array. The integer array <tt>varset</tt> holds the
   * variable numbers. The BDD variable set is represented by a conjunction of all the variables in
   * their positive form.
   *
   * <p>Compare to bdd_makeset.
   */
  public BDD makeSet(int[] varset) {
    BDD res = one();
    int varnum = varset.length;
    for (int v = varnum - 1; v >= 0; v--) {
      res.andWith(ithVar(varset[v]));
    }
    return res;
  }

  /** ** STARTUP / SHUTDOWN *** */

  /**
   * Compare to bdd_init.
   *
   * @param nodenum the initial number of BDD nodes
   * @param cachesize the size of caches used by the BDD operators
   */
  protected abstract void initialize(int nodenum, int cachesize);

  /**
   * Returns true if this BDD factory is initialized, false otherwise.
   *
   * <p>Compare to bdd_isrunning.
   *
   * @return true if this BDD factory is initialized
   */
  public abstract boolean isInitialized();

  /**
   * Reset the BDD factory to its initial state. Everything is reallocated from scratch. This is
   * like calling done() followed by initialize().
   */
  public void reset() {
    int nodes = getNodeTableSize();
    int cache = getCacheSize();
    domain = null;
    fdvarnum = 0;
    firstbddvar = 0;
    done();
    initialize(nodes, cache);
  }

  /**
   * This function frees all memory used by the BDD package and resets the package to its
   * uninitialized state. The BDD package is no longer usable after this call.
   *
   * <p>Compare to bdd_done.
   */
  public abstract void done();

  /**
   * Sets the error condition. This will cause the BDD package to throw an exception at the next
   * garbage collection.
   *
   * @param code the error code to set
   */
  public abstract void setError(int code);

  /** Clears any outstanding error condition. */
  public abstract void clearError();

  /** ** CACHE/TABLE PARAMETERS *** */

  /**
   * Set the maximum available number of BDD nodes.
   *
   * <p>Compare to bdd_setmaxnodenum.
   *
   * @param size maximum number of nodes
   * @return old value
   */
  public abstract int setMaxNodeNum(int size);

  /**
   * Set minimum percentage of nodes to be reclaimed after a garbage collection. If this percentage
   * is not reclaimed, the node table will be grown. The range of x is 0..1. The default is .20.
   *
   * <p>Compare to bdd_setminfreenodes.
   *
   * @param x number from 0 to 1
   * @return old value
   */
  public abstract double setMinFreeNodes(double x);

  /**
   * Set maximum number of nodes by which to increase node table after a garbage collection.
   *
   * <p>Compare to bdd_setmaxincrease.
   *
   * @param x maximum number of nodes by which to increase node table
   * @return old value
   */
  public abstract int setMaxIncrease(int x);

  /**
   * Set factor by which to increase node table after a garbage collection. The amount of growth is
   * still limited by <tt>setMaxIncrease()</tt>.
   *
   * @param x factor by which to increase node table after GC
   * @return old value
   */
  public abstract double setIncreaseFactor(double x);

  /**
   * Sets the cache ratio for the operator caches. When the node table grows, operator caches will
   * also grow to maintain the ratio.
   *
   * <p>Compare to bdd_setcacheratio.
   *
   * @param x cache ratio
   * @return the previous cache ratio
   */
  public abstract int setCacheRatio(int x);

  /**
   * Returns the logical 'or' of zero or more BDDs. None of the input BDDs are consumed or mutated.
   * More efficient than using {@link BDD::or} or {@link BDD::orWith} iteratively, especially for
   * large numbers of operands, because it creates fewer intermediate BDDs.
   *
   * @param bddOperands the BDDs to 'or' together
   */
  public abstract BDD orAll(BDD... bddOperands);

  /**
   * Returns the logical 'or' of zero or more BDDs. None of the input BDDs are consumed or mutated.
   * More efficient than using {@link BDD::or} or {@link BDD::orWith} iteratively, especially for
   * large numbers of operands, because it creates fewer intermediate BDDs.
   *
   * @param bddOperands the BDDs to 'or' together
   */
  public abstract BDD orAll(Collection<BDD> bddOperands);

  /**
   * Sets the node table size.
   *
   * @param n new size of table
   * @return old size of table
   */
  public abstract int setNodeTableSize(int n);

  /**
   * Sets cache size.
   *
   * @return old cache size
   */
  public abstract int setCacheSize(int n);

  /** ** VARIABLE NUMBERS *** */

  /**
   * Returns the number of defined variables.
   *
   * <p>Compare to bdd_varnum.
   */
  public abstract int varNum();

  /**
   * Set the number of used BDD variables. It can be called more than one time, but only to increase
   * the number of variables.
   *
   * <p>Compare to bdd_setvarnum.
   *
   * @param num new number of BDD variables
   * @return old number of BDD variables
   */
  public abstract int setVarNum(int num);

  /**
   * Add extra BDD variables. Extends the current number of allocated BDD variables with num extra
   * variables.
   *
   * <p>Compare to bdd_extvarnum.
   *
   * @param num number of BDD variables to add
   * @return old number of BDD variables
   */
  public int extVarNum(int num) {
    int start = varNum();
    if (num < 0 || num > 0x3FFFFFFF) {
      throw new BDDException();
    }
    setVarNum(start + num);
    return start;
  }

  /**
   * Returns a BDD representing the I'th variable. (One node with the children true and false.) The
   * requested variable must be in the (zero-indexed) range defined by <tt>setVarNum</tt>.
   *
   * <p>Compare to bdd_ithvar.
   *
   * @param var the variable number
   * @return the I'th variable on success, otherwise the constant false BDD
   */
  public abstract BDD ithVar(int var);

  /**
   * Returns a BDD representing the negation of the I'th variable. (One node with the children false
   * and true.) The requested variable must be in the (zero-indexed) range defined by
   * <tt>setVarNum</tt>.
   *
   * <p>Compare to bdd_nithvar.
   *
   * @param var the variable number
   * @return the negated I'th variable on success, otherwise the constant false BDD
   */
  public abstract BDD nithVar(int var);

  /** ** INPUT / OUTPUT *** */

  /**
   * Prints all used entries in the node table.
   *
   * <p>Compare to bdd_printall.
   */
  public abstract void printAll();

  /**
   * Prints the node table entries used by a BDD.
   *
   * <p>Compare to bdd_printtable.
   */
  public abstract void printTable(BDD b);

  /** Used for tokenization during loading. */
  protected StringTokenizer tokenizer;

  /**
   * Read the next token from the file.
   *
   * @param ifile reader
   * @return next string token
   */
  protected String readNext(BufferedReader ifile) throws IOException {
    while (tokenizer == null || !tokenizer.hasMoreTokens()) {
      String s = ifile.readLine();
      if (s == null) {
        throw new BDDException("Incorrect file format");
      }
      tokenizer = new StringTokenizer(s);
    }
    return tokenizer.nextToken();
  }

  /** ** REORDERING *** */

  /**
   * Convert from a BDD level to a BDD variable.
   *
   * <p>Compare to bdd_level2var.
   */
  public abstract int level2Var(int level);

  /**
   * Convert from a BDD variable to a BDD level.
   *
   * <p>Compare to bdd_var2level.
   */
  public abstract int var2Level(int var);

  /**
   * This function sets the current variable order to be the one defined by neworder. The variable
   * parameter neworder is interpreted as a sequence of variable indices and the new variable order
   * is exactly this sequence. The array must contain all the variables defined so far. If, for
   * instance the current number of variables is 3 and neworder contains [1; 0; 2] then the new
   * variable order is v1<v0<v2.
   *
   * <p>Note that this operation must walk through the node table many times, and therefore it is
   * much more efficient to call this when the node table is small.
   *
   * @param neworder new variable order
   */
  public abstract void setVarOrder(int[] neworder);

  /**
   * Gets the current variable order.
   *
   * @return variable order
   */
  public int[] getVarOrder() {
    int n = varNum();
    int[] result = new int[n];
    for (int i = 0; i < n; ++i) {
      result[i] = level2Var(i);
    }
    return result;
  }

  /**
   * Make a new BDDPairing object.
   *
   * <p>Compare to bdd_newpair.
   */
  public abstract BDDPairing makePair();

  /**
   * Make a new pairing that maps from one variable to another.
   *
   * @param oldvar old variable
   * @param newvar new variable
   * @return BDD pairing
   */
  public BDDPairing makePair(int oldvar, int newvar) {
    BDDPairing p = makePair();
    p.set(oldvar, newvar);
    return p;
  }

  /**
   * Make a new pairing that maps from one variable to another BDD.
   *
   * @param oldvar old variable
   * @param newvar new BDD
   * @return BDD pairing
   */
  public BDDPairing makePair(int oldvar, BDD newvar) {
    BDDPairing p = makePair();
    p.set(oldvar, newvar);
    return p;
  }

  /**
   * Make a new pairing that maps from one BDD domain to another.
   *
   * @param oldvar old BDD domain
   * @param newvar new BDD domain
   * @return BDD pairing
   */
  public BDDPairing makePair(BDDDomain oldvar, BDDDomain newvar) {
    BDDPairing p = makePair();
    p.set(oldvar, newvar);
    return p;
  }

  /**
   * Duplicate a BDD variable.
   *
   * @param var var to duplicate
   * @return index of new variable
   */
  public abstract int duplicateVar(int var);

  /** ** BDD STATS *** */

  /**
   * Get the BDD library version.
   *
   * @return version string
   */
  public abstract String getVersion();

  /**
   * Counts the number of shared nodes in a collection of BDDs. Counts all distinct nodes that are
   * used in the BDDs -- if a node is used in more than one BDD then it only counts once.
   *
   * <p>Compare to bdd_anodecount.
   */
  public abstract int nodeCount(Collection<BDD> r);

  /**
   * Get the number of allocated nodes. This includes both dead and active nodes.
   *
   * <p>Compare to bdd_getallocnum.
   */
  public abstract int getNodeTableSize();

  /**
   * Get the number of active nodes in use. Note that dead nodes that have not been reclaimed yet by
   * a garbage collection are counted as active.
   *
   * <p>Compare to bdd_getnodenum.
   */
  public abstract int getNodeNum();

  /**
   * Get the current size of the cache, in entries.
   *
   * @return size of cache
   */
  public abstract int getCacheSize();

  /**
   * Print cache statistics.
   *
   * <p>Compare to bdd_printstat.
   */
  public abstract void printStat();

  /**
   * Stores statistics about garbage collections.
   *
   * @author jwhaley
   * @version $Id: BDDFactory.java,v 1.18 2005/10/12 10:27:08 joewhaley Exp $
   */
  public static class GCStats {
    public int nodes;
    public int freenodes;
    public long time;
    public long sumtime;
    public int num;

    protected GCStats() {}

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Garbage collection #");
      sb.append(num);
      sb.append(": ");
      sb.append(nodes);
      sb.append(" nodes / ");
      sb.append(freenodes);
      sb.append(" free");

      sb.append(" / ");
      sb.append((float) time / (float) 1000);
      sb.append("s / ");
      sb.append((float) sumtime / (float) 1000);
      sb.append("s total");
      return sb.toString();
    }
  }

  /** Singleton object for GC statistics. */
  protected GCStats gcstats = new GCStats();

  /**
   * Return the current GC statistics for this BDD factory.
   *
   * @return GC statistics
   */
  public GCStats getGCStats() {
    return gcstats;
  }

  /**
   * Stores statistics about reordering.
   *
   * @author jwhaley
   * @version $Id: BDDFactory.java,v 1.18 2005/10/12 10:27:08 joewhaley Exp $
   */
  public static class ReorderStats {

    public long time;
    public int usednum_before, usednum_after;

    protected ReorderStats() {}

    public int gain() {
      if (usednum_before == 0) {
        return 0;
      }

      return (100 * (usednum_before - usednum_after)) / usednum_before;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Went from ");
      sb.append(usednum_before);
      sb.append(" to ");
      sb.append(usednum_after);
      sb.append(" nodes, gain = ");
      sb.append(gain());
      sb.append("% (");
      sb.append((float) time / 1000f);
      sb.append(" sec)");
      return sb.toString();
    }
  }

  /** Singleton object for reorder statistics. */
  ReorderStats reorderstats = new ReorderStats();

  /**
   * Return the current reordering statistics for this BDD factory.
   *
   * @return reorder statistics
   */
  public ReorderStats getReorderStats() {
    return reorderstats;
  }

  /**
   * Stores statistics about the operator cache.
   *
   * @author jwhaley
   * @version $Id: BDDFactory.java,v 1.18 2005/10/12 10:27:08 joewhaley Exp $
   */
  public static class CacheStats {
    public int uniqueAccess;
    public int uniqueChain;
    public int uniqueHit;
    public int uniqueMiss;
    public int uniqueTrivial;
    public int opHit;
    public int opMiss;
    public int opOverwrite;
    public int swapCount;

    protected CacheStats() {}

    void copyFrom(CacheStats that) {
      uniqueAccess = that.uniqueAccess;
      uniqueChain = that.uniqueChain;
      uniqueHit = that.uniqueHit;
      uniqueMiss = that.uniqueMiss;
      uniqueTrivial = that.uniqueTrivial;
      opHit = that.opHit;
      opMiss = that.opMiss;
      opOverwrite = that.opOverwrite;
      swapCount = that.swapCount;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      String newLine = getProperty("line.separator", "\n");
      sb.append(newLine);
      sb.append("Cache statistics");
      sb.append(newLine);
      sb.append("----------------");
      sb.append(newLine);

      sb.append("Unique Trivial: ");
      sb.append(uniqueTrivial);
      sb.append(newLine);
      sb.append("Unique Access:  ");
      sb.append(uniqueAccess);
      sb.append(newLine);
      sb.append("Unique Hit:     ");
      sb.append(uniqueHit);
      sb.append(newLine);
      sb.append("Unique Miss:    ");
      sb.append(uniqueMiss);
      sb.append(newLine);
      sb.append("Unique Chain:   ");
      sb.append(uniqueChain);
      sb.append(newLine);
      sb.append("=> Hit rate =   ");
      if (uniqueHit + uniqueMiss > 0) {
        sb.append(((float) uniqueHit) / ((float) uniqueHit + uniqueMiss));
      } else {
        sb.append((float) 0);
      }
      sb.append(newLine);
      sb.append("Operator Hits:  ");
      sb.append(opHit);
      sb.append(newLine);
      sb.append("Operator Miss:  ");
      sb.append(opMiss);
      sb.append(newLine);
      sb.append("Operator Overwrite:  ");
      sb.append(opOverwrite);
      sb.append(newLine);
      sb.append("=> Hit rate =   ");
      if (opHit + opMiss > 0) {
        sb.append(((float) opHit) / ((float) opHit + opMiss));
      } else {
        sb.append((float) 0);
      }
      sb.append(newLine);
      sb.append("Swap count =    ");
      sb.append(swapCount);
      sb.append(newLine);
      return sb.toString();
    }
  }

  /** Singleton object for cache statistics. */
  protected CacheStats cachestats = new CacheStats();

  /**
   * Return the current cache statistics for this BDD factory.
   *
   * @return cache statistics
   */
  public CacheStats getCacheStats() {
    return cachestats;
  }

  // TODO: bdd_sizeprobe_hook
  // TODO: bdd_reorder_probe

  /** ** FINITE DOMAINS *** */
  protected BDDDomain[] domain;

  protected int fdvarnum;
  protected int firstbddvar;

  /**
   * Implementors must implement this factory method to create BDDDomain objects of the correct
   * type.
   */
  protected abstract BDDDomain createDomain(int a, BigInteger b);

  /**
   * Creates a new finite domain block of the given size. Allocates log 2 (|domainSize|) BDD
   * variables for the domain.
   */
  public BDDDomain extDomain(long domainSize) {
    return extDomain(BigInteger.valueOf(domainSize));
  }

  public BDDDomain extDomain(BigInteger domainSize) {
    return extDomain(new BigInteger[] {domainSize})[0];
  }

  /**
   * Extends the set of finite domain blocks with domains of the given sizes. Each entry in
   * domainSizes is the size of a new finite domain which later on can be used for finite state
   * machine traversal and other operations on finite domains. Each domain allocates log 2
   * (|domainSizes[i]|) BDD variables to be used later. The ordering is interleaved for the domains
   * defined in each call to extDomain. This means that assuming domain D0 needs 2 BDD variables x1
   * and x2 , and another domain D1 needs 4 BDD variables y1, y2, y3 and y4, then the order then
   * will be x1, y1, x2, y2, y3, y4. The new domains are returned in order. The BDD variables needed
   * to encode the domain are created for the purpose and do not interfere with the BDD variables
   * already in use.
   *
   * <p>Compare to fdd_extdomain.
   */
  public BDDDomain[] extDomain(int[] dom) {
    BigInteger[] a = new BigInteger[dom.length];
    for (int i = 0; i < a.length; ++i) {
      a[i] = BigInteger.valueOf(dom[i]);
    }
    return extDomain(a);
  }

  public BDDDomain[] extDomain(long[] dom) {
    BigInteger[] a = new BigInteger[dom.length];
    for (int i = 0; i < a.length; ++i) {
      a[i] = BigInteger.valueOf(dom[i]);
    }
    return extDomain(a);
  }

  public BDDDomain[] extDomain(BigInteger[] domainSizes) {
    int offset = fdvarnum;
    int binoffset;
    int extravars = 0;
    int n, bn;
    boolean more;
    int num = domainSizes.length;

    /* Build domain table */
    if (domain == null) /* First time */ {
      domain = new BDDDomain[num];
    } else /* Allocated before */ {
      if (fdvarnum + num > domain.length) {
        int fdvaralloc = domain.length + Math.max(num, domain.length);
        BDDDomain[] d2 = new BDDDomain[fdvaralloc];
        System.arraycopy(domain, 0, d2, 0, domain.length);
        domain = d2;
      }
    }

    /* Create bdd variable tables */
    for (n = 0; n < num; n++) {
      domain[n + fdvarnum] = createDomain(n + fdvarnum, domainSizes[n]);
      extravars += domain[n + fdvarnum].varNum();
    }

    binoffset = firstbddvar;
    int bddvarnum = varNum();
    if (firstbddvar + extravars > bddvarnum) {
      setVarNum(firstbddvar + extravars);
    }

    /* Set correct variable sequence (interleaved) */
    for (bn = 0, more = true; more; bn++) {
      more = false;

      for (n = 0; n < num; n++) {
        if (bn < domain[n + fdvarnum].varNum()) {
          more = true;
          domain[n + fdvarnum].ivar[bn] = binoffset++;
        }
      }
    }

    for (n = 0; n < num; n++) {
      domain[n + fdvarnum].var = makeSet(domain[n + fdvarnum].ivar);
    }

    fdvarnum += num;
    firstbddvar += extravars;

    BDDDomain[] r = new BDDDomain[num];
    System.arraycopy(domain, offset, r, 0, num);
    return r;
  }

  /**
   * This function takes two finite domain blocks and merges them into a new one, such that the new
   * one is encoded using both sets of BDD variables.
   *
   * <p>Compare to fdd_overlapdomain.
   */
  public BDDDomain overlapDomain(BDDDomain d1, BDDDomain d2) {
    BDDDomain d;
    int n;

    int fdvaralloc = domain.length;
    if (fdvarnum + 1 > fdvaralloc) {
      fdvaralloc += fdvaralloc;
      BDDDomain[] domain2 = new BDDDomain[fdvaralloc];
      System.arraycopy(domain, 0, domain2, 0, domain.length);
      domain = domain2;
    }

    d = domain[fdvarnum];
    d.realsize = d1.realsize.multiply(d2.realsize);
    d.ivar = new int[d1.varNum() + d2.varNum()];

    for (n = 0; n < d1.varNum(); n++) {
      d.ivar[n] = d1.ivar[n];
    }
    for (n = 0; n < d2.varNum(); n++) {
      d.ivar[d1.varNum() + n] = d2.ivar[n];
    }

    d.var = makeSet(d.ivar);
    // bdd_addref(d.var);

    fdvarnum++;
    return d;
  }

  /**
   * Returns a BDD defining all the variable sets used to define the variable blocks in the given
   * array.
   *
   * <p>Compare to fdd_makeset.
   */
  public BDD makeSet(BDDDomain[] v) {
    BDD res = one();
    int n;

    for (n = 0; n < v.length; n++) {
      res.andWith(v[n].set());
    }

    return res;
  }

  /**
   * Clear all allocated finite domain blocks that were defined by extDomain() or overlapDomain().
   *
   * <p>Compare to fdd_clearall.
   */
  public void clearAllDomains() {
    domain = null;
    fdvarnum = 0;
    firstbddvar = 0;
  }

  /**
   * Returns the number of finite domain blocks defined by calls to extDomain().
   *
   * <p>Compare to fdd_domainnum.
   */
  public int numberOfDomains() {
    return fdvarnum;
  }

  /** Returns the ith finite domain block, as defined by calls to extDomain(). */
  public BDDDomain getDomain(int i) {
    if (i < 0 || i >= fdvarnum) {
      throw new IndexOutOfBoundsException();
    }
    return domain[i];
  }

  // TODO: fdd_file_hook, fdd_strm_hook

  /**
   * Creates a variable ordering from a string. The resulting order can be passed into
   * <tt>setVarOrder()</tt>. Example: in the order "A_BxC_DxExF", the bits for A are first, followed
   * by the bits for B and C interleaved, followed by the bits for D, E, and F interleaved.
   *
   * <p>Obviously, domain names cannot contain the 'x' or '_' characters.
   *
   * @param reverseLocal whether to reverse the bits of each domain
   * @param ordering string representation of ordering
   * @return int[] of ordering
   * @see net.sf.javabdd.BDDFactory#setVarOrder(int[])
   */
  public int[] makeVarOrdering(boolean reverseLocal, String ordering) {

    int varnum = varNum();

    int nDomains = numberOfDomains();
    int[][] localOrders = new int[nDomains][];
    for (int i = 0; i < localOrders.length; ++i) {
      localOrders[i] = new int[getDomain(i).varNum()];
    }

    for (int i = 0; i < nDomains; ++i) {
      BDDDomain d = getDomain(i);
      int nVars = d.varNum();
      for (int j = 0; j < nVars; ++j) {
        if (reverseLocal) {
          localOrders[i][j] = nVars - j - 1;
        } else {
          localOrders[i][j] = j;
        }
      }
    }

    BDDDomain[] doms = new BDDDomain[nDomains];

    int[] varorder = new int[varnum];

    // LOGGER.info("Ordering: "+ordering);
    StringTokenizer st = new StringTokenizer(ordering, "x_", true);
    int numberOfDomains = 0, bitIndex = 0;
    boolean[] done = new boolean[nDomains];
    for (int i = 0; ; ++i) {
      String s = st.nextToken();
      BDDDomain d;
      for (int j = 0; ; ++j) {
        if (j == numberOfDomains()) {
          throw new BDDException("bad domain: " + s);
        }
        d = getDomain(j);
        if (s.equals(d.getName())) {
          break;
        }
      }
      if (done[d.getIndex()]) {
        throw new BDDException("duplicate domain: " + s);
      }
      done[d.getIndex()] = true;
      doms[i] = d;
      if (st.hasMoreTokens()) {
        s = st.nextToken();
        if (s.equals("x")) {
          ++numberOfDomains;
          continue;
        }
      }
      bitIndex =
          fillInVarIndices(
              doms, i - numberOfDomains, numberOfDomains + 1, localOrders, bitIndex, varorder);
      if (!st.hasMoreTokens()) {
        break;
      }
      if (s.equals("_")) {
        numberOfDomains = 0;
      } else {
        throw new BDDException("bad token: " + s);
      }
    }

    for (int i = 0; i < doms.length; ++i) {
      if (!done[i]) {
        throw new BDDException("missing domain #" + i + ": " + getDomain(i));
      }
    }

    while (bitIndex < varorder.length) {
      varorder[bitIndex] = bitIndex;
      ++bitIndex;
    }

    int[] test = new int[varorder.length];
    System.arraycopy(varorder, 0, test, 0, varorder.length);
    Arrays.sort(test);
    for (int i = 0; i < test.length; ++i) {
      if (test[i] != i) {
        throw new BDDException(test[i] + " != " + i);
      }
    }

    return varorder;
  }

  /** Helper function for makeVarOrder(). */
  static int fillInVarIndices(
      BDDDomain[] doms,
      int domainIndex,
      int numDomains,
      int[][] localOrders,
      int bitIndex,
      int[] varorder) {
    // calculate size of largest domain to interleave
    int maxBits = 0;
    for (int i = 0; i < numDomains; ++i) {
      BDDDomain d = doms[domainIndex + i];
      maxBits = Math.max(maxBits, d.varNum());
    }
    // interleave the domains
    for (int bitNumber = 0; bitNumber < maxBits; ++bitNumber) {
      for (int i = 0; i < numDomains; ++i) {
        BDDDomain d = doms[domainIndex + i];
        if (bitNumber < d.varNum()) {
          int di = d.getIndex();
          int local = localOrders[di][bitNumber];
          if (local >= d.vars().length) {
            LOGGER.error("bug!");
          }
          if (bitIndex >= varorder.length) {
            LOGGER.error("bug2!");
          }
          varorder[bitIndex++] = d.vars()[local];
        }
      }
    }
    return bitIndex;
  }

  /** ** BIT VECTORS *** */

  /**
   * Implementors must implement this factory method to create BDDBitVector objects of the correct
   * type.
   */
  protected abstract BDDBitVector createBitVector(int a);

  /**
   * Build a bit vector that is constant true or constant false.
   *
   * <p>Compare to bvec_true, bvec_false.
   */
  public BDDBitVector buildVector(int bitnum, boolean b) {
    BDDBitVector v = createBitVector(bitnum);
    v.initialize(b);
    return v;
  }

  /**
   * Build a bit vector that corresponds to a constant value.
   *
   * <p>Compare to bvec_con.
   */
  public BDDBitVector constantVector(int bitnum, long val) {
    BDDBitVector v = createBitVector(bitnum);
    v.initialize(val);
    return v;
  }

  public BDDBitVector constantVector(int bitnum, BigInteger val) {
    BDDBitVector v = createBitVector(bitnum);
    v.initialize(val);
    return v;
  }

  /**
   * Build a bit vector using variables offset, offset+step, offset+2*step, ... ,
   * offset+(bitnum-1)*step.
   *
   * <p>Compare to bvec_var.
   */
  public BDDBitVector buildVector(int bitnum, int offset, int step) {
    BDDBitVector v = createBitVector(bitnum);
    v.initialize(offset, step);
    return v;
  }

  /**
   * Build a bit vector using variables from the given BDD domain.
   *
   * <p>Compare to bvec_varfdd.
   */
  public BDDBitVector buildVector(BDDDomain d) {
    BDDBitVector v = createBitVector(d.varNum());
    v.initialize(d);
    return v;
  }

  /**
   * Build a bit vector using the given variables.
   *
   * <p>compare to bvec_varvec.
   */
  public BDDBitVector buildVector(int[] var) {
    BDDBitVector v = createBitVector(var.length);
    v.initialize(var);
    return v;
  }

  /** ** CALLBACKS *** */
  protected List<Object[]> gc_callbacks, reorder_callbacks, resize_callbacks;

  /**
   * Register a callback that is called when garbage collection is about to occur.
   *
   * @param o base object
   * @param m method
   */
  public void registerGCCallback(Object o, Method m) {
    if (gc_callbacks == null) {
      gc_callbacks = new LinkedList<>();
    }
    registerCallback(gc_callbacks, o, m);
  }

  /**
   * Unregister a garbage collection callback that was previously registered.
   *
   * @param o base object
   * @param m method
   */
  public void unregisterGCCallback(Object o, Method m) {
    if (gc_callbacks == null) {
      throw new BDDException();
    }
    if (!unregisterCallback(gc_callbacks, o, m)) {
      throw new BDDException();
    }
  }

  /**
   * Register a callback that is called when reordering is about to occur.
   *
   * @param o base object
   * @param m method
   */
  public void registerReorderCallback(Object o, Method m) {
    if (reorder_callbacks == null) {
      reorder_callbacks = new LinkedList<>();
    }
    registerCallback(reorder_callbacks, o, m);
  }

  /**
   * Unregister a reorder callback that was previously registered.
   *
   * @param o base object
   * @param m method
   */
  public void unregisterReorderCallback(Object o, Method m) {
    if (reorder_callbacks == null) {
      throw new BDDException();
    }
    if (!unregisterCallback(reorder_callbacks, o, m)) {
      throw new BDDException();
    }
  }

  /**
   * Register a callback that is called when node table resizing is about to occur.
   *
   * @param o base object
   * @param m method
   */
  public void registerResizeCallback(Object o, Method m) {
    if (resize_callbacks == null) {
      resize_callbacks = new LinkedList<>();
    }
    registerCallback(resize_callbacks, o, m);
  }

  /**
   * Unregister a reorder callback that was previously registered.
   *
   * @param o base object
   * @param m method
   */
  public void unregisterResizeCallback(Object o, Method m) {
    if (resize_callbacks == null) {
      throw new BDDException();
    }
    if (!unregisterCallback(resize_callbacks, o, m)) {
      throw new BDDException();
    }
  }

  protected void gbc_handler(boolean pre, GCStats s) {
    if (gc_callbacks == null) {
      bdd_default_gbchandler(pre, s);
    } else {
      doCallbacks(gc_callbacks, pre ? 1 : 0, s);
    }
  }

  protected static void bdd_default_gbchandler(boolean pre, GCStats s) {
    if (!pre) {
      LOGGER.info(s);
    }
  }

  void reorder_handler(boolean b, ReorderStats s) {
    if (b) {
      s.usednum_before = getNodeNum();
      s.time = System.currentTimeMillis();
    } else {
      s.time = System.currentTimeMillis() - s.time;
      s.usednum_after = getNodeNum();
    }
    if (reorder_callbacks == null) {
      bdd_default_reohandler(b, s);
    } else {
      doCallbacks(reorder_callbacks, b, s);
    }
  }

  protected void bdd_default_reohandler(boolean prestate, ReorderStats s) {
    int verbose = 1;
    if (verbose > 0) {
      if (prestate) {
        LOGGER.info("Start reordering");
      } else {
        LOGGER.info("End reordering. {}", s);
      }
    }
  }

  protected void resize_handler(int oldsize, int newsize) {
    if (resize_callbacks == null) {
      bdd_default_reshandler(oldsize, newsize);
    } else {
      doCallbacks(resize_callbacks, oldsize, newsize);
    }
  }

  protected static void bdd_default_reshandler(int oldsize, int newsize) {
    int verbose = 1;
    if (verbose > 0) {
      LOGGER.info("Resizing node table from {} to {}", oldsize, newsize);
    }
  }

  protected void registerCallback(List<Object[]> callbacks, Object o, Method m) {
    if (!Modifier.isPublic(m.getModifiers()) && !m.isAccessible()) {
      throw new BDDException("Callback method not accessible");
    }
    if (!Modifier.isStatic(m.getModifiers())) {
      if (o == null) {
        throw new BDDException("Base object for callback method is null");
      }
      if (!m.getDeclaringClass().isAssignableFrom(o.getClass())) {
        throw new BDDException("Base object for callback method is the wrong type");
      }
    }
    callbacks.add(new Object[] {o, m});
  }

  protected boolean unregisterCallback(List<Object[]> callbacks, Object o, Method m) {
    if (callbacks != null) {
      for (Iterator<Object[]> i = callbacks.iterator(); i.hasNext(); ) {
        Object[] cb = i.next();
        if (o == cb[0] && m.equals(cb[1])) {
          i.remove();
          return true;
        }
      }
    }
    return false;
  }

  protected void doCallbacks(List<Object[]> callbacks, Object arg1, Object arg2) {
    if (callbacks != null) {
      for (Object callback : callbacks) {
        Object[] cb = (Object[]) callback;
        Object o = cb[0];
        Method m = (Method) cb[1];
        try {
          switch (m.getParameterTypes().length) {
            case 0:
              m.invoke(o);
              break;
            case 1:
              m.invoke(o, arg1);
              break;
            case 2:
              m.invoke(o, arg1, arg2);
              break;
            default:
              throw new BDDException("Wrong number of arguments for " + m);
          }
        } catch (IllegalArgumentException | IllegalAccessException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          if (e.getTargetException() instanceof RuntimeException) {
            throw (RuntimeException) e.getTargetException();
          }
          if (e.getTargetException() instanceof Error) {
            throw (Error) e.getTargetException();
          }
          e.printStackTrace();
        }
      }
    }
  }
}
