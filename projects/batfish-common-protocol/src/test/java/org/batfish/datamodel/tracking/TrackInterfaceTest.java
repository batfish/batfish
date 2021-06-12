package org.batfish.datamodel.tracking;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.junit.Test;

/** Tests of {@link TrackInterface} */
public class TrackInterfaceTest {
  @Test
  public void testEvaluate() {
    Configuration c1 =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    c1.setTrackingGroups(ImmutableMap.of("1", new TrackInterface("i1tracked")));
    // i1: active
    Interface.builder()
        .setOwner(c1)
        .setName("i1")
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setActive(true)
        .build();
    // i2: not active
    Interface.builder()
        .setOwner(c1)
        .setName("i2")
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setActive(false)
        .build();

    // Iface is active
    TrackInterface trackInterface1 = new TrackInterface("i1");
    assertFalse(trackInterface1.evaluate(c1));

    // Iface is not active
    TrackInterface trackInterface2 = new TrackInterface("i2");
    assertTrue(trackInterface2.evaluate(c1));

    // Non-existent iface
    TrackInterface trackInterface3 = new TrackInterface("i3");
    assertFalse(trackInterface3.evaluate(c1));
  }
}
