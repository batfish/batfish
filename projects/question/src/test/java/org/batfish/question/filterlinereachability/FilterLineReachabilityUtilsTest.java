package org.batfish.question.filterlinereachability;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.junit.Test;

public class FilterLineReachabilityUtilsTest {

  @Test
  public void testReferencedAcls() {
    String aclName = "acl";
    PermittedByAcl permittedByAcl = new PermittedByAcl(aclName);
    ExprAclLine.Builder lineBuilder = ExprAclLine.builder().setAction(LineAction.PERMIT);

    // PermittedByAcl
    ExprAclLine line = lineBuilder.setMatchCondition(permittedByAcl).build();
    assertThat(FilterLineReachabilityUtils.getReferencedAcls(line), contains(aclName));

    // And
    line =
        lineBuilder.setMatchCondition(new AndMatchExpr(ImmutableList.of(permittedByAcl))).build();
    assertThat(FilterLineReachabilityUtils.getReferencedAcls(line), contains(aclName));

    // Or
    line = lineBuilder.setMatchCondition(new OrMatchExpr(ImmutableList.of(permittedByAcl))).build();
    assertThat(FilterLineReachabilityUtils.getReferencedAcls(line), contains(aclName));

    // Not
    line = lineBuilder.setMatchCondition(new NotMatchExpr(permittedByAcl)).build();
    assertThat(FilterLineReachabilityUtils.getReferencedAcls(line), contains(aclName));
  }

  @Test
  public void testReferencedInterfaces() {
    Set<String> interfaces = ImmutableSet.of("iface1", "iface2");
    MatchSrcInterface matchSrcInterface = new MatchSrcInterface(interfaces);
    ExprAclLine.Builder lineBuilder = ExprAclLine.builder().setAction(LineAction.PERMIT);

    // PermittedByAcl
    ExprAclLine line = lineBuilder.setMatchCondition(matchSrcInterface).build();
    assertThat(FilterLineReachabilityUtils.getReferencedInterfaces(line), equalTo(interfaces));

    // And
    line =
        lineBuilder
            .setMatchCondition(new AndMatchExpr(ImmutableList.of(matchSrcInterface)))
            .build();
    assertThat(FilterLineReachabilityUtils.getReferencedInterfaces(line), equalTo(interfaces));

    // Or
    line =
        lineBuilder.setMatchCondition(new OrMatchExpr(ImmutableList.of(matchSrcInterface))).build();
    assertThat(FilterLineReachabilityUtils.getReferencedInterfaces(line), equalTo(interfaces));

    // Not
    line = lineBuilder.setMatchCondition(new NotMatchExpr(matchSrcInterface)).build();
    assertThat(FilterLineReachabilityUtils.getReferencedInterfaces(line), equalTo(interfaces));
  }
}
