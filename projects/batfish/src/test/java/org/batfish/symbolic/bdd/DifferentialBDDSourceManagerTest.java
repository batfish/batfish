package org.batfish.symbolic.bdd;

import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.junit.Before;
import org.junit.Test;

public class DifferentialBDDSourceManagerTest {
  private static final String HOSTNAME = "hostname";
  private static final String IFACE1 = "iface1";
  private BDDPacket _pkt = new BDDPacket();
  private NetworkFactory _nf;
  private Configuration.Builder _cb;
  private Interface.Builder _ib;
  private IpAccessList.Builder _ab;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb =
        _nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _ib = _nf.interfaceBuilder().setActive(true);
    _ab = _nf.aclBuilder();
  }

  @Test
  public void testInterfaceDeactived() {
    Configuration baseConfig = _cb.build();
    Configuration deltaConfig = _cb.build();
    _ib.setName(IFACE1).setOwner(baseConfig).build();
    _ib.setOwner(deltaConfig).setActive(false).build();
    IpAccessList acl =
        _ab.setLines(
                ImmutableList.of(accepting().setMatchCondition(matchSrcInterface(IFACE1)).build()))
            .build();

    DifferentialBDDSourceManager diffMgr =
        DifferentialBDDSourceManager.forAcls(_pkt, baseConfig, acl, deltaConfig, acl);
    BDDSourceManager mgr = diffMgr.getBddSourceManager();
    assertThat(diffMgr.getBaseSane(), equalTo(mgr.isSane()));
    assertThat(
        diffMgr.getDeltaSane(), equalTo(mgr.isSane().and(mgr.getSourceInterfaceBDD(IFACE1).not())));
  }

  @Test
  public void testInterfaceRemoved() {
    Configuration baseConfig = _cb.build();
    Configuration deltaConfig = _cb.build();
    _ib.setName(IFACE1).setOwner(baseConfig).build();
    IpAccessList acl =
        _ab.setLines(
                ImmutableList.of(accepting().setMatchCondition(matchSrcInterface(IFACE1)).build()))
            .build();

    DifferentialBDDSourceManager diffMgr =
        DifferentialBDDSourceManager.forAcls(_pkt, baseConfig, acl, deltaConfig, acl);
    BDDSourceManager mgr = diffMgr.getBddSourceManager();
    assertThat(diffMgr.getBaseSane(), equalTo(mgr.isSane()));
    assertThat(
        diffMgr.getDeltaSane(), equalTo(mgr.isSane().and(mgr.getSourceInterfaceBDD(IFACE1).not())));
  }
}
