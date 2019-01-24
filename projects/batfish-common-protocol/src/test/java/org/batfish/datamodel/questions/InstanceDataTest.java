package org.batfish.datamodel.questions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link InstanceData} */
public class InstanceDataTest {

  /**
   * Test that getOrderedVariableNames returns an empty list when orderedVariableNames are not
   * provided
   */
  @Test
  public void testGetOrderedVariableNamesMissing() throws IOException {
    InstanceData instanceData = BatfishObjectMapper.mapper().readValue("{}", InstanceData.class);
    assertThat(instanceData.getOrderedVariableNames(), equalTo(ImmutableList.of()));
  }

  /**
   * Test that getOrderedVariableNames returns an empty list when orderedVariableNames is an empty
   * list
   */
  @Test
  public void testGetOrderedVariableNamesEmptyList() throws IOException {
    InstanceData instanceData =
        BatfishObjectMapper.mapper().readValue("{\"orderedVariableNames\":[]}", InstanceData.class);
    assertThat(instanceData.getOrderedVariableNames(), equalTo(ImmutableList.of()));
  }

  /** Test that getOrderedVariableNames returns orderedVariableNames in the correct order */
  @Test
  public void testGetOrderedVariableNamesNonEmptyList() throws IOException {
    InstanceData instanceData =
        BatfishObjectMapper.mapper()
            .readValue("{\"orderedVariableNames\":[\"r\", \"a\", \"d\"]}", InstanceData.class);
    assertThat(instanceData.getOrderedVariableNames(), equalTo(ImmutableList.of("r", "a", "d")));
  }

  /**
   * Test that translating an InstanceData object to JSON and using that JSON to create a new
   * InstanceData object produces an object equal to the original instance
   */
  @Test
  public void testJsonSerialization() throws IOException {
    InstanceData instanceData = new InstanceData();
    instanceData.setInstanceName("instanceName");
    instanceData.setDescription("The description");
    instanceData.setLongDescription("The long description");
    instanceData.setOrderedVariableNames(ImmutableList.of());
    instanceData.setTags(new TreeSet<>());
    instanceData.setVariables(new TreeMap<>());
    assertThat(BatfishObjectMapper.clone(instanceData, InstanceData.class), equalTo(instanceData));
  }
}
