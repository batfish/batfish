package org.batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.DiffieHellmanGroup;

public class IpsecPolicy extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private DiffieHellmanGroup _pfsKeyGroup;

  private final List<String> _proposals;

  public IpsecPolicy(String name) {
    super(name);
    _proposals = new ArrayList<>();
  }

  public DiffieHellmanGroup getPfsKeyGroup() {
    return _pfsKeyGroup;
  }

  public List<String> getProposals() {
    return _proposals;
  }

  public void setPfsKeyGroup(DiffieHellmanGroup dhGroup) {
    _pfsKeyGroup = dhGroup;
  }
}
