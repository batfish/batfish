package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;

/** An OSPFv3 routing process. */
@ParametersAreNonnullByDefault
public final class Ospfv3Process implements Serializable {

  public static final class Builder {
    private @Nonnull Map<Long, Ospfv3Area> _areas;
    private @Nullable String _processId;
    private @Nullable Ip _routerId;
    private @Nullable Vrf _vrf;

    private Builder() {
      _areas = ImmutableMap.of();
    }

    public Ospfv3Process build() {
      checkArgument(_processId != null, "Missing processId");
      checkArgument(_routerId != null, "Missing routerId");

      Ospfv3Process process =
          new Ospfv3Process(_processId, _routerId, _areas);

      if (_vrf != null) {
        _vrf.addOspfv3Process(process);
      }
      return process;
    }

    public Builder setProcessId(String processId) {
      _processId = processId;
      return this;
    }

    public Builder setRouterId(Ip routerId) {
      _routerId = routerId;
      return this;
    }

    public Builder setAreas(Map<Long, Ospfv3Area> areas) {
      _areas = areas;
      return this;
    }

    public Builder setVrf(Vrf vrf) {
      _vrf = vrf;
      return this;
    }
  }

  private static final String PROP_AREAS = "areas";
  private static final String PROP_PROCESS_ID = "processId";
  private static final String PROP_ROUTER_ID = "routerId";

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static Ospfv3Process create(
      @JsonProperty(PROP_PROCESS_ID) @Nullable String processId,
      @JsonProperty(PROP_ROUTER_ID) @Nullable Ip routerId,
      @JsonProperty(PROP_AREAS) @Nullable Map<Long, Ospfv3Area> areas) {
    checkArgument(processId != null, "Missing %s", PROP_PROCESS_ID);
    checkArgument(routerId != null, "Missing %s", PROP_ROUTER_ID);

    return new Ospfv3Process(
        processId,
        routerId,
        firstNonNull(areas, ImmutableMap.of()));
  }

  private Ospfv3Process(
      String processId, Ip routerId, Map<Long, Ospfv3Area> areas) {
    _processId = processId;
    _routerId = routerId;
    _areas = ImmutableSortedMap.copyOf(areas);
  }

  @JsonProperty(PROP_PROCESS_ID)
  public @Nonnull String getProcessId() {
    return _processId;
  }

  @JsonProperty(PROP_ROUTER_ID)
  public @Nonnull Ip getRouterId() {
    return _routerId;
  }

  @JsonProperty(PROP_AREAS)
  public @Nonnull SortedMap<Long, Ospfv3Area> getAreas() {
    return _areas;
  }

  private final @Nonnull String _processId;
  private final @Nonnull Ip _routerId;
  private final @Nonnull SortedMap<Long, Ospfv3Area> _areas;
}
