package org.batfish.datamodel.acl;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.Traverser;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.trace.TraceNode;
import org.batfish.datamodel.trace.Tracer;
import org.batfish.datamodel.visitors.IpSpaceTracer;

/**
 * Tracer for {@link IpAccessList}, {@link IpSpace}, {@link HeaderSpace}.<br>
 * Acts like {@link Evaluator} on {@link IpAccessList}, except that it introduces tracing when
 * encountering traceable classes.
 */
public final class AclTracer extends AclLineEvaluator {

  public static AclTrace trace(
      @Nonnull IpAccessList ipAccessList,
      @Nonnull Flow flow,
      @Nullable String srcInterface,
      @Nonnull Map<String, IpAccessList> availableAcls,
      @Nonnull Map<String, IpSpace> namedIpSpaces,
      @Nonnull Map<String, IpSpaceMetadata> namedIpSpaceMetadata) {
    AclTracer tracer =
        new AclTracer(flow, srcInterface, availableAcls, namedIpSpaces, namedIpSpaceMetadata);
    tracer.trace(ipAccessList);
    return tracer.getTrace();
  }

  private final Map<IpSpace, IpSpaceMetadata> _ipSpaceMetadata;

  private final Map<IpSpace, String> _ipSpaceNames;

  private final @Nonnull Tracer _tracer;

  public AclTracer(
      @Nonnull Flow flow,
      @Nullable String srcInterface,
      @Nonnull Map<String, IpAccessList> availableAcls,
      @Nonnull Map<String, IpSpace> namedIpSpaces,
      @Nonnull Map<String, IpSpaceMetadata> namedIpSpaceMetadata) {
    super(flow, srcInterface, availableAcls, namedIpSpaces);
    _ipSpaceNames = new IdentityHashMap<>();
    _ipSpaceMetadata = new IdentityHashMap<>();
    _tracer = new Tracer();
    namedIpSpaces.forEach((name, ipSpace) -> _ipSpaceNames.put(ipSpace, name));
    namedIpSpaceMetadata.forEach(
        (name, ipSpaceMetadata) -> _ipSpaceMetadata.put(namedIpSpaces.get(name), ipSpaceMetadata));
  }

  public Flow getFlow() {
    return _flow;
  }

