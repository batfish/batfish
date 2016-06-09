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

   @JsonProperty(LINES_VAR)
   public List<IpAccessListLine> getLines() {
      return _lines;
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

}
