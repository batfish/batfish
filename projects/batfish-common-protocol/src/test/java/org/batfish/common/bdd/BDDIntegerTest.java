package org.batfish.common.bdd;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
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
    _exception.expectMessage("value 32 is out of range");
    var.geq(32);
  }

  @Test
  public void testLeqOutOfRange() {
    BDDFactory factory = BDDUtils.bddFactory(5);
    BDDInteger var = BDDInteger.makeFromIndex(factory, 5, 0, false);
    _exception.expect(IllegalArgumentException.class);
    _exception.expectMessage("value 32 is out of range");
    var.leq(32);
  }

  @Test
  public void testValueOutOfRange() {
    BDDFactory factory = BDDUtils.bddFactory(5);
    BDDInteger var = BDDInteger.makeFromIndex(factory, 5, 0, false);
    _exception.expect(IllegalArgumentException.class);
    _exception.expectMessage("value 32 is out of range");
    var.value(32);
  }

  @Test
  public void testAdd() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    BDDInteger inVar = BDDInteger.makeFromIndex(factory, 5, 0, false);
    BDDInteger constant1 = BDDInteger.makeFromValue(factory, 5, 1);
    BDDInteger add1 = inVar.add(constant1);

    assertTrue(add1.value(0).equals(inVar.value(31)));
    assertTrue(add1.value(1).equals(inVar.value(0)));
    assertTrue(add1.value(2).equals(inVar.value(1)));

    // now convert to a relation representation
    BDDInteger outVar = BDDInteger.makeFromIndex(factory, 5, 5, false);
    BDD[] bv1 = add1.getBitvec();
    BDD[] bv2 = outVar.getBitvec();
    BDD add1Bdd = factory.one();
    for (int i = 0; i < bv2.length; i++) {
      add1Bdd = add1Bdd.and(bv2[i].biimp(bv1[i]));
    }

    assertThat(outVar.getValuesSatisfying(add1Bdd.and(inVar.value(1)), 5), contains(2L));
    assertThat(outVar.getValuesSatisfying(add1Bdd.and(inVar.value(10)), 5), contains(11L));
    assertThat(outVar.getValuesSatisfying(add1Bdd.and(inVar.value(31)), 5), contains(0L));

    assertThat(inVar.getValuesSatisfying(add1Bdd.and(outVar.value(0)), 5), contains(31L));
    assertThat(inVar.getValuesSatisfying(add1Bdd.and(outVar.value(10)), 5), contains(9L));
    assertThat(inVar.getValuesSatisfying(add1Bdd.and(outVar.value(31)), 5), contains(30L));
  }
}
