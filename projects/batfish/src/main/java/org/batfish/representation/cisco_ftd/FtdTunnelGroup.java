package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Represents a Cisco FTD tunnel group. */
public class FtdTunnelGroup implements Serializable {

    public enum Type {
        IPSEC_L2L,
        REMOTE_ACCESS
    }

    private final String _name;
    private Type _type;
    private String _ikev2Policy;
    private String _presharedKey;
    private String _presharedKeyStandby;

    public FtdTunnelGroup(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    @Nullable
    public Type getType() {
        return _type;
    }

    public void setType(Type type) {
        _type = type;
    }

    @Nullable
    public String getIkev2Policy() {
        return _ikev2Policy;
    }

    public void setIkev2Policy(String ikev2Policy) {
        _ikev2Policy = ikev2Policy;
    }

    @Nullable
    public String getPresharedKey() {
        return _presharedKey;
    }

    public void setPresharedKey(String presharedKey) {
        _presharedKey = presharedKey;
    }

    @Nullable
    public String getPresharedKeyStandby() {
        return _presharedKeyStandby;
    }

    public void setPresharedKeyStandby(String presharedKeyStandby) {
        _presharedKeyStandby = presharedKeyStandby;
    }
}
