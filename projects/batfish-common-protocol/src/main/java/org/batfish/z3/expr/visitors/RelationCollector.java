package org.batfish.z3.expr.visitors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.Comment;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.StateExpr.State;
import org.batfish.z3.expr.Statement;
import org.batfish.z3.expr.VoidStatementVisitor;

public class RelationCollector implements VoidStatementVisitor {

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
    basicRuleStatement.getPreconditionStates().forEach(this::collectStateExpr);
    collectStateExpr(basicRuleStatement.getPostconditionState());
  }

  @Override
  public void visitComment(Comment comment) {}

  @Override
  public void visitQueryStatement(QueryStatement queryStatement) {
    collectStateExpr(queryStatement.getStateExpr());
  }

  private void collectStateExpr(StateExpr stateExpr) {
    _relations.add(
        Maps.immutableEntry(
            BoolExprTransformer.getNodName(_input, stateExpr), stateExpr.getState()));
  }
}
