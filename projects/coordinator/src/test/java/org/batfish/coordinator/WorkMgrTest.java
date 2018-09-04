package org.batfish.coordinator;

import static org.batfish.coordinator.WorkMgr.generateFileDateString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.AnalysisAnswerOptions;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.ColumnFilter;
import org.batfish.common.ColumnSortOption;
import org.batfish.common.Container;
import org.batfish.common.WorkItem;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.WorkItemBuilder;
import org.batfish.coordinator.AnalysisMetadataMgr.AnalysisType;
import org.batfish.datamodel.TestrigMetadata;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.Issue;
import org.batfish.datamodel.answers.Metrics;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.pojo.Topology;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.storage.StorageProvider;
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

  private StorageProvider _storage;

  @Before
  public void initManager() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
    _manager = Main.getWorkMgr();
    _storage = _manager.getStorage();
  }

  private static void createTestrigWithMetadata(String container, String testrig)
      throws IOException {
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve(container).toAbsolutePath();
    Files.createDirectories(containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve(testrig));
    TestrigMetadataMgr.writeMetadata(
        new TestrigMetadata(new Date().toInstant(), "env"), container, testrig);
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
    assertThat(questions, equalTo(Sets.newHashSet("access", "initinfo", "nodes")));
  }

  @Test
  public void getEmptyContainer() {
    _manager.initContainer("container", null);
    Container container = _manager.getContainer("container");
    assertThat(container, equalTo(Container.of("container", new TreeSet<>())));
  }

  @Test
  public void getLatestTestrig() throws IOException {
    _manager.initContainer("container", null);

    // empty should be returned if no testrigs exist
    assertThat(_manager.getLatestTestrig("container"), equalTo(Optional.empty()));

    // create testrig1, which should be returned
    createTestrigWithMetadata("container", "testrig1");
    assertThat(_manager.getLatestTestrig("container"), equalTo(Optional.of("testrig1")));

    // create a second testrig, which should be returned
    createTestrigWithMetadata("container", "testrig2");
    assertThat(_manager.getLatestTestrig("container"), equalTo(Optional.of("testrig2")));
  }

  @Test
  public void getNodes() throws IOException {
    _manager.initContainer("container", null);

    // create a testrig and write a topology object for it
    createTestrigWithMetadata("container", "testrig1");
    Topology topology = new Topology("testrig1");
    topology.setNodes(ImmutableSet.of(new Node("a1"), new Node("b1")));
    CommonUtil.writeFile(
        _manager
            .getdirTestrig("container", "testrig1")
            .resolve(BfConsts.RELPATH_TESTRIG_POJO_TOPOLOGY_PATH),
        BatfishObjectMapper.mapper().writeValueAsString(topology));

    // should get the nodes of the topology when we ask for it
    assertThat(_manager.getNodes("container", "testrig1"), equalTo(ImmutableSet.of("a1", "b1")));
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
            "Configuration file config.cfg does not exist in snapshot testrig "
                + "for network container"));
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

    // checking that we get analyses according to AnalysisType
    assertThat(
        _manager.listAnalyses(containerName, AnalysisType.ALL),
        equalTo(Sets.newHashSet("analysis1", "analysis2")));
    assertThat(
        _manager.listAnalyses(containerName, AnalysisType.USER),
        equalTo(Sets.newHashSet("analysis1")));
    assertThat(
        _manager.listAnalyses(containerName, AnalysisType.SUGGESTED),
        equalTo(Sets.newHashSet("analysis2")));
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
    _thrown.expectMessage(equalTo("Question 'question1' does not exist for analysis 'analysis'"));
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

  @Test
  public void testGetAnalysisAnswer() throws JsonProcessingException, FileNotFoundException {
    String containerName = "container1";
    String testrigName = "testrig1";
    String analysisName = "analysis1";
    String question1Name = "question1";
    String question1Content = "question1Content";
    String question2Name = "question2Name";
    String question2Content = "question2Content";
    String question3Name = "question3";
    String question3Content = "question3Content";
    String answer1 = "answer1";
    String answer2 = "answer2";

    _manager.initContainer(containerName, null);
    Map<String, String> questionsToAdd =
        Maps.newHashMap(Collections.singletonMap(question1Name, question1Content));
    questionsToAdd.put(question2Name, question2Content);
    questionsToAdd.put(question3Name, question3Content);

    _manager.configureAnalysis(
        containerName, true, analysisName, questionsToAdd, Lists.newArrayList(), null);

    _storage.storeAnswer(answer1, containerName, testrigName, question1Name, null, analysisName);
    _storage.storeAnswer(answer2, containerName, testrigName, question2Name, null, analysisName);

    String answer1Output =
        _manager.getAnswer(containerName, testrigName, question1Name, null, analysisName);
    String answer2Output =
        _manager.getAnswer(containerName, testrigName, question2Name, null, analysisName);
    String answer3Output =
        _manager.getAnswer(containerName, testrigName, question3Name, null, analysisName);

    Answer failedAnswer = Answer.failureAnswer("Not answered", null);
    failedAnswer.setStatus(AnswerStatus.NOTFOUND);
    String failedAnswerString = BatfishObjectMapper.writePrettyString(failedAnswer);

    assertThat(answer1Output, equalTo(answer1));
    assertThat(answer2Output, equalTo(answer2));
    assertThat(answer3Output, equalTo(failedAnswerString));
  }

  @Test
  public void testGetAnalysisAnswers() throws JsonProcessingException, FileNotFoundException {
    String containerName = "container1";
    String testrigName = "testrig1";
    String analysisName = "analysis1";
    String question1Name = "question1";
    String question1Content = "question1Content";
    String question2Name = "question2Name";
    String question2Content = "question2Content";
    String answer1 = "answer1";
    String answer2 = "answer2";

    _manager.initContainer(containerName, null);
    Map<String, String> questionsToAdd =
        Maps.newHashMap(Collections.singletonMap(question1Name, question1Content));
    questionsToAdd.put(question2Name, question2Content);

    _manager.configureAnalysis(
        containerName, true, analysisName, questionsToAdd, Lists.newArrayList(), null);

    Path answer1Dir =
        _folder
            .getRoot()
            .toPath()
            .resolve(
                Paths.get(
                    containerName,
                    BfConsts.RELPATH_TESTRIGS_DIR,
                    testrigName,
                    BfConsts.RELPATH_ANALYSES_DIR,
                    analysisName,
                    BfConsts.RELPATH_QUESTIONS_DIR,
                    question1Name,
                    BfConsts.RELPATH_ENVIRONMENTS_DIR,
                    BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME));

    Path answer2Dir =
        _folder
            .getRoot()
            .toPath()
            .resolve(
                Paths.get(
                    containerName,
                    BfConsts.RELPATH_TESTRIGS_DIR,
                    testrigName,
                    BfConsts.RELPATH_ANALYSES_DIR,
                    analysisName,
                    BfConsts.RELPATH_QUESTIONS_DIR,
                    question2Name,
                    BfConsts.RELPATH_ENVIRONMENTS_DIR,
                    BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME));

    Path answer1Path = answer1Dir.resolve(BfConsts.RELPATH_ANSWER_JSON);
    Path answer2Path = answer2Dir.resolve(BfConsts.RELPATH_ANSWER_JSON);

    answer1Dir.toFile().mkdirs();
    answer2Dir.toFile().mkdirs();

    CommonUtil.writeFile(answer1Path, answer1);
    CommonUtil.writeFile(answer2Path, answer2);

    Map<String, String> answers1 =
        _manager.getAnalysisAnswers(
            containerName,
            testrigName,
            BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
            null,
            null,
            analysisName,
            ImmutableSet.of());
    Map<String, String> answers2 =
        _manager.getAnalysisAnswers(
            containerName,
            testrigName,
            BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
            null,
            null,
            analysisName,
            ImmutableSet.of(question1Name));
    Map<String, String> answers3 =
        _manager.getAnalysisAnswers(
            containerName,
            testrigName,
            BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
            null,
            null,
            analysisName,
            ImmutableSet.of());

    assertThat(answers1, equalTo(ImmutableMap.of(question1Name, answer1, question2Name, answer2)));
    assertThat(answers2, equalTo(ImmutableMap.of(question1Name, answer1)));
    assertThat(answers3, equalTo(ImmutableMap.of(question1Name, answer1, question2Name, answer2)));
  }

  @Test
  public void testGetAutoWorkQueueUserAnalysis() {
    String containerName = "myContainer";
    String testrigName = "myTestrig";
    _manager.initContainer(containerName, null);

    // user policy
    _manager.configureAnalysis(
        containerName, true, "useranalysis", Maps.newHashMap(), Lists.newArrayList(), false);

    WorkItem parseWorkItem = WorkItemBuilder.getWorkItemParse(containerName, testrigName);

    WorkItem analysisWorkItem =
        WorkItemBuilder.getWorkItemRunAnalysis(
            "useranalysis",
            containerName,
            testrigName,
            BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
            null,
            null,
            false,
            false);

    List<WorkItem> workQueue = _manager.getAutoWorkQueue(containerName, testrigName);

    assertThat(workQueue, hasSize(2));

    // checking that the first work item is for parse
    assertThat(workQueue.get(0).matches(parseWorkItem), equalTo(true));

    // checking run analysis workitem
    assertThat(
        "Work Queue not correct for user analyses",
        workQueue.get(1).matches(analysisWorkItem),
        equalTo(true));
  }

  @Test
  public void testGetAnswerMetadataAnalysisSuccess()
      throws JsonProcessingException, FileNotFoundException {
    String networkName = "network1";
    String snapshotName = "snapshot1";
    String analysisName = "analysis1";
    String questionName = "question1";
    String questionContent = "{}";
    AnswerMetadata answerMetadata =
        new AnswerMetadata(new Metrics(ImmutableMap.of(), 1), AnswerStatus.SUCCESS);
    _manager.initContainer(networkName, null);
    _manager.configureAnalysis(
        networkName,
        true,
        analysisName,
        ImmutableMap.of(questionName, questionContent),
        ImmutableList.of(),
        null);
    _storage.storeAnswerMetadata(
        answerMetadata, networkName, snapshotName, questionName, null, analysisName);
    AnswerMetadata answerResult =
        _manager.getAnswerMetadata(networkName, snapshotName, questionName, null, analysisName);

    assertThat(answerResult, equalTo(answerMetadata));
  }

  @Test
  public void testGetAnswerMetadataAnalysisMissingQuestion()
      throws JsonProcessingException, FileNotFoundException {
    String networkName = "network1";
    String snapshotName = "snapshot1";
    String analysisName = "analysis1";
    String questionName = "question1";
    AnswerMetadata answerMetadata =
        new AnswerMetadata(new Metrics(ImmutableMap.of(), 1), AnswerStatus.SUCCESS);
    _manager.initContainer(networkName, null);
    _manager.configureAnalysis(
        networkName, true, analysisName, ImmutableMap.of(), ImmutableList.of(), null);
    _storage.storeAnswerMetadata(
        answerMetadata, networkName, snapshotName, questionName, null, analysisName);

    _thrown.expect(FileNotFoundException.class);
    _manager.getAnswerMetadata(networkName, snapshotName, questionName, null, analysisName);
  }

  @Test
  public void testGetAnswerMetadataAnalysisMissingAnswerMetadata()
      throws JsonProcessingException, FileNotFoundException {
    String networkName = "network1";
    String snapshotName = "snapshot1";
    String analysisName = "analysis1";
    String questionName = "question1";
    String questionContent = "{}";
    _manager.initContainer(networkName, null);
    _manager.configureAnalysis(
        networkName,
        true,
        analysisName,
        ImmutableMap.of(questionName, questionContent),
        ImmutableList.of(),
        null);
    _storage.storeAnswer("answer", networkName, snapshotName, questionName, null, analysisName);
    AnswerMetadata answerResult =
        _manager.getAnswerMetadata(networkName, snapshotName, questionName, null, analysisName);

    assertThat(answerResult, equalTo(new AnswerMetadata(null, AnswerStatus.NOTFOUND)));
  }

  @Test
  public void testGetAnswerMetadataAnalysisMissingAnalysis()
      throws JsonProcessingException, FileNotFoundException {
    String networkName = "network1";
    String snapshotName = "snapshot1";
    String analysisName = "analysis1";
    String questionName = "question1";
    AnswerMetadata answerMetadata =
        new AnswerMetadata(new Metrics(ImmutableMap.of(), 1), AnswerStatus.SUCCESS);
    _manager.initContainer(networkName, null);
    _storage.storeAnswerMetadata(
        answerMetadata, networkName, snapshotName, questionName, null, analysisName);

    _thrown.expect(Exception.class);
    _manager.getAnswerMetadata(networkName, snapshotName, questionName, null, analysisName);
  }

  @Test
  public void testGetAnswerMetadataAdHocSuccess()
      throws JsonProcessingException, FileNotFoundException {
    String networkName = "network1";
    String snapshotName = "snapshot1";
    String questionContent = "{}";
    String questionName = "question2Name";
    AnswerMetadata answerMetadata =
        new AnswerMetadata(new Metrics(ImmutableMap.of(), 2), AnswerStatus.SUCCESS);
    _manager.initContainer(networkName, null);
    _manager.uploadQuestion(networkName, questionName, questionContent, false);
    _storage.storeAnswerMetadata(
        answerMetadata, networkName, snapshotName, questionName, null, null);
    AnswerMetadata answer2Result =
        _manager.getAnswerMetadata(networkName, snapshotName, questionName, null, null);

    assertThat(answer2Result, equalTo(answerMetadata));
  }

  @Test
  public void testGetAnswerMetadataAdHocMissingAnswerMetadata()
      throws JsonProcessingException, FileNotFoundException {
    String networkName = "network1";
    String snapshotName = "snapshot1";
    String questionContent = "{}";
    String questionName = "question2Name";
    _manager.initContainer(networkName, null);
    _manager.uploadQuestion(networkName, questionName, questionContent, false);
    _storage.storeAnswer("answer", networkName, snapshotName, questionName, null, null);
    AnswerMetadata answer2Result =
        _manager.getAnswerMetadata(networkName, snapshotName, questionName, null, null);

    assertThat(answer2Result, equalTo(new AnswerMetadata(null, AnswerStatus.NOTFOUND)));
  }

  @Test
  public void testGetAnswerMetadataAdHocMissingQuestion()
      throws JsonProcessingException, FileNotFoundException {
    String networkName = "network1";
    String snapshotName = "snapshot1";
    String questionName = "question2Name";
    AnswerMetadata answerMetadata =
        new AnswerMetadata(new Metrics(ImmutableMap.of(), 2), AnswerStatus.SUCCESS);
    _manager.initContainer(networkName, null);
    _storage.storeAnswerMetadata(
        answerMetadata, networkName, snapshotName, questionName, null, null);

    _thrown.expect(Exception.class);
    _manager.getAnswerMetadata(networkName, snapshotName, null, null, questionName);
  }

  @Test
  public void testGetAutoWorkQueueSuggestedAnalysis() {
    String containerName = "myContainer";
    String testrigName = "myTestrig";
    _manager.initContainer(containerName, null);

    // user policy
    _manager.configureAnalysis(
        containerName, true, "suggestedanalysis", Maps.newHashMap(), Lists.newArrayList(), true);

    WorkItem parseWorkItem = WorkItemBuilder.getWorkItemParse(containerName, testrigName);

    WorkItem analysisWorkItem =
        WorkItemBuilder.getWorkItemRunAnalysis(
            "suggestedanalysis",
            containerName,
            testrigName,
            BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
            null,
            null,
            false,
            false);

    List<WorkItem> workQueue = _manager.getAutoWorkQueue(containerName, testrigName);

    assertThat(workQueue, hasSize(2));

    // checking that the first work item is for parse
    assertThat(workQueue.get(0).matches(parseWorkItem), equalTo(true));

    // checking run analysis workitem
    assertThat(
        "Work Queue not correct for suggested analyses",
        workQueue.get(1).matches(analysisWorkItem),
        equalTo(true));
  }

  private boolean getMetadataSuggested(String containerName, String analysisName) {
    try {
      return AnalysisMetadataMgr.readMetadata(containerName, analysisName).getSuggested();
    } catch (IOException e) {
      throw new BatfishException("Failed to read metadata", e);
    }
  }

  @Test
  public void testGenerateDateString() {
    assertThat(
        generateFileDateString("foo", Instant.parse("2018-04-19T12:34:56Z")),
        equalTo("foo_2018-04-19T12-34-56.000"));

    assertThat(
        generateFileDateString(
            "foo",
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .parse("2018-04-19T12:34:56-08:00")
                .query(Instant::from)),
        equalTo("foo_2018-04-19T20-34-56.000"));
  }

  @Test
  public void testProcessAnalysisAnswers() throws IOException {
    String questionName = "q";
    String columnName = "issue";
    int maxRows = 1;
    int rowOffset = 0;

    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.ISSUE, "foobar"))));
    table.addRow(Row.of(columnName, new Issue("blah", 5, new Issue.Type("m", "n"))));
    Answer answer = new Answer();
    answer.addAnswerElement(table);
    answer.setStatus(AnswerStatus.SUCCESS);
    String answerStr = BatfishObjectMapper.writePrettyString(answer);
    Map<String, String> rawAnswers = ImmutableMap.of(questionName, answerStr);
    AnalysisAnswerOptions options =
        new AnalysisAnswerOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            maxRows,
            rowOffset,
            ImmutableList.of(new ColumnSortOption(columnName, true)),
            false);
    Map<String, AnalysisAnswerOptions> analysisAnswersOptions =
        ImmutableMap.of(questionName, options);

    Map<String, Answer> processedAnswers =
        _manager.processAnalysisAnswers(rawAnswers, analysisAnswersOptions);
    List<Row> processedRows =
        ((TableAnswerElement) processedAnswers.get(questionName).getAnswerElements().get(0))
            .getRowsList();

    assertThat(processedRows, equalTo(table.getRowsList()));
  }

  @Test
  public void testProcessAnalysisAnswer() throws IOException {
    String columnName = "issue";
    int maxRows = 1;
    int rowOffset = 0;
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.ISSUE, "foobar"))));
    table.addRow(Row.of(columnName, new Issue("blah", 5, new Issue.Type("m", "n"))));
    Answer answer = new Answer();
    answer.addAnswerElement(table);
    answer.setStatus(AnswerStatus.SUCCESS);
    String answerStr = BatfishObjectMapper.writePrettyString(answer);
    AnalysisAnswerOptions options =
        new AnalysisAnswerOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            maxRows,
            rowOffset,
            ImmutableList.of(new ColumnSortOption(columnName, true)),
            false);

    List<Row> processedRows =
        ((TableAnswerElement)
                _manager.processAnalysisAnswer(answerStr, options).getAnswerElements().get(0))
            .getRowsList();

    assertThat(processedRows, equalTo(table.getRowsList()));
  }

  @Test
  public void testProcessAnalysisAnswerFailure() throws IOException {
    String columnName = "issue";
    int maxRows = 1;
    int rowOffset = 0;
    AnalysisAnswerOptions options =
        new AnalysisAnswerOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            maxRows,
            rowOffset,
            ImmutableList.of(new ColumnSortOption(columnName, true)),
            false);
    Answer badInput = new Answer();
    badInput.setStatus(AnswerStatus.SUCCESS);
    String rawAnswerStr = BatfishObjectMapper.writePrettyString(badInput);
    Answer processedAnswer = _manager.processAnalysisAnswer(rawAnswerStr, options);

    assertThat(processedAnswer.getStatus(), equalTo(AnswerStatus.FAILURE));
  }

  @Test
  public void testProcessAnalysisAnswerNotFound() throws IOException {
    String columnName = "issue";
    int maxRows = 1;
    int rowOffset = 0;
    AnalysisAnswerOptions options =
        new AnalysisAnswerOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            maxRows,
            rowOffset,
            ImmutableList.of(new ColumnSortOption(columnName, true)),
            false);
    Answer processedAnswer = _manager.processAnalysisAnswer(null, options);

    assertThat(processedAnswer.getStatus(), equalTo(AnswerStatus.NOTFOUND));
  }

  @Test
  public void testProcessAnalysisAnswerTableSorting() {
    String columnName = "val";
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar"))));
    Row row1 = Row.of(columnName, 1);
    Row row2 = Row.of(columnName, 2);
    table.addRow(row1);
    table.addRow(row2);
    AnalysisAnswerOptions optionsSorting =
        new AnalysisAnswerOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(new ColumnSortOption(columnName, false)),
            false);
    AnalysisAnswerOptions optionsSortingReverse =
        new AnalysisAnswerOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(new ColumnSortOption(columnName, true)),
            false);

    assertThat(
        _manager.processAnalysisAnswerTable(table, optionsSorting).getRowsList(),
        equalTo(ImmutableList.of(row1, row2)));
    assertThat(
        _manager.processAnalysisAnswerTable(table, optionsSortingReverse).getRowsList(),
        equalTo(ImmutableList.of(row2, row1)));
  }

  @Test
  public void testProcessAnalysisAnswerTableOffset() {
    String columnName = "val";
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar"))));
    Row row1 = Row.of(columnName, 1);
    Row row2 = Row.of(columnName, 2);
    table.addRow(row1);
    table.addRow(row2);
    AnalysisAnswerOptions optionsNoOffset =
        new AnalysisAnswerOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(),
            false);
    AnalysisAnswerOptions optionsOffset =
        new AnalysisAnswerOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            Integer.MAX_VALUE,
            1,
            ImmutableList.of(),
            false);

    assertThat(
        _manager.processAnalysisAnswerTable(table, optionsNoOffset).getRowsList(),
        equalTo(ImmutableList.of(row1, row2)));
    assertThat(
        _manager.processAnalysisAnswerTable(table, optionsOffset).getRowsList(),
        equalTo(ImmutableList.of(row2)));
  }

  @Test
  public void testProcessAnalysisAnswerTableFiltered() {
    String columnName = "val";
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.STRING, "foobar"))));
    String whitelistedValue = "hello";
    Row row1 = Row.of(columnName, whitelistedValue);
    Row row2 = Row.of(columnName, "goodbye");
    table.addRow(row1);
    table.addRow(row2);
    AnalysisAnswerOptions optionsNotFiltered =
        new AnalysisAnswerOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), false);
    AnalysisAnswerOptions optionsFiltered =
        new AnalysisAnswerOptions(
            ImmutableSet.of(),
            ImmutableList.of(new ColumnFilter(columnName, whitelistedValue)),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(),
            false);

    assertThat(
        _manager.processAnalysisAnswerTable(table, optionsNotFiltered).getRowsList(),
        equalTo(ImmutableList.of(row1, row2)));
    assertThat(
        _manager.processAnalysisAnswerTable(table, optionsFiltered).getRowsList(),
        equalTo(ImmutableList.of(row1)));
  }

  @Test
  public void testProcessAnalysisAnswerTableMaxRows() {
    String columnName = "val";
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar"))));
    Row row1 = Row.of(columnName, 1);
    Row row2 = Row.of(columnName, 2);
    table.addRow(row1);
    table.addRow(row2);
    AnalysisAnswerOptions optionsNoLimit =
        new AnalysisAnswerOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), false);
    AnalysisAnswerOptions optionsLimit =
        new AnalysisAnswerOptions(
            ImmutableSet.of(), ImmutableList.of(), 1, 0, ImmutableList.of(), false);

    assertThat(
        _manager.processAnalysisAnswerTable(table, optionsNoLimit).getRowsList(),
        equalTo(ImmutableList.of(row1, row2)));
    assertThat(
        _manager.processAnalysisAnswerTable(table, optionsLimit).getRowsList(),
        equalTo(ImmutableList.of(row1)));
  }

  @Test
  public void testProcessAnalysisAnswerTableProject() {
    String columnName = "val";
    String otherColumnName = "val2";
    TableMetadata originalMetadata =
        new TableMetadata(
            ImmutableList.of(
                new ColumnMetadata(columnName, Schema.INTEGER, "foobar"),
                new ColumnMetadata(otherColumnName, Schema.INTEGER, "foobaz")));
    TableAnswerElement table = new TableAnswerElement(originalMetadata);
    Row row1 = Row.of(columnName, 1, otherColumnName, 3);
    Row row2 = Row.of(columnName, 2, otherColumnName, 4);
    table.addRow(row1);
    table.addRow(row2);
    AnalysisAnswerOptions optionsNoProject =
        new AnalysisAnswerOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), false);
    AnalysisAnswerOptions optionsProject =
        new AnalysisAnswerOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(),
            false);

    Row row1Projected = Row.of(columnName, 1);
    Row row2Projected = Row.of(columnName, 2);

    assertThat(
        _manager.processAnalysisAnswerTable(table, optionsNoProject).getRowsList(),
        equalTo(ImmutableList.of(row1, row2)));
    assertThat(
        _manager.processAnalysisAnswerTable(table, optionsProject).getRowsList(),
        equalTo(ImmutableList.of(row1Projected, row2Projected)));
  }

  @Test
  public void testProcessAnalysisAnswerTableUniqueRows() {
    String columnName = "val";
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar"))));
    Row row1 = Row.of(columnName, 1);
    Row row2 = Row.of(columnName, 1);
    table.addRow(row1);
    table.addRow(row2);
    AnalysisAnswerOptions optionsNonUnique =
        new AnalysisAnswerOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), false);
    AnalysisAnswerOptions optionsUnique =
        new AnalysisAnswerOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), true);

    assertThat(
        _manager.processAnalysisAnswerTable(table, optionsNonUnique).getRowsList(),
        equalTo(ImmutableList.of(row1, row2)));
    assertThat(
        _manager.processAnalysisAnswerTable(table, optionsUnique).getRowsList(),
        equalTo(ImmutableList.of(row1)));
  }

  @Test
  public void testBuildComparator() {
    String col1 = "col1";
    String col2 = "col2";
    Map<String, ColumnMetadata> rawColumnMap =
        ImmutableMap.of(
            col1,
            new ColumnMetadata(col1, Schema.INTEGER, "blah"),
            col2,
            new ColumnMetadata(col2, Schema.INTEGER, "bloop"));
    Comparator<Row> comCol1 =
        _manager.buildComparator(rawColumnMap, ImmutableList.of(new ColumnSortOption(col1, false)));
    Comparator<Row> comCol1Reversed =
        _manager.buildComparator(rawColumnMap, ImmutableList.of(new ColumnSortOption(col1, true)));
    Comparator<Row> comCol2 =
        _manager.buildComparator(rawColumnMap, ImmutableList.of(new ColumnSortOption(col2, false)));
    Comparator<Row> comCol2Reversed =
        _manager.buildComparator(rawColumnMap, ImmutableList.of(new ColumnSortOption(col2, true)));
    Comparator<Row> comCol1Then2 =
        _manager.buildComparator(
            rawColumnMap,
            ImmutableList.of(new ColumnSortOption(col1, false), new ColumnSortOption(col2, false)));
    Comparator<Row> comCol2Then1 =
        _manager.buildComparator(
            rawColumnMap,
            ImmutableList.of(new ColumnSortOption(col2, false), new ColumnSortOption(col1, false)));

    Row row1 = Row.of(col1, 1, col2, 10);
    Row row2 = Row.of(col1, 1, col2, 20);
    Row row3 = Row.of(col1, 2, col2, 10);
    Row row4 = Row.of(col1, 2, col2, 20);

    assertThat(comCol1.compare(row1, row2), equalTo(0));
    assertThat(comCol1.compare(row1, row3), equalTo(-1));
    assertThat(comCol1.compare(row1, row4), equalTo(-1));
    assertThat(comCol1.compare(row2, row3), equalTo(-1));
    assertThat(comCol1.compare(row2, row4), equalTo(-1));
    assertThat(comCol1.compare(row3, row4), equalTo(0));

    assertThat(comCol1Reversed.compare(row1, row2), equalTo(0));
    assertThat(comCol1Reversed.compare(row1, row3), equalTo(1));
    assertThat(comCol1Reversed.compare(row1, row4), equalTo(1));
    assertThat(comCol1Reversed.compare(row2, row3), equalTo(1));
    assertThat(comCol1Reversed.compare(row2, row4), equalTo(1));
    assertThat(comCol1Reversed.compare(row3, row4), equalTo(0));

    assertThat(comCol2.compare(row1, row2), equalTo(-1));
    assertThat(comCol2.compare(row1, row3), equalTo(0));
    assertThat(comCol2.compare(row1, row4), equalTo(-1));
    assertThat(comCol2.compare(row2, row3), equalTo(1));
    assertThat(comCol2.compare(row2, row4), equalTo(0));
    assertThat(comCol2.compare(row3, row4), equalTo(-1));

    assertThat(comCol2Reversed.compare(row1, row2), equalTo(1));
    assertThat(comCol2Reversed.compare(row1, row3), equalTo(0));
    assertThat(comCol2Reversed.compare(row1, row4), equalTo(1));
    assertThat(comCol2Reversed.compare(row2, row3), equalTo(-1));
    assertThat(comCol2Reversed.compare(row2, row4), equalTo(0));
    assertThat(comCol2Reversed.compare(row3, row4), equalTo(1));

    assertThat(comCol1Then2.compare(row1, row2), equalTo(-1));
    assertThat(comCol1Then2.compare(row1, row3), equalTo(-1));
    assertThat(comCol1Then2.compare(row1, row4), equalTo(-1));
    assertThat(comCol1Then2.compare(row2, row3), equalTo(-1));
    assertThat(comCol1Then2.compare(row2, row4), equalTo(-1));
    assertThat(comCol1Then2.compare(row3, row4), equalTo(-1));

    assertThat(comCol2Then1.compare(row1, row2), equalTo(-1));
    assertThat(comCol2Then1.compare(row1, row3), equalTo(-1));
    assertThat(comCol2Then1.compare(row1, row4), equalTo(-1));
    assertThat(comCol2Then1.compare(row2, row3), equalTo(1));
    assertThat(comCol2Then1.compare(row2, row4), equalTo(-1));
    assertThat(comCol2Then1.compare(row3, row4), equalTo(-1));
  }

  @Test
  public void testColumnComparator() {
    String colInteger = "colInteger";
    String colIssue = "colIssue";
    String colString = "colString";

    ColumnMetadata columnMetadataInteger =
        new ColumnMetadata(colInteger, Schema.INTEGER, "colIntegerDesc");
    ColumnMetadata columnMetadataIssue = new ColumnMetadata(colIssue, Schema.ISSUE, "colIssueDesc");
    ColumnMetadata columnMetadataString =
        new ColumnMetadata(colString, Schema.STRING, "colStringDesc");

    Comparator<Row> comInteger = _manager.columnComparator(columnMetadataInteger);
    Comparator<Row> comIssue = _manager.columnComparator(columnMetadataIssue);
    Comparator<Row> comString = _manager.columnComparator(columnMetadataString);

    Row r1 =
        Row.of(
            colInteger,
            1,
            colIssue,
            new Issue("blah", 1, new Issue.Type("major", "minor")),
            colString,
            "a");
    Row r2 =
        Row.of(
            colInteger,
            2,
            colIssue,
            new Issue("blah", 2, new Issue.Type("major", "minor")),
            colString,
            "b");

    assertThat(comInteger.compare(r1, r2), equalTo(-1));
    assertThat(comIssue.compare(r1, r2), equalTo(-1));
    assertThat(comString.compare(r1, r2), equalTo(-1));
  }

  @Test
  public void testColumnComparatorUnsupported() {
    String colObject = "colObject";

    ColumnMetadata columnObject = new ColumnMetadata(colObject, Schema.OBJECT, "colObjectDesc");

    _thrown.expect(UnsupportedOperationException.class);
    _manager.columnComparator(columnObject);
  }
}
