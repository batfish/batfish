package org.batfish.common.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecDynamicPeerConfig;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecPeerConfigId;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecSession;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.NetworkConfigurations;

public class IpsecUtil {

  /**
   * Compute the IPSec topology -- a network of {@link IpsecPeerConfigId}s connected by {@link
   * IpsecSession}s
   *
   * @param configurations {@link Configuration}s for which the topology has to be computed
   * @return the constructed {@link ValueGraph} for the IPSec topology
   */
  public static ValueGraph<IpsecPeerConfigId, IpsecSession> initIpsecTopology(
      Map<String, Configuration> configurations) {

    NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);
    Map<Ip, Set<IpsecPeerConfigId>> localIpIpsecPeerConfigIds = new HashMap<>();
    MutableValueGraph<IpsecPeerConfigId, IpsecSession> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();

    for (Configuration node : configurations.values()) {
      for (Entry<String, IpsecPeerConfig> entry : node.getIpsecPeerConfigs().entrySet()) {
        IpsecPeerConfig ipsecPeerConfig = entry.getValue();
        Set<IpsecPeerConfigId> ipsecPeerConfigIdsWithIp =
            localIpIpsecPeerConfigIds.computeIfAbsent(
                ipsecPeerConfig.getLocalAddress(), k -> new HashSet<>());
        IpsecPeerConfigId ipsecPeerConfigId =
            new IpsecPeerConfigId(entry.getKey(), node.getHostname());
        ipsecPeerConfigIdsWithIp.add(ipsecPeerConfigId);
        graph.addNode(ipsecPeerConfigId);
      }
    }

    // populating the graph
    for (IpsecPeerConfigId ipsecPeerConfigId : graph.nodes()) {
      IpsecPeerConfig ipsecPeerConfig = networkConfigurations.getIpsecPeerConfig(ipsecPeerConfigId);
      if (ipsecPeerConfig == null || ipsecPeerConfig instanceof IpsecDynamicPeerConfig) {
        continue;
      }
      // IPSec peer should be static
      IpsecStaticPeerConfig ipsecStaticPeerConfig = (IpsecStaticPeerConfig) ipsecPeerConfig;

      if (ipsecStaticPeerConfig.getDestinationAddress() == null) {
        continue;
      }
      Configuration initiatorOwner = configurations.get(ipsecPeerConfigId.getHostName());
      Set<IpsecPeerConfigId> candidateIpsecPeerConfigIds =
          localIpIpsecPeerConfigIds.get(ipsecStaticPeerConfig.getDestinationAddress());
      if (candidateIpsecPeerConfigIds == null) {
        continue;
      }
      for (IpsecPeerConfigId candidateIpsecPeerConfigId : candidateIpsecPeerConfigIds) {

        IpsecPeerConfig candidateIpsecPeer =
            networkConfigurations.getIpsecPeerConfig(candidateIpsecPeerConfigId);
        if (candidateIpsecPeer == null) {
          continue;
        }
        // skip if an IPSec peer is a crypto map based vpn and other is a tunnel interface based vpn
        if (ipsecStaticPeerConfig.getTunnelInterface() == null
            ^ candidateIpsecPeer.getTunnelInterface() == null) {
          continue;
        }
        Configuration candidateOwner = configurations.get(candidateIpsecPeerConfigId.getHostName());

        IpsecSession ipsecSession =
            getIpsecSession(
                initiatorOwner, candidateOwner, ipsecStaticPeerConfig, candidateIpsecPeer);

        graph.putEdgeValue(ipsecPeerConfigId, candidateIpsecPeerConfigId, ipsecSession);
      }
    }

