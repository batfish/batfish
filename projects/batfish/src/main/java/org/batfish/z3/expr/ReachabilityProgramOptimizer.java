package org.batfish.z3.expr;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
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
  private Multimap<StateExpr, RuleStatement> _derivingRules;
  private Multimap<StateExpr, RuleStatement> _dependentRules;
  private Set<RuleStatement> _rules;

  private ReachabilityProgramOptimizer(List<RuleStatement> rules, List<QueryStatement> queries) {
    _rules = new HashSet<>(rules);
    _derivingRules = HashMultimap.create();
    _dependentRules = HashMultimap.create();
    _queryStates = queries.stream().map(QueryStatement::getStateExpr).collect(Collectors.toList());

    init();
    computeFixpoint();
  }

  private void init() {
    _dependentRules.clear();
    _derivingRules.clear();

    _rules.forEach(
        rule -> {
          _derivingRules.put(rule.getPostconditionState(), rule);
          rule.getPreconditionStates().forEach(stateExpr -> _dependentRules.put(stateExpr, rule));
        });
  }

  private void computeFixpoint() {
    boolean converged = false;

    while (!converged) {
      converged = true;

      if (forwardReachability()) {
        init();
        converged = false;
      }

      if (backwardReachability()) {
        init();
        converged = false;
      }
    }
  }

  public Set<RuleStatement> getOptimizedRules() {
    return _rules;
  }

  /**
   * Find all states that can be used (transitively) to reach any query state, ignoring any boolean
   * constraints (i.e. assuming they can be satisfied).
   *
   * @return whether any rules were removed.
   */
  private boolean backwardReachability() {
    Set<RuleStatement> relevantRules = new HashSet<>();

    int numOldRules = _rules.size();

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

    return _rules.size() < numOldRules;
  }

  /**
   * Find all states forward reachable from the graph roots (states without prestates), ignoring any
   * boolean constraints (i.e. assuming they can be satisfied).
   *
   * @return whether any rules were removed.
   */
  private boolean forwardReachability() {
    Set<StateExpr> derivableStates = new HashSet<>();

    int numOldRules = _rules.size();

    // start at axioms and the states they derive
    Set<RuleStatement> usableRules =
        _rules
            .stream()
            .filter(rule -> rule.getPreconditionStates().isEmpty())
            .collect(Collectors.toSet());
    Set<StateExpr> newStates =
        usableRules.stream().map(RuleStatement::getPostconditionState).collect(Collectors.toSet());

    // keep looking for new forward-reachable states until we're done
    while (!newStates.isEmpty()) {
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
                      /*
                       * Don't mark a rule usable until we know all its precondition states are
                       * derivable. This may never happen, in which case this rule can be removed.
                       */
                      .filter(
                          rule ->
                              !usableRules.contains(rule)
                                  && derivableStates.containsAll(rule.getPreconditionStates()))
                      .forEach(
                          rule -> {
                            usableRules.add(rule);

                            /*
                             * rule's postState is derivable, so explore its out-edges if we haven't
                             * already. This only needs to be done once because if a rule is
                             * unusable the first time but becomes usable later, then that rule
                             * has another prestate that becomes derivable after this one. We
                             * will collectTransformedVars that rule when we explore the out-edges
                             * of that other prestate.
                             */
                            StateExpr postState = rule.getPostconditionState();
                            if (!derivableStates.contains(postState)) {
                              newNewStates.add(postState);
                            }
                          }));
      newStates = newNewStates;
    }
    _rules = usableRules;
    return _rules.size() < numOldRules;
  }
}
