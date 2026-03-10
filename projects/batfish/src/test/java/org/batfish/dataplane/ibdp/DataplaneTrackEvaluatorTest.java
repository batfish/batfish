package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.tracking.TrackMethods.all;
import static org.batfish.datamodel.tracking.TrackMethods.alwaysFalse;
import static org.batfish.datamodel.tracking.TrackMethods.alwaysTrue;
import static org.batfish.datamodel.tracking.TrackMethods.interfaceActive;
import static org.batfish.datamodel.tracking.TrackMethods.negated;
import static org.batfish.datamodel.tracking.TrackMethods.reachability;
import static org.batfish.datamodel.tracking.TrackMethods.reference;
import static org.batfish.datamodel.tracking.TrackMethods.route;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.tracking.TrackReachability;
import org.batfish.datamodel.tracking.TrackRoute;
import org.junit.Test;

/** Test of {@link DataplaneTrackEvaluator}. */
public final class DataplaneTrackEvaluatorTest {

  @Test
  public void testVisitTrackInterface() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    TestInterface.builder()
        .setName("i1")
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setAdminUp(true)
        .build();
    TestInterface.builder()
        .setName("i2")
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setAdminUp(false)
        .build();
    TrackMethod tiUp = interfaceActive("i1");
    TrackMethod tiDown = interfaceActive("i2");
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(c, ImmutableMap.of(), ImmutableMap.of());

    assertTrue(e.visit(tiUp));
    assertFalse(e.visit(tiDown));
  }

  @Test
  public void testTrackReachability() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    // contents don't matter
    TrackMethod trackReachability = reachability(Ip.parse("192.0.2.1"), DEFAULT_VRF_NAME);
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(
            c, ImmutableMap.of((TrackReachability) trackReachability, true), ImmutableMap.of());

    assertTrue(e.visit(trackReachability));
  }

  @Test
  public void testTrackRoute() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    // contents don't matter
    TrackMethod trackRoute = route(Prefix.ZERO, ImmutableSet.of(), "bar");
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(
            c, ImmutableMap.of(), ImmutableMap.of((TrackRoute) trackRoute, true));

    assertTrue(e.visit(trackRoute));
  }

  @Test
  public void testVisitTrackMethodReference() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    TrackMethod base = alwaysTrue();
    c.setTrackingGroups(ImmutableMap.of("1", base));
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(c, ImmutableMap.of(), ImmutableMap.of());

    assertTrue(e.visit(reference("1")));
  }

  @Test
  public void testVisitNegatedTrackMethod() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(c, ImmutableMap.of(), ImmutableMap.of());

    assertFalse(e.visit(alwaysFalse()));
  }

  @Test
  public void testVisitTrackAll() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(c, ImmutableMap.of(), ImmutableMap.of());

    // vacuously true
    assertTrue(e.visit(all(ImmutableList.of())));
    // singleton
    assertTrue(e.visit(all(ImmutableList.of(alwaysTrue()))));
    assertFalse(e.visit(all(ImmutableList.of(negated(alwaysTrue())))));
    // multiple
    assertTrue(e.visit(all(ImmutableList.of(alwaysTrue(), alwaysTrue()))));
    assertFalse(e.visit(all(ImmutableList.of(alwaysTrue(), negated(alwaysTrue())))));
  }

  @Test
  public void testVisitTrackTrue() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    DataplaneTrackEvaluator evaluator =
        new DataplaneTrackEvaluator(c, ImmutableMap.of(), ImmutableMap.of());

    assertTrue(evaluator.visit(alwaysTrue()));
  }
}
