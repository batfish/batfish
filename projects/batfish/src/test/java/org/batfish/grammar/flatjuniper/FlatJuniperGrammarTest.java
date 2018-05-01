package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AndMatchExprMatchers.hasConjuncts;
import static org.batfish.datamodel.matchers.AndMatchExprMatchers.isAndMatchExprThat;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasEnforceFirstAs;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasLocalAs;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasNeighbor;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasNeighbors;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDefaultVrf;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessList;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrfs;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.isDynamic;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAdditionalArpIps;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasOspfCost;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasZoneName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isOspfPassive;
import static org.batfish.datamodel.matchers.IpAccessListLineMatchers.hasAction;
import static org.batfish.datamodel.matchers.IpAccessListLineMatchers.hasMatchCondition;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.hasLines;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.batfish.datamodel.matchers.OrMatchExprMatchers.hasDisjuncts;
import static org.batfish.datamodel.matchers.OrMatchExprMatchers.isOrMatchExprThat;
import static org.batfish.datamodel.matchers.OspfAreaSummaryMatchers.hasMetric;
import static org.batfish.datamodel.matchers.OspfAreaSummaryMatchers.isAdvertised;
import static org.batfish.datamodel.matchers.OspfProcessMatchers.hasArea;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.batfish.datamodel.matchers.VrfMatchers.hasOspfProcess;
import static org.batfish.datamodel.matchers.VrfMatchers.hasStaticRoutes;
import static org.batfish.representation.juniper.JuniperConfiguration.ACL_NAME_EXISTING_CONNECTION;
import static org.batfish.representation.juniper.JuniperConfiguration.ACL_NAME_GLOBAL_POLICY;
import static org.batfish.representation.juniper.JuniperConfiguration.ACL_NAME_SECURITY_POLICY;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.OspfAreaSummary;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.State;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.matchers.OspfAreaMatchers;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.representation.juniper.JuniperStructureType;
import org.batfish.representation.juniper.JuniperStructureUsage;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@link FlatJuniperParser}flat Junpier parser and {@link
 * FlatJuniperControlPlaneExtractor}.
 */
public class FlatJuniperGrammarTest {

  private static class HasClusterId extends FeatureMatcher<BgpNeighbor, Long> {
    public HasClusterId(Matcher<? super Long> subMatcher) {
      super(subMatcher, "clusterId", "clusterId");
    }

    @Override
    protected Long featureValueOf(BgpNeighbor actual) {
      return actual.getClusterId();
    }
  }

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/juniper/testconfigs/";

  private static String TESTRIGS_PREFIX = "org/batfish/grammar/juniper/testrigs/";

  private static HasClusterId hasClusterId(long expectedClusterId) {
    return new HasClusterId(equalTo(expectedClusterId));
  }

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static Flow createFlow(String sourceAddress, String destinationAddress) {
    return createFlow(sourceAddress, destinationAddress, State.NEW);
  }

  private static Flow createFlow(String sourceAddress, String destinationAddress, State state) {
    Flow.Builder fb = new Flow.Builder();
    fb.setIngressNode("node");
    fb.setSrcIp(new Ip(sourceAddress));
    fb.setDstIp(new Ip(destinationAddress));
    fb.setState(state);
    fb.setTag("test");
    return fb.build();
  }

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private Configuration parseConfig(String hostname) throws IOException {
    return parseTextConfigs(hostname).get(hostname);
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    return getBatfishForConfigurationNames(configurationNames).loadConfigurations();
  }

  @Test
  public void testApplications() throws IOException {
    String hostname = "applications";

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse();

    /* a1 should be used, while a2 should be unused */
    assertThat(ccae, hasNumReferrers(hostname, JuniperStructureType.APPLICATION, "a2", 0));
    assertThat(ccae, hasNumReferrers(hostname, JuniperStructureType.APPLICATION, "a1", 1));
    assertThat(ccae, hasNumReferrers(hostname, JuniperStructureType.APPLICATION, "a3", 1));
  }

