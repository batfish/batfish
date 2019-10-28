package org.batfish.datamodel.phc_to_flow;

import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.Location;

public interface FieldExtractor<T> {
  T getValue(PacketHeaderConstraints phc, Location srcLocation);
}
