package org.batfish.datamodel.acl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.Test;

public class AclLineMatchExprTest {

  @Test
  public void testCompareDifferentClasses() {
    assertThat(FalseExpr.INSTANCE.compareTo(TrueExpr.INSTANCE), not(equalTo(0)));
  }

  @Test
  public void testCompareSameDescription() {
    assertThat(exprWithDescription("desc").compareTo(exprWithDescription("desc")), equalTo(0));
  }

  @Test
  public void testCompareDifferentDescriptions() {
    assertThat(
        exprWithDescription("desc1").compareTo(exprWithDescription("desc2")), not(equalTo(0)));
  }

  @Test
  public void testCompareFirstDescriptionNull() {
    assertThat(exprWithDescription(null).compareTo(exprWithDescription("desc")), not(equalTo(0)));
  }

  @Test
  public void testCompareSecondDescriptionNull() {
    assertThat(exprWithDescription("desc").compareTo(exprWithDescription(null)), not(equalTo(0)));
  }

  @Test
  public void testCompareBothDescriptionsNull() {
    assertThat(exprWithDescription(null).compareTo(exprWithDescription(null)), equalTo(0));
  }

  private AclLineMatchExpr exprWithDescription(String description) {
    return new AndMatchExpr(null, description);
  }
}