  @Test
  public void testApplicationSet() throws IOException {
    String hostname = "application-set";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse();
    SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
        undefinedReferences = ccae.getUndefinedReferences();
    Configuration c = parseConfig(hostname);
    String aclName = "~FROM_ZONE~z1~TO_ZONE~z2";

    /* Check that appset2 contains definition of appset1 concatenated with definition of a3 */
    assertThat(
        c,
        hasIpAccessList(
            aclName,
            hasLines(
                equalTo(
                    ImmutableList.of(
                        IpAccessListLine.acceptingHeaderSpace(
                            HeaderSpace.builder()
                                .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
                                .setSrcPorts(ImmutableList.of(new SubRange(1, 1)))
                                .build()),
                        IpAccessListLine.acceptingHeaderSpace(
                            HeaderSpace.builder()
                                .setDstPorts(ImmutableList.of(new SubRange(2, 2)))
                                .setIpProtocols(ImmutableList.of(IpProtocol.UDP))
                                .build()),
                        IpAccessListLine.acceptingHeaderSpace(
                            HeaderSpace.builder()
                                .setDstPorts(ImmutableList.of(new SubRange(3, 3)))
                                .setIpProtocols(ImmutableList.of(IpProtocol.UDP))
                                .build()))))));

    /* Check that appset1 and appset2 are referenced, but appset3 is not */
    assertThat(ccae, hasNumReferrers(hostname, JuniperStructureType.APPLICATION_SET, "appset1", 1));
    assertThat(ccae, hasNumReferrers(hostname, JuniperStructureType.APPLICATION_SET, "appset2", 1));
    assertThat(ccae, hasNumReferrers(hostname, JuniperStructureType.APPLICATION_SET, "appset3", 0));

    /*
     * Check that there is an undefined reference to appset4, but not to appset1-3
     * (via reference in security policy).
     */
    assertThat(undefinedReferences, hasKey(hostname));
    SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>
        undefinedReferencesByType = undefinedReferences.get(hostname);
    assertThat(
        undefinedReferencesByType,
        hasKey(JuniperStructureType.APPLICATION_OR_APPLICATION_SET.getDescription()));
    SortedMap<String, SortedMap<String, SortedSet<Integer>>> urApplicationOrApplicationSetByName =
        undefinedReferencesByType.get(
            JuniperStructureType.APPLICATION_OR_APPLICATION_SET.getDescription());
    assertThat(urApplicationOrApplicationSetByName, not(hasKey("appset1")));
    assertThat(urApplicationOrApplicationSetByName, not(hasKey("appset2")));
    assertThat(urApplicationOrApplicationSetByName, not(hasKey("appset3")));
    assertThat(urApplicationOrApplicationSetByName, hasKey("appset4"));
    SortedMap<String, SortedSet<Integer>> urApplicationOrApplicationSetByUsage =
        urApplicationOrApplicationSetByName.get("appset4");
    assertThat(
        urApplicationOrApplicationSetByUsage,
        hasKey(JuniperStructureUsage.SECURITY_POLICY_MATCH_APPLICATION.getDescription()));

    /*
     * Check that there is an undefined reference to application-set appset4, but not to appset1-3
     * (via reference in application-set definition).
     */
    assertThat(
        undefinedReferencesByType, hasKey(JuniperStructureType.APPLICATION_SET.getDescription()));
    SortedMap<String, SortedMap<String, SortedSet<Integer>>> urApplicationSetByName =
        undefinedReferencesByType.get(JuniperStructureType.APPLICATION_SET.getDescription());
    assertThat(urApplicationSetByName, not(hasKey("appset1")));
    assertThat(urApplicationSetByName, not(hasKey("appset2")));
    assertThat(urApplicationSetByName, not(hasKey("appset3")));
    assertThat(urApplicationSetByName, hasKey("appset4"));
    SortedMap<String, SortedSet<Integer>> urApplicationSetByUsage =
        urApplicationSetByName.get("appset4");
    assertThat(
        urApplicationSetByUsage,
        hasKey(JuniperStructureUsage.APPLICATION_SET_MEMBER_APPLICATION_SET.getDescription()));

    /*
     * Check that there is an undefined reference to application a4 but not a1-3
     * (via reference in application-set definition).
     */
    assertThat(
        undefinedReferencesByType, hasKey(JuniperStructureType.APPLICATION.getDescription()));
    SortedMap<String, SortedMap<String, SortedSet<Integer>>> urApplicationByName =
        undefinedReferencesByType.get(JuniperStructureType.APPLICATION.getDescription());
    assertThat(urApplicationByName, not(hasKey("a1")));
    assertThat(urApplicationByName, not(hasKey("a2")));
    assertThat(urApplicationByName, not(hasKey("a3")));
    assertThat(urApplicationByName, hasKey("a4"));
    SortedMap<String, SortedSet<Integer>> urApplicationByUsage = urApplicationByName.get("a4");
    assertThat(
        urApplicationByUsage,
        hasKey(JuniperStructureUsage.APPLICATION_SET_MEMBER_APPLICATION.getDescription()));
  }

  @Test
  public void testApplicationWithTerms() throws IOException {
    String hostname = "application-with-terms";
    Configuration c = parseConfig(hostname);
    String aclName = "~FROM_ZONE~z1~TO_ZONE~z2";

    /*
     * An IpAccessList should be generated for the cross-zone policy from z1 to z2. Its definition
     * should inline the matched application, with the action applied to each generated line
     * from the application. One line should be generated per application term.
     */
    assertThat(
        c,
        hasIpAccessList(
            aclName,
            hasLines(
                equalTo(
                    ImmutableList.of(
                        IpAccessListLine.acceptingHeaderSpace(
                            HeaderSpace.builder()
                                .setDstPorts(ImmutableList.of(new SubRange(1, 1)))
                                .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
                                .setSrcPorts(ImmutableList.of(new SubRange(2, 2)))
                                .build()),
                        IpAccessListLine.acceptingHeaderSpace(
                            HeaderSpace.builder()
                                .setDstPorts(ImmutableList.of(new SubRange(3, 3)))
                                .setIpProtocols(ImmutableList.of(IpProtocol.UDP))
                                .setSrcPorts(ImmutableList.of(new SubRange(4, 4)))
                                .build()))))));
  }

  /** Tests support for dynamic bgp parsing using "bgp allow" command */
  @Test
  public void testBgpAllow() throws IOException {
    Configuration c = parseConfig("bgp-allow");
    assertThat(
        c, hasDefaultVrf(hasBgpProcess(hasNeighbor(Prefix.parse("10.1.1.0/24"), isDynamic()))));
  }

