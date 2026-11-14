package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;

public final class RouteFilter implements Serializable {

  private boolean _ipv4;

  private boolean _ipv6;

  private final Map<RouteFilterLine, RouteFilterLine> _lines;

  public RouteFilter() {
    _lines = new LinkedHashMap<>();
  }

  public boolean getIpv4() {
    return _ipv4;
  }

  public boolean getIpv6() {
    return _ipv6;
  }

  public Set<RouteFilterLine> getLines() {
    return _lines.keySet();
  }

  /** Convert IPv4 lines without line-specific actions, preserving empty longest-prefix matches. */
  public List<org.batfish.datamodel.RouteFilterLine> toRouteFilterLines() {
    List<Route4FilterLine> lines =
        _lines.keySet().stream()
            .filter(Route4FilterLine.class::isInstance)
            .map(Route4FilterLine.class::cast)
            .collect(ImmutableList.toImmutableList());
    boolean hasEmptyMatch =
        lines.stream()
            .anyMatch(line ->
                line.toRouteFilterLines().stream()
                    .allMatch(converted -> converted.getLengthRange().isEmpty()));
    if (!hasEmptyMatch) {
      return lines.stream()
          .filter(line -> line.getThens().isEmpty())
          .flatMap(line -> line.toRouteFilterLines().stream())
          .collect(ImmutableList.toImmutableList());
    }
    ImmutableList.Builder<org.batfish.datamodel.RouteFilterLine> converted =
        ImmutableList.builder();
    lines.stream()
        .sorted(
            Comparator.comparingInt(
                    (Route4FilterLine line) -> line.getLookupPrefix().getPrefixLength())
                .reversed())
        .forEach(line -> {
          List<org.batfish.datamodel.RouteFilterLine> matches = line.toRouteFilterLines();
          if (matches.stream().allMatch(match -> match.getLengthRange().isEmpty())) {
            // An empty match still prevents fallback to a less-specific route filter.
            converted.add(
                new org.batfish.datamodel.RouteFilterLine(
                    LineAction.DENY,
                    line.getLookupPrefix(),
                    new SubRange(
                        line.getLookupPrefix().getPrefixLength(), Prefix.MAX_PREFIX_LENGTH)));
          } else if (line.getThens().isEmpty()) {
            converted.addAll(matches);
          }
        });
    return converted.build();
  }

  public <T extends RouteFilterLine> T insertLine(T line, Class<T> lineClass) {
    RouteFilterLine existingLine = _lines.get(line);
    if (existingLine == null) {
      _lines.put(line, line);
      return line;
    } else {
      return lineClass.cast(existingLine);
    }
  }

  public void setIpv4(boolean ipv4) {
    _ipv4 = ipv4;
  }

  public void setIpv6(boolean ipv6) {
    _ipv6 = ipv6;
  }
}
