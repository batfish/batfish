package org.batfish.representation.juniper;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.IpWildcard;

public final class AddressBook extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private final Map<String, AddressBookEntry> _entries;

  private final Map<String, AddressBook> _globalBooks;

  public AddressBook(String name, Map<String, AddressBook> globalBooks) {
    super(name);
    _entries = new TreeMap<>();
    _globalBooks = globalBooks;
  }

  /** Get address book for the corresponding entry. */
  private @Nullable AddressBook getAddressBook(String entryName) {
    if (_entries.get(entryName) != null) {
      return this;
    } else {
      for (AddressBook globalBook : _globalBooks.values()) {
        if (globalBook._entries.get(entryName) != null) {
          return globalBook;
        }
      }
    }
    return null;
  }

  /** Get the address book name for the corresponding entry. */
  @Nullable
  String getAddressBookName(String entryName) {
    AddressBook addressBook = getAddressBook(entryName);
    return (addressBook == null) ? null : addressBook.getName();
  }

  public Map<String, AddressBookEntry> getEntries() {
    return _entries;
  }

  Set<IpWildcard> getIpWildcards(String entryName, Warnings w) {
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
              + _key
              + "\" or any global address book");
      return Collections.emptySet();
    } else {
      return entry.getIpWildcards(w);
    }
  }
}
