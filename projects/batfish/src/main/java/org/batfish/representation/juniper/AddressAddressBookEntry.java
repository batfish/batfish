package org.batfish.representation.juniper;

import java.util.Collections;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpWildcard;

public final class AddressAddressBookEntry extends AddressBookEntry {

  /** */
  private static final long serialVersionUID = 1L;

  private final IpWildcard _ipWildcard;

  public AddressAddressBookEntry(String name, IpWildcard ipWildcard) {
    super(name);
    _ipWildcard = ipWildcard;
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public Set<IpWildcard> getIpWildcards(Warnings w) {
    return Collections.singleton(_ipWildcard);
  }
}
