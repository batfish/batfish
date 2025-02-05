package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.SubRange;

/** Entry serving as an access-control line in a prefix-list */
@ParametersAreNonnullByDefault
public final class PrefixListEntry implements Serializable {

  private @Nullable LineAction _action;

  private @Nullable SubRange _lengthRange;

  private final long _num;

  private @Nullable Prefix _prefix;

  private @Nullable Prefix6 _prefix6;

  public PrefixListEntry(long num) {
    _num = num;
  }

  private boolean checkValidIpv4LengthRange(Warnings w, String listName) {
    if (_lengthRange == null) {
      return false;
    }
    if (_lengthRange.getStart() < 0 || _lengthRange.getEnd() > Prefix.MAX_PREFIX_LENGTH) {
      w.redFlagf(
          "Invalid IPv4 prefix-len-range '%d:%d' in prefix-list '%s' entry '%d'",
          _lengthRange.getStart(), _lengthRange.getEnd(), listName, _num);
      return false;
    }
    return true;
  }

  public @Nullable LineAction getAction() {
    return _action;
  }

  public @Nullable SubRange getLengthRange() {
    return _lengthRange;
  }

  public long getNum() {
    return _num;
  }

  public @Nullable Prefix getPrefix() {
    return _prefix;
  }

  public @Nullable Prefix6 getPrefix6() {
    return _prefix6;
  }

  public void setAction(@Nullable LineAction action) {
    _action = action;
  }

  public void setLengthRange(@Nullable SubRange lengthRange) {
    _lengthRange = lengthRange;
  }

  public void setPrefix(@Nullable Prefix prefix) {
    _prefix = prefix;
  }

  public void setPrefix6(@Nullable Prefix6 prefix6) {
    _prefix6 = prefix6;
  }

  /** Convert to a {@link RouteFilterLine}. If action or prefix is missing, return {@code null}. */
  public @Nullable RouteFilterLine toRouteFilterLine(Warnings w, String listName) {
    if (_action == null) {
      w.redFlagf("Missing action in prefix-list '%s' entry '%d'", listName, _num);
      return null;
    }
    if (_prefix == null) {
      w.redFlagf("Missing IPv4 prefix in prefix-list '%s' entry '%d'", listName, _num);
      return null;
    }
    return new RouteFilterLine(
        _action,
        _prefix,
        checkValidIpv4LengthRange(w, listName)
            ? _lengthRange
            : new SubRange(_prefix.getPrefixLength(), Prefix.MAX_PREFIX_LENGTH));
  }
}
