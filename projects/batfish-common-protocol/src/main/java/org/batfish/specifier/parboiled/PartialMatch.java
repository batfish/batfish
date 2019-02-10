package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/** Represents one (of possibly multiple) potential matches when the parser input does not match */
public class PartialMatch {

  /** The current rule would have matched if the input was followed by this */
  private final String _matchCompletion;

  /** What matched thus far when trying to match the current rule */
  private final String _matchPrefix;

  /** The label for the rule that we were trying to match */
  private final String _ruleLabel;

  public PartialMatch(String ruleLabel, String matchPrefix, String matchCompletion) {
    _ruleLabel = ruleLabel;
    _matchPrefix = matchPrefix;
    _matchCompletion = matchCompletion;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PartialMatch)) {
      return false;
    }
    return Objects.equals(_matchCompletion, ((PartialMatch) o)._matchCompletion)
        && Objects.equals(_matchPrefix, ((PartialMatch) o)._matchPrefix)
        && Objects.equals(_ruleLabel, ((PartialMatch) o)._ruleLabel);
  }

  public String getMatchCompletion() {
    return _matchCompletion;
  }

  public String getMatchPrefix() {
    return _matchPrefix;
  }

  public String getRuleLabel() {
    return _ruleLabel;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_matchCompletion, _matchPrefix, _ruleLabel);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this.getClass())
        .add("ruleLabel", _ruleLabel)
        .add("matchingPrefix", _matchPrefix)
        .add("matchingCompletion", _matchCompletion)
        .toString();
  }
}
