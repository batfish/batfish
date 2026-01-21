package org.batfish.grammar.palo_alto;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.Trees;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.config.Settings;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Test;

public class PaloAltoReproductionTest {

  @Test
  public void testSecurityRuleWithUuid() {
    String config =
        "set config devices localhost.localdomain vsys vsys1 rulebase security rules Test-Rule"
            + " 00000000-0000-0000-0000-000000000000 to Internal\n";
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(config, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    CommonTokenStream tokens = parser.getTokens();
    tokens.fill();
    for (Token t : tokens.getTokens()) {
      System.out.println(
          "TOKEN: "
              + t.toString()
              + " TYPE: "
              + parser.getParser().getVocabulary().getSymbolicName(t.getType()));
    }

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(config, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    // We expect this to FAIL currently, producing warnings or errors.
    // If the bug exists, we should see warnings about unrecognized syntax.
    // We expect the fix to resolve the warning, so we should NOT find "unrecognized syntax".
    System.out.println("Tree: " + Trees.toStringTree(tree, parser.getParser()));

    for (ParseWarning pw : warnings.getParseWarnings()) {
      System.out.println("Computed Warning: " + pw);
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning: " + pw.getComment());
      }
    }
  }

  @Test
  public void testUserIdCollector() {
    String config =
        "set user-id-collector setting enable-mapping-timeout yes\n"
            + "set user-id-collector setting ip-user-mapping-timeout 45\n";
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(config, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(config, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    System.out.println("Tree: " + Trees.toStringTree(tree, parser.getParser()));

    for (ParseWarning pw : warnings.getParseWarnings()) {
      System.out.println("Computed Warning: " + pw);
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning: " + pw.getComment());
      }
    }
  }

  @Test
  public void testUserExample2Warnings() throws java.io.IOException {
    String src =
        org.batfish.common.util.Resources.readResource(
            "org/batfish/grammar/palo_alto/testconfigs/user-example-2",
            java.nio.charset.StandardCharsets.UTF_8);
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(src, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(src, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    System.out.println("Tree (User-Example-2): " + Trees.toStringTree(tree, parser.getParser()));

    for (ParseWarning pw : warnings.getParseWarnings()) {
      System.out.println("User-Example-2 Warning: " + pw);
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning in user-example-2: " + pw.getComment());
      }
    }
  }

  @Test
  public void testSecurityRuleWithQuotedName() {
    String config =
        "set config devices localhost.localdomain vsys vsys1 rulebase security rules"
            + " \"test-quoted-rule\" 00000000-0000-0000-0000-000000000000 to External\n"
            + "set config devices localhost.localdomain vsys vsys1 rulebase security rules"
            + " \"test-quoted-rule\" 00000000-0000-0000-0000-000000000000 from Internal\n"
            + "set config devices localhost.localdomain vsys vsys1 rulebase security rules"
            + " \"test-quoted-rule\" 00000000-0000-0000-0000-000000000000 source 172.17.200.0/21\n"
            + "set config devices localhost.localdomain vsys vsys1 rulebase security rules"
            + " \"test-quoted-rule\" 00000000-0000-0000-0000-000000000000 destination 10.0.0.0/8\n"
            + "set config devices localhost.localdomain vsys vsys1 rulebase security rules"
            + " \"test-quoted-rule\" 00000000-0000-0000-0000-000000000000 action allow\n"
            + "set config devices localhost.localdomain vsys vsys1 rulebase security rules"
            + " \"test-quoted-rule\" 00000000-0000-0000-0000-000000000000 service any\n"
            + "set config devices localhost.localdomain vsys vsys1 rulebase security rules"
            + " \"test-quoted-rule\" 00000000-0000-0000-0000-000000000000 application any\n";
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(config, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(config, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    for (ParseWarning pw : warnings.getParseWarnings()) {
      System.out.println("Quoted-Rule Warning: " + pw);
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning for quoted rule name: "
                + pw.getComment());
      }
    }
  }

  /**
   * Test that multiple security rules can be defined in the same rulebase. Verifies the fix for
   * rulebase grammar allowing multiple rule definitions.
   */
  @Test
  public void testMultipleSecurityRules() {
    String config =
        "set config devices localhost.localdomain vsys vsys1 rulebase security rules rule1"
            + " 11111111-1111-1111-1111-111111111111 to External from Internal action allow\n"
            + "set config devices localhost.localdomain vsys vsys1 rulebase security rules rule2"
            + " 22222222-2222-2222-2222-222222222222 to Internal from External action deny\n"
            + "set config devices localhost.localdomain vsys vsys1 rulebase security rules rule3"
            + " 33333333-3333-3333-3333-333333333333 to DMZ from Internal action allow\n";
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(config, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(config, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    for (ParseWarning pw : warnings.getParseWarnings()) {
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning for multiple security rules: "
                + pw.getComment());
      }
    }
  }

  /**
   * Test that PBF (Policy Based Forwarding) rules can have multiple properties. Verifies the fix
   * for rulebase grammar allowing multiple PBF rule properties.
   */
  @Test
  public void testPbfRuleMultipleProperties() {
    String config =
        "set config devices localhost.localdomain vsys vsys1 rulebase pbf rules pbf-rule-1"
            + " 00000000-0000-0000-0000-000000000000 to Trust\n"
            + "set config devices localhost.localdomain vsys vsys1 rulebase pbf rules pbf-rule-1"
            + " 00000000-0000-0000-0000-000000000000 from Untrust\n"
            + "set config devices localhost.localdomain vsys vsys1 rulebase pbf rules pbf-rule-1"
            + " 00000000-0000-0000-0000-000000000000 source any\n";
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(config, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(config, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    for (ParseWarning pw : warnings.getParseWarnings()) {
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning for PBF rules: " + pw.getComment());
      }
    }
  }

  /**
   * Test that tags can have multiple properties defined. Verifies the fix for tag grammar allowing
   * multiple tag properties.
   */
  @Test
  public void testTagMultipleProperties() {
    String config =
        "set config devices localhost.localdomain vsys vsys1 tag test-tag color red\n"
            + "set config devices localhost.localdomain vsys vsys1 tag test-tag comments"
            + " \"Production tag\"\n"
            + "set config devices localhost.localdomain vsys vsys1 tag another-tag color blue\n"
            + "set config devices localhost.localdomain vsys vsys1 tag another-tag comments \"Test"
            + " tag\"\n";
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(config, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(config, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    for (ParseWarning pw : warnings.getParseWarnings()) {
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning for tags: " + pw.getComment());
      }
    }
  }

  /**
   * Test that zones can have multiple network types defined. Verifies the fix for zone grammar
   * allowing multiple network configurations.
   */
  @Test
  public void testZoneMultipleNetworks() {
    String config =
        "set config devices localhost.localdomain vsys vsys1 zone trust network layer3"
            + " ethernet1/1\n"
            + "set config devices localhost.localdomain vsys vsys1 zone trust network layer3"
            + " ethernet1/2\n"
            + "set config devices localhost.localdomain vsys vsys1 zone untrust network layer3"
            + " ethernet2/1\n";
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(config, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(config, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    for (ParseWarning pw : warnings.getParseWarnings()) {
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning for zone networks: " + pw.getComment());
      }
    }
  }

  /**
   * Test that virtual routers can have multiple protocol configurations. Verifies the fix for
   * virtual_router grammar allowing multiple protocols.
   */
  @Test
  public void testVirtualRouterMultipleProtocols() {
    String config =
        "set config devices localhost.localdomain network virtual-router vr1 protocol bgp enable"
            + " yes\n"
            + "set config devices localhost.localdomain network virtual-router vr1 protocol ospf"
            + " enable yes\n"
            + "set config devices localhost.localdomain network virtual-router vr1 admin-dists ebgp"
            + " 20\n";
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(config, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(config, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    for (ParseWarning pw : warnings.getParseWarnings()) {
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning for virtual router protocols: "
                + pw.getComment());
      }
    }
  }

  /**
   * Test that VSYS can have multiple sub-configurations. Verifies the fix for vsys grammar allowing
   * multiple configuration blocks.
   */
  @Test
  public void testVsysMultipleConfigs() {
    String config =
        "set config devices localhost.localdomain vsys vsys1 display-name \"Production VSYS\"\n"
            + "set config devices localhost.localdomain vsys vsys1 zone trust network layer3"
            + " ethernet1/1\n"
            + "set config devices localhost.localdomain vsys vsys1 region Europe\n";
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(config, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(config, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    for (ParseWarning pw : warnings.getParseWarnings()) {
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning for VSYS: " + pw.getComment());
      }
    }
  }

  /**
   * Test that interfaces can have multiple layer3 properties. Verifies the fix for interface
   * grammar allowing multiple layer3 configurations.
   */
  @Test
  public void testInterfaceMultipleLayer3() {
    String config =
        "set config devices localhost.localdomain network interface ethernet ethernet1/1 layer3 ip"
            + " 10.0.0.1/24\n"
            + "set config devices localhost.localdomain network interface ethernet ethernet1/1"
            + " layer3 mtu 1500\n";
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(config, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(config, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    for (ParseWarning pw : warnings.getParseWarnings()) {
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning for interface layer3: "
                + pw.getComment());
      }
    }
  }

  /**
   * Test that virtual wires can have multiple properties. Verifies the fix for virtual_wire grammar
   * allowing multiple properties.
   */
  @Test
  public void testVirtualWireMultipleProperties() {
    String config =
        "set config devices localhost.localdomain network virtual-wire vw1 interface1 ethernet1/1\n"
            + "set config devices localhost.localdomain network virtual-wire vw1 interface2"
            + " ethernet1/2\n"
            + "set config devices localhost.localdomain network virtual-wire vw1 tag zone1\n";
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(config, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(config, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    for (ParseWarning pw : warnings.getParseWarnings()) {
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning for virtual wire: " + pw.getComment());
      }
    }
  }

  /**
   * Test that group mappings can have multiple properties. Verifies the fix for group_mapping
   * grammar allowing multiple mappings.
   */
  @Test
  public void testGroupMappingMultipleProperties() {
    String config =
        "set config devices localhost.localdomain vsys vsys1 group-mapping test-group group-object"
            + " admins\n"
            + "set config devices localhost.localdomain vsys vsys1 group-mapping test-group"
            + " user-object admin\n"
            + "set config devices localhost.localdomain vsys vsys1 group-mapping test-group"
            + " user-name admin-user\n";
    Settings settings = new Settings();
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(config, settings, null);
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    Warnings warnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(config, parser, warnings, new SilentSyntaxCollection());
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    for (ParseWarning pw : warnings.getParseWarnings()) {
      if (pw.getComment() != null && pw.getComment().contains("unrecognized")) {
        throw new AssertionError(
            "Found unexpected unrecognized syntax warning for group mapping: " + pw.getComment());
      }
    }
  }
}
