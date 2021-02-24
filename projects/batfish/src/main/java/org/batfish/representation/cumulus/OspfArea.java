package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** OSPF configuration for an area in a vrf. */
public class OspfArea implements Serializable {

  private final long _area;
  private final @Nonnull Map<Prefix, OspfAreaRange> _ranges;

  public OspfArea(long area) {
    _area = area;
    _ranges = new HashMap<>();
  }

  public long getArea() {
    return _area;
  }

  public @Nonnull OspfAreaRange getOrCreateRange(@Nonnull Prefix range) {
    return _ranges.computeIfAbsent(range, OspfAreaRange::new);
  }

  public @Nullable OspfAreaRange getRange(@Nonnull Prefix range) {
    return _ranges.get(range);
  }

  public @Nonnull Map<Prefix, OspfAreaRange> getRanges() {
    return Collections.unmodifiableMap(_ranges);
  }
}
