package org.batfish.question.searchfilters;

import static org.batfish.datamodel.ExprAclLine.ACCEPT_ALL;
import static org.batfish.datamodel.ExprAclLine.accepting;
import static org.batfish.datamodel.ExprAclLine.rejecting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstPort;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressInterface;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcPort;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_ACTION;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_FILTER_NAME;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface.Builder;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.question.SearchFiltersParameters;
import org.batfish.question.searchfilters.SearchFiltersAnswerer.NonDiffConfigContext;
import org.batfish.question.testfilters.TestFiltersAnswerer;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NameRegexInterfaceLinkLocationSpecifier;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of {@link org.batfish.question.searchfilters}. */
public final class SearchFiltersTest {
  private static final String IFACE1 = "iface1";
  private static final String IFACE2 = "iface2";

  private static final Ip IP0 = Ip.parse("1.1.1.0");
  private static final Ip IP1 = Ip.parse("1.1.1.1");
  private static final Ip IP2 = Ip.parse("1.1.1.2");
  private static final Ip IP3 = Ip.parse("1.1.1.3");

  private static final PermitQuery PERMIT_QUERY = PermitQuery.INSTANCE;
  private static final DenyQuery DENY_QUERY = DenyQuery.INSTANCE;

  private static final IpAccessList ACL =
      IpAccessList.builder()
          .setName("acl")
          .setLines(
              ImmutableList.of(
                  accepting().setMatchCondition(matchDst(IP0)).build(),
                  rejecting().setMatchCondition(matchDst(IP1)).build(),
                  rejecting().setMatchCondition(matchDst(IP2)).build(),
                  accepting().setMatchCondition(matchDst(IP3)).build()))
          .build();

  private static final IpAccessList ACCEPT_ALL_ACL =
      IpAccessList.builder().setName("ACCEPT_ALL").setLines(ImmutableList.of(ACCEPT_ALL)).build();

  private static final IpAccessList BLOCKED_LINE_ACL =
      IpAccessList.builder()
          .setName("blockedAcl")
          .setLines(
              ImmutableList.of(
                  accepting().setMatchCondition(matchDst(IP0)).build(),
                  rejecting().setMatchCondition(matchDst(IP1)).build(),
                  accepting().setMatchCondition(matchDst(Prefix.parse("1.1.1.0/31"))).build()))
          .build();

  private static final IpAccessList SRC_ACL =
      IpAccessList.builder()
          .setName("srcAcl")
          .setLines(
              ImmutableList.of(
                  accepting()
                      .setMatchCondition(and(ORIGINATING_FROM_DEVICE, matchDst(IP0)))
                      .build(),
                  accepting()
                      .setMatchCondition(and(matchSrcInterface(IFACE1), matchDst(IP1)))
                      .build(),
                  accepting()
                      .setMatchCondition(and(matchSrcInterface(IFACE2), matchDst(IP2)))
                      .build(),
                  accepting()
                      .setMatchCondition(and(matchSrcInterface(IFACE1), matchSrcInterface(IFACE2)))
                      .build(),
                  accepting()
                      .setMatchCondition(and(ORIGINATING_FROM_DEVICE, matchSrcInterface(IFACE1)))
                      .build()))
          .build();

  private static final IpAccessList REJECT_ALL_ACL =
      IpAccessList.builder()
          .setName("REJECT_ALL")
          .setLines(ImmutableList.of(ExprAclLine.REJECT_ALL))
          .build();

  @ClassRule public static TemporaryFolder _tmp = new TemporaryFolder();

  private static IBatfish _batfish;
  private static Configuration _config;
  private static SearchFiltersParameters _allLocationsParams;

  @BeforeClass
  public static void setup() {
    NetworkFactory nf = new NetworkFactory();
    _config =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("A")
            .build();
    _config
        .getIpAccessLists()
        .putAll(
            ImmutableMap.of(
                ACL.getName(),
                ACL,
                BLOCKED_LINE_ACL.getName(),
                BLOCKED_LINE_ACL,
                SRC_ACL.getName(),
                SRC_ACL,
                ACCEPT_ALL_ACL.getName(),
                ACCEPT_ALL_ACL,
                REJECT_ALL_ACL.getName(),
                REJECT_ALL_ACL));

    Builder ib = nf.interfaceBuilder().setActive(true).setOwner(_config);
    ib.setName(IFACE1).build();
    ib.setName(IFACE2).build();
    ib.setName("inactiveIface").setActive(false).build();

    _batfish = new MockBatfish(_config);
    _allLocationsParams =
        new SearchFiltersQuestion()
            .toSearchFiltersParameters()
            .toBuilder()
            .setStartLocationSpecifier(LocationSpecifier.ALL_LOCATIONS)
            .build();
  }

