package org.batfish.vendor.sonic.representation;

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
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.SwitchportMode;
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
              .setActive(port.getAdminStatusUp().orElse(true)); // default is active

      if (interfaces.containsKey(portName)) {
        L3Interface l3Interface = interfaces.get(portName);
        ib.setAddress(l3Interface.getAddress());
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
        Interface.builder()
            .setName(vlanName)
            .setOwner(c)
            .setVrf(vrf)
            .setType(InterfaceType.VLAN)
            .setVlan(vlanId)
            .setActive(true)
            .setAddress(vlanInterfaces.get(vlanName).getAddress())
            .build();
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
          w.redFlag(String.format("Vlan member %s is not configured", memberKey));
          continue;
        }
        if (!vlanMember.getTaggingMode().isPresent()) {
          w.redFlag(String.format("tagging_mode is not configured for vlan member %s", memberKey));
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
          default:
            w.redFlag(
                String.format(
                    "Unhandled tagging_mode %s for vlan member %s",
                    vlanMember.getTaggingMode().get(), memberKey));
        }
      }
    }
  }

  private static void warnMissingVlans(Set<String> vlans, Set<String> vlanInterfaces, Warnings w) {
    Set<String> missingVlans = Sets.difference(vlanInterfaces, vlans);
    if (!missingVlans.isEmpty()) {
      w.redFlag(
          String.format(
              "Ignoring VLAN_INTERFACEs %s because they don't have VLANs defined.", missingVlans));
    }
  }

  @VisibleForTesting
  static boolean checkVlanId(String vlanName, @Nullable Integer vlanId, Warnings w) {
    if (vlanId == null) {
      w.redFlag(String.format("%s ignored: vlanid is not configured.", vlanName));
      return false;
    }
    if (vlanId < 1 || vlanId > 4094) {
      w.redFlag(
          String.format(
              "%s ignored: It has invalid vlan id %d. Vlan ids should be between 1 and 4094.",
              vlanName, vlanId));
      return false;
    }
    try {
      int vlanIdFromName = Integer.parseInt(vlanName.replaceFirst("^Vlan", ""));
      if (vlanIdFromName != vlanId) {
        w.redFlag(
            String.format(
                "%s ignored: Vlan id in the name does not match configured vlan id %d",
                vlanName, vlanId));
        return false;
      }
    } catch (NumberFormatException e) {
      w.redFlag(
          String.format(
              "%s ignored: Unexpected name format. Vlan names should be like 'Vlan1', with 'Vlan'"
                  + " followed by its numerical id.",
              vlanName));
      return false;
    }
    return true;
  }

  // Super simple name to type conversion
  // TODO: expand later as needed
  private @Nonnull static InterfaceType interfaceNameToType(String interfaceName) {
    if (interfaceName.toLowerCase().startsWith("eth")) {
      return InterfaceType.PHYSICAL;
    }
    if (interfaceName.toLowerCase().startsWith("portchannel")) {
      return InterfaceType.AGGREGATED;
    }
    throw new IllegalArgumentException("Cannot infer type for interface name " + interfaceName);
  }

  /** An intermediate class to order ACL rules and simplify processing code. */
  @VisibleForTesting
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
      if (!getPriority().equals(other.getPriority())) {
        return other.getPriority().compareTo(getPriority());
      }
      return _name.compareTo(other._name);
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
   * Converts the ACL information under ACL_TABLE and ACL_RULE tables into IpAccessLists and
   * attached them to the appropriate interfaces.
   *
   * <p>All VI interfaces must have been created prior to calling this method.
   */
  static void convertAcls(
      Configuration c, Map<String, AclTable> aclTables, Map<String, AclRule> aclRules, Warnings w) {
    /*
     Walk the AclRule entries, to filter out bad rules and organize the rest by the ACL of which they are a part.
     This organization  puts the rules of an ACL in a SortedSet that is ordered from high to low PRIORITY.
     If two rules have  the same PRIORITY, ties are broken by rule name.
    */

    Set<String> rulesWithoutPriority = new HashSet<>();
    Set<String> rulesWithoutPacketAction = new HashSet<>();
    Set<String> rulesWithBadKeys = new HashSet<>();
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
          aclNameToRules
              .computeIfAbsent(ruleKeyParts[0], aclName -> new TreeSet<>())
              .add(new AclRuleWithName(ruleKeyParts[1], value));
        });

    warnBadRules(
        rulesWithBadKeys,
        rulesWithoutPriority,
        rulesWithoutPacketAction,
        aclNameToRules,
        aclTables.keySet(),
        w);

    for (String aclName : aclTables.keySet()) {
      IpAccessList ipAccessList =
          convertAcl(
              c,
              aclName,
              // if an Acl is in ACL_TABLE but not in ACL_RULE, we create an empty Acl
              Optional.ofNullable(aclNameToRules.get(aclName)).orElse(ImmutableSortedSet.of()),
              w);
      AclTable aclTable = aclTables.get(aclName);
      attachAcl(c, aclName, ipAccessList, aclTable, w);
    }
  }

  private static void warnBadRules(
      Set<String> rulesWithBadKeys,
      Set<String> rulesWithoutPriority,
      Set<String> rulesWithoutPacketAction,
      Map<String, SortedSet<AclRuleWithName>> aclNameToRules,
      Set<String> aclTableNames,
      Warnings w) {
    rulesWithBadKeys.forEach(
        key -> w.redFlag(String.format("Ignored ACL_RULE %s: Badly formatted name", key)));
    rulesWithoutPriority.forEach(
        key -> w.redFlag(String.format("Ignored ACL_RULE %s: Missing PRIORITY", key)));
    rulesWithoutPacketAction.forEach(
        key -> w.redFlag(String.format("Ignored ACL_RULE %s: Missing PACKET_ACTION", key)));
    Sets.difference(aclNameToRules.keySet(), aclTableNames) // missing acl tables
        .forEach(
            aclName ->
                aclNameToRules
                    .get(aclName)
                    .forEach(
                        aclRuleWithName ->
                            w.redFlag(
                                String.format(
                                    "Ignored ACL_RULE %s|%s: Missing ACL_TABLE '%s'",
                                    aclName, aclRuleWithName._name, aclName))));
  }

  /**
   * Attaches the {@code ipAccessList} derived from {@cide aclTable} to the appropriate interfaces
   * in {@code c}.
   */
  @VisibleForTesting
  static void attachAcl(
      Configuration c, String aclName, IpAccessList ipAccessList, AclTable aclTable, Warnings w) {
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
          w.redFlag(
              String.format(
                  "Port '%s' referenced in ACL_TABLE '%s' does not exist.", port, aclName));
        }
        continue;
      }
      if (aclStage == Stage.INGRESS) {
        viIface.setInboundFilter(ipAccessList);
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
      Configuration c, String aclName, SortedSet<AclRuleWithName> aclRules, Warnings w) {
    return IpAccessList.builder()
        .setOwner(c)
        .setName(aclName)
        .setLines(
            aclRules.stream()
                .map(rule -> convertAclRule(aclName, rule, w))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  private static Optional<AclLine> convertAclRule(
      String aclName, AclRuleWithName aclRuleWithName, Warnings w) {
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

    switch (aclRuleWithName.getPacketAction()) {
      case ACCEPT:
      case FORWARD:
        return Optional.of(ExprAclLine.accepting(aclRuleWithName._name, matchExpr));
      case DROP:
        return Optional.of(ExprAclLine.rejecting(aclRuleWithName._name, matchExpr));
      default:
        w.redFlag(
            String.format(
                "Ignored ACL_RULE %s|%s: PACKET_ACTION %s is unimplemented",
                aclName, aclRuleWithName._name, aclRuleWithName.getPacketAction()));
        return Optional.empty();
    }
  }
}
