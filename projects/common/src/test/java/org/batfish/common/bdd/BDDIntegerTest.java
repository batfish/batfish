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
