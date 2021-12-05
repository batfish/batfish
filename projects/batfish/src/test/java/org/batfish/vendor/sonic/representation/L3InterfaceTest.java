package org.batfish.vendor.sonic.representation;

import static org.batfish.vendor.sonic.representation.L3Interface.createL3Interfaces;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.junit.Test;

public class L3InterfaceTest {

  @Test
  public void testCreateL3Interfaces() {
    assertThat(
        createL3Interfaces(
            ImmutableMap.of(
                "Ethernet136",
                ImmutableMap.of(),
                "Ethernet136|0:0:0:0:0:ffff:ac13:5d00/127",
                ImmutableMap.of(),
                "Ethernet136|172.19.93.0/31",
                ImmutableMap.of(),
                "Ethernet137",
                ImmutableMap.of())),
        equalTo(
            ImmutableMap.of(
                "Ethernet136",
                new L3Interface(ConcreteInterfaceAddress.parse("172.19.93.0/31")),
                "Ethernet137",
                new L3Interface(null))));
  }

  @Test
  public void testJavaSerialization() {
    L3Interface obj = new L3Interface(ConcreteInterfaceAddress.parse("172.19.93.0/31"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    L3Interface obj = new L3Interface(null);
    new EqualsTester()
        .addEqualityGroup(obj, new L3Interface(null))
        .addEqualityGroup(new L3Interface(ConcreteInterfaceAddress.parse("1.1.1.1/24")))
        .testEquals();
  }
}
