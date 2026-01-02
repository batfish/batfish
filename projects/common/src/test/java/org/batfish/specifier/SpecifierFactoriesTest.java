package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.applications.NamedApplication;
import org.junit.Test;

public class SpecifierFactoriesTest {

  @Test
  public void testGetApplicationSpecifierOrDefault() {
    assertThat(
        SpecifierFactories.getApplicationSpecifierOrDefault(
                "", NoApplicationsApplicationSpecifier.INSTANCE)
            .resolve(),
        equalTo(ImmutableSet.of()));
    assertThat(
        SpecifierFactories.getApplicationSpecifierOrDefault(
                "http", NoApplicationsApplicationSpecifier.INSTANCE)
            .resolve(),
        equalTo(ImmutableSet.of(NamedApplication.HTTP.getApplication())));
  }
}
