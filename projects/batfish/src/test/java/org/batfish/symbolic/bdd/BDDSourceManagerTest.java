package org.batfish.symbolic.bdd;

import static org.batfish.datamodel.IpAccessListLine.ACCEPT_ALL;
import static org.batfish.datamodel.IpAccessListLine.rejecting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.symbolic.bdd.BDDMatchers.isZero;
import static org.batfish.symbolic.bdd.BDDOps.orNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
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

  private static final List<String> IFACES = ImmutableList.of(IFACE1, IFACE2);

  BDDPacket _pkt = new BDDPacket();

  BDDSourceManager _mgr = BDDSourceManager.forInterfaces(_pkt, IFACES);

  @Test
  public void test() {
    BDD bdd1 = _mgr.getSrcInterfaceBDD(IFACE1);
    BDD bdd2 = _mgr.getSrcInterfaceBDD(IFACE2);
    assertThat(_mgr.getInterfaceFromAssignment(bdd1), equalTo(Optional.of(IFACE1)));
    assertThat(_mgr.getInterfaceFromAssignment(bdd2), equalTo(Optional.of(IFACE2)));
    assertThat(
        _mgr.getInterfaceFromAssignment(_mgr.getSrcInterfaceVar().value(0)),
        equalTo(Optional.empty()));
  }

  @Test
  public void testSane() {
    BDD noSource =
        orNull(
                _mgr.getOriginatingFromDeviceBDD(),
                _mgr.getSrcInterfaceBDD(IFACE1),
                _mgr.getSrcInterfaceBDD(IFACE2))
            .not();
    assertThat(_mgr.isSane().and(noSource), isZero());
  }

  @Test
  public void testForIpAccessList() {
    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Interface.Builder ib = nf.interfaceBuilder().setOwner(config);
    ib.setName(IFACE1).build();
    ib.setName(IFACE2).build();
    String iface3 = "iface3";
    ib.setName(iface3).build();

    // an ACL that can only match with an IFACE2 or iface3
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(
                ImmutableList.of(
                    rejecting().setMatchCondition(matchSrcInterface(IFACE1)).build(), ACCEPT_ALL))
            .build();

    List<String> trackedIfaces = BDDSourceManager.interfacesForIpAccessList(config, acl);
    assertThat(trackedIfaces, hasSize(2));
    assertThat(trackedIfaces, hasItem(IFACE1));
    assertThat(trackedIfaces, anyOf(hasItem(IFACE2), hasItem(iface3)));
  }
}
