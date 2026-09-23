package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Route-flap damping parameters referenced by routing policy actions. */
@ParametersAreNonnullByDefault
public final class DampingProfile implements Serializable {

  public boolean getDisabled() {
    return _disabled;
  }

  public void setDisabled(boolean disabled) {
    _disabled = disabled;
  }

  public @Nullable Integer getHalfLife() {
    return _halfLife;
  }

  public void setHalfLife(int halfLife) {
    _halfLife = halfLife;
  }

  public @Nullable Integer getMaxSuppress() {
    return _maxSuppress;
  }

  public void setMaxSuppress(int maxSuppress) {
    _maxSuppress = maxSuppress;
  }

  public @Nullable Integer getReuse() {
    return _reuse;
  }

  public void setReuse(int reuse) {
    _reuse = reuse;
  }

  public @Nullable Integer getSuppress() {
    return _suppress;
  }

  public void setSuppress(int suppress) {
    _suppress = suppress;
  }

  private boolean _disabled;
  private @Nullable Integer _halfLife;
  private @Nullable Integer _maxSuppress;
  private @Nullable Integer _reuse;
  private @Nullable Integer _suppress;
}
