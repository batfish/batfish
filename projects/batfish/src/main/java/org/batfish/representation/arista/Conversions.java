package org.batfish.representation.arista;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.IkePhase1Policy.PREFIX_ISAKMP_KEY;
import static org.batfish.datamodel.IkePhase1Policy.PREFIX_RSA_PUB;
import static org.batfish.datamodel.Interface.INVALID_LOCAL_INTERFACE;
import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST;
import static org.batfish.datamodel.ospf.OspfNetworkType.POINT_TO_POINT;
import static org.batfish.representation.arista.AristaConfiguration.aclLineStructureName;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpsecDynamicPeerConfig;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.isis.IsisLevelSettings;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.visitors.HeaderSpaceConverter;
import org.batfish.representation.arista.DistributeList.DistributeListFilterType;
import org.batfish.representation.arista.StaticRoute.NextHop;
import org.batfish.vendor.VendorStructureId;

/** Utilities that convert Cisco-specific representations to vendor-independent model. */
@ParametersAreNonnullByDefault
public class Conversions {

  // Defaults from
  // https://www.cisco.com/c/en/us/support/docs/ip/open-shortest-path-first-ospf/13689-17.html
  static int DEFAULT_OSPF_HELLO_INTERVAL_P2P_AND_BROADCAST = 10;

  static int DEFAULT_OSPF_HELLO_INTERVAL = 30;

  // Default dead interval is hello interval times 4
  static int OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER = 4;

  static int DEFAULT_OSPF_DEAD_INTERVAL_P2P_AND_BROADCAST =
      OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * DEFAULT_OSPF_HELLO_INTERVAL_P2P_AND_BROADCAST;

  static int DEFAULT_OSPF_DEAD_INTERVAL =
      OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * DEFAULT_OSPF_HELLO_INTERVAL;
  private static final int DEFAULT_STATIC_ROUTE_DISTANCE = 1;

  static Ip getHighestIp(Map<String, Interface> allInterfaces) {
    Map<String, Interface> interfacesToCheck;
    Map<String, Interface> loopbackInterfaces = new HashMap<>();
    for (Entry<String, Interface> e : allInterfaces.entrySet()) {
      String ifaceName = e.getKey();
      Interface iface = e.getValue();
      if (ifaceName.toLowerCase().startsWith("loopback")
          && !iface.getShutdown()
          && iface.getAddress() != null) {
        loopbackInterfaces.put(ifaceName, iface);
      }
    }
    if (loopbackInterfaces.isEmpty()) {
      interfacesToCheck = allInterfaces;
    } else {
      interfacesToCheck = loopbackInterfaces;
    }
    Ip highestIp = Ip.ZERO;
    for (Interface iface : interfacesToCheck.values()) {
      if (iface.getShutdown()) {
        continue;
      }
      for (ConcreteInterfaceAddress address : iface.getAllAddresses()) {
        Ip ip = address.getIp();
        if (highestIp.asLong() < ip.asLong()) {
          highestIp = ip;
        }
      }
    }
    return highestIp;
  }

  /**
   * Converts a {@link CryptoMapEntry} to an {@link IpsecPhase2Policy} and a list of {@link
   * IpsecPeerConfig}
   */
  private static void convertCryptoMapEntry(
      Configuration c,
      CryptoMapEntry cryptoMapEntry,
      String cryptoMapNameSeqNumber,
      String cryptoMapName,
      Warnings w) {
    // skipping incomplete static or dynamic crypto maps
    if (!cryptoMapEntry.getDynamic()) {
      if (cryptoMapEntry.getAccessList() == null || cryptoMapEntry.getPeer() == null) {
        return;
      }
    } else {
      if (cryptoMapEntry.getAccessList() == null) {
        return;
      }
    }

    IpsecPhase2Policy ipsecPhase2Policy = toIpsecPhase2Policy(cryptoMapEntry);
    String ipsecPhase2PolicyName =
        String.format("~IPSEC_PHASE2_POLICY:%s~", cryptoMapNameSeqNumber);

    // add IPSec phase 2 policies to existing ones
    ImmutableSortedMap.Builder<String, IpsecPhase2Policy> ipsecPhase2PolicyBuilder =
        ImmutableSortedMap.naturalOrder();
    c.setIpsecPhase2Policies(
        ipsecPhase2PolicyBuilder
            .putAll(c.getIpsecPhase2Policies())
            .put(ipsecPhase2PolicyName, ipsecPhase2Policy)
            .build());

    ImmutableSortedMap.Builder<String, IpsecPeerConfig> ipsecPeerConfigsBuilder =
        ImmutableSortedMap.naturalOrder();
    c.setIpsecPeerConfigs(
        ipsecPeerConfigsBuilder
            .putAll(c.getIpsecPeerConfigs())
            .putAll(
                toIpsecPeerConfigs(
                    c,
                    cryptoMapEntry,
                    cryptoMapNameSeqNumber,
                    cryptoMapName,
                    ipsecPhase2PolicyName,
                    w))
            .build());
  }

  /**
   * Converts each crypto map entry in all crypto map sets to {@link IpsecPhase2Policy} and {@link
   * IpsecPeerConfig}s
   */
  static void convertCryptoMapSet(
      Configuration c,
      CryptoMapSet ciscoCryptoMapSet,
      Map<String, CryptoMapSet> cryptoMapSets,
      Warnings w) {
    if (ciscoCryptoMapSet.getDynamic()) {
      return;
    }
    for (CryptoMapEntry cryptoMapEntry : ciscoCryptoMapSet.getCryptoMapEntries()) {
      String nameSeqNum =
          String.format("%s:%s", cryptoMapEntry.getName(), cryptoMapEntry.getSequenceNumber());
      if (cryptoMapEntry.getReferredDynamicMapSet() != null) {
        CryptoMapSet dynamicCryptoMapSet =
            cryptoMapSets.get(cryptoMapEntry.getReferredDynamicMapSet());
        if (dynamicCryptoMapSet != null && dynamicCryptoMapSet.getDynamic()) {
          // convert all entries of the referred dynamic crypto map
          dynamicCryptoMapSet
              .getCryptoMapEntries()
              .forEach(
                  cryptoMap ->
                      convertCryptoMapEntry(
                          c,
                          cryptoMap,
                          String.format("%s:%s", nameSeqNum, cryptoMap.getSequenceNumber()),
                          cryptoMapEntry.getName(),
                          w));
        }
      } else {
        convertCryptoMapEntry(c, cryptoMapEntry, nameSeqNum, cryptoMapEntry.getName(), w);
      }
    }
  }

