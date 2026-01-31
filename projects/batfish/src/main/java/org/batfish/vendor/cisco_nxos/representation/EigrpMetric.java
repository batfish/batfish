package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.Objects;
import org.batfish.datamodel.eigrp.EigrpMetricValues;

/** EIGRP metric values (bandwidth, delay, etc.) */
public final class EigrpMetric implements Serializable {
  private final long _bandwidth;
  private final long _delayTensOfMicroseconds;
  private final int _reliability;
  private final int _load;
  private final long _mtu;

  public EigrpMetric(long bandwidth, long delay, int reliability, int load, long mtu) {
    _bandwidth = bandwidth;
    _delayTensOfMicroseconds = delay;
    _reliability = reliability;
    _load = load;
    _mtu = mtu;
  }

  /** Bandwidth in Kb/s */
  public long getBandwidth() {
    return _bandwidth;
  }

  /** Delay in tens of microseconds */
  public long getDelayTensOfMicroseconds() {
    return _delayTensOfMicroseconds;
  }

  /** Reliability from 0 to 255 (100 percent reliable) */
  public int getReliability() {
    return _reliability;
  }

  /** Load from 1 to 255 (100 percent loaded) */
  public int getLoad() {
    return _load;
  }

  /** MTU of the path from 1 to 16777215 */
  public long getMtu() {
    return _mtu;
  }

  public EigrpMetricValues toEigrpMetricValues() {
    return EigrpMetricValues.builder()
        .setBandwidth(_bandwidth)
        // convert to picoseconds
        .setDelay(_delayTensOfMicroseconds * 1e7)
        .setReliability(_reliability)
        .setEffectiveBandwidth(_load)
        .setMtu(_mtu)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EigrpMetric)) {
      return false;
    }
    EigrpMetric that = (EigrpMetric) o;
    return _bandwidth == that._bandwidth
        && _delayTensOfMicroseconds == that._delayTensOfMicroseconds
        && _reliability == that._reliability
        && _load == that._load
        && _mtu == that._mtu;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_bandwidth, _delayTensOfMicroseconds, _reliability, _load, _mtu);
  }
}
