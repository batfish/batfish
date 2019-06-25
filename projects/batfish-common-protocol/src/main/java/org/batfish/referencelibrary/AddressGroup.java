package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Names.Type;

/** Represent an address group within a {@link ReferenceBook} */
@ParametersAreNonnullByDefault
public class AddressGroup implements Comparable<AddressGroup>, Serializable {

  private static final String PROP_ADDRESSES = "addresses";
  private static final String PROP_CHILD_GROUP_NAMES = "childGroupNames";
  private static final String PROP_NAME = "name";

  @Nonnull private SortedSet<String> _addresses;
  @Nonnull private SortedSet<String> _childGroupNames;
  @Nonnull private String _name;

  @JsonCreator
  private static AddressGroup create(
      @Nullable @JsonProperty(PROP_CHILD_GROUP_NAMES) SortedSet<String> addressGroups,
      @Nullable @JsonProperty(PROP_ADDRESSES) SortedSet<String> addresses,
      @Nullable @JsonProperty(PROP_NAME) String name) {
    checkArgument(name != null, "Address group name cannot not be null");

    return new AddressGroup(name, addresses, addressGroups);
  }

  public AddressGroup(@Nullable SortedSet<String> addresses, String name) {
    this(name, addresses, null);
  }

  public AddressGroup(
      String name,
      @Nullable SortedSet<String> addresses,
      @Nullable SortedSet<String> childGroupNames) {
    Names.checkName(name, "address group", Type.REFERENCE_OBJECT);

    _name = name;
    _addresses = firstNonNull(addresses, new TreeSet<>());
    _childGroupNames = firstNonNull(childGroupNames, new TreeSet<>());

    // check if all the input address strings can be mapped to an IpWildCard
    _addresses.forEach(IpWildcard::parse);
  }

  @Override
  public int compareTo(AddressGroup o) {
    return _name.compareTo(o._name);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AddressGroup)) {
      return false;
    }
    return Objects.equals(_name, ((AddressGroup) o)._name)
        && Objects.equals(_addresses, ((AddressGroup) o)._addresses)
        && Objects.equals(_childGroupNames, ((AddressGroup) o)._childGroupNames);
  }

  @JsonProperty(PROP_ADDRESSES)
  @Nonnull
  public SortedSet<String> getAddresses() {
    return _addresses;
  }

  @JsonProperty(PROP_CHILD_GROUP_NAMES)
  @Nonnull
  public SortedSet<String> getChildGroupNames() {
    return _childGroupNames;
  }

  @JsonProperty(PROP_NAME)
  @Nonnull
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _addresses, _childGroupNames);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add(PROP_NAME, _name)
        .add(PROP_ADDRESSES, _addresses)
        .add(PROP_CHILD_GROUP_NAMES, _childGroupNames)
        .toString();
  }
}
