package org.batfish.representation.fortios;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpRange;

/** FortiOS datamodel component containing firewall service configuration */
public final class Service extends ServiceGroupMember implements Serializable {

  public static final Protocol DEFAULT_PROTOCOL = Protocol.TCP_UDP_SCTP;
  public static final int DEFAULT_PROTOCOL_NUMBER = 0;
  public static final IntegerSpace DEFAULT_SOURCE_PORT_RANGE =
      IntegerSpace.of(Range.closed(1, 65535));

  public enum Protocol {
    TCP_UDP_SCTP,
    ICMP,
    ICMP6,
    IP,
  }

  @Override
  @Nonnull
  public String getName() {
    return _name;
  }

  @Override
  public BatfishUUID getBatfishUUID() {
    return _uuid;
  }

  @Override
  public void setName(String name) {
    _name = name;
  }

  @VisibleForTesting
  @Nullable
  public Protocol getProtocol() {
    return _protocol;
  }

  @Nonnull
  public Protocol getProtocolEffective() {
    if (_protocol == null) {
      return DEFAULT_PROTOCOL;
    }
    return _protocol;
  }

  @Nullable
  public Integer getIcmpCode() {
    return _icmpCode;
  }

  @Nullable
  public Integer getIcmpType() {
    return _icmpType;
  }

  @Nullable
  public IpRange getIpRange() {
    return _ipRange;
  }

  @VisibleForTesting
  @Nullable
  public Integer getProtocolNumber() {
    return _protocolNumber;
  }

  public int getProtocolNumberEffective() {
    return _protocolNumber == null ? DEFAULT_PROTOCOL_NUMBER : _protocolNumber;
  }

  @Nullable
  public IntegerSpace getTcpPortRangeDst() {
    return _tcpPortRangeDst;
  }

  @VisibleForTesting
  @Nullable
  public IntegerSpace getTcpPortRangeSrc() {
    return _tcpPortRangeSrc;
  }

  @Nullable
  public IntegerSpace getTcpPortRangeSrcEffective() {
    return _tcpPortRangeSrc == null ? DEFAULT_SOURCE_PORT_RANGE : _tcpPortRangeSrc;
  }

  @Nullable
  public IntegerSpace getUdpPortRangeDst() {
    return _udpPortRangeDst;
  }

  @VisibleForTesting
  @Nullable
  public IntegerSpace getUdpPortRangeSrc() {
    return _udpPortRangeSrc;
  }

  @Nullable
  public IntegerSpace getUdpPortRangeSrcEffective() {
    return _udpPortRangeSrc == null ? DEFAULT_SOURCE_PORT_RANGE : _udpPortRangeSrc;
  }

  @Nullable
  public IntegerSpace getSctpPortRangeDst() {
    return _sctpPortRangeDst;
  }

  @VisibleForTesting
  @Nullable
  public IntegerSpace getSctpPortRangeSrc() {
    return _sctpPortRangeSrc;
  }

  @Nullable
  public IntegerSpace getSctpPortRangeSrcEffective() {
    return _sctpPortRangeSrc == null ? DEFAULT_SOURCE_PORT_RANGE : _sctpPortRangeSrc;
  }

  public void setProtocol(Protocol protocol) {
    // If effective protocol changes, need to clear protocol-specified fields
    if (protocol != getProtocolEffective()) {
      _icmpCode = null;
      _icmpType = null;
      _protocolNumber = null;
      _tcpPortRangeDst = null;
      _tcpPortRangeSrc = null;
      _udpPortRangeDst = null;
      _udpPortRangeSrc = null;
      _sctpPortRangeDst = null;
      _sctpPortRangeSrc = null;
    }
    _protocol = protocol;
  }

  public void setProtocolNumber(int protocolNumber) {
    _protocolNumber = protocolNumber;
  }

  public void setIcmpCode(int icmpCode) {
    _icmpCode = icmpCode;
  }

  public void setIcmpType(@Nullable Integer icmpType) {
    _icmpType = icmpType;
    if (_icmpType == null) { // When ICMP type is cleared, it also clears ICMP code
      _icmpCode = null;
    }
  }

  public void setIpRange(IpRange ipRange) {
    _ipRange = ipRange;
  }

  public void setTcpPortRangeDst(@Nullable IntegerSpace tcpPortRange) {
    _tcpPortRangeDst = tcpPortRange;
  }

  public void setTcpPortRangeSrc(@Nullable IntegerSpace tcpPortRange) {
    _tcpPortRangeSrc = tcpPortRange;
  }

  public void setUdpPortRangeDst(@Nullable IntegerSpace udpPortRange) {
    _udpPortRangeDst = udpPortRange;
  }

  public void setUdpPortRangeSrc(@Nullable IntegerSpace udpPortRange) {
    _udpPortRangeSrc = udpPortRange;
  }

  public void setSctpPortRangeDst(@Nullable IntegerSpace sctpPortRange) {
    _sctpPortRangeDst = sctpPortRange;
  }

  public void setSctpPortRangeSrc(@Nullable IntegerSpace sctpPortRange) {
    _sctpPortRangeSrc = sctpPortRange;
  }

  public Service(String name, BatfishUUID uuid) {
    _name = name;
    _uuid = uuid;
  }

  @Nonnull private String _name;
  @Nonnull private final BatfishUUID _uuid;
  @Nullable private Protocol _protocol;
  @Nullable private Integer _protocolNumber;
  @Nullable private Integer _icmpCode;
  @Nullable private Integer _icmpType;
  @Nullable private IpRange _ipRange;
  @Nullable private IntegerSpace _tcpPortRangeDst;
  @Nullable private IntegerSpace _tcpPortRangeSrc;
  @Nullable private IntegerSpace _udpPortRangeDst;
  @Nullable private IntegerSpace _udpPortRangeSrc;
  @Nullable private IntegerSpace _sctpPortRangeDst;
  @Nullable private IntegerSpace _sctpPortRangeSrc;
}
