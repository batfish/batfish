package org.batfish.datamodel.ospf;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Contains OSPF settings for an OSPF interface. */
@ParametersAreNonnullByDefault
public final class OspfInterfaceSettings implements Serializable {

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  /** Returns a builder with default values for most OSPF settings, useful for tests. */
  public static @Nonnull Builder defaultSettingsBuilder() {
    return new Builder()
        .setEnabled(true)
        .setPassive(false)
        .setHelloInterval(10)
        .setDeadInterval(40)
        .setNetworkType(OspfNetworkType.POINT_TO_POINT);
  }

  public static final class Builder {
    private @Nullable OspfAddresses _ospfAddresses;
    private Long _ospfAreaName;
    private Integer _ospfCost;
    private int _ospfDeadInterval;
    private boolean _ospfEnabled;
    private int _ospfHelloInterval;
    private Integer _ospfHelloMultiplier;
    private String _ospfInboundDistributeListPolicy;
    private Set<Ip> _ospfNbmaNeighbors;
    private OspfNetworkType _ospfNetworkType;
    private Boolean _ospfPassive;
    private String _ospfProcess;
    private @Nullable String _ospfType5FilterPolicy;

    public Builder() {
      _ospfEnabled = true;
    }

    public @Nonnull Builder setOspfAddresses(@Nullable OspfAddresses ospfAddresses) {
      _ospfAddresses = ospfAddresses;
      return this;
    }

    public Builder setAreaName(@Nullable Long ospfAreaName) {
      _ospfAreaName = ospfAreaName;
      return this;
    }

    public Builder setCost(@Nullable Integer ospfCost) {
      _ospfCost = ospfCost;
      return this;
    }

    public Builder setDeadInterval(int ospfDeadInterval) {
      _ospfDeadInterval = ospfDeadInterval;
      return this;
    }

    public Builder setEnabled(boolean ospfEnabled) {
      _ospfEnabled = ospfEnabled;
      return this;
    }

    public Builder setHelloInterval(int ospfHelloInterval) {
      _ospfHelloInterval = ospfHelloInterval;
      return this;
    }

    public Builder setHelloMultiplier(Integer ospfHelloMultiplier) {
      _ospfHelloMultiplier = ospfHelloMultiplier;
      return this;
    }

    public Builder setInboundDistributeListPolicy(String ospfInboundDistributeListPolicy) {
      _ospfInboundDistributeListPolicy = ospfInboundDistributeListPolicy;
      return this;
    }

    public Builder setNbmaNeighbors(@Nonnull Set<Ip> ospfNbmaNeighbors) {
      _ospfNbmaNeighbors = ImmutableSet.copyOf(ospfNbmaNeighbors);
      return this;
    }

    public Builder setNetworkType(@Nullable OspfNetworkType ospfNetworkType) {
      _ospfNetworkType = ospfNetworkType;
      return this;
    }

    public Builder setPassive(boolean ospfPassive) {
      _ospfPassive = ospfPassive;
      return this;
    }

    public Builder setProcess(String ospfProcess) {
      _ospfProcess = ospfProcess;
      return this;
    }

    public Builder setType5FilterPolicy(@Nullable String ospfType5FilterPolicy) {
      _ospfType5FilterPolicy = ospfType5FilterPolicy;
      return this;
    }

    public OspfInterfaceSettings build() {
      return create(
          _ospfAddresses,
          _ospfAreaName,
          _ospfCost,
          _ospfDeadInterval,
          _ospfEnabled,
          _ospfHelloInterval,
          _ospfHelloMultiplier,
          _ospfInboundDistributeListPolicy,
          _ospfNbmaNeighbors,
          _ospfNetworkType,
          _ospfPassive,
          _ospfProcess,
          _ospfType5FilterPolicy);
    }
  }

  private @Nullable OspfAddresses _ospfAddresses;
  private @Nullable Long _ospfAreaName;
  private @Nullable Integer _ospfCost;
  private int _ospfDeadInterval;
  private boolean _ospfEnabled;
  private int _ospfHelloInterval;
  private @Nullable Integer _ospfHelloMultiplier;
  private @Nullable String _ospfInboundDistributeListPolicy;
  private @Nonnull Set<Ip> _ospfNbmaNeighbors;
  private @Nullable OspfNetworkType _ospfNetworkType;
  private boolean _ospfPassive;
  private @Nullable String _ospfProcess;
  private @Nullable String _ospfType5FilterPolicy;

