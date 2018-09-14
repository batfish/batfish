package org.batfish.symbolic.bdd;

import static org.batfish.common.bdd.BDDOps.orNull;
import static org.batfish.datamodel.IpAccessListLine.ACCEPT_ALL;
import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.IpAccessListLine.rejecting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.SOURCE_ORIGINATING_FROM_DEVICE;
import static org.batfish.symbolic.bdd.BDDMatchers.isZero;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.junit.Test;

public class BDDSourceManagerTest {
  private static final String IFACE1 = "iface1";

  private static final String IFACE2 = "iface2";

  private static final Set<String> IFACES = ImmutableSet.of(IFACE1, IFACE2);

  BDDPacket _pkt = new BDDPacket();

  Configuration _cfg;

  IpAccessList _acl;

  private void setupAclAndConfig() {
    NetworkFactory nf = new NetworkFactory();
    _cfg = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Interface.Builder ib = nf.interfaceBuilder().setOwner(_cfg);
    ib.setName(IFACE1).build();
    ib.setName(IFACE2).build();
    String iface3 = "iface3";
    ib.setName(iface3).build();
    String iface4 = "iface4";
    ib.setName(iface4).setActive(false).build();

    // an ACL that can only match with an IFACE2 or iface3
    _acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(
                ImmutableList.of(
                    accepting().setMatchCondition(matchSrcInterface(IFACE1)).build(),
                    accepting().setMatchCondition(ORIGINATING_FROM_DEVICE).build(),
                    rejecting().setMatchCondition(matchSrcInterface(iface4)).build(),
                    ACCEPT_ALL))
            .build();
  }

  @Test
  public void test() {
    BDDSourceManager mgr = BDDSourceManager.forInterfaces(_pkt, IFACES);
    BDD deviceBDD = mgr.getOriginatingFromDeviceBDD();
    BDD bdd1 = mgr.getSourceInterfaceBDD(IFACE1);
    BDD bdd2 = mgr.getSourceInterfaceBDD(IFACE2);
    assertThat(mgr.getSourceFromAssignment(bdd1), equalTo(Optional.of(IFACE1)));
    assertThat(mgr.getSourceFromAssignment(bdd2), equalTo(Optional.of(IFACE2)));
    assertThat(mgr.getSourceFromAssignment(deviceBDD), equalTo(Optional.empty()));
  }

  @Test
  public void testSane() {
    BDDSourceManager mgr = BDDSourceManager.forInterfaces(_pkt, IFACES);
    BDD noSource =
        orNull(
                mgr.getOriginatingFromDeviceBDD(),
                mgr.getSourceInterfaceBDD(IFACE1),
                mgr.getSourceInterfaceBDD(IFACE2))
            .not();
    assertThat(mgr.isSane().and(noSource), isZero());
  }

  @Test
  public void testForIpAccessList() {
    setupAclAndConfig();
    BDDSourceManager mgr =
        BDDSourceManager.forIpAccessList(
            _pkt, true, _cfg.activeInterfaces(), _cfg.getIpAccessLists(), _acl);
    Map<String, BDD> srcBDDs = mgr.getSourceBDDs();
    // 3 entries: one for each referenced and active source, plus one unreferenced active source.
    assertThat(srcBDDs.entrySet(), hasSize(3));
    assertThat(srcBDDs, hasEntry(equalTo(IFACE1), not(isZero())));
    assertThat(srcBDDs, hasEntry(equalTo(IFACE2), not(isZero())));
    assertThat(srcBDDs, hasEntry(equalTo(SOURCE_ORIGINATING_FROM_DEVICE), not(isZero())));
  }

  @Test
  public void testDisallowOriginatingFromDevice() {
    setupAclAndConfig();
    BDDSourceManager mgr =
        BDDSourceManager.forIpAccessList(
            _pkt, false, _cfg.activeInterfaces(), _cfg.getIpAccessLists(), _acl);
    Map<String, BDD> srcBDDs = mgr.getSourceBDDs();
    assertThat(srcBDDs.entrySet(), hasSize(2));
    assertThat(srcBDDs, hasEntry(equalTo(IFACE1), not(isZero())));
    assertThat(srcBDDs, hasEntry(equalTo(IFACE2), not(isZero())));
  }
}
