package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeSet;

public class RouteFilterListsDiff extends ConfigDiffElement {

  @JsonCreator()
  public RouteFilterListsDiff() {}

  public RouteFilterListsDiff(
      NavigableMap<String, RouteFilterList> before, NavigableMap<String, RouteFilterList> after) {
    super(before.keySet(), after.keySet());
    for (String name : super.common()) {
      if (before.get(name).equals(after.get(name))) {
        _identical.add(name);
      } else {
        _diff.add(name);
        genDiffInfo(before, after, name);
      }
    }
  }

  private void genDiffInfo(
      NavigableMap<String, RouteFilterList> before,
      NavigableMap<String, RouteFilterList> after,
      String name) {
    Set<String> beforeNames = new TreeSet<>();
    Set<String> afterNames = new TreeSet<>();
    ConfigDiffElement di = new ConfigDiffElement(beforeNames, afterNames);
    List<RouteFilterLine> beforeLines = before.get(name).getLines();
    List<RouteFilterLine> afterLines = after.get(name).getLines();
    for (RouteFilterLine line : beforeLines) {
      String compactLine = line.toCompactString();
      if (afterLines.contains(line)) {
        di._identical.add(compactLine);
      } else {
        di._inBeforeOnly.add(compactLine);
      }
    }
    for (RouteFilterLine line : afterLines) {
      if (!beforeLines.contains(line)) {
        di._inAfterOnly.add(line.toCompactString());
      }
    }
    _diffInfo.put(name, di);
  }
}
