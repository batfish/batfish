package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a Juniper firewall policer. */
public final class Policer implements Serializable {

  private final @Nonnull String _name;
  private @Nullable Boolean _filterSpecific;
  private @Nullable PolicerIfExceeding _ifExceeding;
  private @Nullable Boolean _logicalInterfacePolicer;
  private @Nullable PolicerThen _then;

  public Policer(@Nonnull String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Boolean getFilterSpecific() {
    return _filterSpecific;
  }

  public void setFilterSpecific(boolean filterSpecific) {
    _filterSpecific = filterSpecific;
  }

  public @Nullable PolicerIfExceeding getIfExceeding() {
    return _ifExceeding;
  }

  public void setIfExceeding(@Nullable PolicerIfExceeding ifExceeding) {
    _ifExceeding = ifExceeding;
  }

  public @Nullable Boolean getLogicalInterfacePolicer() {
    return _logicalInterfacePolicer;
  }

  public void setLogicalInterfacePolicer(boolean logicalInterfacePolicer) {
    _logicalInterfacePolicer = logicalInterfacePolicer;
  }

  public @Nullable PolicerThen getThen() {
    return _then;
  }

  public void setThen(@Nullable PolicerThen then) {
    _then = then;
  }
}
