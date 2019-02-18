package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDOps.orNull;
import static org.batfish.datamodel.IpAccessListLine.ACCEPT_ALL;
import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.IpAccessListLine.rejecting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.junit.Test;

/** Tests of {@link BDDSourceManager}. */
public class BDDSourceManagerTest {
  private static final String IFACE1 = "iface1";
  private static final String IFACE2 = "iface2";
  private static final String IFACE3 = "iface3";
  private static final String IFACE4 = "iface4";

  private static final Set<String> IFACES_1_2 = ImmutableSet.of(IFACE1, IFACE2);

  private BDDPacket _pkt = new BDDPacket();

  @Test
  public void test() {
    BDDSourceManager mgr = BDDSourceManager.forInterfaces(_pkt, IFACES_1_2);
    BDD deviceBDD = mgr.getOriginatingFromDeviceBDD();
    BDD bdd1 = mgr.getSourceInterfaceBDD(IFACE1);
    BDD bdd2 = mgr.getSourceInterfaceBDD(IFACE2);
    assertThat(mgr.getSourceFromAssignment(bdd1), equalTo(Optional.of(IFACE1)));
    assertThat(mgr.getSourceFromAssignment(bdd2), equalTo(Optional.of(IFACE2)));
    assertThat(mgr.getSourceFromAssignment(deviceBDD), equalTo(Optional.empty()));
  }

  @Test
  public void testSane() {
    BDDSourceManager mgr = BDDSourceManager.forInterfaces(_pkt, IFACES_1_2);
    BDD noSource =
        orNull(
                mgr.getOriginatingFromDeviceBDD(),
                mgr.getSourceInterfaceBDD(IFACE1),
                mgr.getSourceInterfaceBDD(IFACE2))
            .not();
    assertThat(mgr.isValidValue().and(noSource), BDDMatchers.isZero());
  }

  @Test
  public void testNoReferencedSources() {
    BDDSourceManager mgr = BDDSourceManager.forSources(_pkt, IFACES_1_2, ImmutableSet.of());
    assertTrue(mgr.getSourceInterfaceBDD(IFACE1).isOne());
    assertTrue(mgr.getSourceInterfaceBDD(IFACE2).isOne());
  }

  private static Configuration configWithOneAcl(NetworkFactory nf) {
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Interface.Builder ib = nf.interfaceBuilder().setOwner(config);
    ib.setName(IFACE1).build();
    ib.setName(IFACE2).build();
    ib.setName(IFACE3).build();
    ib.setName(IFACE4).setActive(false).build();

    // an ACL that can only match with an IFACE2 or iface3
    IpAccessList.builder()
        .setName("acl")
        .setOwner(config)
        .setLines(
            ImmutableList.of(
                accepting().setMatchCondition(matchSrcInterface(IFACE1)).build(),
                rejecting().setMatchCondition(matchSrcInterface(IFACE4)).build(),
                ACCEPT_ALL))
        .build();
    return config;
  }

  @Test
  public void testForIpAccessList() {
    NetworkFactory nf = new NetworkFactory();
    Configuration config = configWithOneAcl(nf);
    IpAccessList acl = config.getIpAccessLists().values().iterator().next();

    BDDSourceManager mgr = BDDSourceManager.forIpAccessList(_pkt, config, acl);

    BDD iface1BDD = mgr.getSourceInterfaceBDD(IFACE1);
    BDD iface2BDD = mgr.getSourceInterfaceBDD(IFACE2);
    BDD iface3BDD = mgr.getSourceInterfaceBDD(IFACE3);
    BDD origBDD = mgr.getOriginatingFromDeviceBDD();

    assertThat(
        "BDDs for IFACE1 and IFACE2 should be different", iface1BDD, not(equalTo(iface2BDD)));
    assertThat(
        "BDDs for all sources other than IFACE1 should be the same",
        iface2BDD,
        allOf(equalTo(iface3BDD), equalTo(origBDD)));
  }

  @Test
  public void testForNetwork() {
    /*
     * Create a network with two configs. Both have two interfaces and
     */
    NetworkFactory nf = new NetworkFactory();
    Configuration config1 = configWithOneAcl(nf);
    Configuration config2 = configWithOneAcl(nf);
    Map<String, Configuration> network =
        ImmutableMap.of(config1.getHostname(), config1, config2.getHostname(), config2);

    Map<String, BDDSourceManager> bddSourceManagers = BDDSourceManager.forNetwork(_pkt, network);
    BDDSourceManager mgr1 = bddSourceManagers.get(config1.getHostname());
    BDDSourceManager mgr2 = bddSourceManagers.get(config2.getHostname());

    /*
     * The two managers use the same BDD values to track sources.
     */
    assertThat(mgr1.getSourceBDDs().values(), equalTo(mgr2.getSourceBDDs().values()));
  }
}
