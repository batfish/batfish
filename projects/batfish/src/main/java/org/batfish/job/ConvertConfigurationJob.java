package org.batfish.job;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.naturalOrder;
import static org.batfish.common.bdd.util.AclPacketMatchValidityChecker.checkerFor;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.addTenantVniInterfaces;
import static org.batfish.vendor.ConversionContext.EMPTY_CONVERSION_CONTEXT;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.util.AclPacketMatchValidityChecker;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.common.util.InterfaceNameComparator;
import org.batfish.config.Settings;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Mlag;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchDestinationIp;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSourceIp;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.bgp.community.CommunityStructuresVerifier;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.hsrp.HsrpGroup;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVisitor;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.batfish.datamodel.route.nh.NextHopVtep;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.AsPathStructuresVerifier;
import org.batfish.datamodel.tracking.TrackAction;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;
import org.batfish.main.Batfish;
import org.batfish.representation.host.HostConfiguration;
import org.batfish.representation.iptables.IptablesVendorConfiguration;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.VendorStructureId;

public class ConvertConfigurationJob extends BatfishJob<ConvertConfigurationResult> {

  private final Object _configObject;
  private final @Nonnull ConversionContext _conversionContext;
  private final @Nonnull SnapshotRuntimeData _runtimeData;
  private final String _name;

  public ConvertConfigurationJob(
      Settings settings,
      @Nullable ConversionContext conversionContext,
      @Nullable SnapshotRuntimeData runtimeData,
      Object configObject,
      String name) {
    super(settings);
    _configObject = configObject;
    _conversionContext = firstNonNull(conversionContext, EMPTY_CONVERSION_CONTEXT);
    _runtimeData = firstNonNull(runtimeData, SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA);
    _name = name;
  }

  /**
   * Sanity checks the given map from name-of-thing to thing-with-name for name consistency. If the
   * names are not consistent, warns and does not convert them.
   *
   * <p>The created maps are sorted by key in ascending order.
   *
   * <p>Hopefully this will only happen during new parser development, and will help authors of
   * those new parsers get it right.
   */
  private static <T> ImmutableMap<String, T> verifyAndToImmutableMap(
      @Nullable Map<String, T> map, Function<T, String> keyFn, Warnings w) {
    return verifyAndToImmutableMap(map, keyFn, w, naturalOrder());
  }

  /**
   * Sanity checks the given map from name-of-thing to thing-with-name for name consistency. If the
   * names are not consistent, warns and does not convert them.
   *
   * <p>The created maps are sorted by key in ascending order using the given comparator.
   *
   * <p>Hopefully this will only happen during new parser development, and will help authors of
   * those new parsers get it right.
   */
  private static <T> ImmutableMap<String, T> verifyAndToImmutableMap(
      @Nullable Map<String, T> map,
      Function<T, String> keyFn,
      Warnings w,
      Comparator<String> comparator) {
    if (map == null || map.isEmpty()) {
      return ImmutableMap.of();
    }
    return map.entrySet().stream()
        .filter(
            e -> {
              String key = keyFn.apply(e.getValue());
              if (key.equals(e.getKey())) {
                return true;
              }
              w.redFlag(
                  String.format(
                      "Batfish internal error: invalid entry %s -> %s named %s",
                      e.getKey(), e.getValue(), key));
              return false;
            })
        .sorted(Entry.comparingByKey(comparator)) /* ImmutableMap is insert ordered. */
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Converts {@link Map} to {@link ImmutableMap}. The created maps are sorted by key in ascending
   * order.
   */
  private static <T> ImmutableMap<String, T> toImmutableMap(@Nullable Map<String, T> map) {
    if (map == null || map.isEmpty()) {
      return ImmutableMap.of();
    }
    return map.entrySet().stream()
        .sorted(Entry.comparingByKey()) /* ImmutableMap is insert ordered. */
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Converts {@link Set} to {@link ImmutableSet}. The order of the returned set is the iteration
   * order of the given set.
   */
  private static <T> ImmutableSet<T> toImmutableSet(@Nullable Set<T> set) {
    if (set == null || set.isEmpty()) {
      return ImmutableSet.of();
    }
    return ImmutableSet.copyOf(set);
  }

  @VisibleForTesting
  @ParametersAreNonnullByDefault
  static final class CollectIpSpaceReferences
      implements GenericIpSpaceVisitor<Void>,
          GenericAclLineVisitor<Void>,
          GenericAclLineMatchExprVisitor<Void> {
    /** Returns {@link IpSpaceReference#getName()} for all {@link IpSpaceReference} in {@code c}. */
    public static Set<String> collect(Configuration c) {
      ImmutableSet.Builder<String> set = ImmutableSet.builder();
      CollectIpSpaceReferences collector = new CollectIpSpaceReferences(c, set);
      for (IpSpace space : c.getIpSpaces().values()) {
        collector.visit(space);
      }
      for (IpAccessList acl : c.getIpAccessLists().values()) {
        collector.visit(acl);
      }
      for (Interface i : c.getAllInterfaces().values()) {
        collector.visit(i.getIncomingTransformation());
        collector.visit(i.getOutgoingTransformation());
      }
      return set.build();
    }

    private final @Nonnull Configuration _c;
    private final @Nonnull ImmutableSet.Builder<String> _set;
    private final @Nonnull Set<Object> _visited;

    private CollectIpSpaceReferences(Configuration c, ImmutableSet.Builder<String> set) {
      _c = c;
      _set = set;
      _visited = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    // Impl below here.

    private void visit(@Nullable Transformation t) {
      // to avoid stack overflow on large transformations, use a work queue instead of recursion
      Queue<Transformation> queue = new LinkedList<>();
      Consumer<Transformation> enqueue =
          tx -> {
            if (tx != null && _visited.add(tx)) {
              queue.add(tx);
            }
          };
      enqueue.accept(t);
      while (!queue.isEmpty()) {
        Transformation tx = queue.remove();
        visit(tx.getGuard());
        enqueue.accept(tx.getAndThen());
        enqueue.accept(tx.getOrElse());
      }
    }

    private void visit(IpAccessList acl) {
      if (!_visited.add(acl)) {
        return;
      }
      for (AclLine line : acl.getLines()) {
        visit(line);
      }
    }

    private void visitAclNamed(String name) {
      IpAccessList acl = _c.getIpAccessLists().get(name);
      if (acl == null) {
        return;
      }
      visit(acl);
    }

    @Override
    public Void visit(AclLineMatchExpr expr) {
      if (!_visited.add(expr)) {
        return null;
      }
      return GenericAclLineMatchExprVisitor.super.visit(expr);
    }

    @Override
    public Void visit(AclLine line) {
      if (!_visited.add(line)) {
        return null;
      }
      return GenericAclLineVisitor.super.visit(line);
    }

    @Override
    public Void visit(IpSpace ipSpace) {
      if (!_visited.add(ipSpace)) {
        return null;
      }
      return GenericIpSpaceVisitor.super.visit(ipSpace);
    }

    @Override
    public Void visitAclIpSpace(AclIpSpace aclIpSpace) {
      aclIpSpace.getLines().stream().map(AclIpSpaceLine::getIpSpace).forEach(this::visit);
      return null;
    }

    @Override
    public Void visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
      return null;
    }

    @Override
    public Void visitIpIpSpace(IpIpSpace ipIpSpace) {
      return null;
    }

    @Override
    public Void visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
      _set.add(ipSpaceReference.getName());
      return null;
    }

    @Override
    public Void visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
      return null;
    }

    @Override
    public Void visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
      return null;
    }

