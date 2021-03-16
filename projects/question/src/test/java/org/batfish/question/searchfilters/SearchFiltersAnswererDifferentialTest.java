package org.batfish.question.searchfilters;

import static org.batfish.datamodel.ExprAclLine.accepting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressInterface;
import static org.batfish.question.searchfilters.SearchFiltersAnswerer.getDiffResult;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.question.SearchFiltersParameters;
import org.batfish.question.searchfilters.SearchFiltersAnswerer.DiffConfigContext;
import org.batfish.specifier.NameRegexInterfaceLinkLocationSpecifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of {@link SearchFiltersAnswerer} */
public class SearchFiltersAnswererDifferentialTest {
  @Rule public TemporaryFolder _tmp = new TemporaryFolder();

  private final BDDPacket _pkt = new BDDPacket();
  private static final String HOSTNAME = "hostname";
  private static final String IFACE1 = "iface1";
  private static final String IFACE2 = "iface2";
  private static final Ip IP = Ip.parse("1.2.3.4");
  private static final SearchFiltersQuery PERMIT_QUERY = PermitQuery.INSTANCE;
  private static final SearchFiltersParameters DEFAULT_PARAMS =
      new SearchFiltersQuestion().toSearchFiltersParameters();

  private Configuration.Builder _cb;
  private Interface.Builder _ib;
  private IpAccessList.Builder _ab;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    _cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _ib = nf.interfaceBuilder();
    _ab = nf.aclBuilder();
  }

  private static IBatfish getBatfish(Configuration baseConfig, Configuration deltaConfig) {
    return new MockBatfish(baseConfig, deltaConfig);
  }

  @Test
  public void testMatchSrcInterface() {
    Configuration config = _cb.build();
    Configuration refConfig = _cb.build();
    _ib.setName(IFACE1).setOwner(config).build();
    _ib.setOwner(refConfig).build();
    String aclName = "aclName";
    IpAccessList refAcl = _ab.setName(aclName).setOwner(config).build();
    IpAccessList acl =
        _ab.setOwner(refConfig)
            .setLines(
                ImmutableList.of(
                    accepting()
                        .setMatchCondition(and(matchSrcInterface(IFACE1), matchDst(IP)))
                        .build()))
            .build();
    IBatfish batfish = getBatfish(config, refConfig);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    NetworkSnapshot reference = batfish.getReferenceSnapshot();

    DiffConfigContext configContext =
        new DiffConfigContext(
            config,
            refConfig,
            ImmutableSet.of(aclName),
            snapshot,
            reference,
            batfish,
            DEFAULT_PARAMS,
            _pkt);
    DifferentialSearchFiltersResult result =
        getDiffResult(acl, refAcl, configContext, PERMIT_QUERY);
    assertFalse("Expected no decreased result", result.getDecreasedFlow().isPresent());
    assertTrue("Expected increased result", result.getIncreasedFlow().isPresent());
    assertThat(result.getIncreasedFlow().get(), allOf(hasIngressInterface(IFACE1), hasDstIp(IP)));

    // flip base and delta
    configContext =
        new DiffConfigContext(
            refConfig,
            config,
            ImmutableSet.of(aclName),
            reference,
            snapshot,
            getBatfish(refConfig, config),
            DEFAULT_PARAMS,
            _pkt);
    result = getDiffResult(refAcl, acl, configContext, PERMIT_QUERY);
    assertFalse("Expected no increased result", result.getIncreasedFlow().isPresent());
    assertTrue("Expected decreased result", result.getDecreasedFlow().isPresent());
    assertThat(result.getDecreasedFlow().get(), allOf(hasIngressInterface(IFACE1), hasDstIp(IP)));
  }

  @Test
  public void testAclLineAddedRemoved() {
    Configuration config = _cb.build();
    Configuration refConfig = _cb.build();
    String aclName = "aclName";
    IpAccessList refAcl = _ab.setName(aclName).setOwner(refConfig).build();
    IpAccessList acl =
        _ab.setOwner(config)
            .setLines(ImmutableList.of(accepting().setMatchCondition(matchDst(IP)).build()))
            .build();
    IBatfish batfish = getBatfish(config, refConfig);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    NetworkSnapshot reference = batfish.getReferenceSnapshot();

    DiffConfigContext configContext =
        new DiffConfigContext(
            config,
            refConfig,
            ImmutableSet.of(aclName),
            snapshot,
            reference,
            batfish,
            DEFAULT_PARAMS,
            _pkt);
    DifferentialSearchFiltersResult result =
        getDiffResult(acl, refAcl, configContext, PERMIT_QUERY);
    assertFalse("Expected no decreased result", result.getDecreasedFlow().isPresent());
    assertTrue("Expected increased result", result.getIncreasedFlow().isPresent());
    assertThat(result.getIncreasedFlow().get(), hasDstIp(IP));

    // flip base and delta ACL
    configContext =
        new DiffConfigContext(
            refConfig,
            config,
            ImmutableSet.of(aclName),
            reference,
            snapshot,
            getBatfish(refConfig, config),
            DEFAULT_PARAMS,
            _pkt);
    result = getDiffResult(refAcl, acl, configContext, PERMIT_QUERY);
    assertFalse("Expected no increased result", result.getIncreasedFlow().isPresent());
    assertTrue("Expected decreased result", result.getDecreasedFlow().isPresent());
    assertThat(result.getDecreasedFlow().get(), hasDstIp(IP));
  }

  @Test
  public void testSourceInterfaceParameter() {
    Configuration refConfig = _cb.build();
    Configuration config = _cb.build();
    _ib.setName(IFACE1).setOwner(refConfig).build();
    _ib.setOwner(config).build();
    _ib.setName(IFACE2).setOwner(refConfig).build();
    _ib.setOwner(config).build();
    String aclName = "acl";
    IpAccessList refAcl = _ab.setName(aclName).setOwner(refConfig).build();
    IpAccessList acl =
        _ab.setOwner(config)
            .setLines(
                ImmutableList.of(
                    accepting()
                        .setMatchCondition(and(matchSrcInterface(IFACE1), matchDst(IP)))
                        .build()))
            .build();

    IBatfish batfish = getBatfish(refConfig, config);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    NetworkSnapshot reference = batfish.getReferenceSnapshot();
    SearchFiltersParameters params =
        DEFAULT_PARAMS.toBuilder()
            .setStartLocationSpecifier(new NameRegexInterfaceLinkLocationSpecifier(IFACE1))
            .build();

    // can match line 1 because IFACE1 is specified
    DiffConfigContext configContext =
        new DiffConfigContext(
            config,
            refConfig,
            ImmutableSet.of(aclName),
            snapshot,
            reference,
            batfish,
            params,
            _pkt);
    DifferentialSearchFiltersResult result =
        getDiffResult(acl, refAcl, configContext, PERMIT_QUERY);
    assertFalse("Expected no decreased result", result.getDecreasedFlow().isPresent());
    assertTrue("Expected increased result", result.getIncreasedFlow().isPresent());
    assertThat(result.getIncreasedFlow().get(), allOf(hasIngressInterface(IFACE1), hasDstIp(IP)));

    params =
        DEFAULT_PARAMS.toBuilder()
            .setStartLocationSpecifier(new NameRegexInterfaceLinkLocationSpecifier(IFACE2))
            .build();

    // can't match line 1 because IFACE2 is specified
    configContext =
        new DiffConfigContext(
            config,
            refConfig,
            ImmutableSet.of(aclName),
            snapshot,
            reference,
            batfish,
            params,
            _pkt);
    result = getDiffResult(acl, refAcl, configContext, PERMIT_QUERY);
    assertFalse("Expected no decreased result", result.getDecreasedFlow().isPresent());
    assertFalse("Expected no increased result", result.getIncreasedFlow().isPresent());
  }
}
