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

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * This BDD factory keeps track of what domains each BDD uses, and complains if you try to do an
 * operation where the domains do not match.
 *
 * @see net.sf.javabdd.BDDFactory
 * @author John Whaley
 * @version $Id: TypedBDDFactory.java,v 1.8 2005/05/21 08:47:10 joewhaley Exp $
 */
public class TypedBDDFactory extends BDDFactory {

  static PrintStream out = System.out;
  static boolean STACK_TRACES = true;

  BDDFactory factory;

  public TypedBDDFactory(BDDFactory f) {
    this.factory = f;
  }

  public static BDDFactory init(int nodenum, int cachesize) {
    BDDFactory a = BDDFactory.init(nodenum, cachesize);
    return new TypedBDDFactory(a);
  }

  @Override
  public BDD zero() {
    return new TypedBDD(factory.zero(), makeSet());
  }

  @Override
  public BDD one() {
    Set<BDDDomain> s = makeSet();
    return new TypedBDD(factory.one(), s);
  }

  @Override
  protected void initialize(int nodenum, int cachesize) {
    factory.initialize(nodenum, cachesize);
  }

  @Override
  public boolean isInitialized() {
    return factory.isInitialized();
  }

  @Override
  public void done() {
    factory.done();
  }

  @Override
  public void setError(int code) {
    factory.setError(code);
  }

  @Override
  public void clearError() {
    factory.clearError();
  }

  @Override
  public int setMaxNodeNum(int size) {
    return factory.setMaxNodeNum(size);
  }

  @Override
  public int setNodeTableSize(int size) {
    return factory.setNodeTableSize(size);
  }

  @Override
  public int setCacheSize(int size) {
    return factory.setCacheSize(size);
  }

  @Override
  public double setMinFreeNodes(double x) {
    return factory.setMinFreeNodes(x);
  }

  @Override
  public double setIncreaseFactor(double x) {
    return factory.setIncreaseFactor(x);
  }

  @Override
  public int setMaxIncrease(int x) {
    return factory.setMaxIncrease(x);
  }

  @Override
  public int setCacheRatio(int r) {
    return factory.setCacheRatio(r);
  }

  @Override
  public BDD orAll(BDD... bddOperands) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDD orAll(Collection<BDD> bddOperands) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int varNum() {
    return factory.varNum();
  }

  @Override
  public int setVarNum(int num) {
    return factory.setVarNum(num);
  }

  @Override
  public int duplicateVar(int var) {
    return factory.duplicateVar(var);
  }

  public BDDDomain whichDomain(int var) {
    for (int i = 0; i < numberOfDomains(); ++i) {
      int[] vars = getDomain(i).vars();
      for (int var1 : vars) {
        if (var == var1) {
          return getDomain(i);
        }
      }
    }
    return null;
  }

  @Override
  public BDD ithVar(int var) {
    Set<BDDDomain> s = makeSet();
    // BDDDomain d = whichDomain(var);
    // if (d != null) s.add(d);
    return new TypedBDD(factory.ithVar(var), s);
  }

  @Override
  public BDD nithVar(int var) {
    Set<BDDDomain> s = makeSet();
    // BDDDomain d = whichDomain(var);
    // if (d != null) s.add(d);
    return new TypedBDD(factory.nithVar(var), s);
  }

  @Override
  public void printAll() {
    factory.printAll();
  }

  @Override
  public void printTable(BDD b) {
    TypedBDD bdd1 = (TypedBDD) b;
    factory.printTable(bdd1.bdd);
  }

  @Override
  public BDD load(String filename) throws IOException {
    // TODO domains?
    Set<BDDDomain> d = makeSet();
    return new TypedBDD(factory.load(filename), d);
  }

  @Override
  public void save(String filename, BDD var) throws IOException {
    TypedBDD bdd1 = (TypedBDD) var;
    factory.save(filename, bdd1.bdd);
  }

  @Override
  public int level2Var(int level) {
    return factory.level2Var(level);
  }

