package org.batfish.datamodel.phc_to_flow;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.Location;

/**
 * Extract the first destination port from {@link PacketHeaderConstraints}; if failed a default
 * value.
 */
@ParametersAreNonnullByDefault
public class DstPortExtractorDefault implements FieldExtractor<Integer> {
  /** default HTTP */
  public static DstPortExtractorDefault HTTP = new DstPortExtractorDefault(NamedPort.HTTP.number());

  public DstPortExtractorDefault(int defaultPort) {
    _defaultPort = defaultPort;
  }

  @Override
  public Integer getValue(PacketHeaderConstraints phc, @Nullable Location srcLoction) {
    return Optional.ofNullable(phc.getDstPorts())
        .map(range -> range.getSubRanges().iterator().next().getStart())
        .orElse(_defaultPort);
  }

  private int _defaultPort;
}
