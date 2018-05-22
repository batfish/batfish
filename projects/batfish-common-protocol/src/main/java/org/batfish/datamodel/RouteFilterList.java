package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.common.util.ComparableStructure;

@JsonSchemaDescription("An access-list used to filter IPV4 routes")
public class RouteFilterList extends ComparableStructure<String> {

  private static final String PROP_LINES = "lines";

  private static final long serialVersionUID = 1L;

  private final Supplier<Set<Prefix>> _deniedCache;

  private List<RouteFilterLine> _lines;

  private final Supplier<Set<Prefix>> _permittedCache;

  private static class CacheSupplier implements Supplier<Set<Prefix>>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public Set<Prefix> get() {
      return Collections.newSetFromMap(new ConcurrentHashMap<>());
    }
  }

  @JsonCreator
  public RouteFilterList(@JsonProperty(PROP_NAME) String name) {
    this(name, Collections.emptyList());
  }

  public RouteFilterList(String name, List<RouteFilterLine> lines) {
    super(name);
    _deniedCache = Suppliers.memoize(new CacheSupplier());
    _permittedCache = Suppliers.memoize(new CacheSupplier());
    this._lines = lines;
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

  @JsonProperty(PROP_LINES)
  @JsonPropertyDescription("The lines against which to check an IPV4 route")
  public List<RouteFilterLine> getLines() {
    return _lines;
  }

  private boolean newPermits(Prefix prefix) {
    boolean accept = false;
    for (RouteFilterLine line : _lines) {
      if (line.getIpWildcard().containsIp(prefix.getStartIp())) {
        int prefixLength = prefix.getPrefixLength();
        SubRange range = line.getLengthRange();
        if (prefixLength >= range.getStart() && prefixLength <= range.getEnd()) {
          accept = line.getAction() == LineAction.ACCEPT;
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

  public boolean permits(Prefix prefix) {
    if (_deniedCache.get().contains(prefix)) {
      return false;
    } else if (_permittedCache.get().contains(prefix)) {
      return true;
    }
    return newPermits(prefix);
  }

  /**
   * Returns the set of {@link IpWildcard ips} that match this filter list.
   *
   * @throws BatfishException if any line in this {@link RouteFilterList} does not have an {@link
   *     LineAction#ACCEPT} when matching.
   */
  @JsonIgnore
  public List<IpWildcard> getMatchingIps() {
    return getLines()
        .stream()
        .map(
            rfLine -> {
              if (rfLine.getAction() != LineAction.ACCEPT) {
                throw new BatfishException(
                    "Expected accept action for routerfilterlist from juniper");
              } else {
                return rfLine.getIpWildcard();
              }
            })
        .collect(Collectors.toList());
  }

  @JsonProperty(PROP_LINES)
  public void setLines(List<RouteFilterLine> lines) {
    _lines = lines;
  }
}
