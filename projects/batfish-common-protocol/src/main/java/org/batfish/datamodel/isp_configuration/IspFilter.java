package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

public class IspFilter {
  private static final String PROP_ONLY_REMOTE_ASNS = "onlyRemoteAsns";
  private static final String PROP_ONLY_REMOTE_IPS = "onlyRemoteIps";

  @Nonnull private List<Long> _onlyRemoteAsns;
  @Nonnull private List<Ip> _onlyRemoteIps;

  public IspFilter(@Nonnull List<Long> onlyRemoteAsns, @Nonnull List<Ip> onlyRemoteIps) {
    _onlyRemoteAsns = onlyRemoteAsns;
    _onlyRemoteIps = onlyRemoteIps;
  }

  @JsonCreator
  private static IspFilter jsonCreator(
      @JsonProperty(PROP_ONLY_REMOTE_ASNS) @Nullable List<Long> onlyRemoteAsns,
      @JsonProperty(PROP_ONLY_REMOTE_IPS) @Nullable List<Ip> onlyRemoteIps) {
    return new IspFilter(
        firstNonNull(onlyRemoteAsns, ImmutableList.of()),
        firstNonNull(onlyRemoteIps, ImmutableList.of()));
  }

  @JsonProperty(PROP_ONLY_REMOTE_ASNS)
  @Nonnull
  public List<Long> getOnlyRemoteAsns() {
    return _onlyRemoteAsns;
  }

  @JsonProperty(PROP_ONLY_REMOTE_IPS)
  @Nonnull
  public List<Ip> getOnlyRemoteIps() {
    return _onlyRemoteIps;
  }
}
