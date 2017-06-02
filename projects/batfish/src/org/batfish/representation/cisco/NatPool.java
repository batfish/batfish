package org.batfish.representation.cisco;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NatPool extends ComparableStructure<String> {

   private static final String FIRST_VAR = "first";

   private static final String LAST_VAR = "last";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _definitionLine;

   private Ip _first;

   private Ip _last;

   public NatPool(String name, int definitionLine) {
      super(name);
      _definitionLine = definitionLine;
   }

   public int getDefinitionLine() {
      return _definitionLine;
   }

   @JsonProperty(FIRST_VAR)
   public Ip getFirst() {
      return _first;
   }

   @JsonProperty(LAST_VAR)
   public Ip getLast() {
      return _last;
   }

   @JsonProperty(FIRST_VAR)
   public void setFirst(Ip first) {
      _first = first;
   }

   @JsonProperty(LAST_VAR)
   public void setLast(Ip last) {
      _last = last;
   }

}
