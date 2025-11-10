package org.batfish.minesweeper.bdd;

import static org.batfish.common.bdd.BDDMatchers.isZero;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
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

  @Test
  public void testAugmentPairing() {
    BDDFactory factory = JFactory.init(100, 100);
    BDDRoute orig =
        new BDDRoute(
            factory,
            2,
            2,
            2,
            2,
            2,
            2,
            ImmutableList.of(new TunnelEncapsulationAttribute(Ip.create(1))));
    BDDPairing pairing = factory.makePair();
    BDDPairing attrPairing = factory.makePair();

    // Test adminDist
    BDD adminSupport = orig.getAdminDist().support();
    BDDRoute admin = new BDDRoute(orig);
    admin.getAdminDist().setValue(1);
    admin.augmentPairing(orig, pairing);
    admin.getAdminDist().augmentPairing(orig.getAdminDist(), attrPairing);
    assertEquals(adminSupport.veccompose(pairing), adminSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test localPref
    BDD localPrefSupport = orig.getLocalPref().support();
    BDDRoute localPref = new BDDRoute(orig);
    localPref.getLocalPref().setValue(100);
    localPref.augmentPairing(orig, pairing);
    localPref.getLocalPref().augmentPairing(orig.getLocalPref(), attrPairing);
    assertEquals(localPrefSupport.veccompose(pairing), localPrefSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test med
    BDD medSupport = orig.getMed().support();
    BDDRoute med = new BDDRoute(orig);
    med.getMed().setValue(50);
    med.augmentPairing(orig, pairing);
    med.getMed().augmentPairing(orig.getMed(), attrPairing);
    assertEquals(medSupport.veccompose(pairing), medSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test weight
    BDD weightSupport = orig.getWeight().support();
    BDDRoute weight = new BDDRoute(orig);
    weight.getWeight().setValue(200);
    weight.augmentPairing(orig, pairing);
    weight.getWeight().augmentPairing(orig.getWeight(), attrPairing);
    assertEquals(weightSupport.veccompose(pairing), weightSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test tag
    BDD tagSupport = orig.getTag().support();
    BDDRoute tag = new BDDRoute(orig);
    tag.getTag().setValue(300);
    tag.augmentPairing(orig, pairing);
    tag.getTag().augmentPairing(orig.getTag(), attrPairing);
    assertEquals(tagSupport.veccompose(pairing), tagSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test nextHop
    BDD nextHopSupport = orig.getNextHop().support();
    BDDRoute nextHop = new BDDRoute(orig);
    nextHop.getNextHop().setValue(12345);
    nextHop.augmentPairing(orig, pairing);
    nextHop.getNextHop().augmentPairing(orig.getNextHop(), attrPairing);
    assertEquals(nextHopSupport.veccompose(pairing), nextHopSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test clusterListLength
    BDD clusterListSupport = orig.getClusterListLength().support();
    BDDRoute clusterList = new BDDRoute(orig);
    clusterList.getClusterListLength().setValue(3);
    clusterList.augmentPairing(orig, pairing);
    clusterList.getClusterListLength().augmentPairing(orig.getClusterListLength(), attrPairing);
    assertEquals(
        clusterListSupport.veccompose(pairing), clusterListSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test prefix
    BDD prefixSupport = orig.getPrefix().support();
    BDDRoute prefix = new BDDRoute(orig);
    prefix.getPrefix().setValue(16777216); // 1.0.0.0
    prefix.augmentPairing(orig, pairing);
    prefix.getPrefix().augmentPairing(orig.getPrefix(), attrPairing);
    assertEquals(prefixSupport.veccompose(pairing), prefixSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test prefixLength
    BDD prefixLengthSupport = orig.getPrefixLength().support();
    BDDRoute prefixLength = new BDDRoute(orig);
    prefixLength.getPrefixLength().setValue(24);
    prefixLength.augmentPairing(orig, pairing);
    prefixLength.getPrefixLength().augmentPairing(orig.getPrefixLength(), attrPairing);
    assertEquals(
        prefixLengthSupport.veccompose(pairing), prefixLengthSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test asPathRegexAtomicPredicates
    BDD asPathSupport = orig.getAsPathRegexAtomicPredicates().support();
    BDDRoute asPath = new BDDRoute(orig);
    asPath.getAsPathRegexAtomicPredicates().setValue(1);
    asPath.augmentPairing(orig, pairing);
    asPath
        .getAsPathRegexAtomicPredicates()
        .augmentPairing(orig.getAsPathRegexAtomicPredicates(), attrPairing);
    assertEquals(asPathSupport.veccompose(pairing), asPathSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test originType
    BDD originTypeSupport = orig.getOriginType().support();
    BDDRoute originType = new BDDRoute(orig);
    originType.getOriginType().setValue(OriginType.IGP);
    originType.augmentPairing(orig, pairing);
    originType.getOriginType().augmentPairing(orig.getOriginType(), attrPairing);
    assertEquals(originTypeSupport.veccompose(pairing), originTypeSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test ospfMetric
    BDD ospfMetricSupport = orig.getOspfMetric().support();
    BDDRoute ospfMetric = new BDDRoute(orig);
    ospfMetric.getOspfMetric().setValue(OspfType.E1);
    ospfMetric.augmentPairing(orig, pairing);
    ospfMetric.getOspfMetric().augmentPairing(orig.getOspfMetric(), attrPairing);
    assertEquals(ospfMetricSupport.veccompose(pairing), ospfMetricSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test protocolHistory
    BDD protocolHistorySupport = orig.getProtocolHistory().support();
    BDDRoute protocolHistory = new BDDRoute(orig);
    protocolHistory.getProtocolHistory().setValue(RoutingProtocol.BGP);
    protocolHistory.augmentPairing(orig, pairing);
    protocolHistory.getProtocolHistory().augmentPairing(orig.getProtocolHistory(), attrPairing);
    assertEquals(
        protocolHistorySupport.veccompose(pairing), protocolHistorySupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test nextHopInterfaces
    BDD nextHopInterfacesSupport = orig.getNextHopInterfaces().support();
    BDDRoute nextHopInterfaces = new BDDRoute(orig);
    nextHopInterfaces.getNextHopInterfaces().setValue(1);
    nextHopInterfaces.augmentPairing(orig, pairing);
    nextHopInterfaces
        .getNextHopInterfaces()
        .augmentPairing(orig.getNextHopInterfaces(), attrPairing);
    assertEquals(
        nextHopInterfacesSupport.veccompose(pairing),
        nextHopInterfacesSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test peerAddress
    BDD peerAddressSupport = orig.getPeerAddress().support();
    BDDRoute peerAddress = new BDDRoute(orig);
    peerAddress.getPeerAddress().setValue(1);
    peerAddress.augmentPairing(orig, pairing);
    peerAddress.getPeerAddress().augmentPairing(orig.getPeerAddress(), attrPairing);
    assertEquals(
        peerAddressSupport.veccompose(pairing), peerAddressSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test sourceVrfs
    BDD sourceVrfsSupport = orig.getSourceVrfs().support();
    BDDRoute sourceVrfs = new BDDRoute(orig);
    sourceVrfs.getSourceVrfs().setValue(1);
    sourceVrfs.augmentPairing(orig, pairing);
    sourceVrfs.getSourceVrfs().augmentPairing(orig.getSourceVrfs(), attrPairing);
    assertEquals(sourceVrfsSupport.veccompose(pairing), sourceVrfsSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test tunnelEncapsulationAttribute
    BDD tunnelSupport = orig.getTunnelEncapsulationAttribute().support();
    BDDRoute tunnel = new BDDRoute(orig);
    tunnel
        .getTunnelEncapsulationAttribute()
        .setValue(Value.literal(new TunnelEncapsulationAttribute(Ip.create(1))));
    tunnel.augmentPairing(orig, pairing);
    tunnel
        .getTunnelEncapsulationAttribute()
        .augmentPairing(orig.getTunnelEncapsulationAttribute(), attrPairing);
    assertEquals(tunnelSupport.veccompose(pairing), tunnelSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test communityAtomicPredicates (uses pairing.set instead of augmentPairing)
    BDD[] origCommunities = orig.getCommunityAtomicPredicates();
    BDD communitySupport = factory.andAll(origCommunities);
    BDDRoute community = new BDDRoute(orig);
    BDD[] newCommunities = community.getCommunityAtomicPredicates();
    newCommunities[0] = factory.one(); // modify first community predicate
    community.augmentPairing(orig, pairing);
    attrPairing.set(origCommunities, newCommunities);
    assertEquals(communitySupport.veccompose(pairing), communitySupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();

    // Test tracks (uses pairing.set instead of augmentPairing)
    BDD[] origTracks = orig.getTracks();
    BDD trackSupport = factory.andAll(origTracks);
    BDDRoute track = new BDDRoute(orig);
    BDD[] newTracks = track.getTracks();
    newTracks[0] = factory.one(); // modify first track
    track.augmentPairing(orig, pairing);
    attrPairing.set(origTracks, newTracks);
    assertEquals(trackSupport.veccompose(pairing), trackSupport.veccompose(attrPairing));
    pairing.reset();
    attrPairing.reset();
  }
}
