package org.batfish.vendor.sros.grammar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.vendor.sros.representation.BgpGroup;
import org.batfish.vendor.sros.representation.BgpNeighbor;
import org.batfish.vendor.sros.representation.BgpProcess;
import org.batfish.vendor.sros.representation.Card;
import org.batfish.vendor.sros.representation.PolicyAction;
import org.batfish.vendor.sros.representation.PolicyStatement;
import org.batfish.vendor.sros.representation.PrefixList;
import org.batfish.vendor.sros.representation.PrefixListEntry;
import org.batfish.vendor.sros.representation.Router;
import org.batfish.vendor.sros.representation.RouterInterface;
import org.batfish.vendor.sros.representation.SrosConfiguration;
import org.junit.Test;

/** Tests of SR-OS feature extraction (P4): the canonical tree, the preprocessor, and the model. */
public final class SrosExtractionTest {

  /** The captured r1 config extracts the full characterized feature set with no warnings. */
  @Test
  public void testR1Extraction() {
    SrosConfiguration vc = parseVendorConfig("r1_admin_show_configuration.txt");
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    assertThat(vc.getWarnings().getUnimplementedWarnings(), empty());

    // Hardware: card 1 (iom-1) with mda 1 (me6-100gb-qsfp28).
    assertThat(vc.getCards(), hasKey(1));
    Card card = vc.getCards().get(1);
    assertThat(card.getCardType(), equalTo("iom-1"));
    assertThat(card.getMdas(), hasKey(1));
    assertThat(card.getMdas().get(1).getMdaType(), equalTo("me6-100gb-qsfp28"));

    // Ports: the connector with breakout, and the breakout sub-port. Both admin-state enable.
    assertThat(vc.getPorts(), hasKey("1/1/c1"));
    assertThat(vc.getPorts(), hasKey("1/1/c1/1"));
    assertThat(vc.getPorts().get("1/1/c1").getAdminStateEnable(), equalTo(Boolean.TRUE));
    assertThat(vc.getPorts().get("1/1/c1").getBreakout(), equalTo("c1-100g"));
    assertThat(vc.getPorts().get("1/1/c1/1").getAdminStateEnable(), equalTo(Boolean.TRUE));
    assertThat(vc.getPorts().get("1/1/c1/1").getBreakout(), nullValue());

    // Router Base: AS 65001, two interfaces.
    assertThat(vc.getRouters(), hasKey("Base"));
    Router base = vc.getRouters().get("Base");
    assertThat(base.getAutonomousSystem(), equalTo(65001L));
    assertThat(base.getInterfaces().keySet(), containsInAnyOrder("system", "to-r2"));

    RouterInterface system = base.getInterfaces().get("system");
    assertThat(system.getPort(), nullValue());
    assertThat(system.getPrimaryAddress(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(system.getPrimaryPrefixLength(), equalTo(32));
    assertThat(system.getPrimaryConcreteAddress().toString(), equalTo("1.1.1.1/32"));

    RouterInterface toR2 = base.getInterfaces().get("to-r2");
    assertThat(toR2.getPort(), equalTo("1/1/c1/1"));
    assertThat(toR2.getPrimaryAddress(), equalTo(Ip.parse("10.0.0.0")));
    assertThat(toR2.getPrimaryPrefixLength(), equalTo(31));

    // BGP: router-id, one group, one neighbor referencing it.
    BgpProcess bgp = base.getBgpProcess();
    assertThat(bgp, not(nullValue()));
    assertThat(bgp.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(bgp.getGroups(), hasKey("ebgp"));
    BgpGroup ebgp = bgp.getGroups().get("ebgp");
    assertThat(ebgp.getPeerAs(), equalTo(65002L));
    assertThat(ebgp.getImportPolicies(), contains("import-all"));
    assertThat(ebgp.getExportPolicies(), contains("export-system"));
    assertThat(bgp.getNeighbors(), hasKey("10.0.0.1"));
    BgpNeighbor neighbor = bgp.getNeighbors().get("10.0.0.1");
    assertThat(neighbor.getGroup(), equalTo("ebgp"));

    // policy-options: one prefix-list with one exact entry; two policy-statements.
    assertThat(vc.getPrefixLists(), hasKey("system-pfx"));
    PrefixList pfx = vc.getPrefixLists().get("system-pfx");
    assertThat(
        pfx.getEntries(),
        contains(new PrefixListEntry(Prefix.parse("1.1.1.1/32"), PrefixListEntry.Type.EXACT)));

    assertThat(
        vc.getPolicyStatements().keySet(), containsInAnyOrder("export-system", "import-all"));
    PolicyStatement exportSystem = vc.getPolicyStatements().get("export-system");
    assertThat(exportSystem.getEntries(), hasKey(10L));
    assertThat(exportSystem.getEntries().get(10L).getFromPrefixLists(), contains("system-pfx"));
    assertThat(exportSystem.getEntries().get(10L).getAction(), equalTo(PolicyAction.ACCEPT));
    PolicyStatement importAll = vc.getPolicyStatements().get("import-all");
    assertThat(importAll.getEntries().keySet(), empty());
    assertThat(importAll.getDefaultAction(), equalTo(PolicyAction.ACCEPT));
  }

  /**
   * r1's security/ssh/user-params/persistent-indices subtrees are silently skipped (no warnings).
   */
  @Test
  public void testR1UnmodeledSubtreesSilentlySkipped() {
    SrosConfiguration vc = parseVendorConfig("r1_admin_show_configuration.txt");
    // No hostname configured in r1 (no `system name`); the system subtree is otherwise unread.
    assertThat(vc.getHostname(), nullValue());
    assertThat(vc.getWarnings().getUnimplementedWarnings(), empty());
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
  }

  /** A `system name` leaf becomes the Batfish hostname. */
  @Test
  public void testHostnameExtraction() {
    SrosConfiguration vc = parseVendorConfig("hostname.txt");
    assertThat(vc.getHostname(), equalTo("sros-r1"));
  }

  /**
   * apply-groups expansion: a group applied at {@code router "Base"} contributes {@code bgp group
   * "ebgp" peer-as}, while the locally-configured import policy is preserved (local wins).
   */
  @Test
  public void testApplyGroupsExpansion() {
    SrosConfiguration vc = parseVendorConfig("apply_groups.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    BgpGroup ebgp = vc.getRouters().get("Base").getBgpProcess().getGroups().get("ebgp");
    assertThat(ebgp.getPeerAs(), equalTo(65010L)); // inherited from the group
    assertThat(ebgp.getImportPolicies(), contains("import-all")); // local config preserved
  }

  /**
   * apply-groups with a regex list-key ({@code interface "<to-.*>"}) grafts onto matching branches,
   * and {@code apply-groups-exclude} suppresses inheritance at a branch.
   */
  @Test
  public void testApplyGroupsRegexAndExclude() {
    SrosConfiguration vc = parseVendorConfig("apply_groups_regex.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    Router base = vc.getRouters().get("Base");
    // to-r2 matches the regex key and inherits prefix-length 31.
    assertThat(base.getInterfaces().get("to-r2").getPrimaryPrefixLength(), equalTo(31));
    // to-r3 excludes the group, so it does not inherit prefix-length.
    assertThat(base.getInterfaces().get("to-r3").getPrimaryPrefixLength(), nullValue());
  }

  /**
   * delete edit: a trailing flat {@code /configure router "Base" delete interface "to-r2"} removes
   * that interface from the materialized model, leaving the others.
   */
  @Test
  public void testDeleteEdit() {
    SrosConfiguration vc = parseVendorConfig("delete_edit.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    Router base = vc.getRouters().get("Base");
    assertThat(base.getInterfaces().keySet(), contains("system"));
  }

  private static @Nonnull SrosConfiguration parseVendorConfig(String filename) {
    String src = readResource(TESTCONFIGS_PREFIX + filename, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    SrosCombinedParser parser = new SrosCombinedParser(src, settings);
    Warnings warnings = new Warnings(true, true, true);
    SrosControlPlaneExtractor extractor =
        new SrosControlPlaneExtractor(src, parser, warnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    SrosConfiguration vc = (SrosConfiguration) extractor.getVendorConfiguration();
    vc.setFilename(TESTCONFIGS_PREFIX + filename);
    // Crash if not serializable.
    vc = SerializationUtils.clone(vc);
    vc.setWarnings(warnings);
    return vc;
  }

  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/sros/grammar/testconfigs/";
}
