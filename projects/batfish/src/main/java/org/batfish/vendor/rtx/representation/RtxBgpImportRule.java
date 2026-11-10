package org.batfish.vendor.rtx.representation;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Selects local routes to import into BGP for advertisement to a remote AS. */
@ParametersAreNonnullByDefault
public final class RtxBgpImportRule implements Serializable {

  public enum Protocol {
    BGP,
    STATIC
  }

  public RtxBgpImportRule(
      long remoteAs, Protocol protocol, @Nullable Long fromAs, List<Integer> filters) {
    _remoteAs = remoteAs;
    _protocol = protocol;
    _fromAs = fromAs;
    _filters = ImmutableList.copyOf(filters);
  }

  public long getRemoteAs() {
    return _remoteAs;
  }

  public @Nonnull Protocol getProtocol() {
    return _protocol;
  }

  /** Source AS for a BGP route; null for a static route. */
  public @Nullable Long getFromAs() {
    return _fromAs;
  }

  public @Nonnull List<Integer> getFilters() {
    return _filters;
  }

  private final long _remoteAs;
  private final @Nonnull Protocol _protocol;
  private final @Nullable Long _fromAs;
  private final @Nonnull List<Integer> _filters;
}
