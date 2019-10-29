package org.batfish.datamodel.phc_to_flow;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.Location;

/** Extract the first icmp type from {@link PacketHeaderConstraints}; if failed a default value. */
@ParametersAreNonnullByDefault
public enum IcmpTypeExtractorDefault implements FieldExtractor<Integer> {
  /** default echo reply */
  REPLY(0),
  /** default echo request */
  REQUEST(8);

  @Override
  public Integer getValue(PacketHeaderConstraints phc, @Nullable Location srcLoction) {
    return Optional.ofNullable(phc.getIcmpTypes())
        .map(range -> range.getSubRanges().iterator().next().getStart())
        .orElse(_defaultType);
  }

  private int _defaultType;

  IcmpTypeExtractorDefault(int defaultType) {
    _defaultType = defaultType;
  }
}
