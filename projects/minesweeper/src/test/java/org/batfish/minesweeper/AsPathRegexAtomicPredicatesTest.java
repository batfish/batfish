package org.batfish.minesweeper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasValue;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.Map;
import java.util.Set;
import org.batfish.minesweeper.question.searchroutepolicies.RegexConstraint;
import org.batfish.minesweeper.question.searchroutepolicies.RegexConstraints;
import org.junit.Test;

/** Tests for the {@link AsPathRegexAtomicPredicates} class. */
public class AsPathRegexAtomicPredicatesTest {

  @Test
  public void testInitAtomicPredicatesAsPath() {
    Set<SymbolicAsPathRegex> asPathRegexes =
        ImmutableSet.of(
            new SymbolicAsPathRegex("^$"),
            new SymbolicAsPathRegex(" 4$"),
            new SymbolicAsPathRegex("^5 "));

    AsPathRegexAtomicPredicates asPathAPs = new AsPathRegexAtomicPredicates(asPathRegexes);

    RegexAtomicPredicates<SymbolicAsPathRegex> orig =
        new RegexAtomicPredicates<>(asPathRegexes, SymbolicAsPathRegex.ALL_AS_PATHS);

    assertEquals(orig.getAtomicPredicateAutomata(), asPathAPs.getAtomicPredicateAutomata());
    assertEquals(orig.getRegexAtomicPredicates(), asPathAPs.getRegexAtomicPredicates());
    assertEquals(orig.getNumAtomicPredicates(), asPathAPs.getNumAtomicPredicates());
  }

  @Test
  public void testCopyConstructor() {
    Set<SymbolicAsPathRegex> asPathRegexes =
        ImmutableSet.of(
            new SymbolicAsPathRegex("^$"),
            new SymbolicAsPathRegex(" 4$"),
            new SymbolicAsPathRegex("^5 "));

    AsPathRegexAtomicPredicates asPathAPs = new AsPathRegexAtomicPredicates(asPathRegexes);

    AsPathRegexAtomicPredicates copy = new AsPathRegexAtomicPredicates(asPathAPs);

    assertEquals(asPathAPs.getRegexes(), copy.getRegexes());
    assertEquals(asPathAPs.getTrueRegex(), copy.getTrueRegex());
    assertEquals(asPathAPs.getRegexAtomicPredicates(), copy.getRegexAtomicPredicates());
    assertEquals(asPathAPs.getAtomicPredicateAutomata(), copy.getAtomicPredicateAutomata());
  }

  @Test
  public void testPrependAPs() {
    AsPathRegexAtomicPredicates oneAP = new AsPathRegexAtomicPredicates(ImmutableSet.of());
    oneAP.prependAPs(ImmutableList.of(10L, 20L));
    Map<Integer, Automaton> automataMap = oneAP.getAtomicPredicateAutomata();
    assertEquals(automataMap.keySet().size(), 1);
    assertEquals(
        automataMap.get(0),
        new RegExp("^^10 20$")
            .toAutomaton()
            .union(
                new RegExp("^^10 20 .*")
                    .toAutomaton()
                    .intersection(SymbolicAsPathRegex.ALL_AS_PATHS.toAutomaton())));

    Set<SymbolicAsPathRegex> asPathRegexes = ImmutableSet.of(new SymbolicAsPathRegex("^$"));
    AsPathRegexAtomicPredicates twoAPs = new AsPathRegexAtomicPredicates(asPathRegexes);

    AsPathRegexAtomicPredicates copy1 = new AsPathRegexAtomicPredicates(twoAPs);
    copy1.prependAPs(ImmutableList.of());
    assertEquals(twoAPs.getAtomicPredicateAutomata(), copy1.getAtomicPredicateAutomata());

    AsPathRegexAtomicPredicates copy2 = new AsPathRegexAtomicPredicates(twoAPs);
    copy2.prependAPs(ImmutableList.of(10L, 20L));
    Map<Integer, Automaton> copy2AutomataMap = copy2.getAtomicPredicateAutomata();
    assertEquals(copy2AutomataMap.keySet().size(), 2);
    assertThat(
        copy2AutomataMap,
        allOf(
            hasValue(equalTo(new RegExp("^^10 20$").toAutomaton())),
            hasValue(
                equalTo(
                    new RegExp("^^10 20 .*")
                        .toAutomaton()
                        .intersection(SymbolicAsPathRegex.ALL_AS_PATHS.toAutomaton())))));
  }

  @Test
  public void testConstrainAPs() {
    Set<SymbolicAsPathRegex> asPathRegexes = ImmutableSet.of(new SymbolicAsPathRegex("^$"));
    AsPathRegexAtomicPredicates twoAPs = new AsPathRegexAtomicPredicates(asPathRegexes);

    AsPathRegexAtomicPredicates copy1 = new AsPathRegexAtomicPredicates(twoAPs);
    copy1.constrainAPs(new RegexConstraints());
    assertEquals(twoAPs.getAtomicPredicateAutomata(), copy1.getAtomicPredicateAutomata());

    AsPathRegexAtomicPredicates copy2 = new AsPathRegexAtomicPredicates(twoAPs);
    copy2.constrainAPs(
        new RegexConstraints(
            ImmutableList.of(
                new RegexConstraint("^40 ", false), new RegexConstraint("^50 ", false))));
    Map<Integer, Automaton> copy2AutomataMap = copy2.getAtomicPredicateAutomata();
    assertEquals(copy2AutomataMap.keySet().size(), 2);
    assertThat(
        copy2AutomataMap,
        allOf(
            hasValue(equalTo(Automaton.makeEmpty())),
            hasValue(
                equalTo(
                    new RegExp("^^(40|50) .*")
                        .toAutomaton()
                        .intersection(SymbolicAsPathRegex.ALL_AS_PATHS.toAutomaton())))));

    AsPathRegexAtomicPredicates copy3 = new AsPathRegexAtomicPredicates(twoAPs);
    copy3.constrainAPs(
        new RegexConstraints(
            ImmutableList.of(
                new RegexConstraint("^40 ", true), new RegexConstraint("^50 ", true))));
    Map<Integer, Automaton> copy3AutomataMap = copy3.getAtomicPredicateAutomata();
    assertEquals(copy3AutomataMap.keySet().size(), 2);
    assertThat(
        copy3AutomataMap,
        allOf(
            hasValue(equalTo(new RegExp("^^$").toAutomaton())),
            hasValue(
                equalTo(
                    new RegExp("^^.+$")
                        .toAutomaton()
                        .intersection(new RegExp("^^(40|50) .*").toAutomaton().complement())
                        .intersection(SymbolicAsPathRegex.ALL_AS_PATHS.toAutomaton())))));
  }
}
