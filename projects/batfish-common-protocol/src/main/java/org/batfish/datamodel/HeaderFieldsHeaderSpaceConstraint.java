package org.batfish.datamodel;

import java.util.List;
import java.util.SortedSet;

/**
 * HeaderSpaceConstraint on all fields that don't have their own HeaderSpaceConstraint class. We can
 * break these out further as the need arises.
 */
public class HeaderFieldsHeaderSpaceConstraint implements HeaderSpaceConstraint {

  private final SortedSet<Integer> _dscps;

  private final SortedSet<Protocol> _dstProtocols;

  private final SortedSet<Integer> _ecns;

  private final SortedSet<SubRange> _fragmentOffsets;

  private final SortedSet<SubRange> _icmpCodes;

  private final SortedSet<SubRange> _icmpTypes;

  private final SortedSet<IpProtocol> _ipProtocols;

  private final SortedSet<SubRange> _packetLengths;

  private final SortedSet<Protocol> _srcProtocols;

  private final SortedSet<State> _states;

  private final List<TcpFlags> _tcpFlags;

  private HeaderFieldsHeaderSpaceConstraint(Builder builder) {
    _dscps = builder._dscps;
    _dstProtocols = builder._dstProtocols;
    _ecns = builder._ecns;
    _fragmentOffsets = builder._fragmentOffsets;
    _icmpCodes = builder._icmpCodes;
    _icmpTypes = builder._icmpTypes;
    _ipProtocols = builder._ipProtocols;
    _packetLengths = builder._packetLengths;
    _srcProtocols = builder._srcProtocols;
    _states = builder._states;
    _tcpFlags = builder._tcpFlags;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public <T> T accept(HeaderSpaceConstraintVisitor<T> visitor) {
    return visitor.visitHeaderFieldHeaderSpaceConstraint(this);
  }

  public static final class Builder {
    private SortedSet<Integer> _dscps;
    private SortedSet<Protocol> _dstProtocols;
    private SortedSet<Integer> _ecns;
    private SortedSet<SubRange> _fragmentOffsets;
    private SortedSet<SubRange> _icmpCodes;
    private SortedSet<SubRange> _icmpTypes;
    private SortedSet<IpProtocol> _ipProtocols;
    private SortedSet<SubRange> _packetLengths;
    private SortedSet<Protocol> _srcProtocols;
    private SortedSet<State> _states;
    private List<TcpFlags> _tcpFlags;

    private Builder() {}

    public Builder setDscps(SortedSet<Integer> dscps) {
      this._dscps = dscps;
      return this;
    }

    public Builder setDstProtocols(SortedSet<Protocol> dstProtocols) {
      this._dstProtocols = dstProtocols;
      return this;
    }

    public Builder setEcns(SortedSet<Integer> ecns) {
      this._ecns = ecns;
      return this;
    }

    public Builder setFragmentOffsets(SortedSet<SubRange> fragmentOffsets) {
      this._fragmentOffsets = fragmentOffsets;
      return this;
    }

    public Builder setIcmpCodes(SortedSet<SubRange> icmpCodes) {
      this._icmpCodes = icmpCodes;
      return this;
    }

    public Builder setIcmpTypes(SortedSet<SubRange> icmpTypes) {
      this._icmpTypes = icmpTypes;
      return this;
    }

    public Builder setIpProtocols(SortedSet<IpProtocol> ipProtocols) {
      this._ipProtocols = ipProtocols;
      return this;
    }

    public Builder setPacketLengths(SortedSet<SubRange> packetLengths) {
      this._packetLengths = packetLengths;
      return this;
    }

    public Builder setSrcProtocols(SortedSet<Protocol> srcProtocols) {
      this._srcProtocols = srcProtocols;
      return this;
    }

    public Builder setStates(SortedSet<State> states) {
      this._states = states;
      return this;
    }

    public Builder setTcpFlags(List<TcpFlags> tcpFlags) {
      this._tcpFlags = tcpFlags;
      return this;
    }

    public HeaderFieldsHeaderSpaceConstraint build() {
      return new HeaderFieldsHeaderSpaceConstraint(this);
    }
  }
}
