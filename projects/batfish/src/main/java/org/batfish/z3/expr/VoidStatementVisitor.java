package org.batfish.z3.expr;

public interface VoidStatementVisitor {

  void visitBasicRuleStatement(BasicRuleStatement basicRuleStatement);

  void visitComment(Comment comment);

  void visitQueryStatement(QueryStatement queryStatement);
}
