package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpWildcard;

@ParametersAreNonnullByDefault
public final class AddressBook implements Serializable {

  @Nonnull private final Map<String, AddressBookEntry> _entries;

  @Nullable private final AddressBook _parentBook; // null for global address book

  @Nonnull private final String _name;

  public AddressBook(String name, @Nullable AddressBook parentBook) {
    _name = name;
    _entries = new TreeMap<>();
    _parentBook = parentBook;
  }

  /** Get address book for the corresponding entry. */
  private @Nullable AddressBook getAddressBook(String entryName) {
    if (_entries.get(entryName) != null) {
      return this;
    } else if (_parentBook != null && _parentBook._entries.get(entryName) != null) {
      return _parentBook;
    }
    return null;
  }

  /** Get the address book name for the corresponding entry. */
  @Nullable
  String getAddressBookName(String entryName) {
    AddressBook addressBook = getAddressBook(entryName);
    return (addressBook == null) ? null : addressBook.getName();
  }

  @Nonnull
  public Map<String, AddressBookEntry> getEntries() {
    return _entries;
  }

  SortedSet<IpWildcard> getIpWildcards(String entryName, Warnings w) {
    AddressBook addressBook = getAddressBook(entryName);
    AddressBookEntry entry = null;
    if (addressBook != null) {
      entry = addressBook.getEntries().get(entryName);
    }
    if (entry == null) {
      w.redFlag(
          "Could not find entry: \""
              + entryName
              + "\" in address book: \""
              + _name
              + "\" or any global address book");
      return Collections.emptySortedSet();
    } else {
      return entry.getIpWildcards(w);
    }
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  public boolean isGlobal() {
    return _parentBook == null;
  }
}
