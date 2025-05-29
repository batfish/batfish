package org.batfish.minesweeper.bdd;

import static org.batfish.common.bdd.BDDMatchers.isZero;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.minesweeper.OspfType;
import org.batfish.minesweeper.bdd.BDDRoute.NextHopType;
import org.batfish.minesweeper.bdd.BDDTunnelEncapsulationAttribute.Value;
import org.junit.Test;
import org.junit.internal.matchers.ThrowableMessageMatcher;

/** Tests for {@link BDDRoute}. */
public class BDDRouteTest {
  @Test
  public void testEquals() {
    BDDFactory factory = JFactory.init(100, 100);
    BDDRoute orig = new BDDRoute(factory, 3, 4, 5, 6, 7, 2, ImmutableList.of());
    BDDRoute admin = new BDDRoute(orig);
    admin.getAdminDist().setValue(1);
    BDDRoute asPathRegex = new BDDRoute(orig);
    asPathRegex.getAsPathRegexAtomicPredicates().setValue(1);
    BDDRoute clusterListLength = new BDDRoute(orig);
    clusterListLength.getClusterListLength().setValue(1);
    BDDRoute communities = new BDDRoute(orig);
    communities.getCommunityAtomicPredicates()[0] = factory.one();
    BDDRoute localPref = new BDDRoute(orig);
    localPref.getLocalPref().setValue(1);
    BDDRoute med = new BDDRoute(orig);
    med.getMed().setValue(1);
    BDDRoute nextHop = new BDDRoute(orig);
    nextHop.getNextHop().setValue(1);
    BDDRoute nextHopInterfaces = new BDDRoute(orig);
    nextHopInterfaces.getNextHopInterfaces().setValue(1);
    BDDRoute nextHopSet = new BDDRoute(orig);
    nextHopSet.setNextHopSet(true);
    BDDRoute nextHopType = new BDDRoute(orig);
    nextHopType.setNextHopType(NextHopType.BGP_PEER_ADDRESS);
    BDDRoute originType = new BDDRoute(orig);
    originType.getOriginType().setValue(OriginType.EGP);
    BDDRoute ospfMetric = new BDDRoute(orig);
    ospfMetric.getOspfMetric().setValue(OspfType.E1);
    BDDRoute prefix = new BDDRoute(orig);
    prefix.getPrefix().setValue(1);
    BDDRoute prefixLength = new BDDRoute(orig);
    prefixLength.getPrefixLength().setValue(1);
    BDDRoute prependedAses = new BDDRoute(orig);
    prependedAses.getPrependedASes().add(1L);
    BDDRoute protocolHistory = new BDDRoute(orig);
    protocolHistory.getProtocolHistory().setValue(RoutingProtocol.STATIC);
    BDDRoute sourceVrfs = new BDDRoute(orig);
    sourceVrfs.getSourceVrfs().setValue(1);
    BDDRoute tag = new BDDRoute(orig);
    tag.getTag().setValue(1);
    BDDRoute tracks = new BDDRoute(orig);
    tracks.getTracks()[0] = factory.one();
    BDDRoute peerAddress = new BDDRoute(orig);
    peerAddress.getPeerAddress().setValue(1);
    BDDRoute tunnelEncapsulationAttribute = new BDDRoute(orig);
    tunnelEncapsulationAttribute.getTunnelEncapsulationAttribute().setValue(Value.absent());
    BDDRoute unsupported = new BDDRoute(orig);
    unsupported.setUnsupported(true);
    BDDRoute weight = new BDDRoute(orig);
    weight.getWeight().setValue(1);
    new EqualsTester()
        .addEqualityGroup(orig, new BDDRoute(orig))
        .addEqualityGroup(admin)
        .addEqualityGroup(asPathRegex)
        .addEqualityGroup(clusterListLength)
        .addEqualityGroup(communities)
        .addEqualityGroup(localPref)
        .addEqualityGroup(med)
        .addEqualityGroup(nextHop)
        .addEqualityGroup(nextHopInterfaces)
        .addEqualityGroup(nextHopType)
        .addEqualityGroup(nextHopSet)
        .addEqualityGroup(originType)
        .addEqualityGroup(ospfMetric)
        .addEqualityGroup(peerAddress)
        .addEqualityGroup(prefix)
        .addEqualityGroup(prefixLength)
        .addEqualityGroup(prependedAses)
        .addEqualityGroup(protocolHistory)
        .addEqualityGroup(sourceVrfs)
        .addEqualityGroup(tag)
        .addEqualityGroup(tracks)
        .addEqualityGroup(tunnelEncapsulationAttribute)
        .addEqualityGroup(unsupported)
        .addEqualityGroup(weight)
        .testEquals();
  }

  @Test
  public void testWellFormedOriginType() {
    BDDFactory factory = JFactory.init(100, 100);
    BDDRoute route = new BDDRoute(factory, 0, 0, 0, 0, 0, 0, ImmutableList.of());

    BDD anyOriginType =
        factory.orAll(
            route.getOriginType().value(OriginType.EGP),
            route.getOriginType().value(OriginType.IGP),
            route.getOriginType().value(OriginType.INCOMPLETE));
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> route.getOriginType().satAssignmentToValue(anyOriginType.not()));
    assertThat(
        thrown, ThrowableMessageMatcher.hasMessage(containsString("is not valid in this domain")));
    assertThat(route.wellFormednessConstraints(true).and(anyOriginType.not()), isZero());
  }
}