  @Override
  public int var2Level(int var) {
    return factory.var2Level(var);
  }

  @Override
  public void reorder(ReorderMethod m) {
    factory.reorder(m);
  }

  @Override
  public void autoReorder(ReorderMethod method) {
    factory.autoReorder(method);
  }

  @Override
  public void autoReorder(ReorderMethod method, int max) {
    factory.autoReorder(method, max);
  }

  @Override
  public ReorderMethod getReorderMethod() {
    return factory.getReorderMethod();
  }

  @Override
  public int getReorderTimes() {
    return factory.getReorderTimes();
  }

  @Override
  public void disableReorder() {
    factory.disableReorder();
  }

  @Override
  public void enableReorder() {
    factory.enableReorder();
  }

  @Override
  public int reorderVerbose(int v) {
    return factory.reorderVerbose(v);
  }

  @Override
  public void setVarOrder(int[] neworder) {
    factory.setVarOrder(neworder);
  }

  @Override
  public void addVarBlock(BDD var, boolean fixed) {
    TypedBDD bdd1 = (TypedBDD) var;
    factory.addVarBlock(bdd1.bdd, fixed);
  }

  @Override
  public void addVarBlock(int first, int last, boolean fixed) {
    factory.addVarBlock(first, last, fixed);
  }

  @Override
  public void varBlockAll() {
    factory.varBlockAll();
  }

  @Override
  public void clearVarBlocks() {
    factory.clearVarBlocks();
  }

  @Override
  public void printOrder() {
    factory.printOrder();
  }

  @Override
  public int nodeCount(Collection<BDD> r) {
    List<BDD> inner = r.stream().map(t -> ((TypedBDD) t).bdd).collect(Collectors.toList());
    return factory.nodeCount(inner);
  }

  @Override
  public int getNodeTableSize() {
    return factory.getNodeTableSize();
  }

  @Override
  public int getNodeNum() {
    return factory.getNodeNum();
  }

  @Override
  public int getCacheSize() {
    return factory.getCacheSize();
  }

  @Override
  public int reorderGain() {
    return factory.reorderGain();
  }

  @Override
  public void printStat() {
    factory.printStat();
  }

  @Override
  public BDDPairing makePair() {
    return new TypedBDDPairing(factory.makePair());
  }

  @Override
  public void swapVar(int v1, int v2) {
    factory.swapVar(v1, v2);
  }

  @Override
  protected BDDDomain createDomain(int a, BigInteger b) {
    return new TypedBDDDomain(factory.getDomain(a), a, b);
  }

  @Override
  protected BDDBitVector createBitVector(int a) {
    return factory.createBitVector(a);
  }

  @Override
  public BDDDomain[] extDomain(long[] domainSizes) {
    factory.extDomain(domainSizes);
    return super.extDomain(domainSizes);
  }

  public static Set<BDDDomain> makeSet() {
    // return SortedArraySet.FACTORY.makeSet(DOMAIN_COMPARATOR);
    return new TreeSet<>(DOMAIN_COMPARATOR);
  }

  public static Set<BDDDomain> makeSet(Set<BDDDomain> s) {
    Set<BDDDomain> r = new TreeSet<>(DOMAIN_COMPARATOR);
    r.addAll(s);
    return r;
  }

  public Set<BDDDomain> allDomains() {
    Set<BDDDomain> r = makeSet();
    for (int i = 0; i < factory.numberOfDomains(); ++i) {
      r.add(factory.getDomain(i));
    }
    return r;
  }

  public static Map<BDDDomain, BDDDomain> makeMap() {
    return new TreeMap<>(DOMAIN_COMPARATOR);
  }

  public static String domainNames(Set<BDDDomain> dom) {
    StringBuilder sb = new StringBuilder();
    for (Iterator<BDDDomain> i = dom.iterator(); i.hasNext(); ) {
      BDDDomain d = i.next();
      sb.append(d.getName());
      if (i.hasNext()) {
        sb.append(',');
      }
    }
    return sb.toString();
  }

