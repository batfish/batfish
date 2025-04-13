package org.batfish.vendor.sonic.representation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
import static org.batfish.representation.frr.FrrConversions.SPEED_CONVERSION_FACTOR;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SnmpCommunity;
import org.batfish.datamodel.SnmpServer;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.vendor.sonic.representation.AclRule.PacketAction;
import org.batfish.vendor.sonic.representation.AclTable.Stage;
import org.batfish.vendor.sonic.representation.AclTable.Type;

public class SonicConversions {

  /** Converts physical ports found under PORT or the MGMT_PORT objects. */
  static void convertPorts(
      Configuration c, Map<String, Port> ports, Map<String, L3Interface> interfaces, Vrf vrf) {

    // TODO: Set bandwidth appropriately. Factor in runtime data

    for (String portName : ports.keySet()) {
      Port port = ports.get(portName);
      boolean active = port.getAdminStatusUp().orElse(true); // default is active
      Interface.Builder ib =
          Interface.builder()
              .setName(portName)
              .setHumanName(port.getAlias().orElse(null))
              .setOwner(c)
              .setVrf(vrf)
              .setType(InterfaceType.PHYSICAL)
              .setDescription(port.getDescription().orElse(null))
              .setMtu(port.getMtu().orElse(null))
              .setSpeed(
                  port.getSpeed()
                      .map(speed -> speed * SPEED_CONVERSION_FACTOR)
                      .orElse(null)) // TODO: default speed
              .setAdminUp(active);

      if (interfaces.containsKey(portName)) {
        setInterfaceAddresses(ib, interfaces.get(portName));
      }

      ib.build();
    }
  }

  /**
   * Sets the concrete addresses of the VI Interface based on the L3Interface. If multiple
   * non-secondary interfaces are present, the lowest one is picked as primary
   */
  @VisibleForTesting
  static void setInterfaceAddresses(Interface.Builder viInterfaceBuilder, L3Interface l3Interface) {
    Optional<ConcreteInterfaceAddress> primaryAddress =
        l3Interface.getAddresses().entrySet().stream()
            .filter(e -> !firstNonNull(e.getValue().getSecondary(), false))
            .map(Entry::getKey)
            .sorted()
            .findFirst();
    viInterfaceBuilder.setAddresses(
        primaryAddress.orElse(null),
        l3Interface.getAddresses().keySet().toArray(new ConcreteInterfaceAddress[0]));
  }

  /** Converts loopbacks under LOOPBACK and LOOPBACK_INTERFACE tables */
  static void convertLoopbacks(
      Configuration c,
      Set<String> loopbacks,
      Map<String, L3Interface> loopbackInterfaces,
      Vrf vrf) {

    // TODO: set bandwidth appropriately

    // create VI interface for names that appear in either loopbacks or loopbackInterfaces
    // set addresses for those that appear in the latter
    for (String ifaceName : Sets.union(loopbacks, loopbackInterfaces.keySet())) {
      Interface.Builder ib =
          Interface.builder()
              .setName(ifaceName)
              .setOwner(c)
              .setVrf(vrf)
              .setType(InterfaceType.LOOPBACK);
      if (loopbackInterfaces.containsKey(ifaceName)) {
        setInterfaceAddresses(ib, loopbackInterfaces.get(ifaceName));
      }
      ib.build();
    }
  }

