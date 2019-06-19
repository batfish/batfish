package org.batfish.datamodel.acl.normalize;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.datamodel.acl.normalize.Negate.negate;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.junit.Test;

public class NegateTest {

  @Test
  public void visitAndMatchExpr() {
    assertThat(
        negate(and(ORIGINATING_FROM_DEVICE, TRUE, FALSE)),
        equalTo(or(not(ORIGINATING_FROM_DEVICE), FALSE, TRUE)));
  }

  @Test
  public void visitFalseExpr() {
    assertThat(negate(FALSE), equalTo(TRUE));
  }

  @Test
  public void visitMatchHeaderSpace() {
    AclLineMatchExpr matchHeaderSpace = match(HeaderSpace.builder().build());
    assertThat(negate(matchHeaderSpace), equalTo(not(matchHeaderSpace)));
  }

  @Test
  public void visitMatchSrcInterface() {
    MatchSrcInterface matchSrcInterface = AclLineMatchExprs.matchSrcInterface("foo");
    assertThat(negate(matchSrcInterface), equalTo(not(matchSrcInterface)));
  }

  @Test
  public void visitNotMatchExpr() {
    assertThat(negate(not(ORIGINATING_FROM_DEVICE)), equalTo(ORIGINATING_FROM_DEVICE));
    assertThat(negate(not(null)), nullValue());
  }

  @Test
  public void visitOriginatingFromDevice() {
    assertThat(negate(ORIGINATING_FROM_DEVICE), equalTo(not(ORIGINATING_FROM_DEVICE)));
  }

  @Test
  public void visitOrMatchExpr() {
    assertThat(
        negate(or(ORIGINATING_FROM_DEVICE, TRUE, FALSE)),
        equalTo(and(not(ORIGINATING_FROM_DEVICE), FALSE, TRUE)));
  }

  @Test
  public void visitPermittedByAcl() {
    PermittedByAcl permittedByFoo = permittedByAcl("foo");
    assertThat(negate(permittedByFoo), equalTo(not(permittedByFoo)));
  }

  @Test
  public void visitTrueExpr() {
    assertThat(negate(TRUE), equalTo(FALSE));
  }
}
