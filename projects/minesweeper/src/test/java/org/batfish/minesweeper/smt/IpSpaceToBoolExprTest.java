package org.batfish.minesweeper.smt;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.junit.Test;

public class IpSpaceToBoolExprTest {
  private static final Context CONTEXT = new Context();

  private static final BitVecExpr VAR = CONTEXT.mkBVConst("IP_VAR", 32);

  private static final IpSpaceToBoolExpr IP_SPACE_TO_BOOL_EXPR =
      new IpSpaceToBoolExpr(CONTEXT, VAR);

  @Test
  public void testAclIpSpace_empty() {
    assertThat(
        IP_SPACE_TO_BOOL_EXPR.visit(AclIpSpace.builder().build()), equalTo(CONTEXT.mkFalse()));
  }

  @Test
  public void testAclIpSpace() {
    IpSpace ipSpace1 = Ip.parse("1.1.1.1").toIpSpace();
    IpSpace ipSpace2 = Ip.parse("2.2.2.2").toIpSpace();
    IpSpace ipSpace3 = Ip.parse("3.3.3.3").toIpSpace();

    BoolExpr boolExpr1 = IP_SPACE_TO_BOOL_EXPR.visit(ipSpace1);
    BoolExpr boolExpr2 = IP_SPACE_TO_BOOL_EXPR.visit(ipSpace2);
    BoolExpr boolExpr3 = IP_SPACE_TO_BOOL_EXPR.visit(ipSpace3);

    IpSpace ipSpace =
        AclIpSpace.builder()
            .thenPermitting(ipSpace1)
            .thenRejecting(ipSpace2)
            .thenPermitting(ipSpace3)
            .build();

    assertThat(
        IP_SPACE_TO_BOOL_EXPR.visit(ipSpace),
        equalTo(
            CONTEXT.mkITE(
                boolExpr1,
                CONTEXT.mkTrue(), // permit
                CONTEXT.mkITE(
                    boolExpr2,
                    CONTEXT.mkFalse(), // reject
                    CONTEXT.mkITE(
                        boolExpr3,
                        CONTEXT.mkTrue(), // permit
                        CONTEXT.mkFalse())))));
  }

  @Test
  public void testEmptyIpSpace() {
    assertThat(IP_SPACE_TO_BOOL_EXPR.visit(EmptyIpSpace.INSTANCE), equalTo(CONTEXT.mkFalse()));
  }

  @Test
  public void testIpIpSpace() {
    Ip ip = Ip.parse("1.2.3.4");
    assertThat(
        IP_SPACE_TO_BOOL_EXPR.visit(ip.toIpSpace()),
        equalTo(CONTEXT.mkEq(VAR, CONTEXT.mkBV(ip.asLong(), 32))));
  }

  @Test
  public void testIpWildcardIpSpace() {
    Ip ip = Ip.create(0x01000200);
    Ip mask = Ip.create(0x00FF00FF);
    IpWildcard ipWildcard = IpWildcard.ipWithWildcardMask(ip, mask);
    BitVecExpr ipBV = CONTEXT.mkBV(ip.asLong(), 32);
    BitVecExpr maskBV = CONTEXT.mkBV(mask.inverted().asLong(), 32);
    assertThat(
        IP_SPACE_TO_BOOL_EXPR.visit(ipWildcard.toIpSpace()),
        equalTo(CONTEXT.mkEq(CONTEXT.mkBVAND(VAR, maskBV), CONTEXT.mkBVAND(ipBV, maskBV))));
  }

  @Test
  public void testPrefixIpSpace() {
    Prefix prefix = Prefix.parse("1.2.0.0/16");
    BitVecExpr subnet = CONTEXT.mkBV(0x01020000, 32);
    BitVecExpr mask = CONTEXT.mkBV(0xFFFF0000, 32);
    assertThat(
        IP_SPACE_TO_BOOL_EXPR.visit(prefix.toIpSpace()),
        equalTo(CONTEXT.mkEq(CONTEXT.mkBVAND(VAR, mask), CONTEXT.mkBVAND(subnet, mask))));
  }

  @Test
  public void testUniverseIpSpace() {
    assertThat(IP_SPACE_TO_BOOL_EXPR.visit(UniverseIpSpace.INSTANCE), equalTo(CONTEXT.mkTrue()));
  }
}
