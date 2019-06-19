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
package net.sf.javabdd.bdd;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;

/**
 * IteratorTests
 *
 * @author jwhaley
 * @version $Id: IteratorTests.java,v 1.6 2005/06/03 20:19:46 joewhaley Exp $
 */
@RunWith(JUnit38ClassRunner.class)
public class IteratorTests extends BDDTestCase {
  public static void main(String[] args) {
    junit.textui.TestRunner.run(IteratorTests.class);
  }

  public void testOneZeroIterator() {
    reset();
    assertTrue(hasNext());
    while (hasNext()) {
      BDDFactory bdd = next();
      int domainSize = 1024;
      BDDDomain[] ds = bdd.extDomain(new int[] {domainSize});
      BDDDomain d = ds[0];
      BDD b = bdd.zero();
      BDD var = d.set();
      Iterator<BDD> i = b.iterator(var);
      b.free();
      assertFalse(i.hasNext());
      try {
        i.next();
        fail();
      } catch (NoSuchElementException ignored) {
        /* Expected. */
      }

      b = bdd.one();
      Iterator<BDD> i1 = b.iterator(var);
      Iterator<BDD> i2 = new MyBDDIterator(b, var);
      b.free();
      Set<BDD> s1 = new HashSet<>();
      Set<BDD> s2 = new HashSet<>();
      while (i1.hasNext()) {
        BDD b1 = i1.next();
        double sc = b1.satCount(var);
        assertEquals(1., sc, 0.0000001);
        s1.add(b1);
      }
      while (i2.hasNext()) {
        BDD b2 = i2.next();
        double sc = b2.satCount(var);
        assertEquals(1., sc, 0.0000001);
        s2.add(b2);
      }
      var.free();
      assertEquals(s1.size(), domainSize);
      assertEquals(s2.size(), domainSize);
      if (!s1.equals(s2)) {
        Set<BDD> s1_minus_s2 = new HashSet<>(s1);
        s1_minus_s2.removeAll(s2);
        Set<BDD> s2_minus_s1 = new HashSet<>(s2);
        s2_minus_s1.removeAll(s1);
        fail(
            "iterator() contains these extras: "
                + s1_minus_s2
                + "\n"
                + "iterator2() contains these extras: "
                + s2_minus_s1);
      }
      for (Object o1 : s1) {
        BDD q = (BDD) o1;
        q.free();
      }
      for (Object o : s2) {
        BDD q = (BDD) o;
        q.free();
      }
    }
  }

  public void testRandomIterator() {
    reset();
    assertTrue(hasNext());
    while (hasNext()) {
      BDDFactory bdd = next();
      bdd.setNodeTableSize(200000);
      int domainSize = 1024;
      BDDDomain[] ds = bdd.extDomain(new int[] {domainSize, domainSize});
      BDDDomain d = ds[0];
      d.setName("D0");
      BDDDomain d2 = ds[1];
      d2.setName("D1");
      bdd.setVarOrder(bdd.makeVarOrdering(true, "D1xD0"));
      Random r = new Random(666);
      int times = 1000;
      int combine = 400;
      boolean dual = true;
      for (int i = 0; i < times; ++i) {
        int count = r.nextInt(combine);
        BDD b = bdd.zero();
        for (int j = 0; j < count; ++j) {
          int varNum = r.nextInt(domainSize);
          BDD c = d.ithVar(varNum);
          if (dual) {
            c.andWith(d2.ithVar(r.nextInt(domainSize)));
          }
          b.orWith(c);
        }
        BDD var = d.set();
        if (dual) {
          var.andWith(d2.set());
        }
        Iterator<BDD> i1 = b.iterator(var);
        Iterator<BDD> i2 = new MyBDDIterator(b, var);
        b.free();
        Set<BDD> s1 = new HashSet<>();
        Set<BDD> s2 = new HashSet<>();
        while (i1.hasNext()) {
          BDD b1 = i1.next();
          double sc = b1.satCount(var);
          assertEquals(1., sc, 0.0000001);
          s1.add(b1);
        }
        while (i2.hasNext()) {
          BDD b2 = i2.next();
          double sc = b2.satCount(var);
          assertEquals(1., sc, 0.0000001);
          s2.add(b2);
        }
        var.free();
        if (!s1.equals(s2)) {
          Set<BDD> s1_minus_s2 = new HashSet<>(s1);
          s1_minus_s2.removeAll(s2);
          Set<BDD> s2_minus_s1 = new HashSet<>(s2);
          s2_minus_s1.removeAll(s1);
          fail(
              "iterator() contains these extras: "
                  + s1_minus_s2
                  + "\n"
                  + "iterator2() contains these extras: "
                  + s2_minus_s1);
        }
        for (Object o1 : s1) {
          BDD q = (BDD) o1;
          q.free();
        }
        for (Object o : s2) {
          BDD q = (BDD) o;
          q.free();
        }
      }
    }
  }

  /**
   * This is another version of iterator() that exists for testing purposes. It is much slower than
   * the other one.
   */
  static class MyBDDIterator implements Iterator<BDD> {

    BDD orig;
    BDD b = null;
    BDD myVar;
    BDD last = null;

    MyBDDIterator(BDD dis, BDD var) {
      orig = dis;
      if (!dis.isZero()) {
        b = dis.id();
        myVar = var.id();
      }
    }

    @Override
    public void remove() {
      if (last != null) {
        orig.applyWith(last.id(), BDDFactory.diff);
        last = null;
      } else {
        throw new IllegalStateException();
      }
    }

    @Override
    public boolean hasNext() {
      return b != null;
    }

    @Override
    public BDD next() {
      if (b == null) {
        throw new NoSuchElementException();
      }
      BDD c = b.satOne(myVar, false);
      b.applyWith(c.id(), BDDFactory.diff);
      if (b.isZero()) {
        myVar.free();
        myVar = null;
        b.free();
        b = null;
      }
      return last = c;
    }
  }
}
