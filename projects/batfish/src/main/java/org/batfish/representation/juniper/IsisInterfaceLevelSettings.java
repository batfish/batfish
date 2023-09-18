package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.isis.IsisHelloAuthenticationType;

public class IsisInterfaceLevelSettings implements Serializable {

  // Enabled by default
  private boolean _enabled = true;
  private @Nullable String _helloAuthenticationKey;
  private @Nullable IsisHelloAuthenticationType _helloAuthenticationType;
  private @Nullable Integer _helloInterval;
  private @Nullable Integer _holdTime;
  private @Nullable Long _metric;
  private boolean _passive;
  private @Nullable Long _teMetric;

  public boolean getEnabled() {
    return _enabled;
  }

  public @Nullable String getHelloAuthenticationKey() {
    return _helloAuthenticationKey;
  }

  public @Nullable IsisHelloAuthenticationType getHelloAuthenticationType() {
    return _helloAuthenticationType;
  }

  public @Nullable Integer getHelloInterval() {
    return _helloInterval;
  }

  public @Nullable Integer getHoldTime() {
    return _holdTime;
  }

  public @Nullable Long getMetric() {
    return _metric;
  }

  public boolean getPassive() {
    return _passive;
  }

  public @Nullable Long getTeMetric() {
    return _teMetric;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public void setHelloAuthenticationKey(String helloAuthenticationKey) {
    _helloAuthenticationKey = helloAuthenticationKey;
  }

  public void setHelloAuthenticationType(IsisHelloAuthenticationType helloAuthenticationType) {
    _helloAuthenticationType = helloAuthenticationType;
  }

  public void setHelloInterval(int helloInterval) {
    _helloInterval = helloInterval;
  }

  public void setHoldTime(int holdTime) {
    _holdTime = holdTime;
  }

  public void setMetric(long metric) {
    _metric = metric;
  }

  public void setPassive(boolean passive) {
    _passive = passive;
  }

  public void setTeMetric(long teMetric) {
    _teMetric = teMetric;
  }
}
