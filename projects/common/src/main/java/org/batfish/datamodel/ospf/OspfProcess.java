package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Streams;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/** An OSPF routing process */
@ParametersAreNonnullByDefault
public final class OspfProcess implements Serializable {

  /** Builder for {@link OspfProcess} */
  public static class Builder {

    private static final long DEFAULT_SUMMARY_DISCARD_METRIC = 0L;

    private @Nullable Map<RoutingProtocol, Integer> _adminCosts;
    private @Nullable String _exportPolicy;
    private @Nullable Long _maxMetricExternalNetworks;
    private @Nullable Long _maxMetricStubNetworks;
    private @Nullable Long _maxMetricSummaryNetworks;
    private @Nullable Long _maxMetricTransitLinks;
    private @Nonnull Map<OspfNeighborConfigId, OspfNeighborConfig> _neighborConfigs;
    private @Nullable String _processId;
    private @Nullable Supplier<String> _processIdGenerator;
    private @Nullable Double _referenceBandwidth;
    private @Nullable Vrf _vrf;
    private @Nullable Map<Long, OspfArea> _areas;
    private @Nullable Set<String> _exportPolicySources;
    private @Nullable Set<GeneratedRoute> _generatedRoutes;
    private @Nullable Boolean _rfc1583Compatible;
    private @Nullable Ip _routerId;
    private @Nullable Integer _summaryAdminCost;
    private @Nullable Long _summaryDiscardMetric;

    private Builder(@Nullable Supplier<String> processIdGenerator) {
      // Default to Cisco IOS values
      _adminCosts = computeDefaultAdminCosts(ConfigurationFormat.CISCO_IOS);
      _areas = ImmutableMap.of();
      _exportPolicySources = ImmutableSet.of();
      _neighborConfigs = ImmutableMap.of();
      _processIdGenerator = processIdGenerator;
      // Default to Cisco IOS value
      _summaryAdminCost =
          RoutingProtocol.OSPF_IA.getSummaryAdministrativeCost(ConfigurationFormat.CISCO_IOS);
      _generatedRoutes = ImmutableSet.of();
    }

    public @Nonnull OspfProcess build() {
      checkArgument(
          _processId != null || _processIdGenerator != null, "Missing %s", PROP_PROCESS_ID);
      checkArgument(_referenceBandwidth != null, "Missing %s", PROP_REFERENCE_BANDWIDTH);
      checkArgument(_routerId != null, "Missing %s", PROP_ROUTER_ID);
      checkArgument(_adminCosts.keySet().containsAll(REQUIRES_ADMIN));
      // Ensure area numbers match up
      checkArgument(
          _areas.entrySet().stream()
              .allMatch(entry -> entry.getKey() == entry.getValue().getAreaNumber()),
          "Area number does not match up with map key");

      OspfProcess ospfProcess =
          new OspfProcess(
              _adminCosts,
              _areas,
              _exportPolicy,
              _exportPolicySources,
              _generatedRoutes,
              _maxMetricExternalNetworks,
              _maxMetricStubNetworks,
              _maxMetricSummaryNetworks,
              _maxMetricTransitLinks,
              _neighborConfigs,
              _processId != null ? _processId : _processIdGenerator.get(),
              _referenceBandwidth,
              _rfc1583Compatible,
              _routerId,
              _summaryAdminCost,
              firstNonNull(_summaryDiscardMetric, DEFAULT_SUMMARY_DISCARD_METRIC));
      if (_vrf != null) {
        _vrf.setOspfProcesses(
            Streams.concat(_vrf.getOspfProcesses().values().stream(), Stream.of(ospfProcess)));
      }
      return ospfProcess;
    }

    public Builder setAllAdminCosts(@Nonnull int adminCosts) {
      _adminCosts =
          REQUIRES_ADMIN.stream()
              .collect(ImmutableMap.toImmutableMap(Function.identity(), rp -> adminCosts));
      return this;
    }

    public Builder setAdminCosts(@Nonnull Map<RoutingProtocol, Integer> adminCosts) {
      _adminCosts = ImmutableSortedMap.copyOf(adminCosts);
      return this;
    }