  /**
   * Computes a mapping of primary {@link Ip}s to the names of interfaces owning them. Filters out
   * the interfaces having no primary {@link ConcreteInterfaceAddress}
   */
  private static Map<Ip, String> computeIpToIfaceNameMap(Map<String, Interface> interfaces) {
    Map<Ip, String> ipToIfaceNameMap = new HashMap<>();
    for (Entry<String, Interface> interfaceNameToInterface : interfaces.entrySet()) {
      interfaceNameToInterface
          .getValue()
          .getAllAddresses()
          .forEach(
              interfaceAddress -> {
                ipToIfaceNameMap.put(interfaceAddress.getIp(), interfaceNameToInterface.getKey());
              });
    }
    return ipToIfaceNameMap;
  }

  /** Resolves the interface names of the addresses used as local addresses of {@link Keyring} */
  static void resolveKeyringIfaceNames(
      Map<String, Interface> interfaces, Map<String, Keyring> keyrings) {
    Map<Ip, String> iptoIfaceName = computeIpToIfaceNameMap(interfaces);

    // setting empty string as interface name if cannot find the IP
    keyrings.values().stream()
        .filter(keyring -> keyring.getLocalAddress() != null)
        .forEach(
            keyring ->
                keyring.setLocalInterfaceName(
                    firstNonNull(
                        iptoIfaceName.get(keyring.getLocalAddress()), INVALID_LOCAL_INTERFACE)));
  }

  /**
   * Resolves the interface names of the addresses used as local addresses of {@link IsakmpProfile}
   */
  static void resolveIsakmpProfileIfaceNames(
      Map<String, Interface> interfaces, Map<String, IsakmpProfile> isakpProfiles) {
    Map<Ip, String> iptoIfaceName = computeIpToIfaceNameMap(interfaces);

    isakpProfiles.values().stream()
        .filter(isakmpProfile -> isakmpProfile.getLocalAddress() != null)
        .forEach(
            isakmpProfile ->
                isakmpProfile.setLocalInterfaceName(
                    firstNonNull(
                        iptoIfaceName.get(isakmpProfile.getLocalAddress()),
                        INVALID_LOCAL_INTERFACE)));
  }

  /** Resolves the interface names of the addresses used as source addresses in {@link Tunnel}s */
  static void resolveTunnelIfaceNames(Map<String, Interface> interfaces) {
    Map<Ip, String> iptoIfaceName = computeIpToIfaceNameMap(interfaces);

    for (Interface iface : interfaces.values()) {
      Tunnel tunnel = iface.getTunnel();
      // resolve if tunnel's source interface name is not set
      if (tunnel != null
          && UNSET_LOCAL_INTERFACE.equals(tunnel.getSourceInterfaceName())
          && tunnel.getSourceAddress() != null) {
        tunnel.setSourceInterfaceName(
            firstNonNull(iptoIfaceName.get(tunnel.getSourceAddress()), INVALID_LOCAL_INTERFACE));
      }
    }
  }

  static AsPathAccessList toAsPathAccessList(IpAsPathAccessList pathList) {
    List<AsPathAccessListLine> lines =
        pathList.getLines().stream()
            .map(IpAsPathAccessListLine::toAsPathAccessListLine)
            .collect(ImmutableList.toImmutableList());
    return new AsPathAccessList(pathList.getName(), lines);
  }

  static IkePhase1Key toIkePhase1Key(Keyring keyring) {
    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash(keyring.getKey());
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
    ikePhase1Key.setLocalInterface(keyring.getLocalInterfaceName());
    if (keyring.getRemoteIdentity() != null) {
      ikePhase1Key.setRemoteIdentity(keyring.getRemoteIdentity().toIpSpace());
    }
    return ikePhase1Key;
  }

  static IkePhase1Key toIkePhase1Key(@Nonnull NamedRsaPubKey rsaPubKey) {
    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash(rsaPubKey.getKey());
    ikePhase1Key.setKeyType(IkeKeyType.RSA_PUB_KEY);
    if (rsaPubKey.getAddress() != null) {
      ikePhase1Key.setRemoteIdentity(rsaPubKey.getAddress().toIpSpace());
    }
    return ikePhase1Key;
  }

  static IkePhase1Key toIkePhase1Key(@Nonnull IsakmpKey isakmpKey) {
    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash(isakmpKey.getKey());
    ikePhase1Key.setKeyType(isakmpKey.getIkeKeyType());
    ikePhase1Key.setRemoteIdentity(isakmpKey.getAddress());
    return ikePhase1Key;
  }

  static IkePhase1Policy toIkePhase1Policy(
      @Nonnull NamedRsaPubKey rsaPubKey,
      @Nonnull AristaConfiguration oldConfig,
      @Nonnull IkePhase1Key ikePhase1KeyFromRsaPubKey) {
    IkePhase1Policy ikePhase1Policy = new IkePhase1Policy(getRsaPubKeyGeneratedName(rsaPubKey));

    ikePhase1Policy.setIkePhase1Proposals(
        oldConfig.getIsakmpPolicies().values().stream()
            .filter(
                isakmpPolicy ->
                    isakmpPolicy.getAuthenticationMethod()
                        == IkeAuthenticationMethod.RSA_ENCRYPTED_NONCES)
            .map(isakmpPolicy -> isakmpPolicy.getName().toString())
            .collect(ImmutableList.toImmutableList()));
    if (rsaPubKey.getAddress() != null) {
      ikePhase1Policy.setRemoteIdentity(rsaPubKey.getAddress().toIpSpace());
    }
    ikePhase1Policy.setIkePhase1Key(ikePhase1KeyFromRsaPubKey);
    // RSA pub key is not per interface so unsetting local interface
    ikePhase1Policy.setLocalInterface(UNSET_LOCAL_INTERFACE);
    return ikePhase1Policy;
  }

