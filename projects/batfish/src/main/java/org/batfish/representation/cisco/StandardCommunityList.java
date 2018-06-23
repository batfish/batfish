package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.ComparableStructure;

public final class StandardCommunityList extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private final List<StandardCommunityListLine> _lines;

  public StandardCommunityList(String name) {
    super(name);
    _lines = new ArrayList<>();
  }

  public List<StandardCommunityListLine> getLines() {
    return _lines;
  }

  public ExpandedCommunityList toExpandedCommunityList() {
    ExpandedCommunityList newList = new ExpandedCommunityList(_key);
    for (StandardCommunityListLine line : _lines) {
      List<Long> standardCommunities = line.getCommunities();
      String regex;
      if (standardCommunities.size() == 1) {
        regex = "^" + CommonUtil.longToCommunity(standardCommunities.get(0)) + "$";
      } else {
        regex = "(";
        for (Long l : standardCommunities) {
          regex += "^" + CommonUtil.longToCommunity(l) + "$|";
        }
        regex = regex.substring(0, regex.length() - 1) + ")";
      }
      ExpandedCommunityListLine newLine = new ExpandedCommunityListLine(line.getAction(), regex);
      newList.addLine(newLine);
    }
    return newList;
  }
}