    public Builder setExportPolicy(@Nullable RoutingPolicy exportPolicy) {
      _exportPolicy = exportPolicy != null ? exportPolicy.getName() : null;
      return this;
    }

    public Builder setMaxMetricExternalNetworks(Long maxMetricExternalNetworks) {
      _maxMetricExternalNetworks = maxMetricExternalNetworks;
      return this;
    }

    public Builder setMaxMetricStubNetworks(Long maxMetricStubNetworks) {
      _maxMetricStubNetworks = maxMetricStubNetworks;
      return this;
    }

    public Builder setMaxMetricSummaryNetworks(Long maxMetricSummaryNetworks) {
      _maxMetricSummaryNetworks = maxMetricSummaryNetworks;
      return this;
    }

    public Builder setMaxMetricTransitLinks(Long maxMetricTransitLinks) {
      _maxMetricTransitLinks = maxMetricTransitLinks;
      return this;
    }

    public @Nonnull Builder setNeighborConfigs(
        Map<OspfNeighborConfigId, OspfNeighborConfig> neighborConfigs) {
      _neighborConfigs = neighborConfigs;
      return this;
    }

    public Builder setProcessId(@Nonnull String processId) {
      _processId = processId;
      return this;
    }

    public Builder setReferenceBandwidth(Double referenceBandwidth) {
      _referenceBandwidth = referenceBandwidth;
      return this;
    }

    public Builder setVrf(Vrf vrf) {
      _vrf = vrf;
      return this;
    }

    public Builder setAreas(Map<Long, OspfArea> areas) {
      _areas = areas;
      return this;
    }

    public Builder setExportPolicyName(@Nullable String exportPolicy) {
      _exportPolicy = exportPolicy;
      return this;
    }

    public Builder setExportPolicySources(Set<String> exportPolicySources) {
      _exportPolicySources = exportPolicySources;
      return this;
    }

    public Builder setGeneratedRoutes(Set<GeneratedRoute> generatedRoutes) {
      _generatedRoutes = generatedRoutes;
      return this;
    }

    public Builder setRfc1583Compatible(Boolean rfc1583Compatible) {
      _rfc1583Compatible = rfc1583Compatible;
      return this;
    }

    public Builder setRouterId(@Nonnull Ip routerId) {
      _routerId = routerId;
      return this;
    }

    public Builder setSummaryAdminCost(int admin) {
      _summaryAdminCost = admin;
      return this;
    }

    public Builder setSummaryDiscardMetric(long summaryDiscardMetric) {
      _summaryDiscardMetric = summaryDiscardMetric;
      return this;
    }
  }

  /** Set of routing protocols that are required to have a defined admin cost */
  public static final EnumSet<RoutingProtocol> REQUIRES_ADMIN =
      EnumSet.of(
          RoutingProtocol.OSPF,
          RoutingProtocol.OSPF_IA,
          RoutingProtocol.OSPF_IS,
          RoutingProtocol.OSPF_E1,
          RoutingProtocol.OSPF_E2);

  private static final String PROP_ADMIN_COSTS = "adminCosts";
  private static final String PROP_AREAS = "areas";
  private static final String PROP_EXPORT_POLICY = "exportPolicy";
  private static final String PROP_EXPORT_POLICY_SOURCES = "exportPolicySources";
  private static final String PROP_GENERATED_ROUTES = "generatedRoutes";
  private static final String PROP_MAX_METRIC_EXTERNAL_NETWORKS = "maxMetricExternalNetworks";
  private static final String PROP_MAX_METRIC_STUB_NETWORKS = "maxMetricStubNetworks";
  private static final String PROP_MAX_METRIC_SUMMARY_NETWORKS = "maxMetricSummaryNetworks";
  private static final String PROP_MAX_METRIC_TRANSIT_LINKS = "maxMetricTransitLinks";
  private static final String PROP_NEIGHBORS = "neighbors";
  private static final String PROP_PROCESS_ID = "processId";
  private static final String PROP_REFERENCE_BANDWIDTH = "referenceBandwidth";
  private static final String PROP_ROUTER_ID = "routerId";
  private static final String PROP_RFC1583 = "rfc1583Compatible";
  private static final String PROP_SUMMARY_ADMIN = "summaryAdminCost";
  private static final String PROP_SUMMARY_DISCARD_METRIC = "summaryDiscardMetric";

