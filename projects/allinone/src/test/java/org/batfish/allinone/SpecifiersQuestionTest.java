package org.batfish.allinone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.question.specifiers.SpecifiersQuestion;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.junit.Test;

public class SpecifiersQuestionTest {
  @Test
  public void testGetIpSpaceSpecifier_defaultFactory() {
    IpSpaceSpecifier specifier = new SpecifiersQuestion().getIpSpaceSpecifier();
    assertThat(specifier, equalTo(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE)));
  }

  @Test
  public void testGetLocationSpecifier_defaultFactory() {
    LocationSpecifier locationSpecifier = new SpecifiersQuestion().getLocationSpecifier();
    assertThat(locationSpecifier, equalTo(AllInterfacesLocationSpecifier.INSTANCE));
  }
}
