package org.batfish.representation.cumulus_concatenated;

import static org.batfish.representation.cumulus_concatenated.CumulusStructureType.fromFrrStructureType;

import org.batfish.representation.frr.FrrStructureType;
import org.junit.Test;

public class CumulusStructureTypeTest {

  /**
   * Test that there is a {@link CumulusStructureType} corresponding to each {@link
   * FrrStructureType}.
   */
  @Test
  public void testFrrInheritance() {

    for (FrrStructureType frrStructureType : FrrStructureType.values()) {
      // will throw if conversion fails
      fromFrrStructureType(frrStructureType);
    }
  }
}
