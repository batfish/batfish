package org.batfish.datamodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IpAccessListsDiff extends ConfigDiffElement {

   private static final String DIFF_INFO_VAR = "diff";
   private Map<String, ConfigDiffElement> _diffInfo;

   private Pattern _seq;

   @JsonCreator()
   public IpAccessListsDiff() {

   }

   public IpAccessListsDiff(NavigableMap<String, IpAccessList> a,
         NavigableMap<String, IpAccessList> b) {

      super(a.keySet(), b.keySet());
      _diff = new HashSet<>();
      _diffInfo = new HashMap<>();
      _seq = Pattern.compile("(Seq [0-9]+) (.*)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
      for (String name : super.common()) {
         if (a.get(name).unorderedEqual(b.get(name))) {
            _identical.add(name);
         }
         else {
            _diff.add(name);
            genDiffInfo(a, b, name);

         }
      }
   }

   private void genDiffInfo(NavigableMap<String, IpAccessList> a,
         NavigableMap<String, IpAccessList> b, String name) {

      Set<String> aNames = new TreeSet<>();
      Set<String> bNames = new TreeSet<>();
      ConfigDiffElement di = new ConfigDiffElement(aNames, bNames);
      List<IpAccessListLine> aLines = a.get(name).getLines();
      List<IpAccessListLine> bLines = b.get(name).getLines();
      int as = aLines.size();
      int bs = bLines.size();
      for (IpAccessListLine line : aLines) {
         if (bLines.contains(line)) {
            di._identical.add(getAclLineWithoutSequence(line.getName()));
         }
         else {
            di._inAOnly.add(getAclLineWithoutSequence(line.getName()));
         }
      }

      for (IpAccessListLine line : bLines) {
         if (!aLines.contains(line)) {
            di._inBOnly.add(getAclLineWithoutSequence(line.getName()));
         }
      }
      _diffInfo.put(name, di);
   }

   private String getAclLineWithoutSequence(String line) {
      return _seq.matcher(line).replaceAll("$2");
   }

   @JsonProperty(DIFF_INFO_VAR)
   public Map<String, ConfigDiffElement> getDiffInfo() {
      return _diffInfo;
   }

   public void setDiffInfo(Map<String, ConfigDiffElement> diffInfo) {
      _diffInfo = diffInfo;
   }

}
