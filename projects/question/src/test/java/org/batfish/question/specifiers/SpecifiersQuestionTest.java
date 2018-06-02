package org.batfish.question.specifiers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;

import org.batfish.specifier.IpSpaceSpecifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SpecifiersQuestionTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testGetIpSpaceSpecifier_noFactory() {
    exception.expect(NullPointerException.class);
    new SpecifiersQuestion().getIpSpaceSpecifier();
  }

  @Test
  public void testGetIpSpaceSpecifier_noInput() {
    SpecifiersQuestion question = new SpecifiersQuestion();
    question.setIpSpaceSpecifierFactory(new TestIpSpaceSpecifierFactory().getName());

    IpSpaceSpecifier ipSpaceSpecifier = question.getIpSpaceSpecifier();
    assertThat(ipSpaceSpecifier, instanceOf(TestIpSpaceSpecifier.class));

    TestIpSpaceSpecifier testIpSpaceSpecifier = (TestIpSpaceSpecifier) ipSpaceSpecifier;
    assertThat(testIpSpaceSpecifier.getInput(), nullValue());
  }

  @Test
  public void testGetIpSpaceSpecifier() {
    String input = "input";
    SpecifiersQuestion question = new SpecifiersQuestion();
    question.setIpSpaceSpecifierFactory(new TestIpSpaceSpecifierFactory().getName());

    question.setIpSpaceSpecifierInput(input);
    IpSpaceSpecifier ipSpaceSpecifier = question.getIpSpaceSpecifier();
    assertThat(ipSpaceSpecifier, instanceOf(TestIpSpaceSpecifier.class));

    TestIpSpaceSpecifier testIpSpaceSpecifier = (TestIpSpaceSpecifier) ipSpaceSpecifier;
    assertThat(testIpSpaceSpecifier.getInput(), equalTo(input));
  }

  @Test
  public void testGetLocationSpecifier_noFactory() {
    exception.expect(NullPointerException.class);
    new SpecifiersQuestion().getLocationSpecifier();
  }
}
