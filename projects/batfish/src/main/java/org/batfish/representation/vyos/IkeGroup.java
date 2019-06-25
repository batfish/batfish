package org.batfish.representation.vyos;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class IkeGroup implements Serializable {

  private int _lifetimeSeconds;

  private final Map<Integer, IkeProposal> _proposals = new TreeMap<>();

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
