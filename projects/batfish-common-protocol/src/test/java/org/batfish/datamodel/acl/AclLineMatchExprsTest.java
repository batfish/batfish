package org.batfish.datamodel.acl;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class AclLineMatchExprsTest {

  @Test
  public void testAnd() {
    assertThat(and(ImmutableList.of()), equalTo(TrueExpr.INSTANCE));

    MatchSrcInterface expr = matchSrcInterface("a");
    assertThat(and(ImmutableList.of(expr)), equalTo(expr));
  }

  @Test
  public void testOr() {
    assertThat(or(ImmutableList.of()), equalTo(FalseExpr.INSTANCE));

    MatchSrcInterface expr = matchSrcInterface("a");
    assertThat(or(ImmutableList.of(expr)), equalTo(expr));
  }
}
