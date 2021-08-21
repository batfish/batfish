package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link AllNatInstallTarget}. */
public final class AllNatInstallTargetTest {

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(AllNatInstallTarget.instance()),
        equalTo(AllNatInstallTarget.instance()));
  }
}