  static IkePhase1Policy toIkePhase1Policy(
      @Nonnull IsakmpKey isakmpKey,
      @Nonnull AristaConfiguration oldConfig,
      @Nonnull IkePhase1Key ikePhase1KeyFromIsakmpKey) {
    IkePhase1Policy ikePhase1Policy = new IkePhase1Policy(getIsakmpKeyGeneratedName(isakmpKey));

    ikePhase1Policy.setIkePhase1Proposals(
        oldConfig.getIsakmpPolicies().values().stream()
            .filter(
                isakmpPolicy ->
                    isakmpPolicy.getAuthenticationMethod()
                        == IkeAuthenticationMethod.PRE_SHARED_KEYS)
            .map(isakmpPolicy -> isakmpPolicy.getName().toString())
            .collect(ImmutableList.toImmutableList()));
    ikePhase1Policy.setRemoteIdentity(isakmpKey.getAddress());

    ikePhase1Policy.setIkePhase1Key(ikePhase1KeyFromIsakmpKey);
    // ISAKMP key is not per interface so local interface will not be set
    ikePhase1Policy.setLocalInterface(UNSET_LOCAL_INTERFACE);
    return ikePhase1Policy;
  }

  static String getRsaPubKeyGeneratedName(NamedRsaPubKey namedRsaPubKey) {
    return String.format("~%s_%s~", PREFIX_RSA_PUB, namedRsaPubKey.getName());
  }

  static String getIsakmpKeyGeneratedName(IsakmpKey isakmpKey) {
    return String.format("~%s_%s~", PREFIX_ISAKMP_KEY, isakmpKey.getAddress());
  }

  static IkePhase1Policy toIkePhase1Policy(
      IsakmpProfile isakmpProfile,
      AristaConfiguration oldConfig,
      Configuration config,
      Warnings w) {
    IkePhase1Policy ikePhase1Policy = new IkePhase1Policy(isakmpProfile.getName());

    ImmutableList.Builder<String> ikePhase1ProposalBuilder = ImmutableList.builder();
    for (Entry<Integer, IsakmpPolicy> entry : oldConfig.getIsakmpPolicies().entrySet()) {
      ikePhase1ProposalBuilder.add(entry.getKey().toString());
    }

    ikePhase1Policy.setIkePhase1Proposals(ikePhase1ProposalBuilder.build());
    ikePhase1Policy.setSelfIdentity(isakmpProfile.getSelfIdentity());
    if (isakmpProfile.getMatchIdentity() != null) {
      ikePhase1Policy.setRemoteIdentity(isakmpProfile.getMatchIdentity().toIpSpace());
    }

    ikePhase1Policy.setLocalInterface(isakmpProfile.getLocalInterfaceName());
    ikePhase1Policy.setIkePhase1Key(getMatchingPsk(isakmpProfile, w, config.getIkePhase1Keys()));
    return ikePhase1Policy;
  }

  /**
   * Gets the {@link IkePhase1Key} that can be used for the given {@link IsakmpProfile} based on
   * {@code remoteIdentity} and {@code localInterfaceName} present in the {@link IkePhase1Key}
   */
  static IkePhase1Key getMatchingPsk(
      IsakmpProfile isakmpProfile, Warnings w, Map<String, IkePhase1Key> ikePhase1Keys) {
    IkePhase1Key ikePhase1Key = null;
    String isakmpProfileName = isakmpProfile.getName();
    if (isakmpProfile.getLocalInterfaceName().equals(INVALID_LOCAL_INTERFACE)) {
      w.redFlag(
          String.format(
              "Invalid local address interface configured for ISAKMP profile %s",
              isakmpProfileName));
    } else if (isakmpProfile.getKeyring() == null) {
      w.redFlagf("Keyring not set for ISAKMP profile %s", isakmpProfileName);
    } else if (!ikePhase1Keys.containsKey(isakmpProfile.getKeyring())) {
      w.redFlag(
          String.format(
              "Cannot find keyring %s for ISAKMP profile %s",
              isakmpProfile.getKeyring(), isakmpProfileName));
    } else {
      IkePhase1Key tempIkePhase1Key = ikePhase1Keys.get(isakmpProfile.getKeyring());
      if (tempIkePhase1Key.getLocalInterface().equals(INVALID_LOCAL_INTERFACE)) {
        w.redFlag(
            String.format(
                "Invalid local address interface configured for keyring %s",
                isakmpProfile.getKeyring()));
      } else if (tempIkePhase1Key.match(
          isakmpProfile.getLocalInterfaceName(), isakmpProfile.getMatchIdentity())) {
        // found a matching keyring
        ikePhase1Key = tempIkePhase1Key;
      }
    }
    return ikePhase1Key;
  }

  static IkePhase1Proposal toIkePhase1Proposal(IsakmpPolicy isakmpPolicy) {
    IkePhase1Proposal ikePhase1Proposal = new IkePhase1Proposal(isakmpPolicy.getName().toString());
    ikePhase1Proposal.setDiffieHellmanGroup(isakmpPolicy.getDiffieHellmanGroup());
    ikePhase1Proposal.setAuthenticationMethod(isakmpPolicy.getAuthenticationMethod());
    ikePhase1Proposal.setEncryptionAlgorithm(isakmpPolicy.getEncryptionAlgorithm());
    ikePhase1Proposal.setLifetimeSeconds(isakmpPolicy.getLifetimeSeconds());
    ikePhase1Proposal.setHashingAlgorithm(isakmpPolicy.getHashAlgorithm());
    return ikePhase1Proposal;
  }

