package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.datamodel.IsisHelloAuthenticationType;

public class IsisInterfaceLevelSettings implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _enabled;

  private String _helloAuthenticationKey;

  private IsisHelloAuthenticationType _helloAuthenticationType;

  private Integer _helloInterval;

  private Integer _holdTime;

  private Long _metric;

  private Long _teMetric;

  public boolean getEnabled() {
    return _enabled;
  }

  public String getHelloAuthenticationKey() {
    return _helloAuthenticationKey;
  }

  public IsisHelloAuthenticationType getHelloAuthenticationType() {
    return _helloAuthenticationType;
  }

  public Integer getHelloInterval() {
    return _helloInterval;
  }

  public Integer getHoldTime() {
    return _holdTime;
  }

  public Long getMetric() {
    return _metric;
  }

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

  public void setTeMetric(long teMetric) {
    _teMetric = teMetric;
  }
}
