package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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

  private static String getCommunityRegex(List<Long> communities) {
    if (communities.size() == 1) {
      return "^" + CommonUtil.longToCommunity(communities.get(0)) + "$";
    } else {
      StringBuilder regexB = new StringBuilder("(");
      for (ListIterator<Long> it = communities.listIterator(); it.hasNext(); ) {
        Long l = it.next();
        if (it.hasPrevious()) {
          regexB.append("$|^");
        } else {
          regexB.append("^");
        }
        regexB.append(CommonUtil.longToCommunity(l));
      }
      return regexB.append("$)").toString();
    }
  }

  public ExpandedCommunityList toExpandedCommunityList() {
    ExpandedCommunityList newList = new ExpandedCommunityList(_key);
    for (StandardCommunityListLine line : _lines) {
      ExpandedCommunityListLine newLine =
          new ExpandedCommunityListLine(line.getAction(), getCommunityRegex(line.getCommunities()));
      newList.addLine(newLine);
    }
    return newList;
  }
}