  /**
   * Converts vlan information under the VLAN, VLAN_MEMBER, and VLAN_INTERFACE tables.
   *
   * <p>It will create a new VI interface for the VLAN and put it in {@code vrf}. If the VI version
   * of a member interface does not already exist in {@code c}, it is created as well. The member
   * interfaces are configured based on the tagging mode.
   */
  static void convertVlans(
      Configuration c,
      Map<String, Vlan> vlans,
      Map<String, VlanMember> vlanMembers,
      Map<String, L3Interface> vlanInterfaces,
      Vrf vrf,
      Warnings w) {
    warnMissingVlans(vlans.keySet(), vlanInterfaces.keySet(), w);
    for (String vlanName : vlans.keySet()) {
      Vlan vlan = vlans.get(vlanName);
      if (!checkVlanId(vlanName, vlan.getVlanId().orElse(null), w)) {
        continue;
      }
      int vlanId = vlan.getVlanId().get(); // must exist since checkVlanId passed
      if (vlanInterfaces.containsKey(vlanName)) {
        Interface.Builder ib =
            Interface.builder()
                .setName(vlanName)
                .setOwner(c)
                .setVrf(vrf)
                .setType(InterfaceType.VLAN)
                .setVlan(vlanId)
                .setDhcpRelayAddresses(convertDhcpServers(vlan.getDhcpServers(), w));
        setInterfaceAddresses(ib, vlanInterfaces.get(vlanName));
        ib.build();
      }

      for (String memberName : vlan.getMembers()) {
        Interface memberInterface = c.getAllInterfaces().get(memberName);
        if (memberInterface == null) {
          memberInterface =
              Interface.builder()
                  .setName(memberName)
                  .setOwner(c)
                  .setVrf(vrf)
                  .setType(interfaceNameToType(memberName))
                  .build();
        }

        String memberKey =
            String.format(
                "%s|%s", vlanName, memberName); // how members are encoded in configdb data
        VlanMember vlanMember = vlanMembers.get(memberKey);
        if (vlanMember == null) {
          w.redFlagf("Vlan member %s is not configured", memberKey);
          continue;
        }
        if (!vlanMember.getTaggingMode().isPresent()) {
          w.redFlagf("tagging_mode is not configured for vlan member %s", memberKey);
          continue;
        }
        switch (vlanMember.getTaggingMode().get()) {
          case TAGGED:
            memberInterface.setSwitchport(true);
            memberInterface.setSwitchportMode(SwitchportMode.TRUNK);
            memberInterface.setNativeVlan(vlanId);
            memberInterface.setAllowedVlans(IntegerSpace.of(vlanId));
            break;
          case UNTAGGED:
            memberInterface.setSwitchport(true);
            memberInterface.setSwitchportMode(SwitchportMode.ACCESS);
            memberInterface.setAccessVlan(vlanId);
            break;
        }
      }
    }
  }

