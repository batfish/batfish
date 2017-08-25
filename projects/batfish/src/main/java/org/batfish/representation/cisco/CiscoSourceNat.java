package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;

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
}