  public static @Nonnull Builder builder(@Nullable Supplier<String> processIdGenerator) {
    return new Builder(processIdGenerator);
  }

  public static @Nonnull Builder builder() {
    return new Builder(null);
  }

  private final @Nonnull Map<RoutingProtocol, Integer> _adminCosts;

  private @Nonnull Map<Long, OspfArea> _areas;
  private @Nullable String _exportPolicy;
  private @Nonnull Set<String> _exportPolicySources;
  private @Nonnull Set<GeneratedRoute> _generatedRoutes;
  private @Nullable Long _maxMetricExternalNetworks;
  private @Nullable Long _maxMetricStubNetworks;
  private @Nullable Long _maxMetricSummaryNetworks;
  private @Nullable Long _maxMetricTransitLinks;
  private transient Map<IpLink, OspfNeighbor> _ospfNeighbors;

  /** Mapping from interface name to an OSPF config */
  private @Nonnull Map<OspfNeighborConfigId, OspfNeighborConfig> _ospfNeighborConfigs;

  private final @Nonnull String _processId;
  private @Nonnull Double _referenceBandwidth;
  private @Nullable Boolean _rfc1583Compatible;
  private @Nonnull Ip _routerId;
  private int _summaryAdminCost;
  private long _summaryDiscardMetric;

  private OspfProcess(
      Map<RoutingProtocol, Integer> adminCosts,
      Map<Long, OspfArea> areas,
      @Nullable String exportPolicy,
      Set<String> exportPolicySources,
      Set<GeneratedRoute> generatedRoutes,
      @Nullable Long maxMetricExternalNetworks,
      @Nullable Long maxMetricStubNetworks,
      @Nullable Long maxMetricSummaryNetworks,
      @Nullable Long maxMetricTransitLinks,
      Map<OspfNeighborConfigId, OspfNeighborConfig> ospfNeighborConfigs,
      String processId,
      Double referenceBandwidth,
      @Nullable Boolean rfc1583Compatible,
      Ip routerId,
      Integer summaryAdminCost,
      long summaryDiscardMetric) {
    _adminCosts = adminCosts;
    _areas = areas;
    _exportPolicy = exportPolicy;
    _exportPolicySources = exportPolicySources;
    _generatedRoutes = generatedRoutes;
    _maxMetricExternalNetworks = maxMetricExternalNetworks;
    _maxMetricStubNetworks = maxMetricStubNetworks;
    _maxMetricSummaryNetworks = maxMetricSummaryNetworks;
    _maxMetricTransitLinks = maxMetricTransitLinks;
    _ospfNeighborConfigs = ospfNeighborConfigs;
    _processId = processId;
    _referenceBandwidth = referenceBandwidth;
    _rfc1583Compatible = rfc1583Compatible;
    _routerId = routerId;
    _summaryAdminCost = summaryAdminCost;
    _summaryDiscardMetric = summaryDiscardMetric;

    // transient
    _ospfNeighbors = new TreeMap<>();
  }

