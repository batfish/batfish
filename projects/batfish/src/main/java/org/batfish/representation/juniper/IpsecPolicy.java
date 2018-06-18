package org.batfish.representation.juniper;

import java.util.Set;
import java.util.TreeSet;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.DiffieHellmanGroup;

public class IpsecPolicy extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private DiffieHellmanGroup _pfsKeyGroup;

  private final Set<String> _proposals;

  public IpsecPolicy(String name) {
    super(name);
    _proposals = new TreeSet<>();
  }

  public DiffieHellmanGroup getPfsKeyGroup() {
    return _pfsKeyGroup;
  }

  public Set<String> getProposals() {
    return _proposals;
  }

  public void setPfsKeyGroup(DiffieHellmanGroup dhGroup) {
    _pfsKeyGroup = dhGroup;
  }
}
