package org.batfish.grammar.host;

import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.datamodel.matchers.IpsecSessionMatchers.hasNegotiatedIpsecP2Proposal;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.ValueGraph;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.batfish.common.util.IpsecUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecEncapsulationMode;
import org.batfish.datamodel.IpsecPeerConfigId;
import org.batfish.datamodel.IpsecProtocol;
import org.batfish.datamodel.IpsecSession;
import org.batfish.datamodel.matchers.IpsecPhase2ProposalMatchers;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class HostTest {

  private static String TESTRIG_PREFIX = "org/batfish/grammar/host/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  public Map<String, Configuration> natIpsecVpnsTestHelper(String hostFilename) throws IOException {
    String testrigResourcePrefix = TESTRIG_PREFIX + "ipsec-vpn-host-aws-cisco";
    List<String> awsFilenames =
        ImmutableList.of(
            "AvailabilityZones-us-west-2.json",
            "CustomerGateways-us-west-2.json",
            "InternetGateways-us-west-2.json",
            "NetworkAcls-us-west-2.json",
            "NetworkInterfaces-us-west-2.json",
            "RouteTables-us-west-2.json",
            "SecurityGroups-us-west-2.json",
            "Subnets-us-west-2.json",
            "VpcEndpoints-us-west-2.json",
            "Vpcs-us-west-2.json",
            "VpnConnections-us-west-2.json",
            "VpnGateways-us-west-2.json");
    List<String> configurationFilenames = ImmutableList.of("cisco_host");
    List<String> hostFilenames = ImmutableList.of(hostFilename);

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setAwsText(testrigResourcePrefix, awsFilenames)
                .setConfigurationText(testrigResourcePrefix, configurationFilenames)
                .setHostsText(testrigResourcePrefix, hostFilenames)
                .build(),
            _folder);
    return batfish.loadConfigurations();
  }

  @Test
  public void testNatIpsecVpnsNotShared() throws IOException {
    Map<String, Configuration> configurations = natIpsecVpnsTestHelper("host1-not-shared.json");

    /*
     * NAT settings on host1 (not-shared version) should result in tunnel interfaces being down
     */
    assertThat(configurations.get("cisco_host").getAllInterfaces().get("Tunnel1"), not(isActive()));
    assertThat(configurations.get("cisco_host").getAllInterfaces().get("Tunnel2"), not(isActive()));
    assertThat(configurations.get("vgw-81fd279f").getAllInterfaces().get("vpn1"), not(isActive()));
    assertThat(configurations.get("vgw-81fd279f").getAllInterfaces().get("vpn2"), not(isActive()));
  }

  @Test
  public void testNatIpsecVpnsShared() throws IOException {
    Map<String, Configuration> configurations = natIpsecVpnsTestHelper("host1-shared.json");

    /*
     * NAT settings on host1 (shared version) should result in tunnel interfaces being up
     */
    assertThat(configurations.get("cisco_host").getAllInterfaces().get("Tunnel1"), isActive());
    assertThat(configurations.get("cisco_host").getAllInterfaces().get("Tunnel2"), isActive());
    assertThat(configurations.get("vgw-81fd279f").getAllInterfaces().get("vpn1"), isActive());
    assertThat(configurations.get("vgw-81fd279f").getAllInterfaces().get("vpn2"), isActive());
  }

  @Test
  public void testNatIpsecTopologyNotShared() throws IOException {
    Map<String, Configuration> configurations = natIpsecVpnsTestHelper("host1-not-shared.json");

    ValueGraph<IpsecPeerConfigId, IpsecSession> ipsecTopology =
        IpsecUtil.initIpsecTopology(configurations);

    IpsecPeerConfigId tunnel1 = new IpsecPeerConfigId("Tunnel1", "cisco_host");
    IpsecPeerConfigId tunnel2 = new IpsecPeerConfigId("Tunnel2", "cisco_host");
    IpsecPeerConfigId awsVpn1 = new IpsecPeerConfigId("vpn-ba2e34a8-1", "vgw-81fd279f");
    IpsecPeerConfigId awsVpn2 = new IpsecPeerConfigId("vpn-ba2e34a8-2", "vgw-81fd279f");

    //  NAT settings on host1 (not-shared version) should result in one directional edges in the
    // topology since cisco_host's interface will not be mapped to the public IP

    // assert that edges exist from the cisco host's tunnel to AWS
    assertTrue(ipsecTopology.edgeValue(tunnel1, awsVpn1).isPresent());
    assertTrue(ipsecTopology.edgeValue(tunnel2, awsVpn2).isPresent());

    // assert that IPsec proposal is set in the IPsec session
    assertThat(
        ipsecTopology.edgeValue(tunnel1, awsVpn1).get(),
        hasNegotiatedIpsecP2Proposal(
            allOf(
                IpsecPhase2ProposalMatchers.hasAuthenticationAlgorithm(
                    IpsecAuthenticationAlgorithm.HMAC_SHA1_96),
                IpsecPhase2ProposalMatchers.hasEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC),
                IpsecPhase2ProposalMatchers.hasProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP)),
                IpsecPhase2ProposalMatchers.hasIpsecEncapsulationMode(
                    IpsecEncapsulationMode.TUNNEL))));
    assertThat(
        ipsecTopology.edgeValue(tunnel2, awsVpn2).get(),
        hasNegotiatedIpsecP2Proposal(
            allOf(
                IpsecPhase2ProposalMatchers.hasAuthenticationAlgorithm(
                    IpsecAuthenticationAlgorithm.HMAC_SHA1_96),
                IpsecPhase2ProposalMatchers.hasEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC),
                IpsecPhase2ProposalMatchers.hasProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP)),
                IpsecPhase2ProposalMatchers.hasIpsecEncapsulationMode(
                    IpsecEncapsulationMode.TUNNEL))));

    // no edges from AWS's VPN to cisco_host's tunnel as public IP (4.4.4.27) is not mapped to
    // anything
    assertFalse(ipsecTopology.edgeValue(awsVpn1, tunnel1).isPresent());
    assertFalse(ipsecTopology.edgeValue(awsVpn2, tunnel2).isPresent());
  }

  @Test
  public void testNatIpsecTopologyShared() throws IOException {
    Map<String, Configuration> configurations = natIpsecVpnsTestHelper("host1-shared.json");

    ValueGraph<IpsecPeerConfigId, IpsecSession> ipsecTopology =
        IpsecUtil.initIpsecTopology(configurations);

    IpsecPeerConfigId tunnel1 = new IpsecPeerConfigId("Tunnel1", "cisco_host");
    IpsecPeerConfigId tunnel2 = new IpsecPeerConfigId("Tunnel2", "cisco_host");
    IpsecPeerConfigId awsVpn1 = new IpsecPeerConfigId("vpn-ba2e34a8-1", "vgw-81fd279f");
    IpsecPeerConfigId awsVpn2 = new IpsecPeerConfigId("vpn-ba2e34a8-2", "vgw-81fd279f");

    //  In the shared version, now the public IP 4.4.4.27 will be mapped to the private IPs owned by
    // cisco_host, so edges will exist in both directions

    // edges exist from the cisco host's tunnel to AWS
    assertTrue(ipsecTopology.edgeValue(tunnel1, awsVpn1).isPresent());
    assertTrue(ipsecTopology.edgeValue(tunnel2, awsVpn2).isPresent());

    // edges also exist from AWS's VPN to cisco_host's tunnel as public IP (4.4.4.27) is now mapped
    // to
    // local addresses of the tunnel interfaces
    assertTrue(ipsecTopology.edgeValue(awsVpn1, tunnel1).isPresent());
    assertTrue(ipsecTopology.edgeValue(awsVpn2, tunnel2).isPresent());

    // assert that IPsec proposal is set in the IPsec sessions
    assertThat(
        ipsecTopology.edgeValue(tunnel1, awsVpn1).get(),
        hasNegotiatedIpsecP2Proposal(
            allOf(
                IpsecPhase2ProposalMatchers.hasAuthenticationAlgorithm(
                    IpsecAuthenticationAlgorithm.HMAC_SHA1_96),
                IpsecPhase2ProposalMatchers.hasEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC),
                IpsecPhase2ProposalMatchers.hasProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP)),
                IpsecPhase2ProposalMatchers.hasIpsecEncapsulationMode(
                    IpsecEncapsulationMode.TUNNEL))));
    assertThat(
        ipsecTopology.edgeValue(tunnel2, awsVpn2).get(),
        hasNegotiatedIpsecP2Proposal(
            allOf(
                IpsecPhase2ProposalMatchers.hasAuthenticationAlgorithm(
                    IpsecAuthenticationAlgorithm.HMAC_SHA1_96),
                IpsecPhase2ProposalMatchers.hasEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC),
                IpsecPhase2ProposalMatchers.hasProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP)),
                IpsecPhase2ProposalMatchers.hasIpsecEncapsulationMode(
                    IpsecEncapsulationMode.TUNNEL))));
    assertThat(
        ipsecTopology.edgeValue(awsVpn1, tunnel1).get(),
        hasNegotiatedIpsecP2Proposal(
            allOf(
                IpsecPhase2ProposalMatchers.hasAuthenticationAlgorithm(
                    IpsecAuthenticationAlgorithm.HMAC_SHA1_96),
                IpsecPhase2ProposalMatchers.hasEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC),
                IpsecPhase2ProposalMatchers.hasProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP)),
                IpsecPhase2ProposalMatchers.hasIpsecEncapsulationMode(
                    IpsecEncapsulationMode.TUNNEL))));
    assertThat(
        ipsecTopology.edgeValue(awsVpn2, tunnel2).get(),
        hasNegotiatedIpsecP2Proposal(
            allOf(
                IpsecPhase2ProposalMatchers.hasAuthenticationAlgorithm(
                    IpsecAuthenticationAlgorithm.HMAC_SHA1_96),
                IpsecPhase2ProposalMatchers.hasEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC),
                IpsecPhase2ProposalMatchers.hasProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP)),
                IpsecPhase2ProposalMatchers.hasIpsecEncapsulationMode(
                    IpsecEncapsulationMode.TUNNEL))));
  }
}
