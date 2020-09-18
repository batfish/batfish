package org.batfish.datamodel.acl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.TraceElement;
import org.junit.Test;

/** Tests of {@link TrueExpr} */
public class TrueExprTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(TrueExpr.INSTANCE, new TrueExpr(null))
        .addEqualityGroup(new Object())
        .addEqualityGroup(new TrueExpr(TraceElement.of("trace element")))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    assertThat(
        TrueExpr.INSTANCE,
        equalTo(BatfishObjectMapper.clone(TrueExpr.INSTANCE, AclLineMatchExpr.class)));
    assertThat(
        new TrueExpr(TraceElement.of("foo")),
        equalTo(
            BatfishObjectMapper.clone(
                new TrueExpr(TraceElement.of("foo")), AclLineMatchExpr.class)));
  }
}
