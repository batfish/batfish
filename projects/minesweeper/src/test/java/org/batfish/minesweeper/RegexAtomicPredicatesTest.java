package org.batfish.minesweeper;

import static org.batfish.minesweeper.SymbolicAsPathRegex.AS_NUM_REGEX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.Set;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Tests for the {@link RegexAtomicPredicates} class. */
public class RegexAtomicPredicatesTest {
  @Test
  public void testInitAtomicPredicatesCVars() {
    Set<CommunityVar> cvars =
        ImmutableSet.of(
            CommunityVar.from("^2[0-3]:40$"),
            CommunityVar.from("^21:4[0-3]$"),
            CommunityVar.from(StandardCommunity.parse("20:40")),
            CommunityVar.from(StandardCommunity.parse("22:22")));

    RegexAtomicPredicates<CommunityVar> commAPs =
        new RegexAtomicPredicates<>(cvars, CommunityVar.ALL_STANDARD_COMMUNITIES);

    assertEquals(commAPs.getNumAtomicPredicates(), 6);

    Automaton a1 = new RegExp("^20:40$").toAutomaton();
    Automaton a2 = new RegExp("^21:40$").toAutomaton();
    Automaton a3 = new RegExp("^2[2-3]:40$").toAutomaton();
    Automaton a4 = new RegExp("^21:4[1-3]$").toAutomaton();
    Automaton a5 = new RegExp("^22:22$").toAutomaton();

    assertEquals(commAPs.getAtomicPredicateAutomata().size(), 6);
    assertThat(commAPs.getAtomicPredicateAutomata().values(), hasItem(a1));
    assertThat(commAPs.getAtomicPredicateAutomata().values(), hasItem(a2));
    assertThat(commAPs.getAtomicPredicateAutomata().values(), hasItem(a3));
    assertThat(commAPs.getAtomicPredicateAutomata().values(), hasItem(a4));
    assertThat(commAPs.getAtomicPredicateAutomata().values(), hasItem(a5));

    assertEquals(commAPs.getRegexAtomicPredicates().size(), 5);
    assertThat(
        commAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(CommunityVar.from("^2[0-3]:40$")), iterableWithSize(3)));
    assertThat(
        commAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(CommunityVar.from("^21:4[0-3]$")), iterableWithSize(2)));
    assertThat(
        commAPs.getRegexAtomicPredicates(),
        hasEntry(
            equalTo(CommunityVar.from(StandardCommunity.parse("20:40"))), iterableWithSize(1)));
    assertThat(
        commAPs.getRegexAtomicPredicates(),
        hasEntry(
            equalTo(CommunityVar.from(StandardCommunity.parse("22:22"))), iterableWithSize(1)));
    assertThat(
        commAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(CommunityVar.from(".*")), iterableWithSize(6)));
  }

  @Test
  public void testNoOutOfIntegerRangeCommunities() {
    // check that we don't create atomic predicates that represent ill-formed communities, due to
    // out-of-range community values

    // these two community regexes are equivalent because adding any prefix to the number 12345
    // yields a number that is out of range (beyond 16 bits)
    Set<CommunityVar> cvars =
        ImmutableSet.of(CommunityVar.from("12345:67$"), CommunityVar.from("^12345:67$"));

    RegexAtomicPredicates<CommunityVar> commAPs =
        new RegexAtomicPredicates<>(cvars, CommunityVar.ALL_STANDARD_COMMUNITIES);

    assertEquals(commAPs.getNumAtomicPredicates(), 2);

    Automaton a1 = new RegExp("^12345:67$").toAutomaton();

    assertEquals(commAPs.getAtomicPredicateAutomata().size(), 2);
    assertThat(commAPs.getAtomicPredicateAutomata().values(), hasItem(a1));

    assertEquals(commAPs.getRegexAtomicPredicates().size(), 3);
    assertThat(
        commAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(CommunityVar.from("12345:67$")), iterableWithSize(1)));
    assertThat(
        commAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(CommunityVar.from("^12345:67$")), iterableWithSize(1)));
    assertThat(
        commAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(CommunityVar.from(".*")), iterableWithSize(2)));
  }

  @Test
  public void testInitAtomicPredicatesAsPath() {
    Set<SymbolicAsPathRegex> asPathRegexes =
        ImmutableSet.of(
            new SymbolicAsPathRegex("^$"),
            new SymbolicAsPathRegex(" 4$"),
            new SymbolicAsPathRegex("^5 "));

    RegexAtomicPredicates<SymbolicAsPathRegex> asPathAPs =
        new RegexAtomicPredicates<>(asPathRegexes, SymbolicAsPathRegex.ALL_AS_PATHS);

    assertEquals(asPathAPs.getNumAtomicPredicates(), 5);

    Automaton a1 = new RegExp("^^$").toAutomaton();
    // starts with 5 and ends with 4
    Automaton a2 = new RegExp("^^5 (" + AS_NUM_REGEX + " )*4$").toAutomaton();
    // ends with 4 but does not start with 5
    Automaton a3 =
        new RegExp(
                "^^(([0-4]|[6-9]|[1-9][0-9]+)&"
                    + AS_NUM_REGEX
                    + ")"
                    + " ("
                    + AS_NUM_REGEX
                    + " )*4$")
            .toAutomaton();
    // starts with 5 but does not end with 4
    Automaton a4 =
        new RegExp("^^5( " + AS_NUM_REGEX + ")* (([0-3]|[5-9]|[1-9][0-9]+)&" + AS_NUM_REGEX + ")$")
            .toAutomaton();

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
  public void testNoOutOfRangeASNs() {
    Set<SymbolicAsPathRegex> asPathRegexes =
        ImmutableSet.of(
            new SymbolicAsPathRegex("3333333333$"), new SymbolicAsPathRegex("^3333333333$"));

    RegexAtomicPredicates<SymbolicAsPathRegex> asPathAPs =
        new RegexAtomicPredicates<>(asPathRegexes, SymbolicAsPathRegex.ALL_AS_PATHS);

    assertEquals(asPathAPs.getNumAtomicPredicates(), 3);

    // only 10 3s
    Automaton a1 = new RegExp("^^3333333333$").toAutomaton();
    // ends with 10 3s and has at least one other ASN;
    // we can't create a large ASN by prepending to 10 3s because that would be out of ASN range
    Automaton a2 =
        new RegExp("^^(" + AS_NUM_REGEX + " )*" + AS_NUM_REGEX + " 3333333333$").toAutomaton();

    assertEquals(asPathAPs.getAtomicPredicateAutomata().size(), 3);
    assertThat(asPathAPs.getAtomicPredicateAutomata().values(), hasItem(a1));
    assertThat(asPathAPs.getAtomicPredicateAutomata().values(), hasItem(a2));

    assertEquals(asPathAPs.getRegexAtomicPredicates().size(), 3);
    assertThat(
        asPathAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(new SymbolicAsPathRegex("3333333333$")), iterableWithSize(2)));
    assertThat(
        asPathAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(new SymbolicAsPathRegex("^3333333333$")), iterableWithSize(1)));
    assertThat(
        asPathAPs.getRegexAtomicPredicates(),
        hasEntry(equalTo(new SymbolicAsPathRegex(".*")), iterableWithSize(3)));
  }

  @Test
  public void testCopyConstructor() {
    Set<CommunityVar> cvars =
        ImmutableSet.of(
            CommunityVar.from("^2[0-3]:40$"),
            CommunityVar.from("^21:4[0-3]$"),
            CommunityVar.from(StandardCommunity.parse("20:40")),
            CommunityVar.from(StandardCommunity.parse("22:22")));

    RegexAtomicPredicates<CommunityVar> commAPs =
        new RegexAtomicPredicates<>(cvars, CommunityVar.ALL_STANDARD_COMMUNITIES);

    RegexAtomicPredicates<CommunityVar> copy = new RegexAtomicPredicates<>(commAPs);

    assertEquals(commAPs.getRegexes(), copy.getRegexes());
    assertEquals(commAPs.getTrueRegex(), copy.getTrueRegex());
    assertEquals(commAPs.getRegexAtomicPredicates(), copy.getRegexAtomicPredicates());
    assertEquals(commAPs.getAtomicPredicateAutomata(), copy.getAtomicPredicateAutomata());
  }
}
