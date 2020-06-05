package org.batfish.bddreachability.transition;

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

/** Tests of {@link AddOutgoingOriginalFlowFiltersConstraint} */
public class AddOutgoingOriginalFlowFiltersConstraintTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static final BDDPacket PKT = new BDDPacket();
  private static final BDD ONE = PKT.getFactory().one();

  private static final String IFACE = "iface";
  private static final Ip PERMITTED_DST_IP = Ip.parse("1.1.1.1");

  private static BDDOutgoingOriginalFlowFilterManager NONTRIVIAL_MGR;
  private static BDDOutgoingOriginalFlowFilterManager TRIVIAL_MGR;
  private static AddOutgoingOriginalFlowFiltersConstraint NONTRIVIAL_TRANSITION;
  private static AddOutgoingOriginalFlowFiltersConstraint TRIVIAL_TRANSITION;

  @BeforeClass
  public static void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Configuration c2 =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();

    // Create ACL to use as outgoingOriginalFlowFilter that permits only PERMITTED_DST_IP
    AclLine permitDstIp =
        acceptingHeaderSpace(HeaderSpace.builder().setDstIps(PERMITTED_DST_IP.toIpSpace()).build());
    IpAccessList acl =
        nf.aclBuilder().setOwner(c1).setLines(ImmutableList.of(permitDstIp, REJECT_ALL)).build();

    // Give c1 an interface with the above ACL as outgoingOriginalFlowFilter
    nf.interfaceBuilder().setOwner(c1).setName(IFACE).setOutgoingOriginalFlowFilter(acl).build();

    // Create managers and corresponding transitions for both configs
    Map<String, Configuration> configs =
        ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
    Map<String, BDDSourceManager> srcMgrs = BDDSourceManager.forNetwork(PKT, configs);
    Map<String, BDDOutgoingOriginalFlowFilterManager> mgrs =
        BDDOutgoingOriginalFlowFilterManager.forNetwork(PKT, configs, srcMgrs);
    NONTRIVIAL_MGR = mgrs.get(c1.getHostname());
    TRIVIAL_MGR = mgrs.get(c2.getHostname());
    NONTRIVIAL_TRANSITION = new AddOutgoingOriginalFlowFiltersConstraint(NONTRIVIAL_MGR);
    TRIVIAL_TRANSITION = new AddOutgoingOriginalFlowFiltersConstraint(TRIVIAL_MGR);

    // Sanity check
    assert !NONTRIVIAL_MGR.isTrivial();
    assert TRIVIAL_MGR.isTrivial();
  }

  @Test
  public void testAddOriginalFlowEgressFiltersConstraint_nontrivialManager() {
    // Transiting forwards should add manager's outgoingOriginalFlowFiltersConstraint.
    assertThat(
        NONTRIVIAL_TRANSITION.transitForward(ONE),
        equalTo(NONTRIVIAL_MGR.outgoingOriginalFlowFiltersConstraint()));

    // Transit backwards with a flow that was permitted by an egress interface whose
    // originalFlowOutgoingFilter only permits PERMITTED_DST_IP. Transiting backwards should apply
    // the manager's outgoingOriginalFlowFiltersConstraint and then erase egress interface
    // constraints, so we should end up with a BDD of PERMITTED_DST_IP.
    BDD permittedDstIp = PKT.getDstIp().value(PERMITTED_DST_IP.asLong());
    BDD permitOutIfaceWithFilter = NONTRIVIAL_MGR.permittedByOriginalFlowEgressFilter(IFACE);
    assertThat(
        NONTRIVIAL_TRANSITION.transitBackward(permitOutIfaceWithFilter), equalTo(permittedDstIp));

    // Transit backwards with unconstrained flow. This is allowed and shouldn't have any effect.
    assertThat(NONTRIVIAL_TRANSITION.transitBackward(ONE), equalTo(ONE));
  }

  @Test
  public void testAddOriginalFlowEgressFiltersConstraint_trivialManager() {
    // Transiting forwards should have no effect.
    assertThat(TRIVIAL_TRANSITION.transitForward(ONE), equalTo(ONE));

    // Transiting backwards should still clear interface constraints. Use nontrivial mgr to generate
    // a starting BDD that does have an interface constraint.
    BDD permitOutIfaceWithFilter = NONTRIVIAL_MGR.permittedByOriginalFlowEgressFilter(IFACE);
    assertThat(TRIVIAL_TRANSITION.transitBackward(permitOutIfaceWithFilter), equalTo(ONE));
  }

  @Test
  public void testTransitForward_alreadyConstrained() {
    // When transiting forwards, BDD isn't allowed to have egress interface constraints
    BDD permitOutIfaceWithFilter = NONTRIVIAL_MGR.permittedByOriginalFlowEgressFilter(IFACE);
    _thrown.expect(AssertionError.class);
    TRIVIAL_TRANSITION.transitForward(permitOutIfaceWithFilter);
  }
}