  static IpAccessList toIpAccessList(ExtendedAccessList eaList, String filename) {
    boolean isStandard = eaList.getParent() != null;
    AristaStructureType lineType =
        isStandard
            ? AristaStructureType.IP_ACCESS_LIST_STANDARD_LINE
            : AristaStructureType.IPV4_ACCESS_LIST_EXTENDED_LINE;
    List<AclLine> lines =
        eaList.getLines().stream()
            .map(
                l ->
                    toIpAccessListLine(l)
                        .setVendorStructureId(
                            new VendorStructureId(
                                filename,
                                lineType.getDescription(),
                                aclLineStructureName(eaList.getName(), l.getName())))
                        .build())
            .collect(ImmutableList.toImmutableList());
    String sourceType =
        eaList.getParent() != null
            ? AristaStructureType.IP_ACCESS_LIST_STANDARD.getDescription()
            : AristaStructureType.IPV4_ACCESS_LIST_EXTENDED.getDescription();
    String name = eaList.getName();
    return IpAccessList.builder()
        .setName(name)
        .setLines(lines)
        .setSourceName(name)
        .setSourceType(sourceType)
        .build();
  }

  /**
   * Converts a {@link Tunnel} to an {@link IpsecPeerConfig}, or empty optional if it can't be
   * converted
   */
  static Optional<IpsecPeerConfig> toIpsecPeerConfig(
      Tunnel tunnel,
      String tunnelIfaceName,
      AristaConfiguration oldConfig,
      Configuration newConfig,
      Warnings w) {
    Ip localAddress = tunnel.getSourceAddress();
    if (localAddress == null || !localAddress.valid()) {
      w.redFlag(
          String.format(
              "Cannot create IPsec peer on tunnel %s: cannot determine tunnel source address",
              tunnelIfaceName));
      return Optional.empty();
    }

    IpsecStaticPeerConfig.Builder ipsecStaticPeerConfigBuilder =
        IpsecStaticPeerConfig.builder()
            .setTunnelInterface(tunnelIfaceName)
            .setDestinationAddress(tunnel.getDestination())
            .setLocalAddress(localAddress)
            .setSourceInterface(tunnel.getSourceInterfaceName())
            .setIpsecPolicy(tunnel.getIpsecProfileName());

    IpsecProfile ipsecProfile = null;
    if (tunnel.getIpsecProfileName() != null) {
      ipsecProfile = oldConfig.getIpsecProfiles().get(tunnel.getIpsecProfileName());
    }

    if (ipsecProfile != null && ipsecProfile.getIsakmpProfile() != null) {
      ipsecStaticPeerConfigBuilder.setIkePhase1Policy(ipsecProfile.getIsakmpProfile());
    } else {
      ipsecStaticPeerConfigBuilder.setIkePhase1Policy(
          getIkePhase1Policy(
              newConfig.getIkePhase1Policies(),
              tunnel.getDestination(),
              tunnel.getSourceInterfaceName()));
    }

    return Optional.of(ipsecStaticPeerConfigBuilder.build());
  }

  /**
   * Converts a {@link CryptoMapEntry} to multiple {@link IpsecPeerConfig}(one per interface on
   * which crypto map is referred)
   */
  private static Map<String, IpsecPeerConfig> toIpsecPeerConfigs(
      Configuration c,
      CryptoMapEntry cryptoMapEntry,
      String cryptoMapNameSeqNumber,
      String cryptoMapName,
      String ipsecPhase2Policy,
      Warnings w) {

    List<org.batfish.datamodel.Interface> referencingInterfaces =
        c.getAllInterfaces().values().stream()
            .filter(iface -> Objects.equals(iface.getCryptoMap(), cryptoMapName))
            .collect(Collectors.toList());

    ImmutableSortedMap.Builder<String, IpsecPeerConfig> ipsecPeerConfigsBuilder =
        ImmutableSortedMap.naturalOrder();

    for (org.batfish.datamodel.Interface iface : referencingInterfaces) {
      // skipping interfaces with no ip-address
      if (iface.getConcreteAddress() == null) {
        w.redFlag(
            String.format(
                "Interface %s with declared crypto-map %s has no ip-address",
                iface.getName(), cryptoMapName));
        continue;
      }
      // add one IPSec peer config per interface for the crypto map entry
      ipsecPeerConfigsBuilder.put(
          String.format("~IPSEC_PEER_CONFIG:%s_%s~", cryptoMapNameSeqNumber, iface.getName()),
          toIpsecPeerConfig(c, cryptoMapEntry, iface, ipsecPhase2Policy, w));
    }
    return ipsecPeerConfigsBuilder.build();
  }

  private static IpsecPeerConfig toIpsecPeerConfig(
      Configuration c,
      CryptoMapEntry cryptoMapEntry,
      org.batfish.datamodel.Interface iface,
      String ipsecPhase2Policy,
      Warnings w) {

    IpsecPeerConfig.Builder<?, ?> newIpsecPeerConfigBuilder;

    String ikePhase1Policy = cryptoMapEntry.getIsakmpProfile();

    // static crypto maps
    if (cryptoMapEntry.getPeer() != null) {
      if (ikePhase1Policy == null) {
        ikePhase1Policy =
            getIkePhase1Policy(c.getIkePhase1Policies(), cryptoMapEntry.getPeer(), iface.getName());
      }
      newIpsecPeerConfigBuilder =
          IpsecStaticPeerConfig.builder()
              .setDestinationAddress(cryptoMapEntry.getPeer())
              .setIkePhase1Policy(ikePhase1Policy);
    } else {
      // dynamic crypto maps
      List<String> ikePhase1Policies;
      if (ikePhase1Policy != null) {
        ikePhase1Policies = ImmutableList.of(ikePhase1Policy);
      } else {
        ikePhase1Policies = getMatchingIKePhase1Policies(c.getIkePhase1Policies(), iface.getName());
      }

      newIpsecPeerConfigBuilder =
          IpsecDynamicPeerConfig.builder().setIkePhase1Policies(ikePhase1Policies);
    }

    newIpsecPeerConfigBuilder
        .setSourceInterface(iface.getName())
        .setIpsecPolicy(ipsecPhase2Policy)
        .setLocalAddress(iface.getConcreteAddress().getIp());

    setIpsecPeerConfigPolicyAccessList(c, cryptoMapEntry, newIpsecPeerConfigBuilder, w);

    return newIpsecPeerConfigBuilder.build();
  }

