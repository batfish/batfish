package org.batfish.representation.vyos;

import java.util.Map;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;

public class IkeGroup extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private int _lifetimeSeconds;

  private final Map<Integer, IkeProposal> _proposals;

  public IkeGroup(String name) {
    super(name);
    _proposals = new TreeMap<>();
  }

  public int getLifetimeSeconds() {
    return _lifetimeSeconds;
  }

  public Map<Integer, IkeProposal> getProposals() {
    return _proposals;
  }

  public void setLifetimeSeconds(int lifetimeSeconds) {
    _lifetimeSeconds = lifetimeSeconds;
  }
}