  @Test
  public void testAutonomousSystem() throws IOException {
    String testrigName = "autonomous-system";
    String c1Name = "as1";
    String c2Name = "as2";
    String c3Name = "as3";
    Prefix neighborPrefix = Prefix.parse("1.0.0.1/32");

    List<String> configurationNames = ImmutableList.of(c1Name, c2Name, c3Name);
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    Map<String, Configuration> configurations = batfish.loadConfigurations();
    Configuration c1 = configurations.get(c1Name);
    Configuration c2 = configurations.get(c2Name);
    Configuration c3 = configurations.get(c3Name);

    assertThat(c1, hasDefaultVrf(hasBgpProcess(hasNeighbor(neighborPrefix, hasLocalAs(1)))));
    assertThat(c2, hasDefaultVrf(hasBgpProcess(hasNeighbor(neighborPrefix, hasLocalAs(1)))));
    assertThat(c3, hasDefaultVrf(hasBgpProcess(hasNeighbor(neighborPrefix, hasLocalAs(1)))));
  }

  @Test
  public void testBgpClusterId() throws IOException {
    String testrigName = "rr";
    String configName = "rr";
    Ip neighbor1Ip = new Ip("2.2.2.2");
    Ip neighbor2Ip = new Ip("4.4.4.4");

    List<String> configurationNames = ImmutableList.of(configName);
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    Map<String, Configuration> configurations = batfish.loadConfigurations();

    Configuration rr = configurations.get(configName);
    BgpProcess proc = rr.getDefaultVrf().getBgpProcess();
    BgpNeighbor neighbor1 =
        proc.getNeighbors().get(new Prefix(neighbor1Ip, Prefix.MAX_PREFIX_LENGTH));
    BgpNeighbor neighbor2 =
        proc.getNeighbors().get(new Prefix(neighbor2Ip, Prefix.MAX_PREFIX_LENGTH));

    assertThat(neighbor1, hasClusterId(new Ip("3.3.3.3").asLong()));
    assertThat(neighbor2, hasClusterId(new Ip("1.1.1.1").asLong()));
  }

  @Test
  public void testBgpMultipathMultipleAs() throws IOException {
    String testrigName = "multipath-multiple-as";
    List<String> configurationNames =
        ImmutableList.of("multiple_as_disabled", "multiple_as_enabled", "multiple_as_mixed");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    Map<String, Configuration> configurations = batfish.loadConfigurations();
    MultipathEquivalentAsPathMatchMode multipleAsDisabled =
        configurations
            .get("multiple_as_disabled")
            .getDefaultVrf()
            .getBgpProcess()
            .getMultipathEquivalentAsPathMatchMode();
    MultipathEquivalentAsPathMatchMode multipleAsEnabled =
        configurations
            .get("multiple_as_enabled")
            .getDefaultVrf()
            .getBgpProcess()
            .getMultipathEquivalentAsPathMatchMode();
    MultipathEquivalentAsPathMatchMode multipleAsMixed =
        configurations
            .get("multiple_as_mixed")
            .getDefaultVrf()
            .getBgpProcess()
            .getMultipathEquivalentAsPathMatchMode();

    assertThat(multipleAsDisabled, equalTo(MultipathEquivalentAsPathMatchMode.FIRST_AS));
    assertThat(multipleAsEnabled, equalTo(MultipathEquivalentAsPathMatchMode.PATH_LENGTH));
    assertThat(multipleAsMixed, equalTo(MultipathEquivalentAsPathMatchMode.FIRST_AS));
  }

  @Test
  public void testEnforceFistAs() throws IOException {
    String hostname = "bgp-enforce-first-as";
    Configuration c = parseConfig(hostname);
    assertThat(c, hasDefaultVrf(hasBgpProcess(hasNeighbors(hasValue(hasEnforceFirstAs())))));
  }

  @Test
  public void testEthernetSwitchingFilterReference() throws IOException {
    String hostname = "ethernet-switching-filter";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse();

    /* esfilter should be referred, while esfilter2 should be unreferred */
    assertThat(
        ccae, hasNumReferrers(hostname, JuniperStructureType.FIREWALL_FILTER, "esfilter", 1));
    assertThat(
        ccae, hasNumReferrers(hostname, JuniperStructureType.FIREWALL_FILTER, "esfilter2", 0));
  }

