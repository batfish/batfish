package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public final class IkePolicy implements Serializable {

  private final String _name;

  private String _preSharedKeyHash;

  // In priority order
  private final List<String> _proposals;

  public IkePolicy(String name) {
    _name = name;
    _proposals = new LinkedList<>();
  }

  public String getName() {
    return _name;
  }

  public String getPreSharedKeyHash() {
    return _preSharedKeyHash;
  }

  public List<String> getProposals() {
    return _proposals;
  }

  public void setPreSharedKeyHash(String preSharedKeyHash) {
    _preSharedKeyHash = preSharedKeyHash;
  }
}
