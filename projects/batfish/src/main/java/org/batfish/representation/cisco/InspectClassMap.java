package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InspectClassMap implements Serializable {

  private List<InspectClassMapMatch> _matches;

  private MatchSemantics _matchSemantics;

  private final String _name;

  public InspectClassMap(String name) {
    _matches = new ArrayList<>();
    _name = name;
  }

  public List<InspectClassMapMatch> getMatches() {
    return _matches;
  }

  public MatchSemantics getMatchSemantics() {
    return _matchSemantics;
  }

  public String getName() {
    return _name;
  }

  public void setMatchSemantics(MatchSemantics matchSemantics) {
    _matchSemantics = matchSemantics;
  }
}
