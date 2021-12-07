package org.batfish.vendor.sonic.representation;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.junit.Test;

public class L3InterfaceTest {

  @Test
  public void testJavaSerialization() {
    L3Interface obj = new L3Interface(ConcreteInterfaceAddress.parse("172.19.93.0/31"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new L3Interface(null), new L3Interface(null))
        .addEqualityGroup(new L3Interface(ConcreteInterfaceAddress.parse("172.19.93.0/31")))
        .testEquals();
  }
}
