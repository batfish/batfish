package org.batfish.engine;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.engine.DeviceFacts.AclFacts;
import org.batfish.engine.DeviceFacts.AclLineFacts;
import org.batfish.engine.DeviceFacts.BgpFacts;
import org.batfish.engine.DeviceFacts.BgpPeerFacts;
import org.batfish.engine.DeviceFacts.ClauseFacts;
import org.batfish.engine.DeviceFacts.InterfaceFacts;
import org.batfish.engine.DeviceFacts.OspfAreaFacts;
import org.batfish.engine.DeviceFacts.OspfFacts;
import org.batfish.engine.DeviceFacts.OspfInterfaceFacts;
import org.batfish.engine.DeviceFacts.PolicyFacts;
import org.batfish.engine.DeviceFacts.SetFacts;
import org.batfish.engine.DeviceFacts.SetRef;
import org.batfish.engine.DeviceFacts.StaticRouteFacts;
import org.batfish.engine.DeviceFacts.VrfFacts;

/**
 * Lifts a parsed device into neutral {@link DeviceFacts}.
 *
 * <p>Interfaces, VRFs, routing processes and ACLs come from Batfish's model. Routing policy comes
 * from the structures the parser recorded, with their source lines: each policy and each clause is
 * shown as the device's own text, so the view needs no per-vendor code and never drifts from what
 * was parsed. The agreement check then confirms every policy in use was compiled.
 */
public final class Lifter {

  /** Facts for one device, and policy-agreement holes found while lifting it. */
  public record Lifted(@Nonnull DeviceFacts facts, @Nonnull List<CoverageHole> holes) {}

  private Lifter() {}

  public static @Nonnull Lifted lift(
      @Nonnull String node,
      @Nonnull Configuration c,
      @Nonnull String composedText,
      @Nonnull String file,
      @Nonnull ConvertConfigurationAnswerElement ccae) {
    String[] lines = composedText.split("\n", -1);
    SortedMap<String, SortedMap<String, DefinedStructureInfo>> defined =
        ccae.getDefinedStructures().getOrDefault(file, new TreeMap<>());
    SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> referenced =
        ccae.getReferencedStructures().getOrDefault(file, new TreeMap<>());

    List<CoverageHole> holes = new ArrayList<>();
    List<PolicyFacts> policies = new ArrayList<>();
    List<SetFacts> sets = new ArrayList<>();
    for (Map.Entry<String, SortedMap<String, DefinedStructureInfo>> byType : defined.entrySet()) {
      String type = byType.getKey();
      if (!CoverageHole.classify(type).equals(CoverageHole.POLICY) || isClauseType(type)) {
        continue;
      }
      for (var def : byType.getValue().entrySet()) {
        String name = def.getKey();
        List<String> text = text(lines, def.getValue().getDefinitionLines().enumerate());
        if (isSetType(type)) {
          sets.add(new SetFacts(type, name, text));
          continue;
        }
        List<String> usedBy = usages(referenced, type, name);
        boolean compiled = c.getRoutingPolicies().containsKey(name);
        if (!compiled && !usedBy.isEmpty()) {
          holes.add(
              new CoverageHole(
                  node,
                  CoverageHole.POLICY,
                  "agreement",
                  null,
                  type + " " + name + " is in use but has no compiled policy"));
        }
        policies.add(
            new PolicyFacts(
                type,
                name,
                text,
                clauses(lines, defined, referenced, type, name),
                usedBy,
                compiled));
      }
    }

    DeviceFacts facts =
        new DeviceFacts(
            node,
            c.getConfigurationFormat().name(),
            interfaces(c),
            vrfs(c),
            acls(c),
            ImmutableList.copyOf(policies),
            ImmutableList.copyOf(sets),
            vnis(c));
    return new Lifted(facts, ImmutableList.copyOf(holes));
  }

  /** Clause structures are named after their parent: "route-map entry", "policy-statement term". */
  private static boolean isClauseType(String type) {
    return type.endsWith(" entry")
        || type.endsWith(" term")
        || type.endsWith(" clause")
        || type.endsWith(" line");
  }

  private static boolean isSetType(String type) {
    String t = type.toLowerCase(Locale.ROOT);
    return t.contains("prefix-list")
        || t.contains("community")
        || t.contains("as-path")
        || t.contains("prefix-set")
        || t.contains("route-filter");
  }

