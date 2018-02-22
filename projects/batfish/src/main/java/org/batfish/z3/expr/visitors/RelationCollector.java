package org.batfish.z3.expr.visitors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.expr.Comment;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.StateExpr.State;
import org.batfish.z3.expr.Statement;
import org.batfish.z3.expr.TransformationRuleStatement;
import org.batfish.z3.expr.TransformationStateExpr;
import org.batfish.z3.expr.TransformedBasicRuleStatement;
import org.batfish.z3.expr.VoidStatementVisitor;

public class RelationCollector implements VoidStateExprVisitor, VoidStatementVisitor {

  public static Map<String, State> collectRelations(SynthesizerInput input, Statement statement) {
    RelationCollector relationCollector = new RelationCollector(input);
    statement.accept(relationCollector);
    return relationCollector
        ._relations
        .build()
        .stream()
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  private final SynthesizerInput _input;

  private final ImmutableSet.Builder<Entry<String, State>> _relations;

  private RelationCollector(SynthesizerInput input) {
    _input = input;
    _relations = ImmutableSet.builder();
  }

  @Override
  public void visitBasicRuleStatement(BasicRuleStatement basicRuleStatement) {
    basicRuleStatement.getPreconditionStates().forEach(s -> s.accept(this));
    basicRuleStatement.getPostconditionState().accept(this);
  }

  @Override
  public void visitBasicStateExpr(BasicStateExpr basicStateExpr) {
    visitStateExpr(basicStateExpr);
  }

  @Override
  public void visitComment(Comment comment) {}

  @Override
  public void visitQueryStatement(QueryStatement queryStatement) {
    queryStatement.getSubExpression().accept(this);
  }

  private void visitStateExpr(StateExpr stateExpr) {
    _relations.add(
        Maps.immutableEntry(
            BoolExprTransformer.getNodName(_input, stateExpr), stateExpr.getState()));
  }

  @Override
  public void visitTransformationRuleStatement(
      TransformationRuleStatement transformationRuleStatement) {
    transformationRuleStatement
        .getPreconditionPreTransformationStates()
        .forEach(s -> s.accept(this));
    transformationRuleStatement
        .getPreconditionPostTransformationStates()
        .forEach(s -> s.accept(this));
    transformationRuleStatement.getPreconditionTransformationStates().forEach(s -> s.accept(this));
    transformationRuleStatement.getPostconditionTransformationState().accept(this);
  }

  @Override
  public void visitTransformationStateExpr(TransformationStateExpr transformationStateExpr) {
    visitStateExpr(transformationStateExpr);
  }

  @Override
  public void visitTransformedBasicRuleStatement(
      TransformedBasicRuleStatement transformedBasicRuleStatement) {
    transformedBasicRuleStatement
        .getPreconditionPreTransformationStates()
        .forEach(s -> s.accept(this));
    transformedBasicRuleStatement
        .getPreconditionPostTransformationStates()
        .forEach(s -> s.accept(this));
    transformedBasicRuleStatement
        .getPreconditionTransformationStates()
        .forEach(s -> s.accept(this));
    transformedBasicRuleStatement.getPostconditionPostTransformationState().accept(this);
  }
}
