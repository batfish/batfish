package org.batfish.minesweeper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import dk.brics.automaton.Automaton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;

/**
 * A helper class to compute atomic predicates for a set of regular expressions. This is used by the
 * symbolic analysis to reason about community regexes and also AS-path regexes.
 *
 * <p>The atomic predicates are the minimal set of predicates such that: 1. no atomic predicate is
 * logically false; 2. the disjunction of all atomic predicates is logically true; 3. each atomic
 * predicate is disjoint from all others; 4. each regex is equivalent to a disjunction of some
 * subset of the atomic predicates.
 *
 * <p>The idea of atomic predicates comes from the paper "Real-time Verification of Network
 * Properties using Atomic Predicates" by Yang and Lam, IEEE/ACM Transactions on Networking, April
 * 2016, Volume 24, No. 2, pages 887-900.
 * http://www.cs.utexas.edu/users/lam/Vita/Jpapers/Yang_Lam_TON_2015.pdf In that paper, they create
 * atomic predicates in order to precisely and scalably analyze packet forwarding symbolically; we
 * use the same idea to track regexes in symbolic routing analysis.
 *
 * @param <T> the particular type of regexes
 */
@ParametersAreNonnullByDefault
public class RegexAtomicPredicates<T extends SymbolicRegex> {

  @Nonnull private final Set<T> _regexes;

  // the number of atomic predicates
  private int _numAtomicPredicates;

  // maps each regex to its set of atomic predicates, which are integers in the range
  // 0 ... (_numAtomicPredicates - 1)
  @Nonnull private Map<T, Set<Integer>> _regexAtomicPredicates;

  // maps each atomic predicate number to its semantic representation, which is a finite-state
  // automaton
  @Nonnull private Map<Integer, Automaton> _atomicPredicateAutomata;

  /**
   * Create atomic predicates for the given set of regexes.
   *
   * @param regexes the regexes
   * @param trueRegex a regex representing logical "true", or all possible valid strings
   */
  public RegexAtomicPredicates(Set<T> regexes, T trueRegex) {
    _regexes = ImmutableSet.<T>builder().addAll(regexes).add(trueRegex).build();
    initAtomicPredicates();
  }

  private void initAtomicPredicates() {
    SetMultimap<Automaton, T> mmap = HashMultimap.create();
    for (T regex : _regexes) {
      Automaton rAuto = regex.toAutomaton();
      if (rAuto.isEmpty()) {
        // regex doesn't match any communities; give up
        throw new BatfishException("Regex " + regex + " does not match any strings");
      }
      SetMultimap<Automaton, T> newMMap = HashMultimap.create(mmap);
      for (Automaton a : mmap.keySet()) {
        if (a.equals(rAuto)) {
          newMMap.put(a, regex);
          // since all atomic predicates are disjoint from one another, if
          // this regex is equal to an existing one, we can ignore all the rest
          break;
        }
        Automaton inter = a.intersection(rAuto);
        if (inter.isEmpty()) {
          // this regex is disjoint from a, so move on to the next atomic predicate
          continue;
        }
        // replace automaton a with two new atomic predicates, representing the intersection
        // and difference with regex's automaton
        Set<T> regexes = newMMap.removeAll(a);
        Automaton diff = a.minus(rAuto);
        newMMap.putAll(inter, regexes);
        if (!diff.isEmpty()) {
          newMMap.putAll(diff, regexes);
        }
        // add regex to the intersection
        newMMap.put(inter, regex);
        // update cAuto with the residual automaton that is left
        rAuto = rAuto.minus(a);
      }
      if (!rAuto.isEmpty()) {
        // if there's anything left of cAuto by the end, add it
        newMMap.put(rAuto, regex);
      }
      mmap = newMMap;
    }
    // assign a unique integer to each automaton.
    // create a mapping from each integer to its corresponding automaton
    // and a mapping from each regex to its corresponding set of integers.
    _atomicPredicateAutomata = new HashMap<>();
    SetMultimap<Integer, T> iToR = HashMultimap.create();
    int i = 0;
    for (Automaton a : mmap.keySet()) {
      _atomicPredicateAutomata.put(i, a);
      iToR.putAll(i, mmap.get(a));
      i++;
    }
    _numAtomicPredicates = i;
    _regexAtomicPredicates = Multimaps.asMap(Multimaps.invertFrom(iToR, HashMultimap.create()));
  }

  public int getNumAtomicPredicates() {
    return _numAtomicPredicates;
  }

  @Nonnull
  public Map<Integer, Automaton> getAtomicPredicateAutomata() {
    return _atomicPredicateAutomata;
  }

  @Nonnull
  public Map<T, Set<Integer>> getRegexAtomicPredicates() {
    return _regexAtomicPredicates;
  }
}
