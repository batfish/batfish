package org.batfish.datamodel.acl;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.datamodel.trace.Tracer;
import org.batfish.datamodel.visitors.IpSpaceTracer;

/**
 * Tracer for {@link IpAccessList}, {@link IpSpace}, {@link HeaderSpace}.<br>
 * Acts like {@link Evaluator} on {@link IpAccessList}, except that it introduces tracing when
 * encountering traceable classes.
 */
public final class AclTracer extends AclLineEvaluator {
  @VisibleForTesting static String DEST_IP_DESCRIPTION = "destination IP";

  @VisibleForTesting static String SRC_IP_DESCRIPTION = "source IP";

  public static List<TraceTree> trace(
      @Nonnull IpAccessList ipAccessList,
      @Nonnull Flow flow,
      @Nullable String srcInterface,
      @Nonnull Map<String, IpAccessList> availableAcls,
      @Nonnull Map<String, IpSpace> namedIpSpaces,
      @Nonnull Map<String, IpSpaceMetadata> namedIpSpaceMetadata) {
    AclTracer tracer =
        new AclTracer(flow, srcInterface, availableAcls, namedIpSpaces, namedIpSpaceMetadata);
    tracer._tracer.newSubTrace();
    tracer.trace(ipAccessList);
    tracer._tracer.endSubTrace();
    return tracer.getTrace();
  }

  @VisibleForTesting
  public static List<TraceTree> trace(
      @Nonnull AclLineMatchExpr expr,
      @Nonnull Flow flow,
      @Nullable String srcInterface,
      @Nonnull Map<String, IpAccessList> availableAcls,
      @Nonnull Map<String, IpSpace> namedIpSpaces,
      @Nonnull Map<String, IpSpaceMetadata> namedIpSpaceMetadata) {
    AclTracer tracer =
        new AclTracer(flow, srcInterface, availableAcls, namedIpSpaces, namedIpSpaceMetadata);
    tracer._tracer.newSubTrace();
    if (!tracer.visit(expr)) {
      tracer._tracer.discardSubTrace();
      tracer._tracer.newSubTrace();
    }
    tracer._tracer.endSubTrace();
    return tracer.getTrace();
  }

  private final @Nonnull Map<String, IpSpaceMetadata> _ipSpaceMetadata;

  private final @Nonnull Tracer _tracer;
  private final @Nonnull IpSpaceTracer _dstIpTracer;
  private final @Nonnull IpSpaceTracer _srcIpTracer;

  public AclTracer(
      @Nonnull Flow flow,
      @Nullable String srcInterface,
      @Nonnull Map<String, IpAccessList> availableAcls,
      @Nonnull Map<String, IpSpace> namedIpSpaces,
      @Nonnull Map<String, IpSpaceMetadata> namedIpSpaceMetadata) {
    super(flow, srcInterface, availableAcls, namedIpSpaces);
    _ipSpaceMetadata = namedIpSpaceMetadata;
    _tracer = new Tracer();
    _dstIpTracer =
        new IpSpaceTracer(
            _tracer, flow.getDstIp(), DEST_IP_DESCRIPTION, _ipSpaceMetadata, _namedIpSpaces);
    _srcIpTracer =
        new IpSpaceTracer(
            _tracer, flow.getSrcIp(), SRC_IP_DESCRIPTION, _ipSpaceMetadata, _namedIpSpaces);
  }

  public @Nonnull List<TraceTree> getTrace() {
    return _tracer.getTrace();
  }

  private void setTraceElement(@Nonnull IpAccessList ipAccessList, int index) {
    AclLine line = ipAccessList.getLines().get(index);
    TraceElement traceElement = line.getTraceElement();
    if (traceElement != null) {
      _tracer.setTraceElement(traceElement);
    }
  }

