package org.batfish.common.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link BDDPrefix} */
@ParametersAreNonnullByDefault
public final class BDDPrefixTest {

  private BDDPrefix _bddPrefix;

  @Before
  public void setup() {
    BDDPacket packet = new BDDPacket();
    _bddPrefix = new BDDPrefix(packet.getDstIp(), packet.getDscp());
  }

  @Test
  public void testInPrefixRange() {
    PrefixRange range = PrefixRange.fromString("1.0.0.0/8:16-24");
    BDD rangeBdd = _bddPrefix.inPrefixRange(range);
    BDD notRangeBdd = rangeBdd.not();
    Ip matchingIp = Ip.parse("1.1.1.1");
    Ip nonMatchingIp = Ip.parse("2.2.2.2");
    BDD matchingPrefix8 = _bddPrefix.isPrefix(Prefix.create(matchingIp, 8));
    BDD matchingPrefix24 = _bddPrefix.isPrefix(Prefix.create(matchingIp, 24));
    BDD matchingPrefix32 = _bddPrefix.isPrefix(matchingIp.toPrefix());
    BDD nonMatchingPrefix24 = _bddPrefix.isPrefix(Prefix.create(nonMatchingIp, 24));

    assertTrue(matchingPrefix24.imp(rangeBdd).isOne());
    assertTrue(matchingPrefix32.imp(notRangeBdd).isOne()); // prefix too long
    assertTrue(matchingPrefix8.imp(notRangeBdd).isOne()); // prefix too short
    assertTrue(nonMatchingPrefix24.imp(notRangeBdd).isOne()); // prefix doesn't match
  }

  @Test
  public void testIsPrefix() {
    Prefix p1 = Prefix.strict("192.0.2.0/24");
    Prefix p2 = Prefix.strict("192.0.2.0/32");
    Prefix p3 = Prefix.strict("192.0.3.0/24");

    assertThat(_bddPrefix.isPrefix(p1), equalTo(_bddPrefix.isPrefix(p1)));
    assertFalse(_bddPrefix.isPrefix(p1).andSat(_bddPrefix.isPrefix(p2)));
    assertFalse(_bddPrefix.isPrefix(p1).andSat(_bddPrefix.isPrefix(p3)));
    assertThat(_bddPrefix.isPrefix(p2), equalTo(_bddPrefix.isPrefix(p2)));
    assertFalse(_bddPrefix.isPrefix(p2).andSat(_bddPrefix.isPrefix(p3)));
    assertThat(_bddPrefix.isPrefix(p3), equalTo(_bddPrefix.isPrefix(p3)));
  }

  @Test
  public void testPermittedByRouteFilterList() {
    RouteFilterList routeFilterList =
        new RouteFilterList(
            "list",
            ImmutableList.of(
                new RouteFilterLine(
                    LineAction.DENY, Prefix.strict("1.0.0.0/8"), new SubRange(0, 15)),
                new RouteFilterLine(
                    LineAction.DENY,
                    Prefix.strict("1.0.0.0/8"),
                    new SubRange(25, Prefix.MAX_PREFIX_LENGTH)),
                new RouteFilterLine(
                    LineAction.PERMIT,
                    Prefix.strict("1.0.0.0/8"),
                    new SubRange(0, Prefix.MAX_PREFIX_LENGTH))));

    BDD rangeBdd = _bddPrefix.permittedByRouteFilterList(routeFilterList);
    BDD notRangeBdd = rangeBdd.not();
    Ip matchingIp = Ip.parse("1.1.1.1");
    Ip nonMatchingIp = Ip.parse("2.2.2.2");
    BDD matchingPrefix8 = _bddPrefix.isPrefix(Prefix.create(matchingIp, 8));
    BDD matchingPrefix24 = _bddPrefix.isPrefix(Prefix.create(matchingIp, 24));
    BDD matchingPrefix32 = _bddPrefix.isPrefix(matchingIp.toPrefix());
    BDD nonMatchingPrefix24 = _bddPrefix.isPrefix(Prefix.create(nonMatchingIp, 24));

    assertTrue(matchingPrefix24.imp(rangeBdd).isOne());
    assertTrue(matchingPrefix32.imp(notRangeBdd).isOne()); // prefix too long
    assertTrue(matchingPrefix8.imp(notRangeBdd).isOne()); // prefix too short
    assertTrue(nonMatchingPrefix24.imp(notRangeBdd).isOne()); // prefix doesn't match
  }
}
