package org.batfish.coordinator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.Container;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.config.Settings;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link WorkMgr}. */
public class WorkMgrTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private WorkMgr _manager;

  @Before
  public void initManager() throws Exception {
    Settings settings = new Settings(new String[] {});
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    Main.setLogger(logger);
    _manager = new WorkMgr(settings, logger);
  }

  @Test
  public void initContainerWithContainerName() {
    String initResult = _manager.initContainer("container", null);
    assertThat(initResult, equalTo("container"));
  }

  @Test
  public void initContainerWithContainerPrefix() {
    String initResult = _manager.initContainer(null, "containerPrefix");
    assertThat(initResult, startsWith("containerPrefix"));
  }

  @Test
  public void initContainerWithNullInput() {
    String initResult = _manager.initContainer(null, null);
    assertThat(initResult, startsWith("null_"));
  }

  @Test
  public void initExistingContainer() {
    _manager.initContainer("container", null);
    String expectedMessage = "Container 'container' already exists!";
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo(expectedMessage));
    _manager.initContainer("container", null);
  }

  @Test
  public void listEmptyQuestion() {
    _manager.initContainer("container", null);
    SortedSet<String> questions = _manager.listQuestions("container", false);
    assertThat(questions.isEmpty(), is(true));
  }

  @Test
  public void listQuestionNames() {
    String questionName = "publicquestion";
    // Leading __ means this question is an internal question
    // And should be hidden from listQuestions when verbose is false
    String internalQuestionName = "__internalquestion";
    _manager.initContainer("container", null);
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve("container").toAbsolutePath();
    Path questionsDir = containerDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    // Make sure the question directories are created
    assertThat(questionsDir.resolve(questionName).toFile().mkdirs(), is(true));
    assertThat(questionsDir.resolve(internalQuestionName).toFile().mkdirs(), is(true));

    SortedSet<String> questionsNotVerbose = _manager.listQuestions("container", false);
    SortedSet<String> questionsVerbose = _manager.listQuestions("container", true);

    // Only the public question should show up when verbose is false
    assertThat(questionsNotVerbose, equalTo(Sets.newHashSet(questionName)));

    // Both questions should show up when verbose is true
    assertThat(questionsVerbose, equalTo(Sets.newHashSet(questionName, internalQuestionName)));
  }

  @Test
  public void listQuestionWithNonExistContainer() {
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Container 'container' does not exist"));
    _manager.listQuestions("container", false);
  }

  @Test
  public void listSortedQuestionNames() {
    _manager.initContainer("container", null);
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve("container").toAbsolutePath();
    Path questionsDir = containerDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    assertTrue(questionsDir.resolve("nodes").toFile().mkdirs());
    assertTrue(questionsDir.resolve("access").toFile().mkdirs());
    assertTrue(questionsDir.resolve("initinfo").toFile().mkdirs());
    SortedSet<String> questions = _manager.listQuestions("container", false);
    assertThat(questions, hasSize(3));
    assertThat(questions, equalTo(Sets.newHashSet("access", "initinfo", "nodes")));
  }

  @Test
  public void getEmptyContainer() {
    _manager.initContainer("container", null);
    Container container = _manager.getContainer("container");
    assertThat(container, equalTo(Container.of("container", new TreeSet<>())));
  }

  @Test
  public void getNonEmptyContainer() {
    _manager.initContainer("container", null);
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve("container").toAbsolutePath();
    Path testrigPath = containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    Container container = _manager.getContainer("container");
    assertThat(
        container,
        equalTo(Container.of("container", Sets.newTreeSet(Collections.singleton("testrig")))));
  }

  @Test
  public void getNonExistContainer() {
    _thrown.expect(Exception.class);
    _thrown.expectMessage(equalTo("Container 'container' does not exist"));
    _manager.getContainer("container");
  }

  @Test
  public void getConfigNonExistContainer() {
    _thrown.expect(Exception.class);
    _thrown.expectMessage(equalTo("Container 'container' does not exist"));
    _manager.getConfiguration("container", "testrig", "config.cfg");
  }

  @Test
  public void getNonExistConfig() {
    _manager.initContainer("container", null);
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve("container").toAbsolutePath();
    Path testrigPath =
        containerDir.resolve(
            Paths.get(BfConsts.RELPATH_TESTRIGS_DIR, "testrig", BfConsts.RELPATH_TEST_RIG_DIR));
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    _thrown.expect(Exception.class);
    _thrown.expectMessage(
        equalTo(
            "Configuration file config.cfg does not exist in testrig testrig "
                + "for container container"));
    _manager.getConfiguration("container", "testrig", "config.cfg");
  }

  @Test
  public void getConfigContent() {
    _manager.initContainer("container", null);
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve("container").toAbsolutePath();
    Path configPath =
        containerDir.resolve(
            Paths.get(
                BfConsts.RELPATH_TESTRIGS_DIR,
                "testrig",
                BfConsts.RELPATH_TEST_RIG_DIR,
                BfConsts.RELPATH_CONFIGURATIONS_DIR));
    assertTrue(configPath.toFile().mkdirs());
    CommonUtil.writeFile(configPath.resolve("config.cfg"), "config content");
    String result = _manager.getConfiguration("container", "testrig", "config.cfg");
    assertThat(result, equalTo("config content"));
  }

  @Test
  public void testListAnalysesSuggested() {
    String containerName = "myContainer";
    _manager.initContainer(containerName, null);

    // Create analysis1 (user analysis) and analysis2 (suggested analysis)
    _manager.configureAnalysis(
        containerName, true, "analysis1", Maps.newHashMap(), Lists.newArrayList(), false);
    _manager.configureAnalysis(
        containerName, true, "analysis2", Maps.newHashMap(), Lists.newArrayList(), true);

    SortedSet<String> analyses = _manager.listAnalyses(containerName, null);
    assertTrue("User analyses listed if suggested is null", analyses.contains("analysis1"));
    assertTrue("Suggested analyses listed if suggested is null", analyses.contains("analysis2"));

    analyses = _manager.listAnalyses(containerName, false);
    assertTrue("User analyses listed if suggested is false", analyses.contains("analysis1"));
    assertFalse(
        "Suggested analyses not listed if suggested is false", analyses.contains("analysis2"));

    analyses = _manager.listAnalyses(containerName, true);
    assertFalse("User analyses not listed if suggested is true", analyses.contains("analysis1"));
    assertTrue("Suggested analyses listed if suggested is true", analyses.contains("analysis2"));
  }

  @Test
  public void testConfigureAnalysis() {
    String containerName = "myContainer";
    _manager.initContainer(containerName, null);
    // test init and add questions to analysis
    Map<String, String> questionsToAdd =
        Maps.newHashMap(Collections.singletonMap("question1", "question1Content"));
    _manager.configureAnalysis(
        containerName, true, "analysis", questionsToAdd, Lists.newArrayList(), null);
    questionsToAdd = Maps.newHashMap(Collections.singletonMap("question2", "question2Content"));
    questionsToAdd.put("question3", "question3Content");
    _manager.configureAnalysis(
        containerName, false, "analysis", questionsToAdd, Lists.newArrayList(), null);
    Path questionPath =
        _folder
            .getRoot()
            .toPath()
            .resolve(
                Paths.get(
                    containerName,
                    BfConsts.RELPATH_ANALYSES_DIR,
                    "analysis",
                    BfConsts.RELPATH_QUESTIONS_DIR));
    Path qFile = questionPath.resolve(Paths.get("question1", BfConsts.RELPATH_QUESTION_FILE));
    Path otherQFile = questionPath.resolve(Paths.get("question2", BfConsts.RELPATH_QUESTION_FILE));
    try {
      String actual = new String(Files.readAllBytes(qFile));
      assertThat(actual, equalTo("question1Content"));
      actual = new String(Files.readAllBytes(otherQFile));
      assertThat(actual, equalTo("question2Content"));
    } catch (IOException e) {
      throw new BatfishException("Failed to read question content", e);
    }

    // test delete questions
    List<String> questionsToDelete = Lists.newArrayList();
    _manager.configureAnalysis(
        containerName, false, "analysis", Maps.newHashMap(), questionsToDelete, null);
    assertTrue(
        Files.exists(questionPath.resolve("question1"))
            && Files.exists(questionPath.resolve("question2"))
            && Files.exists(questionPath.resolve("question3")));
    questionsToDelete = Lists.newArrayList("question1", "question2");
    _manager.configureAnalysis(
        containerName, false, "analysis", Maps.newHashMap(), questionsToDelete, null);
    assertFalse(Files.exists(questionPath.resolve("question1")));
    assertFalse(Files.exists(questionPath.resolve("question2")));
    assertTrue(Files.exists(questionPath.resolve("question3")));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Question question1 does not exist for analysis analysis"));
    questionsToDelete = Lists.newArrayList("question1");
    _manager.configureAnalysis(
        containerName, false, "analysis", Maps.newHashMap(), questionsToDelete, null);
  }

  @Test
  public void testConfigureAnalysisSuggested() {
    String containerName = "myContainer";
    _manager.initContainer(containerName, null);

    // Analysis initialized with suggested = null should not be marked as suggested
    _manager.configureAnalysis(
        containerName, true, "analysis", Maps.newHashMap(), Lists.newArrayList(), null);
    assertFalse(getMetadataSuggested(containerName, "analysis"));

    // Analysis initialized with suggested = true should be marked as suggested
    _manager.configureAnalysis(
        containerName, true, "analysis2", Maps.newHashMap(), Lists.newArrayList(), true);
    assertTrue(getMetadataSuggested(containerName, "analysis2"));

    // Analysis initialized with suggested = false should not be marked as suggested
    _manager.configureAnalysis(
        containerName, true, "analysis3", Maps.newHashMap(), Lists.newArrayList(), false);
    assertFalse(getMetadataSuggested(containerName, "analysis3"));

    // Existing analysis should not change suggested if suggested arg is null
    _manager.configureAnalysis(
        containerName, false, "analysis2", Maps.newHashMap(), Lists.newArrayList(), null);
    assertTrue(getMetadataSuggested(containerName, "analysis2"));
    _manager.configureAnalysis(
        containerName, false, "analysis3", Maps.newHashMap(), Lists.newArrayList(), null);
    assertFalse(getMetadataSuggested(containerName, "analysis3"));

    // Existing analysis should update suggested if arg is not null
    _manager.configureAnalysis(
        containerName, false, "analysis2", Maps.newHashMap(), Lists.newArrayList(), false);
    assertFalse(getMetadataSuggested(containerName, "analysis2"));
    _manager.configureAnalysis(
        containerName, false, "analysis3", Maps.newHashMap(), Lists.newArrayList(), true);
    assertTrue(getMetadataSuggested(containerName, "analysis3"));
  }

  private boolean getMetadataSuggested(String containerName, String analysisName) {
    try {
      return AnalysisMetadataMgr.readMetadata(containerName, analysisName).getSuggested();
    } catch (IOException e) {
      throw new BatfishException("Failed to read metadata", e);
    }
  }
}
