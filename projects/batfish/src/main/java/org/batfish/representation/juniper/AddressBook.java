package org.batfish.representation.juniper;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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

  public Map<String, AddressBookEntry> getEntries() {
    return _entries;
  }

  public Set<IpWildcard> getIpWildcards(String entryName, Warnings w) {
    AddressBookEntry entry = _entries.get(entryName);
    if (entry == null) {
      for (AddressBook globalBook : _globalBooks.values()) {
        entry = globalBook._entries.get(entryName);
        if (entry != null) {
          break;
        }
      }
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