  private static final String PROP_AREA = "area";
  private static final String PROP_COST = "cost";
  private static final String PROP_DEAD_INTERVAL = "deadInterval";
  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_HELLO_MULTIPLIER = "helloMultiplier";
  private static final String PROP_HELLO_INTERVAL = "helloInterval";
  private static final String PROP_INBOUND_DISTRIBUTE_LIST_POLICY = "inboundDistributeListPolicy";
  private static final String PROP_NBMA_NEIGHBORS = "nbmaNeighbors";
  private static final String PROP_NETWORK_TYPE = "networkType";
  private static final String PROP_OSPF_ADDRESSES = "ospfAddresses";
  private static final String PROP_PASSIVE = "passive";
  private static final String PROP_PROCESS = "process";
  private static final String PROP_TYPE_5_FILTER_POLICY = "type5FilterPolicy";

  @JsonCreator
  private static OspfInterfaceSettings create(
      @JsonProperty(PROP_OSPF_ADDRESSES) @Nullable OspfAddresses addresses,
      @JsonProperty(PROP_AREA) @Nullable Long area,
      @JsonProperty(PROP_COST) @Nullable Integer cost,
      @JsonProperty(PROP_DEAD_INTERVAL) @Nullable Integer deadInterval,
      @JsonProperty(PROP_ENABLED) @Nullable Boolean enabled,
      @JsonProperty(PROP_HELLO_INTERVAL) @Nullable Integer helloInterval,
      @JsonProperty(PROP_HELLO_MULTIPLIER) @Nullable Integer helloMultiplier,
      @JsonProperty(PROP_INBOUND_DISTRIBUTE_LIST_POLICY) @Nullable
          String inboundDistributeListPolicy,
      @JsonProperty(PROP_NBMA_NEIGHBORS) @Nullable Set<Ip> nbmaNeighbors,
      @JsonProperty(PROP_NETWORK_TYPE) @Nullable OspfNetworkType networkType,
      @JsonProperty(PROP_PASSIVE) @Nullable Boolean passive,
      @JsonProperty(PROP_PROCESS) @Nullable String process,
      @JsonProperty(PROP_TYPE_5_FILTER_POLICY) @Nullable String type5FilterPolicy) {
    checkArgument(enabled != null, "OSPF enabled must be specified");
    checkArgument(passive != null, "OSPF passive must be specified");
    checkArgument(helloInterval != null, "OSPF hello interval must be specified");
    checkArgument(deadInterval != null, "OSPF dead interval must be specified");
    return new OspfInterfaceSettings(
        addresses,
        area,
        cost,
        deadInterval,
        enabled,
        helloInterval,
        helloMultiplier,
        inboundDistributeListPolicy,
        Optional.ofNullable(nbmaNeighbors).orElse(ImmutableSet.of()),
        networkType,
        passive,
        process,
        type5FilterPolicy);
  }

