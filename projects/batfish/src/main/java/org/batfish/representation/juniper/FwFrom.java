package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.TraceElement;
import org.batfish.representation.juniper.FwTerm.Field;

/**
 * Class for match conditions in firewall filters, security policies, and host-inbound traffic
 * filters. TODO: split this class into those three cases.
 */
public abstract class FwFrom implements Serializable {

  public abstract void applyTo(
      HeaderSpace.Builder headerSpaceBuilder, JuniperConfiguration jc, Warnings w, Configuration c);

  // TODO: make this abstract
  Field getField() {
    return Field.SOURCE;
  }

  // TODO: make this abstract
  TraceElement getTraceElement() {
    return TraceElement.of("Matched %s %s except");
  }

  // TODO: make this abstract
  HeaderSpace toHeaderspace(JuniperConfiguration jc, Configuration c, Warnings w) {
    return HeaderSpace.builder().build();
  }
}
