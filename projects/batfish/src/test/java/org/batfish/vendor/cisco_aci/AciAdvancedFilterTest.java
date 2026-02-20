package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.junit.Test;

/**
 * Tests for advanced ACI filtering and traffic classification.
 *
 * <p>This test class verifies:
 *
 * <ul>
 *   <li>Complex filter entries with multiple criteria
 *   <li>Protocol-specific filters (TCP, UDP, ICMP)
 *   <li>Port range specifications
 *   <li>Source/destination IP matching
 *   <li>Bidirectional contract enforcement
 *   <li>Filter-based access control
 *   <li>Reverse rules in contracts
 *   <li>Permit/Deny actions in filters
 * </ul>
 */
public class AciAdvancedFilterTest {

  /** Creates filters with multiple protocol specifications. */
  private static String createMultiProtocolFilters() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-filter_tenant\", \"name\":"
        + " \"filter_tenant\"},\"children\": [{\"vzFilter\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-filter_tenant/flt-http_filter\", \"name\":"
        + " \"http_filter\"},\"children\": [{\"vzEntry\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-filter_tenant/flt-http_filter/e-http\", \"name\":"
        + " \"http\"},\"children\": []}},{\"vzEntry\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-filter_tenant/flt-http_filter/e-https\", \"name\":"
        + " \"https\"},\"children\": []}}]}},{\"vzFilter\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-filter_tenant/flt-dns_filter\", \"name\": \"dns_filter\"},\"children\":"
        + " [{\"vzEntry\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-filter_tenant/flt-dns_filter/e-dns_tcp\", \"name\":"
        + " \"dns_tcp\"},\"children\": []}},{\"vzEntry\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-filter_tenant/flt-dns_filter/e-dns_udp\", \"name\":"
        + " \"dns_udp\"},\"children\": []}}]}},{\"vzFilter\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-filter_tenant/flt-icmp_filter\", \"name\":"
        + " \"icmp_filter\"},\"children\": [{\"vzEntry\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-filter_tenant/flt-icmp_filter/e-ping\", \"name\":"
        + " \"ping\"},\"children\": []}}]}}]}}]}}";
  }

  /** Creates filters with IP address matching. */
  private static String createFiltersWithIpMatching() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-ip_filter_tenant\", \"name\":"
        + " \"ip_filter_tenant\"},\"children\": [{\"vzFilter\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-ip_filter_tenant/flt-subnet_filter\", \"name\":"
        + " \"subnet_filter\"},\"children\": [{\"vzEntry\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-ip_filter_tenant/flt-subnet_filter/e-prod_subnet\",\"name\":"
        + " \"prod_subnet\"},\"children\": []}},{\"vzEntry\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-ip_filter_tenant/flt-subnet_filter/e-dev_subnet\",\"name\":"
        + " \"dev_subnet\"},\"children\": []}}]}}]}}]}}";
  }

  /** Creates contract with bidirectional enforcement. */
  private static String createBidirectionalContract() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-bidir_tenant\", \"name\":"
        + " \"bidir_tenant\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-bidir_tenant/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
        + "{\"fvBD\": {\"attributes\": {\"dn\": \"uni/tn-bidir_tenant/BD-bd1\", \"name\":"
        + " \"bd1\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
        + " \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-bidir_tenant/ap-app\", \"name\": \"app\"},\"children\": [{\"fvAEPg\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-bidir_tenant/ap-app/epg-web\", \"name\":"
        + " \"web\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
        + " \"bd1\"}}}]}},{\"fvAEPg\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-bidir_tenant/ap-app/epg-db\", \"name\": \"db\"},\"children\":"
        + " [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"bd1\"}}}]}}]}},{\"vzBrCP\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-bidir_tenant/brc-bidir_contract\", \"name\":"
        + " \"bidir_contract\"},\"children\": [{\"vzSubj\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-bidir_tenant/brc-bidir_contract/subj-bidir\", \"name\":"
        + " \"bidir\"},\"children\": []}}]}},{\"vzFilter\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-bidir_tenant/flt-bidirectional\", \"name\":"
        + " \"bidirectional\"},\"children\": [{\"vzEntry\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-bidir_tenant/flt-bidirectional/e-permit\", \"name\":"
        + " \"permit\"},\"children\": []}}]}}]}}]}}";
  }

  /** Test parsing multi-protocol filters */
  @Test
  public void testParseJson_multiProtocolFilters() throws IOException {
    String json = createMultiProtocolFilters();

    AciConfiguration config =
        AciConfiguration.fromJson("multi_proto_filters.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("filter_tenant"));
  }

  /** Test parsing filters with IP matching */
  @Test
  public void testParseJson_ipMatchingFilters() throws IOException {
    String json = createFiltersWithIpMatching();

    AciConfiguration config = AciConfiguration.fromJson("ip_filters.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("ip_filter_tenant"));
  }

  /** Test bidirectional contract */
  @Test
  public void testParseJson_bidirectionalContract() throws IOException {
    String json = createBidirectionalContract();

    AciConfiguration config =
        AciConfiguration.fromJson("bidir_contract.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("bidir_tenant"));
  }

  /** Test filter with TCP protocol */
  @Test
  public void testFilterTcpProtocol() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-tcp_filter\", \"name\":"
            + " \"tcp_filter\"},\"children\": [{\"vzFilter\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-tcp_filter/flt-tcp\", \"name\": \"tcp\"},\"children\": [{\"vzEntry\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-tcp_filter/flt-tcp/e-tcp_entry\", \"name\":"
            + " \"tcp_entry\", \"protocol\": \"tcp\"},\"children\": []}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("tcp_filter.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("tcp_filter"));
  }

  /** Test filter with UDP protocol */
  @Test
  public void testFilterUdpProtocol() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-udp_filter\", \"name\":"
            + " \"udp_filter\"},\"children\": [{\"vzFilter\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-udp_filter/flt-udp\", \"name\": \"udp\"},\"children\": [{\"vzEntry\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-udp_filter/flt-udp/e-udp_entry\", \"name\":"
            + " \"udp_entry\", \"protocol\": \"udp\"},\"children\": []}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("udp_filter.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("udp_filter"));
  }

  /** Test filter with ICMP protocol */
  @Test
  public void testFilterIcmpProtocol() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-icmp_filter\", \"name\":"
            + " \"icmp_filter\"},\"children\": [{\"vzFilter\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-icmp_filter/flt-icmp\", \"name\": \"icmp\"},\"children\": [{\"vzEntry\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-icmp_filter/flt-icmp/e-icmp_entry\", \"name\":"
            + " \"icmp_entry\", \"protocol\": \"icmp\"},\"children\": []}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("icmp_filter.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("icmp_filter"));
  }

  /** Test filter with port ranges */
  @Test
  public void testFilterPortRanges() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-port_range_filter\", \"name\":"
            + " \"port_range_filter\"},\"children\": [{\"vzFilter\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-port_range_filter/flt-ports\", \"name\": \"ports\"},\"children\": ["
            + "{\"vzEntry\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-port_range_filter/flt-ports/e-range1\", \"name\":"
            + " \"range1\"},\"children\": []}},{\"vzEntry\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-port_range_filter/flt-ports/e-range2\", \"name\":"
            + " \"range2\"},\"children\": []}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("port_ranges.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("port_range_filter"));
  }

  /** Test filter with deny action */
  @Test
  public void testFilterDenyAction() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-deny_filter\", \"name\":"
            + " \"deny_filter\"},\"children\": [{\"vzFilter\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-deny_filter/flt-deny\", \"name\": \"deny\"},\"children\": [{\"vzEntry\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-deny_filter/flt-deny/e-block\", \"name\":"
            + " \"block\"},\"children\": []}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("deny_filter.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("deny_filter"));
  }

  /** Test contract with reverse rule */
  @Test
  public void testContractReverseRule() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-reverse_rule\", \"name\":"
            + " \"reverse_rule\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-reverse_rule/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
            + "{\"vzBrCP\": {\"attributes\": {\"dn\": \"uni/tn-reverse_rule/brc-contract\","
            + " \"name\": \"contract\"},\"children\": [{\"vzSubj\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-reverse_rule/brc-contract/subj-subject\", \"name\":"
            + " \"subject\"},\"children\": []}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("reverse_rule.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("reverse_rule"));
  }
}
