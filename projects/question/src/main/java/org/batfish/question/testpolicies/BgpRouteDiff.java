package org.batfish.question.testpolicies;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Comparator.comparing;
import static org.batfish.datamodel.BgpRoute.PROP_AS_PATH;
import static org.batfish.datamodel.BgpRoute.PROP_COMMUNITIES;
import static org.batfish.datamodel.BgpRoute.PROP_LOCAL_PREFERENCE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;

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
      ImmutableSet.of(PROP_AS_PATH, BgpRoute.PROP_COMMUNITIES, BgpRoute.PROP_LOCAL_PREFERENCE);

  private final String _fieldName;
  private final String _oldValue;
  private final String _newValue;

  public BgpRouteDiff(String fieldName, String oldValue, String newValue) {
    checkArgument(
        ROUTE_DIFF_FIELD_NAMES.contains(fieldName),
        "fieldName must be one of " + ROUTE_DIFF_FIELD_NAMES);
    checkArgument(!oldValue.equals(newValue), "oldValue and newValule must be different");
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
      @Nullable @JsonProperty(PROP_FIELD_NAME) String fieldName,
      @Nullable @JsonProperty(PROP_OLD_VALUE) String oldValue,
      @Nullable @JsonProperty(PROP_NEW_VALUE) String newValue) {
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
  public static SortedSet<BgpRouteDiff> routeDiffs(
      @Nullable BgpRoute route1, @Nullable BgpRoute route2) {
    if (route1 == null || route2 == null || route1.equals(route2)) {
      return ImmutableSortedSet.of();
    }

    checkArgument(
        route1
            .toBuilder()
            .setAsPath(route2.getAsPath())
            .setCommunities(route2.getCommunities())
            .setLocalPreference(route2.getLocalPreference())
            .build()
            .equals(route2),
        "routeDiffs only supports differences of fields: " + ROUTE_DIFF_FIELD_NAMES);

    ImmutableSortedSet.Builder<BgpRouteDiff> diffs = ImmutableSortedSet.naturalOrder();
    if (!route1.getAsPath().equals(route2.getAsPath())) {
      diffs.add(
          new BgpRouteDiff(
              PROP_AS_PATH, route1.getAsPath().toString(), route2.getAsPath().toString()));
    }
    if (!route1.getCommunities().equals(route2.getCommunities())) {
      diffs.add(
          new BgpRouteDiff(
              PROP_COMMUNITIES,
              route1.getCommunities().toString(),
              route2.getCommunities().toString()));
    }
    if (route1.getLocalPreference() != route2.getLocalPreference()) {
      diffs.add(
          new BgpRouteDiff(
              PROP_LOCAL_PREFERENCE,
              Long.toString(route1.getLocalPreference()),
              Long.toString(route2.getLocalPreference())));
    }
    return diffs.build();
  }

  @Override
  public int compareTo(BgpRouteDiff that) {
    return comparing(BgpRouteDiff::getFieldName)
        .thenComparing(BgpRouteDiff::getOldValue)
        .thenComparing(BgpRouteDiff::getNewValue)
        .compare(this, that);
  }
}
