package org.batfish.representation.cisco;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public class SimpleExtendedAccessListServiceSpecifier implements AccessListServiceSpecifier {

  public static class Builder {

    private Set<Integer> _dscps = ImmutableSet.of();

    private @Nullable PortSpec _dstPorts;

    private Set<Integer> _ecns = ImmutableSet.of();

    private Integer _icmpCode;

    private Integer _icmpType;

    private IpProtocol _protocol;

    private @Nullable PortSpec _srcPorts;

    private List<TcpFlagsMatchConditions> _tcpFlags = ImmutableList.of();

    public SimpleExtendedAccessListServiceSpecifier build() {
      return new SimpleExtendedAccessListServiceSpecifier(this);
    }

    public Builder setDscps(Iterable<Integer> dscps) {
      _dscps = ImmutableSet.copyOf(dscps);
      return this;
    }

    public Builder setDstPorts(@Nullable PortSpec dstPorts) {
      _dstPorts = dstPorts;
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

    public Builder setSrcPorts(@Nullable PortSpec srcPorts) {
      _srcPorts = srcPorts;
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

  private final @Nullable PortSpec _dstPorts;

  private final Set<Integer> _ecns;

  private final Integer _icmpCode;

  private final Integer _icmpType;

  private final IpProtocol _protocol;

  private final @Nullable PortSpec _srcPorts;

  private final List<TcpFlagsMatchConditions> _tcpFlags;

  private SimpleExtendedAccessListServiceSpecifier(Builder builder) {
    _dscps = builder._dscps;
    _dstPorts = builder._dstPorts;
    _ecns = builder._ecns;
    _icmpCode = builder._icmpCode;
    _icmpType = builder._icmpType;
    _protocol = builder._protocol;
    _srcPorts = builder._srcPorts;
    _tcpFlags = builder._tcpFlags;
  }

  @Override
  @Nonnull
  public AclLineMatchExpr toAclLineMatchExpr(Map<String, ObjectGroup> objectGroups) {
    Iterable<SubRange> dstPortRanges = ImmutableList.of();
    Iterable<SubRange> srcPortRanges = ImmutableList.of();
    if (_dstPorts != null) {
      Optional<IntegerSpace> dstPortSpace = toPorts(_dstPorts, objectGroups);
      if (!dstPortSpace.isPresent()) {
        return FalseExpr.INSTANCE;
      }
      dstPortRanges = dstPortSpace.get().getSubRanges();
    }
    if (_srcPorts != null) {
      Optional<IntegerSpace> srcPortSpace = toPorts(_srcPorts, objectGroups);
      if (!srcPortSpace.isPresent()) {
        return FalseExpr.INSTANCE;
      }
      srcPortRanges = srcPortSpace.get().getSubRanges();
    }
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setDscps(_dscps)
            .setDstPorts(dstPortRanges)
            .setEcns(_ecns)
            .setIcmpCodes(
                _icmpCode != null ? ImmutableSet.of(new SubRange(_icmpCode)) : ImmutableSet.of())
            .setIcmpTypes(
                _icmpType != null ? ImmutableSet.of(new SubRange(_icmpType)) : ImmutableSet.of())
            .setIpProtocols(_protocol != null ? ImmutableSet.of(_protocol) : ImmutableSet.of())
            .setSrcPorts(srcPortRanges)
            .setTcpFlags(_tcpFlags)
            .build());
  }

  /**
   * Return an {@link IntegerSpace} of allowed ports if {@code portSpec} or an empty Optional if the
   * corresponding port group object is undefined.
   */
  private @Nonnull Optional<IntegerSpace> toPorts(
      PortSpec portSpec, Map<String, ObjectGroup> objectGroups) {
    return portSpec.accept(
        new PortSpecVisitor<Optional<IntegerSpace>>() {
          @Override
          public Optional<IntegerSpace> visitLiteralPortSpec(LiteralPortSpec literalPortSpec) {
            return Optional.of(IntegerSpace.unionOfSubRanges(literalPortSpec.getPorts()));
          }

          @Override
          public Optional<IntegerSpace> visitPortGroupPortSpec(
              PortObjectGroupPortSpec portGroupPortSpec) {
            if (!objectGroups.containsKey(portGroupPortSpec.getName())) {
              return Optional.empty();
            }
            ObjectGroup objectGroup = objectGroups.get(portGroupPortSpec.getName());
            if (!(objectGroup instanceof PortObjectGroup)) {
              return Optional.empty();
            }
            return Optional.of(
                IntegerSpace.unionOfSubRanges(
                    ((PortObjectGroup) objectGroup)
                        .getLines().stream()
                            .flatMap(line -> line.getRanges().stream())
                            .collect(Collectors.toList())));
          }
        });
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("dscps", _dscps)
        .add("dstPortRanges", _dstPorts)
        .add("ecns", _ecns)
        .add("icmpCode", _icmpCode)
        .add("icmpType", _icmpType)
        .add("protocol", _protocol)
        .add("srcPortRanges", _srcPorts)
        .add("tcpFlags", _tcpFlags)
        .toString();
  }
}
