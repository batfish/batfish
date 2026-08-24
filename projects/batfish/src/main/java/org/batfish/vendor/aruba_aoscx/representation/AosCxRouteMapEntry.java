package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.ospf.OspfMetricType;

/** One sequence in an Aruba AOS-CX route map. */
public final class AosCxRouteMapEntry
    implements Serializable {

  public AosCxRouteMapEntry(
      long sequence,
      LineAction action) {
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

  public void setMatchPrefixList(
      String matchPrefixList) {
    _matchPrefixList = matchPrefixList;
  }

  public @Nullable String getMatchIpv6PrefixList() {
    return _matchIpv6PrefixList;
  }

  public void setMatchIpv6PrefixList(
      String matchIpv6PrefixList) {
    _matchIpv6PrefixList =
        matchIpv6PrefixList;
  }

  public @Nullable Long getMatchTag() {
    return _matchTag;
  }

  public void setMatchTag(long tag) {
    _matchTag =
        tag;
  }

  public void clearMatchTag(long tag) {

    if (_matchTag != null
        && _matchTag.longValue() == tag) {

      _matchTag =
          null;
    }
  }

  public @Nullable Long getSetLocalPreference() {
    return _setLocalPreference;
  }

  public void setSetLocalPreference(
      long localPreference) {
    _setLocalPreference =
        localPreference;
  }

  public @Nullable Long getSetMetric() {
    return _setMetric;
  }

  public void setSetMetric(long metric) {
    _setMetric = metric;
  }

  public @Nullable OspfMetricType getSetOspfMetricType() {
    return _setOspfMetricType;
  }

  public void setSetOspfMetricType(
      OspfMetricType ospfMetricType) {
    _setOspfMetricType = ospfMetricType;
  }

  public @Nullable Long getSetTag() {
    return _setTag;
  }

  public void setSetTag(long tag) {
    _setTag = tag;
  }

  private final long _sequence;
  private final LineAction _action;
  private @Nullable String _matchPrefixList;
  private @Nullable String _matchIpv6PrefixList;
  private @Nullable Long _matchTag;
  private @Nullable Long _setLocalPreference;
  private @Nullable Long _setMetric;
  private @Nullable OspfMetricType _setOspfMetricType;
  private @Nullable Long _setTag;
}
