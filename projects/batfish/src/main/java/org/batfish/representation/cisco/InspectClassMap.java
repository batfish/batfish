package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.ComparableStructure;

public class InspectClassMap extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private List<InspectClassMapMatch> _matches;

  private MatchSemantics _matchSemantics;

  public InspectClassMap(String name) {
    super(name);
    _matches = new ArrayList<>();
  }

  public List<InspectClassMapMatch> getMatches() {
    return _matches;
  }

  public MatchSemantics getMatchSemantics() {
    return _matchSemantics;
  }

  public void setMatchSemantics(MatchSemantics matchSemantics) {
    _matchSemantics = matchSemantics;
  }
}
