package org.batfish.vendor.rtx.representation;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Selects BGP routes received from a remote AS for installation in the local routing table. */
@ParametersAreNonnullByDefault
public final class RtxBgpExportRule implements Serializable {

  public RtxBgpExportRule(long remoteAs, List<Integer> filters) {
    _remoteAs = remoteAs;
    _filters = ImmutableList.copyOf(filters);
  }

  public long getRemoteAs() {
    return _remoteAs;
  }

  public @Nonnull List<Integer> getFilters() {
    return _filters;
  }

  private final long _remoteAs;
  private final @Nonnull List<Integer> _filters;
}