  @JsonCreator
  private static @Nonnull OspfProcess create(
      @JsonProperty(PROP_ADMIN_COSTS) @Nullable SortedMap<RoutingProtocol, Integer> adminCosts,
      @JsonProperty(PROP_AREAS) @Nullable SortedMap<Long, OspfArea> areas,
      @JsonProperty(PROP_EXPORT_POLICY) @Nullable String exportPolicy,
      @JsonProperty(PROP_EXPORT_POLICY_SOURCES) @Nullable SortedSet<String> exportPolicySources,
      @JsonProperty(PROP_GENERATED_ROUTES) @Nullable SortedSet<GeneratedRoute> generatedRoutes,
      @JsonProperty(PROP_MAX_METRIC_EXTERNAL_NETWORKS) @Nullable Long maxMetricExternalNetworks,
      @JsonProperty(PROP_MAX_METRIC_STUB_NETWORKS) @Nullable Long maxMetricStubNetworks,
      @JsonProperty(PROP_MAX_METRIC_SUMMARY_NETWORKS) @Nullable Long maxMetricSummaryNetworks,
      @JsonProperty(PROP_MAX_METRIC_TRANSIT_LINKS) @Nullable Long maxMetricTransitLinks,
      @JsonProperty(PROP_PROCESS_ID) @Nullable String processId,
      @JsonProperty(PROP_REFERENCE_BANDWIDTH) @Nullable Double referenceBandwidth,
      @JsonProperty(PROP_RFC1583) @Nullable Boolean rfc1583Compatible,
      @JsonProperty(PROP_ROUTER_ID) @Nullable Ip routerId,
      @JsonProperty(PROP_SUMMARY_ADMIN) @Nullable Integer summaryAdminCost,
      @JsonProperty(PROP_SUMMARY_DISCARD_METRIC) @Nullable Long ignoredSummaryDiscardMetric) {
    OspfProcess.Builder builder = builder();
    checkArgument(processId != null, "Missing %s", PROP_PROCESS_ID);
    builder.setProcessId(processId);
    ofNullable(referenceBandwidth).ifPresent(builder::setReferenceBandwidth);
    ofNullable(routerId).ifPresent(builder::setRouterId);
    ofNullable(adminCosts).ifPresent(builder::setAdminCosts);
    ofNullable(areas).ifPresent(builder::setAreas);
    builder.setExportPolicyName(exportPolicy);
    ofNullable(exportPolicySources).ifPresent(builder::setExportPolicySources);
    ofNullable(generatedRoutes).ifPresent(builder::setGeneratedRoutes);
    builder
        .setMaxMetricExternalNetworks(maxMetricExternalNetworks)
        .setMaxMetricStubNetworks(maxMetricStubNetworks)
        .setMaxMetricSummaryNetworks(maxMetricSummaryNetworks)
        .setMaxMetricTransitLinks(maxMetricTransitLinks);
    ofNullable(processId).ifPresent(builder::setProcessId);
    ofNullable(referenceBandwidth).ifPresent(builder::setReferenceBandwidth);
    builder.setRfc1583Compatible(rfc1583Compatible);
    ofNullable(routerId).ifPresent(builder::setRouterId);
    ofNullable(summaryAdminCost).ifPresent(builder::setSummaryAdminCost);
    return builder.build();
  }

