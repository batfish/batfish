package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;

public class IsisInterfaceLevelSettings implements Serializable {

  private static final String PROP_COST = "cost";

  private static final String PROP_HELLO_AUTHENTICATION_KEY = "helloAuthenticationKey";

  private static final String PROP_HELLO_AUTHENTICATION_TYPE = "helloAuthenticationType";

  private static final String PROP_HELLO_INTERVAL = "helloInterval";

  private static final String PROP_HOLD_TIME = "holdTime";

  private static final String PROP_MODE = "mode";

  private static final long serialVersionUID = 1L;

  private final Integer _cost;

  private final String _helloAuthenticationKey;

  private final IsisHelloAuthenticationType _helloAuthenticationType;

  private final Integer _helloInterval;

  private final Integer _holdTime;

  private final IsisInterfaceMode _mode;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IsisInterfaceLevelSettings)) {
      return false;
    }
    IsisInterfaceLevelSettings rhs = (IsisInterfaceLevelSettings) obj;
    return Objects.equals(_cost, rhs._cost)
        && Objects.equals(_helloAuthenticationKey, rhs._helloAuthenticationKey)
        && Objects.equals(_helloAuthenticationType, rhs._helloAuthenticationType)
        && Objects.equals(_helloInterval, rhs._helloInterval)
        && Objects.equals(_holdTime, rhs._holdTime)
        && Objects.equals(_mode, rhs._mode);
  }

  @JsonProperty(PROP_COST)
  public @Nullable Integer getCost() {
    return _cost;
  }

  @JsonProperty(PROP_HELLO_AUTHENTICATION_KEY)
  public @Nullable String getHelloAuthenticationKey() {
    return _helloAuthenticationKey;
  }

  @JsonProperty(PROP_HELLO_AUTHENTICATION_TYPE)
  public @Nullable IsisHelloAuthenticationType getHelloAuthenticationType() {
    return _helloAuthenticationType;
  }

  @JsonProperty(PROP_HELLO_INTERVAL)
  public @Nullable Integer getHelloInterval() {
    return _helloInterval;
  }

  @JsonProperty(PROP_HOLD_TIME)
  public @Nullable Integer getHoldTime() {
    return _holdTime;
  }

  @JsonProperty(PROP_MODE)
  public @Nullable IsisInterfaceMode getMode() {
    return _mode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _cost,
        _helloAuthenticationKey,
        _helloAuthenticationType,
        _helloInterval,
        _holdTime,
        _mode != null ? _mode.ordinal() : -1);
  }

  @JsonProperty(PROP_COST)
  public void setCost(@Nullable Integer cost) {
    _cost = cost;
  }

  @JsonCreator
  private IsisInterfaceLevelSettings(
      @JsonProperty(PROP_HELLO_AUTHENTICATION_KEY) @Nullable String helloAuthenticationKey,
      @JsonProperty(PROP_HELLO_AUTHENTICATION_TYPE) @Nullable
          IsisHelloAuthenticationType helloAuthenticationType,
      @JsonProperty(PROP_HELLO_INTERVAL) @Nullable Integer helloInterval,
      @JsonProperty(PROP_HOLD_TIME) @Nullable Integer holdTime,
      @JsonProperty(PROP_MODE) @Nullable IsisInterfaceMode mode) {
    _helloAuthenticationKey = helloAuthenticationKey;
    _helloAuthenticationType = helloAuthenticationType;
    _helloInterval = helloInterval;
    _holdTime = holdTime;
    _mode = mode;
  }
}
