package org.batfish.question.searchfilters;

import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.question.searchfilters.SearchFiltersAnswerer.differentialReachFilter;
import static org.batfish.specifier.LocationSpecifiers.ALL_LOCATIONS;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.matchers.FlowMatchers;
import org.batfish.main.BatfishFactory;
import org.batfish.question.SearchFiltersParameters;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.NameRegexInterfaceLinkLocationSpecifier;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of {@link SearchFiltersAnswerer} */
public class SearchFiltersAnswererDifferentialTest {
  @Rule public TemporaryFolder _tmp = new TemporaryFolder();

  private static final String HOSTNAME = "hostname";
  private static final String IFACE1 = "iface1";
  private static final String IFACE2 = "iface2";
  private static final Ip IP = Ip.parse("1.2.3.4");

  private NetworkFactory _nf;
  private Configuration.Builder _cb;
  private Interface.Builder _ib;
  private IpAccessList.Builder _ab;
  private SearchFiltersParameters _params;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb =
        _nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _ib = _nf.interfaceBuilder();
    _ab = _nf.aclBuilder();
    _params =
        SearchFiltersParameters.builder()
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .setSourceIpSpaceSpecifier(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .setStartLocationSpecifier(ALL_LOCATIONS)
            .setHeaderSpace(HeaderSpace.builder().build())
            .build();
  }

  private IBatfish getBatfish(Configuration baseConfig, Configuration deltaConfig)
      throws IOException {
    return BatfishFactory.load()
        .getBatfish(
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
    String aclName = "aclName";
    IpAccessList baseAcl = _ab.setName(aclName).setOwner(baseConfig).build();
    IpAccessList deltaAcl =
        _ab.setOwner(deltaConfig)
            .setLines(
                ImmutableList.of(
                    accepting()
                        .setMatchCondition(and(matchSrcInterface(IFACE1), matchDst(IP)))
                        .build()))
            .build();
    IBatfish batfish = getBatfish(baseConfig, deltaConfig);

    DifferentialSearchFiltersResult result =
        differentialReachFilter(batfish, baseConfig, baseAcl, deltaConfig, deltaAcl, _params);
    MatcherAssert.assertThat(
        "Expected no decreased result", !result.getDecreasedResult().isPresent());
    MatcherAssert.assertThat("Expected increased result", result.getIncreasedResult().isPresent());
    MatcherAssert.assertThat(
        result.getIncreasedResult().get().getExampleFlow(),
        Matchers.allOf(FlowMatchers.hasIngressInterface(IFACE1), FlowMatchers.hasDstIp(IP)));

    // flip base and delta
    result = differentialReachFilter(batfish, deltaConfig, deltaAcl, baseConfig, baseAcl, _params);
    MatcherAssert.assertThat(
        "Expected no increased result", !result.getIncreasedResult().isPresent());
    MatcherAssert.assertThat("Expected decreased result", result.getDecreasedResult().isPresent());
    MatcherAssert.assertThat(
        result.getDecreasedResult().get().getExampleFlow(),
        Matchers.allOf(FlowMatchers.hasIngressInterface(IFACE1), FlowMatchers.hasDstIp(IP)));
  }

  @Test
  public void testAclLineAddedRemoved() throws IOException {
    Configuration config = _cb.build();
    IpAccessList baseAcl = _ab.setOwner(config).build();
    IpAccessList deltaAcl =
        _ab.setLines(ImmutableList.of(accepting().setMatchCondition(matchDst(IP)).build())).build();
    IBatfish batfish = getBatfish(config, config);

    DifferentialSearchFiltersResult result =
        differentialReachFilter(batfish, config, baseAcl, config, deltaAcl, _params);
    MatcherAssert.assertThat(
        "Expected no decreased result", !result.getDecreasedResult().isPresent());
    MatcherAssert.assertThat("Expected increased result", result.getIncreasedResult().isPresent());
    MatcherAssert.assertThat(
        result.getIncreasedResult().get().getExampleFlow(), FlowMatchers.hasDstIp(IP));
    MatcherAssert.assertThat(
        "No explanation generated by default",
        !result.getIncreasedResult().get().getHeaderSpaceDescription().isPresent());

    // flip base and delta ACL; turn on explanations
    result =
        differentialReachFilter(
            batfish,
            config,
            deltaAcl,
            config,
            baseAcl,
            _params.toBuilder().setGenerateExplanations(true).build());
    MatcherAssert.assertThat(
        "Expected no increased result", !result.getIncreasedResult().isPresent());
    MatcherAssert.assertThat("Expected decreased result", result.getDecreasedResult().isPresent());
    MatcherAssert.assertThat(
        result.getDecreasedResult().get().getExampleFlow(), FlowMatchers.hasDstIp(IP));
    MatcherAssert.assertThat(
        result.getDecreasedResult().get().getHeaderSpaceDescription().get(),
        Matchers.equalTo(matchDst(IP)));
  }

  @Test
  public void testSourceInterfaceParameter() throws IOException {
    Configuration baseConfig = _cb.build();
    Configuration deltaConfig = _cb.build();
    _ib.setName(IFACE1).setOwner(baseConfig).build();
    _ib.setOwner(deltaConfig).build();
    _ib.setName(IFACE2).setOwner(baseConfig).build();
    _ib.setOwner(deltaConfig).build();
    String aclName = "acl";
    IpAccessList baseAcl = _ab.setName(aclName).setOwner(baseConfig).build();
    IpAccessList deltaAcl =
        _ab.setOwner(deltaConfig)
            .setLines(
                ImmutableList.of(
                    accepting()
                        .setMatchCondition(and(matchSrcInterface(IFACE1), matchDst(IP)))
                        .build()))
            .build();

    IBatfish batfish = getBatfish(baseConfig, deltaConfig);
    SearchFiltersParameters params =
        _params
            .toBuilder()
            .setStartLocationSpecifier(new NameRegexInterfaceLinkLocationSpecifier(IFACE1))
            .build();

    // can match line 1 because IFACE1 is specified
    DifferentialSearchFiltersResult result =
        differentialReachFilter(batfish, baseConfig, baseAcl, deltaConfig, deltaAcl, params);
    MatcherAssert.assertThat(
        "Expected no decreased result", !result.getDecreasedResult().isPresent());
    MatcherAssert.assertThat("Expected increased result", result.getIncreasedResult().isPresent());
    MatcherAssert.assertThat(
        result.getIncreasedResult().get().getExampleFlow(),
        Matchers.allOf(FlowMatchers.hasIngressInterface(IFACE1), FlowMatchers.hasDstIp(IP)));

    params =
        _params
            .toBuilder()
            .setStartLocationSpecifier(new NameRegexInterfaceLinkLocationSpecifier(IFACE2))
            .build();

    // not can't match line 1 because IFACE2 is specified
    result = differentialReachFilter(batfish, baseConfig, baseAcl, deltaConfig, deltaAcl, params);
    MatcherAssert.assertThat(
        "Expected no decreased result", !result.getDecreasedResult().isPresent());
    MatcherAssert.assertThat(
        "Expected no increased result", !result.getIncreasedResult().isPresent());
  }
}
