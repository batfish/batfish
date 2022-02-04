package org.batfish.datamodel.tracking;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.junit.Test;

/** Tests of {@link PreDataPlaneTrackMethodEvaluator} */
public class PreDataPlaneTrackMethodEvaluatorTest {
  @Test
  public void testVisitTrackInterface() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    // i1: active
    Interface.builder()
        .setOwner(c)
        .setName("i1")
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setAdminUp(true)
        .build();
    // i2: not active
    Interface.builder()
        .setOwner(c)
        .setName("i2")
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setAdminUp(false)
        .build();

    PreDataPlaneTrackMethodEvaluator evaluator = new PreDataPlaneTrackMethodEvaluator(c);
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
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    TrackMethod base = TrackTrue.instance();
    PreDataPlaneTrackMethodEvaluator evaluator = new PreDataPlaneTrackMethodEvaluator(c);

    assertFalse(evaluator.visit(NegatedTrackMethod.of(base)));
  }

  @Test
  public void testVisitTrackMethodReference() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    TrackMethod base = TrackTrue.instance();
    c.setTrackingGroups(ImmutableMap.of("1", base));
    PreDataPlaneTrackMethodEvaluator evaluator = new PreDataPlaneTrackMethodEvaluator(c);

    assertTrue(evaluator.visit(TrackMethodReference.of("1")));
  }

  @Test
  public void testVisitTrackTrue() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    PreDataPlaneTrackMethodEvaluator evaluator = new PreDataPlaneTrackMethodEvaluator(c);

    assertTrue(evaluator.visit(TrackTrue.instance()));
  }
}
