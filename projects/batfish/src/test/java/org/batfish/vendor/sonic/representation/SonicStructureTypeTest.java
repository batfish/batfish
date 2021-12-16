package org.batfish.vendor.sonic.representation;

import static org.batfish.vendor.sonic.representation.SonicStructureType.fromFrrStructureType;

import org.batfish.representation.frr.FrrStructureType;
import org.junit.Test;

public class SonicStructureTypeTest {
  /**
   * Test that there is a {@link SonicStructureType} corresponding to each {@link FrrStructureType}.
   */
  @Test
  public void testFrrInheritance() {
    for (FrrStructureType frrStructureType : FrrStructureType.values()) {
      // will throw if conversion fails
      fromFrrStructureType(frrStructureType);
    }
  }
}
