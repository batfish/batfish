package org.batfish.representation.cisco_ftd;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecDynamicPeerConfig;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.visitors.HeaderSpaceConverter;

/**
 * Utilities for converting FTD-specific models to vendor-independent models.
 */
public final class FtdConversions {

    public static @Nonnull List<IkePhase1Proposal> toIkePhase1Proposals(FtdIkev2Policy policy) {
        List<IkePhase1Proposal> proposals = new ArrayList<>();
        int count = 0;
        for (org.batfish.datamodel.EncryptionAlgorithm enc : policy.getEncryptionAlgorithms()) {
            for (org.batfish.datamodel.IkeHashingAlgorithm integrity : policy.getIntegrityAlgorithms()) {
                for (org.batfish.datamodel.DiffieHellmanGroup dh : policy.getDhGroups()) {
                    String name = String.format("~IKEV2_PROPOSAL:%d:%d~", policy.getPriority(), count++);
                    IkePhase1Proposal proposal = new IkePhase1Proposal(name);
                    proposal.setEncryptionAlgorithm(enc);
                    // Using integrity as the hashing algorithm for phase 1 proposal
                    proposal.setHashingAlgorithm(integrity);
                    proposal.setDiffieHellmanGroup(dh);
                    proposal.setLifetimeSeconds(policy.getLifetimeSeconds());
                    // FTD IKEv2 usually uses PRE_SHARED_KEYS for L2L VPN
                    proposal.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
                    proposals.add(proposal);
                }
            }
        }
        return proposals;
    }

    public static @Nonnull IpsecPhase2Proposal toIpsecPhase2Proposal(FtdIpsecTransformSet transformSet) {
        IpsecPhase2Proposal proposal = new IpsecPhase2Proposal();
        proposal.setEncryptionAlgorithm(transformSet.getEspEncryption());
        proposal.setAuthenticationAlgorithm(transformSet.getEspAuthentication());
        proposal.setIpsecEncapsulationMode(transformSet.getMode());
        // FTD default protocols are ESP
        proposal.setProtocols(ImmutableSortedSet.of(org.batfish.datamodel.IpsecProtocol.ESP));
        return proposal;
    }

    public static @Nonnull IpsecPhase2Policy toIpsecPhase2Policy(FtdIpsecProfile profile) {
        IpsecPhase2Policy policy = new IpsecPhase2Policy();
        policy.setPfsKeyGroup(profile.getPfsGroup());
        policy.setProposals(ImmutableList.copyOf(profile.getTransformSets()));
        return policy;
    }

    public static @Nonnull IpsecPhase2Policy toIpsecPhase2Policy(FtdCryptoMapEntry entry) {
        IpsecPhase2Policy policy = new IpsecPhase2Policy();
        policy.setPfsKeyGroup(entry.getPfsKeyGroup());
        policy.setProposals(ImmutableList.copyOf(entry.getTransforms()));
        return policy;
    }

    public static Map<String, IpsecPeerConfig> toIpsecPeerConfigs(
            Configuration c,
            FtdCryptoMapEntry entry,
            String cryptoMapName,
            String ipsecPhase2Policy,
            Warnings w) {

        List<org.batfish.datamodel.Interface> referencingInterfaces = c.getAllInterfaces().values().stream()
                .filter(iface -> Objects.equals(iface.getCryptoMap(), cryptoMapName))
                .collect(Collectors.toList());

        ImmutableSortedMap.Builder<String, IpsecPeerConfig> configs = ImmutableSortedMap.naturalOrder();

        for (org.batfish.datamodel.Interface iface : referencingInterfaces) {
            ConcreteInterfaceAddress addr = iface.getConcreteAddress();
            if (addr == null) {
                w.redFlagf("Interface %s has crypto map %s but no IP address", iface.getName(), cryptoMapName);
                continue;
            }

            String peerName = String.format("~IPSEC_PEER_CONFIG:%s:%d:%s~",
                    cryptoMapName, entry.getSequenceNumber(), iface.getName());

            IpsecPeerConfig.Builder<?, ?> builder;
            if (entry.getPeer() != null) {
                // Static peer
                builder = IpsecStaticPeerConfig.builder()
                        .setDestinationAddress(entry.getPeer());
            } else {
                // Dynamic peer (template)
                builder = IpsecDynamicPeerConfig.builder();
            }

            builder.setSourceInterface(iface.getName())
                    .setLocalAddress(addr.getIp())
                    .setIpsecPolicy(ipsecPhase2Policy);

            // Access List symmetry handling
            if (entry.getAccessList() != null) {
                IpAccessList acl = c.getIpAccessLists().get(entry.getAccessList());
                if (acl != null) {
                    IpAccessList symmetricAcl = createAclWithSymmetricalLines(acl);
                    if (symmetricAcl != null) {
                        builder.setPolicyAccessList(symmetricAcl);
                    }
                }
            }

            configs.put(peerName, builder.build());
        }

        return configs.build();
    }

    @VisibleForTesting
    static @Nullable IpAccessList createAclWithSymmetricalLines(IpAccessList ipAccessList) {
        List<AclLine> aclLines = new ArrayList<>(ipAccessList.getLines());
        for (AclLine line : ipAccessList.getLines()) {
            if (!(line instanceof ExprAclLine)) {
                return null;
            }
            ExprAclLine exprAclLine = (ExprAclLine) line;
            HeaderSpace originalHeaderSpace = HeaderSpaceConverter.convert(exprAclLine.getMatchCondition());

            HeaderSpace reversedHeaderSpace = originalHeaderSpace.toBuilder()
                    .setSrcIps(originalHeaderSpace.getDstIps())
                    .setSrcPorts(originalHeaderSpace.getDstPorts())
                    .setDstIps(originalHeaderSpace.getSrcIps())
                    .setDstPorts(originalHeaderSpace.getSrcPorts())
                    .build();

            aclLines.add(ExprAclLine.builder()
                    .setMatchCondition(new MatchHeaderSpace(reversedHeaderSpace))
                    .setAction(exprAclLine.getAction())
                    .build());
        }
        return IpAccessList.builder()
                .setName(ipAccessList.getName() + "~SYMMETRICAL")
                .setLines(aclLines)
                .build();
    }

    public static IkePhase1Key toIkePhase1Key(FtdTunnelGroup tg) {
        IkePhase1Key key = new IkePhase1Key();
        key.setKeyHash(tg.getPresharedKey());
        key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
        // Map the tunnel group name (often the peer IP for L2L) to the remote identity
        try {
            Ip peerIp = Ip.parse(tg.getName());
            key.setRemoteIdentity(peerIp.toIpSpace());
        } catch (IllegalArgumentException e) {
            // Not an IP, might be a name
        }
        return key;
    }

    private FtdConversions() {
    }
}