  private static final Comparator<BDDDomain> DOMAIN_COMPARATOR =
      Comparator.comparingInt(BDDDomain::getIndex);

  /**
   * A BDD with types (domains) attached to it.
   *
   * @author jwhaley
   * @version $Id: TypedBDDFactory.java,v 1.8 2005/05/21 08:47:10 joewhaley Exp $
   */
  public class TypedBDD extends BDD {

    final BDD bdd;
    final Set<BDDDomain> dom;

    public TypedBDD(BDD bdd, Set<BDDDomain> dom) {
      this.bdd = bdd;
      this.dom = dom;
    }

    /** Returns the set of domains that this BDD uses. */
    public Set<BDDDomain> getDomainSet() {
      return dom;
    }

    /** Changes this BDD's domains to be the given set. */
    public void setDomains(Set<BDDDomain> d) {
      dom.clear();
      dom.addAll(d);
    }

    /** Changes this BDD's domain to be the given domain. */
    public void setDomains(BDDDomain d) {
      dom.clear();
      dom.add(d);
    }

    /** Changes this BDD's domains to be the given domains. */
    public void setDomains(BDDDomain d1, BDDDomain d2) {
      dom.clear();
      dom.add(d1);
      dom.add(d2);
    }

    /** Changes this BDD's domains to be the given domains. */
    public void setDomains(BDDDomain d1, BDDDomain d2, BDDDomain d3) {
      dom.clear();
      dom.add(d1);
      dom.add(d2);
      dom.add(d3);
    }

    /** Changes this BDD's domains to be the given domains. */
    public void setDomains(BDDDomain d1, BDDDomain d2, BDDDomain d3, BDDDomain d4) {
      dom.clear();
      dom.add(d1);
      dom.add(d2);
      dom.add(d3);
      dom.add(d4);
    }

    /** Changes this BDD's domains to be the given domains. */
    public void setDomains(BDDDomain d1, BDDDomain d2, BDDDomain d3, BDDDomain d4, BDDDomain d5) {
      dom.clear();
      dom.add(d1);
      dom.add(d2);
      dom.add(d3);
      dom.add(d4);
      dom.add(d5);
    }

    /** Returns the set of domains in BDD format. */
    BDD getDomains() {
      BDD b = factory.one();
      for (Object o : dom) {
        TypedBDDDomain d = (TypedBDDDomain) o;
        b.andWith(d.domain.set());
      }
      return b;
    }

    @Override
    public BDDFactory getFactory() {
      return TypedBDDFactory.this;
    }

    @Override
    public boolean isZero() {
      return bdd.isZero();
    }

    @Override
    public boolean isOne() {
      return bdd.isOne();
    }

    @Override
    public int var() {
      return bdd.var();
    }

    @Override
    public BDD high() {
      return new TypedBDD(bdd.high(), makeSet(dom));
    }

    @Override
    public BDD low() {
      return new TypedBDD(bdd.low(), makeSet(dom));
    }

    @Override
    public BDD id() {
      return new TypedBDD(bdd.id(), makeSet(dom));
    }

    @Override
    public BDD not() {
      return new TypedBDD(bdd.not(), makeSet(dom));
    }

    @Override
    public BDD ite(BDD thenBDD, BDD elseBDD) {
      TypedBDD bdd1 = (TypedBDD) thenBDD;
      TypedBDD bdd2 = (TypedBDD) elseBDD;
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      newDom.addAll(bdd1.dom);
      newDom.addAll(bdd2.dom);
      return new TypedBDD(bdd.ite(bdd1.bdd, bdd2.bdd), newDom);
    }

    @Override
    public BDD relprod(BDD that, BDD var) {
      TypedBDD bdd1 = (TypedBDD) that;
      TypedBDD bdd2 = (TypedBDD) var;
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      newDom.addAll(bdd1.dom);
      if (!newDom.containsAll(bdd2.dom)) {
        out.println("Warning! Quantifying domain that doesn't exist: " + domainNames(bdd2.dom));
        if (STACK_TRACES) {
          new Exception().printStackTrace(out);
        }
      }
      newDom.removeAll(bdd2.dom);
      return new TypedBDD(bdd.relprod(bdd1.bdd, bdd2.bdd), newDom);
    }

