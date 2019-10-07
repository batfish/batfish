package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A Layer1-2 physical interface */
@ParametersAreNonnullByDefault
public final class Interface implements Serializable {

  public static final Double DEFAULT_BANDWIDTH = 1E12D;

  private @Nullable Double _bandwidth;
  private @Nullable String _description;
  private @Nullable Boolean _disabled;
  private final @Nonnull String _name;
  private @Nullable Double _speed;

  public Interface(String name) {
    _name = name;
  }

  public @Nullable Double getBandwidth() {
    return _bandwidth;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nullable Boolean getDisabled() {
    return _disabled;
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

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public void setDisabled(@Nullable Boolean disabled) {
    _disabled = disabled;
  }

  public void setSpeed(@Nullable Double speed) {
    _speed = speed;
  }
}
