package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Represents a Cisco FTD access list.
 */
public class FtdAccessList implements Serializable {

    private final @Nonnull String _name;
    private @Nonnull List<FtdAccessListLine> _lines;

    public FtdAccessList(@Nonnull String name) {
        _name = name;
        _lines = new ArrayList<>();
    }

    public @Nonnull String getName() {
        return _name;
    }

    public @Nonnull List<FtdAccessListLine> getLines() {
        return _lines;
    }

    public void addLine(@Nonnull FtdAccessListLine line) {
        _lines.add(line);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AccessList: ").append(_name).append("\n");
        for (FtdAccessListLine line : _lines) {
            sb.append("  ").append(line).append("\n");
        }
        return sb.toString();
    }
}
