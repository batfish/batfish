package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDMatchers.isOne;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

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
    ImmutableBDDInteger var = ImmutableBDDInteger.makeFromIndex(factory, 5, 0);
    _exception.expect(IllegalArgumentException.class);
    _exception.expectMessage("value 32 is out of range [0, 31]");
    var.geq(32);
  }

  @Test
  public void testLeqOutOfRange() {
    BDDFactory factory = BDDUtils.bddFactory(5);
    ImmutableBDDInteger var = ImmutableBDDInteger.makeFromIndex(factory, 5, 0);
    _exception.expect(IllegalArgumentException.class);
    _exception.expectMessage("value 32 is out of range [0, 31]");
    var.leq(32);
  }

  @Test
  public void testValueOutOfRange() {
    BDDFactory factory = BDDUtils.bddFactory(5);
    ImmutableBDDInteger var = ImmutableBDDInteger.makeFromIndex(factory, 5, 0);
    _exception.expect(IllegalArgumentException.class);
    _exception.expectMessage("value 32 is out of range [0, 31]");
    var.value(32);
  }

  @Test
  public void testBoundary() {
    BDDFactory factory = BDDUtils.bddFactory(63);
    ImmutableBDDInteger var = ImmutableBDDInteger.makeFromIndex(factory, 63, 0);
    assertThat(
        var.getValueSatisfying(var.value(Long.MAX_VALUE)), equalTo(Optional.of(Long.MAX_VALUE)));
    assertThat(
        var.getValueSatisfying(var.geq(Long.MAX_VALUE)), equalTo(Optional.of(Long.MAX_VALUE)));
    assertThat(var.leq(Long.MAX_VALUE), isOne());
  }

  @Test
  public void testRange() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    ImmutableBDDInteger x = ImmutableBDDInteger.makeFromIndex(factory, 5, 0);
    for (int a = 0; a < 32; ++a) {
      for (int b = a; b < 32; ++b) {
        BDD range = x.range(a, b);
        BDD rangeEquiv = x.geq(a).and(x.leq(b));
        assertThat(range, equalTo(rangeEquiv));
      }
    }
  }

  @Test
  public void testSymbolicComparisons() {
    // Two independent 4-bit integers over disjoint variables; exhaustively check every (a, b).
    BDDFactory factory = BDDUtils.bddFactory(8);
    ImmutableBDDInteger x = ImmutableBDDInteger.makeFromIndex(factory, 4, 0);
    ImmutableBDDInteger y = ImmutableBDDInteger.makeFromIndex(factory, 4, 4);
    for (int a = 0; a < 16; a++) {
      for (int b = 0; b < 16; b++) {
        // Restricting every variable to this concrete assignment collapses the comparison to a
        // terminal: it is one iff the relation holds.
        BDD assignment = x.value(a).and(y.value(b));
        assertThat("x=" + a + " lt y=" + b, x.lt(y).restrict(assignment).isOne(), equalTo(a < b));
        assertThat(
            "x=" + a + " leq y=" + b, x.leq(y).restrict(assignment).isOne(), equalTo(a <= b));
        assertThat("x=" + a + " gt y=" + b, x.gt(y).restrict(assignment).isOne(), equalTo(a > b));
        assertThat(
            "x=" + a + " geq y=" + b, x.geq(y).restrict(assignment).isOne(), equalTo(a >= b));
        assertThat("x=" + a + " eq y=" + b, x.eq(y).restrict(assignment).isOne(), equalTo(a == b));
      }
    }
  }

  @Test
  public void testFirstBitsEqual() {
    // Two independent 4-bit integers; firstBitsEqual(n) holds iff the top n (most-significant) bits
    // match. Exhaustive over every (a, b) and every n in [0, 4].
    BDDFactory factory = BDDUtils.bddFactory(8);
    ImmutableBDDInteger x = ImmutableBDDInteger.makeFromIndex(factory, 4, 0);
    ImmutableBDDInteger y = ImmutableBDDInteger.makeFromIndex(factory, 4, 4);
    for (int a = 0; a < 16; a++) {
      for (int b = 0; b < 16; b++) {
        BDD assignment = x.value(a).and(y.value(b));
        for (int n = 0; n <= 4; n++) {
          // The top n of 4 bits are the value shifted right by (4 - n).
          boolean topNMatch = (a >> (4 - n)) == (b >> (4 - n));
          assertThat(
              "x=" + a + " firstBits" + n + " y=" + b,
              x.firstBitsEqual(y, n).restrict(assignment).isOne(),
              equalTo(topNMatch));
        }
      }
    }
    // n == width reduces to eq; n == 0 is unconstrained.
    assertThat(x.firstBitsEqual(y, 4), equalTo(x.eq(y)));
    assertThat(x.firstBitsEqual(y, 0).isOne(), equalTo(true));
  }

  @Test
  public void testSymbolicComparisonsAgainstConstantForm() {
    // For a fixed constant c, comparing the symbolic var against a constant-valued BDDInteger must
    // agree with the existing constant-comparison methods.
    BDDFactory factory = BDDUtils.bddFactory(10);
    ImmutableBDDInteger x = ImmutableBDDInteger.makeFromIndex(factory, 5, 0);
    ImmutableBDDInteger y = ImmutableBDDInteger.makeFromIndex(factory, 5, 5);
    for (int c = 0; c < 32; c++) {
      BDD yIsC = y.value(c);
      assertThat(x.lt(y).and(yIsC), equalTo(x.leq(c).and(x.value(c).not()).and(yIsC)));
      assertThat(x.leq(y).and(yIsC), equalTo(x.leq(c).and(yIsC)));
      assertThat(x.geq(y).and(yIsC), equalTo(x.geq(c).and(yIsC)));
      assertThat(x.eq(y).and(yIsC), equalTo(x.value(c).and(yIsC)));
    }
  }

  @Test
  public void testSymbolicComparisonsMutable() {
    // Same exhaustive check as testSymbolicComparisons, but over MutableBDDIntegers whose bits are
    // compound formulas (sums) rather than plain variables, to exercise the general case.
    BDDFactory factory = BDDUtils.bddFactory(8);
    MutableBDDInteger p = MutableBDDInteger.makeFromIndex(factory, 4, 0, false);
    MutableBDDInteger q = MutableBDDInteger.makeFromIndex(factory, 4, 4, false);
    MutableBDDInteger one = MutableBDDInteger.makeFromValue(factory, 4, 1);
    // x = p + 1 (mod 16) and y = q + 1 (mod 16); each bit is an adder formula, not a variable.
    MutableBDDInteger x = p.add(one);
    MutableBDDInteger y = q.add(one);
    for (int a = 0; a < 16; a++) {
      for (int b = 0; b < 16; b++) {
        // Restrict the underlying variables p=a, q=b; x and y then take the values (a+1) and (b+1).
        BDD assignment = p.value(a).and(q.value(b));
        int xv = (a + 1) % 16;
        int yv = (b + 1) % 16;
        assertThat(
            "x=" + xv + " lt y=" + yv, x.lt(y).restrict(assignment).isOne(), equalTo(xv < yv));
        assertThat(
            "x=" + xv + " leq y=" + yv, x.leq(y).restrict(assignment).isOne(), equalTo(xv <= yv));
        assertThat(
            "x=" + xv + " gt y=" + yv, x.gt(y).restrict(assignment).isOne(), equalTo(xv > yv));
        assertThat(
            "x=" + xv + " geq y=" + yv, x.geq(y).restrict(assignment).isOne(), equalTo(xv >= yv));
        assertThat(
            "x=" + xv + " eq y=" + yv, x.eq(y).restrict(assignment).isOne(), equalTo(xv == yv));
      }
    }
  }

  @Test
  public void testRangeBounds() {
    {
      BDDFactory factory = BDDUtils.bddFactory(32);
      ImmutableBDDInteger x = ImmutableBDDInteger.makeFromIndex(factory, 32, 0);
      assertThat(
          x.range(Prefix.ZERO.getStartIp().asLong(), Prefix.ZERO.getEndIp().asLong()), isOne());
    }

    {
      BDDFactory factory = BDDUtils.bddFactory(63);
      ImmutableBDDInteger x = ImmutableBDDInteger.makeFromIndex(factory, 63, 0);
      assertThat(x.range(0L, 0x7FFF_FFFF_FFFF_FFFFL), isOne());
    }
  }
}