  @Test
  public void testFirewallGlobalAddressBook() throws IOException {
    Configuration c = parseConfig("firewall-global-address-book");
    String interfaceNameTrust = "ge-0/0/0.0";
    String interfaceNameUntrust = "ge-0/0/1.0";
    String specificSpaceName = "global~ADDR1";
    String wildcardSpaceName = "global~ADDR2";
    String indirectSpaceName = "global~ADDRSET";

    // Address on untrust interface's subnet
    String untrustIpAddr = "1.2.4.5";
    // Specific address allowed by the address-set
    String specificAddr = "2.2.2.2";
    // Address allowed by the wildcard-address in the address-set
    String wildcardAddr = "1.3.3.4";
    // Address not allowed by either entry in the address-set
    String notWildcardAddr = "1.2.3.5";

    Flow flowFromSpecificAddr = createFlow(specificAddr, untrustIpAddr);
    Flow flowFromWildcardAddr = createFlow(wildcardAddr, untrustIpAddr);
    Flow flowFromNotWildcardAddr = createFlow(notWildcardAddr, untrustIpAddr);
    IpAccessList untrustCombinedAcl =
        c.getInterfaces().get(interfaceNameUntrust).getOutgoingFilter();

    // Should have three global IpSpaces in the config
    assertThat(
        c.getIpSpaces().keySet(),
        containsInAnyOrder(specificSpaceName, wildcardSpaceName, indirectSpaceName));

    IpSpace specificSpace = c.getIpSpaces().get(specificSpaceName);
    IpSpace wildcardSpace = c.getIpSpaces().get(wildcardSpaceName);
    IpSpace indirectSpace = c.getIpSpaces().get(indirectSpaceName);

    // Specific space should contain the specific addr and not others
    assertThat(specificSpace, containsIp(new Ip(specificAddr)));
    assertThat(specificSpace, not(containsIp(new Ip(wildcardAddr))));

    // Wildcard space should contain the wildcard addr and not others
    assertThat(wildcardSpace, containsIp(new Ip(wildcardAddr)));
    assertThat(wildcardSpace, not(containsIp(new Ip(notWildcardAddr))));

    // Indirect space should contain both specific and wildcard addr, but not others
    assertThat(indirectSpace, containsIp(new Ip(specificAddr), c.getIpSpaces()));
    assertThat(indirectSpace, containsIp(new Ip(wildcardAddr), c.getIpSpaces()));
    assertThat(indirectSpace, not(containsIp(new Ip(notWildcardAddr), c.getIpSpaces())));

    // Specifically allowed source addr should be accepted
    assertThat(
        untrustCombinedAcl,
        accepts(flowFromSpecificAddr, interfaceNameTrust, c.getIpAccessLists(), c.getIpSpaces()));
    // Source addr covered by the wildcard entry should be accepted
    assertThat(
        untrustCombinedAcl,
        accepts(flowFromWildcardAddr, interfaceNameTrust, c.getIpAccessLists(), c.getIpSpaces()));
    // Source addr covered by neither addr-set entry should be rejected
    assertThat(
        untrustCombinedAcl,
        rejects(
            flowFromNotWildcardAddr, interfaceNameTrust, c.getIpAccessLists(), c.getIpSpaces()));
  }

  @Test
  public void testFirewallGlobalPolicy() throws IOException {
    Configuration c = parseConfig("firewall-global-policy");
    String interfaceNameTrust = "ge-0/0/0.0";
    String interfaceNameUntrust = "ge-0/0/1.0";
    String trustedIpAddr = "1.2.3.5";
    String untrustedIpAddr = "1.2.4.5";

    Flow trustToUntrustFlow = createFlow(trustedIpAddr, untrustedIpAddr);
    Flow untrustToTrustFlow = createFlow(untrustedIpAddr, trustedIpAddr);

    IpAccessList aclTrustOut = c.getInterfaces().get(interfaceNameTrust).getOutgoingFilter();
    IpAccessList aclUntrustOut = c.getInterfaces().get(interfaceNameUntrust).getOutgoingFilter();

    /*
     * Should have six ACLs:
     *  Explicitly defined in the config file:
     *    One from the global security policy
     *  Generated by logic in toVendorIndependent
     *    Two combined outgoing filters for the two interfaces (combines security policies with
     *        egress ACLs)
     *    One permitting existing connections (default firewall behavior)
     *    Two defining security policies for each interface (combines explicit security policy with
     *        implicit security policies like allow existing connection)
     */
    assertThat(
        c.getIpAccessLists().keySet(),
        containsInAnyOrder(
            ACL_NAME_GLOBAL_POLICY,
            aclTrustOut.getName(),
            aclUntrustOut.getName(),
            ACL_NAME_EXISTING_CONNECTION,
            ACL_NAME_SECURITY_POLICY + interfaceNameTrust,
            ACL_NAME_SECURITY_POLICY + interfaceNameUntrust));

    IpAccessListLine aclGlobalPolicyLine =
        Iterables.getOnlyElement(c.getIpAccessLists().get(ACL_NAME_GLOBAL_POLICY).getLines());

    /* Global policy should permit the specific src address defined in the config */
    assertThat(
        aclGlobalPolicyLine,
        hasMatchCondition(equalTo(new MatchHeaderSpace(HeaderSpace.builder().build()))));
    assertThat(aclGlobalPolicyLine, hasAction(equalTo(LineAction.ACCEPT)));

    List<IpAccessListLine> aclTrustSPLines =
        c.getIpAccessLists().get(ACL_NAME_SECURITY_POLICY + interfaceNameTrust).getLines();
    List<IpAccessListLine> aclUntrustSPLines =
        c.getIpAccessLists().get(ACL_NAME_SECURITY_POLICY + interfaceNameUntrust).getLines();

    /* Security policy ACLs should have two lines, one for specific policies and one for default */
    assertThat(aclTrustSPLines, iterableWithSize(3));
    assertThat(aclUntrustSPLines, iterableWithSize(3));

    /* First line should be specific security policy (existing connection in this case) */
    assertThat(
        aclTrustSPLines.get(0),
        hasMatchCondition(
            isOrMatchExprThat(
                hasDisjuncts(
                    containsInAnyOrder(new PermittedByAcl(ACL_NAME_EXISTING_CONNECTION))))));
    assertThat(
        aclUntrustSPLines.get(0),
        hasMatchCondition(
            isOrMatchExprThat(
                hasDisjuncts(
                    containsInAnyOrder(new PermittedByAcl(ACL_NAME_EXISTING_CONNECTION))))));

    /* Second line should be global policy */
    assertThat(
        aclTrustSPLines.get(1),
        hasMatchCondition(equalTo(new PermittedByAcl(ACL_NAME_GLOBAL_POLICY))));
    assertThat(
        aclUntrustSPLines.get(1),
        hasMatchCondition(equalTo(new PermittedByAcl(ACL_NAME_GLOBAL_POLICY))));

    /* Third line should be default policy (reject all traffic) */
    assertThat(aclTrustSPLines.get(2), hasMatchCondition(equalTo(TrueExpr.INSTANCE)));
    assertThat(aclTrustSPLines.get(2), hasAction(equalTo(LineAction.REJECT)));
    assertThat(aclUntrustSPLines.get(2), hasMatchCondition(equalTo(TrueExpr.INSTANCE)));
    assertThat(aclUntrustSPLines.get(2), hasAction(equalTo(LineAction.REJECT)));

    /* Flows in either direction should be permitted by the global policy */
    assertThat(
        aclUntrustOut,
        accepts(trustToUntrustFlow, interfaceNameTrust, c.getIpAccessLists(), c.getIpSpaces()));
    assertThat(
        aclTrustOut,
        accepts(untrustToTrustFlow, interfaceNameUntrust, c.getIpAccessLists(), c.getIpSpaces()));
  }

