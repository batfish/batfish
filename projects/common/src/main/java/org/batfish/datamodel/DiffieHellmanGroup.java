package org.batfish.datamodel;

import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;

public enum DiffieHellmanGroup {
  GROUP1(1),
  GROUP14(14),
  GROUP15(15),
  GROUP16(16),
  GROUP17(17),
  GROUP18(18),
  GROUP19(19),
  GROUP2(2),
  GROUP20(20),
  GROUP21(21),
  GROUP22(22),
  GROUP23(23),
  GROUP24(24),
  GROUP25(25),
  GROUP26(26),
  GROUP5(5);

  private static final Map<Integer, DiffieHellmanGroup> FROM_GROUP_NUMBER_MAP =
      initFromGroupNumberMap();

  public static DiffieHellmanGroup fromGroupNumber(int num) {
    DiffieHellmanGroup dhGroup = FROM_GROUP_NUMBER_MAP.get(num);
    if (dhGroup == null) {
      throw new BatfishException("Invalid Diffie-Hellman group number: " + num);
    } else {
      return dhGroup;
    }
  }

  private static Map<Integer, DiffieHellmanGroup> initFromGroupNumberMap() {
    Map<Integer, DiffieHellmanGroup> ret = new HashMap<>();
    for (DiffieHellmanGroup dhGroup : values()) {
      ret.put(dhGroup._groupNumber, dhGroup);
    }
    return ret;
  }

  private final int _groupNumber;

  DiffieHellmanGroup(int groupNumber) {
    _groupNumber = groupNumber;
  }

  /** Returns the group number of this Diffie-Hellman group. */
  public int getGroupNumber() {
    return _groupNumber;
  }
}
