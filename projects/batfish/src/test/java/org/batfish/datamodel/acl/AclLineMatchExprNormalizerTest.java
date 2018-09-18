package org.batfish.datamodel.acl;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBDD;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AclLineMatchExprNormalizerTest {
  private static final String IFACE = "IFACE";

  private static final AclLineMatchExpr A =
      matchDst(new IpWildcard(new Ip(0x00000000L), new Ip(0xFFFFFFF0L)));
  private static final AclLineMatchExpr B =
      matchDst(new IpWildcard(new Ip(0x00000000L), new Ip(0xFFFFFF0FL)));
  private static final AclLineMatchExpr C =
      matchDst(new IpWildcard(new Ip(0x00000000L), new Ip(0xFFFFF0FFL)));
  private static final AclLineMatchExpr D =
      matchDst(new IpWildcard(new Ip(0x00000000L), new Ip(0xFFFF0FFFL)));
  private static final AclLineMatchExpr E =
      matchDst(new IpWildcard(new Ip(0x00000000L), new Ip(0xFFF0FFFFL)));

  private IpAccessListToBDD _toBDD;

  @Before
  public void setup() {
    BDDPacket pkt = new BDDPacket();
    BDDSourceManager mgr = BDDSourceManager.forInterfaces(pkt, ImmutableSet.of(IFACE));
    _toBDD = new IpAccessListToBDD(pkt, mgr, ImmutableMap.of(), ImmutableMap.of());
  }

  private AclLineMatchExpr normalize(AclLineMatchExpr expr) {
    return AclLineMatchExprNormalizer.normalize(_toBDD, expr);
  }

  @Test
  public void visitAndMatchExpr() {
    Assert.assertThat(normalize(and(not(TRUE))), Matchers.equalTo(FALSE));
    AclLineMatchExpr actual = normalize(and(TRUE));
    Assert.assertThat(actual, Matchers.equalTo(TRUE));
    Assert.assertThat(normalize(and(FALSE)), Matchers.equalTo(FALSE));
    Assert.assertThat(normalize(and(TRUE, FALSE)), Matchers.equalTo(FALSE));
  }

  @Test
  public void visitAndMatchExpr_distributeOverOr() {
    AclLineMatchExpr expr = and(A, B, or(C, D));
    AclLineMatchExpr nf = or(and(A, B, C), and(A, B, D));
    Assert.assertThat(normalize(expr), Matchers.equalTo(nf));
  }

  @Test
  public void visitAndMatchExpr_distributeOverOr2() {
    AclLineMatchExpr expr = and(A, B, or(and(C, D), E));
    AclLineMatchExpr nf = or(and(A, B, C, D), and(A, B, E));
    AclLineMatchExpr actual = normalize(expr);
    Assert.assertThat(actual, Matchers.equalTo(nf));
  }

  @Test
  public void visitAndMatchExpr_expandAnd() {
    AclLineMatchExpr expr = and(A, and(B, C));
    AclLineMatchExpr nf = and(A, B, C);
    Assert.assertThat(normalize(expr), Matchers.equalTo(nf));
  }

  @Test
  public void visitAndMatchExpr_nf() {
    // identity on normal forms
    AclLineMatchExpr expr = and(A, B, not(C));
    Assert.assertThat(normalize(expr), Matchers.equalTo(expr));
  }

  @Test
  public void visitAndMatchExpr_recursionExpansion() {
    AclLineMatchExpr expr = and(A, or(and(B, C), D));
    AclLineMatchExpr nf = or(and(A, B, C), and(A, D));
    Assert.assertThat(normalize(expr), Matchers.equalTo(nf));
  }

  @Test
  public void visitFalseExpr() {
    Assert.assertThat(normalize(FALSE), Matchers.equalTo(FALSE));
  }

  @Test
  public void visitMatchHeaderSpace() {
    AclLineMatchExpr matchHeaderSpace = match(HeaderSpace.builder().build());
    Assert.assertThat(normalize(matchHeaderSpace), Matchers.equalTo(TRUE));
  }

  @Test
  public void visitMatchSrcInterface() {
    MatchSrcInterface matchSrcInterface = AclLineMatchExprs.matchSrcInterface(IFACE);
    Assert.assertThat(normalize(matchSrcInterface), Matchers.equalTo(matchSrcInterface));
  }

  @Test
  public void visitNotMatchExpr() {
    Assert.assertThat(normalize(not(FALSE)), Matchers.equalTo(TRUE));
    Assert.assertThat(normalize(not(TRUE)), Matchers.equalTo(FALSE));
    Assert.assertThat(normalize(not(A)), Matchers.equalTo(not(A)));
    Assert.assertThat(normalize(not(not(A))), Matchers.equalTo(A));
    Assert.assertThat(normalize(not(or(A, B))), Matchers.equalTo(and(not(A), not(B))));
    Assert.assertThat(normalize(not(and(A, B))), Matchers.equalTo(or(not(A), not(B))));
  }

  @Test
  public void visitOriginatingFromDevice() {
    Assert.assertThat(
        normalize(ORIGINATING_FROM_DEVICE), Matchers.equalTo(ORIGINATING_FROM_DEVICE));
  }

  @Test
  public void visitOrMatchExpr() {
    Assert.assertThat(normalize(or(TRUE)), Matchers.equalTo(TRUE));
    Assert.assertThat(normalize(or(FALSE)), Matchers.equalTo(FALSE));
    Assert.assertThat(normalize(or(TRUE, FALSE)), Matchers.equalTo(TRUE));
    Assert.assertThat(normalize(or(not(TRUE))), Matchers.equalTo(FALSE));
  }

  @Test
  public void visitOrMatchExpr_nf() {
    // identity on normal forms
    AclLineMatchExpr expr = or(A, B, not(C));
    Assert.assertThat(normalize(expr), Matchers.equalTo(expr));
  }

  @Test
  public void visitOrMatchExpr_expandOr() {
    // normalize recursively
    AclLineMatchExpr expr = or(A, or(B, C));
    AclLineMatchExpr nf = or(A, B, C);
    Assert.assertThat(normalize(expr), Matchers.equalTo(nf));
  }

  @Test
  public void visitOrMatchExpr_dontDistributeOverAnd() {
    AclLineMatchExpr expr = or(A, B, and(C, D));
    Assert.assertThat(normalize(expr), Matchers.equalTo(expr));
  }

  @Test
  public void visitOrMatchExpr_recursionExpansion() {
    AclLineMatchExpr expr = or(A, and(or(B, C), D));
    AclLineMatchExpr nf = or(A, and(B, D), and(C, D));
    Assert.assertThat(normalize(expr), Matchers.equalTo(nf));
  }

  @Test
  public void visitTrueExpr() {
    Assert.assertThat(normalize(TRUE), Matchers.equalTo(TRUE));
  }
}
