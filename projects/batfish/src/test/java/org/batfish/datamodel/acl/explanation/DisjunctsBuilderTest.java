package org.batfish.datamodel.acl.explanation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcIp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.symbolic.bdd.AclLineMatchExprToBDD;
import org.batfish.symbolic.bdd.BDDPacket;
import org.junit.Test;

public class DisjunctsBuilderTest {
  @Test
  public void testDisjunctsBuilder() {
    BDDPacket pkt = new BDDPacket();
    AclLineMatchExprToBDD toBDD =
        new AclLineMatchExprToBDD(pkt.getFactory(), pkt, ImmutableMap.of(), ImmutableMap.of());
    DisjunctsBuilder orBuilder = new DisjunctsBuilder(toBDD);

    assertThat(orBuilder.build(), equalTo(FALSE));

    AclLineMatchExpr dstIp = matchDst(new Ip("1.2.3.4"));
    AclLineMatchExpr dstPrefix = matchDst(Prefix.parse("1.2.3.0/24"));
    AclLineMatchExpr srcIp = matchSrcIp("2.3.4.5");
    AclLineMatchExpr srcPrefix = matchSrc(Prefix.parse("2.3.4.0/24"));

    orBuilder.add(dstPrefix);
    assertThat(orBuilder.build(), equalTo(dstPrefix));

    // discard redundant conjunct
    orBuilder.add(dstIp);
    assertThat(orBuilder.build(), equalTo(dstPrefix));

    orBuilder.add(srcIp);
    assertThat(orBuilder.build(), equalTo(or(dstPrefix, srcIp)));

    // replace conjunct made redundant by the new one
    orBuilder.add(srcPrefix);
    assertThat(orBuilder.build(), equalTo(or(dstPrefix, srcPrefix)));

    // short-circuit
    orBuilder.add(new NotMatchExpr(dstPrefix));
    assertThat(orBuilder.build(), equalTo(TRUE));
  }
}
