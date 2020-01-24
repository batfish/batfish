package org.batfish.datamodel.acl;

import static org.batfish.datamodel.ExprAclLine.accepting;
import static org.batfish.datamodel.ExprAclLine.rejecting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.junit.Test;

/** Tests of {@link AclLineEvaluator} */
public class AclLineEvaluatorTest {

  @Test
  public void testVisitAclAclLine() {
    Ip permitIp = Ip.parse("1.1.1.1");
    Ip denyIp = Ip.parse("2.2.2.2");
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(ImmutableList.of(accepting(matchDst(permitIp)), rejecting(matchDst(denyIp))))
            .build();
    AclAclLine matchAclLine = new AclAclLine("line name", acl.getName());

    // Line should permit the explicitly permitted IP
    Flow flowToPermitIp = Flow.builder().setDstIp(permitIp).setIngressNode("c").build();
    AclLineEvaluator evaluator =
        new AclLineEvaluator(
            flowToPermitIp, "srcIface", ImmutableMap.of(acl.getName(), acl), ImmutableMap.of());
    assertEquals(evaluator.visit(matchAclLine), LineAction.PERMIT);

    // Line should deny the explicitly denied IP
    Flow flowToDenyIp = Flow.builder().setDstIp(denyIp).setIngressNode("c").build();
    evaluator =
        new AclLineEvaluator(
            flowToDenyIp, "srcIface", ImmutableMap.of(acl.getName(), acl), ImmutableMap.of());
    assertEquals(evaluator.visit(matchAclLine), LineAction.DENY);

    // Line should not act on some other IP that would be default denied by the referenced ACL
    Flow flowToOtherIp = Flow.builder().setDstIp(Ip.parse("3.3.3.3")).setIngressNode("c").build();
    evaluator =
        new AclLineEvaluator(
            flowToOtherIp, "srcIface", ImmutableMap.of(acl.getName(), acl), ImmutableMap.of());
    assertNull(evaluator.visit(matchAclLine));
  }
}