  @Test
  public void testFirewallGlobalPolicyGlobalAddressBook() throws IOException {
    /*
     * Test address book behavior when used in a global policy
     * i.e. a policy that does not have fromZone or toZone
     */
    Configuration c = parseConfig("firewall-global-policy-global-address-book");
    String interfaceNameTrust = "ge-0/0/0.0";
    String interfaceNameUntrust = "ge-0/0/1.0";
    String trustedIpAddr = "1.2.3.5";
    String untrustedIpAddr = "1.2.4.5";
    String trustedSpaceName = "global~ADDR1";

    Flow trustToUntrustFlow = createFlow(trustedIpAddr, untrustedIpAddr);
    Flow untrustToTrustFlow = createFlow(untrustedIpAddr, trustedIpAddr);

    IpAccessList aclTrustOut = c.getInterfaces().get(interfaceNameTrust).getOutgoingFilter();
    IpAccessList aclUntrustOut = c.getInterfaces().get(interfaceNameUntrust).getOutgoingFilter();

    /* Make sure the global-address-book address is the only config ipSpace */
    assertThat(c.getIpSpaces().keySet(), containsInAnyOrder(trustedSpaceName));

    IpSpace ipSpace = Iterables.getOnlyElement(c.getIpSpaces().values());

    // It should contain the specific address
    assertThat(ipSpace, containsIp(new Ip(trustedIpAddr)));

    // It should not contain the address that is not allowed
    assertThat(ipSpace, not(containsIp(new Ip(untrustedIpAddr))));

    /* Flow from ADDR1 to untrust should be permitted */
    assertThat(
        aclUntrustOut,
        accepts(trustToUntrustFlow, interfaceNameTrust, c.getIpAccessLists(), c.getIpSpaces()));
    /* Flow from not ADDR1 to trust should be rejected */
    assertThat(
        aclTrustOut,
        rejects(untrustToTrustFlow, interfaceNameUntrust, c.getIpAccessLists(), c.getIpSpaces()));
  }

  @Test
  public void testFirewallNoPolicies() throws IOException {
    Configuration c = parseConfig("firewall-no-policies");
    String interfaceNameTrust = "ge-0/0/0.0";
    String interfaceNameUntrust = "ge-0/0/1.0";
    String trustedIpAddr = "1.2.3.5";
    String untrustedIpAddr = "1.2.4.5";

    Flow trustToUntrustFlow = createFlow(trustedIpAddr, untrustedIpAddr);
    Flow untrustToTrustFlow = createFlow(untrustedIpAddr, trustedIpAddr);

    IpAccessList aclTrustOut = c.getInterfaces().get(interfaceNameTrust).getOutgoingFilter();
    IpAccessList aclUntrustOut = c.getInterfaces().get(interfaceNameUntrust).getOutgoingFilter();

    /*
     * Should have five ACLs generated by logic in toVendorIndependent:
     *    Two combined outgoing filters for the two interfaces (combines security policies with
     *        egress ACLs)
     *    One permitting existing connections (default firewall behavior)
     *    Two defining security policies for each interface (combines explicit security policy with
     *        implicit security policies like allow existing connection)
     */
    assertThat(
        c.getIpAccessLists().keySet(),
        containsInAnyOrder(
            aclTrustOut.getName(),
            aclUntrustOut.getName(),
            ACL_NAME_EXISTING_CONNECTION,
            ACL_NAME_SECURITY_POLICY + interfaceNameTrust,
            ACL_NAME_SECURITY_POLICY + interfaceNameUntrust));

    List<IpAccessListLine> aclTrustSPLines =
        c.getIpAccessLists().get(ACL_NAME_SECURITY_POLICY + interfaceNameTrust).getLines();
    List<IpAccessListLine> aclUntrustSPLines =
        c.getIpAccessLists().get(ACL_NAME_SECURITY_POLICY + interfaceNameUntrust).getLines();

    /*
     * The interface security policies should have two lines each: one for the actual
     * security policy (allow established connections here), and one for the default action
     */
    assertThat(aclTrustSPLines, iterableWithSize(2));
    assertThat(aclUntrustSPLines, iterableWithSize(2));

    IpAccessListLine aclTrustOutLine = Iterables.getOnlyElement(aclTrustOut.getLines());
    IpAccessListLine aclUntrustOutLine = Iterables.getOnlyElement(aclUntrustOut.getLines());

    /* Each interface's outgoing ACL line should reference its security policy */
    assertThat(
        aclTrustOutLine,
        hasMatchCondition(
            isAndMatchExprThat(
                hasConjuncts(
                    containsInAnyOrder(
                        new PermittedByAcl(ACL_NAME_SECURITY_POLICY + interfaceNameTrust))))));
    assertThat(
        aclUntrustOutLine,
        hasMatchCondition(
            isAndMatchExprThat(
                hasConjuncts(
                    containsInAnyOrder(
                        new PermittedByAcl(ACL_NAME_SECURITY_POLICY + interfaceNameUntrust))))));

    /*
     * Since no policies are defined in either direction, should default to allowing only
     * established connections
     */
    assertThat(
        aclTrustSPLines.get(0),
        hasMatchCondition(
            isOrMatchExprThat(
                hasDisjuncts(
                    containsInAnyOrder(new PermittedByAcl(ACL_NAME_EXISTING_CONNECTION))))));
    assertThat(
        aclUntrustSPLines.get(0),
        hasMatchCondition(
            isOrMatchExprThat(
                hasDisjuncts(
                    containsInAnyOrder(new PermittedByAcl(ACL_NAME_EXISTING_CONNECTION))))));

    /* Default line should be reject all traffic */
    assertThat(aclTrustSPLines.get(1), hasMatchCondition(equalTo(TrueExpr.INSTANCE)));
    assertThat(aclTrustSPLines.get(1), hasAction(equalTo(LineAction.REJECT)));
    assertThat(aclUntrustSPLines.get(1), hasMatchCondition(equalTo(TrueExpr.INSTANCE)));
    assertThat(aclUntrustSPLines.get(1), hasAction(equalTo(LineAction.REJECT)));

    /* Simple flow in either direction should be blocked */
    assertThat(
        aclUntrustOut,
        rejects(trustToUntrustFlow, interfaceNameTrust, c.getIpAccessLists(), c.getIpSpaces()));
    assertThat(
        aclTrustOut,
        rejects(untrustToTrustFlow, interfaceNameUntrust, c.getIpAccessLists(), c.getIpSpaces()));
  }

