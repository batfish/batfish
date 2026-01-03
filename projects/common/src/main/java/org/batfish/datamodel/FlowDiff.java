package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Comparator.comparing;
import static org.batfish.datamodel.Flow.PROP_DST_IP;
import static org.batfish.datamodel.Flow.PROP_DST_PORT;
import static org.batfish.datamodel.Flow.PROP_SRC_IP;
import static org.batfish.datamodel.Flow.PROP_SRC_PORT;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.PortField;

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
      ImmutableSet.of(PROP_DST_IP, PROP_SRC_IP, PROP_DST_PORT, PROP_SRC_PORT);

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
      @JsonProperty(PROP_FIELD_NAME) @Nullable String fieldName,
      @JsonProperty(PROP_OLD_VALUE) @Nullable String oldValue,
      @JsonProperty(PROP_NEW_VALUE) @Nullable String newValue) {
    checkNotNull(fieldName);
    checkNotNull(oldValue);
    checkNotNull(newValue);
    return new FlowDiff(fieldName, oldValue, newValue);
  }

  private static String portFieldName(PortField portField) {
    return switch (portField) {
      case DESTINATION -> PROP_DST_PORT;
      case SOURCE -> PROP_SRC_PORT;
    };
  }

  private static String ipFieldName(IpField ipField) {
    return switch (ipField) {
      case DESTINATION -> PROP_DST_IP;
      case SOURCE -> PROP_SRC_IP;
    };
  }

  /** Create a {@link FlowDiff} for a specific changed IpField. */
  public static FlowDiff flowDiff(IpField ipField, Ip oldValue, Ip newValue) {
    return new FlowDiff(ipFieldName(ipField), oldValue.toString(), newValue.toString());
  }

  /** Create a {@link FlowDiff} for a specific changed PortField. */
  public static FlowDiff flowDiff(PortField portField, int oldValue, int newValue) {
    return new FlowDiff(
        portFieldName(portField), Integer.toString(oldValue), Integer.toString(newValue));
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

  /**
   * Helper method for flowDiffs and returnFlowDiffs, creates FlowDiffs given source and destination
   * IPs and ports of two flows
   */
  private static SortedSet<FlowDiff> getFlowDiffs(
      Ip srcIp1,
      Ip dstIp1,
      @Nullable Integer srcPort1,
      @Nullable Integer dstPort1,
      Ip srcIp2,
      Ip dstIp2,
      @Nullable Integer srcPort2,
      @Nullable Integer dstPort2) {
    ImmutableSortedSet.Builder<FlowDiff> diffs = ImmutableSortedSet.naturalOrder();
    if (!Objects.equals(dstPort1, dstPort2)) {
      assert dstPort1 != null && dstPort2 != null;
      diffs.add(
          new FlowDiff(PROP_DST_PORT, Integer.toString(dstPort1), Integer.toString(dstPort2)));
    }
    if (!Objects.equals(srcPort1, srcPort2)) {
      assert srcPort1 != null && srcPort2 != null;
      diffs.add(
          new FlowDiff(PROP_SRC_PORT, Integer.toString(srcPort1), Integer.toString(srcPort2)));
    }
    if (!dstIp1.equals(dstIp2)) {
      diffs.add(new FlowDiff(PROP_DST_IP, dstIp1.toString(), dstIp2.toString()));
    }
    if (!srcIp1.equals(srcIp2)) {
      diffs.add(new FlowDiff(PROP_SRC_IP, srcIp1.toString(), srcIp2.toString()));
    }
    return diffs.build();
  }

  /** Compute the differences between two flows */
  public static SortedSet<FlowDiff> flowDiffs(@Nullable Flow flow1, @Nullable Flow flow2) {
    if (flow1 == null || flow2 == null || flow1.equals(flow2)) {
      return ImmutableSortedSet.of();
    }

    checkArgument(
        flow1.toBuilder()
            .setDstIp(flow2.getDstIp())
            .setSrcIp(flow2.getSrcIp())
            .setDstPort(flow2.getDstPort())
            .setSrcPort(flow2.getSrcPort())
            .build()
            .equals(flow2),
        "flowDiff only supports differences of src/dst Ip and src/dst Port");

    return getFlowDiffs(
        flow1.getSrcIp(),
        flow1.getDstIp(),
        flow1.getSrcPort(),
        flow1.getDstPort(),
        flow2.getSrcIp(),
        flow2.getDstIp(),
        flow2.getSrcPort(),
        flow2.getDstPort());
  }

  /** Compute expected flow diffs of return flows */
  public static SortedSet<FlowDiff> returnFlowDiffs(
      @Nullable Flow origForwardFlow, @Nullable Flow transformedForwardFlow) {
    if (origForwardFlow == null
        || transformedForwardFlow == null
        || origForwardFlow.equals(transformedForwardFlow)) {
      return ImmutableSortedSet.of();
    }
    checkArgument(
        origForwardFlow.toBuilder()
            .setDstIp(transformedForwardFlow.getDstIp())
            .setSrcIp(transformedForwardFlow.getSrcIp())
            .setDstPort(transformedForwardFlow.getDstPort())
            .setSrcPort(transformedForwardFlow.getSrcPort())
            .build()
            .equals(transformedForwardFlow),
        "returnFlowDiffs only supports differences of src/dst Ip and src/dst Port");
    return getFlowDiffs(
        transformedForwardFlow.getDstIp(),
        transformedForwardFlow.getSrcIp(),
        transformedForwardFlow.getDstPort(),
        transformedForwardFlow.getSrcPort(),
        origForwardFlow.getDstIp(),
        origForwardFlow.getSrcIp(),
        origForwardFlow.getDstPort(),
        origForwardFlow.getSrcPort());
  }

  /** Returns {@link PortField} corresponding to field name if applicable, or else {@code null}. */
  @JsonIgnore
  public @Nullable PortField getPortField() {
    switch (_fieldName) {
      case Flow.PROP_DST_PORT:
        return PortField.DESTINATION;
      case Flow.PROP_SRC_PORT:
        return PortField.SOURCE;
      default:
        return null;
    }
  }

  /** Returns {@link IpField} corresponding to field name if applicable, or else {@code null}. */
  @JsonIgnore
  public @Nullable IpField getIpField() {
    switch (_fieldName) {
      case Flow.PROP_DST_IP:
        return IpField.DESTINATION;
      case Flow.PROP_SRC_IP:
        return IpField.SOURCE;
      default:
        return null;
    }
  }

  @Override
  public int compareTo(FlowDiff o) {
    return comparing(FlowDiff::getFieldName)
        .thenComparing(FlowDiff::getOldValue)
        .thenComparing(FlowDiff::getNewValue)
        .compare(this, o);
  }
}
