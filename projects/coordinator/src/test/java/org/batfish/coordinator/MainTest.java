package org.batfish.coordinator;

import static org.batfish.coordinator.Main.readQuestionTemplate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.Maps;
import java.nio.file.Path;
import java.util.Map;
import org.batfish.common.util.CommonUtil;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link Main}. */
@RunWith(JUnit4.class)
public class MainTest {

  private static final String DUPLICATE_TEMPLATE_NAME = "duplicate_template";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testDuplicateNameReadQuestionTemplates() throws Exception {
    Map<String, String> questionTemplates = Maps.newHashMap();
    questionTemplates.put(DUPLICATE_TEMPLATE_NAME, "template_body");

    JSONObject testQuestion = new JSONObject();
    testQuestion.put(
        "instance",
        new JSONObject()
            .put("instanceName", DUPLICATE_TEMPLATE_NAME)
            .put("description", "test question description"));
    Path questionJsonPath = _folder.newFile("testquestion.json").toPath();
    CommonUtil.writeFile(questionJsonPath, testQuestion.toString());

    readQuestionTemplate(questionJsonPath, questionTemplates);

    assertThat(questionTemplates.keySet(), hasSize(1));
    assertThat(questionTemplates, hasKey(DUPLICATE_TEMPLATE_NAME));
    assertThat(
        questionTemplates.get(DUPLICATE_TEMPLATE_NAME),
        equalTo(
            "{\"instance\":{\"instanceName\":\"duplicate_template\",\"description\":\"test question description\"}}"));
  }
}
