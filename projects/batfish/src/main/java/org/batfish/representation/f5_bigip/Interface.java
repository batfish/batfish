package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.checkerframework.checker.nullness.qual.Nullable;

@ParametersAreNonnullByDefault
public final class Interface implements Serializable {

  public static final Double DEFAULT_BANDWIDTH = 1E12D;

  private static final long serialVersionUID = 1L;

  private @Nullable Double _bandwidth;

  private final @Nonnull String _name;

  private @Nullable Double _speed;

  public Interface(String name) {
    _name = name;
  }

  public @Nullable Double getBandwidth() {
    return _bandwidth;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Double getSpeed() {
    return _speed;
  }

  public void setBandwidth(@Nullable Double bandwidth) {
    _bandwidth = bandwidth;
  }

  public void setSpeed(@Nullable Double speed) {
    _speed = speed;
  }
}
