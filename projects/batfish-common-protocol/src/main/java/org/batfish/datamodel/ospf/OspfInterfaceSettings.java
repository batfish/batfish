package org.batfish.datamodel.ospf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OspfInterfaceSettings implements Serializable {

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Long _ospfAreaName;
    private Integer _ospfCost;
    private Integer _ospfDeadInterval;
    private boolean _ospfEnabled;
    private Integer _ospfHelloMultiplier;
    private String _ospfInboundDistributeListPolicy;
    private OspfNetworkType _ospfNetworkType;
    private Boolean _ospfPassive;
    private Boolean _ospfPointToPoint;
    private String _ospfProcess;

    public Builder() {
      _ospfEnabled = true;
    }

    public Builder setAreaName(@Nullable Long ospfAreaName) {
      _ospfAreaName = ospfAreaName;
      return this;
    }

    public Builder setCost(@Nullable Integer ospfCost) {
      _ospfCost = ospfCost;
      return this;
    }

    public Builder setDeadInterval(Integer ospfDeadInterval) {
      _ospfDeadInterval = ospfDeadInterval;
      return this;
    }

    public Builder setEnabled(boolean ospfEnabled) {
      _ospfEnabled = ospfEnabled;
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

    public Builder setNetworkType(@Nullable OspfNetworkType ospfNetworkType) {
      _ospfNetworkType = ospfNetworkType;
      return this;
    }

    public Builder setPassive(Boolean ospfPassive) {
      _ospfPassive = ospfPassive;
      return this;
    }

    public Builder setPointToPoint(Boolean ospfPointToPoint) {
      _ospfPointToPoint = ospfPointToPoint;
      return this;
    }

    public Builder setProcess(String ospfProcess) {
      _ospfProcess = ospfProcess;
      return this;
    }

    public OspfInterfaceSettings build() {
      return new OspfInterfaceSettings(
          _ospfAreaName,
          _ospfCost,
          _ospfDeadInterval,
          _ospfEnabled,
          _ospfHelloMultiplier,
          _ospfInboundDistributeListPolicy,
          _ospfNetworkType,
          _ospfPassive,
          _ospfPointToPoint,
          _ospfProcess);
    }
  }

  private Long _ospfAreaName;
  private Integer _ospfCost;
  private Integer _ospfDeadInterval;
  private Boolean _ospfEnabled;
  private Integer _ospfHelloMultiplier;
  @Nullable private String _ospfInboundDistributeListPolicy;
  @Nonnull private OspfNetworkType _ospfNetworkType;
  private Boolean _ospfPassive;
  private Boolean _ospfPointToPoint;
  @Nonnull private String _ospfProcess;

  private static final String PROP_AREA = "area";
  private static final String PROP_COST = "cost";
  private static final String PROP_DEAD_INTERVAL = "deadInterval";
  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_HELLO_MULTIPLIER = "helloMultiplier";
  private static final String PROP_INBOUND_DISTRIBUTE_LIST_POLICY = "inboundDistributeListPolicy";
  private static final String PROP_NETWORK_TYPE = "networkType";
  private static final String PROP_PASSIVE = "passive";
  private static final String PROP_POINT_TO_POINT = "pointToPoint";
  private static final String PROP_PROCESS = "process";

  @JsonCreator
  private static OspfInterfaceSettings create(
      @JsonProperty(PROP_AREA) Long area,
      @JsonProperty(PROP_COST) Integer cost,
      @JsonProperty(PROP_DEAD_INTERVAL) Integer deadInterval,
      @JsonProperty(PROP_ENABLED) Boolean enabled,
      @JsonProperty(PROP_HELLO_MULTIPLIER) Integer helloMultiplier,
      @JsonProperty(PROP_INBOUND_DISTRIBUTE_LIST_POLICY) String inboundDistributeListPolicy,
      @JsonProperty(PROP_NETWORK_TYPE) OspfNetworkType networkType,
      @JsonProperty(PROP_PASSIVE) Boolean passive,
      @JsonProperty(PROP_POINT_TO_POINT) Boolean pointToPoint,
      @JsonProperty(PROP_PROCESS) String process) {
    return new OspfInterfaceSettings(
        area,
        cost,
        deadInterval,
        enabled,
        helloMultiplier,
        inboundDistributeListPolicy,
        networkType,
        passive,
        pointToPoint,
        process);
  }

  private OspfInterfaceSettings(
      Long area,
      Integer cost,
      Integer deadInterval,
      boolean enabled,
      Integer helloMultiplier,
      String inboundDistributeListPolicy,
      OspfNetworkType networkType,
      Boolean passive,
      Boolean pointToPoint,
      String process) {
    _ospfAreaName = area;
    _ospfCost = cost;
    _ospfDeadInterval = deadInterval;
    _ospfEnabled = enabled;
    _ospfHelloMultiplier = helloMultiplier;
    _ospfInboundDistributeListPolicy = inboundDistributeListPolicy;
    _ospfNetworkType = networkType;
    _ospfPassive = passive;
    _ospfPointToPoint = pointToPoint;
    _ospfProcess = process;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _ospfAreaName,
        _ospfCost,
        _ospfDeadInterval,
        _ospfEnabled,
        _ospfHelloMultiplier,
        _ospfInboundDistributeListPolicy,
        _ospfNetworkType,
        _ospfPassive,
        _ospfPointToPoint,
        _ospfProcess);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof OspfInterfaceSettings)) {
      return false;
    }
    OspfInterfaceSettings other = (OspfInterfaceSettings) o;
    if (!Objects.equals(_ospfAreaName, other._ospfAreaName)) {
      return false;
    }
    if (!Objects.equals(_ospfCost, other._ospfCost)) {
      return false;
    }
    if (!Objects.equals(_ospfDeadInterval, other._ospfDeadInterval)) {
      return false;
    }
    if (!Objects.equals(_ospfEnabled, other._ospfEnabled)) {
      return false;
    }
    if (!Objects.equals(_ospfHelloMultiplier, other._ospfHelloMultiplier)) {
      return false;
    }
    if (!Objects.equals(_ospfInboundDistributeListPolicy, other._ospfInboundDistributeListPolicy)) {
      return false;
    }
    if (!Objects.equals(_ospfNetworkType, other._ospfNetworkType)) {
      return false;
    }
    if (!Objects.equals(_ospfPassive, other._ospfPassive)) {
      return false;
    }
    if (!Objects.equals(_ospfPointToPoint, other._ospfPointToPoint)) {
      return false;
    }
    if (!Objects.equals(_ospfProcess, other._ospfProcess)) {
      return false;
    }
    return true;
  }

  @JsonProperty(PROP_AREA)
  public Long getAreaName() {
    return _ospfAreaName;
  }

  @JsonProperty(PROP_COST)
  public Integer getCost() {
    return _ospfCost;
  }

  @JsonProperty(PROP_DEAD_INTERVAL)
  public Integer getDeadInterval() {
    return _ospfDeadInterval;
  }

  @JsonProperty(PROP_ENABLED)
  public boolean getEnabled() {
    return _ospfEnabled;
  }

  @JsonProperty(PROP_HELLO_MULTIPLIER)
  public Integer getHelloMultiplier() {
    return _ospfHelloMultiplier;
  }

  @JsonProperty(PROP_INBOUND_DISTRIBUTE_LIST_POLICY)
  @Nullable
  public String getInboundDistributeListPolicy() {
    return _ospfInboundDistributeListPolicy;
  }

  @JsonProperty(PROP_NETWORK_TYPE)
  public OspfNetworkType getNetworkType() {
    return _ospfNetworkType;
  }

  @JsonProperty(PROP_PASSIVE)
  public Boolean getPassive() {
    return _ospfPassive;
  }

  @JsonProperty(PROP_POINT_TO_POINT)
  public Boolean getPointToPoint() {
    return _ospfPointToPoint;
  }

  @JsonProperty(PROP_PROCESS)
  public String getProcess() {
    return _ospfProcess;
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
}
