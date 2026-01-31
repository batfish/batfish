package org.batfish.vendor.arista.representation;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class StandardAccessListRemarkLineTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new StandardAccessListRemarkLine(5, "remark"),
            new StandardAccessListRemarkLine(5, "remark"))
        .addEqualityGroup(new StandardAccessListRemarkLine(6, "remark"))
        .addEqualityGroup(new StandardAccessListRemarkLine(6, "remark2"))
        .testEquals();
  }
}
