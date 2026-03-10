package org.batfish.common.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import java.util.BitSet;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class ImmutableBDDIntegerTest {

  @Test
  public void testGetVars_emptyVar() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    ImmutableBDDInteger x = ImmutableBDDInteger.makeFromIndex(factory, 0, 0);
    assertEquals(factory.one(), x.getVars());
  }

  @Test
  public void testIpToBDD() {
    BDDFactory factory = BDDUtils.bddFactory(32);
    ImmutableBDDInteger x = ImmutableBDDInteger.makeFromIndex(factory, 32, 0);
    Ip ip = Ip.parse("1.2.3.4");
    BDD bdd = x.toBDD(ip);
    assertThat(x.getValuesSatisfying(bdd, 100), contains(ip.asLong()));
  }

  @Test
  public void testPrefixToBDD() {
    BDDFactory factory = BDDUtils.bddFactory(32);
    ImmutableBDDInteger x = ImmutableBDDInteger.makeFromIndex(factory, 32, 0);
    Prefix p = Prefix.parse("1.2.3.64/30");
    BDD bdd = x.toBDD(p);
    assertThat(
        x.getValuesSatisfying(bdd, 100),
        contains(
            Ip.parse("1.2.3.64").asLong(),
            Ip.parse("1.2.3.65").asLong(),
            Ip.parse("1.2.3.66").asLong(),
            Ip.parse("1.2.3.67").asLong()));
  }

  @Test
  public void testSatAssignmentToInt() {
    BDDFactory factory = BDDUtils.bddFactory(64);
    ImmutableBDDInteger i = ImmutableBDDInteger.makeFromIndex(factory, 31, 7);
    // Ensure we read exactly the right bits, and all of them.
    assertThat(
        i.satAssignmentToInt(BitSet.valueOf(new long[] {0x7FFFFFFFL << 7})),
        equalTo(Integer.MAX_VALUE));
    // Ensure that zeroing out the lower order 4 bits is equivalent to having the 4 MSBs of the BDD
    // zero. (Since we use most-significant closest to top in BDDs).
    assertThat(
        i.satAssignmentToInt(BitSet.valueOf(new long[] {0x7FFFFFF0L << 7})),
        equalTo(Integer.MAX_VALUE >> 4));
    // Ensure that zeroing out the higher order 3 bits is equivalent to having the 3 LSBs of the BDD
    // zero. (Since we use most-significant closest to top in BDDs).
    assertThat(
        i.satAssignmentToInt(BitSet.valueOf(new long[] {0x0FFFFFFFL << 7})),
        equalTo(Integer.MAX_VALUE - 7));
    // Surround our bits with 1s to ensure we don't have any weird overflows.
    assertThat(
        i.satAssignmentToInt(BitSet.valueOf(new long[] {0xFFFFFFFFFFFFFFFFL})),
        equalTo(Integer.MAX_VALUE));
  }
}
