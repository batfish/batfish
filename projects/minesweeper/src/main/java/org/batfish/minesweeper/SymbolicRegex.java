package org.batfish.minesweeper;

import dk.brics.automaton.Automaton;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A representation of a regular expression for symbolic route analysis. */
@ParametersAreNonnullByDefault
public abstract class SymbolicRegex {
  @Nonnull protected final String _regex;

  public SymbolicRegex(String regex) {
    _regex = toAutomatonRegex(regex);
  }

  @Nonnull
  public String getRegex() {
    return _regex;
  }

  /**
   * Convert the regex to a finite-state automaton. This is used to precisely compare regexes to one
   * another for symbolic route analysis.
   *
   * @return the automaton
   */
  public abstract Automaton toAutomaton();

  // modify the given regex to conform to the grammar of the Automaton library that we use to
  // analyze regexes
  @Nonnull
  private String toAutomatonRegex(String regex) {
    // the Automaton library does not support the character class \d
    return regex.replace("\\d", "[0-9]");
  }
}
