package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.IkeGateway.LocalIdentityType.DISTINGUISHED_NAME;
import static org.batfish.representation.juniper.IkeGateway.LocalIdentityType.HOSTNAME;
import static org.batfish.representation.juniper.IkeGateway.LocalIdentityType.INET;
import static org.batfish.representation.juniper.IkeGateway.LocalIdentityType.INET6;
import static org.batfish.representation.juniper.IkeGateway.LocalIdentityType.KEY_ID;
import static org.batfish.representation.juniper.IkeGateway.LocalIdentityType.USER_AT_HOSTNAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.IkeGateway;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosIkeGatewayIdentityTest {

  private static final String CONFIG_NAME = "ike-gateway-identities";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndWarnings() throws IOException {
    Batfish batfish = getBatfish(_folder, CONFIG_NAME);
    Map<String, IkeGateway> gateways =
        getVendorConfiguration(batfish, CONFIG_NAME).getMasterLogicalSystem().getIkeGateways();

    assertThat(gateways.get("GENERAL").getGeneralIkeId(), equalTo(true));
    assertIdentity(gateways.get("DN"), DISTINGUISHED_NAME, null);
    assertIdentity(gateways.get("HOST"), HOSTNAME, "vpn.example.com");
    assertIdentity(gateways.get("V4"), INET, "192.0.2.1");
    assertIdentity(gateways.get("V6"), INET6, "2001:db8::1");
    assertIdentity(gateways.get("KEY"), KEY_ID, "gateway-key-id");
    assertIdentity(gateways.get("USER"), USER_AT_HOSTNAME, "user@example.com");
    assertThat(
        getParseWarnings(batfish, CONFIG_NAME),
        containsInAnyOrder(
            isTodo("general-ikeid"),
            isTodo("distinguished-name"),
            isTodo("hostname vpn.example.com"),
            isTodo("inet 192.0.2.1"),
            isTodo("inet6 2001:db8::1"),
            isTodo("key-id gateway-key-id"),
            isTodo("user-at-hostname user@example.com")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    Warnings warnings = ccae.getWarnings().getOrDefault(CONFIG_NAME, new Warnings());
    assertThat(warnings.getRedFlagWarnings(), empty());
    assertThat(warnings.getUnimplementedWarnings(), empty());
  }

  private static void assertIdentity(
      IkeGateway gateway, IkeGateway.LocalIdentityType type, String identity) {
    assertThat(gateway.getLocalIdentityType(), equalTo(type));
    assertThat(gateway.getLocalIdentity(), identity == null ? nullValue() : equalTo(identity));
  }
}
