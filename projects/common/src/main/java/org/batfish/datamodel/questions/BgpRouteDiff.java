package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Ordering.natural;
import static java.util.Comparator.comparing;
import static org.batfish.datamodel.questions.BgpRoute.PROP_ADMINISTRATIVE_DISTANCE;
import static org.batfish.datamodel.questions.BgpRoute.PROP_AS_PATH;
import static org.batfish.datamodel.questions.BgpRoute.PROP_COMMUNITIES;
import static org.batfish.datamodel.questions.BgpRoute.PROP_LOCAL_PREFERENCE;
import static org.batfish.datamodel.questions.BgpRoute.PROP_METRIC;
import static org.batfish.datamodel.questions.BgpRoute.PROP_NEXT_HOP;
import static org.batfish.datamodel.questions.BgpRoute.PROP_ORIGINATOR_IP;
import static org.batfish.datamodel.questions.BgpRoute.PROP_ORIGIN_TYPE;
import static org.batfish.datamodel.questions.BgpRoute.PROP_TAG;
import static org.batfish.datamodel.questions.BgpRoute.PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE;
import static org.batfish.datamodel.questions.BgpRoute.PROP_WEIGHT;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.Community;

/** A representation of one difference between two routes. */
@ParametersAreNonnullByDefault
public final class BgpRouteDiff implements Comparable<BgpRouteDiff> {
  private static final String PROP_FIELD_NAME = "fieldName";
  private static final String PROP_OLD_VALUE = "oldValue";
  private static final String PROP_NEW_VALUE = "newValue";

  /*
   * We require the field names to match the route field names.
   */
  private static final Set<String> ROUTE_DIFF_FIELD_NAMES =
      ImmutableSet.of(
          PROP_ADMINISTRATIVE_DISTANCE,
          PROP_AS_PATH,
          PROP_COMMUNITIES,
          PROP_LOCAL_PREFERENCE,
          PROP_METRIC,
          PROP_NEXT_HOP,
          PROP_ORIGINATOR_IP,
          PROP_ORIGIN_TYPE,
          PROP_TAG,
          PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE,
          PROP_WEIGHT);

  private final String _fieldName;
  private final String _oldValue;
  private final String _newValue;

  public BgpRouteDiff(String fieldName, String oldValue, String newValue) {
    checkArgument(
        ROUTE_DIFF_FIELD_NAMES.contains(fieldName),
        "fieldName must be one of " + ROUTE_DIFF_FIELD_NAMES);
    checkArgument(!oldValue.equals(newValue), "oldValue and newValue must be different");
    _fieldName = fieldName;
    _oldValue = oldValue;
    _newValue = newValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpRouteDiff)) {
      return false;
    }
    BgpRouteDiff diff = (BgpRouteDiff) o;
    return Objects.equals(_fieldName, diff._fieldName)
        && Objects.equals(_oldValue, diff._oldValue)
        && Objects.equals(_newValue, diff._newValue);
  }

  @JsonCreator
  private static BgpRouteDiff jsonCreator(
      @JsonProperty(PROP_FIELD_NAME) @Nullable String fieldName,
      @JsonProperty(PROP_OLD_VALUE) @Nullable String oldValue,
      @JsonProperty(PROP_NEW_VALUE) @Nullable String newValue) {
    checkNotNull(fieldName);
    checkNotNull(oldValue);
    checkNotNull(newValue);
    return new BgpRouteDiff(fieldName, oldValue, newValue);
  }

  @JsonProperty(PROP_FIELD_NAME)
  public String getFieldName() {
    return _fieldName;
  }

  @JsonProperty(PROP_OLD_VALUE)
  public String getOldValue() {
    return _oldValue;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_fieldName, _oldValue, _newValue);
  }

  @JsonProperty(PROP_NEW_VALUE)
  public String getNewValue() {
    return _newValue;
  }

  /** Compute the differences between two routes */
  public static BgpRouteDiffs routeDiffs(@Nullable BgpRoute route1, @Nullable BgpRoute route2) {
    return structuredRouteDiffs(route1, route2).toBgpRouteDiffs();
  }

  /** Compute the differences between two routes */
  public static StructuredBgpRouteDiffs structuredRouteDiffs(
      @Nullable BgpRoute route1, @Nullable BgpRoute route2) {
    if (route1 == null || route2 == null || route1.equals(route2)) {
      return new StructuredBgpRouteDiffs();
    }

    checkArgument(
        route1.toBuilder()
            .setAdminDist(route2.getAdminDist())
            .setAsPath(route2.getAsPath())
            .setCommunities(route2.getCommunities())
            .setLocalPreference(route2.getLocalPreference())
            .setMetric(route2.getMetric())
            .setNextHop(route2.getNextHop())
            .setOriginatorIp(route2.getOriginatorIp())
            .setOriginType(route2.getOriginType())
            .setTag(route2.getTag())
            .setTunnelEncapsulationAttribute(route2.getTunnelEncapsulationAttribute())
            .setWeight(route2.getWeight())
            .build()
            .equals(route2),
        "routeDiffs only supports differences of fields: %s, not %s vs %s",
        ROUTE_DIFF_FIELD_NAMES,
        route1,
        route2);

    return new StructuredBgpRouteDiffs(
        Stream.of(
                routeDiff(route1, route2, PROP_ADMINISTRATIVE_DISTANCE, BgpRoute::getAdminDist),
                routeDiff(route1, route2, PROP_AS_PATH, BgpRoute::getAsPath),
                routeDiff(route1, route2, PROP_LOCAL_PREFERENCE, BgpRoute::getLocalPreference),
                routeDiff(route1, route2, PROP_METRIC, BgpRoute::getMetric),
                routeDiff(route1, route2, PROP_NEXT_HOP, BgpRoute::getNextHop),
                routeDiff(route1, route2, PROP_ORIGINATOR_IP, BgpRoute::getOriginatorIp),
                routeDiff(route1, route2, PROP_ORIGIN_TYPE, BgpRoute::getOriginType),
                routeDiff(route1, route2, PROP_TAG, BgpRoute::getTag),
                routeDiff(
                    route1,
                    route2,
                    PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE,
                    BgpRoute::getTunnelEncapsulationAttribute),
                routeDiff(route1, route2, PROP_WEIGHT, BgpRoute::getWeight))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(ImmutableSortedSet.toImmutableSortedSet(natural())),
        communityRouteDiff(route1, route2, BgpRoute::getCommunities));
  }

  private static Optional<BgpRouteDiff> routeDiff(
      BgpRoute route1, BgpRoute route2, String name, Function<BgpRoute, Object> getter) {
    Object o1 = getter.apply(route1);
    Object o2 = getter.apply(route2);
    return Objects.equals(o1, o2)
        ? Optional.empty()
        : Optional.of(
            new BgpRouteDiff(
                name, o1 == null ? "null" : o1.toString(), o2 == null ? "null" : o2.toString()));
  }

  private static Optional<BgpRouteCommunityDiff> communityRouteDiff(
      BgpRoute route1, BgpRoute route2, Function<BgpRoute, SortedSet<Community>> getter) {
    SortedSet<Community> cs1 = getter.apply(route1);
    SortedSet<Community> cs2 = getter.apply(route2);
    return cs1.equals(cs2) ? Optional.empty() : Optional.of(new BgpRouteCommunityDiff(cs1, cs2));
  }

  @Override
  public int compareTo(BgpRouteDiff that) {
    return comparing(BgpRouteDiff::getFieldName)
        .thenComparing(BgpRouteDiff::getOldValue)
        .thenComparing(BgpRouteDiff::getNewValue)
        .compare(this, that);
  }
}
