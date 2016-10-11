package org.batfish.datamodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IpAccessListsDiff extends ConfigDiffElement {

   private static final String DIFF = "diff";
   private static final String DIFF_INFO = "diffInfo";
   
   private Set<String> _diff;
   private Map<String, IpAccessListDiff> _diffInfo;
   
   @JsonCreator()
   public IpAccessListsDiff() {

   }

   public IpAccessListsDiff(NavigableMap<String, IpAccessList> a,
         NavigableMap<String, IpAccessList> b) {
      
      super(a.keySet(), b.keySet());
      _diff = new HashSet<String>();
      _diffInfo = new HashMap<String, IpAccessListDiff>();
      for (String name : _common) {
         if (a.get(name).equals(b.get(name))) {
            _identical.add(name);
         }
         else {
            _diff.add(name);
            _diffInfo.put(name, new IpAccessListDiff(a.get(name), b.get(name)));
         }
      }
   }

   /**
    * @return the _diff
    */
   @JsonProperty(DIFF)
   public Set<String> get_diff() {
      return _diff;
   }

   public void set_diff(Set<String> d) {
      this._diff = d;
   }

   /**
    * @return the _diffInfo
    */
   @JsonProperty(DIFF_INFO)
   public Map<String, IpAccessListDiff> get_diffInfo() {
      return _diffInfo;
   }

   /**
    * @param _diffInfo the _diffInfo to set
    */
   public void set_diffInfo(Map<String, IpAccessListDiff> _diffInfo) {
      this._diffInfo = _diffInfo;
   }
   

}
