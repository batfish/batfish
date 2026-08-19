package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;

/** One sequence in an Aruba AOS-CX route map. */
public final class AosCxRouteMapEntry implements Serializable {

  public AosCxRouteMapEntry(long sequence, LineAction action) {
    _sequence = sequence;
    _action = action;
  }

  public long getSequence() {
    return _sequence;
  }

  public LineAction getAction() {
    return _action;
  }

  public @Nullable String getMatchPrefixList() {
    return _matchPrefixList;
  }

  public void setMatchPrefixList(String matchPrefixList) {
    _matchPrefixList = matchPrefixList;
  }

  public @Nullable Long getSetLocalPreference() {
    return _setLocalPreference;
  }

  public void setSetLocalPreference(long localPreference) {
    _setLocalPreference = localPreference;
  }

  private final long _sequence;
  private final LineAction _action;
  private @Nullable String _matchPrefixList;
  private @Nullable Long _setLocalPreference;
}
