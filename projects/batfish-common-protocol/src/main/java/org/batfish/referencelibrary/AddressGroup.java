package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpWildcard;

public class AddressGroup implements Comparable<AddressGroup> {

  private static final String PROP_ADDRESSES = "addresses";
  private static final String PROP_NAME = "name";

  @Nonnull private SortedSet<String> _addresses;
  @Nonnull private String _name;

  public AddressGroup(
      @Nullable @JsonProperty(PROP_ADDRESSES) SortedSet<String> addresses,
      @Nullable @JsonProperty(PROP_NAME) String name) {
    checkArgument(name != null, "Address group name cannot not be null");
    ReferenceLibrary.checkValidName(name, "address group");

    _name = name;
    _addresses = firstNonNull(addresses, new TreeSet<>());

    // check if all the input address strings can be mapped to an IpWildCard
    _addresses.forEach(IpWildcard::new);
  }

  @Override
  public int compareTo(AddressGroup o) {
    return _name.compareTo(o._name);
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
