package org.batfish.grammar.aws;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIkePhase1Policy;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIkePhase1Proposal;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpsecPeerConfig;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpsecPhase2Policy;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpsecPhase2Proposal;
import static org.batfish.datamodel.matchers.IkePhase1PolicyMatchers.hasIkePhase1Key;
import static org.batfish.datamodel.matchers.IkePhase1PolicyMatchers.hasIkePhase1Proposals;
import static org.batfish.datamodel.matchers.IkePhase1PolicyMatchers.hasLocalInterface;
import static org.batfish.datamodel.matchers.IkePhase1PolicyMatchers.hasRemoteIdentity;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.batfish.datamodel.matchers.IpsecPeerConfigMatchers.hasDestinationAddress;
import static org.batfish.datamodel.matchers.IpsecPeerConfigMatchers.hasLocalAddress;
import static org.batfish.datamodel.matchers.IpsecPeerConfigMatchers.hasSourceInterface;
import static org.batfish.datamodel.matchers.IpsecPeerConfigMatchers.hasTunnelInterface;
import static org.batfish.datamodel.matchers.IpsecPeerConfigMatchers.isIpsecStaticPeerConfigThat;
import static org.batfish.representation.aws.AwsConfiguration.vpnExternalInterfaceName;
import static org.batfish.representation.aws.AwsConfiguration.vpnInterfaceName;
import static org.batfish.representation.aws.AwsConfiguration.vpnTunnelId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecEncapsulationMode;
import org.batfish.datamodel.IpsecProtocol;
import org.batfish.datamodel.matchers.IkePhase1KeyMatchers;
import org.batfish.datamodel.matchers.IkePhase1ProposalMatchers;
import org.batfish.datamodel.matchers.IpsecPeerConfigMatchers;
import org.batfish.datamodel.matchers.IpsecPhase2PolicyMatchers;
import org.batfish.datamodel.matchers.IpsecPhase2ProposalMatchers;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of an AWS network with IPsec peering to a Cisco router on-prem. */
public class AwsIpsecTest {

