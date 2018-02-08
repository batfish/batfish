package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.io.BaseEncoding;
import java.io.Serializable;
import java.math.BigInteger;

public final class IsoAddress implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final byte _afi;

  private final BigInteger _areaId;

  private final byte _nSel;

  private final String _str;

  private final long _systemId;

  @JsonCreator
  public IsoAddress(String isoAddressStr) {
    _str = isoAddressStr;
    String[] parts = isoAddressStr.split("\\.", -1);
    int areaEndOffset = parts.length - 5;
    BigInteger areaId = BigInteger.ZERO;
    for (int i = areaEndOffset, shift = 0; i >= 1; i--) {
      int currentIntVal = Integer.parseInt(parts[i], 16);
      int currentNumBits = parts[i].length() * 4;
      areaId = areaId.add(BigInteger.valueOf(currentIntVal).shiftLeft(shift));
      shift += currentNumBits;
    }
    long systemId = 0L;
    for (int i = parts.length - 2, shift = 0; i >= parts.length - 4; i--) {
      long currentLongVal = Long.parseLong(parts[i], 16);
      int currentNumBits = parts[i].length() * 4;
      systemId += (currentLongVal << shift);
      shift += currentNumBits;
    }
    _systemId = systemId;
    _afi = BaseEncoding.base16().decode(parts[0])[0];
    _areaId = areaId;
    _nSel = BaseEncoding.base16().decode(parts[parts.length - 1])[0];
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    IsoAddress other = (IsoAddress) obj;
    if (_afi != other._afi) {
      return false;
    }
    if (!_areaId.equals(other._areaId)) {
      return false;
    }
    if (_nSel != other._nSel) {
      return false;
    }
    if (_systemId != other._systemId) {
      return false;
    }
    return true;
  }

  public byte getAfi() {
    return _afi;
  }

  public BigInteger getAreaId() {
    return _areaId;
  }

  public String getAreaIdStr() {
    String areaIdStr = _areaId.toString(16);
    int leadingZeros = (4 - (areaIdStr.length() % 4) % 4);
    for (int i = 0; i < leadingZeros; i++) {
      areaIdStr = "0" + areaIdStr;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < areaIdStr.length(); i += 4) {
      String currentPart = areaIdStr.substring(i, i + 4);
      sb.append(currentPart + ".");
    }
    String ret = sb.substring(0, sb.length() - 1);
    return ret;
  }

  public byte getNSelector() {
    return _nSel;
  }

  public long getSystemId() {
    return _systemId;
  }

  public String getSystemIdStr() {
    String systemIdStr = Long.toHexString(_systemId);
    int leadingZeros = 12 - systemIdStr.length();
    for (int i = 0; i < leadingZeros; i++) {
      systemIdStr = "0" + systemIdStr;
    }
    String[] parts = new String[3];
    parts[0] = systemIdStr.substring(0, 4);
    parts[1] = systemIdStr.substring(4, 8);
    parts[2] = systemIdStr.substring(8, 12);
    return parts[0] + "." + parts[1] + "." + parts[2];
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _afi;
    result = prime * result + _areaId.hashCode();
    result = prime * result + _nSel;
    result = prime * result + (int) (_systemId ^ (_systemId >>> 32));
    return result;
  }

  @Override
  @JsonValue
  public String toString() {
    return _str;
  }
}
