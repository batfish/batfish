package org.batfish.common.bdd;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstIp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcIp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link MemoizedIpAccessListToBdd}. */
public class MemoizedIpAccessListToBddTest {
  private static final AclLineMatchExpr MATCH_DST_IP = matchDstIp("1.1.1.1");
  private static final AclLineMatchExpr MATCH_SRC_IP = matchSrcIp("2.2.2.2");

  private MemoizedIpAccessListToBdd _toBdd;

  @Before
  public void setup() {
    BDDPacket pkt = new BDDPacket();
    _toBdd =
        new MemoizedIpAccessListToBdd(
            pkt, BDDSourceManager.empty(pkt), ImmutableMap.of(), ImmutableMap.of());
  }

  @Test
  public void testVisit() {
    assertThat(_toBdd.getMemoizedBdd(MATCH_DST_IP), equalTo(Optional.empty()));
    BDD bdd = _toBdd.toBdd(MATCH_DST_IP);
    assertThat(_toBdd.getMemoizedBdd(MATCH_DST_IP), equalTo(Optional.of(bdd)));
  }

  @Test
  public void testNegate() {
    assertThat(_toBdd.getMemoizedBdd(MATCH_DST_IP), equalTo(Optional.empty()));
    BDD bdd = _toBdd.toBdd(not(MATCH_DST_IP));
    assertThat(_toBdd.getMemoizedBdd(MATCH_DST_IP), equalTo(Optional.of(bdd.not())));
  }

  @Test
  public void testAnd() {
    assertThat(_toBdd.getMemoizedBdd(MATCH_DST_IP), equalTo(Optional.empty()));
    assertThat(_toBdd.getMemoizedBdd(MATCH_SRC_IP), equalTo(Optional.empty()));
    BDD bdd = _toBdd.toBdd(and(MATCH_DST_IP, MATCH_SRC_IP));
    Optional<BDD> dstBdd = _toBdd.getMemoizedBdd(MATCH_DST_IP);
    Optional<BDD> srcBdd = _toBdd.getMemoizedBdd(MATCH_SRC_IP);
    assertTrue("MATCH_DST_IP should be memoized", dstBdd.isPresent());
    assertTrue("MATCH_SRC_IP should be memoized", srcBdd.isPresent());
    assertThat(dstBdd.get().and(srcBdd.get()), equalTo(bdd));
  }

  @Test
  public void testOr() {
    assertThat(_toBdd.getMemoizedBdd(MATCH_DST_IP), equalTo(Optional.empty()));
    assertThat(_toBdd.getMemoizedBdd(MATCH_SRC_IP), equalTo(Optional.empty()));
    BDD bdd = _toBdd.toBdd(or(MATCH_DST_IP, MATCH_SRC_IP));
    Optional<BDD> dstBdd = _toBdd.getMemoizedBdd(MATCH_DST_IP);
    Optional<BDD> srcBdd = _toBdd.getMemoizedBdd(MATCH_SRC_IP);
    assertTrue("MATCH_DST_IP should be memoized", dstBdd.isPresent());
    assertTrue("MATCH_SRC_IP should be memoized", srcBdd.isPresent());
    assertThat(dstBdd.get().or(srcBdd.get()), equalTo(bdd));
  }
}
