package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.dataplane.rib.RibGroup;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.flow.OriginatingSessionScope;
import org.batfish.datamodel.isis.IsisProcess;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;

/** A virtual routing and forwarding (VRF) instance on a node. */
public class Vrf extends ComparableStructure<String> {

  public static class Builder {

    private @Nullable String _name;
    private @Nullable Supplier<String> _nameGenerator;
    private @Nullable Configuration _owner;
    private @Nullable String _resolutionPolicy;
    private @Nonnull Map<Long, EigrpProcess> _eigrpProcesses = ImmutableMap.of();
    private @Nullable VrfLeakConfig _vrfLeakConfig;

    private Builder(Supplier<String> nameGenerator) {
      _nameGenerator = nameGenerator;
    }

    public Vrf build() {
      checkArgument(_name != null || _nameGenerator != null, "Must set name before building");
      String name = _name != null ? _name : _nameGenerator.get();
      Vrf vrf = new Vrf(name);
      if (_owner != null) {
        _owner.getVrfs().put(name, vrf);
      }
      vrf.setEigrpProcesses(_eigrpProcesses);
      vrf.setVrfLeakConfig(_vrfLeakConfig);
      vrf.setResolutionPolicy(_resolutionPolicy);
      return vrf;
    }

    public Builder setEigrpProcesses(@Nonnull Map<Long, EigrpProcess> eigrpProcesses) {
      _eigrpProcesses = eigrpProcesses;
      return this;
    }

    public Builder setName(@Nullable String name) {
      _name = name;
      return this;
    }

    public Builder setOwner(@Nullable Configuration owner) {
      _owner = owner;
      return this;
    }

    public Builder setResolutionPolicy(@Nullable String resolutionPolicy) {
      _resolutionPolicy = resolutionPolicy;
      return this;
    }

    public Builder setVrfLeakConfig(VrfLeakConfig vrfLeakConfig) {
      _vrfLeakConfig = vrfLeakConfig;
      return this;
    }
  }

  private static final String PROP_BGP_PROCESS = "bgpProcess";
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_HAS_ORIGINATING_SESSIONS = "hasOriginatingSessions";
  private static final String PROP_FIREWALL_SESSION_VRF_INFO = "firewallSessionVrfInfo";
  private static final String PROP_GENERATED_ROUTES = "aggregateRoutes";
  private static final String PROP_ISIS_PROCESS = "isisProcess";
  private static final String PROP_EIGRP_PROCESSES = "eigrpProcesses";
  private static final String PROP_KERNEL_ROUTES = "kernelRoutes";
  private static final String PROP_OSPF_PROCESS = "ospfProcess";
  private static final String PROP_OSPF_PROCESSES = "ospfProcesses";
  private static final String PROP_RESOLUTION_POLICY = "resolutionPolicy";
  private static final String PROP_RIP_PROCESS = "ripProcess";
  private static final String PROP_STATIC_ROUTES = "staticRoutes";
  private static final String PROP_VRF_LEAK_CONFIG = "vrfLeakConfig";

  public static @Nonnull Builder builder() {
    return new Builder(null);
  }

  public static @Nonnull Builder builder(@Nonnull Supplier<String> nameGenerator) {
    return new Builder(nameGenerator);
  }

  private SortedMap<RoutingProtocol, RibGroup> _appliedRibGroups;
  private BgpProcess _bgpProcess;
  private String _description;
  private FirewallSessionVrfInfo _firewallSessionVrfInfo;
  private NavigableSet<GeneratedRoute> _generatedRoutes;
  private SortedMap<Long, EigrpProcess> _eigrpProcesses;
  private IsisProcess _isisProcess;
  private SortedSet<KernelRoute> _kernelRoutes;
  private @Nonnull SortedMap<String, OspfProcess> _ospfProcesses;
  private @Nullable String _resolutionPolicy;
  private RipProcess _ripProcess;
  private SnmpServer _snmpServer;
  private SortedSet<StaticRoute> _staticRoutes;
  private Map<Integer, Layer2Vni> _layer2Vnis;
  private Map<Integer, Layer3Vni> _layer3Vnis;
  private @Nullable VrfLeakConfig _vrfLeakConfig;
  private @Nonnull SourceIpInference _sourceIpInference;

  public Vrf(@Nonnull String name) {
    super(name);
    _appliedRibGroups = ImmutableSortedMap.of();
    _eigrpProcesses = ImmutableSortedMap.of();
    _generatedRoutes = new TreeSet<>();
    _kernelRoutes = ImmutableSortedSet.of();
    _ospfProcesses = ImmutableSortedMap.of();
    _staticRoutes = new TreeSet<>();
    _layer2Vnis = ImmutableMap.of();
    _layer3Vnis = ImmutableMap.of();
    _sourceIpInference = InferFromFib.instance();
  }