  private static final String TESTRIG_PREFIX = "org/batfish/grammar/aws/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private SortedMap<String, Configuration> awsIpsecHelper() throws IOException {
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

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setAwsFiles(testrigResourcePrefix, awsFilenames)
                .setConfigurationFiles(testrigResourcePrefix, configurationFilenames)
                .build(),
            _folder);
    return batfish.loadConfigurations(batfish.getSnapshot());
  }

  @Test
  public void testIkePhase1Confs() throws IOException {
    SortedMap<String, Configuration> configurations = awsIpsecHelper();

    assertThat(configurations, hasKey("vgw-81fd279f"));

    Configuration vgwConfiguration = configurations.get("vgw-81fd279f");

    // there are two copies of all IKE P1 configs with suffix '1' and '2', checking conversion on
    // just the first one
    // test for IKE phase 1 policy
    assertThat(
        vgwConfiguration,
        hasIkePhase1Policy(
            "vpn-ba2e34a8-1",
            allOf(
                hasIkePhase1Key(
                    IkePhase1KeyMatchers.hasKeyHash(
                        CommonUtil.sha256Digest("abcdefghijklmnop" + CommonUtil.salt()))),
                hasRemoteIdentity(containsIp(Ip.parse("4.4.4.27"))),
                hasLocalInterface(equalTo("external-vpn-ba2e34a8-1")),
                hasIkePhase1Proposals(
                    equalTo(
                        ImmutableList.of(
                            "ike_proposal_2_SHA1_AES128",
                            "ike_proposal_2_SHA1_AES256",
                            "ike_proposal_2_SHA1_AES128-GCM-16",
                            "ike_proposal_2_SHA1_AES256-GCM-16",
                            "ike_proposal_2_SHA2-256_AES128",
                            "ike_proposal_2_SHA2-256_AES256",
                            "ike_proposal_2_SHA2-256_AES128-GCM-16",
                            "ike_proposal_2_SHA2-256_AES256-GCM-16",
                            "ike_proposal_2_SHA2-384_AES128",
                            "ike_proposal_2_SHA2-384_AES256",
                            "ike_proposal_2_SHA2-384_AES128-GCM-16",
                            "ike_proposal_2_SHA2-384_AES256-GCM-16",
                            "ike_proposal_2_SHA2-512_AES128",
                            "ike_proposal_2_SHA2-512_AES256",
                            "ike_proposal_2_SHA2-512_AES128-GCM-16",
                            "ike_proposal_2_SHA2-512_AES256-GCM-16",
                            "ike_proposal_14_SHA1_AES128",
                            "ike_proposal_14_SHA1_AES256",
                            "ike_proposal_14_SHA1_AES128-GCM-16",
                            "ike_proposal_14_SHA1_AES256-GCM-16",
                            "ike_proposal_14_SHA2-256_AES128",
                            "ike_proposal_14_SHA2-256_AES256",
                            "ike_proposal_14_SHA2-256_AES128-GCM-16",
                            "ike_proposal_14_SHA2-256_AES256-GCM-16",
                            "ike_proposal_14_SHA2-384_AES128",
                            "ike_proposal_14_SHA2-384_AES256",
                            "ike_proposal_14_SHA2-384_AES128-GCM-16",
                            "ike_proposal_14_SHA2-384_AES256-GCM-16",
                            "ike_proposal_14_SHA2-512_AES128",
                            "ike_proposal_14_SHA2-512_AES256",
                            "ike_proposal_14_SHA2-512_AES128-GCM-16",
                            "ike_proposal_14_SHA2-512_AES256-GCM-16",
                            "ike_proposal_15_SHA1_AES128",
                            "ike_proposal_15_SHA1_AES256",
                            "ike_proposal_15_SHA1_AES128-GCM-16",
                            "ike_proposal_15_SHA1_AES256-GCM-16",
                            "ike_proposal_15_SHA2-256_AES128",
                            "ike_proposal_15_SHA2-256_AES256",
                            "ike_proposal_15_SHA2-256_AES128-GCM-16",
                            "ike_proposal_15_SHA2-256_AES256-GCM-16",
                            "ike_proposal_15_SHA2-384_AES128",
                            "ike_proposal_15_SHA2-384_AES256",
                            "ike_proposal_15_SHA2-384_AES128-GCM-16",
                            "ike_proposal_15_SHA2-384_AES256-GCM-16",
                            "ike_proposal_15_SHA2-512_AES128",
                            "ike_proposal_15_SHA2-512_AES256",
                            "ike_proposal_15_SHA2-512_AES128-GCM-16",
                            "ike_proposal_15_SHA2-512_AES256-GCM-16",
                            "ike_proposal_16_SHA1_AES128",
                            "ike_proposal_16_SHA1_AES256",
                            "ike_proposal_16_SHA1_AES128-GCM-16",
                            "ike_proposal_16_SHA1_AES256-GCM-16",
                            "ike_proposal_16_SHA2-256_AES128",
                            "ike_proposal_16_SHA2-256_AES256",
                            "ike_proposal_16_SHA2-256_AES128-GCM-16",
                            "ike_proposal_16_SHA2-256_AES256-GCM-16",
                            "ike_proposal_16_SHA2-384_AES128",
                            "ike_proposal_16_SHA2-384_AES256",
                            "ike_proposal_16_SHA2-384_AES128-GCM-16",
                            "ike_proposal_16_SHA2-384_AES256-GCM-16",
                            "ike_proposal_16_SHA2-512_AES128",
                            "ike_proposal_16_SHA2-512_AES256",
                            "ike_proposal_16_SHA2-512_AES128-GCM-16",
                            "ike_proposal_16_SHA2-512_AES256-GCM-16",
                            "ike_proposal_17_SHA1_AES128",
                            "ike_proposal_17_SHA1_AES256",
                            "ike_proposal_17_SHA1_AES128-GCM-16",
                            "ike_proposal_17_SHA1_AES256-GCM-16",
                            "ike_proposal_17_SHA2-256_AES128",
                            "ike_proposal_17_SHA2-256_AES256",
                            "ike_proposal_17_SHA2-256_AES128-GCM-16",
                            "ike_proposal_17_SHA2-256_AES256-GCM-16",
                            "ike_proposal_17_SHA2-384_AES128",
                            "ike_proposal_17_SHA2-384_AES256",
                            "ike_proposal_17_SHA2-384_AES128-GCM-16",
                            "ike_proposal_17_SHA2-384_AES256-GCM-16",
                            "ike_proposal_17_SHA2-512_AES128",
                            "ike_proposal_17_SHA2-512_AES256",
                            "ike_proposal_17_SHA2-512_AES128-GCM-16",
                            "ike_proposal_17_SHA2-512_AES256-GCM-16",
                            "ike_proposal_18_SHA1_AES128",
                            "ike_proposal_18_SHA1_AES256",
                            "ike_proposal_18_SHA1_AES128-GCM-16",
                            "ike_proposal_18_SHA1_AES256-GCM-16",
                            "ike_proposal_18_SHA2-256_AES128",
                            "ike_proposal_18_SHA2-256_AES256",
                            "ike_proposal_18_SHA2-256_AES128-GCM-16",
                            "ike_proposal_18_SHA2-256_AES256-GCM-16",
                            "ike_proposal_18_SHA2-384_AES128",
                            "ike_proposal_18_SHA2-384_AES256",
                            "ike_proposal_18_SHA2-384_AES128-GCM-16",
                            "ike_proposal_18_SHA2-384_AES256-GCM-16",
                            "ike_proposal_18_SHA2-512_AES128",
                            "ike_proposal_18_SHA2-512_AES256",
                            "ike_proposal_18_SHA2-512_AES128-GCM-16",
                            "ike_proposal_18_SHA2-512_AES256-GCM-16",
                            "ike_proposal_19_SHA1_AES128",
                            "ike_proposal_19_SHA1_AES256",
                            "ike_proposal_19_SHA1_AES128-GCM-16",
                            "ike_proposal_19_SHA1_AES256-GCM-16",
                            "ike_proposal_19_SHA2-256_AES128",
                            "ike_proposal_19_SHA2-256_AES256",
                            "ike_proposal_19_SHA2-256_AES128-GCM-16",
                            "ike_proposal_19_SHA2-256_AES256-GCM-16",
                            "ike_proposal_19_SHA2-384_AES128",
                            "ike_proposal_19_SHA2-384_AES256",
                            "ike_proposal_19_SHA2-384_AES128-GCM-16",
                            "ike_proposal_19_SHA2-384_AES256-GCM-16",
                            "ike_proposal_19_SHA2-512_AES128",
                            "ike_proposal_19_SHA2-512_AES256",
                            "ike_proposal_19_SHA2-512_AES128-GCM-16",
                            "ike_proposal_19_SHA2-512_AES256-GCM-16",
                            "ike_proposal_20_SHA1_AES128",
                            "ike_proposal_20_SHA1_AES256",
                            "ike_proposal_20_SHA1_AES128-GCM-16",
                            "ike_proposal_20_SHA1_AES256-GCM-16",
                            "ike_proposal_20_SHA2-256_AES128",
                            "ike_proposal_20_SHA2-256_AES256",
                            "ike_proposal_20_SHA2-256_AES128-GCM-16",
                            "ike_proposal_20_SHA2-256_AES256-GCM-16",
                            "ike_proposal_20_SHA2-384_AES128",
                            "ike_proposal_20_SHA2-384_AES256",
                            "ike_proposal_20_SHA2-384_AES128-GCM-16",
                            "ike_proposal_20_SHA2-384_AES256-GCM-16",
                            "ike_proposal_20_SHA2-512_AES128",
                            "ike_proposal_20_SHA2-512_AES256",
                            "ike_proposal_20_SHA2-512_AES128-GCM-16",
                            "ike_proposal_20_SHA2-512_AES256-GCM-16",
                            "ike_proposal_21_SHA1_AES128",
                            "ike_proposal_21_SHA1_AES256",
                            "ike_proposal_21_SHA1_AES128-GCM-16",
                            "ike_proposal_21_SHA1_AES256-GCM-16",
                            "ike_proposal_21_SHA2-256_AES128",
                            "ike_proposal_21_SHA2-256_AES256",
                            "ike_proposal_21_SHA2-256_AES128-GCM-16",
                            "ike_proposal_21_SHA2-256_AES256-GCM-16",
                            "ike_proposal_21_SHA2-384_AES128",
                            "ike_proposal_21_SHA2-384_AES256",
                            "ike_proposal_21_SHA2-384_AES128-GCM-16",
                            "ike_proposal_21_SHA2-384_AES256-GCM-16",
                            "ike_proposal_21_SHA2-512_AES128",
                            "ike_proposal_21_SHA2-512_AES256",
                            "ike_proposal_21_SHA2-512_AES128-GCM-16",
                            "ike_proposal_21_SHA2-512_AES256-GCM-16",
                            "ike_proposal_22_SHA1_AES128",
                            "ike_proposal_22_SHA1_AES256",
                            "ike_proposal_22_SHA1_AES128-GCM-16",
                            "ike_proposal_22_SHA1_AES256-GCM-16",
                            "ike_proposal_22_SHA2-256_AES128",
                            "ike_proposal_22_SHA2-256_AES256",
                            "ike_proposal_22_SHA2-256_AES128-GCM-16",
                            "ike_proposal_22_SHA2-256_AES256-GCM-16",
                            "ike_proposal_22_SHA2-384_AES128",
                            "ike_proposal_22_SHA2-384_AES256",
                            "ike_proposal_22_SHA2-384_AES128-GCM-16",
                            "ike_proposal_22_SHA2-384_AES256-GCM-16",
                            "ike_proposal_22_SHA2-512_AES128",
                            "ike_proposal_22_SHA2-512_AES256",
                            "ike_proposal_22_SHA2-512_AES128-GCM-16",
                            "ike_proposal_22_SHA2-512_AES256-GCM-16",
                            "ike_proposal_23_SHA1_AES128",
                            "ike_proposal_23_SHA1_AES256",
                            "ike_proposal_23_SHA1_AES128-GCM-16",
                            "ike_proposal_23_SHA1_AES256-GCM-16",
                            "ike_proposal_23_SHA2-256_AES128",
                            "ike_proposal_23_SHA2-256_AES256",
                            "ike_proposal_23_SHA2-256_AES128-GCM-16",
                            "ike_proposal_23_SHA2-256_AES256-GCM-16",
                            "ike_proposal_23_SHA2-384_AES128",
                            "ike_proposal_23_SHA2-384_AES256",
                            "ike_proposal_23_SHA2-384_AES128-GCM-16",
                            "ike_proposal_23_SHA2-384_AES256-GCM-16",
                            "ike_proposal_23_SHA2-512_AES128",
                            "ike_proposal_23_SHA2-512_AES256",
                            "ike_proposal_23_SHA2-512_AES128-GCM-16",
                            "ike_proposal_23_SHA2-512_AES256-GCM-16",
                            "ike_proposal_24_SHA1_AES128",
                            "ike_proposal_24_SHA1_AES256",
                            "ike_proposal_24_SHA1_AES128-GCM-16",
                            "ike_proposal_24_SHA1_AES256-GCM-16",
                            "ike_proposal_24_SHA2-256_AES128",
                            "ike_proposal_24_SHA2-256_AES256",
                            "ike_proposal_24_SHA2-256_AES128-GCM-16",
                            "ike_proposal_24_SHA2-256_AES256-GCM-16",
                            "ike_proposal_24_SHA2-384_AES128",
                            "ike_proposal_24_SHA2-384_AES256",
                            "ike_proposal_24_SHA2-384_AES128-GCM-16",
                            "ike_proposal_24_SHA2-384_AES256-GCM-16",
                            "ike_proposal_24_SHA2-512_AES128",
                            "ike_proposal_24_SHA2-512_AES256",
                            "ike_proposal_24_SHA2-512_AES128-GCM-16",
                            "ike_proposal_24_SHA2-512_AES256-GCM-16"))))));

    // test for IKE phase1 proposals
    assertThat(
        vgwConfiguration,
        hasIkePhase1Proposal(
            "ike_proposal_2_SHA1_AES128",
            allOf(
                IkePhase1ProposalMatchers.hasEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC),
                IkePhase1ProposalMatchers.hasAuthenticationMethod(
                    IkeAuthenticationMethod.PRE_SHARED_KEYS),
                IkePhase1ProposalMatchers.hasHashingAlgorithm(IkeHashingAlgorithm.SHA1),
                IkePhase1ProposalMatchers.hasDiffieHellmanGroup(DiffieHellmanGroup.GROUP2))));
  }

  @Test
  public void testIpsecPhase2Confs() throws IOException {
    SortedMap<String, Configuration> configurations = awsIpsecHelper();

    assertThat(configurations, hasKey("vgw-81fd279f"));

    Configuration vgwConfiguration = configurations.get("vgw-81fd279f");

    // there are two copies of all IPsec P2 configs with suffix '1' and '2', checking conversion on
    // just the first one
    // test for IPsec phase 2 proposal
    assertThat(
        vgwConfiguration,
        hasIpsecPhase2Proposal(
            "ipsec_proposal_HMAC_SHA1_96_AES_128_CBC",
            allOf(
                IpsecPhase2ProposalMatchers.hasAuthenticationAlgorithm(
                    IpsecAuthenticationAlgorithm.HMAC_SHA1_96),
                IpsecPhase2ProposalMatchers.hasEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC),
                IpsecPhase2ProposalMatchers.hasProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP)),
                IpsecPhase2ProposalMatchers.hasIpsecEncapsulationMode(
                    IpsecEncapsulationMode.TUNNEL))));

    // test for IPsec phase 2 policy
    assertThat(
        vgwConfiguration,
        hasIpsecPhase2Policy(
            "vpn-ba2e34a8-2-GROUP2",
            allOf(
                IpsecPhase2PolicyMatchers.hasIpsecProposals(
                    equalTo(
                        ImmutableList.of(
                            "ipsec_proposal_HMAC_SHA1_96_AES_128_CBC",
                            "ipsec_proposal_HMAC_SHA1_96_AES_256_CBC",
                            "ipsec_proposal_HMAC_SHA1_96_AES_128_GCM",
                            "ipsec_proposal_HMAC_SHA1_96_AES_256_GCM",
                            "ipsec_proposal_HMAC_SHA_256_128_AES_128_CBC",
                            "ipsec_proposal_HMAC_SHA_256_128_AES_256_CBC",
                            "ipsec_proposal_HMAC_SHA_256_128_AES_128_GCM",
                            "ipsec_proposal_HMAC_SHA_256_128_AES_256_GCM",
                            "ipsec_proposal_HMAC_SHA_384_AES_128_CBC",
                            "ipsec_proposal_HMAC_SHA_384_AES_256_CBC",
                            "ipsec_proposal_HMAC_SHA_384_AES_128_GCM",
                            "ipsec_proposal_HMAC_SHA_384_AES_256_GCM",
                            "ipsec_proposal_HMAC_SHA_512_AES_128_CBC",
                            "ipsec_proposal_HMAC_SHA_512_AES_256_CBC",
                            "ipsec_proposal_HMAC_SHA_512_AES_128_GCM",
                            "ipsec_proposal_HMAC_SHA_512_AES_256_GCM"))),
                IpsecPhase2PolicyMatchers.hasPfsKeyGroup(equalTo(DiffieHellmanGroup.GROUP2)))));

    // test for IPsec peer config
    assertThat(
        vgwConfiguration,
        hasIpsecPeerConfig(
            "vpn-ba2e34a8-1-GROUP2",
            isIpsecStaticPeerConfigThat(
                allOf(
                    hasDestinationAddress(Ip.parse("4.4.4.27")),
                    IpsecPeerConfigMatchers.hasIkePhase1Policy("vpn-ba2e34a8-1"),
                    IpsecPeerConfigMatchers.hasIpsecPolicy("vpn-ba2e34a8-1-GROUP2"),
                    hasSourceInterface(vpnExternalInterfaceName(vpnTunnelId("vpn-ba2e34a8", 1))),
                    hasLocalAddress(Ip.parse("1.2.3.4")),
                    hasTunnelInterface(
                        equalTo(vpnInterfaceName(vpnTunnelId("vpn-ba2e34a8", 1))))))));
  }
}
