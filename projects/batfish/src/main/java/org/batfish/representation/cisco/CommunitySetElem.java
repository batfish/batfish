package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;

public class CommunitySetElem implements Serializable {

  private static final long serialVersionUID = 1L;

  private final CommunitySetElemHalfExpr _prefix;

  private final CommunitySetElemHalfExpr _suffix;

  public CommunitySetElem(
      @Nonnull CommunitySetElemHalfExpr prefix, @Nonnull CommunitySetElemHalfExpr suffix) {
    _prefix = prefix;
    _suffix = suffix;
  }

  public CommunitySetElem(long value) {
    int prefixInt = (int) ((value & 0xFFFF0000L) >> 16);
    _prefix = new LiteralCommunitySetElemHalf(prefixInt);
    int suffixInt = (int) (value & 0xFFFFL);
    _suffix = new LiteralCommunitySetElemHalf(suffixInt);
  }
  /*
    public long community() {
      if (_prefix instanceof LiteralCommunitySetElemHalf
          && _suffix instanceof LiteralCommunitySetElemHalf) {
        LiteralCommunitySetElemHalf prefix = (LiteralCommunitySetElemHalf) _prefix;
        LiteralCommunitySetElemHalf suffix = (LiteralCommunitySetElemHalf) _suffix;
        int prefixInt = prefix.getValue();
        int suffixInt = suffix.getValue();
        return (((long) prefixInt) << 16) | suffixInt;
      } else {
        throw new UnsupportedOperationException("Does not represent a single community");
      }
    }
  */
  public CommunitySetElemHalfExpr getPrefix() {
    return _prefix;
  }

  public CommunitySetElemHalfExpr getSuffix() {
    return _suffix;
  }
}
