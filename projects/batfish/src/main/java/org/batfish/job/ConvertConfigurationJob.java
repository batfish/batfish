package org.batfish.job;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.vendor.ConversionContext.EMPTY_CONVERSION_CONTEXT;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.config.Settings;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Mlag;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.bgp.community.CommunityStructuresVerifier;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.AsPathStructuresVerifier;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;
import org.batfish.main.Batfish;
import org.batfish.representation.host.HostConfiguration;
import org.batfish.representation.iptables.IptablesVendorConfiguration;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.VendorConfiguration;

public class ConvertConfigurationJob extends BatfishJob<ConvertConfigurationResult> {

  private Object _configObject;
  @Nonnull private final ConversionContext _conversionContext;
  @Nonnull private final SnapshotRuntimeData _runtimeData;
  private String _name;

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
        .sorted(Comparator.comparing(Entry::getKey)) /* ImmutableMap is insert ordered. */
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
        .sorted(Comparator.comparing(Entry::getKey)) /* ImmutableMap is insert ordered. */
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
      if (t == null) {
        return;
      }
      if (!_visited.add(t)) {
        return;
      }
      visit(t.getGuard());
      visit(t.getAndThen());
      visit(t.getOrElse());
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
   * Applies sanity checks and finishing touches to the given {@link Configuration}.
   *
   * <p>Sanity checks such as asserting that required properties hold.
   *
   * <p>Finishing touches such as converting structures to their immutable forms.
   */
  @VisibleForTesting
  static void finalizeConfiguration(Configuration c, Warnings w) {
    String hostname = c.getHostname();
    if (c.getDefaultCrossZoneAction() == null) {
      throw new BatfishException(
          "Implementation error: missing default cross-zone action for host: '" + hostname + "'");
    }
    if (c.getDefaultInboundAction() == null) {
      throw new BatfishException(
          "Implementation error: missing default inbound action for host: '" + hostname + "'");
    }
    c.simplifyRoutingPolicies();
    c.computeRoutingPolicySources(w);
    verifyInterfaces(c, w);

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
    c.setInterfaces(verifyAndToImmutableMap(c.getAllInterfaces(), Interface::getName, w));
    c.setIp6AccessLists(verifyAndToImmutableMap(c.getIp6AccessLists(), Ip6AccessList::getName, w));
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
    c.setRoute6FilterLists(
        verifyAndToImmutableMap(c.getRoute6FilterLists(), Route6FilterList::getName, w));
    c.setRoutingPolicies(
        verifyAndToImmutableMap(c.getRoutingPolicies(), RoutingPolicy::getName, w));
    c.setSnmpTrapServers(toImmutableSet(c.getSnmpTrapServers()));
    c.setTacacsServers(toImmutableSet(c.getTacacsServers()));
    c.setTrackingGroups(toImmutableMap(c.getTrackingGroups()));
    c.setVrfs(verifyAndToImmutableMap(c.getVrfs(), Vrf::getName, w));
    c.setZones(toImmutableMap(c.getZones()));
    verifyAsPathStructures(c);
    verifyCommunityStructures(c);
    removeInvalidAcls(c, w);
  }

  private static void verifyAsPathStructures(Configuration c) {
    AsPathStructuresVerifier.verify(c);
  }

  private static void verifyCommunityStructures(Configuration c) {
    // TODO: crash on undefined/circular refs (conversion is responsible for preventing them)
    CommunityStructuresVerifier.verify(c);
  }

  /** Warns on and removes interfaces with VI-invalid settings. */
  private static void verifyInterfaces(Configuration c, Warnings w) {
    Set<String> inInterfaces = ImmutableSet.copyOf(c.getAllInterfaces().keySet());
    for (String name : inInterfaces) {
      Interface i = c.getAllInterfaces().get(name);
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
        w.redFlag(String.format("Interface %s is a switchport, but it has L3 addresses", name));
        c.getAllInterfaces().remove(name);
      }
    }
  }

  private static void clearInvalidFilterAndWarn(
      Map<String, IpAccessList> acls,
      @Nonnull Supplier<IpAccessList> getter,
      @Nonnull Consumer<IpAccessList> setter,
      @Nonnull String purpose,
      Warnings w) {
    @Nullable IpAccessList acl = getter.get();
    if (acl == null) {
      return;
    }
    @Nullable IpAccessList inMap = acls.get(acl.getName());
    // Deliberate == comparison.
    if (inMap == acl) {
      return;
    }
    if (inMap == null) {
      w.redFlag(
          String.format("The device ACL map does not contain %s %s.", purpose, acl.getName()));
    } else {
      w.redFlag(
          String.format(
              "The device ACL map has a different version of %s %s.", purpose, acl.getName()));
    }
    setter.accept(null);
  }

  /** Confirm assigned ACLs (e.g. interface's outgoing ACL) exist in config's IpAccessList map */
  private static void removeInvalidAcls(Configuration c, Warnings w) {
    for (Interface iface : c.getAllInterfaces().values()) {
      Map<String, IpAccessList> acls = c.getIpAccessLists();
      clearInvalidFilterAndWarn(
          acls, iface::getInboundFilter, iface::setInboundFilter, "inbound filter", w);
      clearInvalidFilterAndWarn(
          acls, iface::getIncomingFilter, iface::setIncomingFilter, "incoming filter", w);
      clearInvalidFilterAndWarn(
          acls, iface::getOutgoingFilter, iface::setOutgoingFilter, "outgoing filter", w);
      clearInvalidFilterAndWarn(
          acls,
          iface::getOutgoingOriginalFlowFilter,
          iface::setOutgoingOriginalFlowFilter,
          "outgoing filter on original flow",
          w);
      clearInvalidFilterAndWarn(
          acls,
          iface::getPostTransformationIncomingFilter,
          iface::setPostTransformationIncomingFilter,
          "post transformation incoming filter",
          w);
      clearInvalidFilterAndWarn(
          acls,
          iface::getPreTransformationOutgoingFilter,
          iface::setPreTransformationOutgoingFilter,
          "pre transformation outgoing filter",
          w);
    }
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
      String filename = vendorConfiguration.getFilename();
      vendorConfiguration.setWarnings(warnings);
      vendorConfiguration.setAnswerElement(answerElement);
      vendorConfiguration.setConversionContext(_conversionContext);
      vendorConfiguration.setRuntimeData(_runtimeData);
      for (Configuration configuration : vendorConfiguration.toVendorIndependentConfigurations()) {

        // get iptables if applicable
        IptablesVendorConfiguration iptablesConfig = null;
        VendorConfiguration ov = vendorConfiguration.getOverlayConfiguration();
        if (ov != null) {
          // apply overlay
          HostConfiguration oh = (HostConfiguration) ov;
          iptablesConfig = oh.getIptablesVendorConfig();
        } else if (vendorConfiguration instanceof HostConfiguration) {
          // TODO: To enable below, we need to reconcile overlay and non-overlay iptables
          // semantics.
          // HostConfiguration oh = (HostConfiguration)vendorConfiguration;
          // iptablesConfig = oh.getIptablesVendorConfig();
        }
        if (iptablesConfig != null) {
          iptablesConfig.addAsIpAccessLists(configuration, vendorConfiguration, warnings);
          iptablesConfig.applyAsOverlay(configuration, warnings);
        }

        finalizeConfiguration(configuration, warnings);

        String hostname = configuration.getHostname();
        configurations.put(hostname, configuration);
        warningsByHost.put(hostname, warnings);
        fileMap.put(filename, hostname);
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
}
