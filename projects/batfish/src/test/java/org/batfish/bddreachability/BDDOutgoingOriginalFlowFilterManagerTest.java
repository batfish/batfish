package org.batfish.bddreachability;

import static org.batfish.bddreachability.transition.Transitions.addOutgoingOriginalFlowFiltersConstraint;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.removeOutgoingInterfaceConstraints;
import static org.batfish.common.bdd.BDDMatchers.isOne;
import static org.batfish.datamodel.ExprAclLine.REJECT_ALL;
import static org.batfish.datamodel.ExprAclLine.acceptingHeaderSpace;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.TransformationToTransition;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.junit.Test;

/** Tests of {@link BDDOutgoingOriginalFlowFilterManager}. */
public class BDDOutgoingOriginalFlowFilterManagerTest {
  private final BDDPacket _pkt = new BDDPacket();

  private static final String ACTIVE_IFACE_WITH_FILTER_1 = "activeWithFilter1";
  private static final String ACTIVE_IFACE_WITH_FILTER_2 = "activeWithFilter2";
  private static final String ACTIVE_IFACE_NO_FILTER_1 = "activeNoFilter1";
  private static final String ACTIVE_IFACE_NO_FILTER_2 = "activeNoFilter2";
  private static final String INACTIVE_IFACE_WITH_FILTER = "inactiveWithFilter";

  private static final Set<String> ALL_IFACES =
      ImmutableSet.of(
          ACTIVE_IFACE_WITH_FILTER_1,
          ACTIVE_IFACE_WITH_FILTER_2,
          ACTIVE_IFACE_NO_FILTER_1,
          ACTIVE_IFACE_NO_FILTER_2,
          INACTIVE_IFACE_WITH_FILTER);

  private static final Ip DST_IP_1 = Ip.parse("1.1.1.1");
  private static final Ip DST_IP_2 = Ip.parse("2.2.2.2");

  /**
   * Creates a config with the given interfaces, which are expected to be a subset of {@link
   * #ALL_IFACES}. Original flow filters for {@link #ACTIVE_IFACE_WITH_FILTER_1} and {@link
   * #INACTIVE_IFACE_WITH_FILTER} will permit only traffic to {@link #DST_IP_1}, and original flow
   * filter for {@link #ACTIVE_IFACE_WITH_FILTER_2} will permit only traffic to {@link #DST_IP_2}.
   */
  private static Configuration createConfig(NetworkFactory nf, Set<String> ifaces) {
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();

    // Create two filters that match destinations DST_IP_1 and DST_IP_2, respectively
    IpAccessList.Builder aclBuilder = nf.aclBuilder().setOwner(c);
    AclLine acceptDstIp1 =
        acceptingHeaderSpace(HeaderSpace.builder().setDstIps(DST_IP_1.toIpSpace()).build());
    AclLine acceptDstIp2 =
        acceptingHeaderSpace(HeaderSpace.builder().setDstIps(DST_IP_2.toIpSpace()).build());
    IpAccessList filter1 =
        aclBuilder.setName("acl1").setLines(ImmutableList.of(acceptDstIp1, REJECT_ALL)).build();
    IpAccessList filter2 =
        aclBuilder.setName("acl2").setLines(ImmutableList.of(acceptDstIp2, REJECT_ALL)).build();

    Interface.Builder ib = nf.interfaceBuilder().setOwner(c).setActive(true);
    if (ifaces.contains(ACTIVE_IFACE_WITH_FILTER_1)) {
      ib.setName(ACTIVE_IFACE_WITH_FILTER_1).setOutgoingOriginalFlowFilter(filter1).build();
    }
    if (ifaces.contains(ACTIVE_IFACE_WITH_FILTER_2)) {
      ib.setName(ACTIVE_IFACE_WITH_FILTER_2).setOutgoingOriginalFlowFilter(filter2).build();
    }
    if (ifaces.contains(ACTIVE_IFACE_NO_FILTER_1)) {
      ib.setName(ACTIVE_IFACE_NO_FILTER_1).setOutgoingOriginalFlowFilter(null).build();
    }
    if (ifaces.contains(ACTIVE_IFACE_NO_FILTER_2)) {
      ib.setName(ACTIVE_IFACE_NO_FILTER_2).setOutgoingOriginalFlowFilter(null).build();
    }
    if (ifaces.contains(INACTIVE_IFACE_WITH_FILTER)) {
      ib.setName(INACTIVE_IFACE_WITH_FILTER)
          .setActive(false)
          .setOutgoingOriginalFlowFilter(filter1)
          .build();
    }
    return c;
  }

