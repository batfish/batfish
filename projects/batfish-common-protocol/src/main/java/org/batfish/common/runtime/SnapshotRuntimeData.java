package org.batfish.common.runtime;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Represents data provided in runtime data file packaged with snapshots */
@ParametersAreNonnullByDefault
public final class SnapshotRuntimeData {
  public static final class Builder {
    private @Nonnull Map<String, RuntimeData> _runtimeData;

    public SnapshotRuntimeData build() {
      return new SnapshotRuntimeData(_runtimeData);
    }

    @VisibleForTesting
    @SuppressWarnings("PMD.UnnecessaryCaseChange") // that's what we're checking
    public Builder setRuntimeData(@Nonnull Map<String, RuntimeData> runtimeData) {
      // For internal/test use only; shouldn't receive non-canonical hostnames
      checkArgument(
          runtimeData.keySet().stream().allMatch(h -> h.equals(h.toLowerCase())),
          "Non-canonical hostname in provided runtime data");
      _runtimeData = new HashMap<>(runtimeData);
      return this;
    }

    /** Sets interfaces' {@link InterfaceRuntimeData#getLineUp() lineUp} to {@code true} */
    public Builder setInterfacesLineUp(@Nonnull Collection<NodeInterfacePair> interfaces) {
      return setInterfacesLineUp(interfaces, true);
    }

    /** Sets interfaces' {@link InterfaceRuntimeData#getLineUp() lineUp} to {@code false} */
    public Builder setInterfacesLineDown(@Nonnull Collection<NodeInterfacePair> interfaces) {
      return setInterfacesLineUp(interfaces, false);
    }

    /** Sets interfaces' {@link InterfaceRuntimeData#getLineUp() lineUp} to {@code true} */
    public Builder setInterfacesLineUp(@Nonnull NodeInterfacePair... interfaces) {
      return setInterfacesLineUp(ImmutableList.copyOf(interfaces));
    }

    /** Sets interfaces' {@link InterfaceRuntimeData#getLineUp() lineUp} to {@code false} */
    public Builder setInterfacesLineDown(@Nonnull NodeInterfacePair... interfaces) {
      return setInterfacesLineDown(ImmutableList.copyOf(interfaces));
    }

    /**
     * Sets interfaces' {@link InterfaceRuntimeData#getLineUp() lineUp}
     *
     * @param interfaces Collection of {@link NodeInterfacePair} representing all interfaces whose
     *     lineUp status to set
     * @param lineUp Given interfaces' lineUp status will be set to this value
     */
    @VisibleForTesting
    Builder setInterfacesLineUp(@Nonnull Iterable<NodeInterfacePair> interfaces, boolean lineUp) {
      // Compile provided interfaces to a mapping of hostname -> interfaces
      Map<String, Set<String>> ifaceMap = new HashMap<>();
      interfaces.forEach(
          iface ->
              ifaceMap
                  // Canonicalize hostnames (should be unnecessary because NodeInterfacePair always
                  // lowercases hostnames, but just in case)
                  .computeIfAbsent(iface.getHostname().toLowerCase(), i -> new HashSet<>())
                  .add(iface.getInterface()));

      // For each affected hostname, update its runtime data with the new interface data
      for (Entry<String, Set<String>> e : ifaceMap.entrySet()) {
        String hostname = e.getKey();
        RuntimeData.Builder runtimeDataBuilder =
            Optional.ofNullable(_runtimeData.get(hostname))
                .map(RuntimeData::toBuilder)
                .orElse(RuntimeData.builder());
        e.getValue().forEach(iface -> runtimeDataBuilder.setInterfaceLineUp(iface, lineUp));
        _runtimeData.put(hostname, runtimeDataBuilder.build());
      }
      return this;
    }

    private Builder() {
      _runtimeData = new HashMap<>();
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Empty SnapshotRuntimeData instance */
  public static final SnapshotRuntimeData EMPTY_SNAPSHOT_RUNTIME_DATA =
      new SnapshotRuntimeData(ImmutableMap.of());

  @JsonCreator
  private static SnapshotRuntimeData create(
      @JsonProperty(PROP_RUNTIME_DATA) @Nullable Map<String, RuntimeData> runtimeData) {
    return new SnapshotRuntimeData(firstNonNull(runtimeData, ImmutableMap.of()));
  }

  private static final String PROP_RUNTIME_DATA = "runtimeData";

  private final @Nonnull Map<String, RuntimeData> _runtimeData;

  @VisibleForTesting
  SnapshotRuntimeData(Map<String, RuntimeData> runtimeData) {
    // Canonicalize hostnames to lowercase
    _runtimeData =
        runtimeData.entrySet().stream()
            .collect(ImmutableMap.toImmutableMap(e -> e.getKey().toLowerCase(), Entry::getValue));
  }

  /** Map of hostname to {@link RuntimeData} for that device. */
  @JsonProperty(PROP_RUNTIME_DATA)
  @VisibleForTesting
  @Nonnull
  Map<String, RuntimeData> getRuntimeData() {
    return _runtimeData;
  }

  @JsonIgnore
  @SuppressWarnings("PMD.UnnecessaryCaseChange") // that's what we're asserting
  public @Nonnull RuntimeData getRuntimeData(String hostname) {
    assert hostname.equals(hostname.toLowerCase());
    return _runtimeData.getOrDefault(hostname, RuntimeData.EMPTY_RUNTIME_DATA);
  }

  /**
   * Returns set of {@link NodeInterfacePair}s representing all interfaces with {@link
   * InterfaceRuntimeData#getLineUp() lineUp} set to {@code false}.
   */
  @JsonIgnore
  public @Nonnull Set<NodeInterfacePair> getBlacklistedInterfaces() {
    return _runtimeData.entrySet().stream()
        .flatMap(
            nodeEntry ->
                nodeEntry.getValue().getInterfaces().entrySet().stream()
                    .filter(ifaceEntry -> Objects.equals(ifaceEntry.getValue().getLineUp(), false))
                    .map(
                        ifaceEntry ->
                            NodeInterfacePair.of(nodeEntry.getKey(), ifaceEntry.getKey())))
        .collect(ImmutableSet.toImmutableSet());
  }

  public Builder toBuilder() {
    return builder().setRuntimeData(_runtimeData);
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
    return Objects.hashCode(_runtimeData);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add(PROP_RUNTIME_DATA, _runtimeData).toString();
  }
}
