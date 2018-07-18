package org.batfish.role.addressbook;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.role.addressbook.Library.checkValidName;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Comparator;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;

public class AddressGroup implements Comparable<AddressGroup> {

  private static final String PROP_ADDRESSES = "addresses";
  private static final String PROP_NAME = "name";

  @Nonnull private SortedSet<IpSpace> _addresses;
  @Nonnull private String _name;

  public AddressGroup(
      @JsonProperty(PROP_ADDRESSES) SortedSet<String> addresses,
      @JsonProperty(PROP_NAME) String name) {
    checkArgument(addresses != null, "Address group addresses cannot not be null");
    checkArgument(name != null, "Address group name cannot not be null");
    checkValidName(name, "address group");

    _name = name;
    _addresses =
        addresses
            .stream()
            .map(s -> new IpWildcard(s).toIpSpace())
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }

  @Override
  public int compareTo(AddressGroup o) {
    return _name.compareTo(o._name);
  }

  @JsonProperty(PROP_ADDRESSES)
  public SortedSet<IpSpace> getAddresses() {
    return _addresses;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }
}
