package org.batfish.datamodel.ospf;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Contains OSPFv3 settings for an interface. */
@ParametersAreNonnullByDefault
public final class Ospfv3InterfaceSettings implements Serializable {

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  /** Returns a builder populated with common OSPFv3 defaults. */
  public static @Nonnull Builder defaultSettingsBuilder() {
    return new Builder()
        .setEnabled(true)
        .setPassive(false)
        .setHelloInterval(10)
        .setDeadInterval(40)
        .setNetworkType(OspfNetworkType.POINT_TO_POINT);
  }

  public static final class Builder {
    private @Nullable Long _areaName;
    private @Nullable Integer _cost;
    private int _deadInterval;
    private boolean _enabled;
    private int _helloInterval;
    private @Nullable OspfNetworkType _networkType;
    private boolean _passive;
    private @Nullable String _process;

    private Builder() {
      _enabled = true;
    }

    public Builder setAreaName(@Nullable Long areaName) {
      _areaName = areaName;
      return this;
    }

    public Builder setCost(@Nullable Integer cost) {
      _cost = cost;
      return this;
    }

    public Builder setDeadInterval(int deadInterval) {
      _deadInterval = deadInterval;
      return this;
    }

    public Builder setEnabled(boolean enabled) {
      _enabled = enabled;
      return this;
    }

    public Builder setHelloInterval(int helloInterval) {
      _helloInterval = helloInterval;
      return this;
    }

    public Builder setNetworkType(@Nullable OspfNetworkType networkType) {
      _networkType = networkType;
      return this;
    }

    public Builder setPassive(boolean passive) {
      _passive = passive;
      return this;
    }

    public Builder setProcess(@Nullable String process) {
      _process = process;
      return this;
    }

    public Ospfv3InterfaceSettings build() {
      return new Ospfv3InterfaceSettings(
          _areaName,
          _cost,
          _deadInterval,
          _enabled,
          _helloInterval,
          _networkType,
          _passive,
          _process);
    }
  }

  private static final String PROP_AREA = "area";
  private static final String PROP_COST = "cost";
  private static final String PROP_DEAD_INTERVAL = "deadInterval";
  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_HELLO_INTERVAL = "helloInterval";
  private static final String PROP_NETWORK_TYPE = "networkType";
  private static final String PROP_PASSIVE = "passive";
  private static final String PROP_PROCESS = "process";

  @JsonCreator
  private static Ospfv3InterfaceSettings create(
      @JsonProperty(PROP_AREA) @Nullable Long area,
      @JsonProperty(PROP_COST) @Nullable Integer cost,
      @JsonProperty(PROP_DEAD_INTERVAL) @Nullable Integer deadInterval,
      @JsonProperty(PROP_ENABLED) @Nullable Boolean enabled,
      @JsonProperty(PROP_HELLO_INTERVAL) @Nullable Integer helloInterval,
      @JsonProperty(PROP_NETWORK_TYPE) @Nullable OspfNetworkType networkType,
      @JsonProperty(PROP_PASSIVE) @Nullable Boolean passive,
      @JsonProperty(PROP_PROCESS) @Nullable String process) {
    checkArgument(enabled != null, "OSPFv3 enabled must be specified");
    checkArgument(passive != null, "OSPFv3 passive must be specified");
    checkArgument(helloInterval != null, "OSPFv3 hello interval must be specified");
    checkArgument(deadInterval != null, "OSPFv3 dead interval must be specified");

    return new Ospfv3InterfaceSettings(
        area,
        cost,
        deadInterval,
        enabled,
        helloInterval,
        networkType,
        passive,
        process);
  }

  private Ospfv3InterfaceSettings(
      @Nullable Long areaName,
      @Nullable Integer cost,
      int deadInterval,
      boolean enabled,
      int helloInterval,
      @Nullable OspfNetworkType networkType,
      boolean passive,
      @Nullable String process) {
    _areaName = areaName;
    _cost = cost;
    _deadInterval = deadInterval;
    _enabled = enabled;
    _helloInterval = helloInterval;
    _networkType = networkType;
    _passive = passive;
    _process = process;
  }

  @JsonProperty(PROP_AREA)
  public @Nullable Long getAreaName() {
    return _areaName;
  }

  @JsonProperty(PROP_COST)
  public @Nullable Integer getCost() {
    return _cost;
  }

  @JsonProperty(PROP_DEAD_INTERVAL)
  public int getDeadInterval() {
    return _deadInterval;
  }

  @JsonProperty(PROP_ENABLED)
  public boolean getEnabled() {
    return _enabled;
  }

  @JsonProperty(PROP_HELLO_INTERVAL)
  public int getHelloInterval() {
    return _helloInterval;
  }

  @JsonProperty(PROP_NETWORK_TYPE)
  public @Nullable OspfNetworkType getNetworkType() {
    return _networkType;
  }

  @JsonProperty(PROP_PASSIVE)
  public boolean getPassive() {
    return _passive;
  }

  @JsonProperty(PROP_PROCESS)
  public @Nullable String getProcess() {
    return _process;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Ospfv3InterfaceSettings)) {
      return false;
    }
    Ospfv3InterfaceSettings other = (Ospfv3InterfaceSettings) o;
    return Objects.equals(_areaName, other._areaName)
        && Objects.equals(_cost, other._cost)
        && _deadInterval == other._deadInterval
        && _enabled == other._enabled
        && _helloInterval == other._helloInterval
        && _networkType == other._networkType
        && _passive == other._passive
        && Objects.equals(_process, other._process);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _areaName,
        _cost,
        _deadInterval,
        _enabled,
        _helloInterval,
        _networkType,
        _passive,
        _process);
  }

  private final @Nullable Long _areaName;
  private final @Nullable Integer _cost;
  private final int _deadInterval;
  private final boolean _enabled;
  private final int _helloInterval;
  private final @Nullable OspfNetworkType _networkType;
  private final boolean _passive;
  private final @Nullable String _process;
}
