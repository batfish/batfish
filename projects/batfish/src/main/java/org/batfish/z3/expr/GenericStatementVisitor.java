package org.batfish.z3.expr;

public interface GenericStatementVisitor<T> {
  T visitComment(Comment comment);

  T visitDeclareRelStatement(DeclareRelStatement declareRelStatement);

  T visitDeclareVarStatement(DeclareVarStatement declareVarStatement);

  T visitQueryStatement(QueryStatement queryStatement);

  T visitRuleStatement(RuleStatement ruleStatement);
}