  public @Nonnull AclTrace getTrace() {
    TraceNode root = _tracer.getTrace();
    return new AclTrace(
        ImmutableList.copyOf(Traverser.forTree(TraceNode::getChildren).depthFirstPreOrder(root))
            .stream()
            .map(TraceNode::getTraceEvent)
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList()));
  }

  public void recordAction(
      @Nonnull IpAccessList ipAccessList, int index, @Nonnull AclLine line, LineAction action) {
    String lineDescription = firstNonNull(line.getName(), line.toString());
    String type = firstNonNull(ipAccessList.getSourceType(), "filter");
    String name = firstNonNull(ipAccessList.getSourceName(), ipAccessList.getName());
    String actionStr = action == LineAction.PERMIT ? "permitted" : "denied";
    String description =
        String.format(
            "Flow %s by %s named %s, index %d: %s", actionStr, type, name, index, lineDescription);
    if (action == LineAction.PERMIT) {
      _tracer.setEvent(
          new PermittedByAclLine(description, index, lineDescription, ipAccessList.getName()));
    } else {
      _tracer.setEvent(
          new DeniedByAclLine(description, index, lineDescription, ipAccessList.getName()));
    }
  }

  public void recordDefaultDeny(@Nonnull IpAccessList ipAccessList) {
    _tracer.setEvent(
        new DefaultDeniedByIpAccessList(
            ipAccessList.getName(), ipAccessList.getSourceName(), ipAccessList.getSourceType()));
  }

  private static boolean rangesContain(Collection<SubRange> ranges, @Nullable Integer num) {
    return num != null && ranges.stream().anyMatch(sr -> sr.includes(num));
  }

  private boolean trace(@Nonnull HeaderSpace headerSpace) {
    if (!headerSpace.getDscps().isEmpty() && !headerSpace.getDscps().contains(_flow.getDscp())) {
      return false;
    }
    if (!headerSpace.getNotDscps().isEmpty()
        && headerSpace.getNotDscps().contains(_flow.getDscp())) {
      return false;
    }
    if (headerSpace.getDstIps() != null && !traceDstIp(headerSpace.getDstIps(), _flow.getDstIp())) {
      return false;
    }
    if (headerSpace.getNotDstIps() != null
        && traceDstIp(headerSpace.getNotDstIps(), _flow.getDstIp())) {
      return false;
    }
    if (!headerSpace.getDstPorts().isEmpty()
        && !rangesContain(headerSpace.getDstPorts(), _flow.getDstPort())) {
      return false;
    }
    if (!headerSpace.getNotDstPorts().isEmpty()
        && rangesContain(headerSpace.getNotDstPorts(), _flow.getDstPort())) {
      return false;
    }
    if (!headerSpace.getDstProtocols().isEmpty()) {
      boolean match = false;
      for (Protocol dstProtocol : headerSpace.getDstProtocols()) {
        if (dstProtocol.getIpProtocol().equals(_flow.getIpProtocol())) {
          match = true;
          Integer dstPort = dstProtocol.getPort();
          if (!dstPort.equals(_flow.getDstPort())) {
            match = false;
          }
          if (match) {
            break;
          }
        }
      }
      if (!match) {
        return false;
      }
    }
    if (!headerSpace.getNotDstProtocols().isEmpty()) {
      boolean match = false;
      for (Protocol notDstProtocol : headerSpace.getNotDstProtocols()) {
        if (notDstProtocol.getIpProtocol().equals(_flow.getIpProtocol())) {
          match = true;
          Integer dstPort = notDstProtocol.getPort();
          if (!dstPort.equals(_flow.getDstPort())) {
            match = false;
          }
          if (match) {
            return false;
          }
        }
      }
    }
    if (!headerSpace.getFragmentOffsets().isEmpty()
        && !rangesContain(headerSpace.getFragmentOffsets(), _flow.getFragmentOffset())) {
      return false;
    }
    if (!headerSpace.getNotFragmentOffsets().isEmpty()
        && rangesContain(headerSpace.getNotFragmentOffsets(), _flow.getFragmentOffset())) {
      return false;
    }
    if (!headerSpace.getIcmpCodes().isEmpty()
        && !rangesContain(headerSpace.getIcmpCodes(), _flow.getIcmpCode())) {
      return false;
    }
    if (!headerSpace.getNotIcmpCodes().isEmpty()
        && rangesContain(headerSpace.getNotIcmpCodes(), _flow.getFragmentOffset())) {
      return false;
    }
    if (!headerSpace.getIcmpTypes().isEmpty()
        && !rangesContain(headerSpace.getIcmpTypes(), _flow.getIcmpType())) {
      return false;
    }
    if (!headerSpace.getNotIcmpTypes().isEmpty()
        && rangesContain(headerSpace.getNotIcmpTypes(), _flow.getFragmentOffset())) {
      return false;
    }
    if (!headerSpace.getIpProtocols().isEmpty()
        && !headerSpace.getIpProtocols().contains(_flow.getIpProtocol())) {
      return false;
    }
    if (!headerSpace.getNotIpProtocols().isEmpty()
        && headerSpace.getNotIpProtocols().contains(_flow.getIpProtocol())) {
      return false;
    }
    if (!headerSpace.getPacketLengths().isEmpty()
        && !rangesContain(headerSpace.getPacketLengths(), _flow.getPacketLength())) {
      return false;
    }
    if (!headerSpace.getNotPacketLengths().isEmpty()
        && rangesContain(headerSpace.getNotPacketLengths(), _flow.getPacketLength())) {
      return false;
    }
    if (headerSpace.getSrcOrDstIps() != null
        && !(traceSrcIp(headerSpace.getSrcOrDstIps(), _flow.getSrcIp())
            || traceDstIp(headerSpace.getSrcOrDstIps(), _flow.getDstIp()))) {
      return false;
    }
    if (!headerSpace.getSrcOrDstPorts().isEmpty()
        && !(rangesContain(headerSpace.getSrcOrDstPorts(), _flow.getSrcPort())
            || rangesContain(headerSpace.getSrcOrDstPorts(), _flow.getDstPort()))) {
      return false;
    }
    if (!headerSpace.getSrcOrDstProtocols().isEmpty()) {
      boolean match = false;
      for (Protocol protocol : headerSpace.getSrcOrDstProtocols()) {
        if (protocol.getIpProtocol().equals(_flow.getIpProtocol())) {
          match = true;
          Integer port = protocol.getPort();
          if (!port.equals(_flow.getDstPort()) && !port.equals(_flow.getSrcPort())) {
            match = false;
          }
          if (match) {
            break;
          }
        }
      }
      if (!match) {
        return false;
      }
    }
    if (headerSpace.getSrcIps() != null && !traceSrcIp(headerSpace.getSrcIps(), _flow.getSrcIp())) {
      return false;
    }
    if (headerSpace.getNotSrcIps() != null
        && traceSrcIp(headerSpace.getNotSrcIps(), _flow.getSrcIp())) {
      return false;
    }
    if (!headerSpace.getSrcPorts().isEmpty()
        && !rangesContain(headerSpace.getSrcPorts(), _flow.getSrcPort())) {
      return false;
    }
    if (!headerSpace.getNotSrcPorts().isEmpty()
        && rangesContain(headerSpace.getNotSrcPorts(), _flow.getSrcPort())) {
      return false;
    }
    if (!headerSpace.getSrcProtocols().isEmpty()) {
      boolean match = false;
      for (Protocol srcProtocol : headerSpace.getSrcProtocols()) {
        if (srcProtocol.getIpProtocol().equals(_flow.getIpProtocol())) {
          match = true;
          Integer srcPort = srcProtocol.getPort();
          if (!srcPort.equals(_flow.getSrcPort())) {
            match = false;
          }
          if (match) {
            break;
          }
        }
      }
      if (!match) {
        return false;
      }
    }
    if (!headerSpace.getNotSrcProtocols().isEmpty()) {
      boolean match = false;
      for (Protocol notSrcProtocol : headerSpace.getNotSrcProtocols()) {
        if (notSrcProtocol.getIpProtocol().equals(_flow.getIpProtocol())) {
          match = true;
          Integer srcPort = notSrcProtocol.getPort();
          if (!srcPort.equals(_flow.getSrcPort())) {
            match = false;
          }
          if (match) {
            return false;
          }
        }
      }
    }
    if (!headerSpace.getTcpFlags().isEmpty()
        && headerSpace.getTcpFlags().stream().noneMatch(tcpFlags -> tcpFlags.match(_flow))) {
      return false;
    }
    return true;
  }

  private boolean trace(@Nonnull IpAccessList ipAccessList) {
    List<AclLine> lines = ipAccessList.getLines();
    for (int i = 0; i < lines.size(); i++) {
      _tracer.newSubTrace();
      AclLine line = lines.get(i);
      LineAction action = visit(line);
      if (action != null) {
        recordAction(ipAccessList, i, line, action);
        _tracer.endSubTrace();
        return action == LineAction.PERMIT;
      }
      // All previous children are of no interest since they resulted in a no-match on previous line
      _tracer.discardSubTrace();
    }

    _tracer.newSubTrace();
    recordDefaultDeny(ipAccessList);
    _tracer.endSubTrace();
    return false;
  }

  public boolean trace(@Nonnull IpSpace ipSpace, @Nonnull Ip ip, @Nonnull String ipDescription) {
    return ipSpace.accept(
        new IpSpaceTracer(
            _tracer, ip, ipDescription, _ipSpaceNames, _ipSpaceMetadata, _namedIpSpaces));
  }

  private boolean traceDstIp(@Nonnull IpSpace ipSpace, @Nonnull Ip ip) {
    return ipSpace.accept(
        new IpSpaceTracer(
            _tracer, ip, "destination IP", _ipSpaceNames, _ipSpaceMetadata, _namedIpSpaces));
  }

  private boolean traceSrcIp(@Nonnull IpSpace ipSpace, @Nonnull Ip ip) {
    return ipSpace.accept(
        new IpSpaceTracer(
            _tracer, ip, "source IP", _ipSpaceNames, _ipSpaceMetadata, _namedIpSpaces));
  }

  @Override
  public Boolean visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    return trace(matchHeaderSpace.getHeaderspace());
  }

  @Override
  public Boolean visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    return trace(_availableAcls.get(permittedByAcl.getAclName()));
  }

  @Override
  public Boolean visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return andMatchExpr.getConjuncts().stream()
        .allMatch(
            c -> {
              _tracer.newSubTrace();
              Boolean result = c.accept(this);
              _tracer.endSubTrace();
              return result;
            });
  }

  @Override
  public Boolean visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return orMatchExpr.getDisjuncts().stream()
        .anyMatch(
            d -> {
              _tracer.newSubTrace();
              Boolean result = d.accept(this);
              _tracer.endSubTrace();
              return result;
            });
  }
}