  private BDDOutgoingOriginalFlowFilterManager getMgrForConfig(Configuration c) {
    Map<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);
    Map<String, BDDSourceManager> srcMgrs = BDDSourceManager.forNetwork(_pkt, configs);
    Map<String, BDDOutgoingOriginalFlowFilterManager> mgrs =
        BDDOutgoingOriginalFlowFilterManager.forNetwork(_pkt, configs, srcMgrs);
    return mgrs.get(c.getHostname());
  }

  @Test
  public void testEmpty() {
    BDDOutgoingOriginalFlowFilterManager empty = BDDOutgoingOriginalFlowFilterManager.empty(_pkt);
    assertTrue(empty.isTrivial());
  }

  @Test
  public void testOutgoingInterfaceBDD_trivial() {
    // No interfaces/no active interfaces/etc. The constraint is true.
    Configuration c = createConfig(new NetworkFactory(), ImmutableSet.of());
    BDDOutgoingOriginalFlowFilterManager mgr = getMgrForConfig(c);
    assertTrue(mgr.outgoingInterfaceBDD("anything").isOne());
  }

  @Test
  public void testOutgoingInterfaceBDD_activeAndInactive() {
    // If one active interface with a filter is present and no other active interfaces are present,
    // the interface's constraint BDD should be ONE.
    {
      Configuration c =
          createConfig(new NetworkFactory(), ImmutableSet.of(ACTIVE_IFACE_WITH_FILTER_1));
      BDDOutgoingOriginalFlowFilterManager mgr = getMgrForConfig(c);
      assertThat(mgr.outgoingInterfaceBDD(ACTIVE_IFACE_WITH_FILTER_1), isOne());
    }

    // If one active interface with a filter is present and other active interfaces are also
    // present, all interface's constraint BDDs should be nontrivial.
    {
      Configuration c =
          createConfig(
              new NetworkFactory(),
              ImmutableSet.of(ACTIVE_IFACE_WITH_FILTER_1, ACTIVE_IFACE_NO_FILTER_1));
      BDDOutgoingOriginalFlowFilterManager mgr = getMgrForConfig(c);
      BDD hasFilter = mgr.outgoingInterfaceBDD(ACTIVE_IFACE_WITH_FILTER_1);
      BDD noFilter = mgr.outgoingInterfaceBDD(ACTIVE_IFACE_NO_FILTER_1);
      assertThat(hasFilter.or(noFilter), isOne());
      assertThat(hasFilter, not(isOne()));
      assertThat(noFilter, not(isOne()));
    }
  }

  @Test
  public void testFiniteDomainValues_managerTrivial() {
    // If no active interfaces are present, finite domain should be empty.
    {
      Set<String> ifaces = ImmutableSet.of(INACTIVE_IFACE_WITH_FILTER);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingOriginalFlowFilterManager mgr = getMgrForConfig(c);
      assertTrue(mgr.isTrivial());
    }

    // If active interfaces are present but don't have outgoing original flow filters, still empty.
    {
      Set<String> ifaces = ImmutableSet.of(INACTIVE_IFACE_WITH_FILTER, ACTIVE_IFACE_NO_FILTER_1);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingOriginalFlowFilterManager mgr = getMgrForConfig(c);
      assertTrue(mgr.isTrivial());
    }
    {
      Set<String> ifaces =
          ImmutableSet.of(
              INACTIVE_IFACE_WITH_FILTER, ACTIVE_IFACE_NO_FILTER_1, ACTIVE_IFACE_NO_FILTER_2);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingOriginalFlowFilterManager mgr = getMgrForConfig(c);
      assertTrue(mgr.isTrivial());
    }
  }

  @Test
  public void testFiniteDomainValues_oneActiveIfaceWithFilter() {
    // If one active interface with a filter is present and no other active interfaces are present,
    // the interface's selection BDD should be ONE.
    {
      Set<String> ifaces = ImmutableSet.of(ACTIVE_IFACE_WITH_FILTER_1, INACTIVE_IFACE_WITH_FILTER);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingOriginalFlowFilterManager mgr = getMgrForConfig(c);
      assertThat(
          mgr.getInterfaceBDDs(),
          equalTo(ImmutableMap.of(ACTIVE_IFACE_WITH_FILTER_1, _pkt.getFactory().one())));
    }

    // If one active interface with a filter is present and other active interfaces are also
    // present, the interface's selection BDD should be nontrivial.
    {
      Set<String> ifaces =
          ImmutableSet.of(
              ACTIVE_IFACE_WITH_FILTER_1, INACTIVE_IFACE_WITH_FILTER, ACTIVE_IFACE_NO_FILTER_1);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingOriginalFlowFilterManager mgr = getMgrForConfig(c);
      assertThat(mgr.getInterfaceBDDs().keySet(), contains(ACTIVE_IFACE_WITH_FILTER_1));
      BDD activeIfaceWithFilterBdd = mgr.getInterfaceBDDs().get(ACTIVE_IFACE_WITH_FILTER_1);
      assertTrue(!activeIfaceWithFilterBdd.isZero() && !activeIfaceWithFilterBdd.isOne());
    }
  }

  @Test
  public void testFiniteDomainValues_twoActiveIfacesWithFilter() {
    // If two active interfaces with filters are present and no other active interfaces are present,
    // the active interfaces' selection BDDs should be nontrivial and should together make up the
    // entire space of finite domain values.
    {
      Set<String> ifaces =
          ImmutableSet.of(
              ACTIVE_IFACE_WITH_FILTER_1, ACTIVE_IFACE_WITH_FILTER_2, INACTIVE_IFACE_WITH_FILTER);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingOriginalFlowFilterManager mgr = getMgrForConfig(c);

      assertThat(
          mgr.getInterfaceBDDs().keySet(),
          containsInAnyOrder(ACTIVE_IFACE_WITH_FILTER_1, ACTIVE_IFACE_WITH_FILTER_2));
      BDD activeIfaceWithFilter1Bdd = mgr.getInterfaceBDDs().get(ACTIVE_IFACE_WITH_FILTER_1);
      BDD activeIfaceWithFilter2Bdd = mgr.getInterfaceBDDs().get(ACTIVE_IFACE_WITH_FILTER_2);
      assertTrue(!activeIfaceWithFilter1Bdd.isZero() && !activeIfaceWithFilter1Bdd.isOne());
      assertTrue(!activeIfaceWithFilter2Bdd.isZero() && !activeIfaceWithFilter2Bdd.isOne());
      assertTrue(activeIfaceWithFilter1Bdd.and(activeIfaceWithFilter2Bdd).isZero());
      assertTrue(activeIfaceWithFilter1Bdd.or(activeIfaceWithFilter2Bdd).isOne());
    }

    // If two active interfaces with filters are present and any other active interfaces are also
    // present, the active interfaces' selection BDDs should be nontrivial and should *not* make up
    // the entire space of finite domain values (a single value should be allocated to represent the
    // other active interfaces).
    {
      Set<String> ifaces =
          ImmutableSet.of(
              ACTIVE_IFACE_WITH_FILTER_1,
              ACTIVE_IFACE_WITH_FILTER_2,
              INACTIVE_IFACE_WITH_FILTER,
              ACTIVE_IFACE_NO_FILTER_1);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingOriginalFlowFilterManager mgr = getMgrForConfig(c);

      assertThat(
          mgr.getInterfaceBDDs().keySet(),
          containsInAnyOrder(ACTIVE_IFACE_WITH_FILTER_1, ACTIVE_IFACE_WITH_FILTER_2));
      BDD activeIfaceWithFilter1Bdd = mgr.getInterfaceBDDs().get(ACTIVE_IFACE_WITH_FILTER_1);
      BDD activeIfaceWithFilter2Bdd = mgr.getInterfaceBDDs().get(ACTIVE_IFACE_WITH_FILTER_2);
      assertTrue(!activeIfaceWithFilter1Bdd.isZero() && !activeIfaceWithFilter1Bdd.isOne());
      assertTrue(!activeIfaceWithFilter2Bdd.isZero() && !activeIfaceWithFilter2Bdd.isOne());
      assertTrue(activeIfaceWithFilter1Bdd.and(activeIfaceWithFilter2Bdd).isZero());
      assertFalse(activeIfaceWithFilter1Bdd.or(activeIfaceWithFilter2Bdd).isOne());
    }
  }

  @Test
  public void testForNetwork() {
    // Create a network with three configs
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 = createConfig(nf, ALL_IFACES);
    Configuration c2 = createConfig(nf, ALL_IFACES);
    Configuration c3 = createConfig(nf, ImmutableSet.of());
    Map<String, Configuration> configs =
        ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3);

    Map<String, BDDSourceManager> bddSrcMgrs = BDDSourceManager.forNetwork(_pkt, configs, false);
    Map<String, BDDOutgoingOriginalFlowFilterManager> mgrs =
        BDDOutgoingOriginalFlowFilterManager.forNetwork(_pkt, configs, bddSrcMgrs);
    BDDOutgoingOriginalFlowFilterManager mgr1 = mgrs.get(c1.getHostname());
    BDDOutgoingOriginalFlowFilterManager mgr2 = mgrs.get(c2.getHostname());
    BDDOutgoingOriginalFlowFilterManager mgr3 = mgrs.get(c3.getHostname());

    // The first two managers should be nontrivial and should use the same BDD values, so it should
    // work to use one to erase constraints created by the other.
    assertTrue(!mgr1.isTrivial() && !mgr2.isTrivial());
    assertTrue(mgr1.erase(mgr2.outgoingOriginalFlowFiltersConstraint()).isOne());

    // The third manager should be trivial (third config has no interfaces).
    assertTrue(mgr3.isTrivial());
  }

  @Test
  public void testTransitions() {
    /*
    Test that outgoingOriginalFlowFiltersConstraint correctly constrains flows. Will use a variety
    of flows, some of which will undergo transformations. The initial flows are:
      1. srcIp1 -> DST_IP_1
      2. srcIp2 -> DST_IP_1
      3. srcIp3 -> DST_IP_2
      4. srcIp4 -> DST_IP_2
    The transformations are:
      1. Dest NAT on flows from srcIp2 to transform dst to DST_IP_2
      2. Dest NAT on flows from srcIp4 to transform dst to DST_IP_1

    Will then test composite transitions including the outgoingOriginalFlowFiltersConstraint, the
    transformations, and a permit or deny at interface constraint from the manager.
     */
    Configuration c = createConfig(new NetworkFactory(), ALL_IFACES);
    BDDOutgoingOriginalFlowFilterManager mgr = getMgrForConfig(c);

    Ip ip1 = Ip.parse("10.10.10.1");
    Ip ip2 = Ip.parse("10.10.10.2");
    Ip ip3 = Ip.parse("10.10.10.3");
    Ip ip4 = Ip.parse("10.10.10.4");
    BDD srcIp1 = _pkt.getSrcIp().value(ip1.asLong());
    BDD srcIp2 = _pkt.getSrcIp().value(ip2.asLong());
    BDD srcIp3 = _pkt.getSrcIp().value(ip3.asLong());
    BDD srcIp4 = _pkt.getSrcIp().value(ip4.asLong());
    BDD dstIp1 = _pkt.getDstIp().value(DST_IP_1.asLong());
    BDD dstIp2 = _pkt.getDstIp().value(DST_IP_2.asLong());
    BDD srcIp1ToDstIp1 = srcIp1.and(dstIp1);
    BDD srcIp2ToDstIp1 = srcIp2.and(dstIp1);
    BDD srcIp3ToDstIp2 = srcIp3.and(dstIp2);
    BDD srcIp4ToDstIp2 = srcIp4.and(dstIp2);
    BDD initialFlows = srcIp1ToDstIp1.or(srcIp2ToDstIp1).or(srcIp3ToDstIp2).or(srcIp4ToDstIp2);

    Transition addOutgoingOriginalFlowFiltersConstraint =
        addOutgoingOriginalFlowFiltersConstraint(mgr);
    Transition removeOutgoingInterfaceConstraints = removeOutgoingInterfaceConstraints(mgr);

    // Build transformations
    Transformation transformation1 =
        Transformation.when(matchSrc(ip2.toIpSpace()))
            .apply(TransformationStep.assignDestinationIp(DST_IP_2))
            .build();
    Transformation transformation2 =
        Transformation.when(matchSrc(ip4.toIpSpace()))
            .apply(TransformationStep.assignDestinationIp(DST_IP_1))
            .build();
    TransformationToTransition transformationToTransition =
        new TransformationToTransition(
            _pkt,
            new IpAccessListToBddImpl(
                _pkt, BDDSourceManager.empty(_pkt), ImmutableMap.of(), ImmutableMap.of()));
    Transition transformation1Transition = transformationToTransition.toTransition(transformation1);
    Transition transformation2Transition = transformationToTransition.toTransition(transformation2);

    // Flows that will emerge from transformation
    BDD srcIp2TransformedFlows = srcIp2.and(dstIp2);
    BDD srcIp4TransformedFlows = srcIp4.and(dstIp1);

    /*
    Flows permitted out ACTIVE_IFACE_WITH_FILTER_1 should have:
      a. Flows from srcIp1 -> DST_IP_1; these succeed out without getting transformed
      b. Flows from srcIp2 -> DST_IP_2; these get transformed but had correct original dst
      c. No flows from srcIp3; these don't get transformed and get blocked by filter
      d. No flows from srcIp4; these get transformed but get blocked by filter (wrong original dst)
    */
    {
      BDD permittedOutIface1 = mgr.permittedByOriginalFlowEgressFilter(ACTIVE_IFACE_WITH_FILTER_1);
      Transition transition =
          compose(
              addOutgoingOriginalFlowFiltersConstraint,
              transformation1Transition,
              transformation2Transition,
              constraint(permittedOutIface1),
              removeOutgoingInterfaceConstraints);

      BDD flows = transition.transitForward(initialFlows);
      assertThat(flows, equalTo(srcIp1ToDstIp1.or(srcIp2TransformedFlows)));

      // Run it backwards. Should see that only flows originally destined for dstIp1 get permitted.
      BDD backwardsFlows = transition.transitBackward(_pkt.getFactory().one());
      assertThat(backwardsFlows, equalTo(dstIp1));
    }

    // Flows denied out ACTIVE_IFACE_WITH_FILTER_1 should be the opposite of the permitted flows:
    // should include original flows from srcIp3 and transformed flows from srcIp4, and no flows
    // from srcIp1 or srcIp2.
    {
      BDD deniedOutIface1 = mgr.deniedByOriginalFlowEgressFilter(ACTIVE_IFACE_WITH_FILTER_1);
      Transition denyTransition =
          compose(
              addOutgoingOriginalFlowFiltersConstraint,
              transformation1Transition,
              transformation2Transition,
              constraint(deniedOutIface1),
              removeOutgoingInterfaceConstraints);

      BDD flows = denyTransition.transitForward(initialFlows);
      assertThat(flows, equalTo(srcIp3ToDstIp2.or(srcIp4TransformedFlows)));

      // Run it backwards. Should see that only flows originally not destined for dstIp1 get denied.
      BDD backwardsFlows = denyTransition.transitBackward(_pkt.getFactory().one());
      assertThat(backwardsFlows, equalTo(dstIp1.not()));
    }

    // ACTIVE_IFACE_NO_FILTER_1 should permit all flows since it has no filter.
    {
      BDD permittedOutIfaceWithoutFilter =
          mgr.permittedByOriginalFlowEgressFilter(ACTIVE_IFACE_NO_FILTER_1);
      Transition transition =
          compose(
              addOutgoingOriginalFlowFiltersConstraint,
              transformation1Transition,
              transformation2Transition,
              constraint(permittedOutIfaceWithoutFilter),
              removeOutgoingInterfaceConstraints);

      // Just start with all flows since we expect them all to be permitted. Due to transformations,
      // not every possible flow can reach the interface.
      BDD flows = transition.transitForward(_pkt.getFactory().one());
      BDD allUntransformedFlows = _pkt.getFactory().one().diff(srcIp2.or(srcIp4));
      BDD allTransformedFlows = srcIp2TransformedFlows.or(srcIp4TransformedFlows);
      assertThat(flows, equalTo(allUntransformedFlows.or(allTransformedFlows)));

      // Run it backwards; should still see that anything can reach permitted state
      BDD backwardsFlows = transition.transitBackward(_pkt.getFactory().one());
      assertTrue(backwardsFlows.isOne());
    }

    // ACTIVE_IFACE_NO_FILTER_1 should not deny any flows since it has no filter.
    {
      BDD deniedOutIfaceWithoutFilter =
          mgr.deniedByOriginalFlowEgressFilter(ACTIVE_IFACE_NO_FILTER_1);
      Transition denyTransition =
          compose(
              addOutgoingOriginalFlowFiltersConstraint,
              transformation1Transition,
              transformation2Transition,
              constraint(deniedOutIfaceWithoutFilter),
              removeOutgoingInterfaceConstraints);

      // Just start with all flows since we don't expect any to be denied.
      BDD flows = denyTransition.transitForward(_pkt.getFactory().one());
      assertTrue(flows.isZero());

      // Run it backwards; should still see that nothing can reach denied state
      BDD backwardsFlows = denyTransition.transitBackward(_pkt.getFactory().one());
      assertTrue(backwardsFlows.isZero());
    }
  }

  @Test
  public void testErase() {
    Configuration c = createConfig(new NetworkFactory(), ALL_IFACES);
    BDDOutgoingOriginalFlowFilterManager mgr = getMgrForConfig(c);

    // erase should clear implications contingent on interface constraints
    BDD outgoingOriginalFlowFiltersConstraint = mgr.outgoingOriginalFlowFiltersConstraint();
    assertTrue(mgr.erase(outgoingOriginalFlowFiltersConstraint).isOne());

    // erase should clear all interface constraints as well as permit var
    BDD permittedOutIface1 = mgr.permittedByOriginalFlowEgressFilter(ACTIVE_IFACE_WITH_FILTER_1);
    assertTrue(mgr.erase(permittedOutIface1).isOne());
  }
}
