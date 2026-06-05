package org.batfish.vendor.sros.grammar;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.vendor.sros.representation.BgpGroup;
import org.batfish.vendor.sros.representation.BgpNeighbor;
import org.batfish.vendor.sros.representation.BgpProcess;
import org.batfish.vendor.sros.representation.Card;
import org.batfish.vendor.sros.representation.Mda;
import org.batfish.vendor.sros.representation.PolicyAction;
import org.batfish.vendor.sros.representation.PolicyStatement;
import org.batfish.vendor.sros.representation.PolicyStatementEntry;
import org.batfish.vendor.sros.representation.Port;
import org.batfish.vendor.sros.representation.PrefixList;
import org.batfish.vendor.sros.representation.PrefixListEntry;
import org.batfish.vendor.sros.representation.Router;
import org.batfish.vendor.sros.representation.RouterInterface;
import org.batfish.vendor.sros.representation.SrosConfiguration;

/**
 * Populates a {@link SrosConfiguration}'s typed feature model from the canonical, preprocessed
 * {@link SrosStatementTree}.
 *
 * <p>Each leaf's value is the single child key under its leaf node (e.g. {@code card 1 card-type
 * iom-1} is {@code card -> 1 -> card-type -> iom-1}); leaf-lists ({@code policy [a b]}) are the
 * ordered children of the leaf node. The extractor reads only the paths characterized for P4
 * (hardware provisioning, router interfaces, BGP peering, routing-policy). Other configured
 * subtrees present in real configs — {@code system security}/{@code ssh}/{@code user-params},
 * {@code persistent-indices} — are control-plane-irrelevant and intentionally left unread; they are
 * not warnings (the device accepts them and so do we). The one system leaf that matters, {@code
 * system name}, becomes the Batfish hostname.
 */
@ParametersAreNonnullByDefault
public final class SrosFeatureExtractor {

  public static void extract(SrosStatementTree root, SrosConfiguration c, Warnings w) {
    new SrosFeatureExtractor(c, w).extractFrom(root);
  }

  private SrosFeatureExtractor(SrosConfiguration c, Warnings w) {
    _c = c;
    _w = w;
  }

  private void extractFrom(SrosStatementTree root) {
    SrosStatementTree configure = root.getChild("configure");
    if (configure == null) {
      return;
    }
    extractSystem(configure.getChild("system"));
    extractCards(configure.getChild("card"));
    extractPorts(configure.getChild("port"));
    extractPolicyOptions(configure.getChild("policy-options"));
    extractRouters(configure.getChild("router"));
  }

  // --- system -----------------------------------------------------------------------------------

  private void extractSystem(@Nullable SrosStatementTree system) {
    if (system == null) {
      return;
    }
    // Only the system name is control-plane-relevant; it maps to the Batfish hostname. The rest of
    // the system subtree (security, ssh, user-params, ...) is intentionally not modeled.
    String name = singleValue(system, "name");
    if (name != null) {
      _c.setHostname(unquote(name));
    }
  }

  // --- hardware ---------------------------------------------------------------------------------

  private void extractCards(@Nullable SrosStatementTree cardList) {
    if (cardList == null) {
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : cardList.getChildren().entrySet()) {
      Integer slot = parseInt(e.getKey());
      if (slot == null) {
        continue;
      }
      SrosStatementTree cardNode = e.getValue();
      Card card = new Card(slot);
      card.setCardType(singleValue(cardNode, "card-type"));
      SrosStatementTree mdaList = cardNode.getChild("mda");
      if (mdaList != null) {
        for (Map.Entry<String, SrosStatementTree> me : mdaList.getChildren().entrySet()) {
          Integer mdaSlot = parseInt(me.getKey());
          if (mdaSlot == null) {
            continue;
          }
          Mda mda = new Mda(mdaSlot);
          mda.setMdaType(singleValue(me.getValue(), "mda-type"));
          card.getMdas().put(mdaSlot, mda);
        }
      }
      _c.getCards().put(slot, card);
    }
  }

  private void extractPorts(@Nullable SrosStatementTree portList) {
    if (portList == null) {
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : portList.getChildren().entrySet()) {
      String name = e.getKey();
      SrosStatementTree portNode = e.getValue();
      Port port = new Port(name);
      Boolean adminState = parseAdminState(singleValue(portNode, "admin-state"));
      port.setAdminStateEnable(adminState);
      SrosStatementTree connector = portNode.getChild("connector");
      if (connector != null) {
        port.setBreakout(singleValue(connector, "breakout"));
      }
      _c.getPorts().put(name, port);
    }
  }

  // --- routers, interfaces, bgp -----------------------------------------------------------------

