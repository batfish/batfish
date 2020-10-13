package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceType;
import org.junit.Test;

public class TypesNodeSpecifierTest {

  @Test
  public void testResolve() {
    Configuration c1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    c1.setDeviceType(DeviceType.HOST);

    Configuration c2 = new Configuration("node2", ConfigurationFormat.CISCO_IOS);
    c2.setDeviceType(DeviceType.ROUTER);

    MockSpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of("node1", c1, "node2", c2))
            .build();

    assertThat(
        new TypesNodeSpecifier(ImmutableSet.of(DeviceType.HOST)).resolve(ctxt),
        equalTo(ImmutableSet.of("node1")));
    assertThat(
        new TypesNodeSpecifier(ImmutableSet.of(DeviceType.SWITCH)).resolve(ctxt),
        equalTo(ImmutableSet.of()));
  }
}