  @JsonCreator
  private static Vrf create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_OSPF_PROCESSES) @Nullable Map<String, OspfProcess> ospfProcesses,
      // For backwards compatible deserialization
      @JsonProperty(PROP_OSPF_PROCESS) @Nullable OspfProcess ospfProcess) {
    checkArgument(name != null, "%s must be provided", PROP_NAME);
    Vrf v = new Vrf(name);
    if (ospfProcesses != null) {
      v.setOspfProcesses(ImmutableSortedMap.copyOf(ospfProcesses));
    } else if (ospfProcess != null) {
      v.setOspfProcesses(ImmutableSortedMap.of(ospfProcess.getProcessId(), ospfProcess));
    }
    return v;
  }

  /** Return any RIB groups applied to a given routing protocol */
  public Map<RoutingProtocol, RibGroup> getAppliedRibGroups() {
    return _appliedRibGroups;
  }

  /** BGP routing process for this VRF. */
  @JsonProperty(PROP_BGP_PROCESS)
  public BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  /** Description for this VRF. */
  @JsonProperty(PROP_DESCRIPTION)
  public String getDescription() {
    return _description;
  }

  /**
   * Whether this VRF sets up sessions with an {@link OriginatingSessionScope} for accepted traffic.
   */
  @Deprecated
  @JsonIgnore
  @JsonProperty(PROP_HAS_ORIGINATING_SESSIONS)
  private boolean hasOriginatingSessions() {
    return false;
  }

  /**
   * If nonnull, VRF should create a session for ACCEPTED flows. The info object controls details of
   * the session.
   */
  @JsonProperty(PROP_FIREWALL_SESSION_VRF_INFO)
  public @Nullable FirewallSessionVrfInfo getFirewallSessionVrfInfo() {
    return _firewallSessionVrfInfo;
  }

  /** Generated IPV4 routes for this VRF. */
  @JsonProperty(PROP_GENERATED_ROUTES)
  public NavigableSet<GeneratedRoute> getGeneratedRoutes() {
    return _generatedRoutes;
  }

  /**
   * @return EIGRP routing processes for this VRF. This map cannot be modified.
   */
  @JsonProperty(PROP_EIGRP_PROCESSES)
  public Map<Long, EigrpProcess> getEigrpProcesses() {
    return _eigrpProcesses;
  }

  /** IS-IS routing process for this VRF. */
  @JsonProperty(PROP_ISIS_PROCESS)
  public IsisProcess getIsisProcess() {
    return _isisProcess;
  }

  @JsonProperty(PROP_KERNEL_ROUTES)
  public @Nonnull SortedSet<KernelRoute> getKernelRoutes() {
    return _kernelRoutes;
  }

  /** OSPF routing processes for this VRF, keyed on {@link OspfProcess#getProcessId()}. */
  @JsonProperty(PROP_OSPF_PROCESSES)
  public @Nonnull Map<String, OspfProcess> getOspfProcesses() {
    return _ospfProcesses;
  }

  @JsonProperty(PROP_RIP_PROCESS)
  public RipProcess getRipProcess() {
    return _ripProcess;
  }

  public SnmpServer getSnmpServer() {
    return _snmpServer;
  }

  /** Static routes for this VRF. */
  @JsonProperty(PROP_STATIC_ROUTES)
  public SortedSet<StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  @JsonIgnore
  public Map<Integer, Layer2Vni> getLayer2Vnis() {
    return _layer2Vnis;
  }

  public void setLayer2Vnis(Collection<Layer2Vni> layer2Vnis) {
    _layer2Vnis = toImmutableMap(layer2Vnis, Layer2Vni::getVni, Function.identity());
  }

  public void addLayer2Vni(Layer2Vni vni) {
    _layer2Vnis =
        ImmutableMap.<Integer, Layer2Vni>builder()
            .putAll(_layer2Vnis)
            .put(vni.getVni(), vni)
            .build();
  }

  @JsonIgnore
  public Map<Integer, Layer3Vni> getLayer3Vnis() {
    return _layer3Vnis;
  }

  public void setLayer3Vnis(Collection<Layer3Vni> layer3Vnis) {
    _layer3Vnis = toImmutableMap(layer3Vnis, Layer3Vni::getVni, Function.identity());
  }

  public void addLayer3Vni(Layer3Vni vni) {
    _layer3Vnis =
        ImmutableMap.<Integer, Layer3Vni>builder()
            .putAll(_layer3Vnis)
            .put(vni.getVni(), vni)
            .build();
  }

  @JsonProperty(PROP_VRF_LEAK_CONFIG)
  public @Nullable VrfLeakConfig getVrfLeakConfig() {
    return _vrfLeakConfig;
  }

  @JsonProperty(PROP_VRF_LEAK_CONFIG)
  public void setVrfLeakConfig(@Nullable VrfLeakConfig vrfLeakConfig) {
    _vrfLeakConfig = vrfLeakConfig;
  }

  public void setAppliedRibGroups(Map<RoutingProtocol, RibGroup> appliedRibGroups) {
    _appliedRibGroups = ImmutableSortedMap.copyOf(appliedRibGroups);
  }

  @JsonProperty(PROP_BGP_PROCESS)
  public void setBgpProcess(BgpProcess process) {
    _bgpProcess = process;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public void setDescription(String description) {
    _description = description;
  }

  /** Preserved for backward compatibility. */
  @Deprecated
  @JsonProperty(PROP_HAS_ORIGINATING_SESSIONS)
  private void setHasOriginatingSessions(boolean ignoredHasOriginatingSessions) {
    _firewallSessionVrfInfo = new FirewallSessionVrfInfo(true);
  }

  @JsonProperty(PROP_FIREWALL_SESSION_VRF_INFO)
  public void setFirewallSessionVrfInfo(FirewallSessionVrfInfo firewallSessionVrfInfo) {
    _firewallSessionVrfInfo = firewallSessionVrfInfo;
  }

  @JsonProperty(PROP_GENERATED_ROUTES)
  public void setGeneratedRoutes(NavigableSet<GeneratedRoute> generatedRoutes) {
    _generatedRoutes = generatedRoutes;
  }

  // For Jackson/Builder use only.
  @JsonProperty(PROP_EIGRP_PROCESSES)
  private void setEigrpProcesses(@Nullable Map<Long, EigrpProcess> eigrpProcesses) {
    _eigrpProcesses =
        ImmutableSortedMap.copyOf(firstNonNull(eigrpProcesses, ImmutableSortedMap.of()));
  }

  /** Add an {@link EigrpProcess} to this VRF */
  public void addEigrpProcess(@Nonnull EigrpProcess proc) {
    _eigrpProcesses =
        ImmutableSortedMap.<Long, EigrpProcess>naturalOrder()
            .putAll(_eigrpProcesses)
            .put(proc.getAsn(), proc)
            .build();
  }

  @JsonProperty(PROP_ISIS_PROCESS)
  public void setIsisProcess(IsisProcess process) {
    _isisProcess = process;
  }

  @JsonProperty(PROP_KERNEL_ROUTES)
  public void setKernelRoutes(@Nonnull SortedSet<KernelRoute> kernelRoutes) {
    _kernelRoutes = kernelRoutes;
  }

  @JsonIgnore
  public void setOspfProcesses(@Nonnull SortedMap<String, OspfProcess> processes) {
    _ospfProcesses = processes;
  }

  /**
   * Sets the VRF's OSPF processes to a map keyed on process IDs with the given {@code processes} as
   * values.
   */
  @JsonIgnore
  public void setOspfProcesses(@Nonnull Stream<OspfProcess> processes) {
    setOspfProcesses(
        processes.collect(
            ImmutableSortedMap.toImmutableSortedMap(
                Comparator.naturalOrder(), OspfProcess::getProcessId, Functions.identity())));
  }

  public void addOspfProcess(@Nonnull OspfProcess ospfProcess) {
    _ospfProcesses =
        ImmutableSortedMap.<String, OspfProcess>naturalOrder()
            .putAll(_ospfProcesses)
            .put(ospfProcess.getProcessId(), ospfProcess)
            .build();
  }

  @JsonProperty(PROP_RESOLUTION_POLICY)
  public @Nullable String getResolutionPolicy() {
    return _resolutionPolicy;
  }

  @JsonProperty(PROP_RESOLUTION_POLICY)
  public void setResolutionPolicy(@Nullable String resolutionPolicy) {
    _resolutionPolicy = resolutionPolicy;
  }

  @JsonProperty(PROP_RIP_PROCESS)
  public void setRipProcess(RipProcess ripProcess) {
    _ripProcess = ripProcess;
  }

  public void setSnmpServer(SnmpServer snmpServer) {
    _snmpServer = snmpServer;
  }

  @JsonProperty(PROP_STATIC_ROUTES)
  public void setStaticRoutes(SortedSet<StaticRoute> staticRoutes) {
    _staticRoutes = staticRoutes;
  }

  /** Source IP inference of locally generated IP packets, defaults to {@link InferFromFib}. */
  @JsonIgnore
  public @Nonnull SourceIpInference getSourceIpInference() {
    return _sourceIpInference;
  }

  public void setSourceIpInference(@Nonnull SourceIpInference sourceIpInference) {
    _sourceIpInference = sourceIpInference;
  }
}
