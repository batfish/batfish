package org.batfish.specifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.junit.jupiter.api.Test;

class ConstantIpSpaceSpecifierTest {

  @Test
  void testResolve() {
    assertEquals(
        Ip.ZERO.toIpSpace(),
        new ConstantIpSpaceSpecifier(Ip.ZERO.toIpSpace())
            .resolve(MockSpecifierContext.builder().build()));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new ConstantIpSpaceSpecifier(Ip.ZERO.toIpSpace()),
            new ConstantIpSpaceSpecifier(Ip.ZERO.toIpSpace()))
        .addEqualityGroup(new ConstantIpSpaceSpecifier(Ip.parse("1.1.1.1").toIpSpace()))
        .testEquals();
  }
}
