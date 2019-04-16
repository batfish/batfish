package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AddressGroup implements Comparable<AddressGroup> {

  private static final String PROP_ADDRESS_GROUPS = "addressGroups";
  private static final String PROP_ADDRESSES = "addresses";
  private static final String PROP_NAME = "name";

  @Nonnull private SortedSet<String> _addresses;
  @Nonnull private SortedSet<String> _addressGroups;
  @Nonnull private String _name;

  @JsonCreator
  private static AddressGroup create(
      @Nullable @JsonProperty(PROP_ADDRESS_GROUPS) SortedSet<String> addressGroups,
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
      @Nullable SortedSet<String> addressGroups) {
    Names.checkName(name, "address group", Type.REFERENCE_OBJECT);

    _name = name;
    _addressGroups = firstNonNull(addressGroups, new TreeSet<>());
    _addresses = firstNonNull(addresses, new TreeSet<>());

    // check if all the input address strings can be mapped to an IpWildCard
    _addresses.forEach(IpWildcard::new);
  }

  @Override
  public int compareTo(AddressGroup o) {
    return _name.compareTo(o._name);
  }

  @JsonProperty(PROP_ADDRESS_GROUPS)
  @Nonnull
  public SortedSet<String> getAddressGroups() {
    return _addressGroups;
  }

  @JsonProperty(PROP_ADDRESSES)
  @Nonnull
  public SortedSet<String> getAddresses() {
    return _addresses;
  }

  @JsonProperty(PROP_NAME)
  @Nonnull
  public String getName() {
    return _name;
  }
}
