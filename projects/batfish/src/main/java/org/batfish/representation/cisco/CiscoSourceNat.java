package org.batfish.representation.cisco;

import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.transformation.Transformation;

public class CiscoSourceNat implements Serializable {
  private static final long serialVersionUID = 1L;

  private @Nullable String _aclName;
  private int _aclNameLine;
  private @Nullable String _natPool;
  private int _natPoolLine;

  @Nullable
  public String getAclName() {
    return _aclName;
  }

  public int getAclNameLine() {
    return _aclNameLine;
  }

  @Nullable
  public String getNatPool() {
    return _natPool;
  }

  public int getNatPoolLine() {
    return _natPoolLine;
  }

  public void setAclName(@Nullable String aclName) {
    this._aclName = aclName;
  }

  public void setAclNameLine(int aclNameLine) {
    this._aclNameLine = aclNameLine;
  }

  public void setNatPool(@Nullable String natPool) {
    this._natPool = natPool;
  }

  public void setNatPoolLine(int natPoolLine) {
    this._natPoolLine = natPoolLine;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CiscoSourceNat)) {
      return false;
    }
    CiscoSourceNat other = (CiscoSourceNat) o;
    return (_aclNameLine == other._aclNameLine)
        && (_natPoolLine == other._natPoolLine)
        && Objects.equals(_aclName, other._aclName)
        && Objects.equals(_natPool, other._natPool);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_aclName, _aclNameLine, _natPool, _natPoolLine);
  }

  /**
   * Convert to vendor-independent model of a packet transformation. If this is incomplete/invalid,
   * return {@link Optional#empty}.
   */
  public Optional<Transformation> toTransformation(
      Map<String, IpAccessList> ipAccessLists, Map<String, NatPool> natPools) {
    if (_aclName == null
        || !ipAccessLists.containsKey(_aclName)
        || _natPool == null
        || !natPools.containsKey(_natPool)) {
      // Unknown ACL or pool.
      return Optional.empty();
    }

    NatPool pool = natPools.get(_natPool);
    Ip firstIp = pool.getFirst();
    Ip lastIp = pool.getLast();
    if (firstIp == null || lastIp == null) {
      // pool is undefined
      return Optional.empty();
    }

    return Optional.of(
        when(permittedByAcl(_aclName)).apply(assignSourceIp(firstIp, lastIp)).build());
  }
}
