package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpWildcard;

public final class AddressBook implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final Map<String, AddressBookEntry> _entries;

  private final Map<String, AddressBook> _globalBooks;

  private final String _name;

  public AddressBook(String name, Map<String, AddressBook> globalBooks) {
    _name = name;
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

  public String getName() {
    return _name;
  }
}
