package org.batfish.datamodel;

import static org.batfish.datamodel.IpRange.range;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link IpRange} */
@ParametersAreNonnullByDefault
public final class IpRangeTest {

  private static final IpSpaceToBDD _bddConverter = new IpSpaceToBDD(new BDDPacket().getDstIp());
  private static final BDDInteger _universeIpSpaceBdd = _bddConverter.getBDDInteger();

  private static void testIpRange(Ip low, Ip high) {
    IpSpace ipRange = range(low, high);
    BDD lowAndAbove = _universeIpSpaceBdd.geq(low.asLong());
    BDD highAndBelow = _universeIpSpaceBdd.leq(high.asLong());
    assertThat(ipRange.accept(_bddConverter), equalTo(lowAndAbove.and(highAndBelow)));
  }

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testRange() {
    testIpRange(Ip.ZERO, Ip.ZERO);
    testIpRange(Ip.ZERO, Ip.MAX);
    testIpRange(Ip.MAX, Ip.MAX);
    testIpRange(Ip.parse("4.23.32.21"), Ip.parse("5.0.0.1"));
    testIpRange(Ip.parse("5.0.0.1"), Ip.parse("5.0.0.1"));
    testIpRange(Ip.parse("123.0.0.0"), Ip.parse("123.0.0.255"));
    testIpRange(Ip.parse("1.255.255.255"), Ip.parse("2.255.255.255"));
    testIpRange(Ip.parse("0.0.0.1"), Ip.parse("255.255.255.254"));
  }

  @Test
  public void testRangeInvalid() {
    _thrown.expect(IllegalArgumentException.class);
    range(Ip.MAX, Ip.ZERO);
  }
}