  private static List<ClauseFacts> clauses(
      String[] lines,
      SortedMap<String, SortedMap<String, DefinedStructureInfo>> defined,
      SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> referenced,
      String parentType,
      String parentName) {
    List<ClauseFacts> clauses = new ArrayList<>();
    for (var byType : defined.entrySet()) {
      if (!isClauseType(byType.getKey()) || !byType.getKey().startsWith(parentType + " ")) {
        continue;
      }
      for (var def : byType.getValue().entrySet()) {
        if (!def.getKey().startsWith(parentName + " ")) {
          continue;
        }
        SortedSet<Integer> clauseLines = def.getValue().getDefinitionLines().enumerate();
        clauses.add(
            new ClauseFacts(
                def.getKey(), text(lines, clauseLines), references(referenced, clauseLines)));
      }
    }
    clauses.sort(Comparator.comparingInt(cl -> firstLine(defined, cl.name())));
    return ImmutableList.copyOf(clauses);
  }

  private static int firstLine(
      SortedMap<String, SortedMap<String, DefinedStructureInfo>> defined, String clauseName) {
    for (var byType : defined.values()) {
      var info = byType.get(clauseName);
      if (info != null && !info.getDefinitionLines().isEmpty()) {
        return info.getDefinitionLines().enumerate().first();
      }
    }
    return Integer.MAX_VALUE;
  }

  /** The sets referenced from within {@code clauseLines}. */
  private static List<SetRef> references(
      SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> referenced,
      SortedSet<Integer> clauseLines) {
    List<SetRef> refs = new ArrayList<>();
    referenced.forEach(
        (type, byName) -> {
          if (!isSetType(type)) {
            return;
          }
          byName.forEach(
              (name, byUsage) -> {
                boolean inClause =
                    byUsage.values().stream()
                        .flatMap(SortedSet::stream)
                        .anyMatch(clauseLines::contains);
                if (inClause) {
                  refs.add(new SetRef(type, name));
                }
              });
        });
    return ImmutableList.copyOf(refs);
  }

  private static List<String> usages(
      SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> referenced,
      String type,
      String name) {
    var byName = referenced.get(type);
    if (byName == null || !byName.containsKey(name)) {
      return ImmutableList.of();
    }
    return byName.get(name).keySet().stream()
        .filter(usage -> !usage.contains("self-reference") && !usage.contains("self ref"))
        .sorted()
        .collect(ImmutableList.toImmutableList());
  }

  /** Lines are 1-based line numbers in the composed input. */
  private static List<String> text(String[] lines, SortedSet<Integer> numbers) {
    ImmutableList.Builder<String> b = ImmutableList.builder();
    for (int n : numbers) {
      if (n >= 1 && n <= lines.length) {
        b.add(lines[n - 1]);
      }
    }
    return b.build();
  }

  private static List<DeviceFacts.VniFacts> vnis(Configuration c) {
    List<DeviceFacts.VniFacts> rows = new ArrayList<>();
    c.getVrfs()
        .values()
        .forEach(
            vrf -> {
              vrf.getLayer2Vnis()
                  .values()
                  .forEach(
                      vni ->
                          rows.add(
                              new DeviceFacts.VniFacts(
                                  vni.getVni(),
                                  "l2",
                                  vrf.getName(),
                                  vni.getSrcVrf(),
                                  vni.getSourceAddress() == null
                                      ? null
                                      : vni.getSourceAddress().toString(),
                                  vni.getUdpPort(),
                                  vni.getVlan(),
                                  vni.getBumTransportMethod().name(),
                                  vni.getBumTransportIps().stream()
                                      .map(Object::toString)
                                      .sorted()
                                      .toList())));
              vrf.getLayer3Vnis()
                  .values()
                  .forEach(
                      vni ->
                          rows.add(
                              new DeviceFacts.VniFacts(
                                  vni.getVni(),
                                  "l3",
                                  vrf.getName(),
                                  vni.getSrcVrf(),
                                  vni.getSourceAddress() == null
                                      ? null
                                      : vni.getSourceAddress().toString(),
                                  vni.getUdpPort(),
                                  null,
                                  null,
                                  List.of())));
            });
    return rows.stream()
        .sorted(
            Comparator.comparing(DeviceFacts.VniFacts::vrf)
                .thenComparing(DeviceFacts.VniFacts::layer)
                .thenComparingInt(DeviceFacts.VniFacts::vni))
        .toList();
  }

  private static List<InterfaceFacts> interfaces(Configuration c) {
    return c.getAllInterfaces().values().stream()
        .sorted(Comparator.comparing(Interface::getName))
        .map(Lifter::interfaceFacts)
        .collect(ImmutableList.toImmutableList());
  }

