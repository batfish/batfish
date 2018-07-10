package org.batfish.representation.palo_alto;

import java.io.Serializable;
import org.batfish.datamodel.HeaderSpace;

public interface ServiceGroupMember extends Serializable {
  void applyTo(HeaderSpace.Builder srcHeaderSpaceBuilder);
}
