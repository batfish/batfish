package org.batfish.z3.expr.visitors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfThenElse;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.TrueExpr;
import org.junit.Test;

public class IpSpaceBooleanExprTransformerTest {
  private static final IpSpaceBooleanExprTransformer SRC_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER =
      new IpSpaceBooleanExprTransformer(true, false);

  private static final IpSpaceBooleanExprTransformer DST_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER =
      new IpSpaceBooleanExprTransformer(false, true);

  private static final IpSpaceBooleanExprTransformer SRC_OR_DST_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER =
      new IpSpaceBooleanExprTransformer(true, true);

  @Test
  public void testVisitAclIpSpace() {
    AclIpSpace ipSpace =
        AclIpSpace.builder()
            .thenRejecting(UniverseIpSpace.INSTANCE)
            .thenPermitting(EmptyIpSpace.INSTANCE)
            .build();

    BooleanExpr expr = ipSpace.accept(SRC_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER);

    assertThat(
        expr,
        equalTo(
            new IfThenElse(
                // Matches UniverseIpSpace
                TrueExpr.INSTANCE,
                // Reject
                FalseExpr.INSTANCE,
                new IfThenElse(
                    // Matches EmptyIpSpace
                    FalseExpr.INSTANCE,
                    // Accept
                    TrueExpr.INSTANCE,
                    // Matches nothing so reject
                    FalseExpr.INSTANCE))));
  }

  @Test
  public void testVisitEmptyIpSpace() {
    BooleanExpr expr = EmptyIpSpace.INSTANCE.accept(SRC_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER);
    assertThat(expr, equalTo(FalseExpr.INSTANCE));
  }

  @Test
  public void testVisitIp() {
    Ip ip = new Ip("1.2.3.4");

    BooleanExpr matchSrcExpr = ip.toIpSpace().accept(SRC_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER);
    assertThat(
        matchSrcExpr,
        equalTo(HeaderSpaceMatchExpr.matchSrcIp(ImmutableSet.of(new IpWildcard(ip)))));

    BooleanExpr matchDstExpr = ip.toIpSpace().accept(DST_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER);
    assertThat(
        matchDstExpr,
        equalTo(HeaderSpaceMatchExpr.matchDstIp(ImmutableSet.of(new IpWildcard(ip)))));

    BooleanExpr matchSrcOrDstExpr =
        ip.toIpSpace().accept(SRC_OR_DST_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER);
    assertThat(
        matchSrcOrDstExpr,
        equalTo(HeaderSpaceMatchExpr.matchSrcOrDstIp(ImmutableSet.of(new IpWildcard(ip)))));
  }

  @Test
  public void testVisitIpWildcard() {
    IpWildcard wildcard = new IpWildcard(new Ip("1.2.0.4"), new Ip(0x0000FF00L));
    BooleanExpr matchExpr = wildcard.toIpSpace().accept(SRC_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER);
    assertThat(matchExpr, equalTo(HeaderSpaceMatchExpr.matchSrcIp(ImmutableSet.of(wildcard))));
  }

  @Test
  public void testVisitIpWildcardSetIpSpace() {
    IpWildcard includeWildcard = new IpWildcard("1.1.1.1");
    IpWildcard excludeWildcard = new IpWildcard("2.2.2.2");
    IpWildcardSetIpSpace ipSpace =
        IpWildcardSetIpSpace.builder()
            .including(includeWildcard)
            .excluding(excludeWildcard)
            .build();

    BooleanExpr expr = ipSpace.accept(SRC_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER);
    BooleanExpr includeExpr =
        includeWildcard.toIpSpace().accept(SRC_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER);
    BooleanExpr excludeExpr =
        excludeWildcard.toIpSpace().accept(SRC_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER);

    assertThat(expr, equalTo(new AndExpr(ImmutableList.of(new NotExpr(excludeExpr), includeExpr))));
  }

  @Test
  public void testVisitUniverseIpSpace() {
    BooleanExpr expr = UniverseIpSpace.INSTANCE.accept(SRC_IP_SPACE_BOOLEAN_EXPR_TRANSFORMER);
    assertThat(expr, equalTo(TrueExpr.INSTANCE));
  }
}
