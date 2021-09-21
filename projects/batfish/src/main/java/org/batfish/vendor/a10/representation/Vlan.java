package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Datamodel class representing a configured A10 vlan. */
public final class Vlan implements Serializable {

  @Nullable
  public String getName() {
    return _name;
  }

  public int getNumber() {
    return _number;
  }

  @Nullable
  public Integer getRouterInterface() {
    return _routerInterface;
  }

  @Nonnull
  public Set<InterfaceReference> getTagged() {
    return _tagged;
  }

  @Nonnull
  public Set<InterfaceReference> getUntagged() {
    return _untagged;
  }

  public void setName(String name) {
    _name = name;
  }

  public void setRouterInterface(int routerInterfaceNumber) {
    _routerInterface = routerInterfaceNumber;
  }

  public void addTagged(Collection<InterfaceReference> tagged) {
    _tagged.addAll(tagged);
  }

  public void addUntagged(Collection<InterfaceReference> untagged) {
    _untagged.addAll(untagged);
  }

  public Vlan(int number) {
    _number = number;
    _tagged = new HashSet<>();
    _untagged = new HashSet<>();
  }

  @Nonnull private String _name;
  private final int _number;
  @Nullable private Integer _routerInterface;
  @Nonnull private Set<InterfaceReference> _tagged;
  @Nonnull private Set<InterfaceReference> _untagged;
}
