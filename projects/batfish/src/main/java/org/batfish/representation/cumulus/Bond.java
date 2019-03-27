package org.batfish.representation.cumulus;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A logical layer-1 bond interface */
public class Bond implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull InterfaceBridgeSettings _bridge;
  private @Nullable Integer _clagId;
  private final @Nonnull String _name;
  private @Nonnull Set<String> _slaves;

  public Bond(String name) {
    _name = name;
    _bridge = new InterfaceBridgeSettings();
    _slaves = ImmutableSet.of();
  }

  public @Nonnull InterfaceBridgeSettings getBridge() {
    return _bridge;
  }

  public @Nullable Integer getClagId() {
    return _clagId;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void setClagId(@Nullable Integer clagId) {
    _clagId = clagId;
  }

  public void setSlaves(Set<String> slaves) {
    _slaves = ImmutableSet.copyOf(slaves);
  }
}
