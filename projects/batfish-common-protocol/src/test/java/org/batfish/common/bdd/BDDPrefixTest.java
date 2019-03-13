package org.batfish.common.bdd;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.junit.Test;
import org.parboiled.common.ImmutableList;

/** Test of {@link BDDPrefix} */
@ParametersAreNonnullByDefault
public final class BDDPrefixTest {
  @Test
  public void testAllocateBDDBit() {
    BDDPrefix bddPrefix = new BDDPrefix();
    int varNum = bddPrefix.getFactory().varNum();
    BDD bdd = bddPrefix.allocateBDDBit("foo");
    assertThat(bdd, notNullValue());
    assertThat(bddPrefix.getFactory().varNum(), equalTo(varNum + 1));
  }

  @Test
  public void testAllocateBDDInteger() {
    BDDPrefix bddPrefix = new BDDPrefix();
    int varNum = bddPrefix.getFactory().varNum();
    BDDInteger var = bddPrefix.allocateBDDInteger("foo", 5, false);
    assertThat(var, notNullValue());
    assertThat(bddPrefix.getFactory().varNum(), equalTo(varNum + 5));
  }

  @Test
  public void testInPrefixRange() {
    BDDPrefix bddPrefix = new BDDPrefix();
    PrefixRange range = new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24));
    BDD rangeBdd = bddPrefix.inPrefixRange(range);
    BDD notRangeBdd = rangeBdd.not();
    Ip matchingIp = Ip.parse("1.1.1.1");
    Ip nonMatchingIp = Ip.parse("2.2.2.2");
    BDD matchingPrefix8 = bddPrefix.isPrefix(Prefix.create(matchingIp, 8));
    BDD matchingPrefix24 = bddPrefix.isPrefix(Prefix.create(matchingIp, 24));
    BDD matchingPrefix32 = bddPrefix.isPrefix(Prefix.create(matchingIp, 32));
    BDD nonMatchingPrefix24 = bddPrefix.isPrefix(Prefix.create(nonMatchingIp, 24));

    assertTrue(matchingPrefix24.imp(rangeBdd).isOne());
    assertTrue(matchingPrefix32.imp(notRangeBdd).isOne()); // prefix too long
    assertTrue(matchingPrefix8.imp(notRangeBdd).isOne()); // prefix too short
    assertTrue(nonMatchingPrefix24.imp(notRangeBdd).isOne()); // prefix doesn't match
  }

  @Test
  public void testIsPrefix() {
    BDDPrefix bddPrefix = new BDDPrefix();
    Prefix p1 = Prefix.strict("192.0.2.0/24");
    Prefix p2 = Prefix.strict("192.0.2.0/32");
    Prefix p3 = Prefix.strict("192.0.3.0/24");

    assertThat(bddPrefix.isPrefix(p1), equalTo(bddPrefix.isPrefix(p1)));
    assertTrue(bddPrefix.isPrefix(p1).and(bddPrefix.isPrefix(p2)).isZero());
    assertTrue(bddPrefix.isPrefix(p1).and(bddPrefix.isPrefix(p3)).isZero());
    assertThat(bddPrefix.isPrefix(p2), equalTo(bddPrefix.isPrefix(p2)));
    assertTrue(bddPrefix.isPrefix(p2).and(bddPrefix.isPrefix(p3)).isZero());
    assertThat(bddPrefix.isPrefix(p3), equalTo(bddPrefix.isPrefix(p3)));
  }

  @Test
  public void testPermittedByRouteFilterList() {
    BDDPrefix bddPrefix = new BDDPrefix();
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

    BDD rangeBdd = bddPrefix.permittedByRouteFilterList(routeFilterList);
    BDD notRangeBdd = rangeBdd.not();
    Ip matchingIp = Ip.parse("1.1.1.1");
    Ip nonMatchingIp = Ip.parse("2.2.2.2");
    BDD matchingPrefix8 = bddPrefix.isPrefix(Prefix.create(matchingIp, 8));
    BDD matchingPrefix24 = bddPrefix.isPrefix(Prefix.create(matchingIp, 24));
    BDD matchingPrefix32 = bddPrefix.isPrefix(Prefix.create(matchingIp, 32));
    BDD nonMatchingPrefix24 = bddPrefix.isPrefix(Prefix.create(nonMatchingIp, 24));

    assertTrue(matchingPrefix24.imp(rangeBdd).isOne());
    assertTrue(matchingPrefix32.imp(notRangeBdd).isOne()); // prefix too long
    assertTrue(matchingPrefix8.imp(notRangeBdd).isOne()); // prefix too short
    assertTrue(nonMatchingPrefix24.imp(notRangeBdd).isOne()); // prefix doesn't match
  }
}
