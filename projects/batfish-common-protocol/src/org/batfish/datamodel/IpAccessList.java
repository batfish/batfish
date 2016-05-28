package org.batfish.datamodel;

import java.util.List;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IpAccessList extends ComparableStructure<String> {

   private static final long serialVersionUID = 1L;
   private static final String LINES_VAR = "lines";
   
   private List<IpAccessListLine> _lines;

   @JsonCreator
   public IpAccessList(@JsonProperty(NAME_VAR) String name, 
         @JsonProperty(LINES_VAR) List<IpAccessListLine> lines) {
      super(name);
      _lines = lines;
   }

   @JsonProperty(LINES_VAR)
   public List<IpAccessListLine> getLines() {
      return _lines;
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
