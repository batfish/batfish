package org.batfish.datamodel.phc_to_flow;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.Location;

/**
 * Extract the first source port from {@link PacketHeaderConstraints}; if failed use 49152 as the
 * default.
 */
@ParametersAreNonnullByDefault
public enum SrcPortExtractorDefault implements FieldExtractor<Integer> {
  /** single instance */
  INSTANCE;

  @Override
  public Integer getValue(PacketHeaderConstraints phc, @Nullable Location srcLocation) {
    return Optional.ofNullable(phc.getSrcPorts())
        .map(range -> range.getSubRanges().iterator().next().getStart())
        .orElse(NamedPort.EPHEMERAL_LOWEST.number());
  }
}
