package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Specification for a filter on top of ISPs and BGP peerings created through {@link
 * IspConfiguration}. The filter allows limiting modeled nodes and peerings only to those that match
 * whitelisted ASNs and IPs.
 */
public class IspFilter {
  public static final IspFilter ALLOW_ALL = new IspFilter(ImmutableList.of(), ImmutableList.of());
  private static final String PROP_ONLY_REMOTE_ASNS = "onlyRemoteAsns";
  private static final String PROP_ONLY_REMOTE_IPS = "onlyRemoteIps";

  private final @Nonnull List<Long> _onlyRemoteAsns;
  private final @Nonnull List<Ip> _onlyRemoteIps;

  public IspFilter(@Nonnull List<Long> onlyRemoteAsns, @Nonnull List<Ip> onlyRemoteIps) {
    _onlyRemoteAsns = ImmutableList.copyOf(onlyRemoteAsns);
    _onlyRemoteIps = ImmutableList.copyOf(onlyRemoteIps);
  }

  @JsonCreator
  private static IspFilter jsonCreator(
      @JsonProperty(PROP_ONLY_REMOTE_ASNS) @Nullable List<Long> onlyRemoteAsns,
      @JsonProperty(PROP_ONLY_REMOTE_IPS) @Nullable List<Ip> onlyRemoteIps) {
    return new IspFilter(
        firstNonNull(onlyRemoteAsns, ImmutableList.of()),
        firstNonNull(onlyRemoteIps, ImmutableList.of()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspFilter)) {
      return false;
    }
    IspFilter ispFilter = (IspFilter) o;
    return Objects.equals(_onlyRemoteAsns, ispFilter._onlyRemoteAsns)
        && Objects.equals(_onlyRemoteIps, ispFilter._onlyRemoteIps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_onlyRemoteAsns, _onlyRemoteIps);
  }

  @JsonProperty(PROP_ONLY_REMOTE_ASNS)
  public @Nonnull List<Long> getOnlyRemoteAsns() {
    return _onlyRemoteAsns;
  }

  @JsonProperty(PROP_ONLY_REMOTE_IPS)
  public @Nonnull List<Ip> getOnlyRemoteIps() {
    return _onlyRemoteIps;
  }
}
