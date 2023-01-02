package org.batfish.minesweeper;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.minesweeper.question.searchroutepolicies.RegexConstraint;
import org.batfish.minesweeper.question.searchroutepolicies.RegexConstraints;

/**
 * A class that maintains the atomic predicates for AS paths. This class derives from the generic
 * class for handling regex atomic predicates but adds support for AS-path specific things, notably
 * handling AS prepending.
 */
public class AsPathRegexAtomicPredicates extends RegexAtomicPredicates<SymbolicAsPathRegex> {

  public AsPathRegexAtomicPredicates(Set<SymbolicAsPathRegex> regexes) {
    super(regexes, SymbolicAsPathRegex.ALL_AS_PATHS);
  }

  public AsPathRegexAtomicPredicates(AsPathRegexAtomicPredicates other) {
    super(other);
  }

  public void prependAPs(List<Long> prepended) {
    if (prepended.isEmpty()) {
      return;
    }
    Map<Integer, Automaton> apAutomata = this.getAtomicPredicateAutomata();
    List<String> prepends =
        prepended.stream().map(l -> Long.toString(l)).collect(Collectors.toList());
    // we separately consider the case of prepending to an empty AS-path and a non-empty one, since
    // the regex that we have to produce is different for each case
    String prependStr = "^^" + String.join(" ", prepends);
    Automaton prependToEmptyAutomaton = new RegExp(prependStr).toAutomaton();
    Automaton prependToNonemptyAutomaton = new RegExp(prependStr + " ").toAutomaton();
    for (Integer i : apAutomata.keySet()) {
      // remove the leading ^ characters
      Automaton iA = apAutomata.get(i).subst('^', "");
      // concatenate the prepends to the front, handling empty and non-empty AS-paths separately.
      Automaton iAPrependOnly =
          prependToEmptyAutomaton.concatenate(iA.intersection(new RegExp("$").toAutomaton()));
      Automaton iAPrependPlus =
          prependToNonemptyAutomaton.concatenate(iA.intersection(new RegExp(".+$").toAutomaton()));
      // replace the original automaton with the union of these new automata.
      // if the original automaton does not include empty (non-empty) AS paths then iAPrependOnly
      // (iAPrependPlus) will not represent legal AS paths. intersecting with an automaton
      // representing all legal AS paths addresses that issue.
      apAutomata.replace(
          i,
          iAPrependOnly
              .union(iAPrependPlus)
              .intersection(SymbolicAsPathRegex.ALL_AS_PATHS.toAutomaton()));
    }
  }

  public void constrainAPs(RegexConstraints regexes) {
    if (regexes.isEmpty()) {
      return;
    }

    // an automaton representing the disjunction of the positive regexes
    List<RegexConstraint> posRegexes = regexes.getPositiveRegexConstraints();
    Automaton positiveConstraints =
        posRegexes.isEmpty()
            ? SymbolicAsPathRegex.ALL_AS_PATHS.toAutomaton()
            : posRegexes.stream()
                .reduce(
                    Automaton.makeEmpty(),
                    (a, r) -> a.union(new SymbolicAsPathRegex(r.getRegex()).toAutomaton()),
                    Automaton::union);

    // an automaton representing the disjunction of the negative regexes
    Automaton negativeConstraints =
        regexes.getNegativeRegexConstraints().stream()
            .reduce(
                Automaton.makeEmpty(),
                (a, r) -> a.union(new SymbolicAsPathRegex(r.getRegex()).toAutomaton()),
                Automaton::union);

    Automaton constraint = positiveConstraints.intersection(negativeConstraints.complement());

    Map<Integer, Automaton> apAutomata = this.getAtomicPredicateAutomata();
    for (Integer i : apAutomata.keySet()) {
      Automaton iA = apAutomata.get(i);
      apAutomata.replace(i, iA.intersection(constraint));
    }
  }
}
