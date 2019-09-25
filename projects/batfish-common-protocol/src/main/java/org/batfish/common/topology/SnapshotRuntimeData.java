package org.batfish.common.topology;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents data provided in runtime data file packaged with snapshots */
@ParametersAreNonnullByDefault
public final class SnapshotRuntimeData {

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
    public boolean equals(@Nullable Object obj) {
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

  /** Represents runtime data for a device */
  public static final class RuntimeData {
    public static final RuntimeData EMPTY_RUNTIME_DATA = new RuntimeData(ImmutableMap.of());

    private static final String PROP_INTERFACES = "interfaces";

    @Nonnull private final Map<String, InterfaceRuntimeData> _interfaces;

    @JsonCreator
    private static RuntimeData create(
        @Nullable @JsonProperty(PROP_INTERFACES) Map<String, InterfaceRuntimeData> interfaces) {
      return new RuntimeData(firstNonNull(interfaces, ImmutableMap.of()));
    }

    @VisibleForTesting
    RuntimeData(@Nonnull Map<String, InterfaceRuntimeData> interfaces) {
      _interfaces = ImmutableMap.copyOf(interfaces);
    }

    @JsonProperty(PROP_INTERFACES)
    @Nonnull
    public Map<String, InterfaceRuntimeData> getInterfaces() {
      return _interfaces;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) {
        return true;
      } else if (!(obj instanceof RuntimeData)) {
        return false;
      }
      RuntimeData o = (RuntimeData) obj;
      return _interfaces.equals(o._interfaces);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_interfaces);
    }
  }

  /** Empty SnapshotRuntimeData instance */
  public static final SnapshotRuntimeData EMPTY_SNAPSHOT_RUNTIME_DATA =
      new SnapshotRuntimeData(ImmutableMap.of());

  @JsonCreator
  private static SnapshotRuntimeData create(
      @Nullable @JsonProperty(PROP_RUNTIME_DATA) Map<String, RuntimeData> runtimeData) {
    return new SnapshotRuntimeData(firstNonNull(runtimeData, ImmutableMap.of()));
  }

  private static final String PROP_RUNTIME_DATA = "runtimeData";

  @Nonnull private final Map<String, RuntimeData> _runtimeData;

  @VisibleForTesting
  SnapshotRuntimeData(Map<String, RuntimeData> runtimeData) {
    // Canonicalize hostnames to lowercase
    _runtimeData =
        runtimeData.entrySet().stream()
            .collect(ImmutableMap.toImmutableMap(e -> e.getKey().toLowerCase(), Entry::getValue));
  }

  /** Map of hostname to {@link RuntimeData} for that device. */
  @JsonProperty(PROP_RUNTIME_DATA)
  @Nonnull
  public Map<String, RuntimeData> getRuntimeData() {
    return _runtimeData;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SnapshotRuntimeData)) {
      return false;
    }
    SnapshotRuntimeData o = (SnapshotRuntimeData) obj;
    return _runtimeData.equals(o._runtimeData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_runtimeData);
  }
}
