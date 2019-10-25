package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.isis.IsisHelloAuthenticationType;

public class IsisInterfaceLevelSettings implements Serializable {

  // Enabled by default
  private boolean _enabled = true;
  @Nullable private String _helloAuthenticationKey;
  @Nullable private IsisHelloAuthenticationType _helloAuthenticationType;
  @Nullable private Integer _helloInterval;
  @Nullable private Integer _holdTime;
  @Nullable private Long _metric;
  private boolean _passive;
  @Nullable private Long _teMetric;

  public boolean getEnabled() {
    return _enabled;
  }

  @Nullable
  public String getHelloAuthenticationKey() {
    return _helloAuthenticationKey;
  }

  @Nullable
  public IsisHelloAuthenticationType getHelloAuthenticationType() {
    return _helloAuthenticationType;
  }

  @Nullable
  public Integer getHelloInterval() {
    return _helloInterval;
  }

  @Nullable
  public Integer getHoldTime() {
    return _holdTime;
  }

  @Nullable
  public Long getMetric() {
    return _metric;
  }

  public boolean getPassive() {
    return _passive;
  }

  @Nullable
  public Long getTeMetric() {
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
