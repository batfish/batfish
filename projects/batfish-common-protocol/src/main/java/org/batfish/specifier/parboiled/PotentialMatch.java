package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/** Represents one (of possibly multiple) potential matches when the parser input does not match */
class PotentialMatch {

  /** The type of completion this match indicates */
  private final Anchor.Type _anchorType;

  /** The current rule would have matched if the input was this */
  private final String _match;

  /** What the user entered */
  private final String _matchPrefix;

  /** Where in the input this match started */
  private final int _matchStartIndex;

  PotentialMatch(Anchor.Type anchorType, String matchPrefix, String match, int matchStartIndex) {
    _anchorType = anchorType;
    _matchPrefix = matchPrefix;
    _match = match;
    _matchStartIndex = matchStartIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PotentialMatch)) {
      return false;
    }
    return Objects.equals(_anchorType, ((PotentialMatch) o)._anchorType)
        && Objects.equals(_match, ((PotentialMatch) o)._match)
        && Objects.equals(_matchPrefix, ((PotentialMatch) o)._matchPrefix)
        && Objects.equals(_matchStartIndex, ((PotentialMatch) o)._matchStartIndex);
  }

  Anchor.Type getAnchorType() {
    return _anchorType;
  }

  String getMatch() {
    return _match;
  }

  String getMatchPrefix() {
    return _matchPrefix;
  }

  int getMatchStartIndex() {
    return _matchStartIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_anchorType, _match, _matchPrefix, _matchStartIndex);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this.getClass())
        .add("anchorType", _anchorType)
        .add("matchingPrefix", _matchPrefix)
        .add("matchingCompletion", _match)
        .add("matchStartIndex", _matchStartIndex)
        .toString();
  }
}
