package org.batfish.z3.expr;

public interface GenericStatementVisitor<T> {
  T visitBasicRuleStatement(BasicRuleStatement basicRuleStatement);

  T visitComment(Comment comment);

  T visitQueryStatement(QueryStatement queryStatement);
}
