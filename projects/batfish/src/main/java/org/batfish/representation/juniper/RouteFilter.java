package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
