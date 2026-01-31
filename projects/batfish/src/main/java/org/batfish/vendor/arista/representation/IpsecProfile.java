package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.batfish.datamodel.DiffieHellmanGroup;

public class IpsecProfile implements Serializable {

  private String _isakmpProfile;

  private final String _name;

  private DiffieHellmanGroup _pfsGroup;

  private List<String> _transformSets;

  public IpsecProfile(String name) {
    _name = name;
    _transformSets = new ArrayList<>();
  }

  public String getIsakmpProfile() {
    return _isakmpProfile;
  }

  public String getName() {
    return _name;
  }

  public DiffieHellmanGroup getPfsGroup() {
    return _pfsGroup;
  }

  public List<String> getTransformSets() {
    return _transformSets;
  }

  public void setIsakmpProfile(String isakmpProfile) {
    _isakmpProfile = isakmpProfile;
  }

  public void setPfsGroup(DiffieHellmanGroup pfsGroup) {
    _pfsGroup = pfsGroup;
  }
}
