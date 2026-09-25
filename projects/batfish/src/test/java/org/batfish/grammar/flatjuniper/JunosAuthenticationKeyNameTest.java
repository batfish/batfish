package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperAuthenticationKey;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosAuthenticationKeyNameTest {

  private static final String HOSTNAME = "authentication-key-name";
  private static final String KEY_NAME =
      "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndWarnings() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperAuthenticationKey key =
        getVendorConfiguration(batfish, HOSTNAME)
            .getMasterLogicalSystem()
            .getAuthenticationKeyChains()
            .get("MACSEC-KEYS")
            .getKeys()
            .get("1");

    assertThat(key.getKeyName(), equalTo(KEY_NAME));
    assertThat(getParseWarnings(batfish, HOSTNAME), contains(isTodo("key-name " + KEY_NAME)));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    Warnings warnings = ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings());
    assertThat(warnings.getRedFlagWarnings(), empty());
    assertThat(warnings.getUnimplementedWarnings(), empty());
  }
}
