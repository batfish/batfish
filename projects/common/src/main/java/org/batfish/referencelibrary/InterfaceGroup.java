package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Names.Type;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Represents a group of interfaces in {@link ReferenceBook} */
@ParametersAreNonnullByDefault
public class InterfaceGroup implements Comparable<InterfaceGroup>, Serializable {

  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_NAME = "name";

  private final @Nonnull SortedSet<NodeInterfacePair> _interfaces;
  private final @Nonnull String _name;

  @JsonCreator
  private static InterfaceGroup jsonCreator(
      @JsonProperty(PROP_INTERFACES) SortedSet<NodeInterfacePair> interfaces,
      @JsonProperty(PROP_NAME) String name) {
    checkArgument(name != null, "Interface group name cannot not be null");
    return new InterfaceGroup(firstNonNull(interfaces, ImmutableSortedSet.of()), name);
  }

  public InterfaceGroup(SortedSet<NodeInterfacePair> interfaces, String name) {
    Names.checkName(name, "interface group", Type.REFERENCE_OBJECT);

    _name = name;
    _interfaces = ImmutableSortedSet.copyOf(interfaces);
  }

  @Override
  public int compareTo(InterfaceGroup o) {
    return _name.compareTo(o._name);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InterfaceGroup)) {
      return false;
    }
    return Objects.equals(_name, ((InterfaceGroup) o)._name)
        && Objects.equals(_interfaces, ((InterfaceGroup) o)._interfaces);
  }

  @JsonProperty(PROP_INTERFACES)
  public @Nonnull SortedSet<NodeInterfacePair> getInterfaces() {
    return _interfaces;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _interfaces);
  }
}
