package org.batfish.specifier;

import static junit.framework.TestCase.assertEquals;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.junit.Test;

public class ConstantIpSpaceSpecifierTest {

  @Test
  public void testResolve() {
    assertEquals(
        Ip.ZERO.toIpSpace(),
        new ConstantIpSpaceSpecifier(Ip.ZERO.toIpSpace())
            .resolve(MockSpecifierContext.builder().build()));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new ConstantIpSpaceSpecifier(Ip.ZERO.toIpSpace()),
            new ConstantIpSpaceSpecifier(Ip.ZERO.toIpSpace()))
        .addEqualityGroup(new ConstantIpSpaceSpecifier(Ip.parse("1.1.1.1").toIpSpace()))
        .testEquals();
  }
}