  private void extractRouters(@Nullable SrosStatementTree routerList) {
    if (routerList == null) {
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : routerList.getChildren().entrySet()) {
      String name = unquote(e.getKey());
      SrosStatementTree routerNode = e.getValue();
      Router router = new Router(name);
      Long as = parseLong(singleValue(routerNode, "autonomous-system"));
      router.setAutonomousSystem(as);
      extractInterfaces(router, routerNode.getChild("interface"));
      extractBgp(router, routerNode.getChild("bgp"));
      _c.getRouters().put(name, router);
    }
  }

  private void extractInterfaces(Router router, @Nullable SrosStatementTree ifaceList) {
    if (ifaceList == null) {
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : ifaceList.getChildren().entrySet()) {
      String name = unquote(e.getKey());
      SrosStatementTree ifaceNode = e.getValue();
      RouterInterface iface = new RouterInterface(name);
      iface.setPort(singleValue(ifaceNode, "port"));
      SrosStatementTree primary = navigate(ifaceNode, "ipv4", "primary");
      if (primary != null) {
        iface.setPrimaryAddress(parseIp(singleValue(primary, "address")));
        iface.setPrimaryPrefixLength(parseInt(singleValue(primary, "prefix-length")));
      }
      router.getInterfaces().put(name, iface);
    }
  }

  private void extractBgp(Router router, @Nullable SrosStatementTree bgpNode) {
    if (bgpNode == null) {
      return;
    }
    BgpProcess proc = new BgpProcess();
    proc.setRouterId(parseIp(singleValue(bgpNode, "router-id")));

    SrosStatementTree groupList = bgpNode.getChild("group");
    if (groupList != null) {
      for (Map.Entry<String, SrosStatementTree> e : groupList.getChildren().entrySet()) {
        String name = unquote(e.getKey());
        SrosStatementTree groupNode = e.getValue();
        BgpGroup group = new BgpGroup(name);
        group.setPeerAs(parseLong(singleValue(groupNode, "peer-as")));
        group.getImportPolicies().addAll(policyNames(navigate(groupNode, "import", "policy")));
        group.getExportPolicies().addAll(policyNames(navigate(groupNode, "export", "policy")));
        proc.getGroups().put(name, group);
      }
    }

    SrosStatementTree neighborList = bgpNode.getChild("neighbor");
    if (neighborList != null) {
      for (Map.Entry<String, SrosStatementTree> e : neighborList.getChildren().entrySet()) {
        String ip = unquote(e.getKey());
        SrosStatementTree nbrNode = e.getValue();
        BgpNeighbor neighbor = new BgpNeighbor(ip);
        String group = singleValue(nbrNode, "group");
        neighbor.setGroup(group == null ? null : unquote(group));
        neighbor.setPeerAs(parseLong(singleValue(nbrNode, "peer-as")));
        neighbor.getImportPolicies().addAll(policyNames(navigate(nbrNode, "import", "policy")));
        neighbor.getExportPolicies().addAll(policyNames(navigate(nbrNode, "export", "policy")));
        proc.getNeighbors().put(ip, neighbor);
      }
    }
    router.setBgpProcess(proc);
  }

  // --- policy-options ---------------------------------------------------------------------------

  private void extractPolicyOptions(@Nullable SrosStatementTree po) {
    if (po == null) {
      return;
    }
    SrosStatementTree prefixLists = po.getChild("prefix-list");
    if (prefixLists != null) {
      for (Map.Entry<String, SrosStatementTree> e : prefixLists.getChildren().entrySet()) {
        String name = unquote(e.getKey());
        PrefixList pl = new PrefixList(name);
        SrosStatementTree prefixNode = e.getValue().getChild("prefix");
        if (prefixNode != null) {
          for (Map.Entry<String, SrosStatementTree> pe : prefixNode.getChildren().entrySet()) {
            Prefix prefix = parsePrefix(pe.getKey());
            if (prefix == null) {
              continue;
            }
            PrefixListEntry.Type type = parsePrefixType(singleValue(pe.getValue(), "type"));
            if (type == null) {
              continue;
            }
            pl.getEntries().add(new PrefixListEntry(prefix, type));
          }
        }
        _c.getPrefixLists().put(name, pl);
      }
    }

    SrosStatementTree policyStatements = po.getChild("policy-statement");
    if (policyStatements != null) {
      for (Map.Entry<String, SrosStatementTree> e : policyStatements.getChildren().entrySet()) {
        String name = unquote(e.getKey());
        SrosStatementTree psNode = e.getValue();
        PolicyStatement ps = new PolicyStatement(name);
        SrosStatementTree entryList = psNode.getChild("entry");
        if (entryList != null) {
          for (Map.Entry<String, SrosStatementTree> ee : entryList.getChildren().entrySet()) {
            Long entryId = parseLong(ee.getKey());
            if (entryId == null) {
              continue;
            }
            SrosStatementTree entryNode = ee.getValue();
            PolicyStatementEntry entry = new PolicyStatementEntry(entryId);
            entry
                .getFromPrefixLists()
                .addAll(policyNames(navigate(entryNode, "from", "prefix-list")));
            entry.setAction(parseAction(navigate(entryNode, "action", "action-type")));
            ps.getEntries().put(entryId, entry);
          }
        }
        ps.setDefaultAction(parseAction(navigate(psNode, "default-action", "action-type")));
        _c.getPolicyStatements().put(name, ps);
      }
    }
  }