  private static void setIpsecPeerConfigPolicyAccessList(
      Configuration c,
      CryptoMapEntry cryptoMapEntry,
      IpsecPeerConfig.Builder<?, ?> ipsecPeerConfigBuilder,
      Warnings w) {
    if (cryptoMapEntry.getAccessList() != null) {
      IpAccessList cryptoAcl = c.getIpAccessLists().get(cryptoMapEntry.getAccessList());
      if (cryptoAcl != null) {
        IpAccessList symmetricCryptoAcl = createAclWithSymmetricalLines(cryptoAcl);
        if (symmetricCryptoAcl != null) {
          ipsecPeerConfigBuilder.setPolicyAccessList(symmetricCryptoAcl);
        } else {
          // log a warning if the ACL was not made symmetrical successfully
          w.redFlag(
              String.format(
                  "Cannot process the Access List for crypto map %s:%s",
                  cryptoMapEntry.getName(), cryptoMapEntry.getSequenceNumber()));
        }
      }
    }
  }

  /**
   * Returns a new symmetrical {@link IpAccessList} by adding mirror image {@link ExprAclLine}s to
   * the original {@link IpAccessList} or null if the conversion is not supported
   */
  @VisibleForTesting
  static @Nullable IpAccessList createAclWithSymmetricalLines(IpAccessList ipAccessList) {
    List<AclLine> aclLines = new ArrayList<>(ipAccessList.getLines());

    for (AclLine line : ipAccessList.getLines()) {
      // Does not support types of ACL line other than ExprAclLine
      if (!(line instanceof ExprAclLine)) {
        return null;
      }
      ExprAclLine exprAclLine = (ExprAclLine) line;
      HeaderSpace originalHeaderSpace =
          HeaderSpaceConverter.convert(exprAclLine.getMatchCondition());

      if (!originalHeaderSpace.equals(
          HeaderSpace.builder()
              .setSrcIps(originalHeaderSpace.getSrcIps())
              .setDstIps(originalHeaderSpace.getDstIps())
              .setSrcPorts(originalHeaderSpace.getSrcPorts())
              .setDstPorts(originalHeaderSpace.getDstPorts())
              .setIpProtocols(originalHeaderSpace.getIpProtocols())
              .setIcmpCodes(originalHeaderSpace.getIcmpCodes())
              .setTcpFlags(originalHeaderSpace.getTcpFlags())
              .build())) {
        //  not supported if the access list line contains any more fields
        return null;
      } else {
        HeaderSpace.Builder reversedHeaderSpaceBuilder = originalHeaderSpace.toBuilder();
        aclLines.add(
            ExprAclLine.builder()
                .setMatchCondition(
                    new MatchHeaderSpace(
                        reversedHeaderSpaceBuilder
                            .setSrcIps(originalHeaderSpace.getDstIps())
                            .setSrcPorts(originalHeaderSpace.getDstPorts())
                            .setDstIps(originalHeaderSpace.getSrcIps())
                            .setDstPorts(originalHeaderSpace.getSrcPorts())
                            .build()))
                .setAction(exprAclLine.getAction())
                .build());
      }
    }

    return IpAccessList.builder().setName(ipAccessList.getName()).setLines(aclLines).build();
  }

  /**
   * Returns the first {@link IkePhase1Policy} name matching {@code remoteAddress} and {@code
   * localInterface}, null is returned if no matching {@link IkePhase1Policy} could not be found
   */
  private static @Nullable String getIkePhase1Policy(
      Map<String, IkePhase1Policy> ikePhase1Policies, Ip remoteAddress, String localInterface) {
    for (Entry<String, IkePhase1Policy> e : ikePhase1Policies.entrySet()) {
      IkePhase1Policy ikePhase1Policy = e.getValue();
      String ikePhase1PolicyLocalInterface = ikePhase1Policy.getLocalInterface();
      if (ikePhase1Policy.getRemoteIdentity().containsIp(remoteAddress, ImmutableMap.of())
          && (UNSET_LOCAL_INTERFACE.equals(ikePhase1PolicyLocalInterface)
              || ikePhase1PolicyLocalInterface.equals(localInterface))) {
        return e.getKey();
      }
    }
    return null;
  }

  /** Returns all {@link IkePhase1Policy} names matching the {@code localInterface} */
  private static List<String> getMatchingIKePhase1Policies(
      Map<String, IkePhase1Policy> ikePhase1Policies, String localInterface) {
    List<String> filteredIkePhase1Policies = new ArrayList<>();
    for (Entry<String, IkePhase1Policy> e : ikePhase1Policies.entrySet()) {
      String ikePhase1PolicyLocalInterface = e.getValue().getLocalInterface();
      if ((UNSET_LOCAL_INTERFACE.equals(ikePhase1PolicyLocalInterface)
          || ikePhase1PolicyLocalInterface.equals(localInterface))) {
        filteredIkePhase1Policies.add(e.getKey());
      }
    }
    return filteredIkePhase1Policies;
  }

