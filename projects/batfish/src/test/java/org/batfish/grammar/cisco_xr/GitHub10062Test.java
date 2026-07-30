package org.batfish.grammar.cisco_xr;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.representation.cisco_xr.CiscoXrConfiguration.computeExtcommunitySetRtName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunityContext;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprEvaluator;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.cisco_xr.CiscoXrConfiguration;
import org.batfish.representation.cisco_xr.ExtcommunitySetRtElemAsColon;
import org.batfish.representation.cisco_xr.ExtcommunitySetRtElemAsDotColon;
import org.batfish.representation.cisco_xr.LiteralUint16;
import org.batfish.representation.cisco_xr.LiteralUint32;
import org.batfish.representation.cisco_xr.VrfAddressFamily;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for https://github.com/batfish/batfish/issues/10062. */
public final class GitHub10062Test {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco_xr/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private @Nonnull CiscoXrConfiguration parseVendorConfig(String hostname) {
    return parseVendorConfig(hostname, new Warnings());
  }

  private @Nonnull CiscoXrConfiguration parseVendorConfig(String hostname, Warnings warnings) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    CiscoXrCombinedParser ciscoXrParser = new CiscoXrCombinedParser(src, settings);
    CiscoXrControlPlaneExtractor extractor =
        new CiscoXrControlPlaneExtractor(
            src,
            ciscoXrParser,
            ConfigurationFormat.CISCO_IOS_XR,
            warnings,
            new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(
            ciscoXrParser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    CiscoXrConfiguration vendorConfiguration =
        (CiscoXrConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    return SerializationUtils.clone(vendorConfiguration);
  }

  private @Nonnull Configuration parseConfig(String hostname) {
    try {
      Map<String, Configuration> configs =
          BatfishTestUtils.parseTextConfigs(_folder, TESTCONFIGS_PREFIX + hostname);
      assertThat(configs, hasKey(hostname));
      return configs.get(hostname);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testBgpVrfRdExtraction() {
    CiscoXrConfiguration c = parseVendorConfig("gh10062");
    assertThat(
        c.getVrfs(),
        hasKeys(
            "default",
            "two_byte",
            "four_byte",
            "dotted",
            "ip_admin",
            "bad_ip_admin",
            "bad_asn4_val4"));
    // type 0: 2-byte ASN administrator, 4-byte assigned number
    assertThat(
        c.getVrfs().get("two_byte").getRouteDistinguisher(),
        equalTo(RouteDistinguisher.from(65000, 4294967295L)));
    // type 2: 4-byte asplain ASN administrator, 2-byte assigned number
    assertThat(
        c.getVrfs().get("four_byte").getRouteDistinguisher(),
        equalTo(RouteDistinguisher.from(4200000001L, 200)));
    // type 2: 4-byte asdot ASN administrator (1.100 = 65636)
    assertThat(
        c.getVrfs().get("dotted").getRouteDistinguisher(),
        equalTo(RouteDistinguisher.from((1L << 16) + 100, 200)));
    // type 1: IP address administrator
    assertThat(
        c.getVrfs().get("ip_admin").getRouteDistinguisher(),
        equalTo(RouteDistinguisher.from(Ip.parse("10.0.0.1"), 200)));
    // invalid: IP administrator requires a 2-byte assigned number
    assertThat(c.getVrfs().get("bad_ip_admin").getRouteDistinguisher(), nullValue());
    // invalid: 4-byte ASN administrator requires a 2-byte assigned number
    assertThat(c.getVrfs().get("bad_asn4_val4").getRouteDistinguisher(), nullValue());
  }

  @Test
  public void testVrfRouteTargetExtraction() {
    // Route-targets under router bgp / vrf / address-family, including a 4-byte asplain
    // ASN administrator (the extcommunity/RT half of GH-10062). The 4-byte forms must both
    // parse cleanly and be extracted onto the VRF address-family import/export sets.
    CiscoXrConfiguration c = parseVendorConfig("gh10062rt");
    assertThat(c.getVrfs(), hasKeys("default", "rt_test"));
    VrfAddressFamily af = c.getVrfs().get("rt_test").getIpv4UnicastAddressFamily();
    assertThat(
        af.getRouteTargetImport(),
        containsInAnyOrder(
            ExtendedCommunity.target(65000L, 100L), ExtendedCommunity.target(4200000001L, 200L)));
    assertThat(
        af.getRouteTargetExport(), containsInAnyOrder(ExtendedCommunity.target(4200000001L, 300L)));
  }

  @Test
  public void testExtcommunitySetRtLocalAdministrator32Extraction() {
    // An RFC 4360 type-0 route target has a 2-byte global administrator and a 4-byte local
    // administrator; IOS-XR accepts this in an extcommunity-set rt. Both this shape and the
    // type-2 shape (4-byte GA, 2-byte LA) must extract, and neither may disturb the other.
    Warnings warnings = new Warnings(true, true, true);
    CiscoXrConfiguration c = parseVendorConfig("gh10062ecrt", warnings);

    // No set may be damaged, and in particular error recovery must not mis-attribute a failure
    // in one set to the set that follows it.
    assertThat(warnings.getParseWarnings(), empty());
    assertThat(c.getExtcommunitySetRts(), hasKeys("ecrt_la32", "ecrt_ga32", "ecrt_mixed"));

    // type 0: 2-byte ASN GA, 4-byte LA
    assertThat(
        c.getExtcommunitySetRts().get("ecrt_la32").getElements(),
        contains(
            new ExtcommunitySetRtElemAsColon(
                new LiteralUint32(65000L), new LiteralUint32(11311111L))));
    // type 2: 4-byte asplain ASN GA, 2-byte LA
    assertThat(
        c.getExtcommunitySetRts().get("ecrt_ga32").getElements(),
        contains(
            new ExtcommunitySetRtElemAsColon(
                new LiteralUint32(4200000001L), new LiteralUint32(100L))));
    // both widths, plus asdot, in one set
    assertThat(
        c.getExtcommunitySetRts().get("ecrt_mixed").getElements(),
        contains(
            new ExtcommunitySetRtElemAsColon(
                new LiteralUint32(65000L), new LiteralUint32(11311111L)),
            new ExtcommunitySetRtElemAsColon(
                new LiteralUint32(4200000001L), new LiteralUint32(100L)),
            new ExtcommunitySetRtElemAsColon(new LiteralUint32(1234L), new LiteralUint32(56L)),
            new ExtcommunitySetRtElemAsDotColon(
                new LiteralUint16(12), new LiteralUint16(34), new LiteralUint16(56))));
  }

  @Test
  public void testExtcommunitySetRtLocalAdministrator32Conversion() {
    Configuration c = parseConfig("gh10062ecrt");
    CommunityContext ctx = CommunityContext.builder().build();

    // Matching: a type-0 route target with a 4-byte local administrator must match itself, and
    // must not match a route target that merely shares its global administrator.
    {
      CommunityMatchExpr expr =
          c.getCommunityMatchExprs().get(computeExtcommunitySetRtName("ecrt_la32"));
      assertTrue(
          expr.accept(
              ctx.getCommunityMatchExprEvaluator(), ExtendedCommunity.target(65000L, 11311111L)));
      assertFalse(
          expr.accept(ctx.getCommunityMatchExprEvaluator(), ExtendedCommunity.target(65000L, 1L)));
    }

    // Setting: the literal community produced must round-trip to the same 4-byte LA.
    {
      CommunitySetExpr expr =
          c.getCommunitySetExprs().get(computeExtcommunitySetRtName("ecrt_mixed"));
      assertThat(
          expr.accept(CommunitySetExprEvaluator.instance(), ctx),
          equalTo(
              CommunitySet.of(
                  ExtendedCommunity.target(65000L, 11311111L),
                  ExtendedCommunity.target(4200000001L, 100L),
                  ExtendedCommunity.target(1234L, 56L),
                  ExtendedCommunity.target((12L << 16) | 34L, 56L))));
    }
  }
}
