package org.batfish.vendor.sonic.representation;

import static org.batfish.representation.frr.FrrConversions.SPEED_CONVERSION_FACTOR;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
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
   * Converts the ACL information under ACL_TABLE and ACL_RULE tables into IpAccessLists and
   * attached them to the appropriate interfaces.
   *
   * <p>All VI interfaces must have been created prior to calling this method.
   */
  static void convertAcls(
      Configuration c, Map<String, AclTable> aclTables, Map<String, AclRule> aclRules, Warnings w) {
    for (String aclName : aclTables.keySet()) {
      IpAccessList ipAccessList = convertAcl(c, aclName, aclRules, w);
      AclTable aclTable = aclTables.get(aclName);
      attachAcl(c, aclName, ipAccessList, aclTable, w);
    }
  }

  @VisibleForTesting
  static void attachAcl(
      Configuration c, String aclName, IpAccessList ipAccessList, AclTable aclTable, Warnings w) {
    if (aclTable.getType().orElse(null) != Type.L3) {
      // While all ACLs are added to the configuration, but only type L3 is attached to interfaces
      return;
    }
    Stage aclStage = aclTable.getStage().orElse(null);
    if (aclStage != Stage.EGRESS && aclStage != Stage.INGRESS) {
      return;
    }
    for (String port : aclTable.getPorts()) {
      Interface viIface = c.getAllInterfaces().get(port);
      if (viIface == null) {
        if (!port.equalsIgnoreCase("CtrlPlane")) {
          w.redFlag(
              String.format("Port %s referenced in ACL_TABLE %s does not exist.", port, aclName));
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
   * Converts all the rules of the given {@code aclName} into {@link IpAccessList} and adds it to
   * device configuration {@code c}.
   */
  @VisibleForTesting
  static IpAccessList convertAcl(
      Configuration c, String aclName, Map<String, AclRule> aclRules, Warnings w) {
    Map<Integer, AclLine> aclLines = new HashMap<>();
    for (String ruleKey : aclRules.keySet()) {
      String[] parts = ruleKey.split("\\|", 2);
      if (parts.length != 2) {
        w.redFlag("Rule name not found in ACL_RULE key " + ruleKey);
        continue;
      }
      if (parts[0].equals(aclName)) { // not this ACL's rule
        continue;
      }
      Integer priority = aclRules.get(ruleKey).getPriority().orElse(null);
      if (priority == null) {
        w.redFlag(String.format("Ignored ACL_RULE %s because PRIORITY was not defined", ruleKey));
        continue;
      }
      if (aclLines.containsKey(priority)) {
        w.redFlag(
            String.format(
                "Ignored ACL_RULE %s because its PRIORITY is duplicate of %s|%s",
                ruleKey, aclName, aclLines.get(priority).getName()));
        continue;
      }
      Optional<AclLine> aclLine = convertAclRule(ruleKey, parts[1], aclRules.get(ruleKey), w);
      if (!aclLine.isPresent()) {
        continue;
      }
      aclLines.put(priority, aclLine.get());
    }
    return IpAccessList.builder()
        .setOwner(c)
        .setName(aclName)
        .setLines(
            aclLines.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .map(aclLines::get)
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  @VisibleForTesting
  static Optional<AclLine> convertAclRule(
      String ruleKey, String ruleName, AclRule aclRule, Warnings w) {
    PacketAction packetAction = aclRule.getPacketAction().orElse(null);
    if (packetAction == null) {
      w.redFlag(String.format("Ignored ACL_RULE %s because PACKET_ACTION is not set", ruleKey));
      return Optional.empty();
    }
    AclLineMatchExpr matchExpr =
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(
                    aclRule
                        .getIpProtocol()
                        .map(proto -> ImmutableSet.of(IpProtocol.fromNumber(proto)))
                        .orElse(ImmutableSet.of()))
                .setDstIps(aclRule.getDstIp().map(Prefix::toIpSpace).orElse(null))
                .setSrcIps(aclRule.getSrcIp().map(Prefix::toIpSpace).orElse(null))
                .setDstPorts(
                    aclRule
                        .getL4DstPort()
                        .map(port -> ImmutableSet.of(SubRange.singleton(port)))
                        .orElse(ImmutableSet.of()))
                .setSrcPorts(
                    aclRule
                        .getL4SrcPort()
                        .map(port -> ImmutableSet.of(SubRange.singleton(port)))
                        .orElse(ImmutableSet.of()))
                .build());
    switch (packetAction) {
      case ACCEPT:
      case FORWARD:
        return Optional.of(ExprAclLine.accepting(ruleName, matchExpr));
      case DROP:
        return Optional.of(ExprAclLine.rejecting(ruleName, matchExpr));
      default:
        w.redFlag(
            String.format(
                "Ignored ACL_RULE %s because PACKET_ACTION %s is unimplemented",
                ruleKey, packetAction));
        return Optional.empty();
    }
  }
}