  // --- value helpers ----------------------------------------------------------------------------

  /** The single child key of {@code node}'s {@code leaf} child (a leaf value), or {@code null}. */
  private static @Nullable String singleValue(SrosStatementTree node, String leaf) {
    SrosStatementTree leafNode = node.getChild(leaf);
    return singleKey(leafNode);
  }

  private static @Nullable String singleKey(@Nullable SrosStatementTree node) {
    if (node == null || node.getChildren().size() != 1) {
      return null;
    }
    return node.getChildren().keySet().iterator().next();
  }

  /** The ordered, unquoted children of a leaf-list node (policy names), or empty. */
  private static @Nonnull List<String> policyNames(@Nullable SrosStatementTree leafListNode) {
    if (leafListNode == null) {
      return List.of();
    }
    return leafListNode.getChildren().keySet().stream()
        .map(SrosFeatureExtractor::unquote)
        .collect(java.util.stream.Collectors.toList());
  }

  private static @Nullable SrosStatementTree navigate(SrosStatementTree start, String... path) {
    SrosStatementTree node = start;
    for (String word : path) {
      node = node.getChild(word);
      if (node == null) {
        return null;
      }
    }
    return node;
  }

  private @Nullable Integer parseInt(@Nullable String text) {
    if (text == null) {
      return null;
    }
    try {
      return Integer.valueOf(text);
    } catch (NumberFormatException e) {
      _w.redFlagf("SR-OS: expected an integer but got '%s'", text);
      return null;
    }
  }

  private @Nullable Long parseLong(@Nullable String text) {
    if (text == null) {
      return null;
    }
    try {
      return Long.valueOf(text);
    } catch (NumberFormatException e) {
      _w.redFlagf("SR-OS: expected an integer but got '%s'", text);
      return null;
    }
  }

  private @Nullable Ip parseIp(@Nullable String text) {
    if (text == null) {
      return null;
    }
    try {
      return Ip.parse(text);
    } catch (IllegalArgumentException e) {
      _w.redFlagf("SR-OS: expected an IPv4 address but got '%s'", text);
      return null;
    }
  }

  private @Nullable Prefix parsePrefix(@Nullable String text) {
    if (text == null) {
      return null;
    }
    try {
      return Prefix.parse(text);
    } catch (IllegalArgumentException e) {
      _w.redFlagf("SR-OS: expected an IPv4 prefix but got '%s'", text);
      return null;
    }
  }

  private static @Nullable Boolean parseAdminState(@Nullable String text) {
    if (text == null) {
      return null;
    }
    switch (text) {
      case "enable":
        return Boolean.TRUE;
      case "disable":
        return Boolean.FALSE;
      default:
        return null;
    }
  }

  private static @Nullable PrefixListEntry.Type parsePrefixType(@Nullable String text) {
    if (text == null) {
      return null;
    }
    switch (text) {
      case "exact":
        return PrefixListEntry.Type.EXACT;
      case "longer":
        return PrefixListEntry.Type.LONGER;
      case "through":
        return PrefixListEntry.Type.THROUGH;
      case "range":
        return PrefixListEntry.Type.RANGE;
      case "to":
        return PrefixListEntry.Type.TO;
      case "address-mask":
        return PrefixListEntry.Type.ADDRESS_MASK;
      default:
        return null;
    }
  }

  private static @Nullable PolicyAction parseAction(@Nullable SrosStatementTree actionTypeNode) {
    String text = singleKey(actionTypeNode);
    if (text == null) {
      return null;
    }
    switch (text) {
      case "accept":
        return PolicyAction.ACCEPT;
      case "reject":
        return PolicyAction.REJECT;
      case "next-entry":
        return PolicyAction.NEXT_ENTRY;
      case "next-policy":
        return PolicyAction.NEXT_POLICY;
      default:
        return null;
    }
  }

  private static @Nonnull String unquote(String text) {
    if (text.length() >= 2 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
      return text.substring(1, text.length() - 1);
    }
    return text;
  }

  private final @Nonnull SrosConfiguration _c;
  private final @Nonnull Warnings _w;
}
