package org.batfish.vendor.check_point_management;

import static org.batfish.datamodel.IntegerSpace.PORTS;
import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.IpProtocol.UDP;
import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.applications.PortsApplication.MAX_PORT_NUMBER;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.vendor.check_point_management.parsing.parboiled.BooleanExprAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.BooleanExprAstNodeVisitor;
import org.batfish.vendor.check_point_management.parsing.parboiled.ComparatorAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.ComparatorAstNodeVisitor;
import org.batfish.vendor.check_point_management.parsing.parboiled.ConjunctionAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.DisjunctionAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.DportAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.EmptyAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.EqualsAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.ErrorAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.GreaterThanAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.GreaterThanOrEqualsAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.HasInspectText;
import org.batfish.vendor.check_point_management.parsing.parboiled.IncomingAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.LessThanAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.LessThanOrEqualsAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.OutgoingAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.TcpAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.UdpAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.UhDportAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.UnhandledAstNode;

/** Converter from {@link BooleanExprAstNode} to {@link AclLineMatchExpr}. */
public final class BooleanExprAstNodeToAclLineMatchExpr
    implements BooleanExprAstNodeVisitor<AclLineMatchExpr, Boolean> {

  /**
   * Converts the {@code booleanExprAstNode} to an {@link AclLineMatchExpr}.
   *
   * <p>Matches on unsupported features (e.g. L7, firewall state) are converted directly to the
   * value of {@code permitUnsupported}. Matches on incoming/outgoing direction are currently
   * treated as unsupported features.
   */
  public static @Nonnull AclLineMatchExpr convert(
      BooleanExprAstNode booleanExprAstNode, boolean permitUnsupported) {
    // TODO: Support direction as follows:
    //       Matches on incoming direction are converted to {@code true} iff {@code incoming}.
    //       Matches on outgoing direction are converted to {@code false} iff not {@code incoming}.
    return INSTANCE.visit(booleanExprAstNode, permitUnsupported);
  }

  @Override
  public @Nonnull AclLineMatchExpr visitConjunctionAstNode(
      ConjunctionAstNode conjunctionAstNode, Boolean permitUnsupported) {
    return and(
        conjunctionAstNode.getConjuncts().stream()
            .map(conjunct -> visit(conjunct, permitUnsupported))
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public @Nonnull AclLineMatchExpr visitDisjunctionAstNode(
      DisjunctionAstNode disjunctionAstNode, Boolean permitUnsupported) {
    return or(
        disjunctionAstNode.getDisjuncts().stream()
            .map(disjunct -> visit(disjunct, permitUnsupported))
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public @Nonnull AclLineMatchExpr visitDportAstNode(
      DportAstNode dportAstNode, Boolean permitUnsupported) {
    return and(
        inspectTraceElement(dportAstNode),
        matchDstPort(
            portRangeToIntegerSpace(
                dportAstNode.getComparator(), dportAstNode.getValue().getValue())));
  }

  @Override
  public @Nonnull AclLineMatchExpr visitEmptyAstNode(
      EmptyAstNode emptyAstNode, Boolean permitUnsupported) {
    return TRUE;
  }

  @Override
  public @Nonnull AclLineMatchExpr visitErrorAstNode(
      ErrorAstNode errorAstNode, Boolean permitUnsupported) {
    return FALSE;
  }

  @Override
  public @Nonnull AclLineMatchExpr visitIncomingAstNode(
      IncomingAstNode incomingAstNode, Boolean permitUnsupported) {
    // TODO: implement direction
    return permitUnsupported ? new TrueExpr(unhandledInspectTraceElement(incomingAstNode)) : FALSE;
  }

  @Override
  public @Nonnull AclLineMatchExpr visitOutgoingAstNode(
      OutgoingAstNode outgoingAstNode, Boolean permitUnsupported) {
    // TODO: implement direction
    return permitUnsupported ? new TrueExpr(unhandledInspectTraceElement(outgoingAstNode)) : FALSE;
  }

  @Override
  public @Nonnull AclLineMatchExpr visitTcpAstNode(
      TcpAstNode tcpAstNode, Boolean permitUnsupported) {
    return and(inspectTraceElement(tcpAstNode.getInspectText()), matchIpProtocol(TCP));
  }

  @Override
  public @Nonnull AclLineMatchExpr visitUdpAstNode(
      UdpAstNode udpAstNode, Boolean permitUnsupported) {
    return and(inspectTraceElement(udpAstNode.getInspectText()), matchIpProtocol(UDP));
  }

  @Override
  public @Nonnull AclLineMatchExpr visitUhDportAstNode(
      UhDportAstNode uhDportAstNode, Boolean permitUnsupported) {
    return and(
        inspectTraceElement(uhDportAstNode),
        matchIpProtocol(UDP),
        matchDstPort(
            portRangeToIntegerSpace(
                uhDportAstNode.getComparator(), uhDportAstNode.getValue().getValue())));
  }

  @Override
  public @Nonnull AclLineMatchExpr visitUnhandledAstNode(
      UnhandledAstNode unhandledAstNode, Boolean permitUnsupported) {
    return permitUnsupported ? new TrueExpr(unhandledInspectTraceElement(unhandledAstNode)) : FALSE;
  }

  /** Convert a port range represented by a comparator and a value to an {@link IntegerSpace}. */
  @VisibleForTesting
  static @Nonnull IntegerSpace portRangeToIntegerSpace(
      ComparatorAstNode comparatorAstNode, int value) {
    return COMPARATOR_AND_VALUE_TO_INTEGER_SPACE
        .visit(comparatorAstNode, value)
        .intersection(PORTS);
  }

  private static final ComparatorAndValueToIntegerSpace COMPARATOR_AND_VALUE_TO_INTEGER_SPACE =
      new ComparatorAndValueToIntegerSpace();

  private static final class ComparatorAndValueToIntegerSpace
      implements ComparatorAstNodeVisitor<IntegerSpace, Integer> {

    @Override
    public @Nonnull IntegerSpace visitEqualsAstNode(EqualsAstNode equalsAstNode, Integer arg) {
      return IntegerSpace.of(arg);
    }

    @Override
    public @Nonnull IntegerSpace visitGreaterThanAstNode(
        GreaterThanAstNode greaterThanAstNode, Integer arg) {
      return IntegerSpace.of(new SubRange(arg + 1, MAX_PORT_NUMBER));
    }

    @Override
    public @Nonnull IntegerSpace visitGreaterThanOrEqualsAstNode(
        GreaterThanOrEqualsAstNode greaterThanOrEqualsAstNode, Integer arg) {
      return IntegerSpace.of(new SubRange(arg, MAX_PORT_NUMBER));
    }

    @Override
    public @Nonnull IntegerSpace visitLessThanAstNode(
        LessThanAstNode lessThanAstNode, Integer arg) {
      return IntegerSpace.of(new SubRange(0, arg - 1));
    }

    @Override
    public @Nonnull IntegerSpace visitLessThanOrEqualsAstNode(
        LessThanOrEqualsAstNode lessThanOrEqualsAstNode, Integer arg) {
      return IntegerSpace.of(new SubRange(0, arg));
    }
  }

  private static @Nonnull TraceElement inspectTraceElement(HasInspectText hasInspectText) {
    return inspectTraceElement(hasInspectText.getInspectText());
  }

  @VisibleForTesting
  static @Nonnull TraceElement inspectTraceElement(String inspectText) {
    return TraceElement.of(String.format("Matched INSPECT expression '%s'", inspectText));
  }

  private static @Nonnull TraceElement unhandledInspectTraceElement(HasInspectText hasInspectText) {
    return unhandledInspectTraceElement(hasInspectText.getInspectText());
  }

  @VisibleForTesting
  static @Nonnull TraceElement unhandledInspectTraceElement(String inspectText) {
    return TraceElement.of(
        String.format("Assumed matched unsupported INSPECT expression '%s'", inspectText));
  }

  private static final BooleanExprAstNodeToAclLineMatchExpr INSTANCE =
      new BooleanExprAstNodeToAclLineMatchExpr();

  private BooleanExprAstNodeToAclLineMatchExpr() {}
}