  @Test
  public void testFirewallPolicies() throws IOException {
    Configuration c = parseConfig("firewall-policies");
    String interfaceNameTrust = "ge-0/0/0.0";
    String interfaceNameUntrust = "ge-0/0/1.0";
    String securityPolicyName = "~FROM_ZONE~trust~TO_ZONE~untrust";
    String trustedIpAddr = "1.2.3.5";
    String untrustedIpAddr = "1.2.4.5";

    Flow trustToUntrustFlow = createFlow(trustedIpAddr, untrustedIpAddr);
    Flow untrustToTrustFlow = createFlow(untrustedIpAddr, trustedIpAddr);
    Flow trustToUntrustReturnFlow = createFlow(trustedIpAddr, untrustedIpAddr, State.ESTABLISHED);
    Flow untrustToTrustReturnFlow = createFlow(untrustedIpAddr, trustedIpAddr, State.ESTABLISHED);

    IpAccessList aclTrustOut = c.getInterfaces().get(interfaceNameTrust).getOutgoingFilter();
    IpAccessList aclUntrustOut = c.getInterfaces().get(interfaceNameUntrust).getOutgoingFilter();

    /*
     * Should have six ACLs:
     *  Explicitly defined in the config file:
     *    One from the security policy from trust to untrust
     *  Generated by logic in toVendorIndependent
     *    Two combined outgoing filters for the two interfaces (combines security policies with
     *        egress ACLs)
     *    One permitting existing connections (default firewall behavior)
     *    Two defining security policies for each interface (combines explicit security policy with
     *        implicit security policies like allow existing connection)
     */
    assertThat(
        c.getIpAccessLists().keySet(),
        containsInAnyOrder(
            securityPolicyName,
            aclTrustOut.getName(),
            aclUntrustOut.getName(),
            ACL_NAME_EXISTING_CONNECTION,
            ACL_NAME_SECURITY_POLICY + interfaceNameTrust,
            ACL_NAME_SECURITY_POLICY + interfaceNameUntrust));

    List<IpAccessListLine> aclTrustSPLines =
        c.getIpAccessLists().get(ACL_NAME_SECURITY_POLICY + interfaceNameTrust).getLines();
    List<IpAccessListLine> aclUntrustSPLines =
        c.getIpAccessLists().get(ACL_NAME_SECURITY_POLICY + interfaceNameUntrust).getLines();

    /* Security policy ACLs should have two lines, one for specific policies and one for default */
    assertThat(aclTrustSPLines, iterableWithSize(2));
    assertThat(aclUntrustSPLines, iterableWithSize(2));

    /* Extract the lines for content testing */
    IpAccessListLine aclTrustSecurityPolicyLine = aclTrustSPLines.get(0);
    IpAccessListLine aclUntrustSecurityPolicyLine = aclUntrustSPLines.get(0);
    IpAccessListLine aclSecurityPolicyLine =
        Iterables.getOnlyElement(c.getIpAccessLists().get(securityPolicyName).getLines());

    /* Security policy for traffic to trust zone should just allow existing connections */
    assertThat(
        aclTrustSecurityPolicyLine,
        hasMatchCondition(
            isOrMatchExprThat(
                hasDisjuncts(
                    containsInAnyOrder(new PermittedByAcl(ACL_NAME_EXISTING_CONNECTION))))));

    /*
     * Security policy for traffic to untrust zone should allow existing connections OR matching
     * the security policy
     */
    assertThat(
        aclUntrustSecurityPolicyLine,
        hasMatchCondition(
            isOrMatchExprThat(
                hasDisjuncts(
                    containsInAnyOrder(
                        new PermittedByAcl(ACL_NAME_EXISTING_CONNECTION),
                        new PermittedByAcl(securityPolicyName))))));

    /* Confirm the security policy acl contains logical AND of srcInterface and headerSpace match */
    assertThat(
        aclSecurityPolicyLine,
        hasMatchCondition(
            isAndMatchExprThat(
                hasConjuncts(
                    containsInAnyOrder(
                        new MatchSrcInterface(ImmutableList.of(interfaceNameTrust)),
                        new MatchHeaderSpace(HeaderSpace.builder().build()))))));

    /* Simple flow from trust to untrust should be permitted */
    assertThat(
        aclUntrustOut,
        accepts(trustToUntrustFlow, interfaceNameTrust, c.getIpAccessLists(), c.getIpSpaces()));

    /* Simple flow from untrust to trust should be blocked */
    assertThat(
        aclTrustOut,
        rejects(untrustToTrustFlow, interfaceNameUntrust, c.getIpAccessLists(), c.getIpSpaces()));

    /* Return flow in either direction should be permitted */
    assertThat(
        aclUntrustOut,
        accepts(
            trustToUntrustReturnFlow, interfaceNameTrust, c.getIpAccessLists(), c.getIpSpaces()));
    assertThat(
        aclTrustOut,
        accepts(
            untrustToTrustReturnFlow, interfaceNameUntrust, c.getIpAccessLists(), c.getIpSpaces()));
  }

