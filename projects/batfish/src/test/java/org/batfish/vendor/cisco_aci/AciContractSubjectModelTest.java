package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.batfish.vendor.cisco_aci.representation.apic.AciContractSubject;
import org.junit.Test;

/**
 * Tests for {@link AciContractSubject} model class.
 *
 * <p>This test class verifies JSON deserialization and all getters/setters for the Contract Subject
 * model and its nested attributes class.
 */
public class AciContractSubjectModelTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  public void testDeserializeAciContractSubject_fullAttributes() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"annotation\": \"test-annotation\","
            + "  \"consMatchT\": \"AtleastOne\","
            + "  \"descr\": \"Test subject description\","
            + "  \"dn\": \"uni/tn-tenant1/brc-contract1/subj-subject1\","
            + "  \"name\": \"subject1\","
            + "  \"nameAlias\": \"subject-alias\","
            + "  \"prio\": \"level1\","
            + "  \"provMatchT\": \"AtleastOne\","
            + "  \"revFltPorts\": \"yes\","
            + "  \"targetDscp\": \"CS1\","
            + "  \"userdom\": \"all\""
            + "}"
            + "}";

    AciContractSubject subject = MAPPER.readValue(json, AciContractSubject.class);
    assertThat(subject.getAttributes().getAnnotation(), equalTo("test-annotation"));
    assertThat(subject.getAttributes().getConsumerMatchType(), equalTo("AtleastOne"));
    assertThat(subject.getAttributes().getDescription(), equalTo("Test subject description"));
    assertThat(
        subject.getAttributes().getDistinguishedName(),
        equalTo("uni/tn-tenant1/brc-contract1/subj-subject1"));
    assertThat(subject.getAttributes().getName(), equalTo("subject1"));
    assertThat(subject.getAttributes().getNameAlias(), equalTo("subject-alias"));
    assertThat(subject.getAttributes().getPriority(), equalTo("level1"));
    assertThat(subject.getAttributes().getProviderMatchType(), equalTo("AtleastOne"));
    assertThat(subject.getAttributes().getReverseFilterPorts(), equalTo("yes"));
    assertThat(subject.getAttributes().getTargetDscp(), equalTo("CS1"));
    assertThat(subject.getAttributes().getUserDomain(), equalTo("all"));
  }

  @Test
  public void testDeserializeAciContractSubject_basicFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"web-subject\","
            + "  \"descr\": \"Web traffic subject\""
            + "}"
            + "}";

    AciContractSubject subject = MAPPER.readValue(json, AciContractSubject.class);
    assertThat(subject.getAttributes().getName(), equalTo("web-subject"));
    assertThat(subject.getAttributes().getDescription(), equalTo("Web traffic subject"));
  }

  @Test
  public void testDeserializeAciContractSubject_matchTypes() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"consMatchT\": \"All\","
            + "  \"provMatchT\": \"AtleastOne\""
            + "}"
            + "}";

    AciContractSubject subject = MAPPER.readValue(json, AciContractSubject.class);
    assertThat(subject.getAttributes().getConsumerMatchType(), equalTo("All"));
    assertThat(subject.getAttributes().getProviderMatchType(), equalTo("AtleastOne"));
  }

  @Test
  public void testDeserializeAciContractSubject_priority() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"high-priority-subject\","
            + "  \"prio\": \"level1\""
            + "}"
            + "}";

    AciContractSubject subject = MAPPER.readValue(json, AciContractSubject.class);
    assertThat(subject.getAttributes().getName(), equalTo("high-priority-subject"));
    assertThat(subject.getAttributes().getPriority(), equalTo("level1"));
  }

  @Test
  public void testDeserializeAciContractSubject_dscpAndQos() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"qos-subject\","
            + "  \"targetDscp\": \"AF11\","
            + "  \"revFltPorts\": \"yes\""
            + "}"
            + "}";

    AciContractSubject subject = MAPPER.readValue(json, AciContractSubject.class);
    assertThat(subject.getAttributes().getName(), equalTo("qos-subject"));
    assertThat(subject.getAttributes().getTargetDscp(), equalTo("AF11"));
    assertThat(subject.getAttributes().getReverseFilterPorts(), equalTo("yes"));
  }

  @Test
  public void testDeserializeAciContractSubject_withChildren() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"subject-with-filters\""
            + "},"
            + "\"children\": ["
            + "  {\"filterRef\": {\"attributes\": {\"name\": \"filter1\"}}},"
            + "  {\"filterRef\": {\"attributes\": {\"name\": \"filter2\"}}}"
            + "]"
            + "}";

    AciContractSubject subject = MAPPER.readValue(json, AciContractSubject.class);
    assertThat(subject.getAttributes().getName(), equalTo("subject-with-filters"));
    assertThat(subject.getChildren(), hasSize(2));
  }

  @Test
  public void testDeserializeAciContractSubject_emptyObject() throws IOException {
    String json = "{}";

    AciContractSubject subject = MAPPER.readValue(json, AciContractSubject.class);
    assertThat(subject.getAttributes(), nullValue());
    assertThat(subject.getChildren(), nullValue());
  }

  @Test
  public void testDeserializeAciContractSubject_nullValues() throws IOException {
    String json = "{" + "\"attributes\": {" + "  \"name\": null," + "  \"descr\": null" + "}" + "}";

    AciContractSubject subject = MAPPER.readValue(json, AciContractSubject.class);
    assertThat(subject.getAttributes().getName(), nullValue());
    assertThat(subject.getAttributes().getDescription(), nullValue());
  }

  @Test
  public void testGettersSetters_annotation() {
    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();
    assertThat(attrs.getAnnotation(), nullValue());

    attrs.setAnnotation("test-annotation");
    assertEquals("test-annotation", attrs.getAnnotation());

    attrs.setAnnotation(null);
    assertThat(attrs.getAnnotation(), nullValue());

    attrs.setAnnotation("");
    assertEquals("", attrs.getAnnotation());
  }

  @Test
  public void testGettersSetters_consumerMatchType() {
    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();
    assertThat(attrs.getConsumerMatchType(), nullValue());

    attrs.setConsumerMatchType("AtleastOne");
    assertEquals("AtleastOne", attrs.getConsumerMatchType());

    attrs.setConsumerMatchType("All");
    assertEquals("All", attrs.getConsumerMatchType());

    attrs.setConsumerMatchType("None");
    assertEquals("None", attrs.getConsumerMatchType());
  }

  @Test
  public void testGettersSetters_description() {
    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();
    assertThat(attrs.getDescription(), nullValue());

    attrs.setDescription("Test description");
    assertEquals("Test description", attrs.getDescription());

    attrs.setDescription("");
    assertEquals("", attrs.getDescription());

    attrs.setDescription(null);
    assertThat(attrs.getDescription(), nullValue());
  }

  @Test
  public void testGettersSetters_distinguishedName() {
    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();
    assertThat(attrs.getDistinguishedName(), nullValue());

    attrs.setDistinguishedName("uni/tn-tenant1/brc-contract1/subj-subject1");
    assertEquals("uni/tn-tenant1/brc-contract1/subj-subject1", attrs.getDistinguishedName());
  }

  @Test
  public void testGettersSetters_name() {
    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();
    assertThat(attrs.getName(), nullValue());

    attrs.setName("web-subject");
    assertEquals("web-subject", attrs.getName());

    attrs.setName("db-subject");
    assertEquals("db-subject", attrs.getName());
  }

  @Test
  public void testGettersSetters_nameAlias() {
    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();
    assertThat(attrs.getNameAlias(), nullValue());

    attrs.setNameAlias("web-alias");
    assertEquals("web-alias", attrs.getNameAlias());

    attrs.setNameAlias(null);
    assertThat(attrs.getNameAlias(), nullValue());

    attrs.setNameAlias("");
    assertEquals("", attrs.getNameAlias());
  }

  @Test
  public void testGettersSetters_priority() {
    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();
    assertThat(attrs.getPriority(), nullValue());

    attrs.setPriority("level1");
    assertEquals("level1", attrs.getPriority());

    attrs.setPriority("level2");
    assertEquals("level2", attrs.getPriority());

    attrs.setPriority("level3");
    assertEquals("level3", attrs.getPriority());
  }

  @Test
  public void testGettersSetters_providerMatchType() {
    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();
    assertThat(attrs.getProviderMatchType(), nullValue());

    attrs.setProviderMatchType("AtleastOne");
    assertEquals("AtleastOne", attrs.getProviderMatchType());

    attrs.setProviderMatchType("All");
    assertEquals("All", attrs.getProviderMatchType());

    attrs.setProviderMatchType("None");
    assertEquals("None", attrs.getProviderMatchType());
  }

  @Test
  public void testGettersSetters_reverseFilterPorts() {
    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();
    assertThat(attrs.getReverseFilterPorts(), nullValue());

    attrs.setReverseFilterPorts("yes");
    assertEquals("yes", attrs.getReverseFilterPorts());

    attrs.setReverseFilterPorts("no");
    assertEquals("no", attrs.getReverseFilterPorts());
  }

  @Test
  public void testGettersSetters_targetDscp() {
    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();
    assertThat(attrs.getTargetDscp(), nullValue());

    attrs.setTargetDscp("CS1");
    assertEquals("CS1", attrs.getTargetDscp());

    attrs.setTargetDscp("AF11");
    assertEquals("AF11", attrs.getTargetDscp());

    attrs.setTargetDscp("EF");
    assertEquals("EF", attrs.getTargetDscp());
  }

  @Test
  public void testGettersSetters_userDomain() {
    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();
    assertThat(attrs.getUserDomain(), nullValue());

    attrs.setUserDomain("all");
    assertEquals("all", attrs.getUserDomain());

    attrs.setUserDomain("infra");
    assertEquals("infra", attrs.getUserDomain());

    attrs.setUserDomain(null);
    assertThat(attrs.getUserDomain(), nullValue());
  }

  @Test
  public void testGettersSetters_contractSubject() {
    AciContractSubject subject = new AciContractSubject();
    assertThat(subject.getAttributes(), nullValue());
    assertThat(subject.getChildren(), nullValue());

    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();
    attrs.setName("test-subject");
    subject.setAttributes(attrs);
    assertEquals(attrs, subject.getAttributes());

    subject.setAttributes(null);
    assertThat(subject.getAttributes(), nullValue());
  }

  @Test
  public void testGettersSetters_children() {
    AciContractSubject subject = new AciContractSubject();
    assertThat(subject.getChildren(), nullValue());

    // Children is List<Object>, so we can't easily test it without creating objects
    // Just test null and setting null
    subject.setChildren(null);
    assertThat(subject.getChildren(), nullValue());
  }

  @Test
  public void testGettersSetters_emptyStrings() {
    AciContractSubject.AciContractSubjectAttributes attrs =
        new AciContractSubject.AciContractSubjectAttributes();

    attrs.setAnnotation("");
    assertEquals("", attrs.getAnnotation());

    attrs.setDescription("");
    assertEquals("", attrs.getDescription());

    attrs.setDistinguishedName("");
    assertEquals("", attrs.getDistinguishedName());

    attrs.setName("");
    assertEquals("", attrs.getName());

    attrs.setNameAlias("");
    assertEquals("", attrs.getNameAlias());

    attrs.setPriority("");
    assertEquals("", attrs.getPriority());

    attrs.setConsumerMatchType("");
    assertEquals("", attrs.getConsumerMatchType());

    attrs.setProviderMatchType("");
    assertEquals("", attrs.getProviderMatchType());

    attrs.setReverseFilterPorts("");
    assertEquals("", attrs.getReverseFilterPorts());

    attrs.setTargetDscp("");
    assertEquals("", attrs.getTargetDscp());

    attrs.setUserDomain("");
    assertEquals("", attrs.getUserDomain());
  }

  @Test
  public void testDeserializeAciContractSubject_allMatchTypeCombinations() throws IOException {
    // Test consumerMatchType and providerMatchType combinations
    String[][] combinations = {
      {"All", "All"},
      {"All", "AtleastOne"},
      {"All", "None"},
      {"AtleastOne", "All"},
      {"AtleastOne", "AtleastOne"},
      {"AtleastOne", "None"},
      {"None", "All"},
      {"None", "AtleastOne"},
      {"None", "None"}
    };

    for (String[] combination : combinations) {
      String json =
          "{"
              + "\"attributes\": {"
              + "  \"consMatchT\": \""
              + combination[0]
              + "\","
              + "  \"provMatchT\": \""
              + combination[1]
              + "\""
              + "}"
              + "}";

      AciContractSubject subject = MAPPER.readValue(json, AciContractSubject.class);
      assertThat(subject.getAttributes().getConsumerMatchType(), equalTo(combination[0]));
      assertThat(subject.getAttributes().getProviderMatchType(), equalTo(combination[1]));
    }
  }

  @Test
  public void testDeserializeAciContractSubject_allPriorityLevels() throws IOException {
    String[] priorities = {"level1", "level2", "level3", "unspecified"};

    for (String priority : priorities) {
      String json =
          "{"
              + "\"attributes\": {"
              + "  \"name\": \"subject-"
              + priority
              + "\","
              + "  \"prio\": \""
              + priority
              + "\""
              + "}"
              + "}";

      AciContractSubject subject = MAPPER.readValue(json, AciContractSubject.class);
      assertThat(subject.getAttributes().getName(), equalTo("subject-" + priority));
      assertThat(subject.getAttributes().getPriority(), equalTo(priority));
    }
  }

  @Test
  public void testDeserializeAciContractSubject_dscpValues() throws IOException {
    String[] dscpValues = {
      "CS0", "CS1", "CS2", "CS3", "CS4", "CS5", "CS6", "CS7", "AF11", "AF12", "AF13", "AF21",
      "AF22", "AF23", "AF31", "AF32", "AF33", "AF41", "AF42", "AF43", "EF", "VA"
    };

    for (String dscp : dscpValues) {
      String json =
          "{"
              + "\"attributes\": {"
              + "  \"name\": \"subject-"
              + dscp
              + "\","
              + "  \"targetDscp\": \""
              + dscp
              + "\""
              + "}"
              + "}";

      AciContractSubject subject = MAPPER.readValue(json, AciContractSubject.class);
      assertThat(subject.getAttributes().getName(), equalTo("subject-" + dscp));
      assertThat(subject.getAttributes().getTargetDscp(), equalTo(dscp));
    }
  }
}
