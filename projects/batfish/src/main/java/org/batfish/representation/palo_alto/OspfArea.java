package org.batfish.representation.palo_alto;

import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

public class OspfArea {
  @Nullable
  public OspfAreaTypeSettings getTypeSettings() {
    return _typeSettings;
  }

  public void setTypeSettings(@Nullable OspfAreaTypeSettings typeSettings) {
    _typeSettings = typeSettings;
  }

  @Nullable
  public Ip getAreaId() {
    return _areaId;
  }

  public void setAreaId(@Nullable Ip areaId) {
    _areaId = areaId;
  }

  private @Nullable Ip _areaId;
  private @Nullable OspfAreaTypeSettings _typeSettings;
}