    @Override
    public BDD compose(BDD g, int var) {
      TypedBDD bdd1 = (TypedBDD) g;
      // TODO How does this change the domains?
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      return new TypedBDD(bdd.compose(bdd1.bdd, var), newDom);
    }

    @Override
    public BDD veccompose(BDDPairing pair) {
      TypedBDDPairing p = (TypedBDDPairing) pair;
      // TODO How does this change the domains?
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      return new TypedBDD(bdd.veccompose(p.pairing), newDom);
    }

    @Override
    public BDD constrain(BDD that) {
      TypedBDD bdd1 = (TypedBDD) that;
      // TODO How does this change the domains?
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      return new TypedBDD(bdd.constrain(bdd1.bdd), newDom);
    }

    @Override
    public BDD exist(BDD var) {
      TypedBDD bdd1 = (TypedBDD) var;
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      if (!newDom.containsAll(bdd1.dom)) {
        out.println("Warning! Quantifying domain that doesn't exist: " + domainNames(bdd1.dom));
        if (STACK_TRACES) {
          new Exception().printStackTrace(out);
        }
      }
      newDom.removeAll(bdd1.dom);
      return new TypedBDD(bdd.exist(bdd1.bdd), newDom);
    }

    @Override
    public BDD forAll(BDD var) {
      TypedBDD bdd1 = (TypedBDD) var;
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      if (!newDom.containsAll(bdd1.dom)) {
        out.println("Warning! Quantifying domain that doesn't exist: " + domainNames(bdd1.dom));
        if (STACK_TRACES) {
          new Exception().printStackTrace(out);
        }
      }
      newDom.removeAll(bdd1.dom);
      return new TypedBDD(bdd.forAll(bdd1.bdd), newDom);
    }

    @Override
    public BDD unique(BDD var) {
      TypedBDD bdd1 = (TypedBDD) var;
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      if (!newDom.containsAll(bdd1.dom)) {
        out.println("Warning! Quantifying domain that doesn't exist: " + domainNames(bdd1.dom));
        if (STACK_TRACES) {
          new Exception().printStackTrace(out);
        }
      }
      newDom.removeAll(bdd1.dom);
      return new TypedBDD(bdd.unique(bdd1.bdd), newDom);
    }

    @Override
    public BDD restrict(BDD var) {
      TypedBDD bdd1 = (TypedBDD) var;
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      if (!newDom.containsAll(bdd1.dom)) {
        out.println("Warning! Restricting domain that doesn't exist: " + domainNames(bdd1.dom));
        if (STACK_TRACES) {
          new Exception().printStackTrace(out);
        }
      }
      if (bdd1.bdd.satCount(bdd1.getDomains()) > 1.0) {
        out.println("Warning! Using restrict with more than one value");
        if (STACK_TRACES) {
          new Exception().printStackTrace(out);
        }
      }
      newDom.removeAll(bdd1.dom);
      return new TypedBDD(bdd.restrict(bdd1.bdd), newDom);
    }

    @Override
    public BDD restrictWith(BDD var) {
      TypedBDD bdd1 = (TypedBDD) var;
      if (!dom.containsAll(bdd1.dom)) {
        out.println("Warning! Restricting domain that doesn't exist: " + domainNames(bdd1.dom));
        if (STACK_TRACES) {
          new Exception().printStackTrace(out);
        }
      }
      if (bdd1.bdd.satCount(bdd1.getDomains()) > 1.0) {
        out.println("Warning! Using restrict with more than one value");
        if (STACK_TRACES) {
          new Exception().printStackTrace(out);
        }
      }
      dom.removeAll(bdd1.dom);
      bdd.restrictWith(bdd1.bdd);
      return this;
    }

