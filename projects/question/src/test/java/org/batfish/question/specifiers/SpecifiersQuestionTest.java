package org.batfish.question.specifiers;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.batfish.question.specifiers.SpecifiersQuestion.QueryType;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.ShorthandFilterSpecifier;
import org.batfish.specifier.ShorthandInterfaceSpecifier;
import org.batfish.specifier.SpecifierFactories;
import org.batfish.specifier.SpecifierFactories.Version;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class SpecifiersQuestionTest {

  @Rule public final ExpectedException exception = ExpectedException.none();

  /** Test that we get default specifiers when the input is null */
  @Test
  public void testGetSpecifierNullInput() {
    // query type is filter for everything because it is immaterial to which specifier is used
    SpecifiersQuestion question = new SpecifiersQuestion(QueryType.FILTER);
    assertThat(question.getFilterSpecifier(), instanceOf(ShorthandFilterSpecifier.class));
    assertThat(question.getInterfaceSpecifier(), instanceOf(ShorthandInterfaceSpecifier.class));
    assertThat(question.getIpSpaceSpecifier(), instanceOf(InferFromLocationIpSpaceSpecifier.class));
    assertThat(question.getLocationSpecifier(), instanceOf(AllInterfacesLocationSpecifier.class));
    assertThat(question.getNodeSpecifier(), instanceOf(AllNodesNodeSpecifier.class));
  }

  /** Test that we get specifiers based on active version when the version is not programmed */
  @Test
  public void testGetSpecifierNonNullInput() {
    // query type is filter in all these because it is immaterial to which specifier is used
    SpecifiersQuestion question = new SpecifiersQuestion(QueryType.FILTER);

    question.setFilterSpecifierInput("filters");
    question.setInterfaceSpecifierInput("interfaces");
    question.setIpSpaceSpecifierInput("1.1.1.1");
    question.setLocationSpecifierInput("locations");
    question.setNodeSpecifierInput("nodes");

    assertThat(
        question.getFilterSpecifier(),
        instanceOf(SpecifierFactories.getFilterSpecifierOrDefault("input", null).getClass()));
    assertThat(
        question.getInterfaceSpecifier(),
        instanceOf(SpecifierFactories.getInterfaceSpecifierOrDefault("input", null).getClass()));
    assertThat(
        question.getIpSpaceSpecifier(),
        instanceOf(SpecifierFactories.getIpSpaceSpecifierOrDefault("input", null).getClass()));
    assertThat(
        question.getLocationSpecifier(),
        instanceOf(SpecifierFactories.getLocationSpecifierOrDefault("input", null).getClass()));
    assertThat(
        question.getNodeSpecifier(),
        instanceOf(SpecifierFactories.getNodeSpecifierOrDefault("input", null).getClass()));
  }

  /** Test that when we use a different version from the active one, we get the right specifiers */
  @Test
  public void testGetSpecifierVersionOverride() {
    Version otherVersion =
        SpecifierFactories.ACTIVE_VERSION == Version.V1 ? Version.V2 : Version.V1;

    // query type is filter in all these because it is immaterial to which specifier is used
    SpecifiersQuestion question = new SpecifiersQuestion(QueryType.FILTER, otherVersion);

    question.setFilterSpecifierInput("filters");
    question.setInterfaceSpecifierInput("interfaces");
    question.setIpSpaceSpecifierInput("1.1.1.1");
    question.setLocationSpecifierInput("locations");
    question.setNodeSpecifierInput("nodes");

    assertThat(
        question.getFilterSpecifier(),
        instanceOf(
            SpecifierFactories.getFilterSpecifierOrDefault(
                    "input", null, SpecifierFactories.getFilterFactory(otherVersion))
                .getClass()));
    assertThat(
        question.getInterfaceSpecifier(),
        instanceOf(
            SpecifierFactories.getInterfaceSpecifierOrDefault(
                    "input", null, SpecifierFactories.getInterfaceFactory(otherVersion))
                .getClass()));
    assertThat(
        question.getIpSpaceSpecifier(),
        instanceOf(
            SpecifierFactories.getIpSpaceSpecifierOrDefault(
                    "1.1.1.1", null, SpecifierFactories.getIpSpaceFactory(otherVersion))
                .getClass()));
    assertThat(
        question.getLocationSpecifier(),
        instanceOf(
            SpecifierFactories.getLocationSpecifierOrDefault(
                    "input", null, SpecifierFactories.getLocationFactory(otherVersion))
                .getClass()));
    assertThat(
        question.getNodeSpecifier(),
        instanceOf(
            SpecifierFactories.getNodeSpecifierOrDefault(
                    "input", null, SpecifierFactories.getNodeFactory(otherVersion))
                .getClass()));
  }
}
