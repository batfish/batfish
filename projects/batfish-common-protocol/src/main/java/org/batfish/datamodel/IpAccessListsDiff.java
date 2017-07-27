package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class IpAccessListsDiff extends ConfigDiffElement {

  private Pattern _seq;

  @JsonCreator()
  public IpAccessListsDiff() {}

  public IpAccessListsDiff(
      NavigableMap<String, IpAccessList> before, NavigableMap<String, IpAccessList> after) {
    super(before.keySet(), after.keySet());
    _seq = Pattern.compile("(Seq [0-9]+) (.*)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    for (String name : super.common()) {
      if (before.get(name).unorderedEqual(after.get(name))) {
        _identical.add(name);
      } else {
        _diff.add(name);
        genDiffInfo(before, after, name);
      }
    }
  }

  private void genDiffInfo(
      NavigableMap<String, IpAccessList> before,
      NavigableMap<String, IpAccessList> after,
      String name) {
    Set<String> beforeNames = new TreeSet<>();
    Set<String> afterNames = new TreeSet<>();
    ConfigDiffElement di = new ConfigDiffElement(beforeNames, afterNames);
    List<IpAccessListLine> beforeLines = before.get(name).getLines();
    List<IpAccessListLine> afterLines = after.get(name).getLines();
    for (IpAccessListLine line : beforeLines) {
      if (afterLines.contains(line)) {
        di._identical.add(getAclLineWithoutSequence(line.getName()));
      } else {
        di._inBeforeOnly.add(getAclLineWithoutSequence(line.getName()));
      }
    }
    for (IpAccessListLine line : afterLines) {
      if (!beforeLines.contains(line)) {
        di._inAfterOnly.add(getAclLineWithoutSequence(line.getName()));
      }
    }
    di.summarizeIdentical();
  }

  private String getAclLineWithoutSequence(String line) {
    return _seq.matcher(line).replaceAll("$2");
  }
}