    @Override
    public BDD simplify(BDD d) {
      TypedBDD bdd1 = (TypedBDD) d;
      // TODO How does this change the domains?
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      return new TypedBDD(bdd.simplify(bdd1.bdd), newDom);
    }

    @Override
    public BDD support() {
      // TODO How does this change the domains?
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      return new TypedBDD(bdd.support(), newDom);
    }

    void applyHelper(Set<BDDDomain> newDom, TypedBDD bdd0, TypedBDD bdd1, BDDOp opr) {
      switch (opr.id) {
        case 1: // xor
        case 2: // or
        case 4: // nor
        case 5: // imp
        case 6: // biimp
        case 7: // diff
        case 8: // less
        case 9: // invimp
          if (!bdd0.isZero() && !bdd1.isZero() && !newDom.equals(bdd1.dom)) {
            out.println(
                "Warning! Or'ing BDD with different domains: "
                    + domainNames(newDom)
                    + " != "
                    + domainNames(bdd1.dom));
            if (STACK_TRACES) {
              new Exception().printStackTrace(out);
            }
          }
          // fallthrough
        case 0: // and
        case 3: // nand
          newDom.addAll(bdd1.dom);
          break;
        default:
          throw new BDDException();
      }
    }

    @Override
    public BDD apply(BDD that, BDDOp opr) {
      TypedBDD bdd1 = (TypedBDD) that;
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      applyHelper(newDom, this, bdd1, opr);
      return new TypedBDD(bdd.apply(bdd1.bdd, opr), newDom);
    }

    @Override
    public BDD applyWith(BDD that, BDDOp opr) {
      TypedBDD bdd1 = (TypedBDD) that;
      applyHelper(dom, this, bdd1, opr);
      bdd.applyWith(bdd1.bdd, opr);
      return this;
    }

    @Override
    public BDD applyAll(BDD that, BDDOp opr, BDD var) {
      TypedBDD bdd1 = (TypedBDD) that;
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      applyHelper(newDom, this, bdd1, opr);
      TypedBDD bdd2 = (TypedBDD) var;
      if (!newDom.containsAll(bdd2.dom)) {
        out.println("Warning! Quantifying domain that doesn't exist: " + domainNames(bdd2.dom));
        if (STACK_TRACES) {
          new Exception().printStackTrace(out);
        }
      }
      newDom.removeAll(bdd2.dom);
      out.println(
          domainNames(dom)
              + " "
              + opr
              + " "
              + domainNames(bdd1.dom)
              + " / "
              + domainNames(bdd2.dom)
              + " = "
              + domainNames(newDom));
      return new TypedBDD(bdd.applyAll(bdd1.bdd, opr, bdd2.bdd), newDom);
    }

    @Override
    public BDD applyEx(BDD that, BDDOp opr, BDD var) {
      TypedBDD bdd1 = (TypedBDD) that;
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      applyHelper(newDom, this, bdd1, opr);
      TypedBDD bdd2 = (TypedBDD) var;
      if (!newDom.containsAll(bdd2.dom)) {
        out.println("Warning! Quantifying domain that doesn't exist: " + domainNames(bdd2.dom));
        if (STACK_TRACES) {
          new Exception().printStackTrace(out);
        }
      }
      newDom.removeAll(bdd2.dom);
      out.println(
          domainNames(dom)
              + " "
              + opr
              + " "
              + domainNames(bdd1.dom)
              + " / "
              + domainNames(bdd2.dom)
              + " = "
              + domainNames(newDom));
      return new TypedBDD(bdd.applyEx(bdd1.bdd, opr, bdd2.bdd), newDom);
    }