  private static NonDiffConfigContext getConfigContextWithParams(SearchFiltersParameters params) {
    return new NonDiffConfigContext(
        _config, _config.getIpAccessLists().keySet(), _batfish.getSnapshot(), _batfish, params);
  }

  @Test
  public void testPermittedFlows_ACCEPT_ALL() {
    NonDiffConfigContext configContext = getConfigContextWithParams(_allLocationsParams);
    Flow flow = configContext.getFlow(configContext.getReachBdd(ACCEPT_ALL_ACL, PERMIT_QUERY));
    assertNotNull("Should find permitted flow", flow);
  }

  @Test
  public void testPermittedFlows_REJECT_ALL() {
    NonDiffConfigContext configContext = getConfigContextWithParams(_allLocationsParams);
    BDD reachBdd = configContext.getReachBdd(REJECT_ALL_ACL, PERMIT_QUERY);
    assertTrue("Reach BDD should be empty", reachBdd.isZero());
    Flow flow = configContext.getFlow(reachBdd);
    assertNull("Should not find permitted flow", flow);
  }

  @Test
  public void testDeniedFlows_ACCEPT_ALL() {
    NonDiffConfigContext configContext = getConfigContextWithParams(_allLocationsParams);
    BDD reachBdd = configContext.getReachBdd(ACCEPT_ALL_ACL, DENY_QUERY);
    assertTrue("Reach BDD should be empty", reachBdd.isZero());
    Flow flow = configContext.getFlow(reachBdd);
    assertNull("Should not find denied flow", flow);
  }

  @Test
  public void testDeniedFlows_REJECT_ALL() {
    NonDiffConfigContext configContext = getConfigContextWithParams(_allLocationsParams);
    BDD reachBdd = configContext.getReachBdd(REJECT_ALL_ACL, DENY_QUERY);
    Flow flow = configContext.getFlow(reachBdd);
    assertNotNull("Should find denied flow", flow);
  }

