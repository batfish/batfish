package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Configuration of an OSPF area {@code network virtual-router NAME protocol ospf area AREA_ID}. */
public class OspfArea implements Serializable {

  public OspfArea(Ip areaId) {
    _areaId = areaId;
    _interfaces = new HashMap<>();
  }

  public @Nonnull OspfInterface getOrCreateOspfInterface(String ifaceName) {
    return _interfaces.computeIfAbsent(ifaceName, OspfInterface::new);
  }

  public @Nonnull Map<String, OspfInterface> getInterfaces() {
    return _interfaces;
  }

  public @Nullable OspfAreaTypeSettings getTypeSettings() {
    return _typeSettings;
  }

  public void setTypeSettings(@Nullable OspfAreaTypeSettings typeSettings) {
    _typeSettings = typeSettings;
  }

  public @Nonnull Ip getAreaId() {
    return _areaId;
  }

  public void setAreaId(Ip areaId) {
    _areaId = areaId;
  }

  private @Nonnull Ip _areaId;
  private @Nonnull Map<String, OspfInterface> _interfaces;
  private @Nullable OspfAreaTypeSettings _typeSettings;
}
