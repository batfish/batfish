package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.transformation.TransformationStep;

/** Represents the action part of a Juniper nat rule */
public interface NatRuleThen extends Serializable {
  List<TransformationStep> toTransformationSteps(
      Nat nat,
      @Nullable Map<String, AddressBookEntry> addressBookEntryMap,
      Ip interfaceIp,
      Warnings warnings);
}