  private static InterfaceFacts interfaceFacts(Interface i) {
    OspfInterfaceSettings o = i.getOspfSettings();
    return new InterfaceFacts(
        i.getName(),
        i.getInterfaceType().name(),
        i.getVrfName(),
        i.getAdminUp(),
        i.getActive(),
        i.getAllConcreteAddresses().stream()
            .map(Object::toString)
            .sorted()
            .collect(ImmutableList.toImmutableList()),
        i.getMtu(),
        i.getBandwidth(),
        i.getSwitchportMode().name(),
        i.getAccessVlan(),
        i.getAllowedVlans().isEmpty() ? null : i.getAllowedVlans().toString(),
        i.getNativeVlan(),
        i.getVlan(),
        i.getChannelGroup(),
        o == null
            ? null
            : new OspfInterfaceFacts(
                o.getProcess(),
                o.getAreaName(),
                o.getCost(),
                o.getNetworkType() == null ? null : o.getNetworkType().name(),
                o.getPassive(),
                o.getHelloInterval(),
                o.getDeadInterval()),
        nameOf(i.getIncomingFilter()),
        nameOf(i.getOutgoingFilter()),
        i.getIncomingTransformation() != null || i.getOutgoingTransformation() != null);
  }

  private static @Nullable String nameOf(@Nullable IpAccessList acl) {
    return acl == null ? null : acl.getName();
  }

  private static List<VrfFacts> vrfs(Configuration c) {
    return c.getVrfs().values().stream()
        .sorted(Comparator.comparing(Vrf::getName))
        .map(
            v ->
                new VrfFacts(
                    v.getName(),
                    v.getStaticRoutes().stream()
                        .map(Lifter::staticRoute)
                        .sorted(Comparator.comparing(StaticRouteFacts::prefix))
                        .collect(ImmutableList.toImmutableList()),
                    bgp(v.getBgpProcess()),
                    v.getOspfProcesses().values().stream()
                        .sorted(Comparator.comparing(OspfProcess::getProcessId))
                        .map(Lifter::ospf)
                        .collect(ImmutableList.toImmutableList())))
        .collect(ImmutableList.toImmutableList());
  }

  private static StaticRouteFacts staticRoute(StaticRoute r) {
    String iface = r.getNextHopInterface();
    return new StaticRouteFacts(
        r.getNetwork().toString(),
        r.getNextHopIp() == null ? null : r.getNextHopIp().toString(),
        iface == null || iface.isEmpty() || iface.equals("dynamic") ? null : iface,
        r.getAdministrativeCost(),
        r.getTag());
  }

  private static @Nullable BgpFacts bgp(@Nullable BgpProcess p) {
    if (p == null) {
      return null;
    }
    List<BgpPeerFacts> peers = new ArrayList<>();
    p.getActiveNeighbors().forEach((ip, peer) -> peers.add(peer(ip.toString(), "address", peer)));
    p.getPassiveNeighbors()
        .forEach((prefix, peer) -> peers.add(peer(prefix.toString(), "passive", peer)));
    p.getInterfaceNeighbors().forEach((iface, peer) -> peers.add(peer(iface, "interface", peer)));
    peers.sort(Comparator.comparing(BgpPeerFacts::peer));
    return new BgpFacts(p.getRouterId().toString(), ImmutableList.copyOf(peers));
  }

  private static BgpPeerFacts peer(String id, String kind, BgpPeerConfig peer) {
    ImmutableList.Builder<String> families = ImmutableList.builder();
    AddressFamily v4 = peer.getIpv4UnicastAddressFamily();
    if (v4 != null) {
      families.add("ipv4-unicast");
    }
    if (peer.getEvpnAddressFamily() != null) {
      families.add("evpn");
    }
    return new BgpPeerFacts(
        id,
        kind,
        peer.getLocalAs(),
        peer.getRemoteAsns().toString(),
        peer.getLocalIp() == null ? null : peer.getLocalIp().toString(),
        families.build(),
        v4 == null ? null : v4.getImportPolicy(),
        v4 == null ? null : v4.getExportPolicy(),
        peer.getDescription());
  }

  private static OspfFacts ospf(OspfProcess p) {
    return new OspfFacts(
        p.getProcessId(),
        p.getRouterId().toString(),
        p.getReferenceBandwidth(),
        p.getAreas().values().stream()
            .map(
                (OspfArea a) ->
                    new OspfAreaFacts(
                        a.getAreaNumber(),
                        a.getStubType().name(),
                        ImmutableList.copyOf(a.getInterfaces())))
            .collect(ImmutableList.toImmutableList()));
  }

  private static List<AclFacts> acls(Configuration c) {
    return c.getIpAccessLists().values().stream()
        .filter(acl -> !acl.getName().startsWith("~"))
        .sorted(Comparator.comparing(IpAccessList::getName))
        .map(
            acl ->
                new AclFacts(
                    acl.getName(),
                    acl.getLines().stream()
                        .map(Lifter::aclLine)
                        .collect(ImmutableList.toImmutableList())))
        .collect(ImmutableList.toImmutableList());
  }

  private static AclLineFacts aclLine(AclLine line) {
    String action =
        line instanceof ExprAclLine e ? e.getAction().name().toLowerCase(Locale.ROOT) : null;
    return new AclLineFacts(line.getName(), action);
  }
}
