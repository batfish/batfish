package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class Vlan implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull Map<String, VlanInterface> _interfaces;

  private final @Nonnull String _name;

  private @Nullable Integer _tag;

  public Vlan(String name) {
    _name = name;
    _interfaces = new HashMap<>();
  }

  public Map<String, VlanInterface> getInterfaces() {
    return _interfaces;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Integer getTag() {
    return _tag;
  }

  public void setTag(@Nullable Integer tag) {
    _tag = tag;
  }
}
