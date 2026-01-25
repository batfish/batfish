package org.batfish.representation.fortios;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;

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
    ALL,
  }

  @Override
  public @Nonnull String getName() {
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
  public @Nullable Protocol getProtocol() {
    return _protocol;
  }

  public @Nonnull Protocol getProtocolEffective() {
    if (_protocol == null) {
      return DEFAULT_PROTOCOL;
    }
    return _protocol;
  }

  public @Nullable Integer getIcmpCode() {
    return _icmpCode;
  }

  public @Nullable Integer getIcmpType() {
    return _icmpType;
  }

  @VisibleForTesting
  public @Nullable Integer getProtocolNumber() {
    return _protocolNumber;
  }

  public int getProtocolNumberEffective() {
    return _protocolNumber == null ? DEFAULT_PROTOCOL_NUMBER : _protocolNumber;
  }

  public @Nullable IntegerSpace getTcpPortRangeDst() {
    return _tcpPortRangeDst;
  }

  @VisibleForTesting
  public @Nullable IntegerSpace getTcpPortRangeSrc() {
    return _tcpPortRangeSrc;
  }

  public @Nullable IntegerSpace getTcpPortRangeSrcEffective() {
    return _tcpPortRangeSrc == null ? DEFAULT_SOURCE_PORT_RANGE : _tcpPortRangeSrc;
  }

  public @Nullable IntegerSpace getUdpPortRangeDst() {
    return _udpPortRangeDst;
  }

  @VisibleForTesting
  public @Nullable IntegerSpace getUdpPortRangeSrc() {
    return _udpPortRangeSrc;
  }

  public @Nullable IntegerSpace getUdpPortRangeSrcEffective() {
    return _udpPortRangeSrc == null ? DEFAULT_SOURCE_PORT_RANGE : _udpPortRangeSrc;
  }

  public @Nullable IntegerSpace getSctpPortRangeDst() {
    return _sctpPortRangeDst;
  }

  @VisibleForTesting
  public @Nullable IntegerSpace getSctpPortRangeSrc() {
    return _sctpPortRangeSrc;
  }

  public @Nullable IntegerSpace getSctpPortRangeSrcEffective() {
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

  public void setIcmpCode(@Nullable Integer icmpCode) {
    _icmpCode = icmpCode;
  }

  public void setIcmpType(@Nullable Integer icmpType) {
    _icmpType = icmpType;
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

  private @Nonnull String _name;
  private final @Nonnull BatfishUUID _uuid;
  private @Nullable Protocol _protocol;
  private @Nullable Integer _protocolNumber;
  private @Nullable Integer _icmpCode;
  private @Nullable Integer _icmpType;
  private @Nullable IntegerSpace _tcpPortRangeDst;
  private @Nullable IntegerSpace _tcpPortRangeSrc;
  private @Nullable IntegerSpace _udpPortRangeDst;
  private @Nullable IntegerSpace _udpPortRangeSrc;
  private @Nullable IntegerSpace _sctpPortRangeDst;
  private @Nullable IntegerSpace _sctpPortRangeSrc;
}
