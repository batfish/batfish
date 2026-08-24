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

  public static final int DEFAULT_PRIORITY = 1;
  public static final int DEFAULT_RETRANSMIT_INTERVAL = 5;
  public static final int DEFAULT_TRANSIT_DELAY = 1;

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
        .setNetworkType(OspfNetworkType.POINT_TO_POINT)
        .setPriority(DEFAULT_PRIORITY)
        .setRetransmitInterval(DEFAULT_RETRANSMIT_INTERVAL)
        .setTransitDelay(DEFAULT_TRANSIT_DELAY);
  }

  public static final class Builder {
    private @Nullable Long _areaName;
    private @Nullable Ospfv3Authentication
        _authentication;
    private boolean _bfdEnabled;
    private @Nullable Integer _cost;
    private int _deadInterval;
    private boolean _enabled;
    private int _helloInterval;
    private @Nullable OspfNetworkType _networkType;
    private boolean _passive;
    private int _priority;
    private @Nullable String _process;
    private int _retransmitInterval;
    private int _transitDelay;

    private Builder() {
      _enabled = true;
      _priority = DEFAULT_PRIORITY;
      _retransmitInterval =
          DEFAULT_RETRANSMIT_INTERVAL;
      _transitDelay =
          DEFAULT_TRANSIT_DELAY;
    }

    public Builder setAreaName(
        @Nullable Long areaName) {
      _areaName = areaName;
      return this;
    }

    public Builder setAuthentication(
        @Nullable Ospfv3Authentication authentication) {

      _authentication =
          authentication;

      return this;
    }

    public Builder setBfdEnabled(
        boolean bfdEnabled) {

      _bfdEnabled =
          bfdEnabled;

      return this;
    }

    public Builder setCost(
        @Nullable Integer cost) {
      _cost = cost;
      return this;
    }

    public Builder setDeadInterval(
        int deadInterval) {
      _deadInterval = deadInterval;
      return this;
    }

    public Builder setEnabled(
        boolean enabled) {
      _enabled = enabled;
      return this;
    }

    public Builder setHelloInterval(
        int helloInterval) {
      _helloInterval = helloInterval;
      return this;
    }

    public Builder setNetworkType(
        @Nullable OspfNetworkType networkType) {
      _networkType = networkType;
      return this;
    }

    public Builder setPassive(
        boolean passive) {
      _passive = passive;
      return this;
    }

    public Builder setPriority(
        int priority) {
      _priority = priority;
      return this;
    }

    public Builder setProcess(
        @Nullable String process) {
      _process = process;
      return this;
    }

    public Builder setRetransmitInterval(
        int retransmitInterval) {
      _retransmitInterval =
          retransmitInterval;
      return this;
    }

    public Builder setTransitDelay(
        int transitDelay) {
      _transitDelay = transitDelay;
      return this;
    }

    public Ospfv3InterfaceSettings build() {
      return new Ospfv3InterfaceSettings(
          _areaName,
          _authentication,
          _bfdEnabled,
          _cost,
          _deadInterval,
          _enabled,
          _helloInterval,
          _networkType,
          _passive,
          _priority,
          _process,
          _retransmitInterval,
          _transitDelay);
    }
  }

  private static final String PROP_AREA =
      "area";
  private static final String PROP_AUTHENTICATION =
      "authentication";
  private static final String PROP_BFD_ENABLED =
      "bfdEnabled";
  private static final String PROP_COST =
      "cost";
  private static final String PROP_DEAD_INTERVAL =
      "deadInterval";
  private static final String PROP_ENABLED =
      "enabled";
  private static final String PROP_HELLO_INTERVAL =
      "helloInterval";
  private static final String PROP_NETWORK_TYPE =
      "networkType";
  private static final String PROP_PASSIVE =
      "passive";
  private static final String PROP_PRIORITY =
      "priority";
  private static final String PROP_PROCESS =
      "process";
  private static final String PROP_RETRANSMIT_INTERVAL =
      "retransmitInterval";
  private static final String PROP_TRANSIT_DELAY =
      "transitDelay";

  @JsonCreator
  private static Ospfv3InterfaceSettings create(
      @JsonProperty(PROP_AREA)
          @Nullable Long area,
      @JsonProperty(PROP_AUTHENTICATION)
          @Nullable Ospfv3Authentication authentication,
      @JsonProperty(PROP_BFD_ENABLED)
          @Nullable Boolean bfdEnabled,
      @JsonProperty(PROP_COST)
          @Nullable Integer cost,
      @JsonProperty(PROP_DEAD_INTERVAL)
          @Nullable Integer deadInterval,
      @JsonProperty(PROP_ENABLED)
          @Nullable Boolean enabled,
      @JsonProperty(PROP_HELLO_INTERVAL)
          @Nullable Integer helloInterval,
      @JsonProperty(PROP_NETWORK_TYPE)
          @Nullable OspfNetworkType networkType,
      @JsonProperty(PROP_PASSIVE)
          @Nullable Boolean passive,
      @JsonProperty(PROP_PRIORITY)
          @Nullable Integer priority,
      @JsonProperty(PROP_PROCESS)
          @Nullable String process,
      @JsonProperty(PROP_RETRANSMIT_INTERVAL)
          @Nullable Integer retransmitInterval,
      @JsonProperty(PROP_TRANSIT_DELAY)
          @Nullable Integer transitDelay) {

    checkArgument(
        enabled != null,
        "OSPFv3 enabled must be specified");

    checkArgument(
        passive != null,
        "OSPFv3 passive must be specified");

    checkArgument(
        helloInterval != null,
        "OSPFv3 hello interval must be specified");

    checkArgument(
        deadInterval != null,
        "OSPFv3 dead interval must be specified");

    return new Ospfv3InterfaceSettings(
        area,
        authentication,
        bfdEnabled != null
            && bfdEnabled,
        cost,
        deadInterval,
        enabled,
        helloInterval,
        networkType,
        passive,
        priority != null
            ? priority
            : DEFAULT_PRIORITY,
        process,
        retransmitInterval != null
            ? retransmitInterval
            : DEFAULT_RETRANSMIT_INTERVAL,
        transitDelay != null
            ? transitDelay
            : DEFAULT_TRANSIT_DELAY);
  }

  private Ospfv3InterfaceSettings(
      @Nullable Long areaName,
      @Nullable Ospfv3Authentication authentication,
      boolean bfdEnabled,
      @Nullable Integer cost,
      int deadInterval,
      boolean enabled,
      int helloInterval,
      @Nullable OspfNetworkType networkType,
      boolean passive,
      int priority,
      @Nullable String process,
      int retransmitInterval,
      int transitDelay) {

    checkArgument(
        priority >= 0 && priority <= 255,
        "Invalid OSPFv3 interface priority %s",
        priority);

    checkArgument(
        retransmitInterval >= 1
            && retransmitInterval <= 3600,
        "Invalid OSPFv3 retransmit interval %s",
        retransmitInterval);

    checkArgument(
        transitDelay >= 1
            && transitDelay <= 3600,
        "Invalid OSPFv3 transit delay %s",
        transitDelay);

    _areaName = areaName;
    _authentication =
        authentication;
    _bfdEnabled =
        bfdEnabled;
    _cost = cost;
    _deadInterval = deadInterval;
    _enabled = enabled;
    _helloInterval = helloInterval;
    _networkType = networkType;
    _passive = passive;
    _priority = priority;
    _process = process;
    _retransmitInterval =
        retransmitInterval;
    _transitDelay = transitDelay;
  }

  @JsonProperty(PROP_AREA)
  public @Nullable Long getAreaName() {
    return _areaName;
  }

  @JsonProperty(PROP_AUTHENTICATION)
  public @Nullable Ospfv3Authentication
      getAuthentication() {

    return _authentication;
  }

  @JsonProperty(PROP_BFD_ENABLED)
  public boolean getBfdEnabled() {
    return _bfdEnabled;
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
  public @Nullable OspfNetworkType
      getNetworkType() {
    return _networkType;
  }

  @JsonProperty(PROP_PASSIVE)
  public boolean getPassive() {
    return _passive;
  }

  @JsonProperty(PROP_PRIORITY)
  public int getPriority() {
    return _priority;
  }

  @JsonProperty(PROP_PROCESS)
  public @Nullable String getProcess() {
    return _process;
  }

  @JsonProperty(PROP_RETRANSMIT_INTERVAL)
  public int getRetransmitInterval() {
    return _retransmitInterval;
  }

  @JsonProperty(PROP_TRANSIT_DELAY)
  public int getTransitDelay() {
    return _transitDelay;
  }

  @Override
  public boolean equals(
      @Nullable Object o) {
    if (this == o) {
      return true;
    }

    if (!(o
        instanceof Ospfv3InterfaceSettings)) {
      return false;
    }

    Ospfv3InterfaceSettings other =
        (Ospfv3InterfaceSettings) o;

    return Objects.equals(
            _areaName,
            other._areaName)
        && Objects.equals(
            _authentication,
            other._authentication)
        && _bfdEnabled
            == other._bfdEnabled
        && Objects.equals(
            _cost,
            other._cost)
        && _deadInterval
            == other._deadInterval
        && _enabled
            == other._enabled
        && _helloInterval
            == other._helloInterval
        && _networkType
            == other._networkType
        && _passive
            == other._passive
        && _priority
            == other._priority
        && Objects.equals(
            _process,
            other._process)
        && _retransmitInterval
            == other._retransmitInterval
        && _transitDelay
            == other._transitDelay;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _areaName,
        _authentication,
        _bfdEnabled,
        _cost,
        _deadInterval,
        _enabled,
        _helloInterval,
        _networkType,
        _passive,
        _priority,
        _process,
        _retransmitInterval,
        _transitDelay);
  }

  private final @Nullable Long _areaName;
  private final @Nullable Ospfv3Authentication
      _authentication;
  private final boolean _bfdEnabled;
  private final @Nullable Integer _cost;
  private final int _deadInterval;
  private final boolean _enabled;
  private final int _helloInterval;
  private final @Nullable OspfNetworkType
      _networkType;
  private final boolean _passive;
  private final int _priority;
  private final @Nullable String _process;
  private final int _retransmitInterval;
  private final int _transitDelay;
}
