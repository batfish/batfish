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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Basic tests of {@link BDDFactory} implementations. */
@RunWith(JUnit4.class)
public class BasicTest {
  BDDFactory _factory = JFactory.init(1000, 1000);

  @Test
  public void testIsAssignment() {
    BDD zero = _factory.zero();
    BDD one = _factory.one();
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD x = _factory.ithVar(1);
    BDD y = _factory.ithVar(2);
    BDD z = _factory.ithVar(3);
    assertFalse(zero.isAssignment());
    assertTrue(one.isAssignment());
    assertTrue(x.isAssignment());
    assertTrue(y.isAssignment());
    assertTrue(z.isAssignment());
    BDD a = x.or(y);
    assertFalse(a.isAssignment());
    BDD b = x.and(y);
    assertTrue(b.isAssignment());
    BDD c = b.or(z);
    assertFalse(c.isAssignment());
    // Calling satOne should always produce an assignment.
    BDD d = c.satOne();
    assertTrue(d.isAssignment());
    // Calling fullSatOne should always produce an assignment.
    BDD e = c.fullSatOne();
    assertTrue(e.isAssignment());
    a.free();
    b.free();
    c.free();
    d.free();
    e.free();
    x.free();
    y.free();
    z.free();
  }

  // Assertions that randomFullSatOne returns an assignment that is full and implies the input.
  private static void randomSatOneTestAndFree(BDD input, int seed) {
    BDD randomSatOne = input.randomFullSatOne(seed);
    assertTrue(randomSatOne.isAssignment());

    BDD fullSatOne = randomSatOne.fullSatOne();
    assertThat(randomSatOne, equalTo(fullSatOne));
    fullSatOne.free();

    BDD imp = randomSatOne.imp(input);
    assertTrue(imp.isOne());
    imp.free();

    randomSatOne.free();
    input.free();
  }

  @Test
  public void testRandomSatOne() {
    if (_factory.varNum() < 100) {
      _factory.setVarNum(100);
    }

    for (int seed = 0; seed < 100; ++seed) {
      BDD zero = _factory.zero();
      BDD res = zero.randomFullSatOne(seed);
      assertTrue(res.isZero());
      res.free();

      randomSatOneTestAndFree(_factory.one(), seed);

      BDD x = _factory.ithVar(0);
      BDD y = _factory.ithVar(24);
      BDD z = _factory.ithVar(49);
      BDD w = _factory.ithVar(99);
      randomSatOneTestAndFree(x.or(y), seed);
      randomSatOneTestAndFree(x.and(y), seed);
      randomSatOneTestAndFree(x.and(y).or(z), seed);
      randomSatOneTestAndFree(x.and(y).or(z.and(w)), seed);
      randomSatOneTestAndFree(x.and(y).or(z.and(w)).not(), seed);
      randomSatOneTestAndFree(x, seed);
      randomSatOneTestAndFree(y, seed);
      randomSatOneTestAndFree(z, seed);
      randomSatOneTestAndFree(w, seed);
    }
  }

  @Test
  public void testMinAssignmentBits() {
    _factory.setVarNum(17);
    assertTrue(_factory.zero().minAssignmentBits().isEmpty());
    assertTrue(_factory.one().minAssignmentBits().isEmpty());

    BDD x = _factory.ithVar(0);
    BDD y = _factory.ithVar(1);
    BDD z = _factory.ithVar(2);
    {
      BitSet b = x.minAssignmentBits();
      assertThat(b.cardinality(), equalTo(1));
      assertTrue(b.get(0));
    }
    {
      BitSet b = y.minAssignmentBits();
      assertThat(b.cardinality(), equalTo(1));
      assertTrue(b.get(1));
    }
    {
      // Given x or y, the assignment should be 010 as we should pick lower value for x
      BitSet b = x.or(y).minAssignmentBits();
      assertThat(b.cardinality(), equalTo(1));
      assertTrue(b.get(1));
    }
    {
      // Given x and y or z, the assignment should be 101 as we should pick lower value for y
      BitSet b = x.and(y.or(z)).minAssignmentBits();
      assertThat(b.cardinality(), equalTo(2));
      assertTrue(b.get(0));
      assertTrue(b.get(2));
    }
  }

