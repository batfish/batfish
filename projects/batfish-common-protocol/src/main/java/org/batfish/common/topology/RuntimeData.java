package org.batfish.common.topology;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents data provided in runtime data file packaged with snapshots */
@ParametersAreNonnullByDefault
public final class RuntimeData {

  /** Represents runtime data for an interface */
  public static final class InterfaceRuntimeData {
    private static final String PROP_BANDWIDTH = "bandwidth";
    private static final String PROP_SPEED = "speed";

    @Nullable private final Double _bandwidth;
    @Nullable private final Double _speed;

    @JsonCreator
    @VisibleForTesting
    InterfaceRuntimeData(
        @Nullable @JsonProperty(PROP_BANDWIDTH) Double bandwidth,
        @Nullable @JsonProperty(PROP_SPEED) Double speed) {
      _bandwidth = bandwidth;
      _speed = speed;
    }

    @JsonProperty(PROP_BANDWIDTH)
    @Nullable
    public Double getBandwidth() {
      return _bandwidth;
    }

    @JsonProperty(PROP_SPEED)
    @Nullable
    public Double getSpeed() {
      return _speed;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (!(obj instanceof InterfaceRuntimeData)) {
        return false;
      }
      InterfaceRuntimeData o = (InterfaceRuntimeData) obj;
      return Objects.equals(_bandwidth, o._bandwidth) && Objects.equals(_speed, o._speed);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_bandwidth, _speed);
    }
  }

  /** Empty RuntimeData instance */
  public static final RuntimeData EMPTY_RUNTIME_DATA = new RuntimeData(ImmutableMap.of());

  @JsonCreator
  private static RuntimeData create(
      @Nullable @JsonProperty(PROP_INTERFACE_RUNTIME_DATA)
          Map<String, Map<String, InterfaceRuntimeData>> interfaceRuntimeData) {
    return new RuntimeData(firstNonNull(interfaceRuntimeData, ImmutableMap.of()));
  }

  private static final String PROP_INTERFACE_RUNTIME_DATA = "interfaceRuntimeData";

  @Nonnull private final Map<String, Map<String, InterfaceRuntimeData>> _interfaceRuntimeData;

  @VisibleForTesting
  RuntimeData(Map<String, Map<String, InterfaceRuntimeData>> interfaceRuntimeData) {
    // Canonicalize hostnames to lowercase and make everything immutable
    _interfaceRuntimeData =
        interfaceRuntimeData.entrySet().stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    e -> e.getKey().toLowerCase(), e -> ImmutableMap.copyOf(e.getValue())));
  }

  /** Map of hostname to interface name to {@link InterfaceRuntimeData}. */
  @JsonProperty(PROP_INTERFACE_RUNTIME_DATA)
  @Nonnull
  public Map<String, Map<String, InterfaceRuntimeData>> getInterfaceRuntimeData() {
    return _interfaceRuntimeData;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof RuntimeData)) {
      return false;
    }
    RuntimeData o = (RuntimeData) obj;
    return _interfaceRuntimeData.equals(o._interfaceRuntimeData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_interfaceRuntimeData);
  }
}