  @Test
  public void testFirewallZoneAddressBook() throws IOException {
    Configuration c = parseConfig("firewall-zone-address-book");
    String interfaceNameTrust = "ge-0/0/0.0";
    String interfaceNameUntrust = "ge-0/0/1.0";
    // Address on untrust interface's subnet
    String untrustIpAddr = "1.2.4.5";
    // Specific address allowed by the address-book
    String specificAddr = "2.2.2.2";
    // Address not allowed by the address-book
    String notAllowedAddr = "3.3.3.3";

    Flow flowFromSpecificAddr = createFlow(specificAddr, untrustIpAddr);
    Flow flowFromNotAllowedAddr = createFlow(notAllowedAddr, untrustIpAddr);

    IpAccessList aclUntrustOut = c.getInterfaces().get(interfaceNameUntrust).getOutgoingFilter();

    // Should have a an IpSpace in the config corresponding to the trust zone's ADDR1 address
    assertThat(c.getIpSpaces(), hasKey(equalTo("trust~ADDR1")));

    // It should be the only IpSpace
    assertThat(c.getIpSpaces().keySet(), iterableWithSize(1));
    IpSpace ipSpace = Iterables.getOnlyElement(c.getIpSpaces().values());

    // It should contain the specific address
    assertThat(ipSpace, containsIp(new Ip(specificAddr)));

    // It should not contain the address that is not allowed
    assertThat(ipSpace, not(containsIp(new Ip(notAllowedAddr))));

    // Specifically allowed source address should be accepted
    assertThat(
        aclUntrustOut,
        accepts(flowFromSpecificAddr, interfaceNameTrust, c.getIpAccessLists(), c.getIpSpaces()));
    // Source address not covered by the address-book should be rejected
    assertThat(
        aclUntrustOut,
        rejects(flowFromNotAllowedAddr, interfaceNameTrust, c.getIpAccessLists(), c.getIpSpaces()));
  }

  @Test
  public void testFirewallZones() throws IOException {
    Configuration c = parseConfig("firewall-no-policies");
    String interfaceNameTrust = "ge-0/0/0.0";
    String interfaceNameUntrust = "ge-0/0/1.0";
    String zoneTrust = "trust";
    String zoneUntrust = "untrust";
    String trustedIpAddr = "1.2.3.5";
    String untrustedIpAddr = "1.2.4.5";

    Flow trustToUntrustFlow = createFlow(trustedIpAddr, untrustedIpAddr);
    Flow untrustToTrustFlow = createFlow(untrustedIpAddr, trustedIpAddr);

    IpAccessList aclTrustOut = c.getInterfaces().get(interfaceNameTrust).getOutgoingFilter();
    IpAccessList aclUntrustOut = c.getInterfaces().get(interfaceNameUntrust).getOutgoingFilter();

    // Should have two zones
    assertThat(c.getZones().keySet(), containsInAnyOrder(zoneTrust, zoneUntrust));

    // Should have two interfaces
    assertThat(
        c.getInterfaces().keySet(), containsInAnyOrder(interfaceNameTrust, interfaceNameUntrust));

    // Confirm the interfaces are associated with their zones
    assertThat(c.getInterfaces().get(interfaceNameTrust), hasZoneName(equalTo(zoneTrust)));
    assertThat(c.getInterfaces().get(interfaceNameUntrust), hasZoneName(equalTo(zoneUntrust)));

    /* Simple flows should be blocked */
    assertThat(
        aclUntrustOut,
        rejects(trustToUntrustFlow, interfaceNameTrust, c.getIpAccessLists(), c.getIpSpaces()));
    assertThat(
        aclTrustOut,
        rejects(untrustToTrustFlow, interfaceNameUntrust, c.getIpAccessLists(), c.getIpSpaces()));
  }

