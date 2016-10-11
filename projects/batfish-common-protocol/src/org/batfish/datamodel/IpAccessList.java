package org.batfish.datamodel;

import java.util.List;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IpAccessList extends ComparableStructure<String> {

   private static final String LINES_VAR = "lines";

   private static final long serialVersionUID = 1L;

   private List<IpAccessListLine> _lines;

   @JsonCreator
   public IpAccessList(@JsonProperty(NAME_VAR) String name) {
      super(name);
   }

   public IpAccessList(String name, List<IpAccessListLine> lines) {
      super(name);
      _lines = lines;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      IpAccessList other = (IpAccessList) obj;
      return other._lines.equals(_lines);
   }

   public FilterResult filter(Flow flow) {
      for (int i = 0; i < _lines.size(); i++) {
         IpAccessListLine line = _lines.get(i);
         if (line.matches(flow)) {
            return new FilterResult(i, line.getAction());
         }
      }
      return new FilterResult(null, LineAction.REJECT);
   }

   @JsonProperty(LINES_VAR)
   public List<IpAccessListLine> getLines() {
      return _lines;
   }

   private boolean noDenyOrLastDeny(IpAccessList acl) {
      int count = 0;
      for (IpAccessListLine line : acl.getLines()) {
         if (line.getAction() == LineAction.REJECT
               && count < acl.getLines().size() - 1) {
            return false;
         }
         count++;
      }
      return true;
   }

   @JsonProperty(LINES_VAR)
   public void setLines(List<IpAccessListLine> lines) {
      _lines = lines;
   }

   @Override
   public String toString() {
      String output = super.toString() + "\n" + "Identifier: " + _key;
      for (IpAccessListLine line : _lines) {
         output += "\n" + line;
      }
      return output;
   }

   public boolean unorderedEqual(Object obj) {
      if (this == obj) {
         return true;
      }
      if (this.equals(obj)) {
         return true;
      }
      IpAccessList other = (IpAccessList) obj;
      int x = this.getLines().size();
      int y = other.getLines().size();
      if (x != y) {
         return false;
      }
      // Unordered check is valid only if there is no deny (or only one, at the
      // end) in both lists.
      if (!noDenyOrLastDeny(this) || !noDenyOrLastDeny(other)) {
         return false;
      }
      for (IpAccessListLine line : this.getLines()) {
         if (!other.getLines().contains(line)) {
            return false;
         }
      }
      return true;
   }
}
