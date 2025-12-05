package org.batfish.representation.cisco_asa;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public class SimpleExtendedAccessListServiceSpecifier implements AccessListServiceSpecifier {

  public static class Builder {

    private Set<Integer> _dscps = ImmutableSet.of();

    private List<SubRange> _dstPortRanges = ImmutableList.of();

    private Set<Integer> _ecns = ImmutableSet.of();

    private Integer _icmpCode;

    private Integer _icmpType;

    private IpProtocol _protocol;

    private List<SubRange> _srcPortRanges = ImmutableList.of();

    private List<TcpFlagsMatchConditions> _tcpFlags = ImmutableList.of();

    public SimpleExtendedAccessListServiceSpecifier build() {
      return new SimpleExtendedAccessListServiceSpecifier(this);
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

    public Builder setTcpFlags(Iterable<TcpFlagsMatchConditions> tcpFlags) {
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

  private final List<TcpFlagsMatchConditions> _tcpFlags;

  private SimpleExtendedAccessListServiceSpecifier(Builder builder) {
    _dscps = builder._dscps;
    _dstPortRanges = builder._dstPortRanges;
    _ecns = builder._ecns;
    _icmpCode = builder._icmpCode;
    _icmpType = builder._icmpType;
    _protocol = builder._protocol;
    _srcPortRanges = builder._srcPortRanges;
    _tcpFlags = builder._tcpFlags;
  }

  @Override
  public @Nonnull AclLineMatchExpr toAclLineMatchExpr(
      Map<String, ObjectGroup> objectGroups, Map<String, ServiceObject> serviceObjects) {
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setDscps(_dscps)
            .setDstPorts(_dstPortRanges)
            .setEcns(_ecns)
            .setIcmpCodes(
                _icmpCode != null ? ImmutableSet.of(new SubRange(_icmpCode)) : ImmutableSet.of())
            .setIcmpTypes(
                _icmpType != null ? ImmutableSet.of(new SubRange(_icmpType)) : ImmutableSet.of())
            .setIpProtocols(_protocol != null ? ImmutableSet.of(_protocol) : ImmutableSet.of())
            .setSrcPorts(_srcPortRanges)
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
        .add("tcpFlags", _tcpFlags)
        .toString();
  }
}
