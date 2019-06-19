package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.transformation.TransformationStep;

/** Represents the action part of a Juniper nat rule */
public interface NatRuleThen extends Serializable {
  List<TransformationStep> toTransformationSteps(
      Nat nat,
      Map<String, AddressBookEntry> addressBookEntryMap,
      Ip interfaceIp,
      Warnings warnings);
}
