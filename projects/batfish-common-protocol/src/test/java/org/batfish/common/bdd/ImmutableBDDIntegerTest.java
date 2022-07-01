package org.batfish.common.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;

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
}
