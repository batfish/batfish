package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Represents a group of interfaces in {@link ReferenceBook} */
@ParametersAreNonnullByDefault
public class InterfaceGroup implements Comparable<InterfaceGroup> {

  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_NAME = "name";

  @Nonnull private final SortedSet<NodeInterfacePair> _interfaces;
  @Nonnull private final String _name;

  @JsonCreator
  private static InterfaceGroup jsonCreator(
      @JsonProperty(PROP_INTERFACES) SortedSet<NodeInterfacePair> interfaces,
      @JsonProperty(PROP_NAME) String name) {
    checkArgument(name != null, "Interface group name cannot not be null");
    return new InterfaceGroup(firstNonNull(interfaces, ImmutableSortedSet.of()), name);
  }

  public InterfaceGroup(SortedSet<NodeInterfacePair> interfaces, String name) {
    ReferenceLibrary.checkValidName(name, "interface group");

    _name = name;
    _interfaces = ImmutableSortedSet.copyOf(interfaces);
  }

  @Override
  public int compareTo(InterfaceGroup o) {
    return _name.compareTo(o._name);
  }

  @JsonProperty(PROP_INTERFACES)
  @Nonnull
  public SortedSet<NodeInterfacePair> getInterfaces() {
    return _interfaces;
  }

  @JsonProperty(PROP_NAME)
  @Nonnull
  public String getName() {
    return _name;
  }
}
