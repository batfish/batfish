package org.batfish.specifier;

import java.util.Map;
import java.util.Optional;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpSpace;
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
  public IpSpace getInterfaceOwnedIps(String hostname, String iface) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IpSpace getInterfaceLinkOwnedIps(String hostname, String iface) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<ReferenceBook> getReferenceBook(String bookName) {
    throw new UnsupportedOperationException();
  }
}