  static IpsecPhase2Proposal toIpsecPhase2Proposal(IpsecTransformSet ipsecTransformSet) {
    IpsecPhase2Proposal ipsecPhase2Proposal = new IpsecPhase2Proposal();
    ipsecPhase2Proposal.setAuthenticationAlgorithm(ipsecTransformSet.getAuthenticationAlgorithm());
    ipsecPhase2Proposal.setEncryptionAlgorithm(ipsecTransformSet.getEncryptionAlgorithm());
    ipsecPhase2Proposal.setProtocols(ipsecTransformSet.getProtocols());
    ipsecPhase2Proposal.setIpsecEncapsulationMode(ipsecTransformSet.getIpsecEncapsulationMode());

    return ipsecPhase2Proposal;
  }

  static IpsecPhase2Policy toIpsecPhase2Policy(IpsecProfile ipsecProfile) {
    IpsecPhase2Policy ipsecPhase2Policy = new IpsecPhase2Policy();
    ipsecPhase2Policy.setPfsKeyGroup(ipsecProfile.getPfsGroup());
    ipsecPhase2Policy.setProposals(ImmutableList.copyOf(ipsecProfile.getTransformSets()));

    return ipsecPhase2Policy;
  }

  static IpsecPhase2Policy toIpsecPhase2Policy(CryptoMapEntry cryptoMapEntry) {
    IpsecPhase2Policy ipsecPhase2Policy = new IpsecPhase2Policy();
    ipsecPhase2Policy.setProposals(ImmutableList.copyOf(cryptoMapEntry.getTransforms()));
    ipsecPhase2Policy.setPfsKeyGroup(cryptoMapEntry.getPfsKeyGroup());
    return ipsecPhase2Policy;
  }

  static org.batfish.datamodel.isis.IsisProcess toIsisProcess(
      IsisProcess proc, Configuration c, AristaConfiguration oldConfig) {
    org.batfish.datamodel.isis.IsisProcess.Builder newProcess =
        org.batfish.datamodel.isis.IsisProcess.builder();
    if (proc.getNetAddress() == null) {
      oldConfig.getWarnings().redFlag("Cannot create IS-IS process without specifying net-address");
      return null;
    }
    newProcess.setNetAddress(proc.getNetAddress());
    IsisLevelSettings settings = IsisLevelSettings.builder().build();
    switch (proc.getLevel()) {
      case LEVEL_1:
        newProcess.setLevel1(settings);
        break;
      case LEVEL_1_2:
        newProcess.setLevel1(settings);
        newProcess.setLevel2(settings);
        break;
      case LEVEL_2:
        newProcess.setLevel2(settings);
        break;
      default:
        throw new BatfishException("Unhandled IS-IS level.");
    }
    return newProcess.build();
  }

  static RouteFilterList toRouteFilterList(ExtendedAccessList eaList, String vendorConfigFilename) {
    List<RouteFilterLine> lines =
        eaList.getLines().stream()
            .map(Conversions::toRouteFilterLine)
            .collect(ImmutableList.toImmutableList());
    return new RouteFilterList(
        eaList.getName(),
        lines,
        new VendorStructureId(
            vendorConfigFilename,
            AristaStructureType.IPV4_ACCESS_LIST_EXTENDED.getDescription(),
            eaList.getName()));
  }

  static RouteFilterList toRouteFilterList(StandardAccessList saList, String vendorConfigFilename) {
    List<RouteFilterLine> lines =
        saList.getLines().values().stream()
            .filter(line -> line instanceof StandardAccessListActionLine)
            .map(line -> (StandardAccessListActionLine) line)
            .map(Conversions::toRouteFilterLine)
            .collect(ImmutableList.toImmutableList());
    return new RouteFilterList(
        saList.getName(),
        lines,
        new VendorStructureId(
            vendorConfigFilename,
            AristaStructureType.IP_ACCESS_LIST_STANDARD.getDescription(),
            saList.getName()));
  }

  static RouteFilterList toRouteFilterList(PrefixList list, String vendorConfigFilename) {
    List<RouteFilterLine> newLines =
        list.getLines().values().stream()
            .map(
                l ->
                    new RouteFilterLine(
                        l.getAction(), IpWildcard.create(l.getPrefix()), l.getLengthRange()))
            .collect(ImmutableList.toImmutableList());
    return new RouteFilterList(
        list.getName(),
        newLines,
        new VendorStructureId(
            vendorConfigFilename,
            AristaStructureType.PREFIX_LIST.getDescription(),
            list.getName()));
  }

  @VisibleForTesting
  static boolean sanityCheckDistributeList(
      @Nonnull DistributeList distributeList,
      @Nonnull Configuration c,
      @Nonnull AristaConfiguration oldConfig,
      String vrfName,
      String ospfProcessId) {
    if (distributeList.getFilterType() != DistributeListFilterType.PREFIX_LIST) {
      // only prefix-lists are supported in distribute-list
      oldConfig
          .getWarnings()
          .redFlag(
              String.format(
                  "OSPF process %s:%s in %s uses distribute-list of type %s, only prefix-lists are"
                      + " supported in dist-lists by Batfish",
                  vrfName, ospfProcessId, oldConfig.getHostname(), distributeList.getFilterType()));
      return false;
    } else if (!c.getRouteFilterLists().containsKey(distributeList.getFilterName())) {
      // if referred prefix-list is not defined, all prefixes will be allowed
      oldConfig
          .getWarnings()
          .redFlag(
              String.format(
                  "dist-list in OSPF process %s:%s uses a prefix-list which is not defined, this"
                      + " dist-list will allow everything",
                  vrfName, ospfProcessId));
      return false;
    }
    return true;
  }

