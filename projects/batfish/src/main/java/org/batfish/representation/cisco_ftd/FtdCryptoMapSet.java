package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/** Container for a set of crypto map entries */
public class FtdCryptoMapSet implements Serializable {

    private final String _name;
    private final Map<Integer, FtdCryptoMapEntry> _entries;

    public FtdCryptoMapSet(String name) {
        _name = name;
        _entries = new TreeMap<>();
    }

    public String getName() {
        return _name;
    }

    public Map<Integer, FtdCryptoMapEntry> getEntries() {
        return _entries;
    }

    public void addEntry(FtdCryptoMapEntry entry) {
        _entries.put(entry.getSequenceNumber(), entry);
    }
}
