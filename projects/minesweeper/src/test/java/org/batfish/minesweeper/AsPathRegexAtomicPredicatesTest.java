package org.batfish.minesweeper;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.Set;
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

    assertEquals(asPathAPs.getNumAtomicPredicates(), 5);

    Automaton a1 = new RegExp("^^$").toAutomaton();
    // starts with 5 and ends with 4
    Automaton a2 = new RegExp("^^5 ((0|[1-9][0-9]*) )*4$").toAutomaton();
    // ends with 4 but does not start with 5
    Automaton a3 = new RegExp("^^([0-4]|[6-9]|[1-9][0-9]+) ((0|[1-9][0-9]*) )*4$").toAutomaton();
    // starts with 5 but does not end with 4
    Automaton a4 = new RegExp("^^5( (0|[1-9][0-9]*))* ([0-3]|[5-9]|[1-9][0-9]+)$").toAutomaton();

    assertEquals(asPathAPs.getAtomicPredicateAutomata().size(), 5);
    assertThat(asPathAPs.getAtomicPredicateAutomata().values(), hasItem(a1));
    assertThat(asPathAPs.getAtomicPredicateAutomata().values(), hasItem(a2));
    assertThat(asPathAPs.getAtomicPredicateAutomata().values(), hasItem(a3));
    assertThat(asPathAPs.getAtomicPredicateAutomata().values(), hasItem(a4));

    assertEquals(asPathAPs.getRegexAtomicPredicates().size(), 4);
    assertThat(
        asPathAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(new SymbolicAsPathRegex("^$")), iterableWithSize(1)));
    assertThat(
        asPathAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(new SymbolicAsPathRegex(" 4$")), iterableWithSize(2)));
    assertThat(
        asPathAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(new SymbolicAsPathRegex("^5 ")), iterableWithSize(2)));
    assertThat(
        asPathAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(new SymbolicAsPathRegex(".*")), iterableWithSize(5)));
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

    assertEquals(asPathAPs, copy);
    assertNotSame(asPathAPs.getRegexes(), copy.getRegexes());
    assertNotSame(asPathAPs.getRegexAtomicPredicates(), copy.getRegexAtomicPredicates());
    assertNotSame(asPathAPs.getAtomicPredicateAutomata(), copy.getAtomicPredicateAutomata());
  }
}
