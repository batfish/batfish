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

  private static final String PROP_BORDER_INTERFACE_INFOS = "borderInterfaceInfos";
  private static final String PROP_ISP_FILTER = "ispFilter";

  @Nonnull private List<BorderInterfaceInfo> _borderInterfaceInfos;
  @Nonnull private IspFilter _ispFilter;

  public IspConfiguration(
      @Nonnull List<BorderInterfaceInfo> borderInterfaceInfos, @Nonnull IspFilter ispFilter) {
    _borderInterfaceInfos = borderInterfaceInfos;
    _ispFilter = ispFilter;
  }

  @JsonCreator
  private static IspConfiguration jsonCreator(
      @JsonProperty(PROP_BORDER_INTERFACE_INFOS) @Nullable
          List<BorderInterfaceInfo> borderInterfaceInfos,
      @JsonProperty(PROP_ISP_FILTER) @Nullable IspFilter ispFilter) {
    return new IspConfiguration(
        firstNonNull(borderInterfaceInfos, ImmutableList.of()),
        firstNonNull(ispFilter, new IspFilter(ImmutableList.of(), ImmutableList.of())));
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
    return Objects.equals(_borderInterfaceInfos, that._borderInterfaceInfos)
        && Objects.equals(_ispFilter, that._ispFilter);
  }

  @Override
  public int hashCode() {

    return Objects.hash(_borderInterfaceInfos, _ispFilter);
  }

  @JsonProperty(PROP_BORDER_INTERFACE_INFOS)
  @Nonnull
  public List<BorderInterfaceInfo> getBorderInterfaceInfos() {
    return _borderInterfaceInfos;
  }

  @JsonProperty(PROP_ISP_FILTER)
  @Nonnull
  public IspFilter getIspFilter() {
    return _ispFilter;
  }
}
