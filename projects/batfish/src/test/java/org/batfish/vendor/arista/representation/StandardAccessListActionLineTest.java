package org.batfish.vendor.arista.representation;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.junit.Test;

public class StandardAccessListActionLineTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new StandardAccessListActionLine(5, LineAction.DENY, "name", IpWildcard.ANY),
            new StandardAccessListActionLine(5, LineAction.DENY, "name", IpWildcard.ANY))
        .addEqualityGroup(
            new StandardAccessListActionLine(6, LineAction.DENY, "name", IpWildcard.ANY))
        .addEqualityGroup(
            new StandardAccessListActionLine(6, LineAction.PERMIT, "name", IpWildcard.ANY))
        .addEqualityGroup(
            new StandardAccessListActionLine(6, LineAction.PERMIT, "name2", IpWildcard.ANY))
        .addEqualityGroup(
            new StandardAccessListActionLine(
                6, LineAction.PERMIT, "name2", IpWildcard.parse("1.2.3.4")))
        .testEquals();
  }
}
