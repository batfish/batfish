package org.batfish.question.specifiers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;

import com.google.auto.service.AutoService;
import java.util.Set;
import org.batfish.question.specifiers.SpecifiersQuestion.QueryType;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.SpecifierContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class SpecifiersQuestionTest {
  @AutoService(IpSpaceSpecifierFactory.class)
  public static final class TestIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {
    @Override
    public String getName() {
      return getClass().getSimpleName();
    }

    @Override
    public IpSpaceSpecifier buildIpSpaceSpecifier(Object input) {
      return new TestIpSpaceSpecifier(input);
    }
  }

  public static final class TestIpSpaceSpecifier implements IpSpaceSpecifier {
    private final Object _input;

    TestIpSpaceSpecifier(Object input) {
      _input = input;
    }

    Object getInput() {
      return _input;
    }

    @Override
    public IpSpaceAssignment resolve(Set<Location> locations, SpecifierContext ctxt) {
      return null;
    }
  }

  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testGetIpSpaceSpecifier_inputWithoutFactory() {
    exception.expect(NullPointerException.class);
    SpecifiersQuestion question = new SpecifiersQuestion(QueryType.IP_SPACE);
    question.setIpSpaceSpecifierFactory(null);
    question.setIpSpaceSpecifierInput("foo");
    question.getIpSpaceSpecifier();
  }

  @Test
  public void testGetIpSpaceSpecifier_defaultInput() {
    SpecifiersQuestion question = new SpecifiersQuestion(QueryType.IP_SPACE);
    question.setIpSpaceSpecifierFactory(new TestIpSpaceSpecifierFactory().getName());

    IpSpaceSpecifier ipSpaceSpecifier = question.getIpSpaceSpecifier();
    assertThat(ipSpaceSpecifier, instanceOf(TestIpSpaceSpecifier.class));

    TestIpSpaceSpecifier testIpSpaceSpecifier = (TestIpSpaceSpecifier) ipSpaceSpecifier;
    assertThat(testIpSpaceSpecifier.getInput(), nullValue());
  }

  @Test
  public void testGetIpSpaceSpecifier() {
    String input = "input";
    SpecifiersQuestion question = new SpecifiersQuestion(QueryType.IP_SPACE);
    question.setIpSpaceSpecifierFactory(new TestIpSpaceSpecifierFactory().getName());

    question.setIpSpaceSpecifierInput(input);
    IpSpaceSpecifier ipSpaceSpecifier = question.getIpSpaceSpecifier();
    assertThat(ipSpaceSpecifier, instanceOf(TestIpSpaceSpecifier.class));

    TestIpSpaceSpecifier testIpSpaceSpecifier = (TestIpSpaceSpecifier) ipSpaceSpecifier;
    assertThat(testIpSpaceSpecifier.getInput(), equalTo(input));
  }

  @Test
  public void testGetLocationSpecifier_defaultFactory() {
    LocationSpecifier locationSpecifier =
        new SpecifiersQuestion(QueryType.LOCATION).getLocationSpecifier();
    assertThat(locationSpecifier, equalTo(AllInterfacesLocationSpecifier.INSTANCE));
  }

  @Test
  public void testGetLocationSpecifier_inputWithoutFactory() {
    exception.expect(NullPointerException.class);
    SpecifiersQuestion question = new SpecifiersQuestion(QueryType.LOCATION);
    question.setLocationSpecifierFactory(null);
    question.setLocationSpecifierInput("foo");
    question.getLocationSpecifier();
  }
}
