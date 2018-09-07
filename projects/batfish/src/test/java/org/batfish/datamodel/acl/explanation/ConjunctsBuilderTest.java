package org.batfish.datamodel.acl.explanation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.symbolic.bdd.AclLineMatchExprToBDD;
import org.batfish.symbolic.bdd.BDDPacket;
import org.junit.Test;

public class ConjunctsBuilderTest {

  @Test
  public void testConjunctsBuilder() {
    BDDPacket pkt = new BDDPacket();
    AclLineMatchExprToBDD toBDD =
        new AclLineMatchExprToBDD(pkt.getFactory(), pkt, ImmutableMap.of(), ImmutableMap.of());
    ConjunctsBuilder andBuilder = new ConjunctsBuilder(toBDD);

    assertThat(andBuilder.build(), equalTo(TRUE));

    AclLineMatchExpr dstIp = matchDst(new Ip("1.2.3.4"));
    AclLineMatchExpr dstPrefix = matchDst(Prefix.parse("1.2.3.0/24"));
    AclLineMatchExpr srcIp = matchSrcIp("2.3.4.5");
    AclLineMatchExpr srcPrefix = matchSrc(Prefix.parse("2.3.4.0/24"));

    andBuilder.add(dstIp);
    assertThat(andBuilder.build(), equalTo(dstIp));

    // discard redundant conjunct
    andBuilder.add(dstPrefix);
    assertThat(andBuilder.build(), equalTo(dstIp));

    andBuilder.add(srcPrefix);
    assertThat(andBuilder.build(), equalTo(and(dstIp, srcPrefix)));

    // replace conjunct made redundant by the new one
    andBuilder.add(srcIp);
    assertThat(andBuilder.build(), equalTo(and(dstIp, srcIp)));

    // short-circuit
    andBuilder.add(matchSrcIp("1.1.1.1"));
    assertThat(andBuilder.build(), equalTo(FALSE));
  }
}
