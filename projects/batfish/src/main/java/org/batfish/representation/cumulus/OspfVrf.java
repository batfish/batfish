package org.batfish.representation.cumulus;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.io.Serializable;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/** OSPF configuration for a particular VRF. */
public class OspfVrf implements Serializable {

  private final @Nonnull String _vrfName;
  private @Nullable Ip _routerId;
  private final @Nonnull Table<Long, Prefix, OspfAreaRange> _ospfAreaRange;

  public OspfVrf(String name) {
    _vrfName = name;
    _ospfAreaRange = HashBasedTable.create();
  }

  public @Nonnull OspfAreaRange getOrCreateAreaRange(long area, @Nonnull Prefix prefix) {
    return _ospfAreaRange.row(area).computeIfAbsent(prefix, p -> new OspfAreaRange(area, p));
  }

  public @Nullable OspfAreaRange getAreaRange(long area, @Nonnull Prefix prefix) {
    return _ospfAreaRange.get(area, prefix);
  }

  public @Nonnull Collection<OspfAreaRange> getAreaRanges() {
    return _ospfAreaRange.values();
  }

  @Nullable
  public Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  @Nonnull
  public String getVrfName() {
    return _vrfName;
  }
}
