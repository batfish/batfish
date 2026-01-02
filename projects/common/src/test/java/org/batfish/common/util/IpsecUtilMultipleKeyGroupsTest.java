package org.batfish.common.util;

import static org.batfish.common.util.IpsecUtil.getIpsecSession;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecSession;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.NetworkFactory;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link IpsecUtil} with multiple PFS key groups */
public class IpsecUtilMultipleKeyGroupsTest {

  private Configuration _initiatorHost;
  private Configuration _responderHost;
  private IpsecStaticPeerConfig.Builder _ipsecPeerConfigBuilder;
  private IpsecPhase2Policy _initiatorPhase2Policy;
  private IpsecPhase2Policy _responderPhase2Policy;
  private IpsecPhase2Proposal _ipsecPhase2Proposal;
  private IkePhase1Policy _initiatorIkePhase1Policy;
  private IkePhase1Policy _responderIkePhase1Policy;
  private IkePhase1Proposal _ikePhase1Proposal;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb = nf.configurationBuilder();
    _initiatorHost = cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    _responderHost = cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    _ipsecPeerConfigBuilder = IpsecStaticPeerConfig.builder();

    // Create a common IPsec Phase 2 proposal
    _ipsecPhase2Proposal = new IpsecPhase2Proposal();
    _ipsecPhase2Proposal.setAuthenticationAlgorithm(null);
    _ipsecPhase2Proposal.setEncryptionAlgorithm(null);
    _ipsecPhase2Proposal.setProtocols(null);

    // Create IPsec Phase 2 policies
    _initiatorPhase2Policy = new IpsecPhase2Policy();
    _responderPhase2Policy = new IpsecPhase2Policy();

    // Set up configurations with the proposal
    _initiatorHost.setIpsecPhase2Proposals(
        ImmutableSortedMap.of("proposal1", _ipsecPhase2Proposal));
    _responderHost.setIpsecPhase2Proposals(
        ImmutableSortedMap.of("proposal1", _ipsecPhase2Proposal));

    // Set up policies with the proposal
    _initiatorPhase2Policy.setProposals(ImmutableList.of("proposal1"));
    _responderPhase2Policy.setProposals(ImmutableList.of("proposal1"));

    // Set up configurations with the policies
    _initiatorHost.setIpsecPhase2Policies(ImmutableSortedMap.of("policy1", _initiatorPhase2Policy));
    _responderHost.setIpsecPhase2Policies(ImmutableSortedMap.of("policy1", _responderPhase2Policy));

