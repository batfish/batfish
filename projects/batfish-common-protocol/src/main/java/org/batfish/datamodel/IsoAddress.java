package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.io.BaseEncoding;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;

/**
 * First byte - AFI<br>
 * Next 0-12 bytes - Area ID<br>
 * Next 6 bytes - System ID<br>
 * Last byte - NSEL<br>
 *
 * <p>A - AFI<br>
 * B - Area ID<br>
 * C - System ID<br>
 * D - NSEL<br>
 *
 * <p>If odd number of bytes, canonical text format is:<br>
 * AA.(BBBB.)^[0,6].CCCC.CCDD<br>
 *
 * <p>If even number of bytes, canonical text format is:<br>
 * AA.(BBBB.)^[0,5].BBCC.CCCC.DD
 */
public final class IsoAddress implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private static final int SYSTEM_ID_SIZE = 6;

  private static final int AREA_ID_OFFSET = 1;

  private final byte _afi;

  private final byte[] _areaId;

  private final byte _nSel;

  private final byte[] _systemId;

  @JsonCreator
  private static @Nonnull IsoAddress create(String isoAddressStr) {
    return new IsoAddress(requireNonNull(isoAddressStr));
  }

  /**
   * Create an ISO address from hexadecimal digits, optionally interspersed with period (.)
   * characters that are ignored. See {@link IsoAddres} documentation for canonical text format.
   */
  public IsoAddress(@Nonnull String isoAddressStr) {
    String trimmed = isoAddressStr.replaceAll(Pattern.quote("."), "");
    int numChars = trimmed.length();
    if (numChars % 2 != 0 || numChars < 16 || 40 < numChars) {
      throw new BatfishException(
          String.format(
              "Expected an even number of hexadecimal digits representing 8-20 octets, but got: '%s' after trimming: '%s'",
              trimmed, isoAddressStr));
    }
    byte[] all;
    try {
      all = BaseEncoding.base16().decode(trimmed);
    } catch (IllegalArgumentException e) {
      throw new BatfishException(
          String.format(
              "Expected only hexadecimal and period (.) characters, but got: '%s' after trimming '%s'",
              trimmed, isoAddressStr),
          e);
    }
    int numBytes = all.length;
    int nSelOffset = numBytes - 1;
    int systemIdOffset = nSelOffset - SYSTEM_ID_SIZE;
    _afi = all[0];
    _areaId = Arrays.copyOfRange(all, AREA_ID_OFFSET, systemIdOffset);
    _systemId = Arrays.copyOfRange(all, systemIdOffset, nSelOffset);
    _nSel = all[nSelOffset];
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    IsoAddress rhs = (IsoAddress) obj;
    return _afi == rhs._afi
        && Arrays.equals(_areaId, rhs._areaId)
        && _nSel == rhs._nSel
        && Arrays.equals(_systemId, rhs._systemId);
  }

  public byte getAfi() {
    return _afi;
  }

  public @Nonnull byte[] getAreaId() {
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

  public @Nonnull byte[] getSystemId() {
    return _systemId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_afi, _areaId, _nSel, _systemId);
  }

  @Override
  @JsonValue
  public String toString() {
    StringBuilder sb = new StringBuilder("")
    if (_areaId.length %2 == 0) {
      
    }
  }
}
