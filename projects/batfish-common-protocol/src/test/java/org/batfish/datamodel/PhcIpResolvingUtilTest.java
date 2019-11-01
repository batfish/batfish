package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.SpecifierContext;
import org.junit.Test;

/** Tests for {@link PhcIpResolvingUtil} */
public class PhcIpResolvingUtilTest {

  @Test
  public void testResolveIpSpaceOrUniverse_fromIp() {
    SpecifierContext specifierContext = MockSpecifierContext.builder().build();
    assertThat(
        PhcIpResolvingUtil.resolveIpSpaceOrUniverse("1.2.3.4", ImmutableSet.of(), specifierContext),
        equalTo(Ip.parse("1.2.3.4").toIpSpace()));
  }

  @Test
  public void testResolveIpSpaceOrUniverse_fromLocation() {
    SpecifierContext specifierContext =
        MockSpecifierContext.builder()
            .setInterfaceOwnedIps(
                ImmutableMap.of("node", ImmutableMap.of("iface", Ip.parse("1.2.3.4").toIpSpace())))
            .build();
    assertThat(
        PhcIpResolvingUtil.resolveIpSpaceOrUniverse(
            "", ImmutableSet.of(new InterfaceLocation("node", "iface")), specifierContext),
        equalTo(Ip.parse("1.2.3.4").toIpSpace()));
  }

  @Test
  public void testResolveIpSpaceOrUniverse_defaultUniverse() {
    SpecifierContext specifierContext = MockSpecifierContext.builder().build();
    assertThat(
        PhcIpResolvingUtil.resolveIpSpaceOrUniverse("", ImmutableSet.of(), specifierContext),
        equalTo(UniverseIpSpace.INSTANCE));
  }
}
