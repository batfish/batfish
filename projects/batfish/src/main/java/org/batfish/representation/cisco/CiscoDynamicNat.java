package org.batfish.representation.cisco;

import java.util.Objects;
import javax.annotation.Nullable;

public class CiscoDynamicNat extends CiscoNat {
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
    _aclName = aclName;
  }

  public void setAclNameLine(int aclNameLine) {
    _aclNameLine = aclNameLine;
  }

  public void setNatPool(@Nullable String natPool) {
    _natPool = natPool;
  }

  public void setNatPoolLine(int natPoolLine) {
    _natPoolLine = natPoolLine;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CiscoDynamicNat)) {
      return false;
    }
    CiscoDynamicNat other = (CiscoDynamicNat) o;
    return (_aclNameLine == other._aclNameLine)
        && (_action == other._action)
        && (_natPoolLine == other._natPoolLine)
        && Objects.equals(_aclName, other._aclName)
        && Objects.equals(_natPool, other._natPool);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_aclName, _aclNameLine, _action, _natPool, _natPoolLine);
  }
}
