package org.batfish.common.runtime;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents runtime data for a device.
 *
 * <p>Note: Legacy runtime data files may have interface names as direct properties instead of under
 * "interfaces". These are ignored to allow parsing to succeed. The proper fix is to regenerate the
 * runtime data with the correct format.
 */
@ParametersAreNonnullByDefault
@JsonIgnoreProperties(ignoreUnknown = true)
public final class RuntimeData {
  public static final class Builder {
    private @Nonnull Map<String, InterfaceRuntimeData> _interfaces;

    public RuntimeData build() {
      return new RuntimeData(_interfaces);
    }

    public Builder setInterfaces(@Nonnull Map<String, InterfaceRuntimeData> interfaces) {
      _interfaces = new HashMap<>(interfaces);
      return this;
    }

    /** Convenience method to set an interface's {@link InterfaceRuntimeData#getLineUp() lineUp} */
    public Builder setInterfaceLineUp(@Nonnull String ifaceName, boolean lineUp) {
      InterfaceRuntimeData ifaceData =
          _interfaces.getOrDefault(ifaceName, InterfaceRuntimeData.EMPTY_INTERFACE_RUNTIME_DATA);
      _interfaces.put(ifaceName, ifaceData.toBuilder().setLineUp(lineUp).build());
      return this;
    }

    private Builder() {
      _interfaces = new HashMap<>();
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final RuntimeData EMPTY_RUNTIME_DATA = builder().build();

  private static final String PROP_INTERFACES = "interfaces";

  private final @Nonnull Map<String, InterfaceRuntimeData> _interfaces;

  @JsonCreator
  private static RuntimeData create(
      @JsonProperty(PROP_INTERFACES) @Nullable Map<String, InterfaceRuntimeData> interfaces) {
    return new RuntimeData(firstNonNull(interfaces, ImmutableMap.of()));
  }

  @VisibleForTesting
  private RuntimeData(@Nonnull Map<String, InterfaceRuntimeData> interfaces) {
    _interfaces = ImmutableMap.copyOf(interfaces);
  }

  @JsonProperty(PROP_INTERFACES)
  @VisibleForTesting
  @Nonnull
  Map<String, InterfaceRuntimeData> getInterfaces() {
    return _interfaces;
  }

  @JsonIgnore
  public @Nullable InterfaceRuntimeData getInterface(String ifaceName) {
    return _interfaces.get(ifaceName);
  }

  Builder toBuilder() {
    return builder().setInterfaces(_interfaces);
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
    return Objects.hashCode(_interfaces);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add(PROP_INTERFACES, _interfaces).toString();
  }
}
