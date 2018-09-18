package org.batfish.datamodel.acl.explanation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcIp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBDD;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.junit.Before;
import org.junit.Test;

public class ConjunctsBuilderTest {

  private ConjunctsBuilder _andBuilder;
  private static final AclLineMatchExpr DST_IP = matchDst(new Ip("1.2.3.4"));
  private static final AclLineMatchExpr DST_PREFIX = matchDst(Prefix.parse("1.2.3.0/24"));
  private static final AclLineMatchExpr SRC_IP = matchSrcIp("2.3.4.5");

  @Before
  public void setup() {
    BDDPacket pkt = new BDDPacket();
    IpAccessListToBDD toBDD =
        new IpAccessListToBDD(
            pkt,
            BDDSourceManager.forInterfaces(pkt, ImmutableSet.of()),
            ImmutableMap.of(),
            ImmutableMap.of());
    _andBuilder = new ConjunctsBuilder(toBDD);
  }

  @Test
  public void testEmpty() {
    assertThat(_andBuilder.build(), equalTo(TRUE));
  }

  @Test
  public void testOne() {
    _andBuilder.add(DST_IP);
    assertThat(_andBuilder.build(), equalTo(DST_IP));
  }

  @Test
  public void testRedundantFirst() {
    // discard redundant conjunct added first
    _andBuilder.add(DST_PREFIX);
    _andBuilder.add(DST_IP);
    assertThat(_andBuilder.build(), equalTo(DST_IP));
  }

  @Test
  public void testRedundantSecond() {
    // discard redundant conjunct added second
    _andBuilder.add(DST_IP);
    _andBuilder.add(DST_PREFIX);
    assertThat(_andBuilder.build(), equalTo(DST_IP));
  }

  @Test
  public void testRelevant() {
    _andBuilder.add(DST_IP);
    _andBuilder.add(SRC_IP);
    assertThat(_andBuilder.build(), equalTo(and(DST_IP, SRC_IP)));
  }

  @Test
  public void testShort() {
    // short-circuit
    _andBuilder.add(SRC_IP);
    _andBuilder.add(matchSrcIp("1.1.1.1"));
    assertThat(_andBuilder.build(), equalTo(FALSE));
  }

  @Test
  public void testExpandAnd() {
    _andBuilder.add(and(DST_IP, SRC_IP));
    _andBuilder.add(DST_PREFIX);
    assertThat(_andBuilder.build(), equalTo(and(SRC_IP, DST_IP)));
  }

  @Test
  public void testDeMorgan() {
    _andBuilder.add(not(or(not(DST_IP), SRC_IP)));
    _andBuilder.add(DST_PREFIX);
    assertThat(_andBuilder.build(), equalTo(and(not(SRC_IP), DST_IP)));
  }
}
