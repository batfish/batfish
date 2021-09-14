package org.batfish.vendor.check_point_management;

import static org.batfish.datamodel.IntegerSpace.PORTS;
import static org.batfish.datamodel.applications.PortsApplication.MAX_PORT_NUMBER;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.serviceCpmiAnyTraceElement;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.serviceGroupTraceElement;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.serviceIcmpTraceElement;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.serviceTcpTraceElement;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.serviceUdpTraceElement;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;

/** Generates an {@link AclLineMatchExpr} for the specified {@link Service}. */
public class ServiceToMatchExpr implements ServiceVisitor<AclLineMatchExpr> {

  public ServiceToMatchExpr(Map<Uid, NamedManagementObject> objs) {
    _objs = objs;
  }

  @Override
  public AclLineMatchExpr visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject) {
    // Does not constrain headerspace
    return new TrueExpr(serviceCpmiAnyTraceElement());
  }

  @Override
  public AclLineMatchExpr visitServiceGroup(ServiceGroup serviceGroup) {
    return getDescendantMatchExpr(serviceGroup, new HashSet<>());
  }

  @Override
  public AclLineMatchExpr visitServiceIcmp(ServiceIcmp serviceIcmp) {
    ImmutableList.Builder<AclLineMatchExpr> exprs = ImmutableList.builder();
    int type = serviceIcmp.getIcmpType();

    exprs.add(
        AclLineMatchExprs.matchIpProtocol(
            IpProtocol.ICMP, ipProtocolTraceElement(IpProtocol.ICMP)));
    exprs.add(AclLineMatchExprs.matchIcmpType(type, icmpTypeTraceElement(type)));
    Optional.ofNullable(serviceIcmp.getIcmpCode())
        .ifPresent(
            code ->
                exprs.add(
                    AclLineMatchExprs.match(
                        HeaderSpace.builder().setIcmpCodes(code).build(),
                        icmpCodeTraceElement(code))));

    return new AndMatchExpr(exprs.build(), serviceIcmpTraceElement(serviceIcmp));
  }

  @Override
  public AclLineMatchExpr visitServiceTcp(ServiceTcp serviceTcp) {
    String portDefinition = serviceTcp.getPort();
    return AclLineMatchExprs.and(
        serviceTcpTraceElement(serviceTcp),
        AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP, ipProtocolTraceElement(IpProtocol.TCP)),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setDstPorts(portStringToIntegerSpace(portDefinition).getSubRanges())
                .build(),
            destPortTraceElement(portDefinition)));
  }

  @Override
  public AclLineMatchExpr visitServiceUdp(ServiceUdp serviceUdp) {
    String portDefinition = serviceUdp.getPort();
    return AclLineMatchExprs.and(
        serviceUdpTraceElement(serviceUdp),
        AclLineMatchExprs.matchIpProtocol(IpProtocol.UDP, ipProtocolTraceElement(IpProtocol.UDP)),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setDstPorts(portStringToIntegerSpace(portDefinition).getSubRanges())
                .build(),
            destPortTraceElement(portDefinition)));
  }

  @VisibleForTesting
  static TraceElement ipProtocolTraceElement(IpProtocol ipProtocol) {
    return TraceElement.of(String.format("Matched IP protocol %s", ipProtocol));
  }

  @VisibleForTesting
  static TraceElement destPortTraceElement(String portDefinition) {
    return TraceElement.of(String.format("Matched destination port '%s'", portDefinition));
  }

  @VisibleForTesting
  static TraceElement icmpCodeTraceElement(int code) {
    return TraceElement.of(String.format("Matched ICMP code %s", code));
  }

  @VisibleForTesting
  static TraceElement icmpTypeTraceElement(int type) {
    return TraceElement.of(String.format("Matched ICMP type %s", type));
  }

  /**
   * Returns an {@link AclLineMatchExpr} representing descendant objects. Keeps track of visited
   * descendants to prevent loops, though these should not occur in real configs.
   */
  private AclLineMatchExpr getDescendantMatchExpr(
      ServiceGroup group, Set<Uid> alreadyTraversedMembers) {
    Uid groupUid = group.getUid();
    alreadyTraversedMembers.add(groupUid);

    List<AclLineMatchExpr> descendantObjExprs = new ArrayList<>();
    for (Uid memberUid : group.getMembers()) {
      NamedManagementObject member = _objs.get(memberUid);
      if (member instanceof ServiceGroup) {
        if (!alreadyTraversedMembers.contains(memberUid)) {
          descendantObjExprs.add(
              getDescendantMatchExpr((ServiceGroup) member, alreadyTraversedMembers));
        }
      } else if (member instanceof Service) {
        descendantObjExprs.add(this.visit((Service) member));
      } else {
        // Don't match non-servicey objects
        descendantObjExprs.add(FalseExpr.INSTANCE);
      }
    }
    return AclLineMatchExprs.or(serviceGroupTraceElement(group), descendantObjExprs);
  }

  /** Convert an entire CheckPoint port string to an {@link IntegerSpace}. */
  @VisibleForTesting
  static @Nonnull IntegerSpace portStringToIntegerSpace(String portStr) {
    String[] ranges = portStr.split(",", -1);
    IntegerSpace.Builder builder = IntegerSpace.builder();
    for (String range : ranges) {
      builder.including(portRangeStringToIntegerSpace(range.trim()));
    }
    return builder.build();
  }

  /** Convert a single element of a CheckPoint port string to an {@link IntegerSpace}. */
  @VisibleForTesting
  static @Nonnull IntegerSpace portRangeStringToIntegerSpace(String range) {
    if (range.isEmpty()) {
      // warn? all ports instead?
      return IntegerSpace.EMPTY;
    }
    IntegerSpace raw;
    char firstChar = range.charAt(0);
    if ('0' <= firstChar && firstChar <= '9') {
      // Examples:
      // 123
      // 50-90
      raw = IntegerSpace.parse(range);
    } else if (range.startsWith("<=")) {
      // Example: <=10
      raw = IntegerSpace.of(new SubRange(0, Integer.parseInt(range.substring(2))));
    } else if (range.startsWith("<")) {
      // Example: <10
      raw = IntegerSpace.of(new SubRange(0, Integer.parseInt(range.substring(1)) - 1));
    } else if (range.startsWith(">=")) {
      raw = IntegerSpace.of(new SubRange(Integer.parseInt(range.substring(2)), MAX_PORT_NUMBER));
    } else if (range.startsWith(">")) {
      raw =
          IntegerSpace.of(new SubRange(Integer.parseInt(range.substring(1)) + 1, MAX_PORT_NUMBER));
    } else {
      // unhandled
      // TODO: warn
      raw = IntegerSpace.EMPTY;
    }
    return raw.intersection(PORTS);
  }

  private final @Nonnull Map<Uid, NamedManagementObject> _objs;
}