  /**
   * Converts a list of configured DHCP servers to a list of IPs.
   *
   * <p>Batfish VI datamodel currently expects DHCP relays to be IP addresses, while SONiC allows
   * DHCP servers to be names. This method converts those it can and warns about the remaining
   * entries.
   */
  @VisibleForTesting
  static List<Ip> convertDhcpServers(List<String> dhcpServers, Warnings w) {
    return dhcpServers.stream()
        .map(
            server -> {
              try {
                return Ip.parse(server);
              } catch (IllegalArgumentException e) {
                w.redFlagf("Cannot add a non-IP address value '%s' as DHCP server", server);
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
  }

  private static void warnMissingVlans(Set<String> vlans, Set<String> vlanInterfaces, Warnings w) {
    Set<String> missingVlans = Sets.difference(vlanInterfaces, vlans);
    if (!missingVlans.isEmpty()) {
      w.redFlagf(
          "Ignoring VLAN_INTERFACEs %s because they don't have VLANs defined.", missingVlans);
    }
  }

  @VisibleForTesting
  static boolean checkVlanId(String vlanName, @Nullable Integer vlanId, Warnings w) {
    if (vlanId == null) {
      w.redFlagf("%s ignored: vlanid is not configured.", vlanName);
      return false;
    }
    if (vlanId < 1 || vlanId > 4094) {
      w.redFlagf(
          "%s ignored: It has invalid vlan id %d. Vlan ids should be between 1 and 4094.",
          vlanName, vlanId);
      return false;
    }
    try {
      int vlanIdFromName = Integer.parseInt(vlanName.replaceFirst("^Vlan", ""));
      if (vlanIdFromName != vlanId) {
        w.redFlagf(
            "%s ignored: Vlan id in the name does not match configured vlan id %d",
            vlanName, vlanId);
        return false;
      }
    } catch (NumberFormatException e) {
      w.redFlagf(
          "%s ignored: Unexpected name format. Vlan names should be like 'Vlan1', with 'Vlan'"
              + " followed by its numerical id.",
          vlanName);
      return false;
    }
    return true;
  }

  // Super simple name to type conversion
  // TODO: expand later as needed
  private static @Nonnull InterfaceType interfaceNameToType(String interfaceName) {
    if (interfaceName.toLowerCase().startsWith("eth")) {
      return InterfaceType.PHYSICAL;
    }
    if (interfaceName.toLowerCase().startsWith("portchannel")) {
      return InterfaceType.AGGREGATED;
    }
    throw new IllegalArgumentException("Cannot infer type for interface name " + interfaceName);
  }

  /** An intermediate class to order ACL rules and simplify processing code. */
  static class AclRuleWithName implements Comparable<AclRuleWithName> {
    private final String _name;
    private final AclRule _rule;

    AclRuleWithName(String name, AclRule rule) {
      checkArgument(
          rule.getPriority().isPresent(),
          "AclRuleWithName cannot be instantiated with null priority");
      checkArgument(
          rule.getPacketAction().isPresent(),
          "AclRuleWithName cannot be instantiated with null packet action");
      _name = name;
      _rule = rule;
    }

    /**
     * Gives lower ranks to higher priority rules, and uses names in case priorities are identical.
     */
    @Override
    public int compareTo(AclRuleWithName other) {
      return Comparator.comparing(AclRuleWithName::getPriority, Comparator.reverseOrder())
          .thenComparing(r -> r._name)
          .compare(this, other);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private @Nonnull Integer getPriority() {
      return _rule.getPriority().get(); // must exist
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private @Nonnull PacketAction getPacketAction() {
      return _rule.getPacketAction().get(); // must exist
    }
  }

  /**
   * Returns the set of configured AclRules as a map from AclTable name to the rules for that table.
   * The rules for a table are sorted based on priority (descending), and ties are broken based on
   * rule name.
   *
   * <p>The method filters out bad rules and non-IP rules, and it adds warnings for them.
   */
  static Map<String, SortedSet<AclRuleWithName>> getAclRulesByTableName(
      Map<String, AclTable> aclTables, Map<String, AclRule> aclRules, Warnings w) {
    Set<String> rulesWithoutPriority = new HashSet<>();
    Set<String> rulesWithoutPacketAction = new HashSet<>();
    Set<String> rulesWithBadKeys = new HashSet<>();
    Set<String> rulesWithNonIpv4EtherType = new HashSet<>();
    Map<String, SortedSet<AclRuleWithName>> aclNameToRules = new HashMap<>();
    aclRules.forEach(
        (key, value) -> {
          String[] ruleKeyParts = key.split("\\|", 2);
          if (ruleKeyParts.length < 2) {
            rulesWithBadKeys.add(key);
            return;
          }
          if (!value.getPriority().isPresent()) {
            rulesWithoutPriority.add(key);
            return;
          }
          if (!value.getPacketAction().isPresent()) {
            rulesWithoutPacketAction.add(key);
            return;
          }
          Integer etherType = value.getEtherType();
          if (etherType != null && etherType != 0x800) {
            rulesWithNonIpv4EtherType.add(key);
            return;
          }
          aclNameToRules
              .computeIfAbsent(ruleKeyParts[0], aclName -> new TreeSet<>())
              .add(new AclRuleWithName(ruleKeyParts[1], value));
        });

    warnBadRules(
        rulesWithBadKeys,
        rulesWithoutPriority,
        rulesWithoutPacketAction,
        rulesWithNonIpv4EtherType,
        aclNameToRules,
        aclTables.keySet(),
        w);

    return aclNameToRules;
  }

  private static void warnBadRules(
      Set<String> rulesWithBadKeys,
      Set<String> rulesWithoutPriority,
      Set<String> rulesWithoutPacketAction,
      Set<String> rulesWithNonIpEtherType,
      Map<String, SortedSet<AclRuleWithName>> aclNameToRules,
      Set<String> aclTableNames,
      Warnings w) {
    rulesWithBadKeys.forEach(key -> w.redFlagf("Ignored ACL_RULE %s: Badly formatted name", key));
    rulesWithoutPriority.forEach(key -> w.redFlagf("Ignored ACL_RULE %s: Missing PRIORITY", key));
    rulesWithoutPacketAction.forEach(
        key -> w.redFlagf("Ignored ACL_RULE %s: Missing PACKET_ACTION", key));
    rulesWithNonIpEtherType.forEach(
        key -> w.redFlagf("Ignored ACL_RULE %s: Non-IPv4 ETHER_TYPE", key));
    Sets.difference(aclNameToRules.keySet(), aclTableNames) // missing acl tables
        .forEach(
            aclName ->
                aclNameToRules
                    .get(aclName)
                    .forEach(
                        aclRuleWithName ->
                            w.redFlagf(
                                "Ignored ACL_RULE %s|%s: Missing ACL_TABLE '%s'",
                                aclName, aclRuleWithName._name, aclName)));
  }

  /**
   * Converts the ACL information under ACL_TABLE and ACL_RULE tables into IpAccessLists and
   * attached them to the appropriate interfaces.
   *
   * <p>All VI interfaces must have been created prior to calling this method.
   */
  static void convertAcls(
      Configuration c,
      Map<String, AclTable> aclTables,
      Map<String, SortedSet<AclRuleWithName>> aclNameToRules,
      Warnings w) {
    for (String aclName : aclTables.keySet()) {
      IpAccessList ipAccessList =
          convertAcl(
              c,
              aclName,
              // if an Acl is in ACL_TABLE but not in ACL_RULE, we create an empty Acl
              Optional.ofNullable(aclNameToRules.get(aclName)).orElse(ImmutableSortedSet.of()));
      AclTable aclTable = aclTables.get(aclName);
      attachAcl(c, ipAccessList, aclTable, w);
    }
  }

  /**
   * Attaches the {@code ipAccessList} derived from {@code aclTable} to the appropriate interfaces
   * in {@code c}.
   *
   * <p>The {@link IpAccessList} should already exist in the specified configuration.
   */
  @VisibleForTesting
  static void attachAcl(Configuration c, IpAccessList ipAccessList, AclTable aclTable, Warnings w) {
    if (aclTable.getType().orElse(null) != Type.L3) {
      // All ACLs are added to the configuration, but only type L3 ones are attached to interfaces
      return;
    }
    Stage aclStage = aclTable.getStage().orElse(null);
    if (aclStage != Stage.EGRESS && aclStage != Stage.INGRESS) {
      w.redFlag("Unimplemented ACL stage: " + aclStage);
      return;
    }
    for (String port : aclTable.getPorts()) {
      Interface viIface = c.getAllInterfaces().get(port);
      if (viIface == null) {
        if (!aclTable.isControlPlanePort(port)) {
          w.redFlagf(
              "Port '%s' referenced in ACL_TABLE '%s' does not exist.",
              port, ipAccessList.getName());
        }
        continue;
      }
      if (aclStage == Stage.INGRESS) {
        viIface.setIncomingFilter(ipAccessList);
      } else { // EGRESS
        viIface.setOutgoingFilter(ipAccessList);
      }
    }
  }

  /**
   * Converts all the rules of {@code aclName} into {@link IpAccessList} and adds the result to
   * device configuration {@code c}.
   */
  private static IpAccessList convertAcl(
      Configuration c, String aclName, SortedSet<AclRuleWithName> aclRules) {
    return IpAccessList.builder()
        .setOwner(c)
        .setName(aclName)
        .setLines(
            aclRules.stream()
                .map(SonicConversions::convertAclRule)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  private static Optional<AclLine> convertAclRule(AclRuleWithName aclRuleWithName) {
    AclRule aclRule = aclRuleWithName._rule;

    List<AclLineMatchExpr> conjuncts = new LinkedList<>();
    if (aclRule.getIpProtocol().isPresent()) {
      conjuncts.add(matchIpProtocol(aclRule.getIpProtocol().get()));
    }
    if (aclRule.getDstIp().isPresent()) {
      conjuncts.add(matchDst(aclRule.getDstIp().get()));
    }
    if (aclRule.getSrcIp().isPresent()) {
      conjuncts.add(matchSrc(aclRule.getSrcIp().get()));
    }
    if (aclRule.getL4DstPort().isPresent()) {
      conjuncts.add(matchDstPort(aclRule.getL4DstPort().get()));
    }
    if (aclRule.getL4SrcPort().isPresent()) {
      conjuncts.add(matchSrcPort(aclRule.getL4SrcPort().get()));
    }
    AclLineMatchExpr matchExpr = and(conjuncts);

    return switch (aclRuleWithName.getPacketAction()) {
      case ACCEPT, FORWARD -> Optional.of(ExprAclLine.accepting(aclRuleWithName._name, matchExpr));
      case DROP -> Optional.of(ExprAclLine.rejecting(aclRuleWithName._name, matchExpr));
    };
  }

  /**
   * Generates an {@link SnmpServer} by joining community information in SnmpYml file with ACL
   * information in config DB.
   *
   * <p>Inserts the generated server in the default VRF of {@code c}.
   */
  static void convertSnmpServer(
      Configuration c,
      String communityName,
      Map<String, AclTable> aclTables,
      Map<String, SortedSet<AclRuleWithName>> aclNameToRules,
      Warnings w) {
    SnmpServer snmpServer = new SnmpServer();
    SnmpCommunity snmpCommunity = new SnmpCommunity(communityName);
    snmpServer.setCommunities(ImmutableSortedMap.of(communityName, snmpCommunity));
    c.getDefaultVrf().setSnmpServer(snmpServer);

    List<String> snmpTableNames =
        aclTables.keySet().stream()
            .filter(name -> isSnmpTable(aclTables.get(name)))
            .collect(ImmutableList.toImmutableList());
    if (snmpTableNames.size() > 1) {
      // don't know what to do when we find multiple tables; warn and ignore all
      w.redFlagf("Found multiple SNMP ACL tables: %s. Ignored all.", snmpTableNames);
      return;
    }
    if (snmpTableNames.isEmpty()) { // no table found
      return;
    }
    String snmpTableName = snmpTableNames.get(0);
    SortedSet<AclRuleWithName> aclRules = aclNameToRules.get(snmpTableName);
    if (aclRules == null) {
      w.redFlagf("ACL rules not found for SNMP table '%s'.", snmpTableName);
      return;
    }

    snmpCommunity.setAccessList(snmpTableName);
    snmpCommunity.setClientIps(computeSnmpClientSpace(aclRules));
  }

  /** Computes the client IpSpace that is permitted by the ACL rules */
  @VisibleForTesting
  static IpSpace computeSnmpClientSpace(SortedSet<AclRuleWithName> aclRuleWithNames) {
    AclIpSpace.Builder space = AclIpSpace.builder();
    for (AclRuleWithName ruleWithName : aclRuleWithNames) {
      AclRule rule = ruleWithName._rule;
      if (!allowsSnmp(rule)) {
        continue;
      }
      LineAction action =
          ruleWithName.getPacketAction() == PacketAction.ACCEPT
              ? LineAction.PERMIT
              : LineAction.DENY;
      space.thenAction(
          action, rule.getSrcIp().map(Prefix::toIpSpace).orElse(UniverseIpSpace.INSTANCE));
    }
    return space.build();
  }

  /** Criteria for an ACL Table to be judged as the one to use for SNMP */
  @VisibleForTesting
  static boolean isSnmpTable(AclTable aclTable) {
    return aclTable.getStage().map(stage -> stage == Stage.INGRESS).orElse(false)
        && aclTable.getType().map(type -> type == Type.CTRLPLANE).orElse(false)
        && aclTable.getServices().stream().anyMatch(s -> s.equalsIgnoreCase("SNMP"));
  }

  /** Does the AclRule allow SNMP traffic? */
  @VisibleForTesting
  static boolean allowsSnmp(AclRule rule) {
    return rule.getIpProtocol().map(protocol -> protocol == IpProtocol.UDP.number()).orElse(true)
        && rule.getL4DstPort().map(port -> port == NamedPort.SNMP.number()).orElse(true);
  }
}
