package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.batfish.datamodel.DiffieHellmanGroup;

/** Represents an IPsec profile configuration */
public class FtdIpsecProfile implements Serializable {

  private final String _name;
  private String _isakmpProfile;
  private DiffieHellmanGroup _pfsGroup;
  private List<String> _transformSets;

  public FtdIpsecProfile(String name) {
    _name = name;
    _transformSets = new ArrayList<>();
  }

  public String getName() {
    return _name;
  }

  public @Nullable String getIsakmpProfile() {
    return _isakmpProfile;
  }

  public @Nullable DiffieHellmanGroup getPfsGroup() {
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

  @Override
  public String toString() {
    return String.format(
        "FtdIpsecProfile[name=%s, isakmp=%s, pfs=%s, transforms=%s]",
        _name, _isakmpProfile, _pfsGroup, _transformSets);
  }
}