  /** Compute default admin costs based on a given configuration format */
  public static Map<RoutingProtocol, Integer> computeDefaultAdminCosts(ConfigurationFormat format) {
    return REQUIRES_ADMIN.stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Function.identity(), rp -> rp.getDefaultAdministrativeCost(format)));
  }

  public int computeInterfaceCost(Interface i) {
    return computeInterfaceCost(_referenceBandwidth, i);
  }

  @VisibleForTesting
  static int computeInterfaceCost(Double referenceBandwidth, Interface i) {
    Integer ospfCost = i.getOspfCost();
    if (ospfCost != null) {
      return ospfCost;
    }

    String interfaceName = i.getName();
    checkState(
        i.getBandwidth() != null,
        "Interface %s on %s is missing bandwidth, cannot compute OSPF cost",
        interfaceName,
        i.getOwner().getHostname());
    return Math.max((int) (referenceBandwidth / i.getBandwidth()), 1);
  }

  /**
   * The admin costs assigned to routes by this process, for each OSPF routing protocol (see {@link
   * #REQUIRES_ADMIN})
   */
  @JsonIgnore
  public @Nonnull Map<RoutingProtocol, Integer> getAdminCosts() {
    return _adminCosts;
  }

  @JsonProperty(PROP_ADMIN_COSTS)
  private @Nonnull SortedMap<RoutingProtocol, Integer> getAdminCostsSorted() {
    return ImmutableSortedMap.copyOf(_adminCosts);
  }

  /** The OSPF areas contained in this process */
  @JsonIgnore
  public @Nonnull Map<Long, OspfArea> getAreas() {
    return _areas;
  }

  @JsonProperty(PROP_AREAS)
  private @Nonnull SortedMap<Long, OspfArea> getAreasSorted() {
    return ImmutableSortedMap.copyOf(_areas);
  }

  /**
   * The routing policy applied to routes in the main RIB to determine which ones are to be exported
   * into OSPF and how
   */
  @JsonProperty(PROP_EXPORT_POLICY)
  public @Nullable String getExportPolicy() {
    return _exportPolicy;
  }

  /**
   * Return the names of policies that contribute to the unified export policy. The resulting set is
   * immutable.
   */
  @JsonIgnore
  public @Nonnull Set<String> getExportPolicySources() {
    return _exportPolicySources;
  }

  @JsonProperty(PROP_EXPORT_POLICY_SOURCES)
  private @Nonnull SortedSet<String> getExportPolicySourcesSorted() {
    return ImmutableSortedSet.copyOf(_exportPolicySources);
  }

  /**
   * Generated IPV4 routes for the purpose of export into OSPF. These routes are not imported into
   * the main RIB.
   */
  @JsonIgnore
  public @Nonnull Set<GeneratedRoute> getGeneratedRoutes() {
    return _generatedRoutes;
  }

  @JsonProperty(PROP_GENERATED_ROUTES)
  private @Nonnull SortedSet<GeneratedRoute> getGeneratedRoutesSorted() {
    return ImmutableSortedSet.copyOf(_generatedRoutes);
  }

  @JsonProperty(PROP_MAX_METRIC_EXTERNAL_NETWORKS)
  public @Nullable Long getMaxMetricExternalNetworks() {
    return _maxMetricExternalNetworks;
  }

  @JsonProperty(PROP_MAX_METRIC_STUB_NETWORKS)
  public @Nullable Long getMaxMetricStubNetworks() {
    return _maxMetricStubNetworks;
  }

  @JsonProperty(PROP_MAX_METRIC_SUMMARY_NETWORKS)
  public @Nullable Long getMaxMetricSummaryNetworks() {
    return _maxMetricSummaryNetworks;
  }

  @JsonProperty(PROP_MAX_METRIC_TRANSIT_LINKS)
  public @Nullable Long getMaxMetricTransitLinks() {
    return _maxMetricTransitLinks;
  }

  @JsonIgnore
  public Map<IpLink, OspfNeighbor> getOspfNeighbors() {
    return _ospfNeighbors;
  }

  @JsonIgnore
  public Map<OspfNeighborConfigId, OspfNeighborConfig> getOspfNeighborConfigs() {
    return _ospfNeighborConfigs;
  }

  @JsonProperty(PROP_PROCESS_ID)
  public @Nonnull String getProcessId() {
    return _processId;
  }

  /**
   * The reference bandwidth by which an interface's bandwidth is divided to determine its OSPF cost
   */
  @JsonProperty(PROP_REFERENCE_BANDWIDTH)
  public @Nonnull Double getReferenceBandwidth() {
    return _referenceBandwidth;
  }

  @JsonProperty(PROP_RFC1583)
  public @Nullable Boolean getRfc1583Compatible() {
    return _rfc1583Compatible;
  }

  /** The router-id of this OSPF process */
  @JsonProperty(PROP_ROUTER_ID)
  public @Nonnull Ip getRouterId() {
    return _routerId;
  }

  /** Return the admin cost assigned to inter-area summaries */
  public int getSummaryAdminCost() {
    return _summaryAdminCost;
  }

  /** Return the metric for internal summary discard routes */
  public long getSummaryDiscardMetric() {
    return _summaryDiscardMetric;
  }

  /** Initialize interface costs for all interfaces that belong to this process */
  public void initInterfaceCosts(Configuration c) {
    _areas.values().stream()
        .flatMap(a -> a.getInterfaces().stream())
        .map(interfaceName -> c.getAllInterfaces().get(interfaceName))
        .filter(Interface::getActive)
        .filter(i -> i.getOspfSettings() != null)
        .forEach(i -> i.getOspfSettings().setCost(computeInterfaceCost(i)));
  }

  /**
   * Check if this process serves as and ABR (Area Border Router). An ABR has at least one interface
   * in area 0, and at least one interface in another area.
   */
  @JsonIgnore
  public boolean isAreaBorderRouter() {
    Set<Long> areas = _areas.keySet();
    return areas.contains(0L) && areas.size() > 1;
  }

  @JsonIgnore
  public void setAreas(Map<Long, OspfArea> areas) {
    // Ensure area numbers match up
    checkArgument(
        areas.entrySet().stream()
            .allMatch(entry -> entry.getKey() == entry.getValue().getAreaNumber()),
        "Area number does not match up with map key");
    _areas = areas;
  }

  public void addArea(OspfArea area) {
    _areas =
        ImmutableSortedMap.<Long, OspfArea>naturalOrder()
            .putAll(_areas)
            .put(area.getAreaNumber(), area)
            .build();
  }

  @JsonIgnore
  public void setExportPolicy(@Nullable String exportPolicy) {
    _exportPolicy = exportPolicy;
  }

  @JsonIgnore
  public void setExportPolicySources(SortedSet<String> exportPolicySources) {
    _exportPolicySources = ImmutableSortedSet.copyOf(exportPolicySources);
  }

  /**
   * Overwrite all generated route. See {@link #getGeneratedRoutes} for explanation of generated
   * routes.
   */
  @JsonIgnore
  public void setGeneratedRoutes(SortedSet<GeneratedRoute> generatedRoutes) {
    _generatedRoutes = ImmutableSortedSet.copyOf(generatedRoutes);
  }

  /** Add a generated route. See {@link #getGeneratedRoutes} for explanation of generated routes. */
  public void addGeneratedRoute(GeneratedRoute route) {
    _generatedRoutes =
        new ImmutableSortedSet.Builder<GeneratedRoute>(Ordering.natural())
            .addAll(_generatedRoutes)
            .add(route)
            .build();
  }

  @JsonIgnore
  public void setMaxMetricExternalNetworks(@Nullable Long maxMetricExternalNetworks) {
    _maxMetricExternalNetworks = maxMetricExternalNetworks;
  }

  @JsonIgnore
  public void setMaxMetricStubNetworks(@Nullable Long maxMetricStubNetworks) {
    _maxMetricStubNetworks = maxMetricStubNetworks;
  }

  @JsonIgnore
  public void setMaxMetricSummaryNetworks(@Nullable Long maxMetricSummaryNetworks) {
    _maxMetricSummaryNetworks = maxMetricSummaryNetworks;
  }

  @JsonIgnore
  public void setMaxMetricTransitLinks(@Nullable Long maxMetricTransitLinks) {
    _maxMetricTransitLinks = maxMetricTransitLinks;
  }

  @JsonIgnore
  public void setOspfNeighbors(Map<IpLink, OspfNeighbor> ospfNeighbors) {
    _ospfNeighbors = ospfNeighbors;
  }

  @JsonIgnore
  void setOspfNeighborConfigs(Map<OspfNeighborConfigId, OspfNeighborConfig> ospfNeighborConfigs) {
    _ospfNeighborConfigs = ospfNeighborConfigs;
  }

  @JsonIgnore
  public void setReferenceBandwidth(Double referenceBandwidth) {
    _referenceBandwidth = referenceBandwidth;
  }

  @JsonIgnore
  public void setRfc1583Compatible(@Nullable Boolean rfc1583Compatible) {
    _rfc1583Compatible = rfc1583Compatible;
  }

  @JsonIgnore
  public void setRouterId(@Nonnull Ip id) {
    _routerId = id;
  }

  @Deprecated
  @JsonProperty(PROP_NEIGHBORS)
  private void setNeighborsDeprecated(JsonNode ignoredN) {}
}
