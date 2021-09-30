package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A visitor of {@link BooleanExprAstNode} that takes a generic argument and returns a generic
 * value.
 */
@ParametersAreNonnullByDefault
public interface BooleanExprAstNodeVisitor<T, U> {

  default T visit(BooleanExprAstNode booleanExprAstNode, U arg) {
    return booleanExprAstNode.accept(this, arg);
  }

  T visitConjunctionAstNode(ConjunctionAstNode conjunctionAstNode, U arg);

  T visitDisjunctionAstNode(DisjunctionAstNode disjunctionAstNode, U arg);

  T visitDportAstNode(DportAstNode dportAstNode, U arg);

  T visitEmptyAstNode(EmptyAstNode emptyAstNode, U arg);

  T visitErrorAstNode(ErrorAstNode errorAstNode, U arg);

  T visitIncomingAstNode(IncomingAstNode incomingAstNode, U arg);

  T visitOutgoingAstNode(OutgoingAstNode outgoingAstNode, U arg);

  T visitTcpAstNode(TcpAstNode tcpAstNode, U arg);

  T visitUdpAstNode(UdpAstNode udpAstNode, U arg);

  T visitUhDportAstNode(UhDportAstNode uhDportAstNode, U arg);

  T visitUnhandledAstNode(UnhandledAstNode unhandledAstNode, U arg);
}
