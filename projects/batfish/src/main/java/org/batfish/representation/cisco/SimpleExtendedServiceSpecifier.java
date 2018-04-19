package org.batfish.representation.cisco;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.State;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public class SimpleExtendedServiceSpecifier implements ExtendedAccessListServiceSpecifier {

  public static class Builder {

    private Set<Integer> _dscps;

    private List<SubRange> _dstPortRanges;

    private Set<Integer> _ecns;

    private Integer _icmpCode;

    private Integer _icmpType;

    private IpProtocol _protocol;

    private List<SubRange> _srcPortRanges;

    private Set<State> _states;

    private List<TcpFlags> _tcpFlags;

    public SimpleExtendedServiceSpecifier build() {
      return new SimpleExtendedServiceSpecifier(this);
    }

    public Builder setDscps(Iterable<Integer> dscps) {
      _dscps = ImmutableSet.copyOf(dscps);
      return this;
    }

    public Builder setDstPortRanges(Iterable<SubRange> dstPortRanges) {
      _dstPortRanges = ImmutableList.copyOf(dstPortRanges);
      return this;
    }

    public Builder setEcns(Iterable<Integer> ecns) {
      _ecns = ImmutableSet.copyOf(ecns);
      return this;
    }

    public Builder setIcmpCode(Integer icmpCode) {
      _icmpCode = icmpCode;
      return this;
    }

    public Builder setIcmpType(Integer icmpType) {
      _icmpType = icmpType;
      return this;
    }

    public Builder setProtocol(IpProtocol protocol) {
      _protocol = protocol;
      return this;
    }

    public Builder setSrcPortRanges(Iterable<SubRange> srcPortRanges) {
      _srcPortRanges = ImmutableList.copyOf(srcPortRanges);
      return this;
    }

    public Builder setStates(Iterable<State> states) {
      _states = ImmutableSet.copyOf(states);
      return this;
    }

    public Builder setTcpFlags(Iterable<TcpFlags> tcpFlags) {
      _tcpFlags = ImmutableList.copyOf(tcpFlags);
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final Set<Integer> _dscps;

  private final List<SubRange> _dstPortRanges;

  private final Set<Integer> _ecns;

  private final Integer _icmpCode;

  private final Integer _icmpType;

  private final IpProtocol _protocol;

  private final List<SubRange> _srcPortRanges;

  private final Set<State> _states;

  private final List<TcpFlags> _tcpFlags;

  private SimpleExtendedServiceSpecifier(Builder builder) {
    _dscps = builder._dscps;
    _dstPortRanges = builder._dstPortRanges;
    _ecns = builder._ecns;
    _icmpCode = builder._icmpCode;
    _icmpType = builder._icmpType;
    _protocol = builder._protocol;
    _srcPortRanges = builder._srcPortRanges;
    _states = builder._states;
    _tcpFlags = builder._tcpFlags;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setDscps(_dscps)
            .setDstPorts(_dstPortRanges)
            .setEcns(_ecns)
            .setIcmpCodes(ImmutableSet.of(new SubRange(_icmpCode)))
            .setIcmpTypes(ImmutableSet.of(new SubRange(_icmpType)))
            .setIpProtocols(ImmutableSet.of(_protocol))
            .setSrcPorts(_srcPortRanges)
            .setStates(_states)
            .setTcpFlags(_tcpFlags)
            .build());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("dscps", _dscps)
        .add("dstPortRanges", _dstPortRanges)
        .add("ecns", _ecns)
        .add("icmpCode", _icmpCode)
        .add("icmpType", _icmpType)
        .add("protocol", _protocol)
        .add("srcPortRanges", _srcPortRanges)
        .add("states", _states)
        .add("tcpFlags", _tcpFlags)
        .toString();
  }
}
