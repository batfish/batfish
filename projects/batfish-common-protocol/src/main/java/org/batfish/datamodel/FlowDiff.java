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
import org.batfish.common.BatfishException;
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
      @Nullable @JsonProperty(PROP_FIELD_NAME) String fieldName,
      @Nullable @JsonProperty(PROP_OLD_VALUE) String oldValue,
      @Nullable @JsonProperty(PROP_NEW_VALUE) String newValue) {
    checkNotNull(fieldName);
    checkNotNull(oldValue);
    checkNotNull(newValue);
    return new FlowDiff(fieldName, oldValue, newValue);
  }

  private static String portFieldName(PortField portField) {
    switch (portField) {
      case DESTINATION:
        return PROP_DST_PORT;
      case SOURCE:
        return PROP_SRC_PORT;
      default:
        throw new BatfishException("Unknown PortField: " + portField);
    }
  }

  private static String ipFieldName(IpField ipField) {
    switch (ipField) {
      case DESTINATION:
        return PROP_DST_IP;
      case SOURCE:
        return PROP_SRC_IP;
      default:
        throw new BatfishException("Unknown IpField: " + ipField);
    }
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
            .setDstPort(flow2.getDstPort())
            .setSrcPort(flow2.getSrcPort())
            .build()
            .equals(flow2),
        "flowDiff only supports differences of src/dst Ip and src/dst Port");

    ImmutableSortedSet.Builder<FlowDiff> diffs = ImmutableSortedSet.naturalOrder();
    if (flow1.getDstPort() != flow2.getDstPort()) {
      diffs.add(
          new FlowDiff(
              PROP_DST_PORT,
              Integer.toString(flow1.getDstPort()),
              Integer.toString(flow2.getDstPort())));
    }
    if (flow1.getSrcPort() != flow2.getSrcPort()) {
      diffs.add(
          new FlowDiff(
              PROP_SRC_PORT,
              Integer.toString(flow1.getSrcPort()),
              Integer.toString(flow2.getSrcPort())));
    }
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
