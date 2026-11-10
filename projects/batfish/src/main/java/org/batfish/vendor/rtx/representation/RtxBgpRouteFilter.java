package org.batfish.vendor.rtx.representation;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;

/** One numbered route filter defined by {@code bgp import filter} or {@code bgp export filter}. */
@ParametersAreNonnullByDefault
public final class RtxBgpRouteFilter implements Serializable {

  /** Relationship between a configured prefix and a candidate route. */
  public enum MatchType {
    /** The configured prefix itself and all of its more-specific prefixes. */
    INCLUDE,
    /** Only prefixes more specific than the configured prefix. */
    REFINES,
    /** Only the exact configured prefix. */
    EQUAL
  }

  public RtxBgpRouteFilter(
      int number, LineAction action, MatchType matchType, List<Prefix> prefixes) {
    _number = number;
    _action = action;
    _matchType = matchType;
    _prefixes = ImmutableList.copyOf(prefixes);
  }

  public int getNumber() {
    return _number;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull MatchType getMatchType() {
    return _matchType;
  }

  public @Nonnull List<Prefix> getPrefixes() {
    return _prefixes;
  }

  private final int _number;
  private final @Nonnull LineAction _action;
  private final @Nonnull MatchType _matchType;
  private final @Nonnull List<Prefix> _prefixes;
}
