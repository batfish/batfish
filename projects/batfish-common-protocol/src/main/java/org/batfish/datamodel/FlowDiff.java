package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Comparator.comparing;
import static org.batfish.datamodel.Flow.PROP_DST_IP;
import static org.batfish.datamodel.Flow.PROP_SRC_IP;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A representation of one difference between two flows. */
@JsonTypeName("FlowDiff")
@ParametersAreNonnullByDefault
public final class FlowDiff implements Comparable<FlowDiff> {
  private static final String PROP_FIELD_NAME = "fieldName";
  private static final String PROP_OLD_VALUE = "oldValue";
  private static final String PROP_NEW_VALUE = "newValue";

  /*
   * We require the field names to match the flow field names.
   */
  private static final Set<String> FLOW_DIFF_FIELD_NAMES =
      ImmutableSet.of(PROP_DST_IP, PROP_SRC_IP);

  private final String _fieldName;
  private final String _oldValue;
  private final String _newValue;

  @VisibleForTesting
  FlowDiff(String fieldName, String oldValue, String newValue) {
    checkArgument(FLOW_DIFF_FIELD_NAMES.contains(fieldName), "illegal flow field name");
    checkArgument(!oldValue.equals(newValue), "values must not be equal");
    _fieldName = fieldName;
    _oldValue = oldValue;
    _newValue = newValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FlowDiff)) {
      return false;
    }
    FlowDiff flowDiff = (FlowDiff) o;
    return Objects.equals(_fieldName, flowDiff._fieldName)
        && Objects.equals(_oldValue, flowDiff._oldValue)
        && Objects.equals(_newValue, flowDiff._newValue);
  }

  @JsonCreator
  private static FlowDiff flowDiff(
      @Nullable @JsonProperty(PROP_FIELD_NAME) String fieldName,
      @Nullable @JsonProperty(PROP_OLD_VALUE) String oldValue,
      @Nullable @JsonProperty(PROP_NEW_VALUE) String newValue) {
    checkNotNull(fieldName);
    checkNotNull(oldValue);
    checkNotNull(newValue);
    return new FlowDiff(fieldName, oldValue, newValue);
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

  /** Compute the differences between two flows */
  public static SortedSet<FlowDiff> flowDiffs(@Nullable Flow flow1, @Nullable Flow flow2) {
    if (flow1 == null || flow2 == null || flow1.equals(flow2)) {
      return ImmutableSortedSet.of();
    }

    checkArgument(
        flow1
            .toBuilder()
            .setDstIp(flow2.getDstIp())
            .setSrcIp(flow2.getSrcIp())
            .build()
            .equals(flow2),
        "flowDiff only supports differences of src or dst Ip");

    ImmutableSortedSet.Builder<FlowDiff> diffs = ImmutableSortedSet.naturalOrder();
    if (!flow1.getDstIp().equals(flow2.getDstIp())) {
      diffs.add(
          new FlowDiff(PROP_DST_IP, flow1.getDstIp().toString(), flow2.getDstIp().toString()));
    }
    if (!flow1.getSrcIp().equals(flow2.getSrcIp())) {
      diffs.add(
          new FlowDiff(PROP_SRC_IP, flow1.getSrcIp().toString(), flow2.getSrcIp().toString()));
    }
    return diffs.build();
  }

  @Override
  public int compareTo(FlowDiff o) {
    return comparing(FlowDiff::getFieldName)
        .thenComparing(FlowDiff::getOldValue)
        .thenComparing(FlowDiff::getNewValue)
        .compare(this, o);
  }
}
