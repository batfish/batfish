package org.batfish.z3.expr;

public interface VoidStatementVisitor {

  void visitComment(Comment comment);

  void visitRuleStatement(RuleStatement ruleStatement);

  void visitQueryStatement(QueryStatement queryStatement);

  void visitDeclareRelStatement(DeclareRelStatement declareRelStatement);

  void visitDeclareVarStatement(DeclareVarStatement declareVarStatement);
}
