package org.batfish.bddreachability.transition;

import static org.batfish.bddreachability.transition.Transitions.removeOutgoingInterfaceConstraints;
import static org.batfish.datamodel.ExprAclLine.REJECT_ALL;
import static org.batfish.datamodel.ExprAclLine.acceptingHeaderSpace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.BDDOutgoingOriginalFlowFilterManager;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link RemoveOutgoingInterfaceConstraints} */
public class RemoveOutgoingInterfaceConstraintsTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static final BDDPacket PKT = new BDDPacket();
  private static final BDD ONE = PKT.getFactory().one();

  private static final String IFACE = "iface";
  private static final Ip PERMITTED_DST_IP = Ip.parse("1.1.1.1");

  private static BDDOutgoingOriginalFlowFilterManager MGR;
  private static Transition TRANSITION;

  @BeforeClass
  public static void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();

    // Create ACL to use as outgoingOriginalFlowFilter that permits only PERMITTED_DST_IP
    AclLine permitDstIp =
        acceptingHeaderSpace(HeaderSpace.builder().setDstIps(PERMITTED_DST_IP.toIpSpace()).build());
    IpAccessList acl =
        nf.aclBuilder().setOwner(c).setLines(ImmutableList.of(permitDstIp, REJECT_ALL)).build();

    // Give c an interface with the above ACL as outgoingOriginalFlowFilter
    nf.interfaceBuilder().setOwner(c).setName(IFACE).setOutgoingOriginalFlowFilter(acl).build();

    // Create manager and corresponding RemoveOutgoingInterfaceConstraints transition
    MGR = getMgrForConfig(c);
    assert !MGR.isTrivial(); // sanity check
    TRANSITION = removeOutgoingInterfaceConstraints(MGR);
  }

  private static BDDOutgoingOriginalFlowFilterManager getMgrForConfig(Configuration c) {
    Map<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);
    Map<String, BDDSourceManager> srcMgrs = BDDSourceManager.forNetwork(PKT, configs);
    Map<String, BDDOutgoingOriginalFlowFilterManager> mgrs =
        BDDOutgoingOriginalFlowFilterManager.forNetwork(PKT, configs, srcMgrs);
    return mgrs.get(c.getHostname());
  }

  @Test
  public void testRemoveOutgoingInterfaceConstraints() {
    // Transiting forwards should remove interface constraints, including permit/deny var
    BDD outgoingOriginalFlowFiltersConstraint = MGR.outgoingOriginalFlowFiltersConstraint();
    BDD permittedOutIface = MGR.permittedByOriginalFlowEgressFilter(IFACE);
    BDD permittedDstIp = PKT.getDstIp().value(PERMITTED_DST_IP.asLong());
    assertThat(
        TRANSITION.transitForward(outgoingOriginalFlowFiltersConstraint.and(permittedOutIface)),
        equalTo(permittedDstIp));

    // Transiting backwards should have no effect
    assertThat(TRANSITION.transitBackward(ONE), equalTo(ONE));
  }

  @Test
  public void testConstructorThrowsForTrivialManager() {
    Configuration c =
        new NetworkFactory()
            .configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    BDDOutgoingOriginalFlowFilterManager trivialManager = getMgrForConfig(c);
    _thrown.expect(IllegalArgumentException.class);
    new RemoveOutgoingInterfaceConstraints(trivialManager);
  }

  @Test
  public void testTransitForwards_notConstrained() {
    // When transiting forwards, BDD must have manager's outgoingOriginalFlowFiltersConstraint
    _thrown.expect(AssertionError.class);
    TRANSITION.transitForward(ONE);
  }

  @Test
  public void testTransitBackwards_alreadyConstrained() {
    // When transiting backwards, BDD isn't allowed to have egress interface constraints
    BDD permittedOutIface = MGR.permittedByOriginalFlowEgressFilter(IFACE);
    _thrown.expect(AssertionError.class);
    TRANSITION.transitBackward(permittedOutIface);
  }
}
