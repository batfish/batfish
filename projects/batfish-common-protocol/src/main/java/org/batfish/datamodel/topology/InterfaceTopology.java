package org.batfish.datamodel.topology;

import static org.batfish.datamodel.topology.Layer3NonBridgedSettings.noEncapsulation;

import java.io.Serializable;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data model for the bridging topology of an {@link org.batfish.datamodel.Interface} with one or
 * more of a logical layer-1, layer-2, or layer-3 aspect.
 */
public final class InterfaceTopology implements Serializable {

  public static final class Builder {
    public @Nonnull Builder setLogicalLayer1(boolean logicalLayer1) {
      _logicalLayer1 = logicalLayer1;
      return this;
    }

    public @Nonnull Builder setLayer2Settings(@Nullable Layer2Settings layer2Settings) {
      _layer2Settings = layer2Settings;
      return this;
    }

    public @Nonnull Builder setLayer3Settings(@Nullable Layer3Settings layer3Settings) {
      _layer3Settings = layer3Settings;
      return this;
    }

    public @Nonnull InterfaceTopology build() {
      return new InterfaceTopology(_logicalLayer1, _layer2Settings, _layer3Settings);
    }

    private Builder() {}

    private boolean _logicalLayer1;
    private @Nullable Layer2Settings _layer2Settings;
    private @Nullable Layer3Settings _layer3Settings;
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public static @Nonnull InterfaceTopology l13NoEncapsulation(String interfaceName) {
    return builder()
        .setLogicalLayer1(true)
        .setLayer3Settings(noEncapsulation(interfaceName))
        .build();
  }

  public boolean isLogicalLayer1() {
    return _logicalLayer1;
  }

  public @Nonnull Optional<Layer2Settings> getLayer2Settings() {
    return Optional.ofNullable(_layer2Settings);
  }

  public @Nonnull Optional<Layer3Settings> getLayer3Settings() {
    return Optional.ofNullable(_layer3Settings);
  }

  private InterfaceTopology(
      boolean logicalLayer1,
      @Nullable Layer2Settings layer2Settings,
      @Nullable Layer3Settings layer3Settings) {
    _logicalLayer1 = logicalLayer1;
    _layer2Settings = layer2Settings;
    _layer3Settings = layer3Settings;
  }

  private final boolean _logicalLayer1;
  private final @Nullable Layer2Settings _layer2Settings;
  private final @Nullable Layer3Settings _layer3Settings;
}