  /**
   * Populates the {@link RoutingPolicy}s for inbound {@link DistributeList}s which use {@link
   * PrefixList} as the {@link DistributeList#getFilterType()}. {@link
   * DistributeListFilterType#ROUTE_MAP} and {@link DistributeListFilterType#ACCESS_LIST} are not
   * supported currently.
   *
   * @param ospfProcess {@link OspfProcess} for which {@link DistributeList}s are to be processed
   * @param c {@link Configuration} containing the Vendor Independent representation
   * @param vrf Id of the {@link Vrf} containing the {@link OspfProcess}
   * @param ospfProcessId {@link OspfProcess}'s Id
   */
  static void computeDistributeListPolicies(
      @Nonnull OspfProcess ospfProcess,
      @Nonnull org.batfish.datamodel.ospf.OspfProcess newOspfProcess,
      @Nonnull Configuration c,
      @Nonnull String vrf,
      @Nonnull String ospfProcessId,
      @Nonnull AristaConfiguration oldConfig,
      @Nonnull Warnings w) {
    DistributeList globalDistributeList = ospfProcess.getInboundGlobalDistributeList();

    BooleanExpr globalCondition = null;
    if (globalDistributeList != null
        && sanityCheckDistributeList(globalDistributeList, c, oldConfig, vrf, ospfProcessId)) {
      globalCondition =
          new MatchPrefixSet(
              DestinationNetwork.instance(),
              new NamedPrefixSet(globalDistributeList.getFilterName()));
    }

    Map<String, DistributeList> interfaceDistributeLists =
        ospfProcess.getInboundInterfaceDistributeLists();

    for (String ifaceName :
        newOspfProcess.getAreas().values().stream()
            .flatMap(a -> a.getInterfaces().stream())
            .collect(Collectors.toList())) {
      org.batfish.datamodel.Interface iface = c.getAllInterfaces(vrf).get(ifaceName);
      DistributeList ifaceDistributeList = interfaceDistributeLists.get(ifaceName);
      BooleanExpr ifaceCondition = null;
      if (ifaceDistributeList != null
          && sanityCheckDistributeList(ifaceDistributeList, c, oldConfig, vrf, ospfProcessId)) {
        ifaceCondition =
            new MatchPrefixSet(
                DestinationNetwork.instance(),
                new NamedPrefixSet(ifaceDistributeList.getFilterName()));
      }

      if (globalCondition == null && ifaceCondition == null) {
        // doing nothing if both global and interface conditions are empty
        continue;
      }

      String policyName = String.format("~OSPF_DIST_LIST_%s_%s_%s~", vrf, ospfProcessId, ifaceName);
      RoutingPolicy routingPolicy = new RoutingPolicy(policyName, c);
      routingPolicy
          .getStatements()
          .add(
              new If(
                  new Conjunction(
                      Stream.of(globalCondition, ifaceCondition)
                          .filter(Objects::nonNull)
                          .collect(ImmutableList.toImmutableList())),
                  ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                  ImmutableList.of(Statements.ExitReject.toStaticStatement())));
      c.getRoutingPolicies().put(routingPolicy.getName(), routingPolicy);
      OspfInterfaceSettings ospfSettings = iface.getOspfSettings();
      if (ospfSettings == null) {
        w.redFlag(
            String.format(
                "Cannot attach inbound distribute list policy '%s' to interface '%s' not"
                    + " configured for OSPF.",
                ifaceName, iface.getName()));
      } else {
        ospfSettings.setInboundDistributeListPolicy(policyName);
      }
    }
  }

  static org.batfish.datamodel.StaticRoute toStaticRoute(
      Configuration c, Prefix prefix, StaticRoute staticRoute, long tag) {
    NextHop nh = staticRoute.getNextHop();
    org.batfish.datamodel.route.nh.NextHop viNh;
    if (nh.getNullRouted()) {
      viNh = NextHopDiscard.instance();
    } else if (nh.getNextHopInterface() != null) {
      if (nh.getNextHopIp() == null) {
        viNh = NextHopInterface.of(nh.getNextHopInterface());
      } else {
        viNh = NextHopInterface.of(nh.getNextHopInterface(), nh.getNextHopIp());
      }
    } else {
      assert nh.getNextHopIp() != null;
      viNh = NextHopIp.of(nh.getNextHopIp());
    }
    return org.batfish.datamodel.StaticRoute.builder()
        .setNetwork(prefix)
        .setNextHop(viNh)
        .setAdministrativeCost(
            firstNonNull(staticRoute.getDistance(), DEFAULT_STATIC_ROUTE_DISTANCE))
        .setTag(tag)
        .build();
  }

  private static ExprAclLine.Builder toIpAccessListLine(ExtendedAccessListLine line) {
    IpSpace srcIpSpace = line.getSourceAddressSpecifier().toIpSpace();
    IpSpace dstIpSpace = line.getDestinationAddressSpecifier().toIpSpace();
    AclLineMatchExpr matchService = line.getServiceSpecifier().toAclLineMatchExpr();
    AclLineMatchExpr match;
    if (matchService instanceof MatchHeaderSpace) {
      match =
          new MatchHeaderSpace(
              ((MatchHeaderSpace) matchService)
                  .getHeaderspace().toBuilder()
                      .setSrcIps(srcIpSpace)
                      .setDstIps(dstIpSpace)
                      .build());
    } else {
      match = and(matchService, matchSrc(srcIpSpace), matchDst(dstIpSpace));
    }

    return ExprAclLine.builder()
        .setAction(line.getAction())
        .setMatchCondition(match)
        .setName(line.getName());
  }

  private static RouteFilterLine toRouteFilterLine(ExtendedAccessListLine fromLine) {
    LineAction action = fromLine.getAction();
    IpWildcard srcIpWildcard =
        ((WildcardAddressSpecifier) fromLine.getSourceAddressSpecifier()).getIpWildcard();
    Ip ip = srcIpWildcard.getIp();
    IpWildcard dstIpWildcard =
        ((WildcardAddressSpecifier) fromLine.getDestinationAddressSpecifier()).getIpWildcard();
    long minSubnet = dstIpWildcard.getIp().asLong();
    long maxSubnet = minSubnet | dstIpWildcard.getWildcardMask();
    int minPrefixLength = dstIpWildcard.getIp().numSubnetBits();
    int maxPrefixLength = Ip.create(maxSubnet).numSubnetBits();
    int statedPrefixLength = srcIpWildcard.getWildcardMaskAsIp().inverted().numSubnetBits();
    int prefixLength = Math.min(statedPrefixLength, minPrefixLength);
    Prefix prefix = Prefix.create(ip, prefixLength);
    return new RouteFilterLine(
        action, IpWildcard.create(prefix), new SubRange(minPrefixLength, maxPrefixLength));
  }

