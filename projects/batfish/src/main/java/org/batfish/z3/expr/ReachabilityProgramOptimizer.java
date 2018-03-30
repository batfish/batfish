package org.batfish.z3.expr;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Remove states that can't possibly be derived from the origination states. (i.e. if there is no
 * chain of rules from an OriginateVrf to the state)
 *
 * <p>Remove states that are irrelevant to the query (i.e. if there is no sequence of rules that
 * leads from them to a query state
 */
public class ReachabilityProgramOptimizer {

  public static Set<RuleStatement> optimize(
      List<RuleStatement> rules, List<QueryStatement> queries) {
    return new ReachabilityProgramOptimizer(rules, queries).getOptimizedRules();
  }

  private final List<StateExpr> _queryStates;
  private Map<StateExpr, Set<RuleStatement>> _derivingRules;
  private Map<StateExpr, Set<RuleStatement>> _dependentRules;
  private Set<RuleStatement> _rules;

  private ReachabilityProgramOptimizer(List<RuleStatement> rules, List<QueryStatement> queries) {
    _rules = new HashSet<>(rules);
    _derivingRules = new HashMap<>();
    _dependentRules = new HashMap<>();
    _queryStates = queries.stream().map(QueryStatement::getStateExpr).collect(Collectors.toList());

    initRound();
    computeFixpoint();
  }

  private void initRound() {
    _dependentRules.clear();
    _derivingRules.clear();

    _rules.forEach(
        rule -> {
          _derivingRules
              .computeIfAbsent(rule.getPostconditionState(), s -> new HashSet<>())
              .add(rule);
          rule.getPreconditionStates()
              .forEach(
                  stateExpr ->
                      _dependentRules.computeIfAbsent(stateExpr, s -> new HashSet<>()).add(rule));
        });
  }

  private void computeFixpoint() {
    int round = 0;
    boolean converged = false;

    int origRules = _rules.size();

    while (!converged) {
      round++;

      initRound();

      int rules = _rules.size();

      backwardReachability();
      forwardReachability();

      int rulesRemoved = rules - _rules.size();
      System.out.println(String.format("Round %d removed %d rules.", round, rulesRemoved));
      converged = rulesRemoved == 0;
    }

    System.out.println(
        String.format("Nod optimization removed %d rules.", origRules - _rules.size()));
  }

  public Set<RuleStatement> getOptimizedRules() {
    // return them in the same order as the original rules.
    return _rules;
  }

  private void backwardReachability() {
    Set<RuleStatement> relevantRules = new HashSet<>();

    Set<StateExpr> relevantStates = new HashSet<>(_queryStates);
    Queue<StateExpr> stateWorkQueue = new ArrayDeque<>(_queryStates);

    while (!stateWorkQueue.isEmpty()) {
      StateExpr state = stateWorkQueue.poll();
      if (_derivingRules.containsKey(state)) {
        relevantRules.addAll(_derivingRules.get(state));
        _derivingRules
            .get(state)
            .forEach(
                rule ->
                    rule.getPreconditionStates()
                        .stream()
                        .filter(preState -> !relevantStates.contains(preState))
                        .forEach(
                            preState -> {
                              relevantStates.add(preState);
                              stateWorkQueue.add(preState);
                            }));
      }
    }

    _rules = relevantRules;
  }

  /* Find all states forward reachable from the graph roots (states without prestates) */
  private void forwardReachability() {
    Set<StateExpr> derivableStates = new HashSet<>();

    // start at axioms and the states they derive
    Set<RuleStatement> visitedRules =
        _rules
            .stream()
            .filter(rule -> rule.getPreconditionStates().isEmpty())
            .collect(Collectors.toSet());
    Set<StateExpr> newStates =
        visitedRules.stream().map(RuleStatement::getPostconditionState).collect(Collectors.toSet());

    // keep looking for new forward-reachable states until we're done
    long startTime = System.currentTimeMillis();
    int iterations = 0;
    while (!newStates.isEmpty()) {
      iterations++;
      derivableStates.addAll(newStates);

      HashSet<StateExpr> newNewStates = new HashSet<>();
      newStates
          .stream()
          .filter(_dependentRules::containsKey)
          .forEach(
              state ->
                  _dependentRules
                      .get(state)
                      .stream()
                      .filter(
                          rule ->
                              !visitedRules.contains(rule)
                                  && derivableStates.containsAll(rule.getPreconditionStates()))
                      .forEach(
                          rule -> {
                            StateExpr postState = rule.getPostconditionState();
                            if (!derivableStates.contains(postState)) {
                              newNewStates.add(postState);
                            }
                            visitedRules.add(rule);
                          }));
      newStates = newNewStates;
    }
    _rules = visitedRules;
    long elapsed = System.currentTimeMillis() - startTime;
    System.out.println(
        String.format(
            "forwardReachability completed %d iterations in %d milliseconds", iterations, elapsed));
  }
}