    // Create IKE Phase 1 proposal
    _ikePhase1Proposal = new IkePhase1Proposal("ikeProposal1");
    _ikePhase1Proposal.setEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC);
    _ikePhase1Proposal.setHashingAlgorithm(IkeHashingAlgorithm.SHA_256);
    _ikePhase1Proposal.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP14);
    _ikePhase1Proposal.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);

    // Set up configurations with the IKE proposal
    _initiatorHost.setIkePhase1Proposals(ImmutableSortedMap.of("ikeProposal1", _ikePhase1Proposal));
    _responderHost.setIkePhase1Proposals(ImmutableSortedMap.of("ikeProposal1", _ikePhase1Proposal));

    // Create IKE Phase 1 keys
    IkePhase1Key initiatorKey = new IkePhase1Key();
    initiatorKey.setKeyHash("psk1");
    initiatorKey.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);

    IkePhase1Key responderKey = new IkePhase1Key();
    responderKey.setKeyHash("psk1");
    responderKey.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);

    // Create IKE Phase 1 policies
    _initiatorIkePhase1Policy = new IkePhase1Policy("ikePolicy1");
    _initiatorIkePhase1Policy.setIkePhase1Proposals(ImmutableList.of("ikeProposal1"));
    _initiatorIkePhase1Policy.setIkePhase1Key(initiatorKey);

    _responderIkePhase1Policy = new IkePhase1Policy("ikePolicy1");
    _responderIkePhase1Policy.setIkePhase1Proposals(ImmutableList.of("ikeProposal1"));
    _responderIkePhase1Policy.setIkePhase1Key(responderKey);

    // Set up configurations with the IKE policies
    _initiatorHost.setIkePhase1Policies(
        ImmutableSortedMap.of("ikePolicy1", _initiatorIkePhase1Policy));
    _responderHost.setIkePhase1Policies(
        ImmutableSortedMap.of("ikePolicy1", _responderIkePhase1Policy));
  }

  @Test
  public void testNegotiateIpsecSessionWithOverlappingKeyGroups() {
    // Set up initiator with multiple PFS key groups
    _initiatorPhase2Policy.setPfsKeyGroups(
        ImmutableSet.of(
            DiffieHellmanGroup.GROUP2, DiffieHellmanGroup.GROUP14, DiffieHellmanGroup.GROUP24));

    // Set up responder with multiple PFS key groups that overlap with initiator
    _responderPhase2Policy.setPfsKeyGroups(
        ImmutableSet.of(
            DiffieHellmanGroup.GROUP14, DiffieHellmanGroup.GROUP19, DiffieHellmanGroup.GROUP24));

    // Create peer configs
    IpsecStaticPeerConfig initiatorPeer =
        _ipsecPeerConfigBuilder
            .setIpsecPolicy("policy1")
            .setIkePhase1Policy("ikePolicy1")
            .setLocalAddress(Ip.parse("1.1.1.1"))
            .setDestinationAddress(Ip.parse("2.2.2.2"))
            .build();

    IpsecStaticPeerConfig responderPeer =
        _ipsecPeerConfigBuilder
            .setIpsecPolicy("policy1")
            .setIkePhase1Policy("ikePolicy1")
            .setLocalAddress(Ip.parse("2.2.2.2"))
            .setDestinationAddress(Ip.parse("1.1.1.1"))
            .build();

    // Negotiate session
    IpsecSession ipsecSession =
        getIpsecSession(_initiatorHost, _responderHost, initiatorPeer, responderPeer);

    // Verify negotiation succeeded
    assertThat(ipsecSession.getNegotiatedIpsecP2Proposal(), notNullValue());
  }

  @Test
  public void testNegotiateIpsecSessionWithNoOverlappingKeyGroups() {
    // Set up initiator with PFS key groups
    _initiatorPhase2Policy.setPfsKeyGroups(
        ImmutableSet.of(DiffieHellmanGroup.GROUP2, DiffieHellmanGroup.GROUP5));

    // Set up responder with non-overlapping PFS key groups
    _responderPhase2Policy.setPfsKeyGroups(
        ImmutableSet.of(DiffieHellmanGroup.GROUP14, DiffieHellmanGroup.GROUP24));

    // Create peer configs
    IpsecStaticPeerConfig initiatorPeer =
        _ipsecPeerConfigBuilder
            .setIpsecPolicy("policy1")
            .setIkePhase1Policy("ikePolicy1")
            .setLocalAddress(Ip.parse("1.1.1.1"))
            .setDestinationAddress(Ip.parse("2.2.2.2"))
            .build();

    IpsecStaticPeerConfig responderPeer =
        _ipsecPeerConfigBuilder
            .setIpsecPolicy("policy1")
            .setIkePhase1Policy("ikePolicy1")
            .setLocalAddress(Ip.parse("2.2.2.2"))
            .setDestinationAddress(Ip.parse("1.1.1.1"))
            .build();

    // Negotiate session
    IpsecSession ipsecSession =
        getIpsecSession(_initiatorHost, _responderHost, initiatorPeer, responderPeer);

    // Verify negotiation failed due to no overlapping PFS key groups
    assertThat(ipsecSession.getNegotiatedIpsecP2Proposal(), nullValue());
  }

  @Test
  public void testNegotiateIpsecSessionWithEmptyKeyGroups() {
    // Set up initiator with empty PFS key groups
    _initiatorPhase2Policy.setPfsKeyGroups(ImmutableSet.of());

    // Set up responder with PFS key groups
    _responderPhase2Policy.setPfsKeyGroups(
        ImmutableSet.of(DiffieHellmanGroup.GROUP14, DiffieHellmanGroup.GROUP24));

    // Create peer configs
    IpsecStaticPeerConfig initiatorPeer =
        _ipsecPeerConfigBuilder
            .setIpsecPolicy("policy1")
            .setIkePhase1Policy("ikePolicy1")
            .setLocalAddress(Ip.parse("1.1.1.1"))
            .setDestinationAddress(Ip.parse("2.2.2.2"))
            .build();

    IpsecStaticPeerConfig responderPeer =
        _ipsecPeerConfigBuilder
            .setIpsecPolicy("policy1")
            .setIkePhase1Policy("ikePolicy1")
            .setLocalAddress(Ip.parse("2.2.2.2"))
            .setDestinationAddress(Ip.parse("1.1.1.1"))
            .build();

    // Negotiate session
    IpsecSession ipsecSession =
        getIpsecSession(_initiatorHost, _responderHost, initiatorPeer, responderPeer);

    // Verify negotiation failed due to empty PFS key groups
    assertThat(ipsecSession.getNegotiatedIpsecP2Proposal(), nullValue());
  }

  @Test
  public void testBackwardCompatibility() {
    // Set up initiator with a single PFS key group using the old method
    _initiatorPhase2Policy.setPfsKeyGroup(DiffieHellmanGroup.GROUP14);

    // Set up responder with multiple PFS key groups that include the initiator's group
    _responderPhase2Policy.setPfsKeyGroups(
        ImmutableSet.of(
            DiffieHellmanGroup.GROUP2, DiffieHellmanGroup.GROUP14, DiffieHellmanGroup.GROUP24));

    // Create peer configs
    IpsecStaticPeerConfig initiatorPeer =
        _ipsecPeerConfigBuilder
            .setIpsecPolicy("policy1")
            .setIkePhase1Policy("ikePolicy1")
            .setLocalAddress(Ip.parse("1.1.1.1"))
            .setDestinationAddress(Ip.parse("2.2.2.2"))
            .build();

    IpsecStaticPeerConfig responderPeer =
        _ipsecPeerConfigBuilder
            .setIpsecPolicy("policy1")
            .setIkePhase1Policy("ikePolicy1")
            .setLocalAddress(Ip.parse("2.2.2.2"))
            .setDestinationAddress(Ip.parse("1.1.1.1"))
            .build();

    // Negotiate session
    IpsecSession ipsecSession =
        getIpsecSession(_initiatorHost, _responderHost, initiatorPeer, responderPeer);

    // Verify negotiation succeeded
    assertThat(ipsecSession.getNegotiatedIpsecP2Proposal(), notNullValue());
  }
}
