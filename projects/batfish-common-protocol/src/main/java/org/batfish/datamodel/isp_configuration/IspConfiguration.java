package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Configuration required to create ISPs for a given network snapshot */
public class IspConfiguration {
  private static final String PROP_BORDER_INTERFACES = "borderInterfaces";
  private static final String PROP_FILTER = "filter";

  @Nonnull private final List<BorderInterfaceInfo> _borderInterfaces;
  @Nonnull private final IspFilter _filter;

  public IspConfiguration(
      @Nonnull List<BorderInterfaceInfo> borderInterfaces, @Nonnull IspFilter filter) {
    _borderInterfaces = ImmutableList.copyOf(borderInterfaces);
    _filter = filter;
  }

  @JsonCreator
  private static IspConfiguration jsonCreator(
      @JsonProperty(PROP_BORDER_INTERFACES) @Nullable
          List<BorderInterfaceInfo> borderInterfaceInfos,
      @JsonProperty(PROP_FILTER) @Nullable IspFilter filter) {
    return new IspConfiguration(
        firstNonNull(borderInterfaceInfos, ImmutableList.of()),
        firstNonNull(filter, IspFilter.ALLOW_ALL));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspConfiguration)) {
      return false;
    }
    IspConfiguration that = (IspConfiguration) o;
    return Objects.equals(_borderInterfaces, that._borderInterfaces)
        && Objects.equals(_filter, that._filter);
  }

  @Override
  public int hashCode() {

    return Objects.hash(_borderInterfaces, _filter);
  }

  @JsonProperty(PROP_BORDER_INTERFACES)
  @Nonnull
  public List<BorderInterfaceInfo> getBorderInterfaces() {
    return _borderInterfaces;
  }

  @JsonProperty(PROP_FILTER)
  @Nonnull
  public IspFilter getfilter() {
    return _filter;
  }
}
