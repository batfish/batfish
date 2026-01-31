package org.batfish.grammar.cisco_ftd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import org.batfish.datamodel.Configuration;
import org.batfish.representation.cisco_ftd.FtdConfiguration;
import org.batfish.representation.cisco_ftd.FtdNetworkObjectGroup;
import org.batfish.representation.cisco_ftd.FtdNetworkObjectGroupMember;
import org.junit.Test;

/** Tests for FTD network object group parsing. */
public class FtdNetworkObjectGroupTest extends FtdGrammarTest {

  @Test
  public void testObjectGroupHost() {
    String config = join("object-group network WEB_SERVERS", "  network-object host 10.1.1.1");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getNetworkObjectGroups().keySet(), hasSize(1));

    FtdNetworkObjectGroup group = vc.getNetworkObjectGroups().get("WEB_SERVERS");
    assertThat(group, notNullValue());
    assertThat(group.getName(), equalTo("WEB_SERVERS"));
    assertThat(group.getMembers(), hasSize(1));

    FtdNetworkObjectGroupMember member = group.getMembers().get(0);
    assertThat(member, notNullValue());
  }

  @Test
  public void testObjectGroupMultipleHosts() {
    String config =
        join(
            "object-group network WEB_SERVERS",
            "  network-object host 10.1.1.1",
            "  network-object host 10.1.1.2",
            "  network-object host 10.1.1.3");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdNetworkObjectGroup group = vc.getNetworkObjectGroups().get("WEB_SERVERS");
    assertThat(group.getMembers(), hasSize(3));
  }

  @Test
  public void testObjectGroupObjectReference() {
    String config =
        join(
            "object network WEB1",
            "  host 10.1.1.1",
            "object network WEB2",
            "  host 10.1.1.2",
            "object-group network WEB_SERVERS",
            "  network-object object WEB1",
            "  network-object object WEB2");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdNetworkObjectGroup group = vc.getNetworkObjectGroups().get("WEB_SERVERS");
    assertThat(group.getMembers(), hasSize(2));

    FtdNetworkObjectGroupMember member1 = group.getMembers().get(0);
    assertThat(member1, notNullValue());

    FtdNetworkObjectGroupMember member2 = group.getMembers().get(1);
    assertThat(member2, notNullValue());
  }

  @Test
  public void testObjectGroupNetworkMask() {
    String config =
        join(
            "object-group network INTERNAL_SUBNETS",
            "  network-object 192.168.1.0 255.255.255.0",
            "  network-object 192.168.2.0 255.255.255.0",
            "  network-object 10.1.0.0 255.255.0.0");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdNetworkObjectGroup group = vc.getNetworkObjectGroups().get("INTERNAL_SUBNETS");
    assertThat(group.getMembers(), hasSize(3));
  }

  @Test
  public void testObjectGroupNested() {
    String config =
        join(
            "object-group network WEB_SERVERS",
            "  network-object host 10.1.1.1",
            "object-group network DMZ_SERVERS",
            "  network-object host 10.2.1.1",
            "object-group network ALL_SERVERS",
            "  group-object WEB_SERVERS",
            "  group-object DMZ_SERVERS");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getNetworkObjectGroups().keySet(), hasSize(3));

    FtdNetworkObjectGroup allServers = vc.getNetworkObjectGroups().get("ALL_SERVERS");
    assertThat(allServers.getMembers(), hasSize(2));
  }

  @Test
  public void testObjectGroupMixedMembers() {
    String config =
        join(
            "object network WEB1",
            "  host 10.1.1.1",
            "object-group network ALL_RESOURCES",
            "  network-object host 10.2.1.1",
            "  network-object object WEB1",
            "  network-object 192.168.1.0 255.255.255.0",
            "  group-object OTHER_GROUP");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdNetworkObjectGroup group = vc.getNetworkObjectGroups().get("ALL_RESOURCES");
    assertThat(group.getMembers(), hasSize(4));
  }

  @Test
  public void testMultipleObjectGroups() {
    String config =
        join(
            "object-group network WEB_SERVERS",
            "  network-object host 10.1.1.1",
            "object-group network DB_SERVERS",
            "  network-object host 10.1.2.1",
            "object-group network INTERNAL_NETWORKS",
            "  network-object 192.168.0.0 255.255.0.0");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getNetworkObjectGroups().keySet(), hasSize(3));
    assertThat(vc.getNetworkObjectGroups(), hasKey("WEB_SERVERS"));
    assertThat(vc.getNetworkObjectGroups(), hasKey("DB_SERVERS"));
    assertThat(vc.getNetworkObjectGroups(), hasKey("INTERNAL_NETWORKS"));
  }

  @Test
  public void testObjectGroupWithHyphens() {
    String config =
        join(
            "object-group network WEB-SERVERS",
            "  network-object host 10.1.1.1",
            "object-group network DMZ-HOSTS",
            "  network-object host 10.2.1.1");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getNetworkObjectGroups(), hasKey("WEB-SERVERS"));
    assertThat(vc.getNetworkObjectGroups(), hasKey("DMZ-HOSTS"));
  }

  @Test
  public void testObjectGroupWithNumbers() {
    String config =
        join(
            "object-group network 192-168-1-0",
            "  network-object 192.168.1.0 255.255.255.0",
            "object-group network host-10-1-1-1",
            "  network-object host 10.1.1.1");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getNetworkObjectGroups(), hasKey("192-168-1-0"));
    assertThat(vc.getNetworkObjectGroups(), hasKey("host-10-1-1-1"));
  }

  @Test
  public void testObjectGroupSerialization() {
    String config =
        join(
            "object-group network WEB_SERVERS",
            "  network-object host 10.1.1.1",
            "  network-object host 10.1.1.2",
            "  network-object 192.168.1.0 255.255.255.0");

    FtdConfiguration vc = parseVendorConfigWithSerialization(config);

    FtdNetworkObjectGroup group = vc.getNetworkObjectGroups().get("WEB_SERVERS");
    assertThat(group.getMembers(), hasSize(3));
  }

  @Test
  public void testObjectGroupVendorConversion() {
    String config =
        join(
            "object-group network WEB_SERVERS",
            "  network-object host 10.1.1.1",
            "  network-object host 10.1.1.2");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    assertThat(c, notNullValue());
  }

  @Test
  public void testObjectGroupWithObjects() {
    String config =
        join(
            "object network WEB1",
            "  host 10.1.1.1",
            "object network WEB2",
            "  host 10.1.1.2",
            "object network WEB3",
            "  host 10.1.1.3",
            "object-group network ALL_WEB",
            "  network-object object WEB1",
            "  network-object object WEB2",
            "  network-object object WEB3");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getNetworkObjects().keySet(), hasSize(3));
    assertThat(vc.getNetworkObjectGroups().keySet(), hasSize(1));

    FtdNetworkObjectGroup group = vc.getNetworkObjectGroups().get("ALL_WEB");
    assertThat(group.getMembers(), hasSize(3));
  }

  @Test
  public void testObjectGroupComplexHierarchy() {
    String config =
        join(
            "object-group network LEVEL1",
            "  network-object host 10.1.1.1",
            "object-group network LEVEL2",
            "  network-object host 10.2.1.1",
            "  group-object LEVEL1",
            "object-group network LEVEL3",
            "  network-object host 10.3.1.1",
            "  group-object LEVEL2");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdNetworkObjectGroup level1 = vc.getNetworkObjectGroups().get("LEVEL1");
    assertThat(level1.getMembers(), hasSize(1));

    FtdNetworkObjectGroup level2 = vc.getNetworkObjectGroups().get("LEVEL2");
    assertThat(level2.getMembers(), hasSize(2));

    FtdNetworkObjectGroup level3 = vc.getNetworkObjectGroups().get("LEVEL3");
    assertThat(level3.getMembers(), hasSize(2));
  }

  @Test
  public void testLargeObjectGroup() {
    StringBuilder sb = new StringBuilder();
    sb.append("object-group network LARGE_GROUP\n");
    for (int i = 1; i <= 100; i++) {
      sb.append("  network-object host 10.1.")
          .append(i / 255)
          .append(".")
          .append(i % 255)
          .append("\n");
    }

    FtdConfiguration vc = parseVendorConfig(sb.toString());

    FtdNetworkObjectGroup group = vc.getNetworkObjectGroups().get("LARGE_GROUP");
    assertThat(group.getMembers(), hasSize(100));
  }

  @Test
  public void testEmptyObjectGroup() {
    String config = "object-group network EMPTY_GROUP\n";

    FtdConfiguration vc = parseVendorConfig(config);

    FtdNetworkObjectGroup group = vc.getNetworkObjectGroups().get("EMPTY_GROUP");
    assertThat(group, notNullValue());
    assertThat(group.getMembers(), hasSize(0));
  }

  @Test
  public void testObjectGroupWithDifferentPrefixLengths() {
    String config =
        join(
            "object-group network VARIOUS_SUBNETS",
            "  network-object 10.0.0.0 255.0.0.0",
            "  network-object 192.168.0.0 255.255.0.0",
            "  network-object 172.16.1.0 255.255.255.0",
            "  network-object 203.0.113.0 255.255.255.252");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdNetworkObjectGroup group = vc.getNetworkObjectGroups().get("VARIOUS_SUBNETS");
    assertThat(group.getMembers(), hasSize(4));
  }
}
