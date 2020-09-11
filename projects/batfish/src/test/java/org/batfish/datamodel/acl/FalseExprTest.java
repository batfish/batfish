package org.batfish.datamodel.acl;

import com.google.common.testing.EqualsTester;
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
}
