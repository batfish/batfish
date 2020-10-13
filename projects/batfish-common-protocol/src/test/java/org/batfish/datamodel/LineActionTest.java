package org.batfish.datamodel;

import static org.batfish.datamodel.LineAction.DENY;
import static org.batfish.datamodel.LineAction.PERMIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test of {@link LineAction} */
@RunWith(JUnit4.class)
public class LineActionTest {

  @Test
  public void testCaseInsensitive() {
    assertThat(LineAction.forValue("permit"), equalTo(PERMIT));
    assertThat(LineAction.forValue("PERMIT"), equalTo(PERMIT));
    assertThat(LineAction.forValue("PeRMiT"), equalTo(PERMIT));
    assertThat(LineAction.forValue("deny"), equalTo(DENY));
    assertThat(LineAction.forValue("Deny"), equalTo(DENY));
    assertThat(LineAction.forValue("DeNy"), equalTo(DENY));
  }

  @Test
  public void testCompatible() {
    assertThat(LineAction.forValue("accept"), equalTo(LineAction.forValue("permit")));
    assertThat(LineAction.forValue("reject"), equalTo(LineAction.forValue("deny")));
  }
}
