package org.batfish.datamodel.acl.explanation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcIp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.junit.Before;
import org.junit.Test;

public class DisjunctsBuilderTest {
  private static final AclLineMatchExpr DST_IP = matchDst(Ip.parse("1.2.3.4"));
  private static final AclLineMatchExpr DST_PREFIX = matchDst(Prefix.parse("1.2.3.0/24"));
  private static final AclLineMatchExpr SRC_IP = matchSrcIp("2.3.4.5");

  private DisjunctsBuilder _orBuilder;

  @Before
  public void setup() {
    BDDPacket pkt = new BDDPacket();
    IpAccessListToBdd toBdd =
        new IpAccessListToBddImpl(
            pkt, BDDSourceManager.empty(pkt), ImmutableMap.of(), ImmutableMap.of());
    _orBuilder = new DisjunctsBuilder(toBdd);
  }

  @Test
  public void testEmpty() {
    assertThat(_orBuilder.build(), equalTo(FALSE));
  }

  @Test
  public void testOne() {
    _orBuilder.add(DST_PREFIX);
    assertThat(_orBuilder.build(), equalTo(DST_PREFIX));
  }

  @Test
  public void testRedundantFirst() {
    // discard redundant conjunct added first
    _orBuilder.add(DST_IP);
    _orBuilder.add(DST_PREFIX);
    assertThat(_orBuilder.build(), equalTo(DST_PREFIX));
  }

  @Test
  public void testRedundantSecond() {
    // discard redundant conjunct added second
    _orBuilder.add(DST_PREFIX);
    _orBuilder.add(DST_IP);
    assertThat(_orBuilder.build(), equalTo(DST_PREFIX));
  }

  @Test
  public void testRelevant() {
    _orBuilder.add(DST_PREFIX);
    _orBuilder.add(SRC_IP);
    assertThat(_orBuilder.build(), equalTo(or(DST_PREFIX, SRC_IP)));
  }

  @Test
  public void testShort() {
    // short-circuit
    _orBuilder.add(DST_PREFIX);
    _orBuilder.add(new NotMatchExpr(DST_PREFIX));
    assertThat(_orBuilder.build(), equalTo(TRUE));
  }

  @Test
  public void testExpandOr() {
    _orBuilder.add(or(DST_IP, SRC_IP));
    _orBuilder.add(DST_PREFIX);
    assertThat(_orBuilder.build(), equalTo(or(SRC_IP, DST_PREFIX)));
  }

  @Test
  public void testDeMorgan() {
    _orBuilder.add(not(and(not(DST_IP), SRC_IP)));
    _orBuilder.add(DST_PREFIX);
    assertThat(_orBuilder.build(), equalTo(or(not(SRC_IP), DST_PREFIX)));
  }
}
