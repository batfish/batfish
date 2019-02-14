package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/** Represents one (of possibly multiple) potential matches when the parser input does not match */
class PotentialMatch {

  /** The type of completion this match indicates */
  private final Completion.Type _completionType;

  /** The current rule would have matched if the input was followed by this */
  private final String _matchCompletion;

  /** What matched thus far when trying to match the current rule */
  private final String _matchPrefix;

  PotentialMatch(Completion.Type completionType, String matchPrefix, String matchCompletion) {
    _completionType = completionType;
    _matchPrefix = matchPrefix;
    _matchCompletion = matchCompletion;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PotentialMatch)) {
      return false;
    }
    return Objects.equals(_completionType, ((PotentialMatch) o)._completionType)
        && Objects.equals(_matchCompletion, ((PotentialMatch) o)._matchCompletion)
        && Objects.equals(_matchPrefix, ((PotentialMatch) o)._matchPrefix);
  }

  Completion.Type getCompletionType() {
    return _completionType;
  }

  String getMatchCompletion() {
    return _matchCompletion;
  }

  String getMatchPrefix() {
    return _matchPrefix;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_completionType, _matchCompletion, _matchPrefix);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this.getClass())
        .add("completionType", _completionType)
        .add("matchingPrefix", _matchPrefix)
        .add("matchingCompletion", _matchCompletion)
        .toString();
  }
}
