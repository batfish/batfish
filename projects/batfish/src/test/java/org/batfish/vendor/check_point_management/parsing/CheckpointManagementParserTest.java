package org.batfish.vendor.check_point_management.parsing;

import static org.batfish.common.BfConsts.RELPATH_CHECKPOINT_MANAGEMENT_DIR;
import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.RELPATH_CHECKPOINT_SHOW_GATEWAYS_AND_SERVERS;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.RELPATH_CHECKPOINT_SHOW_GROUPS;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.RELPATH_CHECKPOINT_SHOW_SERVICES_ICMP;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.RELPATH_CHECKPOINT_SHOW_SERVICES_TCP;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.RELPATH_CHECKPOINT_SHOW_SERVICES_UDP;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.RELPATH_CHECKPOINT_SHOW_SERVICE_GROUPS;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.buildObjectsList;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.mergeAccessLayers;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.mergeNatRuleOrSection;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.mergeNatRulebasePages;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.readGatewaysAndServers;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.readNatRulebase;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AccessRule;
import org.batfish.vendor.check_point_management.AccessSection;
import org.batfish.vendor.check_point_management.AllInstallationTargets;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.Domain;
import org.batfish.vendor.check_point_management.GatewayOrServerPolicy;
import org.batfish.vendor.check_point_management.GatewaysAndServers;
import org.batfish.vendor.check_point_management.Group;
import org.batfish.vendor.check_point_management.NatMethod;
import org.batfish.vendor.check_point_management.NatRule;
import org.batfish.vendor.check_point_management.NatRulebase;
import org.batfish.vendor.check_point_management.NatSection;
import org.batfish.vendor.check_point_management.Original;
import org.batfish.vendor.check_point_management.Package;
import org.batfish.vendor.check_point_management.ServiceGroup;
import org.batfish.vendor.check_point_management.ServiceIcmp;
import org.batfish.vendor.check_point_management.ServiceTcp;
import org.batfish.vendor.check_point_management.ServiceUdp;
import org.batfish.vendor.check_point_management.SimpleGateway;
import org.batfish.vendor.check_point_management.Uid;
import org.junit.Test;

/** Test of {@link CheckpointManagementParser}. */
public final class CheckpointManagementParserTest {

  private static final String DOMAIN_NAME = "domain1";
  private static final String PACKAGE_NAME = "package1";
  private static final String SERVER_NAME = "server1";
  private static final Uid UID_ANY = Uid.of("100");
  private static final Uid UID_ORIG = Uid.of("101");
  private static final Uid UID_GW1 = Uid.of("102");
  private static final Uid UID_GW2 = Uid.of("103");

  private static @Nonnull Package testPackage(boolean nat) {
    return new Package(
        new Domain(DOMAIN_NAME, Uid.of("domain1Uid")),
        AllInstallationTargets.instance(),
        PACKAGE_NAME,
        false,
        nat,
        Uid.of("package1Uid"));
  }

  @Test
  public void testReadNatRulebaseNatPolicyFalse() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Package pakij = testPackage(false);
    String natRulebaseInput =
        "[" // show-nat-rulebase
            + "{" // NatRulebase page
            + "\"uid\":\"0\","
            + "\"objects-dictionary\":[],"
            + "\"rulebase\":["
            + "{" // nat-rule
            + "\"type\":\"nat-rule\","
            + "\"auto-generated\":true,"
            + "\"uid\":\"0\","
            + "\"comments\":\"foo\","
            + "\"enabled\":true,"
            + "\"install-on\":[\"100\"],"
            + "\"method\":\"hide\","
            + "\"original-destination\":\"1\","
            + "\"original-service\":\"2\","
            + "\"original-source\":\"3\","
            + "\"rule-number\":1,"
            + "\"translated-destination\":\"4\","
            + "\"translated-service\":\"5\","
            + "\"translated-source\":\"6\""
            + "}" // nat-rule
            + "]," // rulebase
            + "\"from\": 1,"
            + "\"to\": 1"
            + "}" // NatRulebase page
            + "]"; // show-nat-rulebase

