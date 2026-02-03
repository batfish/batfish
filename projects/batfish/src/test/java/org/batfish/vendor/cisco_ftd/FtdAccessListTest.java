package org.batfish.vendor.cisco_ftd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessList;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessListAddressSpecifier;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessListAddressSpecifier.AddressType;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessListLine;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessListLine.AclType;
import org.batfish.vendor.cisco_ftd.representation.FtdConfiguration;
import org.junit.Test;

/** Tests for FTD ACL parsing. */
public class FtdAccessListTest extends FtdGrammarTest {

  @Test
  public void testAclExtendedPermit() {
    String config = "access-list ACL1 extended permit ip any any\n";

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getAccessLists(), hasKey("ACL1"));

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines(), hasSize(1));

    FtdAccessListLine line = acl.getLines().get(0);
    assertThat(line.getAclType(), equalTo(AclType.EXTENDED));
    assertThat(line.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line.getProtocol(), equalTo("ip"));
  }

  @Test
  public void testAclAdvancedTrustFlag() {
    String config = "access-list ACL1 advanced trust tcp ifc OUTSIDE any any eq 80 rule-id 100\n";
    FtdConfiguration vc = parseVendorConfig(config);
    FtdAccessListLine line = vc.getAccessLists().get("ACL1").getLines().get(0);
    assertThat(line.getAclType(), equalTo(AclType.ADVANCED));
    assertThat(line.isTrust(), equalTo(true));
    // Semantics remain PERMIT.
    assertThat(line.getAction(), equalTo(LineAction.PERMIT));
  }

  @Test
  public void testAclExtendedDeny() {
    String config = "access-list ACL1 extended deny tcp any any eq 80\n";

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    FtdAccessListLine line = acl.getLines().get(0);

    assertThat(line.getAclType(), equalTo(AclType.EXTENDED));
    assertThat(line.getAction(), equalTo(LineAction.DENY));
    assertThat(line.getProtocol(), equalTo("tcp"));
  }

  @Test
  public void testAclRemark() {
    String config = "access-list ACL1 remark This is a remark line\n";

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    assertThat(acl.getLines(), hasSize(1));

    FtdAccessListLine line = acl.getLines().get(0);
    assertThat(line.getAclType(), equalTo(AclType.REMARK));
    assertThat(line.getRemark(), equalTo("This is a remark line"));
  }

  @Test
  public void testAclMultipleLines() {
    String config =
        join(
            "access-list ACL1 extended permit ip any any",
            "access-list ACL1 extended deny tcp any any eq 80",
            "access-list ACL1 extended permit udp any any eq 53",
            "access-list ACL1 deny ip any any");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    assertThat(acl.getLines(), hasSize(4));

    assertThat(acl.getLines().get(0).getAction(), equalTo(LineAction.PERMIT));
    assertThat(acl.getLines().get(1).getAction(), equalTo(LineAction.DENY));
    assertThat(acl.getLines().get(2).getAction(), equalTo(LineAction.PERMIT));
    assertThat(acl.getLines().get(3).getAction(), equalTo(LineAction.DENY));
  }

  @Test
  public void testAclHostSpecifier() {
    String config =
        join(
            "access-list ACL1 extended permit tcp host 10.1.1.1 any eq 80",
            "access-list ACL1 extended deny udp any host 10.2.1.1 eq 53");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    assertThat(acl.getLines(), hasSize(2));

    FtdAccessListAddressSpecifier src = acl.getLines().get(0).getSourceAddressSpecifier();
    assertThat(src.getType(), equalTo(AddressType.HOST));
    assertThat(src.getIp(), equalTo(Ip.parse("10.1.1.1")));

    FtdAccessListAddressSpecifier dst = acl.getLines().get(1).getDestinationAddressSpecifier();
    assertThat(dst.getType(), equalTo(AddressType.HOST));
    assertThat(dst.getIp(), equalTo(Ip.parse("10.2.1.1")));
  }

  @Test
  public void testAclAnySpecifier() {
    String config = "access-list ACL1 extended permit ip any any\n";

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    FtdAccessListLine line = acl.getLines().get(0);

    assertThat(line.getSourceAddressSpecifier().getType(), equalTo(AddressType.ANY));
    assertThat(line.getDestinationAddressSpecifier().getType(), equalTo(AddressType.ANY));
  }

  @Test
  public void testAclNetworkMaskSpecifier() {
    String config = "access-list ACL1 extended permit ip 192.168.1.0 255.255.255.0 any\n";

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    FtdAccessListAddressSpecifier src = acl.getLines().get(0).getSourceAddressSpecifier();

    assertThat(src.getType(), equalTo(AddressType.NETWORK_MASK));
    assertThat(src.getIp(), equalTo(Ip.parse("192.168.1.0")));
    assertThat(src.getMask(), equalTo(Ip.parse("255.255.255.0")));
  }

  @Test
  public void testAclObjectSpecifier() {
    String config =
        join(
            "object network WEB_SERVER",
            "  host 10.1.1.10",
            "access-list ACL1 extended permit tcp any object WEB_SERVER eq 80");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    FtdAccessListAddressSpecifier dst = acl.getLines().get(0).getDestinationAddressSpecifier();

    assertThat(dst.getType(), equalTo(AddressType.OBJECT));
    assertThat(dst.getObjectName(), equalTo("WEB_SERVER"));
  }

  @Test
  public void testAclObjectGroupSpecifier() {
    String config =
        join(
            "object-group network WEB_SERVERS",
            "  network-object host 10.1.1.1",
            "access-list ACL1 extended permit tcp any object-group WEB_SERVERS eq 80");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    FtdAccessListAddressSpecifier dst = acl.getLines().get(0).getDestinationAddressSpecifier();

    assertThat(dst.getType(), equalTo(AddressType.OBJECT_GROUP));
    assertThat(dst.getObjectName(), equalTo("WEB_SERVERS"));
  }

  @Test
  public void testAclAdvanced() {
    String config = "access-list ACL1 advanced permit tcp any any eq 80\n";

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    FtdAccessListLine line = acl.getLines().get(0);

    assertThat(line.getAclType(), equalTo(AclType.ADVANCED));
    assertThat(line.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line.getProtocol(), equalTo("tcp"));
  }

  @Test
  public void testAclWithRuleId() {
    String config = "access-list ACL1 extended permit ip any any rule-id 100\n";

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    FtdAccessListLine line = acl.getLines().get(0);

    assertThat(line.getRuleId(), equalTo(100L));
  }

  @Test
  public void testAclWithInactive() {
    String config = "access-list ACL1 extended deny ip any any inactive\n";

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    FtdAccessListLine line = acl.getLines().get(0);

    assertThat(line.isInactive(), equalTo(true));
  }

  @Test
  public void testAclWithLog() {
    String config = "access-list ACL1 extended deny ip any any log\n";

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    FtdAccessListLine line = acl.getLines().get(0);

    assertThat(line.isLog(), equalTo(true));
  }

  @Test
  public void testAclWithTimeRange() {
    String config = "access-list ACL1 extended permit ip any any time-range BUSINESS_HOURS\n";

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    FtdAccessListLine line = acl.getLines().get(0);

    assertThat(line.getTimeRange(), equalTo("BUSINESS_HOURS"));
  }

  @Test
  public void testAclWithMultipleOptions() {
    String config =
        "access-list ACL1 extended permit ip any any rule-id 100 log time-range WORK_HOURS\n";

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    FtdAccessListLine line = acl.getLines().get(0);

    assertThat(line.getRuleId(), equalTo(100L));
    assertThat(line.isLog(), equalTo(true));
    assertThat(line.getTimeRange(), equalTo("WORK_HOURS"));
  }

  @Test
  public void testMultipleAcls() {
    String config =
        join(
            "access-list OUTSIDE_IN extended permit tcp any any eq 80",
            "access-list OUTSIDE_IN extended permit tcp any any eq 443",
            "access-list INSIDE_OUT extended permit ip any any",
            "access-list DMZ_ACL extended permit udp any any eq 53");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getAccessLists().keySet(), hasSize(3));

    FtdAccessList outsideAcl = vc.getAccessLists().get("OUTSIDE_IN");
    assertThat(outsideAcl.getLines(), hasSize(2));

    FtdAccessList insideAcl = vc.getAccessLists().get("INSIDE_OUT");
    assertThat(insideAcl.getLines(), hasSize(1));

    FtdAccessList dmzAcl = vc.getAccessLists().get("DMZ_ACL");
    assertThat(dmzAcl.getLines(), hasSize(1));
  }

  @Test
  public void testAclWithDifferentProtocols() {
    String config =
        join(
            "access-list ACL1 extended permit tcp any any eq 80",
            "access-list ACL1 extended permit udp any any eq 53",
            "access-list ACL1 extended permit icmp any any",
            "access-list ACL1 extended permit ip any any");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    assertThat(acl.getLines(), hasSize(4));

    assertThat(acl.getLines().get(0).getProtocol(), equalTo("tcp"));
    assertThat(acl.getLines().get(1).getProtocol(), equalTo("udp"));
    assertThat(acl.getLines().get(2).getProtocol(), equalTo("icmp"));
    assertThat(acl.getLines().get(3).getProtocol(), equalTo("ip"));
  }

  @Test
  public void testAclSerialization() {
    String config =
        join(
            "access-list ACL1 extended permit ip any any rule-id 100",
            "access-list ACL1 remark Web traffic",
            "access-list ACL1 extended deny tcp any any eq 80");

    FtdConfiguration vc = parseVendorConfigWithSerialization(config);

    FtdAccessList acl = vc.getAccessLists().get("ACL1");
    assertThat(acl.getLines(), hasSize(3));

    assertThat(acl.getLines().get(0).getRuleId(), equalTo(100L));
    assertThat(acl.getLines().get(1).getRemark(), equalTo("Web traffic"));
    assertThat(acl.getLines().get(2).getAction(), equalTo(LineAction.DENY));
  }

  @Test
  public void testAclVendorConversion() {
    String config =
        join(
            "access-list OUTSIDE_IN extended permit ip any any",
            "access-list OUTSIDE_IN extended deny ip any any");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    assertThat(c, notNullValue());
    assertThat(c.getIpAccessLists(), hasKey("OUTSIDE_IN"));
  }

  @Test
  public void testComplexAcl() {
    String config =
        join(
            "object network WEB_SERVER",
            "  host 10.1.1.10",
            "object network DB_SERVER",
            "  host 10.1.2.10",
            "object-group network ALL_SERVERS",
            "  network-object object WEB_SERVER",
            "  network-object object DB_SERVER",
            "access-list SERVER_ACL extended permit tcp any object-group ALL_SERVERS eq 443",
            "access-list SERVER_ACL extended deny tcp any object-group ALL_SERVERS eq 22",
            "access-list SERVER_ACL remark SSH is not allowed to servers",
            "access-list SERVER_ACL extended permit ip any any rule-id 100");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("SERVER_ACL");
    assertThat(acl.getLines(), hasSize(4));
  }

  @Test
  public void testPrefilterPolicyAclLines() {
    String config =
        join(
            "access-list TEST_ACL advanced trust tcp ifc OUTSIDE any object-group OBJ_GRP1"
                + " object-group OBJ_GRP2 rule-id 100 event-log flow-end",
            "access-list TEST_ACL remark rule-id 100: PREFILTER POLICY: TEST_POLICY",
            "access-list TEST_ACL advanced trust tcp ifc OUTSIDE object-group OBJ_GRP3 object-group"
                + " OBJ_GRP4 eq domain rule-id 200 event-log flow-end");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("TEST_ACL");
    assertThat(acl.getLines(), hasSize(3));

    FtdAccessListLine line = acl.getLines().get(0);
    assertThat(line.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line.getProtocol(), equalTo("tcp"));
    assertThat(line.getDestinationAddressSpecifier(), notNullValue());
    assertThat(line.getDestinationAddressSpecifier().getType(), equalTo(AddressType.OBJECT_GROUP));
    assertThat(line.getRuleId(), equalTo(100L));

    FtdAccessListLine remarkLine = acl.getLines().get(1);
    assertThat(remarkLine.getAclType(), equalTo(AclType.REMARK));
    assertThat(remarkLine.getRemark(), equalTo("rule-id 100: PREFILTER POLICY: TEST_POLICY"));
  }

  @Test
  public void testPrefilterPolicyRemarksAndEventLog() {
    String config =
        join(
            "access-list TEST_ACL remark rule-id 300: PREFILTER POLICY: TEST_POLICY",
            "access-list TEST_ACL remark rule-id 300: RULE: TEST_RULE_#4",
            "access-list TEST_ACL advanced trust tcp ifc OUTSIDE any any eq domain rule-id 300"
                + " event-log flow-end",
            "access-list TEST_ACL advanced trust udp ifc OUTSIDE any any eq domain rule-id 300"
                + " event-log flow-start");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("TEST_ACL");
    assertThat(acl.getLines(), hasSize(4));

    FtdAccessListLine policyRemark = acl.getLines().get(0);
    assertThat(policyRemark.getAclType(), equalTo(AclType.REMARK));
    assertThat(policyRemark.getRemark(), equalTo("rule-id 300: PREFILTER POLICY: TEST_POLICY"));

    FtdAccessListLine ruleRemark = acl.getLines().get(1);
    assertThat(ruleRemark.getAclType(), equalTo(AclType.REMARK));
    assertThat(ruleRemark.getRemark(), equalTo("rule-id 300: RULE: TEST_RULE_#4"));

    assertThat(acl.getLines().get(2).getAction(), equalTo(LineAction.PERMIT));
    assertThat(acl.getLines().get(3).getAction(), equalTo(LineAction.PERMIT));
  }

  @Test
  public void testPrefilterObjectGroupServicePortsAndRanges() {
    String config =
        join(
            "access-list TEST_ACL remark rule-id 400: PREFILTER POLICY: TEST_POLICY",
            "access-list TEST_ACL remark rule-id 400: RULE: TEST_RULE_#8",
            "access-list TEST_ACL advanced trust tcp ifc OUTSIDE object-group OBJ_GRP1 object-group"
                + " TCP_22 rule-id 400 event-log flow-end",
            "access-list TEST_ACL advanced trust udp ifc OUTSIDE object-group OBJ_GRP1 object-group"
                + " UDP_161 rule-id 400 event-log flow-end",
            "access-list TEST_ACL advanced trust tcp ifc OUTSIDE host 192.0.2.1 host 192.0.2.2"
                + " range 5000 5001 rule-id 500",
            "access-list TEST_ACL advanced trust tcp ifc OUTSIDE host 192.0.2.1 host 192.0.2.2"
                + " range 7011 7025 rule-id 500");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("TEST_ACL");
    assertThat(acl.getLines(), hasSize(6));

    FtdAccessListLine policyRemark = acl.getLines().get(0);
    assertThat(policyRemark.getAclType(), equalTo(AclType.REMARK));
    assertThat(policyRemark.getRemark(), equalTo("rule-id 400: PREFILTER POLICY: TEST_POLICY"));

    FtdAccessListLine firstRule = acl.getLines().get(2);
    assertThat(firstRule.getAction(), equalTo(LineAction.PERMIT));
    assertThat(
        firstRule.getDestinationAddressSpecifier().getType(), equalTo(AddressType.OBJECT_GROUP));

    FtdAccessListLine rangeRule = acl.getLines().get(4);
    assertThat(rangeRule.getAction(), equalTo(LineAction.PERMIT));
    assertThat(rangeRule.getDestinationAddressSpecifier().getType(), equalTo(AddressType.HOST));
  }

  @Test
  public void testPrefilterMixedServiceSpecsSharedRuleId() {
    String config =
        join(
            "access-list TEST_ACL remark rule-id 600: PREFILTER POLICY: TEST_POLICY",
            "access-list TEST_ACL remark rule-id 600: RULE: TEST_RULE_#26",
            "access-list TEST_ACL advanced trust tcp ifc OUTSIDE object-group OBJ_GRP1 object OBJ1"
                + " object-group TCP-6974 rule-id 600 event-log flow-end",
            "access-list TEST_ACL advanced trust tcp ifc OUTSIDE object-group OBJ_GRP1 object OBJ1"
                + " eq https rule-id 600 event-log flow-end",
            "access-list TEST_ACL advanced trust tcp ifc OUTSIDE object-group OBJ_GRP1 object OBJ1"
                + " range 6977 6979 rule-id 600 event-log flow-end");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("TEST_ACL");
    assertThat(acl.getLines(), hasSize(5));

    FtdAccessListLine policyRemark = acl.getLines().get(0);
    assertThat(policyRemark.getAclType(), equalTo(AclType.REMARK));
    assertThat(policyRemark.getRemark(), equalTo("rule-id 600: PREFILTER POLICY: TEST_POLICY"));

    FtdAccessListLine objectGroupRule = acl.getLines().get(2);
    assertThat(objectGroupRule.getAction(), equalTo(LineAction.PERMIT));
    assertThat(
        objectGroupRule.getDestinationAddressSpecifier().getType(), equalTo(AddressType.OBJECT));
    assertThat(objectGroupRule.getRuleId(), equalTo(600L));

    FtdAccessListLine httpsRule = acl.getLines().get(3);
    assertThat(httpsRule.getAction(), equalTo(LineAction.PERMIT));
    assertThat(httpsRule.getRuleId(), equalTo(600L));

    FtdAccessListLine rangeRule = acl.getLines().get(4);
    assertThat(rangeRule.getAction(), equalTo(LineAction.PERMIT));
    assertThat(rangeRule.getRuleId(), equalTo(600L));
  }

  @Test
  public void testPrefilterAny4Any6AndEventLogVariants() {
    String config =
        join(
            "access-list TEST_ACL remark rule-id 700: PREFILTER POLICY: TEST_POLICY",
            "access-list TEST_ACL remark rule-id 700: RULE: TEST_RULE_#40",
            "access-list TEST_ACL advanced trust icmp ifc OUTSIDE any4 any4 rule-id 700 event-log"
                + " flow-nsel",
            "access-list TEST_ACL advanced trust icmp ifc OUTSIDE any6 any6 rule-id 700 event-log"
                + " flow-start");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("TEST_ACL");
    assertThat(acl.getLines(), hasSize(4));

    FtdAccessListLine any4Rule = acl.getLines().get(2);
    assertThat(any4Rule.getAction(), equalTo(LineAction.PERMIT));
    assertThat(any4Rule.getSourceAddressSpecifier().getType(), equalTo(AddressType.ANY4));
    assertThat(any4Rule.getDestinationAddressSpecifier().getType(), equalTo(AddressType.ANY4));

    FtdAccessListLine any6Rule = acl.getLines().get(3);
    assertThat(any6Rule.getAction(), equalTo(LineAction.PERMIT));
    assertThat(any6Rule.getSourceAddressSpecifier().getType(), equalTo(AddressType.ANY6));
    assertThat(any6Rule.getDestinationAddressSpecifier().getType(), equalTo(AddressType.ANY6));
  }

  @Test
  public void testPrefilterDenyWithAnyAndIpProtocol() {
    String config =
        join(
            "access-list TEST_ACL remark rule-id 800: PREFILTER POLICY: TEST_POLICY",
            "access-list TEST_ACL advanced deny ip ifc OUTSIDE any any rule-id 800 event-log"
                + " flow-end");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdAccessList acl = vc.getAccessLists().get("TEST_ACL");
    assertThat(acl.getLines(), hasSize(2));

    FtdAccessListLine denyRule = acl.getLines().get(1);
    assertThat(denyRule.getAction(), equalTo(LineAction.DENY));
    assertThat(denyRule.getProtocol(), equalTo("ip"));
    assertThat(denyRule.getRuleId(), equalTo(800L));
  }

  @Test
  public void testConvertedAclLineNamesIncludeRuleMetadata() throws IOException {
    String config =
        join(
            "NGFW Version 7.4.2.1",
            "hostname fw",
            "interface Port-channel1.320",
            " nameif OUTSIDE",
            "access-list CSM_FW_ACL_ remark rule-id 268455934: PREFILTER POLICY: Prefilter-FTD",
            "access-list CSM_FW_ACL_ remark rule-id 268455934: RULE: ACL-OUTSIDE-IN_#174",
            "access-list CSM_FW_ACL_ advanced trust tcp ifc OUTSIDE any any eq 80 rule-id"
                + " 268455934",
            "access-group CSM_FW_ACL_ global");

    SortedMap<String, byte[]> configurationBytes = new TreeMap<>();
    configurationBytes.put("fw.cfg", config.getBytes(StandardCharsets.UTF_8));
    var batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setConfigurationBytes(configurationBytes).build(), _folder);
    SortedMap<String, Configuration> configs = batfish.loadConfigurations(batfish.getSnapshot());
    Configuration c = configs.get("fw");
    assertThat(c.getIpAccessLists(), hasKey("CSM_FW_ACL_"));

    String lineName = c.getIpAccessLists().get("CSM_FW_ACL_").getLines().get(0).getName();
    assertThat(lineName, notNullValue());
    assertThat(lineName, containsString("rule-id 268455934"));
    assertThat(lineName, containsString("ACL-OUTSIDE-IN_#174"));
    assertThat(lineName, containsString("Prefilter-FTD"));
    assertThat(lineName, containsString("ifc OUTSIDE"));
  }
}
