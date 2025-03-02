package org.batfish.representation.azure;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

import java.util.BitSet;

// not an Azure component
public class IpAllocator {

    private final BitSet _usedIps;
    private final long _startIp;
    private final long _endIp;
    private final int lowestUsedIp;

    public IpAllocator(Prefix prefix) {
        _startIp = prefix.getStartIp().asLong();
        _endIp = prefix.getEndIp().asLong();
        _usedIps = new BitSet();
        lowestUsedIp = (int) prefix.getFirstHostIp().asLong();
    }

    /*
    Manually allocate ip
     */
    public boolean registerIp(Ip ip){
        int offset = (int) (ip.asLong() - _startIp);

        if(offset < 0 || ip.asLong() > _endIp) {
            throw new IllegalArgumentException("Ip " + ip + " is out of range");
        }

        if(_usedIps.get(offset)){
            return false;
        }

        _usedIps.set(offset);
        return true;
    }

    /*
    Get a free ip once every static ip has been registered
     */
    public Ip getFreeIp() {
        if(lowestUsedIp >= _endIp) {
            throw new BatfishException("Network is full, cannot allocate free ip");
        }
        int offset = _usedIps.nextClearBit(lowestUsedIp);
        _usedIps.set(offset);
        return Ip.create(_startIp + offset);
    }
}
