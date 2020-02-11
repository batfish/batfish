package org.batfish.specifier;

import java.util.Map;
import java.util.Optional;
import org.batfish.datamodel.Configuration;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.role.NodeRoleDimension;

public class TestSpecifierContext implements SpecifierContext {

  @Override
  public Map<String, Configuration> getConfigs() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<NodeRoleDimension> getNodeRoleDimension(String dimension) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocationInfo getLocationInfo(Location location) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<Location, LocationInfo> getLocationInfo() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<ReferenceBook> getReferenceBook(String bookName) {
    throw new UnsupportedOperationException();
  }
}
