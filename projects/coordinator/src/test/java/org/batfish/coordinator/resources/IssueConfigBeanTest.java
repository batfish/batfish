package org.batfish.coordinator.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.MinorIssueConfig;
import org.junit.Test;

public class IssueConfigBeanTest {

  @Test
  public void testEquals() {
    IssueConfigBean group1Elem1 =
        new IssueConfigBean("major1", new MinorIssueConfig("minor1", null, null));
    IssueConfigBean group1Elem2 =
        new IssueConfigBean("major1", new MinorIssueConfig("minor1", null, null));
    IssueConfigBean group2Elem1 =
        new IssueConfigBean("major2", new MinorIssueConfig("minor1", null, null));
    IssueConfigBean group3Elem1 =
        new IssueConfigBean("major1", new MinorIssueConfig("minor2", null, null));
    IssueConfigBean group4Elem1 =
        new IssueConfigBean("major1", new MinorIssueConfig("minor1", 0, null));
    IssueConfigBean group5Elem1 =
        new IssueConfigBean("major1", new MinorIssueConfig("minor1", null, "localhost"));

    new EqualsTester()
        .addEqualityGroup(group1Elem1, group1Elem2)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1)
        .addEqualityGroup(group4Elem1)
        .addEqualityGroup(group5Elem1)
        .testEquals();
  }

  @Test
  public void testNullableSeveritySerialization()
      throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
    IssueConfigBean bean0 = new IssueConfigBean("major1", new MinorIssueConfig("minor1", 0, null));
    IssueConfigBean beanNull =
        new IssueConfigBean("major1", new MinorIssueConfig("minor1", null, null));

    IssueConfigBean bean0Deserialized =
        BatfishObjectMapper.mapper()
            .readValue(BatfishObjectMapper.writeString(bean0), IssueConfigBean.class);
    IssueConfigBean beanNullDeserialized =
        BatfishObjectMapper.mapper()
            .readValue(BatfishObjectMapper.writeString(beanNull), IssueConfigBean.class);

    new EqualsTester()
        .addEqualityGroup(bean0, bean0Deserialized)
        .addEqualityGroup(beanNull, beanNullDeserialized)
        .testEquals();
  }
}
