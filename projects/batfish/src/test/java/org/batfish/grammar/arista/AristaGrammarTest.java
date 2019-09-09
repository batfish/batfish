package org.batfish.grammar.arista;

import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.grammar.cisco.CiscoCombinedParser;
import org.batfish.grammar.cisco.CiscoControlPlaneExtractor;
import org.batfish.main.Batfish;
import org.batfish.representation.cisco.CiscoConfiguration;
import org.batfish.representation.cisco.eos.AristaBgpVlan;
import org.batfish.representation.cisco.eos.AristaBgpVlanAwareBundle;
import org.junit.Test;

@ParametersAreNonnullByDefault
public class AristaGrammarTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/arista/testconfigs/";

  private @Nonnull CiscoConfiguration parseVendorConfig(String hostname) {
    String src = CommonUtil.readResource(TESTCONFIGS_PREFIX + hostname);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    settings.setDebugFlags(ImmutableList.of(CiscoCombinedParser.DEBUG_FLAG_USE_ARISTA_BGP));
    CiscoCombinedParser ciscoParser =
        new CiscoCombinedParser(src, settings, ConfigurationFormat.ARISTA);
    CiscoControlPlaneExtractor extractor =
        new CiscoControlPlaneExtractor(
            src, ciscoParser, ConfigurationFormat.ARISTA, new Warnings());
    ParserRuleContext tree =
        Batfish.parse(
            ciscoParser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(tree);
    CiscoConfiguration vendorConfiguration =
        (CiscoConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    SerializationUtils.clone(vendorConfiguration);
    return vendorConfiguration;
  }

  @Test
  public void testTopLevelBgpExtraction() {
    CiscoConfiguration config = parseVendorConfig("arista_bgp");
    assertTrue(config.getAristaBgp().getShutdown());
    assertThat(config.getAristaBgp().getRouterId(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(config.getAristaBgp().getKeepAliveTimer(), equalTo(3));
    assertThat(config.getAristaBgp().getHoldTimer(), equalTo(9));
  }

  @Test
  public void testBgpVlansExtraction() {
    CiscoConfiguration config = parseVendorConfig("arista_bgp_vlans");
    {
      AristaBgpVlanAwareBundle bundle = config.getAristaBgp().getVlanAwareBundles().get("Tenant_A");
      assertThat(bundle, notNullValue());
      assertThat(bundle.getRd(), equalTo(RouteDistinguisher.parse("192.168.255.8:10101")));
      assertThat(bundle.getRtImport(), equalTo(ExtendedCommunity.target(10101, 10101)));
      assertThat(bundle.getRtExport(), equalTo(ExtendedCommunity.target(10101, 10101)));
      assertThat(bundle.getVlans(), equalTo(IntegerSpace.builder().including(1, 110, 111).build()));
    }

    {
      AristaBgpVlan vlan = config.getAristaBgp().getVlans().get(300);
      assertThat(vlan, notNullValue());
      assertThat(vlan.getRd(), equalTo(RouteDistinguisher.parse("192.168.255.100:10103")));
      assertThat(vlan.getRtImport(), equalTo(ExtendedCommunity.target(10101, 10103)));
      assertThat(vlan.getRtExport(), equalTo(ExtendedCommunity.target(10101, 10103)));
    }
  }
}
