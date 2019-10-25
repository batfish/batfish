package org.batfish.representation.juniper;

import java.io.Serializable;

public class IsisInterfaceSettings implements Serializable {

  private Integer _bfdLivenessDetectionMinimumInterval;
  private Integer _bfdLivenessDetectionMultiplier;
  // Enabled by default
  private boolean _enabled = true;
  private final IsisInterfaceLevelSettings _level1Settings;
  private final IsisInterfaceLevelSettings _level2Settings;
  private boolean _passive;
  private boolean _pointToPoint;

  public IsisInterfaceSettings() {
    _level1Settings = new IsisInterfaceLevelSettings();
    _level2Settings = new IsisInterfaceLevelSettings();
  }

  public Integer getBfdLivenessDetectionMinimumInterval() {
    return _bfdLivenessDetectionMinimumInterval;
  }

  public Integer getBfdLivenessDetectionMultiplier() {
    return _bfdLivenessDetectionMultiplier;
  }

  public boolean getEnabled() {
    return _enabled;
  }

  public IsisInterfaceLevelSettings getLevel1Settings() {
    return _level1Settings;
  }

  public IsisInterfaceLevelSettings getLevel2Settings() {
    return _level2Settings;
  }

  public boolean getPassive() {
    return _passive;
  }

  public boolean getPointToPoint() {
    return _pointToPoint;
  }

  public void setBfdLivenessDetectionMinimumInterval(int bfdLivenessDetectionMinimumInterval) {
    _bfdLivenessDetectionMinimumInterval = bfdLivenessDetectionMinimumInterval;
  }

  public void setBfdLivenessDetectionMultiplier(int bfdLivenessDetectionMultiplier) {
    _bfdLivenessDetectionMultiplier = bfdLivenessDetectionMultiplier;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public void setPassive(boolean passive) {
    _passive = passive;
  }

  public void setPointToPoint(boolean pointToPoint) {
    _pointToPoint = pointToPoint;
  }
}
