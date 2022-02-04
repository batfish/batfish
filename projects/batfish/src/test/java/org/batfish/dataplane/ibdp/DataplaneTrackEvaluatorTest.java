package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.common.traceroute.TraceDag;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.tracking.NegatedTrackMethod;
import org.batfish.datamodel.tracking.TrackInterface;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.tracking.TrackMethodReference;
import org.batfish.datamodel.tracking.TrackRoute;
import org.batfish.datamodel.tracking.TrackTrue;
import org.junit.Test;

/** Test of {@link DataplaneTrackEvaluator}. */
public final class DataplaneTrackEvaluatorTest {

  @Test
  public void testVisitTrackInterface() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    Interface.builder()
        .setName("i1")
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setAdminUp(true)
        .build();
    Interface.builder()
        .setName("i2")
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setAdminUp(false)
        .build();
    TrackInterface tiUp = new TrackInterface("i1");
    TrackInterface tiDown = new TrackInterface("i2");
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(c, new DummyTracerouteEngine(), ImmutableMap.of());

    assertTrue(e.visit(tiUp));
    assertFalse(e.visit(tiDown));
  }

  @Test
  public void testTrackRoute() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    // contents don't matter
    TrackRoute trackRoute = TrackRoute.of(Prefix.ZERO, ImmutableSet.of(), "bar");
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(
            c, new DummyTracerouteEngine(), ImmutableMap.of(trackRoute, true));

    assertTrue(e.visit(trackRoute));
  }

  @Test
  public void testVisitTrackMethodReference() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    TrackMethod base = TrackTrue.instance();
    c.setTrackingGroups(ImmutableMap.of("1", base));
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(c, new DummyTracerouteEngine(), ImmutableMap.of());

    assertTrue(e.visit(TrackMethodReference.of("1")));
  }

  @Test
  public void testVisitNegatedTrackMethod() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(c, new DummyTracerouteEngine(), ImmutableMap.of());

    assertFalse(e.visit(NegatedTrackMethod.of(TrackTrue.instance())));
  }

  @Test
  public void testVisitTrackTrue() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    DataplaneTrackEvaluator evaluator =
        new DataplaneTrackEvaluator(c, new DummyTracerouteEngine(), ImmutableMap.of());

    assertTrue(evaluator.visit(TrackTrue.instance()));
  }

  private static final class DummyTracerouteEngine implements TracerouteEngine {

    @Override
    public SortedMap<Flow, List<TraceAndReverseFlow>> computeTracesAndReverseFlows(
        Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters) {
      return ImmutableSortedMap.of();
    }

    @Override
    public Map<Flow, TraceDag> computeTraceDags(
        Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters) {
      return ImmutableMap.of();
    }
  }
}