    @Override
    public BDD applyUni(BDD that, BDDOp opr, BDD var) {
      TypedBDD bdd1 = (TypedBDD) that;
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      applyHelper(newDom, this, bdd1, opr);
      TypedBDD bdd2 = (TypedBDD) var;
      if (!newDom.containsAll(bdd2.dom)) {
        out.println("Warning! Quantifying domain that doesn't exist: " + domainNames(bdd2.dom));
        if (STACK_TRACES) {
          new Exception().printStackTrace(out);
        }
      }
      newDom.removeAll(bdd2.dom);
      out.println(
          domainNames(dom)
              + " "
              + opr
              + " "
              + domainNames(bdd1.dom)
              + " / "
              + domainNames(bdd2.dom)
              + " = "
              + domainNames(newDom));
      return new TypedBDD(bdd.applyUni(bdd1.bdd, opr, bdd2.bdd), newDom);
    }

    @Override
    public BDD satOne() {
      return new TypedBDD(bdd.satOne(), makeSet(dom));
    }

    @Override
    public BDD fullSatOne() {
      return new TypedBDD(bdd.fullSatOne(), allDomains());
    }

    @Override
    public BDD satOne(BDD var, boolean pol) {
      TypedBDD bdd1 = (TypedBDD) var;
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      if (!newDom.containsAll(bdd1.dom)) {
        out.println("Warning! Selecting domain that doesn't exist: " + domainNames(bdd1.dom));
        if (STACK_TRACES) {
          new Exception().printStackTrace(out);
        }
      }
      newDom.addAll(bdd1.dom);
      return new TypedBDD(bdd.satOne(bdd1.bdd, pol), newDom);
    }

    @Override
    public AllSatIterator allsat() {
      return bdd.allsat();
    }

    @Override
    public BDD replace(BDDPairing pair) {
      TypedBDDPairing tpair = (TypedBDDPairing) pair;
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      for (Entry<BDDDomain, BDDDomain> e : tpair.domMap.entrySet()) {
        BDDDomain d_from = e.getKey();
        BDDDomain d_to = e.getValue();
        // System.out.println("Replace "+domainNames(dom)+" ("+d_from+"->"+d_to+")");
        if (!dom.contains(d_from)) {
          out.println("Warning! Replacing domain that doesn't exist: " + d_from.getName());
          new Exception().printStackTrace();
        }
        if (dom.contains(d_to) && !tpair.domMap.containsKey(d_to)) {
          out.println("Warning! Overwriting domain that exists: " + d_to.getName());
          new Exception().printStackTrace();
        }
      }
      newDom.removeAll(tpair.domMap.keySet());
      newDom.addAll(tpair.domMap.values());
      // System.out.println("Result = "+domainNames(newDom));
      return new TypedBDD(bdd.replace(tpair.pairing), newDom);
    }

    @Override
    public BDD replaceWith(BDDPairing pair) {
      TypedBDDPairing tpair = (TypedBDDPairing) pair;
      for (Entry<BDDDomain, BDDDomain> e : tpair.domMap.entrySet()) {
        BDDDomain d_from = e.getKey();
        BDDDomain d_to = e.getValue();
        if (!dom.contains(d_from)) {
          out.println("Warning! Replacing domain that doesn't exist: " + d_from.getName());
          new Exception().printStackTrace();
        }
        if (dom.contains(d_to) && !tpair.domMap.containsKey(d_to)) {
          out.println("Warning! Overwriting domain that exists: " + d_to.getName());
          new Exception().printStackTrace();
        }
      }
      dom.removeAll(tpair.domMap.keySet());
      dom.addAll(tpair.domMap.values());
      bdd.replaceWith(tpair.pairing);
      return this;
    }

    @Override
    public int nodeCount() {
      return bdd.nodeCount();
    }

    @Override
    public double pathCount() {
      return bdd.pathCount();
    }

    @Override
    public double satCount() {
      return bdd.satCount();
    }

    @Override
    public double satCount(BDD set) {
      TypedBDD bdd1 = (TypedBDD) set;
      if (!bdd.isZero() && !bdd1.dom.equals(dom)) {
        out.println(
            "Warning! satCount on the wrong domains: "
                + domainNames(dom)
                + " != "
                + domainNames(bdd1.dom));
        new Exception().printStackTrace();
      }
      return bdd.satCount(bdd1.bdd);
    }

    @Override
    public int[] varProfile() {
      return bdd.varProfile();
    }

