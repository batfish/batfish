package org.batfish.vendor.sonic.representation;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.junit.Test;

public class L3InterfaceTest {

  @Test
  public void testJavaSerialization() {
    L3Interface obj =
        new L3Interface(
            ImmutableMap.of(
                ConcreteInterfaceAddress.parse("172.19.93.0/31"),
                InterfaceKeyProperties.builder().setSecondary(true).build()));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new L3Interface(ImmutableMap.of()), new L3Interface(ImmutableMap.of()))
        .addEqualityGroup(
            new L3Interface(
                ImmutableMap.of(
                    ConcreteInterfaceAddress.parse("172.19.93.0/31"),
                    InterfaceKeyProperties.builder().build())))
        .testEquals();
  }
}
