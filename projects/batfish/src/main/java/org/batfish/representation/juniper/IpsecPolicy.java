package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.batfish.datamodel.DiffieHellmanGroup;

public class IpsecPolicy implements Serializable {

  private final String _name;

  private DiffieHellmanGroup _pfsKeyGroup;

  private final List<String> _proposals;

  public IpsecPolicy(String name) {
    _name = name;
    _proposals = new ArrayList<>();
  }

  public String getName() {
    return _name;
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
