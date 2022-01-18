package org.batfish.datamodel.tracking;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.junit.Test;

/** Tests of {@link StaticTrackMethodEvaluator} */
public class StaticTrackMethodEvaluatorTest {
  @Test
  public void testVisitTrackInterface() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    // i1: active
    Interface.builder()
        .setOwner(c)
        .setName("i1")
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setActive(true)
        .build();
    // i2: not active
    Interface.builder()
        .setOwner(c)
        .setName("i2")
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setActive(false)
        .build();
    // i3: active, but blacklisted
    Interface.builder()
        .setOwner(c)
        .setName("i3")
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setActive(true)
        .setBlacklisted(true)
        .build();

    StaticTrackMethodEvaluator evaluator = new StaticTrackMethodEvaluator(c);
    // Iface is active
    TrackInterface trackInterface1 = new TrackInterface("i1");
    assertTrue(evaluator.visit(trackInterface1));

    // Iface is not active
    TrackInterface trackInterface2 = new TrackInterface("i2");
    assertFalse(evaluator.visit(trackInterface2));

    // Iface is active, but blacklisted
    TrackInterface trackInterface3 = new TrackInterface("i3");
    assertFalse(evaluator.visit(trackInterface3));

    // Non-existent iface
    TrackInterface trackInterface4 = new TrackInterface("i4");
    assertFalse(evaluator.visit(trackInterface4));
  }

  @Test
  public void testVisitNegatedTrackMethod() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    TrackMethod base = TrackTrue.instance();
    StaticTrackMethodEvaluator evaluator = new StaticTrackMethodEvaluator(c);

    assertFalse(evaluator.visit(NegatedTrackMethod.of(base)));
  }

  @Test
  public void testVisitTrackMethodReference() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    TrackMethod base = TrackTrue.instance();
    c.setTrackingGroups(ImmutableMap.of("1", base));
    StaticTrackMethodEvaluator evaluator = new StaticTrackMethodEvaluator(c);

    assertTrue(evaluator.visit(TrackMethodReference.of("1")));
  }

  @Test
  public void testVisitTrackTrue() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    StaticTrackMethodEvaluator evaluator = new StaticTrackMethodEvaluator(c);

    assertTrue(evaluator.visit(TrackTrue.instance()));
  }
}
