package org.batfish.smt.answers;

import java.util.Map;
import java.util.Set;

public class SmtReachabilityAnswerElement extends SmtOneAnswerElement {

  private Set<String> _unreachableSources;

  private Map<String, Boolean> _diffReachability;

  public Map<String, Boolean> getDiffReachability() {
    return _diffReachability;
  }

  public Set<String> getUnreachableSources() {
    return _unreachableSources;
  }

  public void setDiffReachability(Map<String, Boolean> diffReachability) {
    this._diffReachability = diffReachability;
  }

  public void setUnreachableSources(Set<String> unreachableSources) {
    this._unreachableSources = unreachableSources;
  }

  @Override
  public String prettyPrint() {
    StringBuilder s = new StringBuilder();
    s.append(_result.prettyPrint(null));
    if (_unreachableSources != null) {
      s.append("\n");
      for (String source : _unreachableSources) {
        s.append("Unreachable source: ").append(source).append("\n");
      }
    }
    if (_diffReachability != null) {
      s.append("\n");
      _diffReachability.forEach(
          (source, reachable) -> {
            if (reachable) {
              s.append(source)
                  .append(" can reach the destination with failures but can without")
                  .append("\n");
            } else {
              s.append(source)
                  .append(" can't reach the destination with failures but can without")
                  .append("\n");
            }
          });
    }

    return s.toString();
  }
}
