package org.batfish.vendor.check_point_management.parsing;

import static org.batfish.common.BfConsts.RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.getNatRulebase;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.vendor.check_point_management.AllInstallationTargets;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.Domain;
import org.batfish.vendor.check_point_management.NatMethod;
import org.batfish.vendor.check_point_management.NatRule;
import org.batfish.vendor.check_point_management.NatRulebase;
import org.batfish.vendor.check_point_management.Original;
import org.batfish.vendor.check_point_management.Package;
import org.batfish.vendor.check_point_management.Uid;
import org.junit.Test;

/** Test of {@link CheckpointManagementParser}. */
public final class CheckpointManagementParserTest {

  private static final String DOMAIN_NAME = "domain1";
  private static final String PACKAGE_NAME = "package1";
  private static final String SERVER_NAME = "server1";
  private static final Uid UID_ANY = Uid.of("100");
  private static final Uid UID_ORIG = Uid.of("101");

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
  public void testGetNatRulebaseNatPolicyFalse() {
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
        getNatRulebase(
            pakij,
            DOMAIN_NAME,
            ImmutableMap.of(RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE, natRulebaseInput),
            pvcae,
            SERVER_NAME));
  }

  @Test
  public void testGetNatRulebaseEmpty() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Package pakij = testPackage(true);
    String natRulebaseInput = "[]"; // show-nat-rulebase

    // NAT rulebase should be null since there are no rulebase pages.
    assertNull(
        getNatRulebase(
            pakij,
            DOMAIN_NAME,
            ImmutableMap.of(RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE, natRulebaseInput),
            pvcae,
            SERVER_NAME));
  }

  @Test
  public void testGetNatRulebaseMissingFile() {
    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Package pakij = testPackage(true);

    // NAT rulebase should be null since the file is missing.
    assertNull(getNatRulebase(pakij, DOMAIN_NAME, ImmutableMap.of(), pvcae, SERVER_NAME));
  }

  @Test
  public void testGetNatRulebaseSinglePage() {
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
        getNatRulebase(
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
  public void testGetNatRulebaseTwoPages() {
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

    // NAT rulebase should be populated with merged rules and objects-dictionary from the two pages
    assertThat(
        getNatRulebase(
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
                        Uid.of("0")),
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
                        Uid.of("0"))),
                Uid.of("0"))));
  }

  @Test
  public void testGetNatRulebaseTwoUids() {
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
        getNatRulebase(
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
}
