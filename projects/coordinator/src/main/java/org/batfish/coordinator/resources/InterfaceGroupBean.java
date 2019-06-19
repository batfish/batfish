package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Set;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.referencelibrary.InterfaceGroup;

public class InterfaceGroupBean {

  /** The set of interfaces in this interface group. */
  public Set<NodeInterfacePair> interfaces;

  /** The name of this interface group */
  public String name;

  @JsonCreator
  private InterfaceGroupBean() {}

  public InterfaceGroupBean(InterfaceGroup interfaceGroup) {
    name = interfaceGroup.getName();
    interfaces = ImmutableSet.copyOf(interfaceGroup.getInterfaces());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InterfaceGroupBean)) {
      return false;
    }
    return Objects.equals(interfaces, ((InterfaceGroupBean) o).interfaces)
        && Objects.equals(name, ((InterfaceGroupBean) o).name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(interfaces, name);
  }

  public InterfaceGroup toInterfaceGroup() {
    return new InterfaceGroup(
        ImmutableSortedSet.copyOf(firstNonNull(interfaces, ImmutableSet.of())), name);
  }
}