  @Test
  public void testIsZeroOne() {
    BDD x = _factory.zero();
    BDD y = _factory.one();
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD z = _factory.ithVar(1);
    assertTrue(x.isZero());
    assertFalse(x.isOne());
    assertFalse(y.isZero());
    assertTrue(y.isOne());
    assertFalse(z.isZero());
    assertFalse(z.isOne());
    x.free();
    y.free();
    z.free();
  }

  @Test
  public void testNot() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD a, b, c;
    a = _factory.ithVar(0);
    b = a.not();
    c = _factory.nithVar(0);
    assertEquals(b, c);
    c.free();
    c = b.high();
    assertTrue(c.isZero());
    c.free();
    c = b.low();
    assertTrue(c.isOne());
    a.free();
    b.free();
    c.free();
  }

  @Test
  public void testId() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD a, b;
    a = _factory.ithVar(1);
    b = a.id();
    a.andWith(_factory.ithVar(0));
    assertThat(a, not(equalTo(b)));
    assertEquals(0, a.var());
    assertEquals(1, b.var());
    b.andWith(_factory.zero());
    assertTrue(b.isZero());
    assertFalse(a.isZero());
    a.free();
    b.free();
  }

  private static void testApply(
      BDDFactory bdd, BDDFactory.BDDOp op, boolean b1, boolean b2, boolean b3, boolean b4) {
    BDD a;
    assertEquals(b1, (a = bdd.zero().applyWith(bdd.zero(), op)).isOne());
    a.free();
    assertEquals(b2, (a = bdd.zero().applyWith(bdd.one(), op)).isOne());
    a.free();
    assertEquals(b3, (a = bdd.one().applyWith(bdd.zero(), op)).isOne());
    a.free();
    assertEquals(b4, (a = bdd.one().applyWith(bdd.one(), op)).isOne());
    a.free();
  }

  private static boolean isFreed(BDD b) {
    return b.hashCode() == -1 || b.hashCode() == 0x07ffffff;
  }

  /** A test helper that makes a BDD constrained to match the non-zero suffix of {@code i}. */
  private static BDD makePartiallyConstrainedInteger(BDDFactory f, int i) {
    int bit = 0;
    BDD ret = f.one();
    while (i > 0) {
      ret = ret.andWith((i & 1) == 1 ? f.ithVar(bit) : f.nithVar(bit));
      i >>>= 1;
      ++bit;
    }
    return ret;
  }

  private static void testApplyWith(
      BDDFactory bdd, BDDFactory.BDDOp op, boolean b1, boolean b2, boolean b3, boolean b4) {
    BDD a, b, c, d;
    a = bdd.zero();
    b = bdd.zero();
    c = a;
    d = b;
    assertFalse(isFreed(d));
    a.applyWith(b, op);
    assertEquals(b1, a.isOne());
    assertEquals(b1, c.isOne());
    assertTrue(isFreed(b));
    assertTrue(isFreed(d));
    a.free();

    a = bdd.zero();
    b = bdd.one();
    c = a;
    d = b;
    assertFalse(isFreed(d));
    a.applyWith(b, op);
    assertEquals(b2, a.isOne());
    assertEquals(b2, c.isOne());
    assertTrue(isFreed(b));
    assertTrue(isFreed(d));
    a.free();

    a = bdd.one();
    b = bdd.zero();
    c = a;
    d = b;
    assertFalse(isFreed(d));
    a.applyWith(b, op);
    assertEquals(b3, a.isOne());
    assertEquals(b3, c.isOne());
    assertTrue(isFreed(b));
    assertTrue(isFreed(d));
    a.free();

    a = bdd.one();
    b = bdd.one();
    c = a;
    d = b;
    assertFalse(isFreed(d));
    a.applyWith(b, op);
    assertEquals(b4, a.isOne());
    assertEquals(b4, c.isOne());
    assertTrue(isFreed(b));
    assertTrue(isFreed(d));
    a.free();
  }

  @Test
  public void testAndSat() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD a, b, c;
    for (int i = 0; i < 16; ++i) {
      a = makePartiallyConstrainedInteger(_factory, i);
      for (int j = 0; j < 16; ++j) {
        b = makePartiallyConstrainedInteger(_factory, j);
        c = a.and(b);
        _factory.setCacheSize(123); // clear cache between ops
        assertEquals(a.andSat(b), !c.isZero());
        b.free();
        c.free();
      }
      a.free();
    }
  }

  @Test
  public void testOr() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD a, b, c;
    a = _factory.ithVar(1);
    b = _factory.ithVar(2);
    c = _factory.nithVar(1);
    c.orWith(a);
    assertTrue(c.isOne());
    a = _factory.zero();
    a.orWith(_factory.zero());
    assertTrue(a.isZero());
    b.orWith(b);
    assertEquals(2, b.var());
    a.free();
    b.free();
    c.free();
    testApply(_factory, BDDFactory.or, false, true, true, true);
  }

  @Test
  public void testXor() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD a, b, c;
    a = _factory.ithVar(1);
    b = _factory.ithVar(2);
    c = _factory.nithVar(1);
    c.xorWith(a);
    assertTrue(c.isOne());
    a = _factory.zero();
    a.orWith(_factory.zero());
    assertTrue(a.isZero());
    b.xorWith(b);
    assertTrue(b.isZero());
    a.free();
    b.free();
    c.free();
    testApply(_factory, BDDFactory.xor, false, true, true, false);
  }

  @Test
  public void testImp() {
    // TODO: more tests
    testApply(_factory, BDDFactory.imp, true, true, false, true);
  }

  @Test
  public void testBiimp() {
    // TODO: more tests
    testApply(_factory, BDDFactory.biimp, true, false, false, true);
  }

  @Test
  public void testDiff() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD a = _factory.ithVar(1);
    BDD b = _factory.ithVar(2);
    BDD notb = _factory.nithVar(2);
    BDD diff = a.diff(b);
    BDD diffLongForm = a.and(notb);
    assertThat(diff, equalTo(diffLongForm));
    a.free();
    b.free();
    notb.free();
    diff.free();
    diffLongForm.free();
    testApply(_factory, BDDFactory.diff, false, false, true, false);
  }

  @Test
  public void testDiffSat() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD a, b, c;
    for (int i = 0; i < 16; ++i) {
      a = makePartiallyConstrainedInteger(_factory, i);
      for (int j = 0; j < 16; ++j) {
        b = makePartiallyConstrainedInteger(_factory, j);
        c = a.diff(b);
        _factory.setCacheSize(123); // clear cache between ops
        assertEquals(a.diffSat(b), !c.isZero());
        b.free();
        c.free();
      }
      a.free();
    }
  }

  @Test
  public void testLess() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD a = _factory.ithVar(1);
    BDD b = _factory.ithVar(2);
    BDD nota = _factory.nithVar(1);
    BDD less = a.less(b);
    BDD lessLongForm = nota.and(b);
    assertThat(less, equalTo(lessLongForm));
    a.free();
    b.free();
    nota.free();
    less.free();
    lessLongForm.free();
    testApply(_factory, BDDFactory.less, false, true, false, false);
  }

  @Test
  public void testLessSat() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD a, b, c;
    for (int i = 0; i < 16; ++i) {
      a = makePartiallyConstrainedInteger(_factory, i);
      for (int j = 0; j < 16; ++j) {
        b = makePartiallyConstrainedInteger(_factory, j);
        c = a.less(b);
        _factory.setCacheSize(123); // clear cache between ops
        assertEquals(a.lessSat(b), !c.isZero());
        b.free();
        c.free();
      }
      a.free();
    }
  }

  @Test
  public void testInvImp() {
    // TODO: more tests
    testApply(_factory, BDDFactory.invimp, true, false, true, true);
  }

  @Test
  public void testNand() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD a = _factory.ithVar(1);
    BDD b = _factory.ithVar(2);
    BDD nota = _factory.nithVar(1);
    BDD notb = _factory.nithVar(2);
    BDD nand = a.nand(nota);
    assertTrue(nand.isOne());
    nand.free();
    nand = a.nand(a);
    assertThat(nand, equalTo(nota));
    nand.free();
    nand = a.nand(b);
    BDD nandLongForm = nota.or(notb);
    assertThat(nand, equalTo(nandLongForm));
    a.free();
    b.free();
    nota.free();
    notb.free();
    nand.free();
    nandLongForm.free();
    testApply(_factory, BDDFactory.nand, true, true, true, false);
  }

  @Test
  public void testNor() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD a = _factory.ithVar(1);
    BDD b = _factory.ithVar(2);
    BDD nota = _factory.nithVar(1);
    BDD notb = _factory.nithVar(2);
    BDD nor = a.nor(nota);
    assertTrue(nor.isZero());
    nor.free();
    nor = b.nor(a);
    BDD norLongForm = notb.and(nota);
    assertThat(nor, equalTo(norLongForm));
    a.free();
    b.free();
    nota.free();
    notb.free();
    nor.free();
    norLongForm.free();
    testApply(_factory, BDDFactory.nor, true, false, false, false);
  }

  @Test
  public void testApplyWith() {
    // TODO: more tests
    testApplyWith(_factory, BDDFactory.and, false, false, false, true);
    testApplyWith(_factory, BDDFactory.or, false, true, true, true);
    testApplyWith(_factory, BDDFactory.xor, false, true, true, false);
    testApplyWith(_factory, BDDFactory.imp, true, true, false, true);
    testApplyWith(_factory, BDDFactory.biimp, true, false, false, true);
    testApplyWith(_factory, BDDFactory.diff, false, false, true, false);
    testApplyWith(_factory, BDDFactory.less, false, true, false, false);
    testApplyWith(_factory, BDDFactory.invimp, true, false, true, true);
    testApplyWith(_factory, BDDFactory.nand, true, true, true, false);
    testApplyWith(_factory, BDDFactory.nor, true, false, false, false);
  }

  @Test
  public void testIte() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDD a, b, c, d, e;
    a = _factory.ithVar(1);
    b = _factory.one();
    c = _factory.zero();
    d = a.ite(b, c);
    assertEquals(a, d);
    d.free();
    d = a.ite(c, b);
    e = d.not();
    assertEquals(a, e);
    d.free();
    e.free();
    e = _factory.ithVar(2);
    d = e.ite(a, a);
    assertEquals(a, d);
    // TODO: more tests.
    a.free();
    b.free();
    c.free();
    d.free();
    e.free();
  }

  @Test
  public void testReplace() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    BDDPairing p1 = _factory.makePair(0, 1);
    BDDPairing p2 = _factory.makePair();
    p2.set(1, 2);
    BDDPairing p3 = _factory.makePair();
    p3.set(new int[] {0, 1}, new int[] {1, 0});
    BDD a, b, c, d, e, f;
    a = _factory.ithVar(0);
    b = _factory.ithVar(1);
    c = _factory.ithVar(2);
    d = _factory.zero();
    e = _factory.one();
    a.replaceWith(p1);
    assertEquals(a, b);
    a.replaceWith(p2);
    assertEquals(a, c);
    if (_factory.varNum() < 25) {
      _factory.setVarNum(25);
    }
    b.andWith(_factory.nithVar(0));
    f = b.replace(p3);
    f.andWith(_factory.ithVar(0));
    assertFalse(f.isZero());
    f.andWith(_factory.ithVar(1));
    assertTrue(f.isZero());
    d.replaceWith(p3);
    assertTrue(d.isZero());
    e.replaceWith(p3);
    assertTrue(e.isOne());
    a.free();
    b.free();
    c.free();
    d.free();
    e.free();
    f.free();
  }
}
