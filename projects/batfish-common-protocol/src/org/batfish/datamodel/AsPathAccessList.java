package org.batfish.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class AsPathAccessList extends ComparableStructure<String>
      implements Serializable {

   private static final String LINES_VAR = "lines";

   private static final long serialVersionUID = 1L;

   private final List<AsPathAccessListLine> _lines;

   public AsPathAccessList(String name) {
      super(name);
      _lines = new ArrayList<>();
   }

   @JsonCreator
   public AsPathAccessList(@JsonProperty(NAME_VAR) String name,
         @JsonProperty(LINES_VAR) List<AsPathAccessListLine> lines) {
      super(name);
      _lines = lines;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      AsPathAccessList other = (AsPathAccessList) obj;
      return other._lines.equals(_lines);
   }

   @JsonProperty(LINES_VAR)
   public List<AsPathAccessListLine> getLines() {
      return _lines;
   }

}
