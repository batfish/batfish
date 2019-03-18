package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/** Represents one (of possibly multiple) potential matches when the parser input does not match */
class PotentialMatch {

  /** The type of completion this match indicates */
  private final Anchor.Type _anchorType;

  /** The current rule would have matched if the input was followed by this */
  private final String _matchCompletion;

  /** What matched thus far when trying to match the current rule */
  private final String _matchPrefix;

  /** Where in the input this match started */
  private final int _matchStartIndex;

  PotentialMatch(
      Anchor.Type anchorType, String matchPrefix, String matchCompletion, int matchStartIndex) {
    _anchorType = anchorType;
    _matchPrefix = matchPrefix;
    _matchCompletion = matchCompletion;
    _matchStartIndex = matchStartIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PotentialMatch)) {
      return false;
    }
    return Objects.equals(_anchorType, ((PotentialMatch) o)._anchorType)
        && Objects.equals(_matchCompletion, ((PotentialMatch) o)._matchCompletion)
        && Objects.equals(_matchPrefix, ((PotentialMatch) o)._matchPrefix)
        && Objects.equals(_matchStartIndex, ((PotentialMatch) o)._matchStartIndex);
  }

  Anchor.Type getAnchorType() {
    return _anchorType;
  }

  String getMatchCompletion() {
    return _matchCompletion;
  }

  String getMatchPrefix() {
    return _matchPrefix;
  }

  int getMatchStartIndex() {
    return _matchStartIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_anchorType, _matchCompletion, _matchPrefix, _matchStartIndex);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this.getClass())
        .add("anchorType", _anchorType)
        .add("matchingPrefix", _matchPrefix)
        .add("matchingCompletion", _matchCompletion)
        .add("matchStartIndex", _matchStartIndex)
        .toString();
  }
}
