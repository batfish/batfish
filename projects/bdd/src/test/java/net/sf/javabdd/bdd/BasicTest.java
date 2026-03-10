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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDException;
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

  @Test
  public void testIsVar() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    assertFalse(_factory.zero().isVar());
    assertFalse(_factory.one().isVar());
    BDD x = _factory.ithVar(1);
    BDD y = _factory.ithVar(2);
    assertTrue(x.isVar());
    assertTrue(y.isVar());
    assertFalse(x.and(y).isVar());
    assertFalse(x.or(y).isVar());
    assertFalse(x.diff(y).isVar());
    assertFalse(x.not().isVar());
  }

  @Test
  public void testIsAnd() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    assertFalse(_factory.zero().isAnd());
    assertTrue(_factory.one().isAnd());
    BDD x = _factory.ithVar(1);
    BDD y = _factory.ithVar(2);
    assertTrue(x.isAnd());
    assertTrue(y.isAnd());
    assertTrue(x.and(y).isAnd());
    assertFalse(x.or(y).isAnd());
    assertFalse(x.diff(y).isAnd());
    assertFalse(x.not().isAnd());
  }

  @Test
  public void testIsNor() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    assertFalse(_factory.zero().isNor());
    assertTrue(_factory.one().isNor());
    BDD x = _factory.ithVar(1);
    BDD y = _factory.ithVar(2);
    assertFalse(x.isNor());
    assertFalse(y.isNor());
    assertFalse(x.and(y).isNor());
    assertFalse(x.or(y).isNor());
    assertFalse(x.diff(y).isNor());
    assertTrue(x.not().isNor());
    assertTrue(x.not().diff(y).isNor());
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
  public void testVar() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    _factory.setVarOrder(new int[] {0, 1, 2, 3, 4});
    BDD a = _factory.ithVar(1);
    BDD b = _factory.ithVar(2);
    BDD c = _factory.ithVar(3);
    BDD d = _factory.one();
    BDD e = _factory.zero();
    assertEquals(1, a.var());
    assertEquals(2, b.var());
    assertEquals(3, c.var());
    try {
      d.var();
      fail();
    } catch (BDDException x) {
    }
    try {
      e.var();
      fail();
    } catch (BDDException x) {
    }
    BDD f = a.and(b);
    assertEquals(1, f.var());
    a.free();
    b.free();
    c.free();
    d.free();
    e.free();
    f.free();
  }

  @Test
  public void testVarOrder() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    _factory.setVarOrder(new int[] {0, 1, 2, 3, 4});
    BDD a = _factory.ithVar(0);
    BDD b = _factory.ithVar(1);
    BDD c = _factory.ithVar(2);
    BDD d = _factory.ithVar(3);
    BDD e = _factory.ithVar(4);
    assertEquals(0, a.var());
    assertEquals(1, b.var());
    assertEquals(2, c.var());
    assertEquals(3, d.var());
    assertEquals(4, e.var());
    _factory.setVarOrder(new int[] {2, 3, 4, 0, 1});
    assertEquals(0, a.var());
    assertEquals(1, b.var());
    assertEquals(2, c.var());
    assertEquals(3, d.var());
    assertEquals(4, e.var());
    assertEquals(3, a.level());
    assertEquals(4, b.level());
    assertEquals(0, c.level());
    assertEquals(1, d.level());
    assertEquals(2, e.level());
    a.free();
    b.free();
    c.free();
    d.free();
    e.free();
  }

  @Test
  public void testLowHigh() {
    if (_factory.varNum() < 5) {
      _factory.setVarNum(5);
    }
    _factory.setVarOrder(new int[] {0, 1, 2, 3, 4});
    BDD a, b, c;
    a = _factory.ithVar(0);
    a.andWith(_factory.ithVar(1));
    a.andWith(_factory.nithVar(2));
    assertEquals(0, a.var());
    b = a.low();
    assertTrue(b.isZero());
    b.free();
    b = a.high();
    assertEquals(1, b.var());
    c = b.high();
    b.free();
    assertEquals(2, c.var());
    b = c.low();
    assertTrue(b.isOne());
    a.free();
    b.free();
    c.free();
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
    p1.reset();
    p2.reset();
    p3.reset();
  }

  @Test
  public void testCompose() {
    if (_factory.varNum() < 4) {
      _factory.setVarNum(4);
    }
    BDD a = _factory.ithVar(0);
    BDD b = _factory.ithVar(1);
    BDD c = _factory.ithVar(2);
    BDD d = _factory.ithVar(3);

    BDD xorCD = c.xor(d);

    // b doesn't occur in a
    BDD res = a.compose(xorCD, b.var());
    assert res.equals(a);

    res = b.compose(xorCD, b.var());
    assert res.equals(xorCD);

    res = b.not().compose(xorCD, b.var());
    assert res.equals(xorCD.not());

    res = a.and(b).compose(xorCD, b.var());
    assert res.equals(a.and(xorCD));

    res = a.diff(b).compose(xorCD, b.var());
    assert res.equals(a.diff(xorCD));
  }

  @Test
  public void testVeccompose() {
    if (_factory.varNum() < 4) {
      _factory.setVarNum(4);
    }

    BDD a = _factory.ithVar(0);
    BDD b = _factory.ithVar(1);
    BDD c = _factory.ithVar(2);
    BDD d = _factory.ithVar(3);

    BDD xorAB = a.xor(b);
    BDD xorCD = c.xor(d);

    BDDPairing pairing = _factory.makePair();

    // create the trivial pairing that maps variable a to itself and variable b to itself
    pairing.set(new BDD[] {a, b}, new BDD[] {a, b});
    // applying that pairing to a leaves it unchanged since it maps to itself
    BDD res = a.veccompose(pairing);
    assert res.equals(a);

    pairing.reset();
    // map variable b to (a xor b) and variable c to (c xor d)
    pairing.set(new BDD[] {b, c}, new BDD[] {xorAB, xorCD});
    // applying that pairing to a leaves it unchanged since it's not part of the pairing's domain
    res = a.veccompose(pairing);
    assert res.equals(a);

    // but applying that pairing to (not b) produces (not (a xor b))
    res = b.not().veccompose(pairing);
    assert res.equals(xorAB.not());

    pairing.reset();
    // map variable a to (a xor b) and variable b to (not (a xor b))
    pairing.set(new BDD[] {a, b}, new BDD[] {xorAB, xorAB.not()});
    // applying that pairing to (a and b) produces ((a xor b) and (not (a xor b))),
    // which simplifies to false
    res = a.and(b).veccompose(pairing);
    assert res.equals(_factory.zero());

    // applying that pairing to (a or b) produces ((a xor b) or (not (a xor b))),
    // which simplifies to true
    res = a.or(b).veccompose(pairing);
    assert res.equals(_factory.one());

    // applying that pairing to (a or (not b)) produces (after simplification) (a xor b)
    res = a.or(b.not()).veccompose(pairing);
    assert res.equals(xorAB);

    pairing.reset();
    // test simultaneous substitution of b for a and a for b
    pairing.set(new BDD[] {a, b}, new BDD[] {b, a});
    res = a.or(b.not()).veccompose(pairing);
    assert res.equals(b.or(a.not()));

    pairing.reset();
  }

  void tEnsureCapacity() {
    long[] domains = new long[] {127, 17, 31, 4};
    BDDDomain[] d = _factory.extDomain(domains);
    BDD q = d[0].ithVar(7);
    BDD r = d[1].ithVar(9);
    BDD s = d[2].ithVar(4);
    BDD t = d[3].ithVar(2);
    BDD u = r.and(s);
    BDD v = q.and(t);
    BDD w = u.and(t);
    // BDD x = d[1].set();
    for (BDDDomain bddDomain : d) {
      bddDomain.ensureCapacity(BigInteger.valueOf(150));
      assertEquals(BigInteger.valueOf(7), q.scanVar(d[0]));
      assertEquals(BigInteger.valueOf(9), r.scanVar(d[1]));
      assertEquals(BigInteger.valueOf(4), s.scanVar(d[2]));
      assertEquals(BigInteger.valueOf(2), t.scanVar(d[3]));
      assertEquals(BigInteger.valueOf(9), u.scanVar(d[1]));
      assertEquals(BigInteger.valueOf(4), u.scanVar(d[2]));
      assertEquals(BigInteger.valueOf(7), v.scanVar(d[0]));
      assertEquals(BigInteger.valueOf(2), v.scanVar(d[3]));
      assertEquals(BigInteger.valueOf(9), w.scanVar(d[1]));
      assertEquals(BigInteger.valueOf(4), w.scanVar(d[2]));
      assertEquals(BigInteger.valueOf(2), w.scanVar(d[3]));
      // BDD y = d[1].set();
      // assertEquals(x, y);
      // y.free();
    }
    // x.free();
    w.free();
    v.free();
    u.free();
    t.free();
    s.free();
    r.free();
    q.free();
  }

  void tEnsureCapacity2() {
    System.out.println("Factory " + _factory);
    long[] domainSizes = new long[] {127, 17, 31, 4, 256, 87, 42, 666, 3405, 18};
    while (_factory.numberOfDomains() < domainSizes.length) {
      _factory.extDomain(domainSizes[_factory.numberOfDomains()]);
    }
    BDDDomain[] d = new BDDDomain[domainSizes.length];
    for (int i = 0; i < domainSizes.length; ++i) {
      d[i] = _factory.getDomain(i);
      domainSizes[i] = d[i].size().longValue();
    }
    for (int i = 0; i < d.length; ++i) {
      d[i].setName(Integer.toString(i));
    }
    final int count = 100;
    final int num = 10;
    for (int i = 0; i < count; ++i) {
      String order = randomOrder(d);
      // System.out.println("Random order: "+order);
      _factory.setVarOrder(_factory.makeVarOrdering(false, order));
      List<BDD> bdds = new LinkedList<>();
      for (int j = 0; j < num; ++j) {
        BDD b = randomBDD(_factory);
        bdds.add(b);
      }
      StringBuffer sb = new StringBuffer();
      for (Object bdd3 : bdds) {
        BDD b = (BDD) bdd3;
        sb.append(b.toStringWithDomains());
        // bdd.save(new BufferedWriter(new PrintWriter(System.out)), b);
      }
      String before = sb.toString();
      int which = random.nextInt(d.length);
      int amount = random.nextInt(d[which].size().intValue() * 3);
      // System.out.println(" Ensure capacity "+d[which]+" = "+amount);
      d[which].ensureCapacity(amount);
      sb = new StringBuffer();
      for (Object bdd2 : bdds) {
        BDD b = (BDD) bdd2;
        sb.append(b.toStringWithDomains());
        // bdd.save(new BufferedWriter(new PrintWriter(System.out)), b);
      }
      String after = sb.toString();
      assertEquals(before, after);
      for (Object bdd1 : bdds) {
        BDD b = (BDD) bdd1;
        b.free();
      }
    }
  }

  private static BDD randomBDD(BDDFactory f) {
    assertTrue(f.numberOfDomains() > 0);
    List<BDDDomain> list = new ArrayList<>(f.numberOfDomains());
    int k = random.nextInt(f.numberOfDomains());
    for (int i = 0; i < f.numberOfDomains(); ++i) {
      list.add(f.getDomain(i));
    }
    BDD result = f.one();
    for (int i = 0; i < k; ++i) {
      int x = random.nextInt(f.numberOfDomains() - i);
      BDDDomain d = list.remove(x);
      int y = random.nextInt(d.size().intValue());
      result.andWith(d.ithVar(y));
    }
    if (k == 0 && random.nextBoolean()) {
      result.andWith(f.zero());
    }
    return result;
  }

  private static String randomOrder(BDDDomain[] domains) {
    domains = (BDDDomain[]) randomShuffle(domains);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < domains.length; ++i) {
      if (i > 0) {
        boolean x = random.nextBoolean();
        if (x) {
          sb.append('x');
        } else {
          sb.append('_');
        }
      }
      sb.append(domains[i].toString());
    }
    return sb.toString();
  }

  private static Random random = new Random(System.currentTimeMillis());

  private static Object[] randomShuffle(Object[] a) {
    int n = a.length;
    List<Object> list = new ArrayList<>(Arrays.asList(a));
    Object[] result = a.clone();
    for (int i = 0; i < n; ++i) {
      int k = random.nextInt(n - i);
      result[i] = list.remove(k);
    }
    assertTrue(list.isEmpty());
    return result;
  }
}
