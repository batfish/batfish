package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * An SR-OS OSPF area ({@code router "<name>" ospf <inst> area <area-id>}), keyed by its area-id
 * (configured as a dotted-quad, e.g. {@code 0.0.0.0}). Holds the OSPF interfaces enabled in the
 * area, keyed by router-interface name.
 */
public final class OspfArea implements Serializable {

  public OspfArea(String areaId) {
    _areaId = areaId;
    _interfaces = new LinkedHashMap<>();
  }

  public @Nonnull String getAreaId() {
    return _areaId;
  }

  /** OSPF interfaces in this area, keyed by router-interface name. */
  public @Nonnull Map<String, OspfAreaInterface> getInterfaces() {
    return _interfaces;
  }

  private final @Nonnull String _areaId;
  private final @Nonnull Map<String, OspfAreaInterface> _interfaces;
}
