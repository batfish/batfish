package org.batfish.minesweeper.smt;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.microsoft.z3.Context;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.TrueExpr;
import org.junit.Test;

public class IpAccessListToBoolExprTest {

  private static final Context CONTEXT = new Context();

  private static final SymbolicPacket PKT = new SymbolicPacket(CONTEXT, 12345, "TEST");

  private static final IpAccessListToBoolExpr IP_ACCESS_LIST_TO_BOOL_EXPR =
      new IpAccessListToBoolExpr(CONTEXT, PKT);

  @Test
  public void testAndMatchExpr() {
    assertThat(
        IP_ACCESS_LIST_TO_BOOL_EXPR.visit(
            new AndMatchExpr(ImmutableList.of(FalseExpr.INSTANCE, TrueExpr.INSTANCE))),
        equalTo(CONTEXT.mkAnd(CONTEXT.mkFalse(), CONTEXT.mkTrue())));
  }

  @Test
  public void testFalseMatchExpr() {
    assertThat(IP_ACCESS_LIST_TO_BOOL_EXPR.visit(FalseExpr.INSTANCE), equalTo(CONTEXT.mkFalse()));
  }

  @Test
  public void testMatchHeaderSpace_unconstrained() {
    assertThat(
        IP_ACCESS_LIST_TO_BOOL_EXPR.visit(new MatchHeaderSpace(HeaderSpace.builder().build())),
        equalTo(CONTEXT.mkTrue()));
  }

  @Test
  public void testMatchHeaderSpace_emptyDstIps() {
    HeaderSpace headerSpace = HeaderSpace.builder().setDstIps(EmptyIpSpace.INSTANCE).build();
    assertThat(
        IP_ACCESS_LIST_TO_BOOL_EXPR.visit(new MatchHeaderSpace(headerSpace)),
        equalTo(CONTEXT.mkAnd(CONTEXT.mkFalse())));
  }

  @Test
  public void testMatchHeaderSpace_emptyDstIps_anySrcIp() {
    HeaderSpace headerSpace =
        HeaderSpace.builder()
            .setDstIps(EmptyIpSpace.INSTANCE)
            .setSrcIps(UniverseIpSpace.INSTANCE)
            .build();

    assertThat(
        IP_ACCESS_LIST_TO_BOOL_EXPR.visit(new MatchHeaderSpace(headerSpace)),
        equalTo(CONTEXT.mkAnd(CONTEXT.mkFalse(), CONTEXT.mkTrue())));
  }

  @Test
  public void testMatchHeaderSpace_emptyDstIps_negated() {
    HeaderSpace headerSpace =
        HeaderSpace.builder().setDstIps(EmptyIpSpace.INSTANCE).setNegate(true).build();

    assertThat(
        IP_ACCESS_LIST_TO_BOOL_EXPR.visit(new MatchHeaderSpace(headerSpace)),
        equalTo(CONTEXT.mkNot(CONTEXT.mkAnd(CONTEXT.mkFalse()))));
  }

  @Test
  public void testNotMatchExpr() {
    assertThat(
        IP_ACCESS_LIST_TO_BOOL_EXPR.visit(new NotMatchExpr(TrueExpr.INSTANCE)),
        equalTo(CONTEXT.mkNot(CONTEXT.mkTrue())));
  }

  @Test
  public void testOrMatchExpr() {
    assertThat(
        IP_ACCESS_LIST_TO_BOOL_EXPR.visit(
            new OrMatchExpr(ImmutableList.of(FalseExpr.INSTANCE, TrueExpr.INSTANCE))),
        equalTo(CONTEXT.mkOr(CONTEXT.mkFalse(), CONTEXT.mkTrue())));
  }

  @Test
  public void testTrueMatchExpr() {
    assertThat(IP_ACCESS_LIST_TO_BOOL_EXPR.visit(TrueExpr.INSTANCE), equalTo(CONTEXT.mkTrue()));
  }
}
