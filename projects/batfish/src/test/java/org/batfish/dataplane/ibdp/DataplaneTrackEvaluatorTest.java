package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.RoutingProtocol.CONNECTED;
import static org.batfish.datamodel.RoutingProtocol.HMM;
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
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
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
import org.batfish.dataplane.rib.Rib;
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
        new DataplaneTrackEvaluator(c, ImmutableMap.of(), new DummyTracerouteEngine());

    assertTrue(e.visit(tiUp));
    assertFalse(e.visit(tiDown));
  }

  @Test
  public void testTrackRoute() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    Rib rib = new Rib();
    Prefix prefix = Prefix.parse("192.0.2.0/24");
    rib.mergeRoute(new AnnotatedRoute<>(new ConnectedRoute(prefix, "foo"), DEFAULT_VRF_NAME));
    TrackRoute trMatchWithoutProtocol = TrackRoute.of(prefix, ImmutableSet.of(), DEFAULT_VRF_NAME);
    TrackRoute trMatchWithProtocol =
        TrackRoute.of(prefix, ImmutableSet.of(CONNECTED), DEFAULT_VRF_NAME);
    TrackRoute trPrefixMismatch = TrackRoute.of(Prefix.ZERO, ImmutableSet.of(), DEFAULT_VRF_NAME);
    TrackRoute trProtocolMismatch = TrackRoute.of(prefix, ImmutableSet.of(HMM), DEFAULT_VRF_NAME);
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(
            c, ImmutableMap.of(DEFAULT_VRF_NAME, rib), new DummyTracerouteEngine());

    assertTrue(e.visit(trMatchWithoutProtocol));
    assertTrue(e.visit(trMatchWithProtocol));
    assertFalse(e.visit(trPrefixMismatch));
    assertFalse(e.visit(trProtocolMismatch));
  }

  @Test
  public void testVisitTrackMethodReference() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    TrackMethod base = TrackTrue.instance();
    c.setTrackingGroups(ImmutableMap.of("1", base));
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(c, ImmutableMap.of(), new DummyTracerouteEngine());

    assertTrue(e.visit(TrackMethodReference.of("1")));
  }

  @Test
  public void testVisitNegatedTrackMethod() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    DataplaneTrackEvaluator e =
        new DataplaneTrackEvaluator(c, ImmutableMap.of(), new DummyTracerouteEngine());

    assertFalse(e.visit(NegatedTrackMethod.of(TrackTrue.instance())));
  }

  @Test
  public void testVisitTrackTrue() {
    Configuration c =
        Configuration.builder().setHostname("c").setConfigurationFormat(CISCO_IOS).build();
    DataplaneTrackEvaluator evaluator =
        new DataplaneTrackEvaluator(c, ImmutableMap.of(), new DummyTracerouteEngine());

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
