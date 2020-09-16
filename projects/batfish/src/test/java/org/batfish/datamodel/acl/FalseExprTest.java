package org.batfish.datamodel.acl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.TraceElement;
import org.junit.Test;

/** Tests of {@link FalseExpr} */
public class FalseExprTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(FalseExpr.INSTANCE, new FalseExpr(null))
        .addEqualityGroup(new Object())
        .addEqualityGroup(new FalseExpr(TraceElement.of("trace element")))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    assertThat(
        FalseExpr.INSTANCE,
        equalTo(BatfishObjectMapper.clone(FalseExpr.INSTANCE, AclLineMatchExpr.class)));
    assertThat(
        new FalseExpr(TraceElement.of("foo")),
        equalTo(
            BatfishObjectMapper.clone(
                new FalseExpr(TraceElement.of("foo")), AclLineMatchExpr.class)));
  }
}
