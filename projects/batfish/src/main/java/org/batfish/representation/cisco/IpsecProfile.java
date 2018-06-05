package org.batfish.representation.cisco;

import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.DiffieHellmanGroup;

public class IpsecProfile extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private String _isakmpProfile;

  private DiffieHellmanGroup _pfsGroup;

  private List<String> _transformSets;

  public IpsecProfile(String name) {
    super(name);
    _transformSets = new LinkedList<>();
  }

  public String getIsakmpProfile() {
    return _isakmpProfile;
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

  public void setTransformSets(List<String> transformSets) {
    _transformSets = transformSets;
  }
}
