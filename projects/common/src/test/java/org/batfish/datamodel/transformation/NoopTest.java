package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests for {@link Noop}. */
public class NoopTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Noop(SOURCE_NAT), new Noop(SOURCE_NAT))
        .addEqualityGroup(new Noop(DEST_NAT))
        .testEquals();
  }
}
