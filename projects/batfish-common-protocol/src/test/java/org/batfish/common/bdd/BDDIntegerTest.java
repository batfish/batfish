package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDMatchers.isOne;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;
import java.util.Optional;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Prefix;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BDDIntegerTest {
  @Rule public ExpectedException _exception = ExpectedException.none();

  @Test
  public void testSatAssignmentToLong() {
    BDDInteger dstIp = new BDDPacket().getDstIp();
    long value = 12345;
    BDD bdd = dstIp.value(value);
    assertThat(dstIp.getValueSatisfying(bdd), equalTo(Optional.of(value)));

    value = 0xFFFFFFFFL;
    bdd = dstIp.value(value);
    assertThat(dstIp.getValueSatisfying(bdd), equalTo(Optional.of(value)));
  }

  @Test
  public void testGetValueSatisfying() {
    BDDInteger dstIp = new BDDPacket().getDstIp();
    BDD bdd = dstIp.geq(1).and(dstIp.leq(0));
    assertThat(dstIp.getValueSatisfying(bdd), equalTo(Optional.empty()));

    bdd = dstIp.geq(1).and(dstIp.leq(1));
    assertThat(dstIp.getValueSatisfying(bdd), equalTo(Optional.of((long) 1)));
  }

  @Test
  public void testGetValuesSatisfying() {
    BDDInteger dstIp = new BDDPacket().getDstIp();
    BDD bdd = dstIp.geq(1).and(dstIp.leq(0));
    assertThat(dstIp.getValuesSatisfying(bdd, 10), hasSize(0));

    long max = 0xFFFFFFFFL;
    long min = 0xFFFFFFFAL;
    bdd = dstIp.geq(min).and(dstIp.leq(max));
    assertThat(
        dstIp.getValuesSatisfying(bdd, 10),
        containsInAnyOrder(
            0xFFFFFFFAL, 0xFFFFFFFBL, 0xFFFFFFFCL, 0xFFFFFFFDL, 0xFFFFFFFEL, 0xFFFFFFFFL));
  }

  @Test
  public void testGeqOutOfRange() {
    BDDFactory factory = BDDUtils.bddFactory(5);
    BDDInteger var = BDDInteger.makeFromIndex(factory, 5, 0, false);
    _exception.expect(IllegalArgumentException.class);
    _exception.expectMessage("value 32 is out of range [0, 31]");
    var.geq(32);
  }

  @Test
  public void testLeqOutOfRange() {
    BDDFactory factory = BDDUtils.bddFactory(5);
    BDDInteger var = BDDInteger.makeFromIndex(factory, 5, 0, false);
    _exception.expect(IllegalArgumentException.class);
    _exception.expectMessage("value 32 is out of range [0, 31]");
    var.leq(32);
  }

  @Test
  public void testValueOutOfRange() {
    BDDFactory factory = BDDUtils.bddFactory(5);
    BDDInteger var = BDDInteger.makeFromIndex(factory, 5, 0, false);
    _exception.expect(IllegalArgumentException.class);
    _exception.expectMessage("value 32 is out of range [0, 31]");
    var.value(32);
  }

  @Test
  public void testBoundary() {
    BDDFactory factory = BDDUtils.bddFactory(63);
    BDDInteger var = BDDInteger.makeFromIndex(factory, 63, 0, false);
    assertThat(
        var.getValueSatisfying(var.value(Long.MAX_VALUE)), equalTo(Optional.of(Long.MAX_VALUE)));
    assertThat(
        var.getValueSatisfying(var.geq(Long.MAX_VALUE)), equalTo(Optional.of(Long.MAX_VALUE)));
    assertThat(var.leq(Long.MAX_VALUE), isOne());
  }

  @Test
  public void testAdd() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    BDDInteger x = BDDInteger.makeFromIndex(factory, 5, 0, false);
    assertTrue(x.hasVariablesOnly());
    BDDInteger constant1 = BDDInteger.makeFromValue(factory, 5, 1);
    assertFalse(constant1.hasVariablesOnly());
    BDDInteger xPlus1 = x.add(constant1);
    assertFalse(xPlus1.hasVariablesOnly());

    assertTrue(x.value(0).equals(xPlus1.value(1))); // x == 0 <==> x+1 == 1
    assertTrue(x.value(1).equals(xPlus1.value(2))); // x == 1 <==> x+1 == 2
    assertTrue(x.value(31).equals(xPlus1.value(0))); // x == 31 <==> x+1 == 0

    // Check that each variable's bitvec is properly used with satisfying assignment.
    assertThat(x.getValuesSatisfying(x.value(3L), 100), contains(3L));
    assertThat(xPlus1.getValuesSatisfying(xPlus1.value(3L), 100), contains(3L));
    assertThat(xPlus1.getValuesSatisfying(x.value(3L), 100), contains(4L));
    assertThat(x.getValuesSatisfying(xPlus1.value(3L), 100), contains(2L));
  }

  @Test
  public void testRange() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    BDDInteger x = BDDInteger.makeFromIndex(factory, 5, 0, false);
    for (int a = 0; a < 32; ++a) {
      for (int b = a; b < 32; ++b) {
        BDD range = x.range(a, b);
        BDD rangeEquiv = x.geq(a).and(x.leq(b));
        assertThat(range, equalTo(rangeEquiv));
      }
    }
  }

  @Test
  public void testRangeBounds() {
    {
      BDDFactory factory = BDDUtils.bddFactory(32);
      BDDInteger x = BDDInteger.makeFromIndex(factory, 32, 0, false);
      assertThat(
          x.range(Prefix.ZERO.getStartIp().asLong(), Prefix.ZERO.getEndIp().asLong()), isOne());
    }

    {
      BDDFactory factory = BDDUtils.bddFactory(63);
      BDDInteger x = BDDInteger.makeFromIndex(factory, 63, 0, false);
      assertThat(x.range(0L, 0x7FFF_FFFF_FFFF_FFFFL), isOne());
    }
  }

  @Test
  public void testGetVars_emptyVar() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    BDDInteger x = BDDInteger.makeFromIndex(factory, 0, 0, false);
    assertEquals(factory.one(), x.getVars());
  }

  @Test
  public void testHasVariablesOnly() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    // makeFromIndex
    BDDInteger x = BDDInteger.makeFromIndex(factory, 5, 0, false);
    assertTrue(x.hasVariablesOnly());
    // copy constructor of variables only
    BDDInteger y = new BDDInteger(x);
    assertTrue(y.hasVariablesOnly());

    // after setValue
    x.setValue(5);
    assertFalse(x.hasVariablesOnly());
    // copy setValue
    y = new BDDInteger(x);
    assertFalse(y.hasVariablesOnly());

    x = BDDInteger.makeFromIndex(factory, 5, 0, false);
    assertTrue(x.hasVariablesOnly());
    y = x.add(x);
    assertFalse(y.hasVariablesOnly());
    assertTrue(x.hasVariablesOnly());

    y = x.sub(x);
    assertFalse(y.hasVariablesOnly());
    assertTrue(x.hasVariablesOnly());
  }

  @Test
  public void testThrowsOnGetVars() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    BDDInteger x = BDDInteger.makeFromValue(factory, 5, 3);
    _exception.expect(IllegalStateException.class);
    x.getVars();
  }

  @Test
  public void testThrowsOnBitSet() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    BDDInteger x = BDDInteger.makeFromValue(factory, 5, 3);
    BitSet anything = factory.one().minAssignmentBits();
    _exception.expect(IllegalStateException.class);
    x.satAssignmentToLong(anything);
  }
}
