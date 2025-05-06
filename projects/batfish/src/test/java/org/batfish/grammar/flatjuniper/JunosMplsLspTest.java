package org.batfish.grammar.flatjuniper;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JunosMplsLspTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/juniper/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        java.util.Arrays.stream(configurationNames)
            .map(s -> TESTCONFIGS_PREFIX + s)
            .toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  @Test
  public void testMplsLspComprehensiveParsing() throws IOException {
    String hostname = "mpls-lsp-comprehensive";
    Batfish batfish = getBatfishForConfigurationNames(hostname);

    // Ensure we can parse and extract configs successfully
    assertThat(batfish.loadConfigurations(batfish.getSnapshot()).get(hostname), notNullValue());

    // Verify the configuration format
    assertThat(
        batfish.loadConfigurations(batfish.getSnapshot()).get(hostname).getConfigurationFormat(),
        equalTo(ConfigurationFormat.FLAT_JUNIPER));

    // Get the vendor-specific configuration
    JuniperConfiguration vc =
        (JuniperConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    // Verify that the configuration was parsed correctly
    assertThat(vc, notNullValue());

    // Since we're using _null rules, we're not extracting the MPLS LSPs
    // We just verify that the configuration was parsed successfully
  }

  @Test
  public void testMplsLspNewParametersParsing() throws IOException {
    String hostname = "mpls-lsp-comprehensive";
    Batfish batfish = getBatfishForConfigurationNames(hostname);

    // Ensure we can parse and extract configs successfully with the new parameters
    assertThat(batfish.loadConfigurations(batfish.getSnapshot()).get(hostname), notNullValue());

    // Verify the configuration format
    assertThat(
        batfish.loadConfigurations(batfish.getSnapshot()).get(hostname).getConfigurationFormat(),
        equalTo(ConfigurationFormat.FLAT_JUNIPER));

    // Get the vendor-specific configuration
    JuniperConfiguration vc =
        (JuniperConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    // Verify that the configuration was parsed correctly
    assertThat(vc, notNullValue());

    // Test focuses on parsing success since we're using _null rules
    // We're verifying that all the new parameters can be parsed without errors
  }
}