  /** Convert a standard access list line to a route filter list line */
  private static RouteFilterLine toRouteFilterLine(StandardAccessListActionLine fromLine) {
    // A standard ACL is simply a wildcard on the network address, and does not filter on the
    // prefix length at all (beyond the prefix length implied by the unmasked bits in wildcard).
    return new RouteFilterLine(
        fromLine.getAction(), fromLine.getSourceIps(), new SubRange(0, Prefix.MAX_PREFIX_LENGTH));
  }

  /**
   * Helper to infer dead interval from configured OSPF settings on an interface. Check explicitly
   * set dead interval, infer from hello interval, or infer from OSPF network type, in that order.
   * See https://www.cisco.com/c/en/us/support/docs/ip/open-shortest-path-first-ospf/13689-17.html
   * for more details.
   */
  @VisibleForTesting
  static int toOspfDeadInterval(
      Interface iface, @Nullable org.batfish.datamodel.ospf.OspfNetworkType networkType) {
    Integer deadInterval = iface.getOspfDeadInterval();
    if (deadInterval != null) {
      return deadInterval;
    }
    Integer helloInterval = iface.getOspfHelloInterval();
    if (helloInterval != null) {
      return OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * helloInterval;
    }
    if (networkType == POINT_TO_POINT || networkType == BROADCAST) {
      return DEFAULT_OSPF_DEAD_INTERVAL_P2P_AND_BROADCAST;
    }
    return DEFAULT_OSPF_DEAD_INTERVAL;
  }

  /**
   * Helper to infer hello interval from configured OSPF settings on an interface. Check explicitly
   * set hello interval or infer from OSPF network type, in that order. See
   * https://www.cisco.com/c/en/us/support/docs/ip/open-shortest-path-first-ospf/13689-17.html for
   * more details.
   */
  @VisibleForTesting
  static int toOspfHelloInterval(
      Interface iface, @Nullable org.batfish.datamodel.ospf.OspfNetworkType networkType) {
    Integer helloInterval = iface.getOspfHelloInterval();
    if (helloInterval != null) {
      return helloInterval;
    }
    if (networkType == POINT_TO_POINT || networkType == BROADCAST) {
      return DEFAULT_OSPF_HELLO_INTERVAL_P2P_AND_BROADCAST;
    }
    return DEFAULT_OSPF_HELLO_INTERVAL;
  }

  /** Helper to convert Cisco VS OSPF network type to VI model type. */
  @VisibleForTesting
  static @Nullable org.batfish.datamodel.ospf.OspfNetworkType toOspfNetworkType(
      @Nullable OspfNetworkType type, Warnings warnings) {
    if (type == null) {
      // default is broadcast for all Ethernet interfaces
      // (https://learningnetwork.cisco.com/thread/66827)
      return org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST;
    }
    switch (type) {
      case BROADCAST:
        return org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST;
      case POINT_TO_POINT:
        return org.batfish.datamodel.ospf.OspfNetworkType.POINT_TO_POINT;
      case NON_BROADCAST:
        return org.batfish.datamodel.ospf.OspfNetworkType.NON_BROADCAST_MULTI_ACCESS;
      case POINT_TO_MULTIPOINT:
        return org.batfish.datamodel.ospf.OspfNetworkType.POINT_TO_MULTIPOINT;
      default:
        warnings.redFlag(
            String.format(
                "Conversion of Cisco OSPF network type '%s' is not handled.", type.toString()));
        return null;
    }
  }

  public static @Nonnull String nameOfSourceNatIpSpaceFromAcl(@Nonnull String aclName) {
    return aclName + "~ip~nat~source~ips";
  }

  /**
   * An ACL can be used to express the set of destination IPs used for source nat. Validate the ACL
   * and return the set of IPs it will match.
   *
   * <p>TODO(https://github.com/batfish/batfish/issues/7047) for truly validating what the behavior
   * here is. Current code is inspired by
   * https://eos.arista.com/7150s-nat-practical-guide-source-nat-static/#2Static_Source_NAT_Unicast_and_multicast_with_routed_ports
   */
  static IpSpace extractSourceNatIpSpaceFromAcl(ExtendedAccessList acl, Warnings w) {
    AclIpSpace.Builder builder = AclIpSpace.builder();
    for (ExtendedAccessListLine line : acl.getLines()) {
      if (!(line.getSourceAddressSpecifier() instanceof AnyAddressSpecifier)) {
        w.redFlag(
            String.format(
                "%s line %s: source address must be 'any'", acl.getName(), line.getName()));
        continue;
      }
      if (line.getDestinationAddressSpecifier() instanceof AnyAddressSpecifier) {
        w.redFlag(
            String.format(
                "%s line %s: destination address cannot be 'any'", acl.getName(), line.getName()));
        continue;
      }
      if (!line.getServiceSpecifier()
          .toAclLineMatchExpr()
          .equals(new MatchHeaderSpace(HeaderSpace.builder().build()))) {
        w.redFlag(
            String.format(
                "%s line %s: cannot filter on anything but destination address",
                acl.getName(), line.getName()));
        continue;
      }
      assert line.getDestinationAddressSpecifier() instanceof WildcardAddressSpecifier;
      WildcardAddressSpecifier destIps =
          (WildcardAddressSpecifier) line.getDestinationAddressSpecifier();
      builder.thenAction(line.getAction(), destIps.toIpSpace());
    }
    return builder.build();
  }

  private Conversions() {} // prevent instantiation of utility class
}
