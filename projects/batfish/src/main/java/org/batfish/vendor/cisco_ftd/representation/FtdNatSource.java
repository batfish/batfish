package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;

public class FtdNatSource implements Serializable {
  public enum Type {
    STATIC,
    DYNAMIC
  }

  private final @Nonnull Type _type;
  private final @Nonnull FtdNatAddress _real;
  private final @Nonnull FtdNatAddress _mapped;

  public FtdNatSource(
      @Nonnull Type type, @Nonnull FtdNatAddress real, @Nonnull FtdNatAddress mapped) {
    _type = type;
    _real = real;
    _mapped = mapped;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  public @Nonnull FtdNatAddress getReal() {
    return _real;
  }

  public @Nonnull FtdNatAddress getMapped() {
    return _mapped;
  }

  @Override
  public String toString() {
    return String.format("FtdNatSource[%s: %s -> %s]", _type, _real, _mapped);
  }
}