    @Override
    public Void visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
      return null;
    }

    @Override
    public Void visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
      return null;
    }

    @Override
    public Void visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      andMatchExpr.getConjuncts().forEach(this::visit);
      return null;
    }

    @Override
    public Void visitDeniedByAcl(DeniedByAcl deniedByAcl) {
      visitAclNamed(deniedByAcl.getAclName());
      return null;
    }

    @Override
    public Void visitFalseExpr(FalseExpr falseExpr) {
      return null;
    }

    @Override
    public Void visitMatchDestinationIp(MatchDestinationIp matchDestinationIp) {
      visit(matchDestinationIp.getIps());
      return null;
    }

    @Override
    public Void visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      HeaderSpace hs = matchHeaderSpace.getHeaderspace();
      if (hs == null) {
        return null;
      }
      visit(firstNonNull(hs.getDstIps(), EmptyIpSpace.INSTANCE));
      visit(firstNonNull(hs.getNotDstIps(), EmptyIpSpace.INSTANCE));
      visit(firstNonNull(hs.getSrcIps(), EmptyIpSpace.INSTANCE));
      visit(firstNonNull(hs.getNotSrcIps(), EmptyIpSpace.INSTANCE));
      return null;
    }

    @Override
    public Void visitMatchSourceIp(MatchSourceIp matchSourceIp) {
      visit(matchSourceIp.getIps());
      return null;
    }

    @Override
    public Void visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      return null;
    }

    @Override
    public Void visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      visit(notMatchExpr.getOperand());
      return null;
    }

    @Override
    public Void visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      return null;
    }

    @Override
    public Void visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      orMatchExpr.getDisjuncts().forEach(this::visit);
      return null;
    }

    @Override
    public Void visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      visitAclNamed(permittedByAcl.getAclName());
      return null;
    }

    @Override
    public Void visitTrueExpr(TrueExpr trueExpr) {
      return null;
    }

    @Override
    public Void visitAclAclLine(AclAclLine aclAclLine) {
      visitAclNamed(aclAclLine.getAclName());
      return null;
    }

    @Override
    public Void visitExprAclLine(ExprAclLine exprAclLine) {
      visit(exprAclLine.getMatchCondition());
      return null;
    }
  }

  private static void finalizeIpSpaces(Configuration c, Warnings w) {
    Set<String> undefinedIpSpaceReferences =
        ImmutableSortedSet.copyOf(
            Sets.difference(CollectIpSpaceReferences.collect(c), c.getIpSpaces().keySet()));
    if (!undefinedIpSpaceReferences.isEmpty()) {
      w.redFlag("Creating empty IP spaces for missing names: " + undefinedIpSpaceReferences);
      undefinedIpSpaceReferences.forEach(n -> c.getIpSpaces().put(n, EmptyIpSpace.INSTANCE));
    }
    c.setIpSpaces(toImmutableMap(c.getIpSpaces()));
  }

  /**
   * Remove invalid {@link VendorStructureId}s from the {@link Configuration}. {@link
   * VendorStructureId}s are considered invalid if they do not point to a defined structure in the
   * specified {@link VendorConfiguration}.
   *
   * <p>Currently only handles {@link VendorStructureId}s in {@link IpAccessList}s and their {@link
   * org.batfish.datamodel.TraceElement}s.
   */
  @VisibleForTesting
  @SuppressWarnings("unused") // https://github.com/batfish/batfish/issues/9267
  static void removeInvalidVendorStructureIds(Configuration c, VendorConfiguration vc, Warnings w) {
    InvalidVendorStructureIdEraser vsidEraser =
        new InvalidVendorStructureIdEraser(vc.getFilename(), vc.getStructureManager());
    c.setIpAccessLists(
        c.getIpAccessLists().entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey, e -> vsidEraser.visit(e.getValue()))));
  }

  /**
   * Asserts that {@link VendorStructureId}s in the specified {@link Configuration} are valid.
   *
   * <p>Currently only checks {@link VendorStructureId}s in {@link IpAccessList}s and their {@link
   * org.batfish.datamodel.TraceElement}s.
   */
  @VisibleForTesting
  static boolean assertVendorStructureIdsValid(
      Configuration c, VendorConfiguration vc, Warnings w) {
    InvalidVendorStructureIdEraser eraser =
        new InvalidVendorStructureIdEraser(vc.getFilename(), vc.getStructureManager());
    // Erase invalid VSIDs and confirm no changes occur
    for (IpAccessList acl : c.getIpAccessLists().values()) {
      for (AclLine line : acl.getLines()) {
        if (!line.equals(eraser.visit(line))) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Applies sanity checks and finishing touches to the given {@link Configuration}.
   *
   * <p>Sanity checks such as asserting that required properties hold.
   *
   * <p>Generation of helper structures such as tenant vrf l3vni interfaces
   *
   * <p>Finishing touches such as converting structures to their immutable forms.
   */
  @VisibleForTesting
  static void finalizeConfiguration(Configuration c, VendorConfiguration vc, Warnings w) {
    String hostname = c.getHostname();
    if (c.getDefaultCrossZoneAction() == null) {
      throw new BatfishException(
          "Implementation error: missing default cross-zone action for host: '" + hostname + "'");
    }
    if (c.getDefaultInboundAction() == null) {
      throw new BatfishException(
          "Implementation error: missing default inbound action for host: '" + hostname + "'");
    }
    addTenantVniInterfaces(c);
    removeUndefinedRoutingPolicyReferences(c, w);
    c.simplifyRoutingPolicies();
    c.computeRoutingPolicySources(w);
    verifyInterfaces(c, w);
    verifyOspfAreas(c, w);
    verifyVrrpGroups(c, w);
    removeInvalidStaticRoutes(c, w);
    removeUndefinedTrackReferences(c, w);
    // Make tests fail if they have invalid VSIDs
    assert assertVendorStructureIdsValid(c, vc, w);
    // Too slow right now: https://github.com/batfish/batfish/issues/9267
    // removeInvalidVendorStructureIds(c, vc, w);

    c.setAsPathAccessLists(
        verifyAndToImmutableMap(c.getAsPathAccessLists(), AsPathAccessList::getName, w));
    c.setAsPathExprs(toImmutableMap(c.getAsPathExprs()));
    c.setAsPathMatchExprs(toImmutableMap(c.getAsPathMatchExprs()));
    c.setAuthenticationKeyChains(toImmutableMap(c.getAuthenticationKeyChains()));
    c.setCommunityMatchExprs(toImmutableMap(c.getCommunityMatchExprs()));
    c.setCommunitySetExprs(toImmutableMap(c.getCommunitySetExprs()));
    c.setCommunitySetMatchExprs(toImmutableMap(c.getCommunitySetMatchExprs()));
    c.setCommunitySets(toImmutableMap(c.getCommunitySets()));
    c.setDnsServers(toImmutableSet(c.getDnsServers()));
    c.setGeneratedReferenceBooks(toImmutableMap(c.getGeneratedReferenceBooks()));
    c.setIkePhase1Keys(toImmutableMap(c.getIkePhase1Keys()));
    c.setIkePhase1Policies(toImmutableMap(c.getIkePhase1Policies()));
    c.setIkePhase1Proposals(toImmutableMap(c.getIkePhase1Proposals()));
    c.setInterfaces(
        verifyAndToImmutableMap(
            c.getAllInterfaces(), Interface::getName, w, InterfaceNameComparator.instance()));
    c.setIpAccessLists(verifyAndToImmutableMap(c.getIpAccessLists(), IpAccessList::getName, w));
    c.setIpsecPeerConfigs(toImmutableMap(c.getIpsecPeerConfigs()));
    c.setIpsecPhase2Policies(toImmutableMap(c.getIpsecPhase2Policies()));
    c.setIpsecPhase2Proposals(toImmutableMap(c.getIpsecPhase2Proposals()));
    c.setIpSpaceMetadata(toImmutableMap(c.getIpSpaceMetadata()));
    finalizeIpSpaces(c, w);
    c.setLoggingServers(toImmutableSet(c.getLoggingServers()));
    c.setMlags(verifyAndToImmutableMap(c.getMlags(), Mlag::getId, w));
    c.setNtpServers(toImmutableSet(c.getNtpServers()));
    c.setPacketPolicies(verifyAndToImmutableMap(c.getPacketPolicies(), PacketPolicy::getName, w));
    c.setRouteFilterLists(
        verifyAndToImmutableMap(c.getRouteFilterLists(), RouteFilterList::getName, w));
    c.setRoutingPolicies(
        verifyAndToImmutableMap(c.getRoutingPolicies(), RoutingPolicy::getName, w));
    c.setSnmpTrapServers(toImmutableSet(c.getSnmpTrapServers()));
    c.setTacacsServers(toImmutableSet(c.getTacacsServers()));
    c.setTrackingGroups(toImmutableMap(c.getTrackingGroups()));
    c.setVrfs(verifyAndToImmutableMap(c.getVrfs(), Vrf::getName, w));
    c.setZones(toImmutableMap(c.getZones()));
    for (Vrf v : c.getVrfs().values()) {
      BgpProcess p = v.getBgpProcess();
      if (p == null) {
        continue;
      }
      p.setNeighbors(ImmutableMap.copyOf(p.getActiveNeighbors()));
      p.setInterfaceNeighbors(ImmutableMap.copyOf(p.getInterfaceNeighbors()));
      p.setPassiveNeighbors(ImmutableMap.copyOf(p.getPassiveNeighbors()));
    }

    verifyAclInvariants(c, w);
    verifyAsPathStructures(c);
    verifyCommunityStructures(c);
  }

  private static void verifyOspfAreas(Configuration c, Warnings w) {
    for (Vrf v : c.getVrfs().values()) {
      for (OspfProcess proc : v.getOspfProcesses().values()) {
        for (OspfArea area : ImmutableList.copyOf(proc.getAreas().values())) {
          List<String> undefinedInterfaces =
              area.getInterfaces().stream()
                  .filter(name -> !c.getAllInterfaces().containsKey(name))
                  .collect(ImmutableList.toImmutableList());
          if (!undefinedInterfaces.isEmpty()) {
            w.redFlag(
                String.format(
                    "Removing undefined interfaces %s from OSPF process %s area %s on node %s vrf"
                        + " %s",
                    undefinedInterfaces,
                    proc.getProcessId(),
                    area.getAreaNumber(),
                    c.getHostname(),
                    v.getName()));
            OspfArea newArea =
                area.toBuilder()
                    .setInterfaces(
                        area.getInterfaces().stream()
                            .filter(name -> c.getAllInterfaces().containsKey(name))
                            .collect(ImmutableSortedSet.toImmutableSortedSet(naturalOrder())))
                    .build();
            ImmutableMap.Builder<Long, OspfArea> areasBuilder = ImmutableMap.builder();
            proc.getAreas().values().stream()
                .filter(a -> a.getAreaNumber() != newArea.getAreaNumber())
                .forEach(a -> areasBuilder.put(a.getAreaNumber(), a));
            areasBuilder.put(newArea.getAreaNumber(), newArea);
            proc.setAreas(areasBuilder.build());
          }
        }
      }
    }
  }

  /** Remove and warn on undefined routing policy references. */
  private static void removeUndefinedRoutingPolicyReferences(Configuration c, Warnings w) {
    removeUndefinedBgpProcessRoutingPolicyReferences(c, w);
  }

  /** Remove and warn on undefined routing policy references within {@link BgpProcess}es. */
  private static void removeUndefinedBgpProcessRoutingPolicyReferences(
      Configuration c, Warnings w) {
    c.getVrfs()
        .forEach(
            (vrfName, vrf) -> {
              BgpProcess proc = vrf.getBgpProcess();
              if (proc == null) {
                // nothing to check
                return;
              }
              // Next hop IP resolver restriction policy
              String nextHopIpResolverRestrictionPolicyName =
                  proc.getNextHopIpResolverRestrictionPolicy();
              if (nextHopIpResolverRestrictionPolicyName != null
                  && !c.getRoutingPolicies().containsKey(nextHopIpResolverRestrictionPolicyName)) {
                proc.setNextHopIpResolverRestrictionPolicy(null);
                w.redFlagf(
                    "Removing reference to undefined nextHopIpResolverRestrictionPolicy '%s' in BGP"
                        + " process for vrf '%s'",
                    nextHopIpResolverRestrictionPolicyName, vrfName);
              }
            });
  }

  /** Remove and warn on undefined track references. */
  private static void removeUndefinedTrackReferences(Configuration c, Warnings w) {
    removeUndefinedVrrpTrackReferences(c, w);
    removeUndefinedHsrpTrackReferences(c, w);
    removeUndefinedStaticRouteTrackReferences(c, w);
    removeUndefinedBgpProcessTrackReferences(c, w);
  }

  private static void removeUndefinedBgpProcessTrackReferences(Configuration c, Warnings w) {
    c.getVrfs()
        .forEach(
            (vrfName, vrf) -> {
              BgpProcess proc = vrf.getBgpProcess();
              if (proc == null) {
                // nothing to check
                return;
              }
              if (proc.getTracks().stream().allMatch(c.getTrackingGroups()::containsKey)) {
                // all good
                return;
              }
              // At least one undefined track
              ImmutableSet.Builder<String> definedTracks = ImmutableSet.builder();
              for (String trackName : proc.getTracks()) {
                if (c.getTrackingGroups().containsKey(trackName)) {
                  definedTracks.add(trackName);
                } else {
                  w.redFlag(
                      String.format(
                          "Removing reference to undefined track '%s' in BGP process for vrf '%s'",
                          trackName, vrfName));
                }
              }
              proc.setTracks(definedTracks.build());
            });
  }

  private static void removeUndefinedStaticRouteTrackReferences(Configuration c, Warnings w) {
    for (Vrf v : c.getVrfs().values()) {
      boolean modified = false;
      ImmutableSortedSet.Builder<StaticRoute> routes = ImmutableSortedSet.naturalOrder();
      for (StaticRoute sr : v.getStaticRoutes()) {
        String track = sr.getTrack();
        if (track != null && !c.getTrackingGroups().containsKey(track)) {
          modified = true;
          w.redFlag(
              String.format(
                  "Removing reference to undefined track '%s' on static route for prefix %s in vrf"
                      + " '%s'",
                  track, sr.getNetwork(), v.getName()));
          routes.add(sr.toBuilder().setTrack(null).build());
        } else {
          routes.add(sr);
        }
      }
      if (modified) {
        v.setStaticRoutes(routes.build());
      }
    }
  }

  private static void removeUndefinedHsrpTrackReferences(Configuration c, Warnings w) {
    for (Interface i : c.getAllInterfaces().values()) {
      boolean groupsModified = false;
      ImmutableMap.Builder<Integer, HsrpGroup> newGroups = ImmutableMap.builder();
      for (Entry<Integer, HsrpGroup> groupById : i.getHsrpGroups().entrySet()) {
        int id = groupById.getKey();
        ImmutableSortedMap.Builder<String, TrackAction> newActions =
            ImmutableSortedMap.naturalOrder();
        HsrpGroup group = groupById.getValue();
        boolean tracksModified = false;
        for (Entry<String, TrackAction> actionByTrack : group.getTrackActions().entrySet()) {
          String track = actionByTrack.getKey();
          if (!c.getTrackingGroups().containsKey(track)) {
            tracksModified = true;
            groupsModified = true;
            w.redFlag(
                String.format(
                    "Removing reference to undefined track '%s' in HSRP group %d on '%s'",
                    track, id, NodeInterfacePair.of(i)));
          } else {
            newActions.put(track, actionByTrack.getValue());
          }
        }
        if (tracksModified) {
          HsrpGroup newGroup = group.toBuilder().setTrackActions(newActions.build()).build();
          newGroups.put(id, newGroup);
        } else {
          newGroups.put(id, group);
        }
      }
      if (groupsModified) {
        i.setHsrpGroups(newGroups.build());
      }
    }
  }

  private static void removeUndefinedVrrpTrackReferences(Configuration c, Warnings w) {
    for (Interface i : c.getAllInterfaces().values()) {
      boolean groupsModified = false;
      ImmutableSortedMap.Builder<Integer, VrrpGroup> newGroups = ImmutableSortedMap.naturalOrder();
      for (Entry<Integer, VrrpGroup> groupById : i.getVrrpGroups().entrySet()) {
        int id = groupById.getKey();
        ImmutableSortedMap.Builder<String, TrackAction> newActions =
            ImmutableSortedMap.naturalOrder();
        VrrpGroup group = groupById.getValue();
        boolean tracksModified = false;
        for (Entry<String, TrackAction> actionByTrack : group.getTrackActions().entrySet()) {
          String track = actionByTrack.getKey();
          if (!c.getTrackingGroups().containsKey(track)) {
            tracksModified = true;
            groupsModified = true;
            w.redFlag(
                String.format(
                    "Removing reference to undefined track '%s' in VRRP group %d on '%s'",
                    track, id, NodeInterfacePair.of(i)));
          } else {
            newActions.put(track, actionByTrack.getValue());
          }
        }
        if (tracksModified) {
          VrrpGroup newGroup = group.toBuilder().setTrackActions(newActions.build()).build();
          newGroups.put(id, newGroup);
        } else {
          newGroups.put(id, group);
        }
      }
      if (groupsModified) {
        i.setVrrpGroups(newGroups.build());
      }
    }
  }

  private static void removeInvalidStaticRoutes(Configuration c, Warnings w) {
    StaticRouteNextHopChecker checker = new StaticRouteNextHopChecker(c, w);
    for (Vrf v : c.getVrfs().values()) {
      boolean modified = false;
      ImmutableSortedSet.Builder<StaticRoute> builder = ImmutableSortedSet.naturalOrder();
      for (StaticRoute sr : v.getStaticRoutes()) {
        if (!checker.visit(sr.getNextHop())) {
          modified = true;
        } else {
          builder.add(sr);
        }
      }
      if (modified) {
        v.setStaticRoutes(builder.build());
      }
    }
  }

  private static final class StaticRouteNextHopChecker implements NextHopVisitor<Boolean> {
    private StaticRouteNextHopChecker(Configuration c, Warnings w) {
      _c = c;
      _w = w;
    }

    private final @Nonnull Configuration _c;
    private final @Nonnull Warnings _w;

    @Override
    public Boolean visitNextHopIp(NextHopIp nextHopIp) {
      return true;
    }

    @Override
    public Boolean visitNextHopInterface(NextHopInterface nextHopInterface) {
      if (!_c.getAllInterfaces().containsKey(nextHopInterface.getInterfaceName())) {
        _w.redFlag(
            String.format(
                "Removing invalid static route on node '%s' with undefined next hop interface '%s'",
                _c.getHostname(), nextHopInterface.getInterfaceName()));
        return false;
      }
      return true;
    }

    @Override
    public Boolean visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
      return true;
    }

    @Override
    public Boolean visitNextHopVrf(NextHopVrf nextHopVrf) {
      if (!_c.getVrfs().containsKey(nextHopVrf.getVrfName())) {
        _w.redFlag(
            String.format(
                "Removing invalid static route on node '%s' with undefined next hop vrf '%s'",
                _c.getHostname(), nextHopVrf.getVrfName()));
        return false;
      }
      return true;
    }

    @Override
    public Boolean visitNextHopVtep(NextHopVtep nextHopVtep) {
      throw new IllegalArgumentException("Static routes cannot have next hop VTEP");
    }
  }

  private static void verifyAclInvariants(Configuration c, Warnings w) {
    if (c.getConfigurationFormat() == ConfigurationFormat.CISCO_ASA) {
      // ASA has some invariant issues.
      return;
    }
    AclPacketMatchValidityChecker checker = checkerFor(c);
    for (IpAccessList acl : c.getIpAccessLists().values()) {
      try {
        if (!checker.check(acl)) {
          String message =
              String.format(
                  "Filter %s on device %s does not meet the expected packet invariants",
                  acl.getName(), c.getHostname());
          w.redFlag(message);
          assert false : message;
        }
      } catch (Exception e) {
        String message =
            String.format(
                "Filter %s on device %s failed to convert to BDD: %s",
                acl.getName(), c.getHostname(), Throwables.getStackTraceAsString(e));
        w.redFlag(message);
        assert false : message;
      }
    }
  }

  private static void verifyAsPathStructures(Configuration c) {
    AsPathStructuresVerifier.verify(c);
  }

  private static void verifyCommunityStructures(Configuration c) {
    // TODO: crash on undefined/circular refs (conversion is responsible for preventing them)
    CommunityStructuresVerifier.verify(c);
  }

  /**
   * Warns on and removes virtual addresses of {@link VrrpGroup}s corresponding to missing
   * interfaces.
   */
  private static void verifyVrrpGroups(Configuration c, Warnings w) {
    for (Interface i : c.getAllInterfaces().values()) {
      boolean modified = false;
      ImmutableSortedMap.Builder<Integer, VrrpGroup> groupsBuilder =
          ImmutableSortedMap.naturalOrder();
      for (Entry<Integer, VrrpGroup> vrrpGroupByVrid : i.getVrrpGroups().entrySet()) {
        int vrid = vrrpGroupByVrid.getKey();
        VrrpGroup vrrpGroup = vrrpGroupByVrid.getValue();
        ImmutableMap.Builder<String, Set<Ip>> addressesBuilder = ImmutableMap.builder();
        for (Entry<String, Set<Ip>> addressesByInterface :
            vrrpGroup.getVirtualAddresses().entrySet()) {
          String referencedIface = addressesByInterface.getKey();
          if (!c.getAllInterfaces().containsKey(referencedIface)) {
            w.redFlag(
                String.format(
                    "Removing virtual addresses to be assigned to non-existent interface '%s' for"
                        + " VRID %d on sync interface '%s'",
                    referencedIface, vrid, i.getName()));
            modified = true;
          } else {
            addressesBuilder.put(referencedIface, addressesByInterface.getValue());
          }
        }
        groupsBuilder.put(
            vrid, vrrpGroup.toBuilder().setVirtualAddresses(addressesBuilder.build()).build());
      }
      if (modified) {
        i.setVrrpGroups(groupsBuilder.build());
      }
    }
  }

  /** Warns on and removes interfaces with VI-invalid settings. */
  private static void verifyInterfaces(Configuration c, Warnings w) {
    Set<String> inInterfaces = ImmutableSet.copyOf(c.getAllInterfaces().keySet());
    for (String name : inInterfaces) {
      Interface i = c.getAllInterfaces().get(name);
      if (!verifyDependencies(c, w, i)) {
        continue;
      }
      // VI invariant: switchport is true iff SwitchportMode is not NONE.
      boolean hasSwitchportMode = i.getSwitchportMode() != SwitchportMode.NONE;
      if (hasSwitchportMode != i.getSwitchport()) {
        w.redFlag(
            String.format(
                "Interface %s has switchport %s but switchport mode %s",
                name, i.getSwitchport(), i.getSwitchportMode()));
        c.getAllInterfaces().remove(name);
        continue;
      }
      if (i.getSwitchport() && !i.getAllAddresses().isEmpty()) {
        w.redFlagf("Interface %s is a switchport, but it has L3 addresses", name);
        c.getAllInterfaces().remove(name);
        continue;
      }
      if (i.getActive()) {
        if (!verifyChannelGroupExists(c, w, i)) {
          continue;
        }
        if (i.getInterfaceType() == InterfaceType.VLAN && i.getVlan() == null) {
          w.redFlagf("Interface %s is a VLAN interface but has no vlan set", name);
          c.getAllInterfaces().remove(name);
          continue;
        }
        if (i.getChannelGroup() != null) {
          String aggregateName = i.getChannelGroup();
          Interface aggregate = c.getAllInterfaces().get(aggregateName);
          if (aggregate == null) {
            w.redFlag(
                String.format(
                    "Interface %s is a member of undefined aggregate or or redundant interface %s",
                    name, aggregateName));
            c.getAllInterfaces().remove(name);
            continue;
          }
          if (!i.getAllAddresses().isEmpty()) {
            w.redFlag(
                String.format(
                    "Interface %s is a member of %s interface %s but it has L3 addresses",
                    name, aggregate.getInterfaceType(), aggregateName));
            c.getAllInterfaces().remove(name);
            continue;
          }
        }
        if (i.getInterfaceType() == InterfaceType.LOGICAL
            && !verifyLogicalBindDependencies(c, w, i)) {
          continue;
        }
      }
    }
  }

  /**
   * Removes undefined aggregate dependencies, and removes interface if it has an undefined bind
   * dependency.
   *
   * <p>Return {@code true} iff interface was not removed.
   */
  private static boolean verifyDependencies(Configuration c, Warnings w, Interface i) {
    String name = i.getName();
    Set<Dependency> inDependencies = ImmutableSet.copyOf(i.getDependencies());
    for (Dependency dependency : inDependencies) {
      String refName = dependency.getInterfaceName();
      if (!c.getAllInterfaces().containsKey(refName)) {
        if (dependency.getType() == DependencyType.BIND) {
          w.redFlag(
              String.format(
                  "Interface %s has a bind dependency on missing interface %s", name, refName));
          c.getAllInterfaces().remove(name);
          return false;
        } else {
          assert dependency.getType() == DependencyType.AGGREGATE;
          w.redFlag(
              String.format(
                  "Interface %s has an aggregate dependency on missing interface %s",
                  name, refName));
          i.setDependencies(
              i.getDependencies().stream()
                  .filter(d -> !d.equals(dependency))
                  .collect(ImmutableSet.toImmutableSet()));
        }
      }
    }
    return true;
  }

  /**
   * Removes an interface if it has an undefined channel group reference.
   *
   * <p>Return {@code true} unless the interface has a channel group that is undefined.
   */
  private static boolean verifyChannelGroupExists(Configuration c, Warnings w, Interface i) {
    String channelGroupName = i.getChannelGroup();
    if (channelGroupName == null || c.getAllInterfaces().containsKey(channelGroupName)) {
      return true;
    }
    String name = i.getName();
    w.redFlagf("Interface %s has an undefined channel group %s", name, channelGroupName);
    c.getAllInterfaces().remove(name);
    return false;
  }

  /**
   * Remove a logical interface if it has L3 settings but is a child of a member of an aggregate.
   *
   * <p>Return {code false} iff the interface is removed.
   */
  private static boolean verifyLogicalBindDependencies(Configuration c, Warnings w, Interface i) {
    for (Dependency dependency : i.getDependencies()) {
      if (dependency.getType() != DependencyType.BIND) {
        continue;
      }
      String parentName = dependency.getInterfaceName();
      Interface parent = c.getAllInterfaces().get(parentName);
      // verified earlier
      assert parent != null;
      String aggregateName = parent.getChannelGroup();
      if (aggregateName == null) {
        continue;
      }
      Interface aggregate = c.getAllInterfaces().get(aggregateName);
      // verified earlier
      assert aggregate != null;
      String name = i.getName();
      if (!i.getAllAddresses().isEmpty()) {
        w.redFlag(
            String.format(
                "Interface %s is a child of a member of %s interface %s but it has L3 addresses",
                name, aggregate.getInterfaceType(), aggregateName));
        c.getAllInterfaces().remove(name);
        return false;
      }
    }
    return true;
  }

  @Override
  public ConvertConfigurationResult call() {
    long startTime = System.currentTimeMillis();
    long elapsedTime;
    _logger.infof("Processing: \"%s\"", _name);
    Map<String, Configuration> configurations = new HashMap<>();
    Map<String, Warnings> warningsByHost = new HashMap<>();
    ConvertConfigurationAnswerElement answerElement = new ConvertConfigurationAnswerElement();
    Multimap<String, String> fileMap = answerElement.getFileMap();
    try {
      VendorConfiguration vendorConfiguration = (VendorConfiguration) _configObject;
      Warnings warnings = Batfish.buildWarnings(_settings);
      List<String> filenames =
          ImmutableList.<String>builder()
              .add(vendorConfiguration.getFilename())
              .addAll(vendorConfiguration.getSecondaryFilenames())
              .build();
      vendorConfiguration.setWarnings(warnings);
      vendorConfiguration.setConversionContext(_conversionContext);
      vendorConfiguration.setRuntimeData(_runtimeData);
      SortedMap<String, Warnings> configSpecificWarnings = new ConcurrentSkipListMap<>();
      // Parallelize because we may get a large number of configurations from e.g. Panorama,
      // CheckPoint MGMT.
      vendorConfiguration.toVendorIndependentConfigurations().parallelStream()
          .forEach(
              configuration -> {
                Warnings currentConfigSpecificWarnings = Batfish.buildWarnings(_settings);
                configSpecificWarnings.put(
                    configuration.getHostname(), currentConfigSpecificWarnings);
                // get iptables if applicable
                IptablesVendorConfiguration iptablesConfig = null;
                VendorConfiguration ov = vendorConfiguration.getOverlayConfiguration();
                if (ov != null) {
                  // apply overlay
                  HostConfiguration oh = (HostConfiguration) ov;
                  iptablesConfig = oh.getIptablesVendorConfig();
                } else if (vendorConfiguration instanceof HostConfiguration) {
                  // TODO: To enable below, we need to reconcile overlay and non-overlay iptables
                  //       semantics.
                  // HostConfiguration oh = (HostConfiguration)vendorConfiguration;
                  // iptablesConfig = oh.getIptablesVendorConfig();
                }
                if (iptablesConfig != null) {
                  iptablesConfig.addAsIpAccessLists(
                      configuration, vendorConfiguration, currentConfigSpecificWarnings);
                  iptablesConfig.applyAsOverlay(configuration, currentConfigSpecificWarnings);
                }
                finalizeConfiguration(
                    configuration, vendorConfiguration, currentConfigSpecificWarnings);
                postFinalize(
                    configurations, warningsByHost, fileMap, warnings, filenames, configuration);
              });
      saveStructureInfo(answerElement, vendorConfiguration);
      for (Warnings currentConfigSpecificWarnings : configSpecificWarnings.values()) {
        // Merge in config-specific warnings in deterministic fashion; values are ordered by
        // Configuration-specific hostname key from SortedMap.
        mergeConfigurationWarnings(warnings, currentConfigSpecificWarnings);
      }
      _logger.info(" ...OK\n");
    } catch (Exception e) {
      String error = "Conversion error for node with hostname '" + _name + "'";
      elapsedTime = System.currentTimeMillis() - startTime;
      return new ConvertConfigurationResult(
          elapsedTime, _logger.getHistory(), _name, new BatfishException(error, e));
    } finally {
      warningsByHost.forEach((hostname, warnings) -> Batfish.logWarnings(_logger, warnings));
    }
    elapsedTime = System.currentTimeMillis() - startTime;
    return new ConvertConfigurationResult(
        elapsedTime, _logger.getHistory(), warningsByHost, _name, configurations, answerElement);
  }

  @VisibleForTesting
  static void saveStructureInfo(
      ConvertConfigurationAnswerElement answerElement, VendorConfiguration vendorConfiguration) {
    vendorConfiguration
        .getStructureManagerByFilename()
        .forEach(
            (filename, structureManager) -> structureManager.saveInto(answerElement, filename));
  }

  /** Apply changes to job-level structures. */
  private synchronized void postFinalize(
      Map<String, Configuration> configurations,
      Map<String, Warnings> warningsByHost,
      Multimap<String, String> fileMap,
      Warnings warnings,
      List<String> filenames,
      Configuration configuration) {
    String hostname = configuration.getHostname();
    configurations.put(hostname, configuration);
    warningsByHost.put(hostname, warnings);
    filenames.forEach(filename -> fileMap.put(filename, hostname));
  }

  /** Merge configuration-specific warnings into job warnings */
  private static void mergeConfigurationWarnings(
      Warnings warnings, Warnings configurationSpecificWarnings) {
    checkArgument(
        configurationSpecificWarnings.getErrorDetails() == null,
        "Configuration-specific warnings should not have error details");
    checkArgument(
        configurationSpecificWarnings.getParseWarnings().isEmpty(),
        "Configuration-specific warnings should not have parse warnings");
    warnings.getPedanticWarnings().addAll(configurationSpecificWarnings.getPedanticWarnings());
    warnings.getRedFlagWarnings().addAll(configurationSpecificWarnings.getRedFlagWarnings());
    warnings
        .getUnimplementedWarnings()
        .addAll(configurationSpecificWarnings.getUnimplementedWarnings());
  }
}
