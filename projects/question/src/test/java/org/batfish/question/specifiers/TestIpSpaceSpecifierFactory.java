package org.batfish.question.specifiers;

import com.google.auto.service.AutoService;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;

@AutoService(IpSpaceSpecifierFactory.class)
public class TestIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {
  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public IpSpaceSpecifier buildIpSpaceSpecifier(Object input) {
    return new TestIpSpaceSpecifier(input);
  }
}
