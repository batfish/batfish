package org.batfish.representation.juniper;

import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.ComparableStructure;

public final class IkePolicy extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private String _preSharedKeyHash;

  // In priority order
  private final List<String> _proposals;

  public IkePolicy(String name) {
    super(name);
    _proposals = new LinkedList<>();
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
