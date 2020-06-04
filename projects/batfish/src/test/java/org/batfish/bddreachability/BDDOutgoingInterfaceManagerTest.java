package org.batfish.bddreachability;

import static org.batfish.datamodel.ExprAclLine.REJECT_ALL;
import static org.batfish.datamodel.ExprAclLine.acceptingHeaderSpace;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
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

/** Tests of {@link BDDOutgoingInterfaceManager}. */
public class BDDOutgoingInterfaceManagerTest {
  private static final BDDPacket PKT = new BDDPacket();

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

  private static BDDOutgoingInterfaceManager getMgrForConfig(Configuration c) {
    Map<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);
    Map<String, BDDSourceManager> srcMgrs = BDDSourceManager.forNetwork(PKT, configs);
    Map<String, BDDOutgoingInterfaceManager> mgrs =
        BDDOutgoingInterfaceManager.forNetwork(PKT, configs, srcMgrs);
    return mgrs.get(c.getHostname());
  }

  @Test
  public void testFiniteDomainValues_managerTrivial() {
    // If no active interfaces are present, finite domain should be empty.
    {
      Set<String> ifaces = ImmutableSet.of(INACTIVE_IFACE_WITH_FILTER);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingInterfaceManager mgr = getMgrForConfig(c);
      assertTrue(mgr.isTrivial());
    }

    // If active interfaces are present but don't have outgoing original flow filters, still empty.
    {
      Set<String> ifaces = ImmutableSet.of(INACTIVE_IFACE_WITH_FILTER, ACTIVE_IFACE_NO_FILTER_1);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingInterfaceManager mgr = getMgrForConfig(c);
      assertTrue(mgr.isTrivial());
    }
    {
      Set<String> ifaces =
          ImmutableSet.of(
              INACTIVE_IFACE_WITH_FILTER, ACTIVE_IFACE_NO_FILTER_1, ACTIVE_IFACE_NO_FILTER_2);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingInterfaceManager mgr = getMgrForConfig(c);
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
      BDDOutgoingInterfaceManager mgr = getMgrForConfig(c);
      assertThat(
          mgr.getInterfaceBDDs(),
          equalTo(ImmutableMap.of(ACTIVE_IFACE_WITH_FILTER_1, PKT.getFactory().one())));
    }

    // If one active interface with a filter is present and other active interfaces are also
    // present, the interface's selection BDD should be nontrivial.
    {
      Set<String> ifaces =
          ImmutableSet.of(
              ACTIVE_IFACE_WITH_FILTER_1, INACTIVE_IFACE_WITH_FILTER, ACTIVE_IFACE_NO_FILTER_1);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingInterfaceManager mgr = getMgrForConfig(c);
      assertThat(mgr.getInterfaceBDDs().keySet(), contains(ACTIVE_IFACE_WITH_FILTER_1));
      BDD activeIfaceWithFilterBdd = mgr.getInterfaceBDDs().get(ACTIVE_IFACE_WITH_FILTER_1);
      assertTrue(!activeIfaceWithFilterBdd.isZero() && !activeIfaceWithFilterBdd.isOne());
    }
    {
      Set<String> ifaces =
          ImmutableSet.of(
              ACTIVE_IFACE_WITH_FILTER_1,
              INACTIVE_IFACE_WITH_FILTER,
              ACTIVE_IFACE_NO_FILTER_1,
              ACTIVE_IFACE_NO_FILTER_2);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingInterfaceManager mgr = getMgrForConfig(c);
      assertThat(mgr.getInterfaceBDDs().keySet(), contains(ACTIVE_IFACE_WITH_FILTER_1));
      BDD activeIfaceWithFilterBdd = mgr.getInterfaceBDDs().get(ACTIVE_IFACE_WITH_FILTER_1);
      assertTrue(!activeIfaceWithFilterBdd.isZero() && !activeIfaceWithFilterBdd.isOne());
    }
  }

  @Test
  public void testFiniteDomainValues_twoActiveIfacesWithFilter() {
    // If two active interfaces with filters are present and no other active interfaces are present,
    // the active interfaces' selection BDDs should be nontrivial and mutually complementary.
    {
      Set<String> ifaces =
          ImmutableSet.of(
              ACTIVE_IFACE_WITH_FILTER_1, ACTIVE_IFACE_WITH_FILTER_2, INACTIVE_IFACE_WITH_FILTER);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingInterfaceManager mgr = getMgrForConfig(c);

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
    // present, the active interfaces' selection BDDs should be nontrivial and mutually incomplete.
    {
      Set<String> ifaces =
          ImmutableSet.of(
              ACTIVE_IFACE_WITH_FILTER_1,
              ACTIVE_IFACE_WITH_FILTER_2,
              INACTIVE_IFACE_WITH_FILTER,
              ACTIVE_IFACE_NO_FILTER_1);
      Configuration c = createConfig(new NetworkFactory(), ifaces);
      BDDOutgoingInterfaceManager mgr = getMgrForConfig(c);

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
    // Create a network with two configs with the same interfaces and outgoing original flow filters
    NetworkFactory nf = new NetworkFactory();
    Configuration config1 = createConfig(nf, ALL_IFACES);
    Configuration config2 = createConfig(nf, ALL_IFACES);
    Map<String, Configuration> configs =
        ImmutableMap.of(config1.getHostname(), config1, config2.getHostname(), config2);

    Map<String, BDDSourceManager> bddSrcMgrs = BDDSourceManager.forNetwork(PKT, configs, false);
    Map<String, BDDOutgoingInterfaceManager> mgrs =
        BDDOutgoingInterfaceManager.forNetwork(PKT, configs, bddSrcMgrs);
    BDDOutgoingInterfaceManager mgr1 = mgrs.get(config1.getHostname());
    BDDOutgoingInterfaceManager mgr2 = mgrs.get(config2.getHostname());

    // The two managers use the same BDD values to track outgoing interfaces.
    assertThat(mgr1.getInterfaceBDDs(), equalTo(mgr2.getInterfaceBDDs()));
  }

  @Test
  public void testForwardConstraints() {
    /*
    Test that outgoingOriginalFlowFiltersConstraint correctly constrains forward flows:
    1. Original flows have dst DST_IP_1
    2. Constrain using outgoingOriginalFlowFiltersConstraint to track outgoing interface permissions
    3. Put all flows through transformation to have transformed dst DST_IP_2
    4. Try sending flows out ACTIVE_IFACE_WITH_FILTER_1 and ACTIVE_IFACE_WITH_FILTER_2. "Sending
       out" means constraining the flows to the finite domain's BDD for the interface.
        a. All flows should be allowed out ACTIVE_IFACE_WITH_FILTER_1 because its original flow
          filter permits DST_IP_1
        b. No flows should be allowed out ACTIVE_IFACE_WITH_FILTER_2 because its original flow
          filter permits only DST_IP_2
     */
    Configuration c = createConfig(new NetworkFactory(), ALL_IFACES);
    BDDOutgoingInterfaceManager mgr = getMgrForConfig(c);

    BDD dstIp1 = PKT.getDstIp().value(DST_IP_1.asLong());
    BDD dstIp2 = PKT.getDstIp().value(DST_IP_2.asLong());
    BDD originalFlowFiltersConstraint = mgr.outgoingOriginalFlowFiltersConstraint();
    Transformation transformation =
        Transformation.always().apply(TransformationStep.assignDestinationIp(DST_IP_2)).build();
    Transition transformationTransition =
        new TransformationToTransition(
                PKT,
                new IpAccessListToBddImpl(
                    PKT, BDDSourceManager.empty(PKT), ImmutableMap.of(), ImmutableMap.of()))
            .toTransition(transformation);

    BDD flows = dstIp1.and(originalFlowFiltersConstraint); // track outgoing interface permissions
    flows = transformationTransition.transitForward(flows); // transform dst to DST_IP_2

    // First iface's original flow filter matches DST_IP_1; all flows should be permitted even
    // though their dst is now DST_IP_2
    BDD permittedOutIface1 =
        flows.and(mgr.permittedByOriginalFlowEgressFilter(ACTIVE_IFACE_WITH_FILTER_1));
    BDD deniedOutIface1 =
        flows.and(mgr.deniedByOriginalFlowEgressFilter(ACTIVE_IFACE_WITH_FILTER_1));
    assertFalse(permittedOutIface1.isZero());
    assertTrue(deniedOutIface1.isZero());

    // Second iface's original flow filter matches DST_IP_2; all flows should be denied, since they
    // were *originally* destined for DST_IP_1
    BDD permittedOutIface2 =
        flows.and(mgr.permittedByOriginalFlowEgressFilter(ACTIVE_IFACE_WITH_FILTER_2));
    BDD deniedOutIface2 =
        flows.and(mgr.deniedByOriginalFlowEgressFilter(ACTIVE_IFACE_WITH_FILTER_2));
    assertTrue(permittedOutIface2.isZero());
    assertFalse(deniedOutIface2.isZero());
  }

  @Test
  public void testBackwardConstraints() {
    /*
    Test that outgoingOriginalFlowFiltersConstraint correctly constrains backward flows:
    1. Start with three sets of flows that allegedly exited ACTIVE_IFACE_WITH_FILTER_1:
       a. Flows from srcIp1 -> DST_IP_1
       b. Flows from srcIp2 -> DST_IP_2
       c. Flows from srcIp3 -> DST_IP_2
    2. Create transformation that applies to flows from srcIp2 and transforms dst to DST_IP_2;
       transit this backwards
    3. Apply outgoingOriginalFlowFiltersConstraint to pre-transformation flows
    4. Should see:
       a. Flows from srcIp1 -> DST_IP_1; these would succeed out without getting transformed
       b. Flows from srcIp2 -> DST_IP_1; these would get transformed and succeed out
       c. No flows from srcIp3; these wouldn't get transformed and would be blocked by filter
     */
    Configuration c = createConfig(new NetworkFactory(), ALL_IFACES);
    BDDOutgoingInterfaceManager mgr = getMgrForConfig(c);

    Ip ip1 = Ip.parse("10.10.10.1");
    Ip ip2 = Ip.parse("10.10.10.2");
    Ip ip3 = Ip.parse("10.10.10.3");
    BDD srcIp1 = PKT.getSrcIp().value(ip1.asLong());
    BDD srcIp2 = PKT.getSrcIp().value(ip2.asLong());
    BDD srcIp3 = PKT.getSrcIp().value(ip3.asLong());
    BDD dstIp1 = PKT.getDstIp().value(DST_IP_1.asLong());
    BDD dstIp2 = PKT.getDstIp().value(DST_IP_2.asLong());

    BDD permittedOutIface1 = mgr.permittedByOriginalFlowEgressFilter(ACTIVE_IFACE_WITH_FILTER_1);
    Transformation transformation =
        Transformation.when(matchSrc(ip2.toIpSpace()))
            .apply(TransformationStep.assignDestinationIp(DST_IP_2))
            .build();
    Transition transformationTransition =
        new TransformationToTransition(
                PKT,
                new IpAccessListToBddImpl(
                    PKT, BDDSourceManager.empty(PKT), ImmutableMap.of(), ImmutableMap.of()))
            .toTransition(transformation);
    BDD originalFlowFiltersConstraint = mgr.outgoingOriginalFlowFiltersConstraint();

    // The "final" flows with which the backwards traversal begins
    BDD srcIp1ToDstIp1 = srcIp1.and(dstIp1).and(permittedOutIface1);
    BDD srcIp2ToDstIp2 = srcIp2.and(dstIp2).and(permittedOutIface1);
    BDD srcIp3ToDstIp2 = srcIp3.and(dstIp2).and(permittedOutIface1);
    BDD finalFlows = srcIp1ToDstIp1.or(srcIp2ToDstIp2).or(srcIp3ToDstIp2);

    BDD flows =
        transformationTransition.transitBackward(finalFlows); // flows with src srcIp2 untransform
    flows = flows.and(originalFlowFiltersConstraint); // track original flow outgoing filters

    // The final flows from srcIp1 should be present unchanged
    assertThat(flows, equalTo(flows.or(srcIp1ToDstIp1)));

    // The final flows from srcIp2 should be present, but now with DST_IP_1 instead of DST_IP_2
    BDD transformedFlowsFromIp2 = srcIp2.and(dstIp1).and(permittedOutIface1);
    assertThat(flows, equalTo(flows.or(transformedFlowsFromIp2)));

    // None of the final flows from srcIp3 should be present
    assertTrue(flows.and(srcIp3).isZero());
  }
}
