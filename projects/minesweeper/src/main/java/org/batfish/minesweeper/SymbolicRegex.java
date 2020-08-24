package org.batfish.minesweeper;

import dk.brics.automaton.Automaton;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A representation of a regular expression for symbolic route analysis. */
@ParametersAreNonnullByDefault
public abstract class SymbolicRegex {
  @Nonnull protected final String _regex;

  public SymbolicRegex(String regex) {
    _regex = regex;
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
}
