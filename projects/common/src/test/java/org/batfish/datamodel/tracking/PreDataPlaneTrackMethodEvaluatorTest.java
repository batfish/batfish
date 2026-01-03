package org.batfish.datamodel.tracking;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.tracking.TrackMethods.interfaceActive;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.TestInterface;
import org.junit.Test;

/** Tests of {@link PreDataPlaneTrackMethodEvaluator} */
public class PreDataPlaneTrackMethodEvaluatorTest {
  @Test
  public void testVisitTrackInterface() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    // i1: active
    TestInterface.builder()
        .setOwner(c)
        .setName("i1")
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setAdminUp(true)
        .build();
    // i2: not active
    TestInterface.builder()
        .setOwner(c)
        .setName("i2")
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setAdminUp(false)
        .build();

    PreDataPlaneTrackMethodEvaluator evaluator = new PreDataPlaneTrackMethodEvaluator(c);
    // Iface is active
    TrackMethod trackInterface1 = interfaceActive("i1");
    assertTrue(evaluator.visit(trackInterface1));

    // Iface is not active
    TrackMethod trackInterface2 = interfaceActive("i2");
    assertFalse(evaluator.visit(trackInterface2));

    // Non-existent iface
    TrackMethod trackInterface4 = interfaceActive("i4");
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
  public void testVisitTrackAll() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    PreDataPlaneTrackMethodEvaluator evaluator = new PreDataPlaneTrackMethodEvaluator(c);

    // vacuously true
    assertTrue(evaluator.visit(TrackAll.of(ImmutableList.of())));
    // singleton
    assertTrue(evaluator.visit(TrackAll.of(ImmutableList.of(TrackTrue.instance()))));
    assertFalse(
        evaluator.visit(
            TrackAll.of(ImmutableList.of(NegatedTrackMethod.of(TrackTrue.instance())))));
    // multiple
    assertTrue(
        evaluator.visit(TrackAll.of(ImmutableList.of(TrackTrue.instance(), TrackTrue.instance()))));
    assertFalse(
        evaluator.visit(
            TrackAll.of(
                ImmutableList.of(
                    TrackTrue.instance(), NegatedTrackMethod.of(TrackTrue.instance())))));
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