    return ImmutableValueGraph.copyOf(graph);
  }

  /**
   * Gets the {@link IpsecSession} between two {@link IpsecPeerConfig}s where the initiator should
   * always be an {@link IpsecStaticPeerConfig}. Returned {@link IpsecSession} object will have
   * respective fields for IKE P1 proposals, IKE P1 keys and IPSec P2 proposals populated depending
   * on the negotiation.
   */
  @Nonnull
  private static IpsecSession getIpsecSession(
      Configuration initiatorOwner,
      Configuration peerOwner,
      IpsecStaticPeerConfig initiator,
      IpsecPeerConfig candidatePeer) {
    IpsecSession.Builder ipsecSessionBuilder = IpsecSession.builder();

    negotiateIkeP1(initiatorOwner, peerOwner, initiator, candidatePeer, ipsecSessionBuilder);

    if (ipsecSessionBuilder.getNegotiatedIkeP1Proposal() == null
        || ipsecSessionBuilder.getNegotiatedIkeP1Key() == null) {
      return ipsecSessionBuilder.build();
    }

    negotiateIpsecP2(initiatorOwner, peerOwner, initiator, candidatePeer, ipsecSessionBuilder);

    return ipsecSessionBuilder.build();
  }

  /**
   * Populates the negotiated {@link IkePhase1Proposal and {@link IkePhase1Key }} between two {@link
   * IpsecPeerConfig}s where the initiator should always be {@link IpsecStaticPeerConfig}, modeled
   * approximately around https://tools.ietf.org/html/rfc2409
   */
  private static void negotiateIkeP1(
      Configuration initiatorOwner,
      Configuration responderOwner,
      IpsecStaticPeerConfig initiator,
      IpsecPeerConfig responder,
      IpsecSession.Builder ipsecSessionBuilder) {
    IkePhase1Policy initiatorIkePhase1Policy =
        initiator.getIkePhase1Policy() == null
            ? null
            : initiatorOwner.getIkePhase1Policies().get(initiator.getIkePhase1Policy());

    IkePhase1Policy responderIkeP1Policy;
    if (responder instanceof IpsecStaticPeerConfig) {
      IpsecStaticPeerConfig ipsecStaticPeerConfig = (IpsecStaticPeerConfig) responder;
      responderIkeP1Policy =
          ipsecStaticPeerConfig.getIkePhase1Policy() == null
              ? null
              : responderOwner
                  .getIkePhase1Policies()
                  .get(ipsecStaticPeerConfig.getIkePhase1Policy());
    } else {
      responderIkeP1Policy = getMatchingDynamicIkeP1Policy(initiator, responder, responderOwner);
    }

    ipsecSessionBuilder.setInitiatorIkeP1Policy(initiatorIkePhase1Policy);
    ipsecSessionBuilder.setResponderIkeP1Policy(responderIkeP1Policy);

    // if initiator or responder don't have an IKE P1 policy for each other, no negotiation can
    // happen
    if (initiatorIkePhase1Policy == null || responderIkeP1Policy == null) {
      return;
    }

    IkePhase1Proposal negotiatedIkePhase1Proposal =
        getMatchingIkeP1Proposal(
            initiatorOwner,
            responderOwner,
            initiatorIkePhase1Policy.getIkePhase1Proposals(),
            responderIkeP1Policy.getIkePhase1Proposals());

    ipsecSessionBuilder.setNegotiatedIkeP1Proposal(negotiatedIkePhase1Proposal);

    // negotiating the IKE phase 1 key
    if (negotiatedIkePhase1Proposal != null) {
      IkePhase1Key initiatorPhase1Key = initiatorIkePhase1Policy.getIkePhase1Key();
      IkePhase1Key responderPhase1Key = responderIkeP1Policy.getIkePhase1Key();
      if (initiatorPhase1Key == null || responderPhase1Key == null) {
        return;
      }
      if (initiatorPhase1Key.getKeyType().equals(responderPhase1Key.getKeyType())
          && initiatorPhase1Key.getKeyHash().equals(responderPhase1Key.getKeyHash())) {
        ipsecSessionBuilder.setNegotiatedIkeP1Key(initiatorIkePhase1Policy.getIkePhase1Key());
      }
    }
  }

  /**
   * Populates the negotiated {@link IpsecPhase2Proposal} between two {@link IpsecPeerConfig}s where
   * the initiator should always be {@link IpsecStaticPeerConfig}, modeled approximately around
   * https://tools.ietf.org/html/rfc2409
   */
  private static void negotiateIpsecP2(
      Configuration initiatorOwner,
      Configuration responderOwner,
      IpsecStaticPeerConfig initiator,
      IpsecPeerConfig responder,
      IpsecSession.Builder ipsecSessionBuilder) {
    IpsecPhase2Policy initiatorIpsecP2policy =
        initiator.getIpsecPolicy() == null
            ? null
            : initiatorOwner.getIpsecPhase2Policies().get(initiator.getIpsecPolicy());
    IpsecPhase2Policy responderIpsecP2Policy =
        responder.getIpsecPolicy() == null
            ? null
            : responderOwner.getIpsecPhase2Policies().get(responder.getIpsecPolicy());

    ipsecSessionBuilder.setInitiatorIpsecP2Policy(initiatorIpsecP2policy);
    ipsecSessionBuilder.setResponderIpsecP2Policy(responderIpsecP2Policy);

    // if initiator or responder don't have an Ipsec phase 2 policy, further negotiation can't
    // happen
    if (initiatorIpsecP2policy == null || responderIpsecP2Policy == null) {
      return;
    }

    IpsecPhase2Proposal negotiatedIpsecPhase2Proposal =
        getMatchingIpsecP2Proposal(
            initiatorOwner,
            responderOwner,
            initiatorIpsecP2policy.getProposals(),
            responderIpsecP2Policy.getProposals());

    ipsecSessionBuilder.setNegotiatedIpsecP2Proposal(negotiatedIpsecPhase2Proposal);
  }

  /**
   * Returns a negotiated {@link IkePhase1Proposal} using a {@link List} of initiator's {@link
   * IkePhase1Proposal}s and responder's {@link IkePhase1Proposal}s, returns null if no compatible
   * {@link IkePhase1Proposal} could be found
   */
  @Nullable
  private static IkePhase1Proposal getMatchingIkeP1Proposal(
      Configuration initiatorOwner,
      Configuration responderOwner,
      List<String> initiatorProposals,
      List<String> responderProposals) {
    List<IkePhase1Proposal> initiatorIkeProposalList =
        initiatorProposals
            .stream()
            .map(ikeProposalName -> initiatorOwner.getIkePhase1Proposals().get(ikeProposalName))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    List<IkePhase1Proposal> responderProposalList =
        responderProposals
            .stream()
            .map(ikeProposalName -> responderOwner.getIkePhase1Proposals().get(ikeProposalName))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    for (IkePhase1Proposal initiatorProposal : initiatorIkeProposalList) {
      for (IkePhase1Proposal responderProposal : responderProposalList) {
        if (initiatorProposal.isCompatibleWith(responderProposal)) {
          IkePhase1Proposal negotiatedProposal =
              new IkePhase1Proposal("~NEGOTIATED_IKE_P1_PROPOSAL~");
          negotiatedProposal.setHashingAlgorithm(initiatorProposal.getHashingAlgorithm());
          negotiatedProposal.setEncryptionAlgorithm(initiatorProposal.getEncryptionAlgorithm());
          negotiatedProposal.setDiffieHellmanGroup(initiatorProposal.getDiffieHellmanGroup());
          negotiatedProposal.setAuthenticationMethod(initiatorProposal.getAuthenticationMethod());
          negotiatedProposal.setLifetimeSeconds(
              Math.min(
                  initiatorProposal.getLifetimeSeconds(), responderProposal.getLifetimeSeconds()));
          return negotiatedProposal;
        }
      }
    }

    return null;
  }

  /**
   * Returns a negotiated {@link IpsecPhase2Proposal} using a {@link List} of initiator's {@link
   * IpsecPhase2Proposal}s and responder's {@link IpsecPhase2Proposal}s, returns null if no
   * compatible {@link IpsecPhase2Proposal} could be found
   */
  @Nullable
  private static IpsecPhase2Proposal getMatchingIpsecP2Proposal(
      Configuration initiatorOwner,
      Configuration responderOwner,
      List<String> initiatorProposals,
      List<String> responderProposals) {
    List<IpsecPhase2Proposal> initiatorIpsecProposalList =
        initiatorProposals
            .stream()
            .map(
                ipsecP2ProposalName ->
                    initiatorOwner.getIpsecPhase2Proposals().get(ipsecP2ProposalName))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    List<IpsecPhase2Proposal> responderProposalList =
        responderProposals
            .stream()
            .map(
                ipsecP2ProposalName ->
                    responderOwner.getIpsecPhase2Proposals().get(ipsecP2ProposalName))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    for (IpsecPhase2Proposal initiatorProposal : initiatorIpsecProposalList) {
      for (IpsecPhase2Proposal responderProposal : responderProposalList) {
        if (initiatorProposal.equals(responderProposal)) {
          return initiatorProposal;
        }
      }
    }

    return null;
  }

  /**
   * Searches and returns the {@link IkePhase1Policy} which can be used for peering with the
   * initiator on the responder. Returns null if no such {@link IkePhase1Policy} could be found.
   */
  @Nullable
  private static IkePhase1Policy getMatchingDynamicIkeP1Policy(
      IpsecStaticPeerConfig initiator, IpsecPeerConfig responder, Configuration responderOwner) {
    List<IkePhase1Policy> dynamicIkePhase1Policies =
        ((IpsecDynamicPeerConfig) responder)
            .getIkePhase1Poliies()
            .stream()
            .map(ikePhase1Policy -> responderOwner.getIkePhase1Policies().get(ikePhase1Policy))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    return dynamicIkePhase1Policies
        .stream()
        .filter(
            dynamicIkeP1Policy ->
                dynamicIkeP1Policy
                    .getRemoteIdentity()
                    .containsIp(initiator.getLocalAddress(), ImmutableMap.of()))
        .findFirst()
        .orElse(null);
  }
}
