package org.batfish.datamodel.acl;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.IpAccessList;

public class CanonicalAcl implements Comparable<CanonicalAcl> {

  // Mapping of ACL name to ACL; contains this ACL and any ACLs it depends on
  private Map<String, IpAccessList> _relatedAcls;
  private String _aclName;
  private IpAccessList _acl;

  public CanonicalAcl(String aclName, IpAccessList acl, Map<String, IpAccessList> dependencies) {
    _aclName = aclName;
    _acl = acl;
    _relatedAcls = ImmutableMap.copyOf(dependencies);
  }

  public IpAccessList getAcl() {
    return _acl;
  }

  public String getAclName() {
    return _aclName;
  }

  @Override
  public int compareTo(CanonicalAcl other) {
    return _aclName.compareTo(other._aclName);
  }
}
