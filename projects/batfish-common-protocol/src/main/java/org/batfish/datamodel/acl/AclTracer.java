package org.batfish.datamodel.acl;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.common.util.CommonUtil.rangesContain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.visitors.IpSpaceTracer;

/**
 * Tracer for {@link IpAccessList}, {@link IpSpace}, {@link HeaderSpace}.<br>
 * Acts like {@link Evaluator} on {@link IpAccessList}, except that it introduces tracing when
 * encountering traceable classes.
 */
public final class AclTracer extends Evaluator {

  public static AclTrace trace(
      IpAccessList ipAccessList,
      Flow flow,
      String srcInterface,
      Map<String, IpAccessList> availableAcls,
      Map<String, IpSpace> namedIpSpaces) {
    AclTracer tracer = new AclTracer(flow, srcInterface, availableAcls, namedIpSpaces);
    tracer.trace(ipAccessList);
    return tracer.getTrace();
  }

  private final Map<IpSpace, String> _ipSpaceNames;

  private Builder<TraceEvent> _traceEvents;

  public AclTracer(
      @Nonnull Flow flow,
      @Nullable String srcInterface,
      @Nonnull Map<String, IpAccessList> availableAcls,
      @Nonnull Map<String, IpSpace> namedIpSpaces) {
    super(flow, srcInterface, availableAcls, namedIpSpaces);
    _ipSpaceNames = new IdentityHashMap<>();
    _traceEvents = ImmutableList.builder();
    namedIpSpaces.forEach((name, ipSpace) -> _ipSpaceNames.put(ipSpace, name));
  }

  public Flow getFlow() {
    return _flow;
  }

  public Map<IpSpace, String> getIpSpaceNames() {
    return _ipSpaceNames;
  }

  public Map<String, IpSpace> getNamedIpSpaces() {
    return _namedIpSpaces;
  }

  public AclTrace getTrace() {
    return new AclTrace(_traceEvents.build());
  }

  public void recordAction(IpAccessList ipAccessList, int index, IpAccessListLine line) {
    String description = firstNonNull(line.getName(), line.toString());
    if (line.getAction() == LineAction.ACCEPT) {
      _traceEvents.add(new PermittedByIpAccessListLine(ipAccessList.getName(), index, description));
    } else {
      _traceEvents.add(new DeniedByIpAccessListLine(ipAccessList.getName(), index, description));
    }
  }

  public void recordAction(String aclIpSpaceName, int index, AclIpSpaceLine line) {
    if (line.getAction() == LineAction.ACCEPT) {
      _traceEvents.add(
          new PermittedByAclIpSpaceLine(aclIpSpaceName, index, line.getIpSpace().toString()));
    } else {
      _traceEvents.add(
          new DeniedByAclIpSpaceLine(aclIpSpaceName, index, line.getIpSpace().toString()));
    }
  }

  public void recordDefaultDeny(IpAccessList ipAccessList) {
    _traceEvents.add(new DefaultDeniedByIpAccessList(ipAccessList.getName()));
  }

  public void recordDefaultDeny(String aclIpSpaceName) {
    _traceEvents.add(new DefaultDeniedByAclIpSpace(aclIpSpaceName));
  }

  public void recordNamedIpSpaceAction(String name, String description, boolean permit) {
    if (permit) {
      _traceEvents.add(new PermittedByNamedIpSpace(name, description));
    } else {
      _traceEvents.add(new DeniedByNamedIpSpace(name, description));
    }
  }

  private boolean trace(HeaderSpace headerSpace) {
    if (!headerSpace.getDscps().isEmpty() && !headerSpace.getDscps().contains(_flow.getDscp())) {
      return false;
    }
    if (!headerSpace.getNotDscps().isEmpty()
        && headerSpace.getNotDscps().contains(_flow.getDscp())) {
      return false;
    }
    if (headerSpace.getDstIps() != null && !trace(headerSpace.getDstIps(), _flow.getDstIp())) {
      return false;
    }
    if (headerSpace.getNotDstIps() != null && trace(headerSpace.getNotDstIps(), _flow.getDstIp())) {
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
          if (dstPort != null && !dstPort.equals(_flow.getDstPort())) {
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
          if (dstPort != null && !dstPort.equals(_flow.getDstPort())) {
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
        && !(trace(headerSpace.getSrcOrDstIps(), _flow.getSrcIp())
            || trace(headerSpace.getSrcOrDstIps(), _flow.getDstIp()))) {
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
          if (port != null
              && !port.equals(_flow.getDstPort())
              && !port.equals(_flow.getSrcPort())) {
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
    if (headerSpace.getSrcIps() != null && !trace(headerSpace.getSrcIps(), _flow.getSrcIp())) {
      return false;
    }
    if (headerSpace.getNotSrcIps() != null && trace(headerSpace.getNotSrcIps(), _flow.getSrcIp())) {
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
          if (srcPort != null && !srcPort.equals(_flow.getSrcPort())) {
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
          if (srcPort != null && !srcPort.equals(_flow.getSrcPort())) {
            match = false;
          }
          if (match) {
            return false;
          }
        }
      }
    }
    if (!headerSpace.getStates().isEmpty() && !headerSpace.getStates().contains(_flow.getState())) {
      return false;
    }
    if (!headerSpace.getTcpFlags().isEmpty()
        && !headerSpace.getTcpFlags().stream().anyMatch(tcpFlags -> tcpFlags.match(_flow))) {
      return false;
    }
    return true;
  }

  private boolean trace(IpAccessList ipAccessList) {
    List<IpAccessListLine> lines = ipAccessList.getLines();
    for (int i = 0; i < lines.size(); i++) {
      IpAccessListLine line = lines.get(i);
      if (line.getMatchCondition().accept(this)) {
        recordAction(ipAccessList, i, line);
        return line.getAction() == LineAction.ACCEPT;
      }
    }
    recordDefaultDeny(ipAccessList);
    return false;
  }

  public boolean trace(IpSpace ipSpace, Ip ip) {
    return ipSpace.accept(new IpSpaceTracer(this, ip));
  }

  @Override
  public Boolean visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    return trace(matchHeaderSpace.getHeaderspace());
  }

  @Override
  public Boolean visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    return trace(_availableAcls.get(permittedByAcl.getAclName()));
  }
}
