package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;

public class CommunitySetElem implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private CommunitySetElemHalfExpr _prefix;

  private CommunitySetElemHalfExpr _suffix;

  @JsonCreator
  private CommunitySetElem() {}

  public CommunitySetElem(CommunitySetElemHalfExpr prefix, CommunitySetElemHalfExpr suffix) {
    _prefix = prefix;
    _suffix = suffix;
  }

  public CommunitySetElem(long value) {
    int prefixInt = (int) ((value & 0xFFFF0000L) >> 16);
    _prefix = new LiteralCommunitySetElemHalf(prefixInt);
    int suffixInt = (int) (value & 0xFFFFL);
    _suffix = new LiteralCommunitySetElemHalf(suffixInt);
  }

  public long community() {
    if (_prefix instanceof LiteralCommunitySetElemHalf
        && _suffix instanceof LiteralCommunitySetElemHalf) {
      LiteralCommunitySetElemHalf prefix = (LiteralCommunitySetElemHalf) _prefix;
      LiteralCommunitySetElemHalf suffix = (LiteralCommunitySetElemHalf) _suffix;
      int prefixInt = prefix.getValue();
      int suffixInt = suffix.getValue();
      return (((long) prefixInt) << 16) | suffixInt;
    } else {
      throw new BatfishException("Does not represent a single community");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CommunitySetElem other = (CommunitySetElem) obj;
    if (_prefix == null) {
      if (other._prefix != null) {
        return false;
      }
    } else if (!_prefix.equals(other._prefix)) {
      return false;
    }
    if (_suffix == null) {
      if (other._suffix != null) {
        return false;
      }
    } else if (!_suffix.equals(other._suffix)) {
      return false;
    }
    return true;
  }

  public CommunitySetElemHalfExpr getPrefix() {
    return _prefix;
  }

  public CommunitySetElemHalfExpr getSuffix() {
    return _suffix;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_prefix == null) ? 0 : _prefix.hashCode());
    result = prime * result + ((_suffix == null) ? 0 : _suffix.hashCode());
    return result;
  }

  public void setPrefix(CommunitySetElemHalfExpr prefix) {
    _prefix = prefix;
  }

  public void setSuffix(CommunitySetElemHalfExpr suffix) {
    _suffix = suffix;
  }
}
