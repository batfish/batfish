package org.batfish.vendor.cisco_ftd;

import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.cisco_ftd.grammar.FtdCombinedParser;
import org.batfish.vendor.cisco_ftd.grammar.FtdControlPlaneExtractor;
import org.batfish.vendor.cisco_ftd.representation.FtdConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FtdVpnTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Configuration parseConfig(String fileText) throws IOException {
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    FtdCombinedParser parser = new FtdCombinedParser(fileText, settings);
    Warnings warnings = new Warnings(true, true, true);
    FtdControlPlaneExtractor extractor =
        new FtdControlPlaneExtractor(fileText, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, parser.parse());
    FtdConfiguration vc = (FtdConfiguration) extractor.getVendorConfiguration();
    vc.setVendor(ConfigurationFormat.CISCO_FTD);
    return vc.toVendorIndependentConfigurations().get(0);
  }

  @Test
  public void testVpnConversion() throws IOException {
    String text =
        "crypto ikev2 policy 10\n"
            + " encryption aes-256\n"
            + " integrity sha256\n"
            + " group 14\n"
            + " lifetime seconds 86400\n"
            + "crypto ipsec transform-set ESP-AES-SHA esp-aes esp-sha-hmac\n"
            + "crypto map CMAP 10 match address CRYPTO_ACL\n"
            + "crypto map CMAP 10 set peer 1.2.3.4\n"
            + "crypto map CMAP 10 set ikev2-policy 10\n"
            + "crypto map CMAP 10 set transform-set ESP-AES-SHA\n"
            + "interface GigabitEthernet0/0\n"
            + " nameif outside\n"
            + " ip address 1.1.1.1 255.255.255.0\n"
            + " crypto map CMAP\n"
            + "tunnel-group 1.2.3.4 type ipsec-l2l\n"
            + "tunnel-group 1.2.3.4 ipsec-attributes\n"
            + " ikev2 remote-authentication pre-shared-key secret123\n";

    Configuration c = parseConfig(text);
    assertThat(c.getIkePhase1Policies(), hasKey("10"));
    assertThat(c.getIkePhase1Proposals(), notNullValue());
    assertThat(c.getIpsecPhase2Policies(), notNullValue());
    assertThat(c.getIpsecPeerConfigs(), notNullValue());
    assertThat(c.getIkePhase1Keys(), hasKey("1.2.3.4"));
    assertThat(c.getIkePhase1Keys().get("1.2.3.4").getKeyHash(), equalTo("secret123"));
  }
}
