package org.batfish.datamodel.tracking;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.junit.Test;

/** Tests of {@link StaticTrackMethodEvaluator} */
public class StaticTrackMethodEvaluatorTest {
  @Test
  public void testVisitTrackInterface() {
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
        .setAdminUp(true)
        .build();
    // i2: not active
    Interface.builder()
        .setOwner(c1)
        .setName("i2")
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setAdminUp(false)
        .build();

    StaticTrackMethodEvaluator evaluator = new StaticTrackMethodEvaluator(c1);
    // Iface is active
    TrackInterface trackInterface1 = new TrackInterface("i1");
    assertTrue(evaluator.visit(trackInterface1));

    // Iface is not active
    TrackInterface trackInterface2 = new TrackInterface("i2");
    assertFalse(evaluator.visit(trackInterface2));

    // Non-existent iface
    TrackInterface trackInterface4 = new TrackInterface("i4");
    assertFalse(evaluator.visit(trackInterface4));
  }

  @Test
  public void testVisitNegatedTrackMethod() {
    Configuration c1 =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    c1.setTrackingGroups(ImmutableMap.of("1", new TrackInterface("i1")));
    // i1: active
    Interface.builder()
        .setOwner(c1)
        .setName("i1")
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setAdminUp(true)
        .build();
    StaticTrackMethodEvaluator evaluator = new StaticTrackMethodEvaluator(c1);
    // Iface is active
    TrackInterface trackInterface1 = new TrackInterface("i1");

    assertTrue(evaluator.visit(trackInterface1));
    assertFalse(evaluator.visit(NegatedTrackMethod.of(trackInterface1)));
  }

  @Test
  public void testVisitTrackMethodReference() {
    Configuration c1 =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    // Iface is active
    TrackInterface trackInterface1 = new TrackInterface("i1");

    c1.setTrackingGroups(ImmutableMap.of("1", trackInterface1));
    // i1: active
    Interface.builder()
        .setOwner(c1)
        .setName("i1")
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setAdminUp(true)
        .build();
    StaticTrackMethodEvaluator evaluator = new StaticTrackMethodEvaluator(c1);

    assertTrue(evaluator.visit(trackInterface1));
    assertTrue(evaluator.visit(TrackMethodReference.of("1")));
  }
}
