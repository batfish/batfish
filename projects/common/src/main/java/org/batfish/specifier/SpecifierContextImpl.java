package org.batfish.specifier;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.role.NodeRoleDimension;

/** Implementation of {@link SpecifierContext}. */
public class SpecifierContextImpl implements SpecifierContext {
  private final @Nonnull IBatfish _batfish;

  private final @Nonnull Supplier<Map<String, Configuration>> _configs;

  private final @Nonnull Supplier<Map<Location, LocationInfo>> _locationInfo;

  public SpecifierContextImpl(@Nonnull IBatfish batfish, @Nonnull NetworkSnapshot networkSnapshot) {
    _batfish = batfish;
    _configs = Suppliers.memoize(() -> batfish.loadConfigurations(networkSnapshot));
    _locationInfo =
        Suppliers.memoize(() -> ImmutableMap.copyOf(batfish.getLocationInfo(networkSnapshot)));
  }

  @Override
  public @Nonnull Map<String, Configuration> getConfigs() {
    return _configs.get();
  }

  @Override
  public Optional<ReferenceBook> getReferenceBook(String bookName) {
    return _batfish.getReferenceLibraryData().getReferenceBook(bookName);
  }

  @Override
  public @Nonnull Optional<NodeRoleDimension> getNodeRoleDimension(String dimension) {
    return _batfish.getNodeRoleDimension(dimension);
  }

  @Override
  public LocationInfo getLocationInfo(Location location) {
    return _locationInfo.get().getOrDefault(location, LocationInfo.NOTHING);
  }

  @Override
  public Map<Location, LocationInfo> getLocationInfo() {
    return _locationInfo.get();
  }
}
