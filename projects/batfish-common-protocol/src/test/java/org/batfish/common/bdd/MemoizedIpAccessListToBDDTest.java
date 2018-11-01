package org.batfish.common.bdd;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstIp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcIp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

public class MemoizedIpAccessListToBDDTest {
  private static final AclLineMatchExpr MATCH_DST_IP = matchDstIp("1.1.1.1");
  private static final AclLineMatchExpr MATCH_SRC_IP = matchSrcIp("2.2.2.2");

  private MemoizedIpAccessListToBDD _toBdd;

  @Before
  public void setup() {
    BDDPacket pkt = new BDDPacket();
    _toBdd = new MemoizedIpAccessListToBDD(pkt, ImmutableMap.of(), ImmutableMap.of());
  }

  @Test
  public void testVisit() {
    MatcherAssert.assertThat(_toBdd.getMemoizedBdd(MATCH_DST_IP), equalTo(Optional.empty()));
    BDD bdd = _toBdd.visit(MATCH_DST_IP);
    MatcherAssert.assertThat(_toBdd.getMemoizedBdd(MATCH_DST_IP), equalTo(Optional.of(bdd)));
  }

  @Test
  public void testNegate() {
    MatcherAssert.assertThat(_toBdd.getMemoizedBdd(MATCH_DST_IP), equalTo(Optional.empty()));
    BDD bdd = _toBdd.visit(not(MATCH_DST_IP));
    MatcherAssert.assertThat(_toBdd.getMemoizedBdd(MATCH_DST_IP), equalTo(Optional.of(bdd.not())));
  }

  @Test
  public void testAnd() {
    MatcherAssert.assertThat(_toBdd.getMemoizedBdd(MATCH_DST_IP), equalTo(Optional.empty()));
    MatcherAssert.assertThat(_toBdd.getMemoizedBdd(MATCH_SRC_IP), equalTo(Optional.empty()));
    BDD bdd = _toBdd.visit(and(MATCH_DST_IP, MATCH_SRC_IP));
    Optional<BDD> dstBdd = _toBdd.getMemoizedBdd(MATCH_DST_IP);
    Optional<BDD> srcBdd = _toBdd.getMemoizedBdd(MATCH_SRC_IP);
    assertThat("MATCH_DST_IP should be memoized", dstBdd.isPresent());
    assertThat("MATCH_SRC_IP should be memoized", srcBdd.isPresent());
    assertThat(dstBdd.get().and(srcBdd.get()), equalTo(bdd));
  }

  @Test
  public void testOr() {
    MatcherAssert.assertThat(_toBdd.getMemoizedBdd(MATCH_DST_IP), equalTo(Optional.empty()));
    MatcherAssert.assertThat(_toBdd.getMemoizedBdd(MATCH_SRC_IP), equalTo(Optional.empty()));
    BDD bdd = _toBdd.visit(or(MATCH_DST_IP, MATCH_SRC_IP));
    Optional<BDD> dstBdd = _toBdd.getMemoizedBdd(MATCH_DST_IP);
    Optional<BDD> srcBdd = _toBdd.getMemoizedBdd(MATCH_SRC_IP);
    assertThat("MATCH_DST_IP should be memoized", dstBdd.isPresent());
    assertThat("MATCH_SRC_IP should be memoized", srcBdd.isPresent());
    assertThat(dstBdd.get().or(srcBdd.get()), equalTo(bdd));
  }
}
