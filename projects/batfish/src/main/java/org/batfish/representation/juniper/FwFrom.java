package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.TraceElement;
import org.batfish.representation.juniper.FwTerm.Field;

public abstract class FwFrom implements Serializable {

  public abstract void applyTo(
      HeaderSpace.Builder headerSpaceBuilder, JuniperConfiguration jc, Warnings w, Configuration c);

  Field getField() {
    return Field.SOURCE;
  }

  TraceElement getTraceElement() {
    return TraceElement.of("Matched %s %s except");
  }

  HeaderSpace toHeaderspace(JuniperConfiguration jc, Configuration c, Warnings w) {
    return HeaderSpace.builder().build();
  }
}