  @Test
  public void testOspfMetric() throws IOException {
    Configuration config =
        BatfishTestUtils.parseTextConfigs(
                _folder, "org/batfish/grammar/juniper/testconfigs/ospfmetric")
            .get("ospfmetric");
    OspfAreaSummary summary =
        config
            .getDefaultVrf()
            .getOspfProcess()
            .getAreas()
            .get(1L)
            .getSummaries()
            .get(Prefix.parse("10.0.0.0/16"));
    assertThat(summary, not(isAdvertised()));
    assertThat(summary, hasMetric(123L));

    // Defaults
    summary =
        config
            .getDefaultVrf()
            .getOspfProcess()
            .getAreas()
            .get(2L)
            .getSummaries()
            .get(Prefix.parse("10.0.0.0/16"));
    assertThat(summary, isAdvertised());
    assertThat(summary, hasMetric(nullValue()));

    // Interface override
    assertThat(config, hasInterface("fe-1/0/1.0", hasOspfCost(equalTo(17))));
  }

  @Test
  public void testOspfPsk() throws IOException {
    /* allow both encrypted and unencrypted key */
    parseConfig("ospf-psk");
  }

  @Test
  public void testTacplusPsk() throws IOException {
    /* allow both encrypted and unencrypted key */
    parseConfig("tacplus-psk");
  }

  @Test
  public void testInterfaceArp() throws IOException {
    Configuration c = parseConfig("interface-arp");

    /* The additional ARP IP set for irb.0 should appear in the data model */
    assertThat(c, hasInterface("irb.0", hasAdditionalArpIps(hasItem(new Ip("1.0.0.2")))));
  }

  @Test
  public void testInterfaceMtu() throws IOException {
    Configuration c = parseConfig("interfaceMtu");

    /* Properly configured interfaces should be present in respective areas. */
    assertThat(c.getInterfaces().keySet(), equalTo(Collections.singleton("xe-0/0/0:0.0")));
    assertThat(c, hasInterface("xe-0/0/0:0.0", hasMtu(9000)));
  }

  @Test
  public void testOspfInterfaceAreaAssignment() throws IOException {
    Configuration c = parseConfig("ospfInterfaceAreaAssignment");

    /* Properly configured interfaces should be present in respective areas. */
    assertThat(c, hasInterface("xe-0/0/0.0", isOspfPassive(equalTo(false))));
    assertThat(
        c,
        hasDefaultVrf(
            hasOspfProcess(hasArea(0L, OspfAreaMatchers.hasInterfaces(hasItem("xe-0/0/0.0"))))));

    assertThat(c, hasInterface("xe-0/0/0.1", isOspfPassive()));
    assertThat(
        c,
        hasDefaultVrf(
            hasOspfProcess(hasArea(0L, OspfAreaMatchers.hasInterfaces(hasItem("xe-0/0/0.1"))))));
    assertThat(
        c,
        hasDefaultVrf(
            hasOspfProcess(hasArea(1L, OspfAreaMatchers.hasInterfaces(hasItem("xe-0/0/0.1"))))));

    /* The following interfaces should be absent since they have no IP addresses assigned. */
    assertThat(
        c,
        hasDefaultVrf(
            hasOspfProcess(
                hasArea(0L, OspfAreaMatchers.hasInterfaces(not(hasItem("xe-0/0/0.2")))))));

    assertThat(
        c,
        hasDefaultVrf(
            hasOspfProcess(
                hasArea(0L, OspfAreaMatchers.hasInterfaces(not(hasItem("xe-0/0/0.3")))))));
    assertThat(
        c,
        hasDefaultVrf(
            hasOspfProcess(
                hasArea(1L, OspfAreaMatchers.hasInterfaces(not(hasItem("xe-0/0/0.3")))))));
  }

  @Test
  public void testParsingRecovery() {
    String recoveryText =
        CommonUtil.readResource("org/batfish/grammar/juniper/testconfigs/recovery");
    Settings settings = new Settings();
    FlatJuniperCombinedParser cp = new FlatJuniperCombinedParser(recoveryText, settings);
    Flat_juniper_configurationContext ctx = cp.parse();
    FlatJuniperRecoveryExtractor extractor = new FlatJuniperRecoveryExtractor();
    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(extractor, ctx);

    assertThat(extractor.getNumSets(), equalTo(8));
    assertThat(extractor.getNumErrorNodes(), equalTo(8));
  }

  @Test
  public void testRoutingInstanceType() throws IOException {
    Configuration c = parseConfig("routing-instance-type");

    /* All types for now should result in a VRF */
    /* TODO: perhaps some types e.g. forwarding should not result in a VRF */
    assertThat(c, hasVrfs(hasKey("ri-forwarding")));
    assertThat(c, hasVrfs(hasKey("ri-l2vpn")));
    assertThat(c, hasVrfs(hasKey("ri-virtual-router")));
    assertThat(c, hasVrfs(hasKey("ri-virtual-switch")));
    assertThat(c, hasVrfs(hasKey("ri-vrf")));
  }

  @Test
  public void testStaticRoutes() throws IOException {
    Configuration c = parseConfig("static-routes");

    assertThat(c, hasDefaultVrf(hasStaticRoutes(hasItem(hasPrefix(Prefix.parse("1.0.0.0/8"))))));
    assertThat(c, hasVrf("ri2", hasStaticRoutes(hasItem(hasPrefix(Prefix.parse("2.0.0.0/8"))))));
  }
}