  private OspfInterfaceSettings(
      @Nullable OspfAddresses addresses,
      @Nullable Long area,
      @Nullable Integer cost,
      int deadInterval,
      boolean enabled,
      int helloInterval,
      @Nullable Integer helloMultiplier,
      @Nullable String inboundDistributeListPolicy,
      @Nonnull Set<Ip> nbmaNeighbors,
      @Nullable OspfNetworkType networkType,
      boolean passive,
      @Nullable String process,
      @Nullable String type5FilterPolicy) {
    _ospfAddresses = addresses;
    _ospfAreaName = area;
    _ospfCost = cost;
    _ospfDeadInterval = deadInterval;
    _ospfEnabled = enabled;
    _ospfHelloInterval = helloInterval;
    _ospfHelloMultiplier = helloMultiplier;
    _ospfInboundDistributeListPolicy = inboundDistributeListPolicy;
    _ospfNbmaNeighbors = ImmutableSet.copyOf(nbmaNeighbors);
    _ospfNetworkType = networkType;
    _ospfPassive = passive;
    _ospfProcess = process;
    _ospfType5FilterPolicy = type5FilterPolicy;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _ospfAddresses,
        _ospfAreaName,
        _ospfCost,
        _ospfDeadInterval,
        _ospfEnabled,
        _ospfHelloInterval,
        _ospfHelloMultiplier,
        _ospfInboundDistributeListPolicy,
        _ospfNbmaNeighbors,
        _ospfNetworkType,
        _ospfPassive,
        _ospfProcess,
        _ospfType5FilterPolicy);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof OspfInterfaceSettings)) {
      return false;
    }
    OspfInterfaceSettings other = (OspfInterfaceSettings) o;
    return Objects.equals(_ospfAddresses, other._ospfAddresses)
        && Objects.equals(_ospfAreaName, other._ospfAreaName)
        && Objects.equals(_ospfCost, other._ospfCost)
        && _ospfDeadInterval == other._ospfDeadInterval
        && _ospfEnabled == other._ospfEnabled
        && _ospfHelloInterval == other._ospfHelloInterval
        && Objects.equals(_ospfHelloMultiplier, other._ospfHelloMultiplier)
        && Objects.equals(_ospfInboundDistributeListPolicy, other._ospfInboundDistributeListPolicy)
        && Objects.equals(_ospfNbmaNeighbors, other._ospfNbmaNeighbors)
        && Objects.equals(_ospfNetworkType, other._ospfNetworkType)
        && _ospfPassive == other._ospfPassive
        && Objects.equals(_ospfProcess, other._ospfProcess)
        && Objects.equals(_ospfType5FilterPolicy, other._ospfType5FilterPolicy);
  }

  /** The OSPF area to which this interface belongs. */
  @JsonProperty(PROP_AREA)
  public @Nullable Long getAreaName() {
    return _ospfAreaName;
  }

  /** The OSPF cost of this interface. */
  @JsonProperty(PROP_COST)
  public @Nullable Integer getCost() {
    return _ospfCost;
  }

  /** Dead-interval in seconds for OSPF updates. */
  @JsonProperty(PROP_DEAD_INTERVAL)
  public int getDeadInterval() {
    return _ospfDeadInterval;
  }

  /** Whether or not OSPF is enabled at all on this interface (either actively or passively). */
  @JsonProperty(PROP_ENABLED)
  public boolean getEnabled() {
    return _ospfEnabled;
  }

  /** Hello-interval in seconds for OSPF updates. */
  @JsonProperty(PROP_HELLO_INTERVAL)
  public int getHelloInterval() {
    return _ospfHelloInterval;
  }

  /** Number of OSPF packets to send out during dead-interval period for fast OSPF updates. */
  @JsonProperty(PROP_HELLO_MULTIPLIER)
  public @Nullable Integer getHelloMultiplier() {
    return _ospfHelloMultiplier;
  }

  /**
   * Returns name of the routing policy which is generated from the Global and Interface level
   * inbound distribute-lists for OSPF
   */
  @JsonProperty(PROP_INBOUND_DISTRIBUTE_LIST_POLICY)
  public @Nullable String getInboundDistributeListPolicy() {
    return _ospfInboundDistributeListPolicy;
  }

  @JsonProperty(PROP_NBMA_NEIGHBORS)
  private @Nonnull SortedSet<Ip> getJacksonNbmaNeighbors() {
    return ImmutableSortedSet.copyOf(_ospfNbmaNeighbors);
  }

  public @Nonnull Set<Ip> getNbmaNeighbors() {
    return _ospfNbmaNeighbors;
  }

  /** OSPF network type for this interface. */
  @JsonProperty(PROP_NETWORK_TYPE)
  public @Nullable OspfNetworkType getNetworkType() {
    return _ospfNetworkType;
  }

  @JsonProperty(PROP_OSPF_ADDRESSES)
  public @Nullable OspfAddresses getOspfAddresses() {
    return _ospfAddresses;
  }

  /**
   * Whether or not OSPF is enabled passively on this interface. If passive, the interface cannot
   * establish a neighbor relationship, but its subnets are still advertised.
   */
  @JsonProperty(PROP_PASSIVE)
  public boolean getPassive() {
    return _ospfPassive;
  }

  /** The OSPF process this interface is associated with. */
  @JsonProperty(PROP_PROCESS)
  public @Nullable String getProcess() {
    return _ospfProcess;
  }

  /**
   * Returns name of the routing policy used to filter type 5 LSAs. Does not update routes - just
   * filters
   */
  @JsonProperty(PROP_TYPE_5_FILTER_POLICY)
  public @Nullable String getType5FilterPolicy() {
    return _ospfType5FilterPolicy;
  }

  public void setCost(int cost) {
    _ospfCost = cost;
  }

  public void setInboundDistributeListPolicy(String inboundDistributeListPolicy) {
    _ospfInboundDistributeListPolicy = inboundDistributeListPolicy;
  }

  public void setHelloMultiplier(Integer helloMultiplier) {
    _ospfHelloMultiplier = helloMultiplier;
  }

  public void setType5FilterPolicy(@Nullable String type5FilterPolicy) {
    _ospfType5FilterPolicy = type5FilterPolicy;
  }
}
