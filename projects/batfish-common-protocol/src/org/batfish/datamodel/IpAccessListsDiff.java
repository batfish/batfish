package org.batfish.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IpAccessListsDiff extends ConfigDiffElement {

   public class IpAccessListDiffInfo {

      private static final String DIFF = "diff";
      List<Map<String, String>> _diff;

      @JsonCreator()
      public IpAccessListDiffInfo() {

      }

      public IpAccessListDiffInfo(IpAccessList a, IpAccessList b) {
         _diff = new ArrayList<>();
         List<IpAccessListLine> aLines = a.getLines();
         List<IpAccessListLine> bLines = b.getLines();
         for (int i = 0; i < Math.max(aLines.size(), bLines.size()); i++) {
            HashMap<String, String> d = new HashMap<>();
            if (i >= aLines.size() && i < bLines.size()) {
               d.put("a", null);
               d.put("b", bLines.get(i).getName());
               _diff.add(d);
            }
            else if (i >= bLines.size() && i < aLines.size()) {
               d.put("b", null);
               d.put("a", aLines.get(i).getName());
               _diff.add(d);
            }
            else if (!aLines.get(i).equals(bLines.get(i))) {
               d.put("a", aLines.get(i).getName());
               d.put("b", bLines.get(i).getName());
               _diff.add(d);
            }
         }
      }

      @JsonProperty(DIFF)
      public List<Map<String, String>> getDiff() {
         return _diff;
      }

      public void setDiff(List<Map<String, String>> diff) {
         _diff = diff;
      }

   }

   private static final String DIFF = "diff";
   private static final String DIFF_INFO = "diffInfo";

   private Set<String> _diff;
   private Map<String, IpAccessListDiffInfo> _diffInfo;

   @JsonCreator()
   public IpAccessListsDiff() {

   }

   public IpAccessListsDiff(NavigableMap<String, IpAccessList> a,
         NavigableMap<String, IpAccessList> b) {

      super(a.keySet(), b.keySet());
      _diff = new HashSet<>();
      _diffInfo = new HashMap<>();
      for (String name : _common) {
         if (a.get(name).unorderedEqual(b.get(name))) {
            _identical.add(name);
         }
         else {
            _diff.add(name);
            _diffInfo.put(name,
                  new IpAccessListDiffInfo(a.get(name), b.get(name)));
         }
      }
   }

   @JsonProperty(DIFF)
   public Set<String> getDiff() {
      return _diff;
   }

   public void setDiff(Set<String> diff) {
      _diff = diff;
   }

   @JsonProperty(DIFF_INFO)
   public Map<String, IpAccessListDiffInfo> getDiffInfo() {
      return _diffInfo;
   }

   public void setDiffInfo(Map<String, IpAccessListDiffInfo> diffInfo) {
      _diffInfo = diffInfo;
   }

   

}
