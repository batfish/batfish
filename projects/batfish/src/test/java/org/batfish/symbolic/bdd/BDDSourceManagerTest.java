package org.batfish.symbolic.bdd;

import static org.batfish.datamodel.IpAccessListLine.ACCEPT_ALL;
import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.IpAccessListLine.rejecting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.SOURCE_ORIGINATING_FROM_DEVICE;
import static org.batfish.symbolic.bdd.BDDMatchers.isZero;
import static org.batfish.symbolic.bdd.BDDOps.orNull;
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

  BDDSourceManager _mgr = BDDSourceManager.forInterfaces(_pkt, IFACES);

  @Test
  public void test() {
    BDD deviceBDD = _mgr.getOriginatingFromDeviceBDD();
    BDD bdd1 = _mgr.getSourceInterfaceBDD(IFACE1);
    BDD bdd2 = _mgr.getSourceInterfaceBDD(IFACE2);
    assertThat(_mgr.getSourceFromAssignment(bdd1), equalTo(Optional.of(IFACE1)));
    assertThat(_mgr.getSourceFromAssignment(bdd2), equalTo(Optional.of(IFACE2)));
    assertThat(_mgr.getSourceFromAssignment(deviceBDD), equalTo(Optional.empty()));
  }

  @Test
  public void testSane() {
    BDD noSource =
        orNull(
                _mgr.getOriginatingFromDeviceBDD(),
                _mgr.getSourceInterfaceBDD(IFACE1),
                _mgr.getSourceInterfaceBDD(IFACE2))
            .not();
    assertThat(_mgr.isSane().and(noSource), isZero());
  }

  @Test
  public void testForIpAccessList() {
    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Interface.Builder ib = nf.interfaceBuilder().setOwner(config).setActive(true);
    ib.setName(IFACE1).build();
    ib.setName(IFACE2).build();
    String iface3 = "iface3";
    ib.setName(iface3).build();
    String iface4 = "iface4";
    ib.setName(iface4).setActive(false).build();

    // an ACL that can only match with an IFACE2 or iface3
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(
                ImmutableList.of(
                    accepting().setMatchCondition(matchSrcInterface(IFACE1)).build(),
                    rejecting().setMatchCondition(matchSrcInterface(iface4)).build(),
                    ACCEPT_ALL))
            .build();

    BDDSourceManager mgr = BDDSourceManager.forIpAccessList(_pkt, config, acl);
    Map<String, BDD> srcBDDs = mgr.getSourceBDDs();
    assertThat(srcBDDs.entrySet(), hasSize(2));
    assertThat(srcBDDs, hasEntry(equalTo(IFACE1), not(isZero())));
    assertThat(srcBDDs, hasEntry(equalTo(SOURCE_ORIGINATING_FROM_DEVICE), not(isZero())));
  }
}
