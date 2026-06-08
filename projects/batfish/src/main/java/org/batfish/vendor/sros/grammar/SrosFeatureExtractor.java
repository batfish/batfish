package org.batfish.vendor.sros.grammar;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.vendor.StructureType;
import org.batfish.vendor.StructureUsage;
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
import org.batfish.vendor.sros.representation.SrosStructureType;
import org.batfish.vendor.sros.representation.SrosStructureUsage;

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
 *
 * <p>Because each tree node carries the parse-tree context of the statement(s) that created it (see
 * {@link SrosStatementTree#getDefContexts}), the extractor emits line-stamped {@link
 * Warnings.ParseWarning}s for malformed or out-of-range values (via {@link #warn}) and records
 * structure definitions and references (via {@link #defineStructure}/{@link #referenceStructure}),
 * driving the {@code definedStructures}/{@code undefinedReferences}/unused-structure questions and
 * the {@code annotate} tool. A bad value with no associated context (should not happen for parsed
 * input) degrades to a context-free red-flag warning.
 */
@ParametersAreNonnullByDefault
public final class SrosFeatureExtractor {

  /** AS number: YANG {@code autonomous-system} is {@code uint32 range "1..max"}. */
  private static final LongSpace AUTONOMOUS_SYSTEM_SPACE =
      LongSpace.of(Range.closed(1L, 4294967295L));

  /** IPv4 prefix length: YANG {@code prefix-length} under a router interface is {@code 0..32}. */
  private static final IntegerSpace IPV4_PREFIX_LENGTH_SPACE = IntegerSpace.of(new SubRange(0, 32));

  /** Line-card and MDA slot numbers (YANG {@code uint32}; constrained to a sane positive range). */
  private static final IntegerSpace SLOT_SPACE = IntegerSpace.of(new SubRange(1, 255));

  /** policy-statement {@code entry-id}: YANG {@code uint32 range "1..65535"}. */
  private static final LongSpace ENTRY_ID_SPACE = LongSpace.of(Range.closed(1L, 65535L));

  /** Port {@code admin-state} enumeration: enable -> up, disable -> down. */
  private static final Map<String, Boolean> ADMIN_STATE =
      ImmutableMap.of("enable", Boolean.TRUE, "disable", Boolean.FALSE);

  /** prefix-list entry {@code type} enumeration. */
  private static final Map<String, PrefixListEntry.Type> PREFIX_LIST_TYPE =
      ImmutableMap.<String, PrefixListEntry.Type>builder()
          .put("exact", PrefixListEntry.Type.EXACT)
          .put("longer", PrefixListEntry.Type.LONGER)
          .put("through", PrefixListEntry.Type.THROUGH)
          .put("range", PrefixListEntry.Type.RANGE)
          .put("to", PrefixListEntry.Type.TO)
          .put("address-mask", PrefixListEntry.Type.ADDRESS_MASK)
          .build();

  /** policy-statement entry/default {@code action-type} enumeration. */
  private static final Map<String, PolicyAction> POLICY_ACTION =
      ImmutableMap.<String, PolicyAction>builder()
          .put("accept", PolicyAction.ACCEPT)
          .put("reject", PolicyAction.REJECT)
          .put("next-entry", PolicyAction.NEXT_ENTRY)
          .put("next-policy", PolicyAction.NEXT_POLICY)
          .build();

  public static void extract(
      SrosStatementTree root,
      SrosConfiguration c,
      Warnings w,
      SrosCombinedParser parser,
      String text) {
    new SrosFeatureExtractor(c, w, parser, text).extractFrom(root);
  }

  private SrosFeatureExtractor(
      SrosConfiguration c, Warnings w, SrosCombinedParser parser, String text) {
    _c = c;
    _w = w;
    _parser = parser;
    _text = text;
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
      SrosStatementTree cardNode = e.getValue();
      Optional<Integer> slot = toIntegerInSpace(e.getKey(), cardNode, SLOT_SPACE, "card slot");
      if (slot.isEmpty()) {
        continue;
      }
      Card card = new Card(slot.get());
      card.setCardType(singleValue(cardNode, "card-type"));
      SrosStatementTree mdaList = cardNode.getChild("mda");
      if (mdaList != null) {
        for (Map.Entry<String, SrosStatementTree> me : mdaList.getChildren().entrySet()) {
          SrosStatementTree mdaNode = me.getValue();
          Optional<Integer> mdaSlot =
              toIntegerInSpace(me.getKey(), mdaNode, SLOT_SPACE, "mda slot");
          if (mdaSlot.isEmpty()) {
            continue;
          }
          Mda mda = new Mda(mdaSlot.get());
          mda.setMdaType(singleValue(mdaNode, "mda-type"));
          card.getMdas().put(mdaSlot.get(), mda);
        }
      }
      _c.getCards().put(slot.get(), card);
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
      port.setAdminStateEnable(
          toEnum(portNode.getChild("admin-state"), ADMIN_STATE, "admin-state"));
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
      toLongInSpace(
              singleValue(routerNode, "autonomous-system"),
              singleValueNode(routerNode, "autonomous-system"),
              AUTONOMOUS_SYSTEM_SPACE,
              "autonomous-system")
          .ifPresent(router::setAutonomousSystem);
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
        iface.setPrimaryAddress(
            parseIp(
                singleValue(primary, "address"), singleValueNode(primary, "address"), "interface"));
        toIntegerInSpace(
                singleValue(primary, "prefix-length"),
                singleValueNode(primary, "prefix-length"),
                IPV4_PREFIX_LENGTH_SPACE,
                "prefix-length")
            .ifPresent(iface::setPrimaryPrefixLength);
      }
      router.getInterfaces().put(name, iface);
    }
  }

  private void extractBgp(Router router, @Nullable SrosStatementTree bgpNode) {
    if (bgpNode == null) {
      return;
    }
    BgpProcess proc = new BgpProcess();
    proc.setRouterId(
        parseIp(
            singleValue(bgpNode, "router-id"), singleValueNode(bgpNode, "router-id"), "router-id"));

    SrosStatementTree groupList = bgpNode.getChild("group");
    if (groupList != null) {
      for (Map.Entry<String, SrosStatementTree> e : groupList.getChildren().entrySet()) {
        String name = unquote(e.getKey());
        SrosStatementTree groupNode = e.getValue();
        defineStructure(SrosStructureType.BGP_GROUP, name, groupNode);
        BgpGroup group = new BgpGroup(name);
        toLongInSpace(
                singleValue(groupNode, "peer-as"),
                singleValueNode(groupNode, "peer-as"),
                AUTONOMOUS_SYSTEM_SPACE,
                "peer-as")
            .ifPresent(group::setPeerAs);
        SrosStatementTree groupImport = navigate(groupNode, "import", "policy");
        SrosStatementTree groupExport = navigate(groupNode, "export", "policy");
        group.getImportPolicies().addAll(policyNames(groupImport));
        group.getExportPolicies().addAll(policyNames(groupExport));
        referencePolicies(groupImport, SrosStructureUsage.BGP_GROUP_IMPORT_POLICY);
        referencePolicies(groupExport, SrosStructureUsage.BGP_GROUP_EXPORT_POLICY);
        proc.getGroups().put(name, group);
      }
    }

    SrosStatementTree neighborList = bgpNode.getChild("neighbor");
    if (neighborList != null) {
      for (Map.Entry<String, SrosStatementTree> e : neighborList.getChildren().entrySet()) {
        String ip = unquote(e.getKey());
        SrosStatementTree nbrNode = e.getValue();
        BgpNeighbor neighbor = new BgpNeighbor(ip);
        SrosStatementTree groupValue = singleValueNode(nbrNode, "group");
        String group = singleValue(nbrNode, "group");
        if (group != null) {
          neighbor.setGroup(unquote(group));
          referenceStructure(
              SrosStructureType.BGP_GROUP,
              unquote(group),
              SrosStructureUsage.BGP_NEIGHBOR_GROUP,
              groupValue);
        }
        toLongInSpace(
                singleValue(nbrNode, "peer-as"),
                singleValueNode(nbrNode, "peer-as"),
                AUTONOMOUS_SYSTEM_SPACE,
                "peer-as")
            .ifPresent(neighbor::setPeerAs);
        SrosStatementTree nbrImport = navigate(nbrNode, "import", "policy");
        SrosStatementTree nbrExport = navigate(nbrNode, "export", "policy");
        neighbor.getImportPolicies().addAll(policyNames(nbrImport));
        neighbor.getExportPolicies().addAll(policyNames(nbrExport));
        referencePolicies(nbrImport, SrosStructureUsage.BGP_NEIGHBOR_IMPORT_POLICY);
        referencePolicies(nbrExport, SrosStructureUsage.BGP_NEIGHBOR_EXPORT_POLICY);
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
        SrosStatementTree plNode = e.getValue();
        defineStructure(SrosStructureType.PREFIX_LIST, name, plNode);
        PrefixList pl = new PrefixList(name);
        SrosStatementTree prefixNode = plNode.getChild("prefix");
        if (prefixNode != null) {
          for (Map.Entry<String, SrosStatementTree> pe : prefixNode.getChildren().entrySet()) {
            Prefix prefix = parsePrefix(pe.getKey(), pe.getValue());
            if (prefix == null) {
              continue;
            }
            PrefixListEntry.Type type =
                toEnum(pe.getValue().getChild("type"), PREFIX_LIST_TYPE, "prefix-list match type");
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
        defineStructure(SrosStructureType.POLICY_STATEMENT, name, psNode);
        PolicyStatement ps = new PolicyStatement(name);
        SrosStatementTree entryList = psNode.getChild("entry");
        if (entryList != null) {
          for (Map.Entry<String, SrosStatementTree> ee : entryList.getChildren().entrySet()) {
            SrosStatementTree entryNode = ee.getValue();
            Optional<Long> entryId =
                toLongInSpace(ee.getKey(), entryNode, ENTRY_ID_SPACE, "policy entry-id");
            if (entryId.isEmpty()) {
              continue;
            }
            PolicyStatementEntry entry = new PolicyStatementEntry(entryId.get());
            SrosStatementTree fromPfx = navigate(entryNode, "from", "prefix-list");
            entry.getFromPrefixLists().addAll(policyNames(fromPfx));
            referenceStructures(
                SrosStructureType.PREFIX_LIST,
                fromPfx,
                SrosStructureUsage.POLICY_STATEMENT_FROM_PREFIX_LIST);
            entry.setAction(
                toEnum(navigate(entryNode, "action", "action-type"), POLICY_ACTION, "action-type"));
            ps.getEntries().put(entryId.get(), entry);
          }
        }
        ps.setDefaultAction(
            toEnum(
                navigate(psNode, "default-action", "action-type"), POLICY_ACTION, "action-type"));
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

  /**
   * The value node of a single-valued leaf — i.e. the (single) child of {@code node}'s {@code leaf}
   * child. For {@code autonomous-system 5000000000} under a router node, this is the {@code
   * 5000000000} node, which is the deepest node of that statement and so carries its source context
   * (the leaf-name {@code autonomous-system} node, being an interior path word, does not). Returns
   * {@code null} if the leaf is absent or not single-valued, in which case warnings degrade to a
   * context-free red-flag.
   */
  private static @Nullable SrosStatementTree singleValueNode(SrosStatementTree node, String leaf) {
    SrosStatementTree leafNode = node.getChild(leaf);
    if (leafNode == null || leafNode.getChildren().size() != 1) {
      return null;
    }
    return leafNode.getChildren().values().iterator().next();
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

  /**
   * Convert an integer leaf value to an {@link Integer} if it parses as a 32-bit decimal and is
   * contained in {@code space}, else {@link Optional#empty} (with a {@link Warnings.ParseWarning}).
   *
   * <p>Unlike the equivalent helpers in grammar-driven extractors (e.g. flatjuniper's {@code
   * toIntegerInSpace}), the value is NOT grammar-guaranteed numeric — every SR-OS leaf value is a
   * generic word — so a malformed value is warned here rather than assumed away. {@code ctxNode} is
   * the tree node whose source context locates the value for the warning; {@code name} names it. A
   * {@code null} input is {@link Optional#empty} with no warning (absent leaf == YANG default).
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      @Nullable String text, @Nullable SrosStatementTree ctxNode, IntegerSpace space, String name) {
    if (text == null) {
      return Optional.empty();
    }
    int num;
    try {
      num = Integer.parseInt(text);
    } catch (NumberFormatException e) {
      warn(ctxNode, String.format("Expected %s in range %s, but got '%s'", name, space, text));
      return Optional.empty();
    }
    if (!space.contains(num)) {
      warn(ctxNode, String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  /**
   * Convert an integer leaf value to a {@link Long} if it parses as a 64-bit decimal and is
   * contained in {@code space}, else {@link Optional#empty} (with a {@link Warnings.ParseWarning}).
   * See {@link #toIntegerInSpace} for why malformed input is warned rather than assumed away.
   */
  private @Nonnull Optional<Long> toLongInSpace(
      @Nullable String text, @Nullable SrosStatementTree ctxNode, LongSpace space, String name) {
    if (text == null) {
      return Optional.empty();
    }
    long num;
    try {
      num = Long.parseLong(text);
    } catch (NumberFormatException e) {
      warn(ctxNode, String.format("Expected %s in range %s, but got '%s'", name, space, text));
      return Optional.empty();
    }
    if (!space.contains(num)) {
      warn(ctxNode, String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  private @Nullable Ip parseIp(
      @Nullable String text, @Nullable SrosStatementTree ctxNode, String what) {
    if (text == null) {
      return null;
    }
    try {
      return Ip.parse(text);
    } catch (IllegalArgumentException e) {
      warn(ctxNode, String.format("SR-OS: expected an IPv4 address %s but got '%s'", what, text));
      return null;
    }
  }

  private @Nullable Prefix parsePrefix(@Nullable String text, @Nullable SrosStatementTree ctxNode) {
    if (text == null) {
      return null;
    }
    try {
      return Prefix.parse(text);
    } catch (IllegalArgumentException e) {
      warn(ctxNode, String.format("SR-OS: expected an IPv4 prefix but got '%s'", text));
      return null;
    }
  }

  /**
   * Resolve the single value of {@code leafNode} (an enumerated leaf, e.g. {@code admin-state} or
   * {@code action-type}) against {@code values}. Returns {@code null} with no warning if the leaf
   * is absent or not single-valued (absent leaf == YANG default), and {@code null} with a
   * line-stamped {@link Warnings.ParseWarning} ({@code what} names the leaf) if the value is
   * outside the enumeration. The value node carries the source context, so the warning points at
   * the value.
   */
  private <T> @Nullable T toEnum(
      @Nullable SrosStatementTree leafNode, Map<String, T> values, String what) {
    String value = singleKey(leafNode);
    if (value == null) {
      return null;
    }
    T result = values.get(value);
    if (result == null) {
      // leafNode is single-valued (singleKey returned non-null), so its one child is the value
      // node.
      SrosStatementTree valueNode = leafNode.getChildren().values().iterator().next();
      warn(valueNode, String.format("SR-OS: unrecognized %s '%s'", what, value));
    }
    return result;
  }

  private static @Nonnull String unquote(String text) {
    if (text.length() >= 2 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
      return text.substring(1, text.length() - 1);
    }
    return text;
  }

  // --- warning + structure helpers --------------------------------------------------------------

  /**
   * Emit a line-stamped {@link Warnings.ParseWarning} for the statement that created {@code
   * ctxNode} (the same channel as the parser's unrecognized-line warnings, so it is
   * annotate-visible and carries a source line). If the node has no source context (should not
   * happen for a parsed value), fall back to a context-free red-flag so the warning is never
   * silently lost.
   */
  private void warn(@Nullable SrosStatementTree ctxNode, String message) {
    ParserRuleContext ctx = ctxNode == null ? null : ctxNode.firstDefContext();
    if (ctx == null) {
      _w.redFlag(message);
      return;
    }
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    String fullText = _text.substring(start, end + 1);
    _w.addWarning(ctx, fullText, _parser, message);
  }

  /**
   * Record a structure definition on every source context of {@code defNode} (so a structure
   * configured by more than one statement — e.g. mixed brace + flat — accumulates all its lines).
   */
  private void defineStructure(StructureType type, String name, SrosStatementTree defNode) {
    for (ParserRuleContext ctx : defNode.getDefContexts()) {
      _c.defineStructure(type, name, ctx);
    }
  }

  /** Record one reference to {@code name} on the line of {@code refNode}'s source context. */
  private void referenceStructure(
      StructureType type, String name, StructureUsage usage, @Nullable SrosStatementTree refNode) {
    ParserRuleContext ctx = refNode == null ? null : refNode.firstDefContext();
    if (ctx == null) {
      return;
    }
    _c.referenceStructure(type, name, usage, _parser.getLine(ctx.getStart()));
  }

  /**
   * Record one reference per (unquoted) child of a leaf-list node {@code refNode}, all at that
   * node's line. Used for the {@code policy [a b]} import/export lists and {@code from
   * prefix-list}.
   */
  private void referenceStructures(
      StructureType type, @Nullable SrosStatementTree refNode, StructureUsage usage) {
    if (refNode == null) {
      return;
    }
    for (String child : refNode.getChildren().keySet()) {
      referenceStructure(type, unquote(child), usage, refNode);
    }
  }

  /** Reference each policy name in an import/export {@code policy [..]} leaf-list. */
  private void referencePolicies(@Nullable SrosStatementTree policyLeafList, StructureUsage usage) {
    referenceStructures(SrosStructureType.POLICY_STATEMENT, policyLeafList, usage);
  }

  private final @Nonnull SrosConfiguration _c;
  private final @Nonnull Warnings _w;
  private final @Nonnull SrosCombinedParser _parser;
  private final @Nonnull String _text;
}
