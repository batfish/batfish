package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.ComparableStructure;

/** An IPV4 firewall zone */
@ParametersAreNonnullByDefault
public final class Zone extends ComparableStructure<String> {
  private static final String PROP_FROM_HOST_FILTER = "fromHostFilter";
  private static final String PROP_INBOUND_FILTER = "inboundFilter";
  private static final String PROP_INBOUND_INTERFACE_FILTERS = "inboundInterfaceFilters";
  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_TO_HOST_FILTER = "toHostFilter";
  private static final String PROP_TO_ZONE_POLICIES = "toZonePolicies";

  private @Nullable String _fromHostFilterName;

  private @Nullable String _inboundFilterName;

  private @Nonnull SortedMap<String, String> _inboundInterfaceFiltersNames;

  private @Nonnull SortedSet<String> _interfaces;

  private @Nullable String _toHostFilterName;

  private @Nonnull SortedMap<String, String> _toZonePoliciesNames;

  @JsonCreator
  public Zone(@JsonProperty(PROP_NAME) String name) {
    super(name);
    _inboundInterfaceFiltersNames = ImmutableSortedMap.of();
    _interfaces = ImmutableSortedSet.of();
    _toZonePoliciesNames = ImmutableSortedMap.of();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Zone)) {
      return false;
    }
    Zone other = (Zone) o;
    return Objects.equals(_fromHostFilterName, other._fromHostFilterName)
        && Objects.equals(_inboundFilterName, other._inboundFilterName)
        && Objects.equals(_interfaces, other._interfaces)
        && Objects.equals(_toHostFilterName, other._toHostFilterName)
        && Objects.equals(_toZonePoliciesNames, other._toZonePoliciesNames);
  }

  /** Filter applied against packets originating from an interface in this zone on this node. */
  @JsonProperty(PROP_FROM_HOST_FILTER)
  public @Nullable String getFromHostFilterName() {
    return _fromHostFilterName;
  }

  /**
   * Filter applied against packets whose final destination is an interface in this zone that does
   * not have its own inbound filter.
   */
  @JsonProperty(PROP_INBOUND_FILTER)
  public @Nullable String getInboundFilterName() {
    return _inboundFilterName;
  }

  /**
   * Mapping of interfaces in this zone to their corresponding inbound filters: the filter applied
   * against packets whose final destination is the interface whose name is the key in this mapping.
   */
  @JsonProperty(PROP_INBOUND_INTERFACE_FILTERS)
  public @Nonnull SortedMap<String, String> getInboundInterfaceFiltersNames() {
    return _inboundInterfaceFiltersNames;
  }

  @JsonProperty(PROP_INTERFACES)
  public @Nonnull SortedSet<String> getInterfaces() {
    return _interfaces;
  }

  /**
   * Filter applied against packets whose final destination is an interface in this zone. If this
   * filter exists, it is applied IN ADDITION to the interface-specific or default inbound filter.
   */
  @JsonProperty(PROP_TO_HOST_FILTER)
  public @Nullable String getToHostFilterName() {
    return _toHostFilterName;
  }

  /**
   * Maps names of destination zones to the corresponding filter applied against packets which are
   * received on this zone and routed to the named zone.
   */
  @JsonProperty(PROP_TO_ZONE_POLICIES)
  public @Nonnull SortedMap<String, String> getToZonePoliciesNames() {
    return _toZonePoliciesNames;
  }

  @JsonProperty(PROP_FROM_HOST_FILTER)
  public void setFromHostFilterName(@Nullable String fromHostFilterName) {
    _fromHostFilterName = fromHostFilterName;
  }

  @JsonProperty(PROP_INBOUND_FILTER)
  public void setInboundFilterName(@Nullable String inboundFilterName) {
    _inboundFilterName = inboundFilterName;
  }

  @JsonProperty(PROP_INBOUND_INTERFACE_FILTERS)
  public void setInboundInterfaceFiltersNames(
      @Nullable SortedMap<String, String> inboundInterfaceFiltersNames) {
    _inboundInterfaceFiltersNames =
        firstNonNull(inboundInterfaceFiltersNames, ImmutableSortedMap.of());
  }

  @JsonProperty(PROP_INTERFACES)
  public void setInterfaces(@Nullable Iterable<String> interfaces) {
    _interfaces = ImmutableSortedSet.copyOf(firstNonNull(interfaces, Collections.emptyList()));
  }

  @JsonProperty(PROP_TO_HOST_FILTER)
  public void setToHostFilterName(@Nullable String toHostFilterName) {
    _toHostFilterName = toHostFilterName;
  }

  @JsonProperty(PROP_TO_ZONE_POLICIES)
  public void setToZonePoliciesNames(@Nullable SortedMap<String, String> toZonePoliciesNames) {
    _toZonePoliciesNames = firstNonNull(toZonePoliciesNames, ImmutableSortedMap.of());
  }
}
