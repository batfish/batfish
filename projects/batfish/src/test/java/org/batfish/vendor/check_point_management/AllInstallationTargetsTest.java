package org.batfish.vendor.check_point_management;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link AllInstallationTargets}. */
final class AllInstallationTargetsTest {

  @Test
  void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(AllInstallationTargets.instance()),
        equalTo(AllInstallationTargets.instance()));
  }
}