  private void setTraceElement(@Nullable TraceElement traceElement) {
    if (traceElement == null) {
      return;
    }
    _tracer.setTraceElement(traceElement);
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
    if (headerSpace.getDstIps() != null && !traceDstIp(headerSpace.getDstIps())) {
      return false;
    }
    if (headerSpace.getNotDstIps() != null && traceDstIp(headerSpace.getNotDstIps())) {
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
        && !(traceSrcIp(headerSpace.getSrcOrDstIps())
            || traceDstIp(headerSpace.getSrcOrDstIps()))) {
      return false;
    }
    if (!headerSpace.getSrcOrDstPorts().isEmpty()
        && !(rangesContain(headerSpace.getSrcOrDstPorts(), _flow.getSrcPort())
            || rangesContain(headerSpace.getSrcOrDstPorts(), _flow.getDstPort()))) {
      return false;
    }
    if (headerSpace.getSrcIps() != null && !traceSrcIp(headerSpace.getSrcIps())) {
      return false;
    }
    if (headerSpace.getNotSrcIps() != null && traceSrcIp(headerSpace.getNotSrcIps())) {
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
    if (!headerSpace.getTcpFlags().isEmpty()
        && headerSpace.getTcpFlags().stream().noneMatch(tcpFlags -> tcpFlags.match(_flow))) {
      return false;
    }
    return true;
  }

  private LineAction trace(@Nonnull IpAccessList ipAccessList) {
    List<AclLine> lines = ipAccessList.getLines();
    for (int i = 0; i < lines.size(); i++) {
      _tracer.newSubTrace();
      AclLine line = lines.get(i);
      LineAction action = visit(line);
      if (action != null) {
        setTraceElement(ipAccessList, i);
        _tracer.endSubTrace();
        return action;
      }
      // All previous children are of no interest since they resulted in a no-match on previous line
      _tracer.discardSubTrace();
    }

    return null;
  }

  private boolean traceDstIp(@Nonnull IpSpace ipSpace) {
    return ipSpace.accept(_dstIpTracer);
  }

  private boolean traceSrcIp(@Nonnull IpSpace ipSpace) {
    return ipSpace.accept(_srcIpTracer);
  }

  @Override
  public LineAction visitAclAclLine(AclAclLine aclAclLine) {
    IpAccessList referencedAcl =
        checkNotNull(
            _availableAcls.get(aclAclLine.getAclName()),
            "Reference to undefined IpAccessList %s",
            aclAclLine.getAclName());
    return trace(referencedAcl);
  }

  @Override
  public LineAction visitExprAclLine(ExprAclLine exprAclLine) {
    // current context is for the line; create a context for the top-level expression
    _tracer.newSubTrace();
    if (visit(exprAclLine.getMatchCondition())) {
      _tracer.endSubTrace();
      return exprAclLine.getAction();
    }
    _tracer.discardSubTrace();
    return null;
  }

  @Override
  public Boolean visitMatchDestinationIp(MatchDestinationIp matchDestinationIp) {
    setTraceElement(matchDestinationIp.getTraceElement());
    return traceDstIp(matchDestinationIp.getIps());
  }

  @Override
  public Boolean visitMatchDestinationPort(MatchDestinationPort matchDestinationPort) {
    setTraceElement(matchDestinationPort.getTraceElement());
    return _flow.getDstPort() != null
        && matchDestinationPort.getPorts().contains(_flow.getDstPort());
  }

  @Override
  public Boolean visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    setTraceElement(matchHeaderSpace.getTraceElement());
    return trace(matchHeaderSpace.getHeaderspace());
  }

  @Override
  public Boolean visitMatchSourceIp(MatchSourceIp matchSourceIp) {
    setTraceElement(matchSourceIp.getTraceElement());
    return traceSrcIp(matchSourceIp.getIps());
  }

  @Override
  public Boolean visitMatchSourcePort(MatchSourcePort matchSourcePort) {
    setTraceElement(matchSourcePort.getTraceElement());
    return _flow.getSrcPort() != null && matchSourcePort.getPorts().contains(_flow.getSrcPort());
  }

  @Override
  public Boolean visitTrueExpr(TrueExpr trueExpr) {
    setTraceElement(trueExpr.getTraceElement());
    return true;
  }

  @Override
  public Boolean visitFalseExpr(FalseExpr falseExpr) {
    setTraceElement(falseExpr.getTraceElement());
    return false;
  }

  @Override
  public Boolean visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    setTraceElement(permittedByAcl.getTraceElement());
    return trace(_availableAcls.get(permittedByAcl.getAclName())) == LineAction.PERMIT;
  }

  @Override
  public Boolean visitDeniedByAcl(DeniedByAcl deniedByAcl) {
    setTraceElement(deniedByAcl.getTraceElement());
    return firstNonNull(trace(_availableAcls.get(deniedByAcl.getAclName())), LineAction.DENY)
        == LineAction.DENY;
  }

  @Override
  public Boolean visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    setTraceElement(andMatchExpr.getTraceElement());
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
    setTraceElement(orMatchExpr.getTraceElement());
    return orMatchExpr.getDisjuncts().stream()
        .anyMatch(
            d -> {
              _tracer.newSubTrace();
              Boolean result = d.accept(this);
              if (result) {
                _tracer.endSubTrace();
              } else {
                _tracer.discardSubTrace();
              }
              return result;
            });
  }

  @Override
  public Boolean visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    setTraceElement(notMatchExpr.getTraceElement());
    _tracer.newSubTrace();
    boolean result = visit(notMatchExpr.getOperand());
    // TODO: how should we handle explaining "did not match"?
    // Preserving the sub-trace for a BooleanExpr that returned false is misleading.
    _tracer.discardSubTrace();
    return !result;
  }

  @Override
  public Boolean visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    setTraceElement(matchSrcInterface.getTraceElement());
    return super.visitMatchSrcInterface(matchSrcInterface);
  }
}
