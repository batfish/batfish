package org.batfish.main;

import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressInterface;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.question.ReachFilterParameters;
import org.batfish.question.reachfilter.DifferentialReachFilterResult;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DifferentialReachFilterTest {
  @Rule public TemporaryFolder _tmp = new TemporaryFolder();

  private static final String HOSTNAME = "hostname";
  private static final String IFACE1 = "iface1";
  private static final Ip IP = new Ip("1.2.3.4");

  private NetworkFactory _nf;
  private Configuration.Builder _cb;
  private Interface.Builder _ib;
  private IpAccessList.Builder _ab;
  private ReachFilterParameters _params;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb =
        _nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _ib = _nf.interfaceBuilder().setActive(true);
    _ab = _nf.aclBuilder();
    _params =
        ReachFilterParameters.builder()
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .setSourceIpSpaceSpecifier(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .setHeaderSpace(HeaderSpace.builder().build())
            .build();
  }

  private Batfish getBatfish(Configuration baseConfig, Configuration deltaConfig)
      throws IOException {
    return BatfishTestUtils.getBatfish(
        ImmutableSortedMap.of(baseConfig.getHostname(), baseConfig),
        ImmutableSortedMap.of(deltaConfig.getHostname(), deltaConfig),
        _tmp);
  }

  @Test
  public void testMatchSrcInterface() throws IOException {
    Configuration baseConfig = _cb.build();
    Configuration deltaConfig = _cb.build();
    _ib.setName(IFACE1).setOwner(baseConfig).build();
    _ib.setOwner(deltaConfig).build();
    IpAccessList baseAcl = _ab.build();
    IpAccessList deltaAcl =
        _ab.setLines(
                ImmutableList.of(
                    accepting()
                        .setMatchCondition(and(matchSrcInterface(IFACE1), matchDst(IP)))
                        .build()))
            .build();
    Batfish batfish = getBatfish(baseConfig, deltaConfig);
    DifferentialReachFilterResult result =
        batfish.differentialReachFilter(baseConfig, baseAcl, deltaConfig, deltaAcl, _params);
    assertThat("Expected no decreased flow", !result.getDecreasedFlow().isPresent());
    assertThat("Expected increased flow", result.getIncreasedFlow().isPresent());
    assertThat(result.getIncreasedFlow().get(), allOf(hasIngressInterface(IFACE1), hasDstIp(IP)));

    // flip base and delta
    result = batfish.differentialReachFilter(deltaConfig, deltaAcl, baseConfig, baseAcl, _params);
    assertThat("Expected no increased flow", !result.getIncreasedFlow().isPresent());
    assertThat("Expected decreased flow", result.getDecreasedFlow().isPresent());
    assertThat(result.getDecreasedFlow().get(), allOf(hasIngressInterface(IFACE1), hasDstIp(IP)));
  }

  @Test
  public void testAclLineAddedRemoved() throws IOException {
    Configuration config = _cb.build();
    IpAccessList baseAcl = _ab.build();
    IpAccessList deltaAcl =
        _ab.setLines(ImmutableList.of(accepting().setMatchCondition(matchDst(IP)).build())).build();
    Batfish batfish = getBatfish(config, config);

    DifferentialReachFilterResult result =
        batfish.differentialReachFilter(config, baseAcl, config, deltaAcl, _params);
    assertThat("Expected no decreased flow", !result.getDecreasedFlow().isPresent());
    assertThat("Expected increased flow", result.getIncreasedFlow().isPresent());
    assertThat(result.getIncreasedFlow().get(), hasDstIp(IP));

    // flip base and delta ACL
    result = batfish.differentialReachFilter(config, deltaAcl, config, baseAcl, _params);
    assertThat("Expected no increased flow", !result.getIncreasedFlow().isPresent());
    assertThat("Expected decreased flow", result.getDecreasedFlow().isPresent());
    assertThat(result.getDecreasedFlow().get(), hasDstIp(IP));
  }
}
