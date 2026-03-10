package org.batfish.datamodel.phc_to_flow;

import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.Location;

/**
 * Interfaces for extracting field values from {@link PacketHeaderConstraints} and source {@link
 * Location}
 */
public interface FieldExtractor<T> {
  T getValue(PacketHeaderConstraints phc, Location srcLocation);
}
