package org.batfish.datamodel.questions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class InstanceDataTest {

  @Test
  public void testOrderedVariableNames() throws IOException {
    InstanceData instanceData =
        BatfishObjectMapper.mapper()
            .readValue("{\"orderedVariableNames\":[\"b\", \"a\", \"c\"]}", InstanceData.class);
  }

  @Test
  public void testJsonSerialization() throws IOException {
    InstanceData instanceData = new InstanceData();
    instanceData.setInstanceName("instanceData");
    instanceData.setDescription("Instance Data Description");
    instanceData.setLongDescription("Long Description");
    instanceData.setOrderedVariableNames(ImmutableList.of());
    SortedSet<String> tags = new TreeSet<>();
    instanceData.setTags(tags);
    SortedMap<String, Variable> variables = new TreeMap<>();
    instanceData.setVariables(variables);
    assertThat(BatfishObjectMapper.clone(instanceData, InstanceData.class), equalTo(instanceData));
  }
}
