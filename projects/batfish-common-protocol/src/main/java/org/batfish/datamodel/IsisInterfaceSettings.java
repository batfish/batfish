package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;

public class IsisInterfaceSettings implements Serializable {

  private static final String PROP_BFD_LIVENESS_DETECTION_MINIMUM_INTERVAL =
      "bfdLivenessDetectionMinimumInterval";

  private static final String PROP_BFD_LIVENESS_DETECTION_MULTIPLIER =
      "bfdLivenessDetectionMultiplier";

  private static final String PROP_ISO_ADDRESS = "isoAddress";

  private static final String PROP_LEVEL1 = "level1";

  private static final String PROP_LEVEL2 = "level2";

  private static final String PROP_POINT_TO_POINT = "pointToPoint";

  private static final long serialVersionUID = 1L;

  private Integer _bfdLivenessDetectionMinimumInterval;

  private Integer _bfdLivenessDetectionMultiplier;

  private IsoAddress _isoAddress;

  private IsisInterfaceLevelSettings _level1;

  private IsisInterfaceLevelSettings _level2;

  private boolean _pointToPoint;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IsisInterfaceSettings)) {
      return false;
    }
    IsisInterfaceSettings rhs = (IsisInterfaceSettings) obj;
    return Objects.equals(
            _bfdLivenessDetectionMinimumInterval, rhs._bfdLivenessDetectionMinimumInterval)
        && Objects.equals(_bfdLivenessDetectionMultiplier, rhs._bfdLivenessDetectionMultiplier)
        && Objects.equals(_isoAddress, rhs._isoAddress)
        && Objects.equals(_level1, rhs._level1)
        && Objects.equals(_level2, rhs._level2)
        && _pointToPoint == rhs._pointToPoint;
  }

  @JsonProperty(PROP_BFD_LIVENESS_DETECTION_MINIMUM_INTERVAL)
  public @Nullable Integer getBfdLivenessDetectionMinimumInterval() {
    return _bfdLivenessDetectionMinimumInterval;
  }

  @JsonProperty(PROP_BFD_LIVENESS_DETECTION_MULTIPLIER)
  public @Nullable Integer getBfdLivenessDetectionMultiplier() {
    return _bfdLivenessDetectionMultiplier;
  }

  @JsonProperty(PROP_ISO_ADDRESS)
  public @Nullable IsoAddress getIsoAddress() {
    return _isoAddress;
  }

  @JsonProperty(PROP_LEVEL1)
  public @Nullable IsisInterfaceLevelSettings getLevel1() {
    return _level1;
  }

  @JsonProperty(PROP_LEVEL2)
  public @Nullable IsisInterfaceLevelSettings getLevel2() {
    return _level2;
  }

  @JsonProperty(PROP_POINT_TO_POINT)
  public boolean getPointToPoint() {
    return _pointToPoint;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _bfdLivenessDetectionMinimumInterval,
        _bfdLivenessDetectionMultiplier,
        _isoAddress,
        _level1,
        _level2,
        _pointToPoint);
  }

  @JsonProperty(PROP_BFD_LIVENESS_DETECTION_MINIMUM_INTERVAL)
  public void setBfdLivenessDetectionMinimumInterval(
      @Nullable Integer bfdLivenessDetectionMinimumInterval) {
    _bfdLivenessDetectionMinimumInterval = bfdLivenessDetectionMinimumInterval;
  }

  @JsonProperty(PROP_BFD_LIVENESS_DETECTION_MULTIPLIER)
  public void setBfdLivenessDetectionMultiplier(@Nullable Integer bfdLivenessDetectionMultiplier) {
    _bfdLivenessDetectionMultiplier = bfdLivenessDetectionMultiplier;
  }

  @JsonProperty(PROP_ISO_ADDRESS)
  public void setIsoAddress(@Nullable IsoAddress isoAddress) {
    _isoAddress = isoAddress;
  }

  @JsonProperty(PROP_LEVEL1)
  public void setLevel1(@Nullable IsisInterfaceLevelSettings level1) {
    _level1 = level1;
  }

  @JsonProperty(PROP_LEVEL2)
  public void setLevel2(@Nullable IsisInterfaceLevelSettings level2) {
    _level2 = level2;
  }

  @JsonProperty(PROP_POINT_TO_POINT)
  public void setPointToPoint(boolean pointToPoint) {
    _pointToPoint = pointToPoint;
  }
}