    // NAT rulebase should be null despite definition because package nat-policy is false.
    assertNull(
        readNatRulebase(
            pakij,
            DOMAIN_NAME,
            ImmutableMap.of(RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE, natRulebaseInput),
            pvcae,
            SERVER_NAME));
  }

  @Test
  public void testReadNatRulebaseEmpty() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Package pakij = testPackage(true);
    String natRulebaseInput = "[]"; // show-nat-rulebase

    // NAT rulebase should be null since there are no rulebase pages.
    assertNull(
        readNatRulebase(
            pakij,
            DOMAIN_NAME,
            ImmutableMap.of(RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE, natRulebaseInput),
            pvcae,
            SERVER_NAME));
  }

  @Test
  public void testReadNatRulebaseMissingFile() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Package pakij = testPackage(true);

    // NAT rulebase should be null since the file is missing.
    assertNull(readNatRulebase(pakij, DOMAIN_NAME, ImmutableMap.of(), pvcae, SERVER_NAME));
  }

  @Test
  public void testReadNatRulebaseSinglePage() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Package pakij = testPackage(true);
    String natRulebaseInput =
        "[" // show-nat-rulebase
            + "{" // NatRulebase page
            + "\"uid\":\"0\","
            + "\"objects-dictionary\":[],"
            + "\"rulebase\":["
            + "{" // nat-rule
            + "\"type\":\"nat-rule\","
            + "\"auto-generated\":true,"
            + "\"uid\":\"0\","
            + "\"comments\":\"foo\","
            + "\"enabled\":true,"
            + "\"install-on\":[\"100\"],"
            + "\"method\":\"hide\","
            + "\"original-destination\":\"1\","
            + "\"original-service\":\"2\","
            + "\"original-source\":\"3\","
            + "\"rule-number\":1,"
            + "\"translated-destination\":\"4\","
            + "\"translated-service\":\"5\","
            + "\"translated-source\":\"6\""
            + "}" // nat-rule
            + "]," // rulebase
            + "\"from\": 1,"
            + "\"to\": 1"
            + "}" // NatRulebase page
            + "]"; // show-nat-rulebase

    // NAT rulebase should be populated.
    assertThat(
        readNatRulebase(
            pakij,
            DOMAIN_NAME,
            ImmutableMap.of(RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE, natRulebaseInput),
            pvcae,
            SERVER_NAME),
        equalTo(
            new NatRulebase(
                ImmutableMap.of(),
                ImmutableList.of(
                    new NatRule(
                        true,
                        "foo",
                        true,
                        ImmutableList.of(Uid.of("100")),
                        NatMethod.HIDE,
                        Uid.of("1"),
                        Uid.of("2"),
                        Uid.of("3"),
                        1,
                        Uid.of("4"),
                        Uid.of("5"),
                        Uid.of("6"),
                        Uid.of("0"))),
                Uid.of("0"))));
  }

  @Test
  public void testReadNatRulebaseTwoPages() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Package pakij = testPackage(true);
    String natRulebaseInput =
        "[" // show-nat-rulebase
            + "{" // NatRulebase page1
            + "\"uid\":\"0\","
            + "\"objects-dictionary\":["
            + "{" // CpmiAnyObject
            + "\"uid\": \"100\","
            + "\"type\": \"CpmiAnyObject\","
            + "\"name\": \"Any\""
            + "}" // CpmiAnyObject
            + "],"
            + "\"rulebase\":["
            + "{" // nat-rule
            + "\"type\":\"nat-rule\","
            + "\"auto-generated\":true,"
            + "\"uid\":\"10\","
            + "\"comments\":\"foo\","
            + "\"enabled\":true,"
            + "\"install-on\":[\"100\"],"
            + "\"method\":\"hide\","
            + "\"original-destination\":\"1\","
            + "\"original-service\":\"2\","
            + "\"original-source\":\"3\","
            + "\"rule-number\":1,"
            + "\"translated-destination\":\"4\","
            + "\"translated-service\":\"5\","
            + "\"translated-source\":\"6\""
            + "}" // nat-rule
            + "]," // rulebase
            + "\"from\": 1,"
            + "\"to\": 1"
            + "}," // NatRulebase page1
            + "{" // NatRulebase page2
            + "\"uid\":\"0\","
            + "\"objects-dictionary\":["
            + "{" // CpmiAnyObject
            + "\"uid\": \"101\","
            + "\"type\": \"Global\","
            + "\"name\": \"Original\""
            + "}" // CpmiAnyObject
            + "],"
            + "\"rulebase\":["
            + "{" // nat-rule
            + "\"type\":\"nat-rule\","
            + "\"auto-generated\":true,"
            + "\"uid\":\"11\","
            + "\"comments\":\"foo\","
            + "\"enabled\":true,"
            + "\"install-on\":[\"100\"],"
            + "\"method\":\"hide\","
            + "\"original-destination\":\"1\","
            + "\"original-service\":\"2\","
            + "\"original-source\":\"3\","
            + "\"rule-number\":2,"
            + "\"translated-destination\":\"4\","
            + "\"translated-service\":\"5\","
            + "\"translated-source\":\"6\""
            + "}" // nat-rule
            + "]," // rulebase
            + "\"from\": 2,"
            + "\"to\": 2"
            + "}" // NatRulebase page2
            + "]"; // show-nat-rulebase

    // NAT rulebase should be populated with merged rules and objects-dictionary from the two pages
    assertThat(
        readNatRulebase(
            pakij,
            DOMAIN_NAME,
            ImmutableMap.of(RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE, natRulebaseInput),
            pvcae,
            SERVER_NAME),
        equalTo(
            new NatRulebase(
                ImmutableMap.of(
                    UID_ANY, new CpmiAnyObject(UID_ANY),
                    UID_ORIG, new Original(UID_ORIG)),
                ImmutableList.of(
                    new NatRule(
                        true,
                        "foo",
                        true,
                        ImmutableList.of(Uid.of("100")),
                        NatMethod.HIDE,
                        Uid.of("1"),
                        Uid.of("2"),
                        Uid.of("3"),
                        1,
                        Uid.of("4"),
                        Uid.of("5"),
                        Uid.of("6"),
                        Uid.of("10")),
                    new NatRule(
                        true,
                        "foo",
                        true,
                        ImmutableList.of(Uid.of("100")),
                        NatMethod.HIDE,
                        Uid.of("1"),
                        Uid.of("2"),
                        Uid.of("3"),
                        2,
                        Uid.of("4"),
                        Uid.of("5"),
                        Uid.of("6"),
                        Uid.of("11"))),
                Uid.of("0"))));
  }

  @Test
  public void testMergeNatRulebasePagesChildSplitAcrossTwoPages() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    CpmiAnyObject anyObject = new CpmiAnyObject(UID_ANY);
    // rulebase contains section1 with two rules (rule1, rule2) & a rule at the top level (rule3)
    // The full rulebase is split across two pages for this test:
    //   1. rulebaseA contains part of section1 with only rule1
    //   2. rulebaseB contains the other part of section1 with rule2, and also the top-level rule3
    NatRule rule1 =
        new NatRule(
            true,
            "foo",
            true,
            ImmutableList.of(UID_ANY),
            NatMethod.HIDE,
            UID_ANY,
            UID_ANY,
            UID_ANY,
            1,
            UID_ANY,
            UID_ANY,
            UID_ANY,
            Uid.of("11"));
    NatSection section1a = new NatSection("section1", ImmutableList.of(rule1), Uid.of("1"));
    NatRulebase rulebaseA =
        new NatRulebase(
            ImmutableMap.of(UID_ANY, anyObject), ImmutableList.of(section1a), Uid.of("1000"));

    NatRule rule2 =
        new NatRule(
            true,
            "foo",
            true,
            ImmutableList.of(UID_ANY),
            NatMethod.HIDE,
            UID_ANY,
            UID_ANY,
            UID_ANY,
            2,
            UID_ANY,
            UID_ANY,
            UID_ANY,
            Uid.of("12"));
    NatRule rule3 =
        new NatRule(
            true,
            "foo",
            true,
            ImmutableList.of(UID_ANY),
            NatMethod.HIDE,
            UID_ANY,
            UID_ANY,
            UID_ANY,
            3,
            UID_ANY,
            UID_ANY,
            UID_ANY,
            Uid.of("13"));
    NatSection section1b = new NatSection("section1", ImmutableList.of(rule2), Uid.of("1"));
    NatRulebase rulebaseB =
        new NatRulebase(
            ImmutableMap.of(UID_ANY, anyObject),
            ImmutableList.of(section1b, rule3),
            Uid.of("1000"));

    NatSection section1 = new NatSection("section1", ImmutableList.of(rule1, rule2), Uid.of("1"));
    NatRulebase rulebaseCombined =
        new NatRulebase(
            ImmutableMap.of(UID_ANY, anyObject), ImmutableList.of(section1, rule3), Uid.of("1000"));

    assertThat(
        mergeNatRulebasePages(ImmutableList.of(rulebaseA, rulebaseB), pvcae),
        equalTo(rulebaseCombined));
  }

  @Test
  public void testReadNatRulebaseTwoUids() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Package pakij = testPackage(true);
    String natRulebaseInput =
        "[" // show-nat-rulebase
            + "{" // NatRulebase page1
            + "\"uid\":\"0\","
            + "\"objects-dictionary\":["
            + "{" // CpmiAnyObject
            + "\"uid\": \"100\","
            + "\"type\": \"CpmiAnyObject\","
            + "\"name\": \"Any\""
            + "}" // CpmiAnyObject
            + "],"
            + "\"rulebase\":["
            + "{" // nat-rule
            + "\"type\":\"nat-rule\","
            + "\"auto-generated\":true,"
            + "\"uid\":\"0\","
            + "\"comments\":\"foo\","
            + "\"enabled\":true,"
            + "\"install-on\":[\"100\"],"
            + "\"method\":\"hide\","
            + "\"original-destination\":\"1\","
            + "\"original-service\":\"2\","
            + "\"original-source\":\"3\","
            + "\"rule-number\":1,"
            + "\"translated-destination\":\"4\","
            + "\"translated-service\":\"5\","
            + "\"translated-source\":\"6\""
            + "}" // nat-rule
            + "]," // rulebase
            + "\"from\": 1,"
            + "\"to\": 1"
            + "}," // NatRulebase page1
            + "{" // NatRulebase page2
            + "\"uid\":\"1\","
            + "\"objects-dictionary\":["
            + "{" // CpmiAnyObject
            + "\"uid\": \"101\","
            + "\"type\": \"Global\","
            + "\"name\": \"Original\""
            + "}" // CpmiAnyObject
            + "],"
            + "\"rulebase\":["
            + "{" // nat-rule
            + "\"type\":\"nat-rule\","
            + "\"auto-generated\":true,"
            + "\"uid\":\"0\","
            + "\"comments\":\"foo\","
            + "\"enabled\":true,"
            + "\"install-on\":[\"100\"],"
            + "\"method\":\"hide\","
            + "\"original-destination\":\"1\","
            + "\"original-service\":\"2\","
            + "\"original-source\":\"3\","
            + "\"rule-number\":2,"
            + "\"translated-destination\":\"4\","
            + "\"translated-service\":\"5\","
            + "\"translated-source\":\"6\""
            + "}" // nat-rule
            + "]," // rulebase
            + "\"from\": 2,"
            + "\"to\": 2"
            + "}" // NatRulebase page2
            + "]"; // show-nat-rulebase

    // NAT rulebase should be populated only with first page, since only first UID is kept
    assertThat(
        readNatRulebase(
            pakij,
            DOMAIN_NAME,
            ImmutableMap.of(RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE, natRulebaseInput),
            pvcae,
            SERVER_NAME),
        equalTo(
            new NatRulebase(
                ImmutableMap.of(UID_ANY, new CpmiAnyObject(UID_ANY)),
                ImmutableList.of(
                    new NatRule(
                        true,
                        "foo",
                        true,
                        ImmutableList.of(Uid.of("100")),
                        NatMethod.HIDE,
                        Uid.of("1"),
                        Uid.of("2"),
                        Uid.of("3"),
                        1,
                        Uid.of("4"),
                        Uid.of("5"),
                        Uid.of("6"),
                        Uid.of("0"))),
                Uid.of("0"))));
  }

  @Test
  public void testMergeNatRuleOrSectionWarning() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    NatRule rule1 =
        new NatRule(
            true,
            "foo",
            true,
            ImmutableList.of(Uid.of("100")),
            NatMethod.HIDE,
            Uid.of("1"),
            Uid.of("2"),
            Uid.of("3"),
            1,
            Uid.of("4"),
            Uid.of("5"),
            Uid.of("6"),
            Uid.of("0"));
    NatSection section1 = new NatSection("bar", ImmutableList.of(), Uid.of("0"));

    assertThat(mergeNatRuleOrSection(ImmutableList.of(rule1, section1), pvcae), equalTo(rule1));
    assertThat(
        pvcae.getWarnings().get(RELPATH_CHECKPOINT_MANAGEMENT_DIR).getRedFlagWarnings(),
        contains(
            hasText("Cannot merge NatRule pages (for uid 0), ignoring instances after the first")));
  }

  @Test
  public void testMergeAccessLayers() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Uid uidAny = Uid.of("0");
    Uid uid1 = Uid.of("1");
    Uid uid2 = Uid.of("2");
    Uid uidTcp = Uid.of("3");
    Uid uidUdp = Uid.of("4");
    Uid uidRule1 = Uid.of("5");
    Uid uidRule2 = Uid.of("6");
    Uid uidRule3 = Uid.of("7");
    Uid uidBogus = Uid.of("999");
    String name1 = "1";
    String name2 = "2";
    CpmiAnyObject any = new CpmiAnyObject(uidAny);
    ServiceTcp tcp = new ServiceTcp("tcp", "1234", uidTcp);
    ServiceUdp udp = new ServiceUdp("udp", "1234", uidUdp);
    AccessRule rule1 =
        AccessRule.testBuilder(uidAny)
            .setName("rule1")
            .setUid(uidRule1)
            .setAction(uidBogus)
            .build();
    AccessRule rule2 =
        AccessRule.testBuilder(uidAny)
            .setName("rule2")
            .setUid(uidRule2)
            .setAction(uidBogus)
            .build();
    AccessRule rule3 =
        AccessRule.testBuilder(uidAny)
            .setName("rule3")
            .setUid(uidRule3)
            .setAction(uidBogus)
            .build();
    AccessLayer al1a =
        new AccessLayer(ImmutableMap.of(uidAny, any), ImmutableList.of(rule1, rule2), uid1, name1);
    AccessLayer al1b =
        new AccessLayer(ImmutableMap.of(uidAny, any), ImmutableList.of(rule3), uid1, name1);
    AccessLayer al1 =
        new AccessLayer(
            ImmutableMap.of(uidAny, any), ImmutableList.of(rule1, rule2, rule3), uid1, name1);

    AccessLayer al2a =
        new AccessLayer(
            ImmutableMap.of(uidAny, any, uidTcp, tcp), ImmutableList.of(rule1), uid2, name2);
    AccessLayer al2b =
        new AccessLayer(
            ImmutableMap.of(uidAny, any, uidUdp, udp), ImmutableList.of(rule2), uid2, name2);
    AccessLayer al2 =
        new AccessLayer(
            ImmutableMap.of(uidAny, any, uidTcp, tcp, uidUdp, udp),
            ImmutableList.of(rule1, rule2),
            uid2,
            name2);

    assertThat(
        mergeAccessLayers(ImmutableList.of(al1a, al1b, al2a, al2b), pvcae),
        equalTo(ImmutableList.of(al1, al2)));
  }

  /** Test merging AccessLayers when their children (AccessSections) are split across pages. */
  @Test
  public void testMergeAccessLayersAccessSectionSplit() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Uid uidAny = Uid.of("0");
    Uid uid2 = Uid.of("2");
    Uid uidTcp = Uid.of("3");
    Uid uidUdp = Uid.of("4");
    Uid uidRule1 = Uid.of("5");
    Uid uidRule2 = Uid.of("6");
    Uid uidRule3 = Uid.of("7");
    Uid uidSection1 = Uid.of("8");
    Uid uidBogus = Uid.of("999");
    String name2 = "2";
    CpmiAnyObject any = new CpmiAnyObject(uidAny);
    ServiceTcp tcp = new ServiceTcp("tcp", "1234", uidTcp);
    ServiceUdp udp = new ServiceUdp("udp", "1234", uidUdp);
    AccessRule rule1 =
        AccessRule.testBuilder(uidAny)
            .setName("rule1")
            .setUid(uidRule1)
            .setAction(uidBogus)
            .build();
    AccessRule rule2 =
        AccessRule.testBuilder(uidAny)
            .setName("rule2")
            .setUid(uidRule2)
            .setAction(uidBogus)
            .build();
    AccessRule rule3 =
        AccessRule.testBuilder(uidAny)
            .setName("rule3")
            .setUid(uidRule3)
            .setAction(uidBogus)
            .build();

    AccessLayer ala =
        new AccessLayer(
            ImmutableMap.of(uidAny, any, uidTcp, tcp),
            ImmutableList.of(new AccessSection("section1", ImmutableList.of(rule1), uidSection1)),
            uid2,
            name2);
    AccessLayer alb =
        new AccessLayer(
            ImmutableMap.of(uidAny, any, uidUdp, udp),
            ImmutableList.of(
                new AccessSection("section1", ImmutableList.of(rule2), uidSection1), rule3),
            uid2,
            name2);
    AccessLayer al =
        new AccessLayer(
            ImmutableMap.of(uidAny, any, uidTcp, tcp, uidUdp, udp),
            ImmutableList.of(
                new AccessSection("section1", ImmutableList.of(rule1, rule2), uidSection1), rule3),
            uid2,
            name2);

    assertThat(mergeAccessLayers(ImmutableList.of(ala, alb), pvcae), equalTo(ImmutableList.of(al)));
  }

  @Test
  public void testMergeAccessLayersWarning() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Uid uidAny = Uid.of("0");
    Uid uid1 = Uid.of("1");
    Uid uidRule1 = Uid.of("5");
    Uid uidBogus = Uid.of("999");
    String name1 = "1";
    CpmiAnyObject any = new CpmiAnyObject(uidAny);
    AccessRule rule1 =
        AccessRule.testBuilder(uidAny)
            .setName("rule1")
            .setUid(uidRule1)
            .setAction(uidBogus)
            .build();
    AccessLayer al1a =
        new AccessLayer(ImmutableMap.of(uidAny, any), ImmutableList.of(rule1), uid1, name1);
    AccessLayer al1b =
        new AccessLayer(ImmutableMap.of(uidAny, any), ImmutableList.of(rule1), uid1, name1);
    assertThat(
        mergeAccessLayers(ImmutableList.of(al1a, al1b), pvcae), equalTo(ImmutableList.of(al1a)));
    assertThat(
        pvcae.getWarnings().get(RELPATH_CHECKPOINT_MANAGEMENT_DIR).getRedFlagWarnings(),
        contains(
            hasText(
                "Cannot merge AccessRule pages (for uid 5), ignoring instances after the first")));
  }

  /** Convert JSON object text into ObjectPage JSON */
  private String wrapJsonObj(String obj) {
    return String.format("[{\"objects\":[%s]}]", obj);
  }

  @Test
  public void testBuildObjectsList() {
    String serviceGroupJson =
        "{\"type\": \"service-group\", \"name\": \"serviceGroup\", \"uid\": \"1\", \"members\":"
            + " [\"2\"]}";
    String icmpJson =
        "{\"type\": \"service-icmp\", \"name\": \"icmp\", \"uid\": \"2\", \"icmp-type\": 1,"
            + " \"icmp-code\": 2}";
    String tcpJson =
        "{\"type\": \"service-tcp\", \"name\": \"tcp\", \"uid\": \"3\", \"port\": \"22\"}";
    String udpJson =
        "{\"type\": \"service-udp\", \"name\": \"udp\", \"uid\": \"4\", \"port\": \"222\"}";
    String groupJson =
        "{\"type\": \"group\", \"name\": \"group\", \"uid\": \"5\", \"members\": [\"6\"]}";

    Map<String, String> fileMap =
        ImmutableMap.<String, String>builder()
            .put(RELPATH_CHECKPOINT_SHOW_GROUPS, wrapJsonObj(groupJson))
            .put(RELPATH_CHECKPOINT_SHOW_SERVICE_GROUPS, wrapJsonObj(serviceGroupJson))
            .put(RELPATH_CHECKPOINT_SHOW_SERVICES_ICMP, wrapJsonObj(icmpJson))
            .put(RELPATH_CHECKPOINT_SHOW_SERVICES_TCP, wrapJsonObj(tcpJson))
            .put(RELPATH_CHECKPOINT_SHOW_SERVICES_UDP, wrapJsonObj(udpJson))
            .build();
    Map<String, Map<String, Map<String, String>>> domainFileMap =
        ImmutableMap.of("server", ImmutableMap.of("domain", fileMap));

    assertThat(
        buildObjectsList(
            domainFileMap, "domain", "server", new ParseVendorConfigurationAnswerElement()),
        containsInAnyOrder(
            new Group("group", ImmutableList.of(Uid.of("6")), Uid.of("5")),
            new ServiceGroup("serviceGroup", ImmutableList.of(Uid.of("2")), Uid.of("1")),
            new ServiceIcmp("icmp", 1, 2, Uid.of("2")),
            new ServiceTcp("tcp", "22", Uid.of("3")),
            new ServiceUdp("udp", "222", Uid.of("4"))));
  }

  @Test
  public void testReadGatewaysAndServersEmpty() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    String sgasInput = "[]"; // show-gateways-and-servers

    // GatewaysAndServers should be null since there are no pages.
    assertNull(
        readGatewaysAndServers(
            SERVER_NAME,
            DOMAIN_NAME,
            ImmutableMap.of(
                SERVER_NAME,
                ImmutableMap.of(
                    DOMAIN_NAME,
                    ImmutableMap.of(RELPATH_CHECKPOINT_SHOW_GATEWAYS_AND_SERVERS, sgasInput))),
            pvcae));
  }

  @Test
  public void testReadGatewaysAndServersMissingFile() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();

    // GatewaysAndServers should be null since the file is missing.
    assertNull(
        readGatewaysAndServers(
            SERVER_NAME,
            DOMAIN_NAME,
            ImmutableMap.of(SERVER_NAME, ImmutableMap.of(DOMAIN_NAME, ImmutableMap.of())),
            pvcae));
  }

  @Test
  public void testReadGatewaysAndServersSinglePage() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    String sgasInput =
        "[" // show-gateways-and-servers
            + "{" // show-gateways-and-servers page
            + "\"objects\":["
            + "{" // simple-gateway
            + "\"type\":\"simple-gateway\","
            + "\"uid\":\"102\","
            + "\"name\":\"gw1\","
            + "\"interfaces\": [],"
            + "\"ipv4-address\":\"1.1.1.1\","
            + "\"policy\":{"
            + "\"access-policy-installed\": false,"
            + "\"access-policy-name\": null,"
            + "\"threat-policy-installed\": false,"
            + "\"threat-policy-name\": null"
            + "}" // policy
            + "}" // simple-gateawy
            + "]," // objects
            + "\"from\": 1,"
            + "\"to\": 1"
            + "}" // show-gateways-and-servers page
            + "]"; // show-gateways-and-servers

    // GatewaysAndServers should be populated.
    assertThat(
        readGatewaysAndServers(
            SERVER_NAME,
            DOMAIN_NAME,
            ImmutableMap.of(
                SERVER_NAME,
                ImmutableMap.of(
                    DOMAIN_NAME,
                    ImmutableMap.of(RELPATH_CHECKPOINT_SHOW_GATEWAYS_AND_SERVERS, sgasInput))),
            pvcae),
        equalTo(
            new GatewaysAndServers(
                ImmutableMap.of(
                    UID_GW1,
                    new SimpleGateway(
                        Ip.parse("1.1.1.1"),
                        "gw1",
                        ImmutableList.of(),
                        new GatewayOrServerPolicy(null, null),
                        UID_GW1)))));
  }

  @Test
  public void testReadGatewaysAndServersTwoPages() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    String sgasInput =
        "[" // show-gateways-and-servers
            + "{" // show-gateways-and-servers page1
            + "\"objects\":["
            + "{" // simple-gateway
            + "\"type\":\"simple-gateway\","
            + "\"uid\":\"102\","
            + "\"name\":\"gw1\","
            + "\"interfaces\": [],"
            + "\"ipv4-address\":\"1.1.1.1\","
            + "\"policy\":{"
            + "\"access-policy-installed\": false,"
            + "\"access-policy-name\": null,"
            + "\"threat-policy-installed\": false,"
            + "\"threat-policy-name\": null"
            + "}" // policy
            + "}" // simple-gateawy
            + "]," // objects
            + "\"from\": 1,"
            + "\"to\": 1"
            + "}," // show-gateways-and-servers page1
            + "{" // show-gateways-and-servers page2
            + "\"objects\":["
            + "{" // simple-gateway
            + "\"type\":\"simple-gateway\","
            + "\"uid\":\"103\","
            + "\"name\":\"gw2\","
            + "\"interfaces\": [],"
            + "\"ipv4-address\":\"2.2.2.2\","
            + "\"policy\":{"
            + "\"access-policy-installed\": false,"
            + "\"access-policy-name\": null,"
            + "\"threat-policy-installed\": false,"
            + "\"threat-policy-name\": null"
            + "}" // policy
            + "}" // simple-gateawy
            + "]," // objects
            + "\"from\": 1,"
            + "\"to\": 1"
            + "}" // show-gateways-and-servers page2
            + "]"; // show-gateways-and-servers

    // GatewaysAndServers should be populated with merged objects from the two pages
    assertThat(
        readGatewaysAndServers(
            SERVER_NAME,
            DOMAIN_NAME,
            ImmutableMap.of(
                SERVER_NAME,
                ImmutableMap.of(
                    DOMAIN_NAME,
                    ImmutableMap.of(RELPATH_CHECKPOINT_SHOW_GATEWAYS_AND_SERVERS, sgasInput))),
            pvcae),
        equalTo(
            new GatewaysAndServers(
                ImmutableMap.of(
                    UID_GW1,
                    new SimpleGateway(
                        Ip.parse("1.1.1.1"),
                        "gw1",
                        ImmutableList.of(),
                        new GatewayOrServerPolicy(null, null),
                        UID_GW1),
                    UID_GW2,
                    new SimpleGateway(
                        Ip.parse("2.2.2.2"),
                        "gw2",
                        ImmutableList.of(),
                        new GatewayOrServerPolicy(null, null),
                        UID_GW2)))));
  }
}
