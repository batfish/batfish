package org.batfish.representation.fortios;

import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

/** FortiOS datamodel component containing firewall service configuration */
public final class Service implements Serializable {

  public static Protocol DEFAULT_PROTOCOL = Protocol.TCP_UDP_SCTP;
  public static Integer DEFAULT_PROTOCOL_NUMBER = 0;
  public static IntegerSpace DEFAULT_SOURCE_PORT_RANGE = IntegerSpace.of(Range.closed(1, 65535));

  public enum Protocol {
    TCP_UDP_SCTP,
    ICMP,
    ICMP6,
    IP,
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @VisibleForTesting
  @Nonnull
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
  public String getComment() {
    return _comment;
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

  @Nullable
  public Integer getProtocolNumberEffective() {
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
    // If protocol changes, need to clear protocol-specified fields
    if (protocol != _protocol) {
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

  public void setComment(String comment) {
    _comment = comment;
  }

  public void setIcmpCode(int icmpCode) {
    _icmpCode = icmpCode;
  }

  public void setIcmpType(int icmpType) {
    _icmpType = icmpType;
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

  public Service(String name) {
    _name = name;
  }

  @Nonnull private String _name;
  @Nullable private Protocol _protocol;
  @Nullable private Integer _protocolNumber;
  @Nullable private String _comment;
  @Nullable private Integer _icmpCode;
  @Nullable private Integer _icmpType;
  @Nullable private IpRange _ipRange;
  @Nullable private IntegerSpace _tcpPortRangeDst;
  @Nullable private IntegerSpace _tcpPortRangeSrc;
  @Nullable private IntegerSpace _udpPortRangeDst;
  @Nullable private IntegerSpace _udpPortRangeSrc;
  @Nullable private IntegerSpace _sctpPortRangeDst;
  @Nullable private IntegerSpace _sctpPortRangeSrc;

  public @Nonnull AclLineMatchExpr toMatchExpr() {
    // TODO Incorporate _protocolNumber, _comment; support other protocols
    switch (getProtocolEffective()) {
      case TCP_UDP_SCTP:
        HeaderSpace.Builder tcp =
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setSrcPorts(getTcpPortRangeSrcEffective().getSubRanges());
        HeaderSpace.Builder udp =
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.UDP)
                .setSrcPorts(getUdpPortRangeSrcEffective().getSubRanges());
        HeaderSpace.Builder sctp =
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.SCTP)
                .setSrcPorts(getSctpPortRangeSrcEffective().getSubRanges());
        if (_tcpPortRangeDst != null) {
          tcp.setDstPorts(_tcpPortRangeDst.getSubRanges());
        }
        return or(
            "Matched service " + _name,
            new MatchHeaderSpace(tcp.build()),
            new MatchHeaderSpace(udp.build()),
            new MatchHeaderSpace(sctp.build()));
      case ICMP:
      case ICMP6:
      case IP:
      default:
        throw new UnsupportedOperationException();
    }
  }
}
