package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Ip.ZERO;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasNumReferrers;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperConfiguration.generateResolutionRibImportPolicyName;
import static org.batfish.representation.juniper.JuniperStructureType.POLICY_STATEMENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.Resolution;
import org.batfish.representation.juniper.ResolutionRib;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosResolutionTest {

  private static final String HOSTNAME = "resolution";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndReferences() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration config = getVendorConfiguration(batfish, HOSTNAME);
    Resolution defaultResolution =
        config.getMasterLogicalSystem().getDefaultRoutingInstance().getResolution();
    Resolution clientResolution =
        config.getMasterLogicalSystem().getRoutingInstances().get("CLIENT").getResolution();

    assertThat(defaultResolution.getPreserveNexthopHierarchy(), is(true));
    ResolutionRib inet0 = defaultResolution.getRibs().get("inet.0");
    assertThat(inet0.getImportPolicies(), contains("GENERIC"));
    assertThat(inet0.getInetImportPolicies(), contains("IPV4"));
    assertThat(inet0.getInet6ImportPolicies(), contains("IPV6"));
    assertThat(inet0.getIsoImportPolicies(), contains("ISO"));
    assertThat(inet0.getResolutionRibs(), contains("CLIENT.inet.0", "inet.3"));
    assertThat(inet0.getInetResolutionRibs(), contains("inet.0", "inet.3"));
    assertThat(inet0.getInet6ResolutionRibs(), contains("inet6.0", "inet6.3"));
    assertThat(inet0.getIsoResolutionRibs(), contains("iso.0"));
    ResolutionRib clientInet60 = clientResolution.getRibs().get("CLIENT.inet6.0");
    assertThat(clientInet60.getInet6ImportPolicies(), contains("IPV6"));

    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("preserve-nexthop-hierarchy"),
            isTodo("inet6-import IPV6"),
            isTodo("iso-import ISO"),
            isTodo("resolution-ribs CLIENT.inet.0"),
            isTodo("resolution-ribs inet.3"),
            isTodo("inet-resolution-ribs inet.0"),
            isTodo("inet-resolution-ribs inet.3"),
            isTodo("inet6-resolution-ribs inet6.0"),
            isTodo("inet6-resolution-ribs inet6.3"),
            isTodo("iso-resolution-ribs iso.0"),
            isTodo("inet6-import IPV6")));

    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(ccae, hasNumReferrers(filename, POLICY_STATEMENT, "GENERIC", 1));
    assertThat(ccae, hasNumReferrers(filename, POLICY_STATEMENT, "IPV4", 2));
    assertThat(ccae, hasNumReferrers(filename, POLICY_STATEMENT, "IPV6", 2));
    assertThat(ccae, hasNumReferrers(filename, POLICY_STATEMENT, "ISO", 1));
    assertThat(
        ccae.getWarnings().getOrDefault(filename, new Warnings()).getRedFlagWarnings(), empty());
  }

  @Test
  public void testConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    Configuration config = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);

    String defaultPolicyName = generateResolutionRibImportPolicyName(DEFAULT_VRF_NAME);
    assertThat(config.getDefaultVrf().getResolutionPolicy(), equalTo(defaultPolicyName));
    assertThat(config.getRoutingPolicies(), hasKey(defaultPolicyName));
    RoutingPolicy defaultPolicy = config.getRoutingPolicies().get(defaultPolicyName);
    assertTrue(defaultPolicy.processReadOnly(new ConnectedRoute(Prefix.create(ZERO, 8), "i")));
    assertTrue(defaultPolicy.processReadOnly(new ConnectedRoute(Prefix.parse("1.0.0.0/8"), "i")));
    assertFalse(defaultPolicy.processReadOnly(new ConnectedRoute(Prefix.parse("2.0.0.0/8"), "i")));

    String clientPolicyName = generateResolutionRibImportPolicyName("CLIENT");
    assertThat(config.getVrfs().get("CLIENT").getResolutionPolicy(), equalTo(clientPolicyName));
    assertThat(config.getRoutingPolicies(), hasKey(clientPolicyName));
    RoutingPolicy clientPolicy = config.getRoutingPolicies().get(clientPolicyName);
    assertFalse(clientPolicy.processReadOnly(new ConnectedRoute(Prefix.create(ZERO, 8), "i")));
    assertTrue(clientPolicy.processReadOnly(new ConnectedRoute(Prefix.parse("1.0.0.0/8"), "i")));
  }
}
