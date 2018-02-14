package org.batfish.z3.expr;

public interface GenericStatementVisitor<T> {
  T visitComment(Comment comment);

  T visitRuleStatement(RuleStatement ruleStatement);

  T visitQueryStatement(QueryStatement queryStatement);

  T visitDeclareRelStatement(DeclareRelStatement declareRelStatement);

  T visitDeclareVarStatement(DeclareVarStatement declareVarStatement);
}
