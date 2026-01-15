package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FtdAccessGroup implements Serializable {
    private final @Nonnull String _aclName;
    private final @Nullable String _interfaceName; // Null if global
    private final @Nonnull String _direction; // "in", "out", or "global"

    public FtdAccessGroup(
            @Nonnull String aclName, @Nullable String interfaceName, @Nonnull String direction) {
        _aclName = aclName;
        _interfaceName = interfaceName;
        _direction = direction;
    }

    public @Nonnull String getAclName() {
        return _aclName;
    }

    public @Nullable String getInterfaceName() {
        return _interfaceName;
    }

    public @Nonnull String getDirection() {
        return _direction;
    }
}
