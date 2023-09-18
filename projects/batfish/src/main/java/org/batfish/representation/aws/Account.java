package org.batfish.representation.aws;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a single AWS account */
@ParametersAreNonnullByDefault
public class Account implements Serializable {

  private final @Nonnull String _id;
  private final @Nonnull Map<String, Region> _regions;

  public Account(String id) {
    this(id, new HashMap<>());
  }

  private Account(String id, Map<String, Region> regions) {
    _id = id;
    _regions = regions;
  }

  public @Nonnull Region addOrGetRegion(String region) {
    return _regions.computeIfAbsent(region, Region::new);
  }

  @VisibleForTesting
  public void addRegion(Region region) {
    _regions.put(region.getName(), region);
  }

  public Collection<Region> getRegions() {
    return _regions.values();
  }

  public @Nonnull String getId() {
    return _id;
  }
}
