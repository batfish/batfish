package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.datamodel.transformation.PortField.DESTINATION;
import static org.batfish.datamodel.transformation.PortField.SOURCE;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class AssignPortFromPoolTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new AssignPortFromPool(DEST_NAT, DESTINATION, 3000, 4000),
            new AssignPortFromPool(DEST_NAT, DESTINATION, 3000, 4000))
        .addEqualityGroup(new AssignPortFromPool(SOURCE_NAT, DESTINATION, 3000, 4000))
        .addEqualityGroup(new AssignPortFromPool(DEST_NAT, SOURCE, 3000, 4000))
        .addEqualityGroup(new AssignPortFromPool(DEST_NAT, DESTINATION, 5000, 4000))
        .addEqualityGroup(new AssignPortFromPool(DEST_NAT, DESTINATION, 3000, 5000))
        .testEquals();
  }
}