    @Override
    public boolean equals(BDD that) {
      TypedBDD bdd1 = (TypedBDD) that;
      if (!dom.containsAll(bdd1.dom)) {
        out.println("Warning! Comparing domain that doesn't exist: " + domainNames(bdd1.dom));
      }
      return bdd.equals(bdd1.bdd);
    }

    @Override
    public int hashCode() {
      return bdd.hashCode();
    }

    @Override
    public BDDIterator iterator(BDD var) {
      TypedBDD bdd1 = (TypedBDD) var;
      if (!dom.equals(bdd1.dom)) {
        out.println(
            "Warning! iterator on the wrong domain(s): "
                + domainNames(dom)
                + " != "
                + domainNames(bdd1.dom));
      }
      return super.iterator(var);
    }

    public BDDIterator iterator() {
      Set<BDDDomain> newDom = makeSet();
      newDom.addAll(dom);
      return super.iterator(new TypedBDD(getDomains(), newDom));
    }

    @Override
    public void free() {
      bdd.free();
      dom.clear();
    }
  }

  private class TypedBDDDomain extends BDDDomain {

    BDDDomain domain;

    protected TypedBDDDomain(BDDDomain domain, int index, BigInteger range) {
      super(index, range);
      this.domain = domain;
    }

    @Override
    public BDDFactory getFactory() {
      return TypedBDDFactory.this;
    }

    @Override
    public BDD ithVar(long val) {
      BDD v = domain.ithVar(val);
      Set<BDDDomain> s = makeSet();
      s.add(this);
      return new TypedBDD(v, s);
    }

    @Override
    public BDD domain() {
      BDD v = domain.domain();
      Set<BDDDomain> s = makeSet();
      s.add(this);
      return new TypedBDD(v, s);
    }

    @Override
    public BDD buildAdd(BDDDomain that, int bits, long value) {
      TypedBDDDomain d = (TypedBDDDomain) that;
      BDD v = domain.buildAdd(d.domain, bits, value);
      Set<BDDDomain> s = makeSet();
      s.add(this);
      s.add(that);
      return new TypedBDD(v, s);
    }

    @Override
    public BDD buildEquals(BDDDomain that) {
      TypedBDDDomain d = (TypedBDDDomain) that;
      BDD v = domain.buildEquals(d.domain);
      Set<BDDDomain> s = makeSet();
      s.add(this);
      s.add(that);
      return new TypedBDD(v, s);
    }

    @Override
    public BDD set() {
      BDD v = domain.set();
      Set<BDDDomain> s = makeSet();
      s.add(this);
      return new TypedBDD(v, s);
    }

    @Override
    public BDD varRange(BigInteger lo, BigInteger hi) {
      BDD v = domain.varRange(lo, hi);
      Set<BDDDomain> s = makeSet();
      s.add(this);
      return new TypedBDD(v, s);
    }
  }

  private static class TypedBDDPairing extends BDDPairing {

    final Map<BDDDomain, BDDDomain> domMap;
    final BDDPairing pairing;

    TypedBDDPairing(BDDPairing pairing) {
      this.domMap = makeMap();
      this.pairing = pairing;
    }

    @Override
    public void set(BDDDomain p1, BDDDomain p2) {
      if (domMap.containsValue(p2)) {
        out.println("Warning! Set domain that already exists: " + p2.getName());
      }
      domMap.put(p1, p2);
      pairing.set(p1, p2);
    }

    @Override
    public void set(int oldvar, int newvar) {
      pairing.set(oldvar, newvar);
      // throw new BDDException();
    }

    @Override
    public void set(int oldvar, BDD newvar) {
      throw new BDDException();
    }

    @Override
    public void reset() {
      domMap.clear();
      pairing.reset();
    }
  }

  public static final String REVISION = "$Revision: 1.8 $";

  @Override
  public String getVersion() {
    return "TypedBDD "
        + REVISION.substring(11, REVISION.length() - 2)
        + " with "
        + factory.getVersion();
  }
}
