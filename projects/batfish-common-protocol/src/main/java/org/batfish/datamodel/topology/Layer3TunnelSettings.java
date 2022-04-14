package org.batfish.datamodel.topology;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Layer3TunnelSettings implements Layer3Settings {

  public static @Nonnull Layer3TunnelSettings instance() {
    return INSTANCE;
  }

  @Override
  public <T> T accept(Layer3SettingsVisitor<T> visitor) {
    return visitor.visitLayer3TunnelSettings(this);
  }

  @Override
  public <T, U> T accept(Layer3SettingsArgVisitor<T, U> visitor, U arg) {
    return visitor.visitLayer3TunnelSettings(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof Layer3TunnelSettings;
  }

  @Override
  public int hashCode() {
    return 0x3956BD62; // randomly generated
  }

  private static final Layer3TunnelSettings INSTANCE = new Layer3TunnelSettings();

  private Layer3TunnelSettings() {}
}
