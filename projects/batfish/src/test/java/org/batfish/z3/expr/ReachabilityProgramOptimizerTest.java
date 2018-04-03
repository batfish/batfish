package org.batfish.z3.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.z3.expr.visitors.RelationCollectorTest.TestStateExpr;
import org.junit.Before;
import org.junit.Test;

public class ReachabilityProgramOptimizerTest {
  private int _stateId;
  private List<RuleStatement> _rules;
  private List<QueryStatement> _queries;

  @Before
  public void init() {
    _stateId = 0;
    _rules = new ArrayList<>();
    _queries = new ArrayList<>();
  }

  private StateExpr freshStateExpr() {
    return new TestStateExpr(_stateId++);
  }

  private StateExpr freshQueryStateExpr() {
    StateExpr stateExpr = freshStateExpr();
    _queries.add(new QueryStatement(stateExpr));
    return stateExpr;
  }

  private RuleStatement addRuleFor(StateExpr postState, StateExpr... preStates) {
    RuleStatement rule =
        new BasicRuleStatement(Arrays.stream(preStates).collect(Collectors.toSet()), postState);
    _rules.add(rule);
    return rule;
  }

  private Set<RuleStatement> optimize() {
    Set<RuleStatement> rules = ReachabilityProgramOptimizer.optimize(_rules, _queries);
    return rules;
  }

  @Test
  public void testStraightLineNoPruning() {
    StateExpr initialState = freshStateExpr();
    RuleStatement axiom = addRuleFor(initialState);
    StateExpr queryState = freshQueryStateExpr();
    RuleStatement rule = addRuleFor(queryState, initialState);
    Set<RuleStatement> rules = optimize();
    assertThat(rules, hasSize(2));
    assertThat(rules, hasItems(axiom, rule));
  }

  @Test
  public void pruneUnreachable() {
    StateExpr state1 = freshStateExpr();
    StateExpr state2 = freshQueryStateExpr();
    addRuleFor(state2, state1);
    Set<RuleStatement> rules = optimize();
    assertThat(rules, empty());
  }

  /*
   * No query statement, so everything is irrelevant
   */
  @Test
  public void pruneIrrelevant() {
    StateExpr state1 = freshStateExpr();
    addRuleFor(state1);
    StateExpr state2 = freshStateExpr();
    addRuleFor(state2, state1);
    Set<RuleStatement> rules = optimize();
    assertThat(rules, empty());
  }

  @Test
  public void pruneStateWithMissingDependency() {
    StateExpr state1 = freshStateExpr();
    StateExpr state2 = freshStateExpr();
    StateExpr state3 = freshStateExpr();
    StateExpr goal = freshQueryStateExpr();

    RuleStatement state1Axiom = addRuleFor(state1);
    RuleStatement state3Rule = addRuleFor(state3, state1, state2);
    RuleStatement goalState1Rule = addRuleFor(goal, state1);
    RuleStatement goalState3Rule = addRuleFor(goal, state3);

    Set<RuleStatement> rules = optimize();

    // all rules involving state 3 have been removed
    assertThat(rules, not(hasItem(state3Rule)));
    assertThat(rules, not(hasItem(goalState3Rule)));

    // all other rules remain
    assertThat(rules, hasItems(goalState1Rule, state1Axiom));
  }

  /*
   * Round 1 should remove the rule for state2 because state3 is underivable
   * Round 2 should remove the axiom for state1 because it's irrelevant (it can only lead to
   * the goal through state2).
   */
  @Test
  public void pruneStateWithMissingDependency2() {
    StateExpr state1 = freshStateExpr();
    StateExpr state2 = freshStateExpr();
    StateExpr state3 = freshStateExpr();
    StateExpr goal = freshQueryStateExpr();

    addRuleFor(state1);
    addRuleFor(state2, state1, state3);
    addRuleFor(goal, state2);

    Set<RuleStatement> rules = optimize();

    // all rules involving state 3 have been removed
    assertThat(rules, empty());
  }

  @Test
  public void keepRulesThatBecomeUsableAfterVisitingDerivingState() {
    StateExpr init1 = freshStateExpr();
    StateExpr init2 = freshStateExpr();
    StateExpr a = freshStateExpr();
    StateExpr b = freshStateExpr();
    StateExpr c = freshStateExpr();
    StateExpr goal = freshQueryStateExpr();

    addRuleFor(init1);
    addRuleFor(init2);
    addRuleFor(a, init1);
    addRuleFor(b, init2);
    addRuleFor(c, b);
    addRuleFor(a, c);
    addRuleFor(goal, a);

    Set<RuleStatement> rules = optimize();

    // no rules should be removed
    assertThat(rules, hasSize(7));
  }
}
