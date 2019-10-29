package org.batfish.datamodel.phc_to_flow;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.Location;

/** Extract the first icmp code from {@link PacketHeaderConstraints}; if failed a default value. */
@ParametersAreNonnullByDefault
public enum IcmpCodeExtractorDefault implements FieldExtractor<Integer> {
  /** default 0 */
  ZERO(0);

  @Override
  public Integer getValue(PacketHeaderConstraints phc, @Nullable Location srcLoction) {
    return Optional.ofNullable(phc.getIcmpCodes())
        .map(range -> range.getSubRanges().iterator().next().getStart())
        .orElse(_defaultCode);
  }

  private int _defaultCode;

  IcmpCodeExtractorDefault(int defaultCode) {
    _defaultCode = defaultCode;
  }
}
