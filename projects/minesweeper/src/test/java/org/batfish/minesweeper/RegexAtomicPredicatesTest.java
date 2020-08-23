package org.batfish.minesweeper;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.Set;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Tests for the {@link RegexAtomicPredicates} class. */
public class RegexAtomicPredicatesTest {
  @Test
  public void testInitAtomicPredicates() {
    Set<CommunityVar> cvars =
        ImmutableSet.of(
            CommunityVar.from("^2[0-3]:40$"),
            CommunityVar.from("^21:4[0-3]$"),
            CommunityVar.from(StandardCommunity.parse("20:40")),
            CommunityVar.from(StandardCommunity.parse("22:22")));

    RegexAtomicPredicates<CommunityVar> commAPs = new RegexAtomicPredicates<>(cvars);

    assertEquals(commAPs.getNumAtomicPredicates(), 5);

    Automaton a1 = new RegExp("^20:40$").toAutomaton();
    Automaton a2 = new RegExp("^21:40$").toAutomaton();
    Automaton a3 = new RegExp("^2[2-3]:40$").toAutomaton();
    Automaton a4 = new RegExp("^21:4[1-3]$").toAutomaton();
    Automaton a5 = new RegExp("^22:22$").toAutomaton();

    assertEquals(commAPs.getAtomicPredicateAutomata().size(), 5);
    assertThat(commAPs.getAtomicPredicateAutomata().values(), hasItem(a1));
    assertThat(commAPs.getAtomicPredicateAutomata().values(), hasItem(a2));
    assertThat(commAPs.getAtomicPredicateAutomata().values(), hasItem(a3));
    assertThat(commAPs.getAtomicPredicateAutomata().values(), hasItem(a4));
    assertThat(commAPs.getAtomicPredicateAutomata().values(), hasItem(a5));

    assertEquals(commAPs.getRegexAtomicPredicates().size(), 4);
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
  }
}
