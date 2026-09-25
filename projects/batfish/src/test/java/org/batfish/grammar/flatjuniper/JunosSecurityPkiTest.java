package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.PKI_CA_PROFILE;
import static org.batfish.representation.juniper.JuniperStructureType.ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.PKI_CA_PROFILE_ROUTING_INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.PkiCaProfile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosSecurityPkiTest {

  private static final String HOSTNAME = "security-pki-ca-profile";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionReferencesAndWarnings() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    PkiCaProfile profile =
        getVendorConfiguration(batfish, HOSTNAME)
            .getMasterLogicalSystem()
            .getPkiCaProfiles()
            .get("ROOT-CA");

    assertThat(profile.getName(), equalTo("ROOT-CA"));
    assertThat(profile.getCaIdentity(), equalTo("root.example.com"));
    assertThat(profile.getRevocationCheckDisabled(), equalTo(true));
    assertThat(profile.getRoutingInstance(), equalTo("MGMT"));
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(ccae, hasDefinedStructure(filename, PKI_CA_PROFILE, "ROOT-CA"));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ROUTING_INSTANCE, "MGMT", PKI_CA_PROFILE_ROUTING_INSTANCE));
    Warnings warnings = ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings());
    assertThat(warnings.getRedFlagWarnings(), empty());
    assertThat(warnings.getUnimplementedWarnings(), empty());
  }
}
