package org.batfish.grammar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.grammar.flattener.FlattenerLineMap;
import org.junit.Test;

public class FlattenerLineMapTest {
  @Test
  public void testFlattenerLineMap() {
    FlattenerLineMap lineMap = new FlattenerLineMap();
    lineMap.setOriginalLine(1, 4, 1);
    lineMap.setOriginalLine(1, 6, 2);
    lineMap.setOriginalLine(2, 4, 3);

    /* Confirm looking up a position corresponding to a mapping returns the correct original line */
    assertThat(lineMap.getOriginalLine(1, 4), equalTo(1));
    assertThat(lineMap.getOriginalLine(1, 6), equalTo(2));
    assertThat(lineMap.getOriginalLine(2, 4), equalTo(3));

    /* Confirm looking up a position between two mappings returns the preceding original line */
    assertThat(lineMap.getOriginalLine(1, 5), equalTo(1));

    /* Confirm looking up a position past the last mapping returns the last original line */
    assertThat(lineMap.getOriginalLine(1, 8), equalTo(2));
    assertThat(lineMap.getOriginalLine(2, 1000), equalTo(3));

    /* Confirm looking up a position before first mapping returns the last original line */
    assertThat(lineMap.getOriginalLine(1, 0), equalTo(2));
    assertThat(lineMap.getOriginalLine(2, 1), equalTo(3));

    /* Confirm looking up an unmapped line results in the default unmapped line number */
    assertThat(lineMap.getOriginalLine(0, 10), equalTo(FlattenerLineMap.UNMAPPED_LINE_NUMBER));
    assertThat(lineMap.getOriginalLine(3, 10), equalTo(FlattenerLineMap.UNMAPPED_LINE_NUMBER));
  }
}
