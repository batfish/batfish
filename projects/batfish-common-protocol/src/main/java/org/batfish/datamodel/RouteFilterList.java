package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

/**
 * Filter list for IPV4 routes. Performs filtering on IPv4 prefixes with access-list-like behavior
 * (match in order, with an implicit "deny all" at the end).
 */
public class RouteFilterList implements Serializable {

  private static final String PROP_LINES = "lines";

  private static final String PROP_NAME = "name";

  private static final long serialVersionUID = 1L;

  private final Supplier<Set<Prefix>> _deniedCache;

  @Nonnull private List<RouteFilterLine> _lines;

  @Nullable private final String _name;

  private final Supplier<Set<Prefix>> _permittedCache;

  private static class CacheSupplier implements Supplier<Set<Prefix>>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public Set<Prefix> get() {
      return Collections.newSetFromMap(new ConcurrentHashMap<>());
    }
  }

  @JsonCreator
  private static RouteFilterList create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_LINES) @Nullable List<RouteFilterLine> lines) {
    return new RouteFilterList(name, firstNonNull(lines, ImmutableList.of()));
  }

  /** Create and empty route filter list (with no lines) */
  public RouteFilterList(@Nullable String name) {
    this(name, ImmutableList.of());
  }

  public RouteFilterList(@Nullable String name, @Nonnull List<RouteFilterLine> lines) {
    _name = name;
    _deniedCache = Suppliers.memoize(new CacheSupplier());
    _permittedCache = Suppliers.memoize(new CacheSupplier());
    _lines = lines;
  }

  public void addLine(RouteFilterLine r) {
    _lines = ImmutableList.<RouteFilterLine>builder().addAll(_lines).add(r).build();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof RouteFilterList)) {
      return false;
    }
    RouteFilterList other = (RouteFilterList) o;
    return other._lines.equals(_lines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_lines);
  }

  @Nullable
  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  /** The lines against which to check an IPV4 route */
  @Nonnull
  @JsonProperty(PROP_LINES)
  public List<RouteFilterLine> getLines() {
    return _lines;
  }

  private boolean evaluatePrefix(Prefix prefix) {
    boolean accept = false;
    for (RouteFilterLine line : _lines) {
      if (line.getIpWildcard().containsIp(prefix.getStartIp())) {
        int prefixLength = prefix.getPrefixLength();
        SubRange range = line.getLengthRange();
        if (prefixLength >= range.getStart() && prefixLength <= range.getEnd()) {
          accept = line.getAction() == LineAction.PERMIT;
          break;
        }
      }
    }
    if (accept) {
      _permittedCache.get().add(prefix);
    } else {
      _deniedCache.get().add(prefix);
    }
    return accept;
  }

  /** Check if a given prefix is permitted by this filter list. */
  public boolean permits(Prefix prefix) {
    if (_deniedCache.get().contains(prefix)) {
      return false;
    } else if (_permittedCache.get().contains(prefix)) {
      return true;
    }
    return evaluatePrefix(prefix);
  }

  /**
   * Returns the set of {@link IpWildcard ips} that match this filter list.
   *
   * @throws BatfishException if any line in this {@link RouteFilterList} does not have an {@link
   *     LineAction#PERMIT} when matching.
   */
  @JsonIgnore
  public List<IpWildcard> getMatchingIps() {
    return getLines()
        .stream()
        .map(
            rfLine -> {
              if (rfLine.getAction() != LineAction.PERMIT) {
                throw new BatfishException(
                    "Expected accept action for routerfilterlist from juniper");
              } else {
                return rfLine.getIpWildcard();
              }
            })
        .collect(Collectors.toList());
  }

  /** Set the list of lines against which to match a route's prefix. */
  public void setLines(@Nonnull List<RouteFilterLine> lines) {
    _lines = lines;
  }
}
