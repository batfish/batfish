package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.BaseEncoding;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

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
 * AA.(BBBB.)^[0,5].BBCC.CCCC.CCCC.CCDD<br>
 *
 * <p>If even number of bytes, canonical text format is:<br>
 * AA.(BBBB.)^[0,6].CCCC.CCCC.CCCC.DD
 */
public final class IsoAddress implements Serializable {

  private static final int AREA_ID_OFFSET = 1;

  private static final int SYSTEM_ID_SIZE = 6;

  @JsonCreator
  private static @Nonnull IsoAddress create(String isoAddressStr) {
    return new IsoAddress(requireNonNull(isoAddressStr));
  }

  @VisibleForTesting
  static @Nonnull String invalidCharsMessage(
      @Nonnull String isoAddressStr, @Nonnull String trimmed) {
    return String.format(
        "Expected only hexadecimal and period (.) characters, but got: '%s' after trimming '%s'",
        trimmed, isoAddressStr);
  }

  @VisibleForTesting
  static @Nonnull String invalidLengthMessage(
      @Nonnull String isoAddressStr, @Nonnull String trimmed) {
    return String.format(
        "Expected an even number of hexadecimal digits representing 8-20 octets, but got: '%s'"
            + " after trimming: '%s'",
        trimmed, isoAddressStr);
  }

  @VisibleForTesting
  static @Nonnull String trim(@Nonnull String isoAddressStr) {
    return isoAddressStr.replaceAll(Pattern.quote("."), "");
  }

  private final byte _afi;

  private final byte[] _areaId;

  private final byte _nSel;

  private final byte[] _systemId;

  /**
   * Create an ISO address from hexadecimal digits, optionally interspersed with period (.)
   * characters that are ignored. See {@link IsoAddress} documentation for canonical text format.
   */
  public IsoAddress(@Nonnull String isoAddressStr) {
    String trimmed = trim(isoAddressStr);

    int numChars = trimmed.length();
    if (numChars % 2 != 0 || numChars < 16 || 40 < numChars) {
      throw new IllegalArgumentException(invalidLengthMessage(isoAddressStr, trimmed));
    }
    byte[] all;
    try {
      all = BaseEncoding.base16().decode(trimmed.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(invalidCharsMessage(isoAddressStr, trimmed), e);
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
    } else if (!(obj instanceof IsoAddress)) {
      return false;
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

  public @Nonnull String getAreaIdString() {
    StringBuilder sb = new StringBuilder();
    for (byte b : _areaId) {
      sb.append(String.format("%02X", b));
    }
    return sb.toString();
  }

  public byte getNSelector() {
    return _nSel;
  }

  public @Nonnull byte[] getSystemId() {
    return _systemId;
  }

  public @Nonnull String getSystemIdString() {
    StringBuilder sb = new StringBuilder();
    for (byte b : _systemId) {
      sb.append(String.format("%02X", b));
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(_afi, Arrays.hashCode(_areaId), _nSel, Arrays.hashCode(_systemId));
  }

  @Override
  @JsonValue
  public String toString() {
    // AA
    StringBuilder sb = new StringBuilder(String.format("%02X", _afi));
    if ((_areaId.length & 0x1) == 0) {
      // even number of bytes
      for (int i = 0; i < _areaId.length; i += 2) {
        // .BBBB
        sb.append(String.format(".%02X%02X", _areaId[i], _areaId[i + 1]));
      }
      for (int i = 0; i < SYSTEM_ID_SIZE; i += 2) {
        // .CCCC
        sb.append(String.format(".%02X%02X", _systemId[i], _systemId[i + 1]));
      }
      // .DD
      sb.append(String.format(".%02X", _nSel));
    } else {
      // odd number of bytes
      int areaIdOffset = 0;
      for (areaIdOffset = 0; areaIdOffset < _areaId.length - 1; areaIdOffset += 2) {
        // .BBBB
        sb.append(String.format(".%02X%02X", _areaId[areaIdOffset], _areaId[areaIdOffset + 1]));
      }
      // .BB
      sb.append(String.format(".%02X", _areaId[areaIdOffset]));
      // CC
      sb.append(String.format("%02X", _systemId[0]));
      for (int i = 1; i < SYSTEM_ID_SIZE - 2; i += 2) {
        // .CCCC
        sb.append(String.format(".%02X%02X", _systemId[i], _systemId[i + 1]));
      }
      // .CC
      sb.append(String.format(".%02X", _systemId[SYSTEM_ID_SIZE - 1]));
      // DD
      sb.append(String.format("%02X", _nSel));
    }
    return sb.toString();
  }
}
