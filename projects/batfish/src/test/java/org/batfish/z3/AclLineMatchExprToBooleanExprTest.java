package org.batfish.z3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfThenElse;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.junit.Test;

public class AclLineMatchExprToBooleanExprTest {
  private static final AclLineMatchExprToBooleanExpr aclLineMatchExprToBooleanExpr =
      new AclLineMatchExprToBooleanExpr(
          ImmutableMap.of(), ImmutableMap.of(), null, ImmutableMap.of());

  @Test
  public void testAndMatchExpr_emptyConjuncts() {
    assertThat(
        new AndMatchExpr(ImmutableList.of()).accept(aclLineMatchExprToBooleanExpr),
        equalTo(new AndExpr(ImmutableList.of())));
  }

  @Test
  public void testAndMatchExpr_twoConjuncts() {
    BooleanExpr booleanExpr =
        new AndMatchExpr(ImmutableList.of(TrueExpr.INSTANCE, FalseExpr.INSTANCE))
            .accept(aclLineMatchExprToBooleanExpr);
    assertThat(booleanExpr, instanceOf(AndExpr.class));

    AndExpr andExpr = (AndExpr) booleanExpr;
    assertThat(
        andExpr.getConjuncts(),
        containsInAnyOrder(
            org.batfish.z3.expr.TrueExpr.INSTANCE, org.batfish.z3.expr.FalseExpr.INSTANCE));
  }

  @Test
  public void testFalseExpr() {
    assertThat(
        FalseExpr.INSTANCE.accept(aclLineMatchExprToBooleanExpr),
        equalTo(org.batfish.z3.expr.FalseExpr.INSTANCE));
  }

  @Test
  public void testMatchHeaderspace_unrestricted() {
    HeaderSpace headerSpace = new HeaderSpace();
    assertThat(
        new MatchHeaderSpace(headerSpace).accept(aclLineMatchExprToBooleanExpr),
        equalTo(new HeaderSpaceMatchExpr(headerSpace, ImmutableMap.of()).getExpr()));
  }

  @Test
  public void testMatchHeaderspace() {
    HeaderSpace headerSpace =
        HeaderSpace.builder().setDstIps(ImmutableList.of(new IpWildcard("1.2.3.4"))).build();
    assertThat(
        new MatchHeaderSpace(headerSpace).accept(aclLineMatchExprToBooleanExpr),
        equalTo(new HeaderSpaceMatchExpr(headerSpace, ImmutableMap.of()).getExpr()));
  }

  @Test
  public void testNotMatchExpr() {
    assertThat(
        new NotMatchExpr(TrueExpr.INSTANCE).accept(aclLineMatchExprToBooleanExpr),
        equalTo(new NotExpr(org.batfish.z3.expr.TrueExpr.INSTANCE)));
  }

  @Test
  public void testOrMatchExpr_noDisjuncts() {
    assertThat(
        new OrMatchExpr(ImmutableList.of()).accept(aclLineMatchExprToBooleanExpr),
        equalTo(new OrExpr(ImmutableList.of())));
  }

  @Test
  public void testOrMatchExpr_twoDisjuncts() {
    BooleanExpr booleanExpr =
        new OrMatchExpr(ImmutableList.of(TrueExpr.INSTANCE, FalseExpr.INSTANCE))
            .accept(aclLineMatchExprToBooleanExpr);
    assertThat(booleanExpr, instanceOf(OrExpr.class));

    OrExpr orExpr = (OrExpr) booleanExpr;
    assertThat(
        orExpr.getDisjuncts(),
        containsInAnyOrder(
            org.batfish.z3.expr.TrueExpr.INSTANCE, org.batfish.z3.expr.FalseExpr.INSTANCE));
  }

  @Test
  public void testPermittedByAcl_noLines() {
    IpAccessList acl = IpAccessList.builder().setName("acl").build();

    AclLineMatchExprToBooleanExpr aclLineMatchExprToBooleanExpr =
        new AclLineMatchExprToBooleanExpr(
            ImmutableMap.of("acl", acl), ImmutableMap.of(), null, ImmutableMap.of());

    assertThat(
        new PermittedByAcl("acl").accept(aclLineMatchExprToBooleanExpr),
        equalTo(org.batfish.z3.expr.FalseExpr.INSTANCE));
  }

  @Test
  public void testPermittedByAcl_twoLines() {
    IpAccessListLine line1 =
        IpAccessListLine.builder()
            .setMatchCondition(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setDstIps(ImmutableList.of(new IpWildcard("1.2.3.4")))
                        .build()))
            .setAction(LineAction.DENY)
            .build();

    IpAccessListLine line2 =
        IpAccessListLine.builder()
            .setMatchCondition(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setDstIps(ImmutableList.of(new IpWildcard("1.2.3.0/24")))
                        .build()))
            .setAction(LineAction.PERMIT)
            .build();

    IpAccessList acl =
        IpAccessList.builder().setName("acl").setLines(ImmutableList.of(line1, line2)).build();

    AclLineMatchExprToBooleanExpr aclLineMatchExprToBooleanExpr =
        new AclLineMatchExprToBooleanExpr(
            ImmutableMap.of("acl", acl), ImmutableMap.of(), null, ImmutableMap.of());

    assertThat(
        new PermittedByAcl("acl").accept(aclLineMatchExprToBooleanExpr),
        equalTo(
            new IfThenElse(
                line1.getMatchCondition().accept(aclLineMatchExprToBooleanExpr),
                org.batfish.z3.expr.FalseExpr.INSTANCE,
                new IfThenElse(
                    line2.getMatchCondition().accept(aclLineMatchExprToBooleanExpr),
                    org.batfish.z3.expr.TrueExpr.INSTANCE,
                    org.batfish.z3.expr.FalseExpr.INSTANCE))));
  }

  @Test
  public void testTrueExpr() {
    assertThat(
        TrueExpr.INSTANCE.accept(aclLineMatchExprToBooleanExpr),
        equalTo(org.batfish.z3.expr.TrueExpr.INSTANCE));
  }
}
