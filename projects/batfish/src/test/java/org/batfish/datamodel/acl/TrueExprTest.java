package org.batfish.datamodel.acl;

import com.google.common.testing.EqualsTester;
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
}