  @Test
  public void testPermittedFlows_ACL() {
    NonDiffConfigContext configContext = getConfigContextWithParams(_allLocationsParams);
    Flow flow = configContext.getFlow(configContext.getReachBdd(ACL, PERMIT_QUERY));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, hasDstIp(oneOf(IP0, IP3)));
  }

  @Test
  public void testPermittedFlows_headerSpace() {
    SearchFiltersParameters.Builder paramsBuilder =
        _allLocationsParams
            .toBuilder()
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(IP0.toIpSpace()))
            .setSourceIpSpaceSpecifier(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .setHeaderSpace(HeaderSpace.builder().build());

    SearchFiltersParameters params = paramsBuilder.build();
    NonDiffConfigContext configContext = getConfigContextWithParams(params);
    Flow flow = configContext.getFlow(configContext.getReachBdd(ACL, PERMIT_QUERY));
    assertNotNull("Should find permitted flow for IP0", flow);
    assertThat(flow, hasDstIp(IP0));

    params = paramsBuilder.setHeaderSpace(HeaderSpace.builder().setNegate(true).build()).build();
    configContext = getConfigContextWithParams(params);
    flow = configContext.getFlow(configContext.getReachBdd(ACL, PERMIT_QUERY));
    assertNotNull("Should find permitted flow for IP3 since IP0 is now excluded", flow);
    assertThat(flow, hasDstIp(IP3));
  }

  @Test
  public void testDeniedFlows_ACL() {
    NonDiffConfigContext configContext = getConfigContextWithParams(_allLocationsParams);
    Flow flow = configContext.getFlow(configContext.getReachBdd(ACL, DENY_QUERY));
    assertNotNull("Should find denied flow", flow);
    assertThat(flow, hasDstIp(not(oneOf(IP0, IP3))));
  }

  @Test
  public void testMatchLine_ACL() {
    MatchLineQuery matchLine0 = new MatchLineQuery(0);
    MatchLineQuery matchLine1 = new MatchLineQuery(1);
    MatchLineQuery matchLine2 = new MatchLineQuery(2);
    MatchLineQuery matchLine3 = new MatchLineQuery(3);

    NonDiffConfigContext configContext = getConfigContextWithParams(_allLocationsParams);
    Flow flow = configContext.getFlow(configContext.getReachBdd(ACL, matchLine0));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, hasDstIp(IP0));

    configContext = getConfigContextWithParams(_allLocationsParams);
    flow = configContext.getFlow(configContext.getReachBdd(ACL, matchLine1));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, hasDstIp(IP1));

    configContext = getConfigContextWithParams(_allLocationsParams);
    flow = configContext.getFlow(configContext.getReachBdd(ACL, matchLine2));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, hasDstIp(IP2));

    configContext = getConfigContextWithParams(_allLocationsParams);
    flow = configContext.getFlow(configContext.getReachBdd(ACL, matchLine3));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, hasDstIp(IP3));
  }

  @Test
  public void testMatchLine_BLOCKED_LINE_ACL() {
    MatchLineQuery matchLine2 = new MatchLineQuery(2);
    NonDiffConfigContext configContext = getConfigContextWithParams(_allLocationsParams);
    Flow flow = configContext.getFlow(configContext.getReachBdd(BLOCKED_LINE_ACL, matchLine2));
    assertNull("Should not find permitted flow", flow);
  }

  @Test
  public void testTestFilter() {
    String hostname = _config.getHostname();
    Flow flow = Flow.builder().setIngressNode(hostname).setDstIp(IP2).build();
    assertThat(
        TestFiltersAnswerer.getRow(ACL, flow, _config),
        allOf(
            hasColumn(COL_ACTION, equalTo("DENY"), Schema.STRING),
            hasColumn(COL_FILTER_NAME, equalTo(ACL.getName()), Schema.STRING)));
  }

  @Test
  public void testPermitAnswer() {
    SearchFiltersQuestion question = new SearchFiltersQuestion();
    SearchFiltersAnswerer answerer = new SearchFiltersAnswerer(question, _batfish);
    TableAnswerElement ae = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    // Should see results for all but ACCEPT_ALL_ACL (since that ACL denies no flows)
    assertThat(
        ae,
        hasRows(
            containsInAnyOrder(
                ImmutableList.of(
                    allOf(
                        hasColumn(COL_ACTION, equalTo("PERMIT"), Schema.STRING),
                        hasColumn(COL_FILTER_NAME, equalTo(ACL.getName()), Schema.STRING)),
                    allOf(
                        hasColumn(COL_ACTION, equalTo("PERMIT"), Schema.STRING),
                        hasColumn(
                            COL_FILTER_NAME, equalTo(BLOCKED_LINE_ACL.getName()), Schema.STRING)),
                    allOf(
                        hasColumn(COL_ACTION, equalTo("PERMIT"), Schema.STRING),
                        hasColumn(COL_FILTER_NAME, equalTo(SRC_ACL.getName()), Schema.STRING)),
                    allOf(
                        hasColumn(COL_ACTION, equalTo("PERMIT"), Schema.STRING),
                        hasColumn(
                            COL_FILTER_NAME, equalTo(ACCEPT_ALL_ACL.getName()), Schema.STRING))))));
  }

  @Test
  public void testDenyAnswer() {
    SearchFiltersQuestion question = SearchFiltersQuestion.builder().setAction("deny").build();
    SearchFiltersAnswerer answerer = new SearchFiltersAnswerer(question, _batfish);
    TableAnswerElement ae = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    // Should see results for all but ACCEPT_ALL_ACL (since that ACL denies no flows)
    assertThat(
        ae,
        hasRows(
            containsInAnyOrder(
                ImmutableList.of(
                    allOf(
                        hasColumn(COL_ACTION, equalTo("DENY"), Schema.STRING),
                        hasColumn(COL_FILTER_NAME, equalTo(ACL.getName()), Schema.STRING)),
                    allOf(
                        hasColumn(COL_ACTION, equalTo("DENY"), Schema.STRING),
                        hasColumn(
                            COL_FILTER_NAME, equalTo(BLOCKED_LINE_ACL.getName()), Schema.STRING)),
                    allOf(
                        hasColumn(COL_ACTION, equalTo("DENY"), Schema.STRING),
                        hasColumn(COL_FILTER_NAME, equalTo(SRC_ACL.getName()), Schema.STRING)),
                    allOf(
                        hasColumn(COL_ACTION, equalTo("DENY"), Schema.STRING),
                        hasColumn(
                            COL_FILTER_NAME, equalTo(REJECT_ALL_ACL.getName()), Schema.STRING))))));
  }

  @Test
  public void testMatchSrcInterface() {
    MatchLineQuery matchLine0 = new MatchLineQuery(0);
    MatchLineQuery matchLine1 = new MatchLineQuery(1);
    MatchLineQuery matchLine2 = new MatchLineQuery(2);
    MatchLineQuery matchLine3 = new MatchLineQuery(3);
    MatchLineQuery matchLine4 = new MatchLineQuery(4);

    NonDiffConfigContext configContext = getConfigContextWithParams(_allLocationsParams);
    Flow flow = configContext.getFlow(configContext.getReachBdd(SRC_ACL, matchLine0));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, allOf(hasIngressInterface(nullValue()), hasDstIp(IP0)));

    configContext = getConfigContextWithParams(_allLocationsParams);
    flow = configContext.getFlow(configContext.getReachBdd(SRC_ACL, matchLine1));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, allOf(hasIngressInterface(IFACE1), hasDstIp(IP1)));

    configContext = getConfigContextWithParams(_allLocationsParams);
    flow = configContext.getFlow(configContext.getReachBdd(SRC_ACL, matchLine2));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, allOf(hasIngressInterface(IFACE2), hasDstIp(IP2)));

    // cannot have two different source interfaces
    configContext = getConfigContextWithParams(_allLocationsParams);
    BDD reachBdd = configContext.getReachBdd(SRC_ACL, matchLine3);
    assertTrue(reachBdd.isZero());
    flow = configContext.getFlow(reachBdd);
    assertNull("Should not find permitted flow", flow);

    // cannot have originate from device and have a source interface
    configContext = getConfigContextWithParams(_allLocationsParams);
    reachBdd = configContext.getReachBdd(SRC_ACL, matchLine4);
    assertTrue(reachBdd.isZero());
    flow = configContext.getFlow(reachBdd);
    assertNull("Should not find permitted flow", flow);
  }

  @Test
  public void testSane() {
    // An ACL that rejects flows originating from all possible sources should not be matchable.
    IpAccessList denyAllSourcesAcl =
        IpAccessList.builder()
            .setName("srcAcl")
            .setLines(
                ImmutableList.of(
                    rejecting().setMatchCondition(ORIGINATING_FROM_DEVICE).build(),
                    rejecting().setMatchCondition(matchSrcInterface(IFACE1)).build(),
                    rejecting().setMatchCondition(matchSrcInterface(IFACE2)).build(),
                    ACCEPT_ALL))
            .build();
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    c.getIpAccessLists().put(denyAllSourcesAcl.getName(), denyAllSourcesAcl);

    Builder ib = nf.interfaceBuilder().setActive(true).setOwner(c);
    ib.setName(IFACE1).build();
    ib.setName(IFACE2).build();
    // Inactive interfaces should not be considered possible sources
    ib.setName("inactiveIface").setActive(false).build();

    IBatfish bf = new MockBatfish(c);
    NonDiffConfigContext configContext =
        new NonDiffConfigContext(
            c,
            ImmutableSet.of(denyAllSourcesAcl.getName()),
            bf.getSnapshot(),
            bf,
            _allLocationsParams);
    Flow flow = configContext.getFlow(configContext.getReachBdd(denyAllSourcesAcl, PERMIT_QUERY));
    assertNull("Should not find permitted flow", flow);
  }

  @Test
  public void testSane2() {
    // An ACL that can only match with ingress interface IFACE2.
    IpAccessList denyAllButIface2 =
        IpAccessList.builder()
            .setName("srcAcl")
            .setLines(
                ImmutableList.of(
                    rejecting().setMatchCondition(ORIGINATING_FROM_DEVICE).build(),
                    rejecting().setMatchCondition(matchSrcInterface(IFACE1)).build(),
                    ACCEPT_ALL))
            .build();
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    c.getIpAccessLists().put(denyAllButIface2.getName(), denyAllButIface2);

    Builder ib = nf.interfaceBuilder().setActive(true).setOwner(c);
    ib.setName(IFACE1).build();
    ib.setName(IFACE2).build();
    ib.setName("inactiveIface").setActive(false).build();

    IBatfish bf = new MockBatfish(c);
    NonDiffConfigContext configContext =
        new NonDiffConfigContext(
            c,
            ImmutableSet.of(denyAllButIface2.getName()),
            bf.getSnapshot(),
            bf,
            _allLocationsParams);
    Flow flow = configContext.getFlow(configContext.getReachBdd(denyAllButIface2, PERMIT_QUERY));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, hasIngressInterface(IFACE2));
  }

  @Test
  public void testSourceInterfaceParameter() {
    SearchFiltersParameters params =
        _allLocationsParams
            .toBuilder()
            .setStartLocationSpecifier(new NameRegexInterfaceLinkLocationSpecifier(IFACE1))
            .build();

    // can match line 1 because IFACE1 is specified
    MatchLineQuery matchLine1 = new MatchLineQuery(1);
    NonDiffConfigContext configContext = getConfigContextWithParams(params);
    Flow flow = configContext.getFlow(configContext.getReachBdd(SRC_ACL, matchLine1));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, allOf(hasIngressInterface(IFACE1), hasDstIp(IP1)));

    // cannot match line 2 because IFACE2 is not specified
    MatchLineQuery matchLine2 = new MatchLineQuery(2);
    flow = configContext.getFlow(configContext.getReachBdd(SRC_ACL, matchLine2));
    assertNull("Should not find permitted flow", flow);
  }

  @Test
  public void testDstIpConstraint_ACCEPT_ALL() {
    Ip constraintIp = Ip.parse("21.21.21.21");
    SearchFiltersParameters params =
        _allLocationsParams
            .toBuilder()
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(constraintIp.toIpSpace()))
            .setSourceIpSpaceSpecifier(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .setHeaderSpace(new HeaderSpace())
            .build();

    NonDiffConfigContext configContext = getConfigContextWithParams(params);
    Flow flow = configContext.getFlow(configContext.getReachBdd(ACCEPT_ALL_ACL, PERMIT_QUERY));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, hasDstIp(constraintIp));
  }

  @Test
  public void testSrcIpConstraint_ACCEPT_ALL() {
    Ip constraintIp = Ip.parse("21.21.21.21");
    SearchFiltersParameters params =
        _allLocationsParams
            .toBuilder()
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .setSourceIpSpaceSpecifier(new ConstantIpSpaceSpecifier(constraintIp.toIpSpace()))
            .setHeaderSpace(new HeaderSpace())
            .build();

    NonDiffConfigContext configContext = getConfigContextWithParams(params);
    Flow flow = configContext.getFlow(configContext.getReachBdd(ACCEPT_ALL_ACL, PERMIT_QUERY));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, hasSrcIp(constraintIp));
  }

  @Test
  public void testPortConstraints_ACCEPT_ALL() {
    HeaderSpace hs = new HeaderSpace();
    hs.setSrcPorts(ImmutableList.of(SubRange.singleton(1111)));
    hs.setDstPorts(ImmutableList.of(SubRange.singleton(2222)));
    hs.setIpProtocols(ImmutableList.of(IpProtocol.TCP));
    SearchFiltersParameters params =
        _allLocationsParams
            .toBuilder()
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .setSourceIpSpaceSpecifier(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .setHeaderSpace(hs)
            .build();

    NonDiffConfigContext configContext = getConfigContextWithParams(params);
    Flow flow = configContext.getFlow(configContext.getReachBdd(ACCEPT_ALL_ACL, PERMIT_QUERY));
    assertNotNull("Should find permitted flow", flow);
    assertThat(flow, allOf(hasSrcPort(1111), hasDstPort(2222)));
  }

  @Test
  public void testReachFilterNodeSpecifierDefault() {
    SearchFiltersQuestion q = new SearchFiltersQuestion();
    Set<String> nodes =
        q.getNodesSpecifier().resolve(_batfish.specifierContext(_batfish.getSnapshot()));
    assertThat(nodes, contains(_config.getHostname()));

    q =
        SearchFiltersQuestion.builder()
            .setFilterSpecifier(ACL.getName())
            .setAction("permit")
            .setNodeSpecifier("UNMATCHABLE")
            .build();
    nodes = q.getNodesSpecifier().resolve(_batfish.specifierContext(_batfish.getSnapshot()));
    assertThat(nodes, emptyIterable());
  }
}
