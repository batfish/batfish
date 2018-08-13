package org.batfish.coordinator;

import static org.batfish.coordinator.WorkMgr.generateFileDateString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.Container;
import org.batfish.common.WorkItem;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.WorkItemBuilder;
import org.batfish.coordinator.AnalysisMetadataMgr.AnalysisType;
import org.batfish.datamodel.TestrigMetadata;
import org.batfish.datamodel.answers.Aggregation;
import org.batfish.datamodel.answers.AnalysisAnswerMetricsResult;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.ColumnAggregation;
import org.batfish.datamodel.answers.ColumnAggregationResult;
import org.batfish.datamodel.answers.Issue;
import org.batfish.datamodel.answers.Metrics;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.pojo.Topology;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
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
    WorkMgrTestUtils.initWorkManager(_folder);
    _manager = Main.getWorkMgr();
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
  public void testGetAnalysisAnswer() throws JsonProcessingException {
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

    String answer1Output =
        _manager.getAnalysisAnswer(
            containerName,
            testrigName,
            BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
            null,
            null,
            analysisName,
            question1Name);
    String answer2Output =
        _manager.getAnalysisAnswer(
            containerName,
            testrigName,
            BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
            null,
            null,
            analysisName,
            question2Name);
    String answer3Output =
        _manager.getAnalysisAnswer(
            containerName,
            testrigName,
            BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
            null,
            null,
            analysisName,
            question3Name);

    Answer failedAnswer = Answer.failureAnswer("Not answered", null);
    failedAnswer.setStatus(AnswerStatus.NOTFOUND);
    String failedAnswerString = BatfishObjectMapper.writePrettyString(failedAnswer);

    assertThat(answer1Output, equalTo(answer1));
    assertThat(answer2Output, equalTo(answer2));
    assertThat(answer3Output, equalTo(failedAnswerString));
  }

  @Test
  public void testGetAnalysisAnswers() throws JsonProcessingException {
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
            analysisName);
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
  public void testGetAnalysisAnswerMetrics() throws IOException {
    String checkName = "check1";
    String columnName = "col";
    int value = 5;

    Answer testAnswer = new Answer();
    testAnswer.addAnswerElement(
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value)));
    testAnswer.setStatus(AnswerStatus.SUCCESS);
    Map<String, String> answers =
        ImmutableMap.of(checkName, BatfishObjectMapper.writePrettyString(testAnswer));
    List<ColumnAggregation> aggregations =
        ImmutableList.of(new ColumnAggregation(Aggregation.MAX, columnName));

    assertThat(
        _manager.getAnalysisAnswersMetrics(answers, aggregations),
        equalTo(
            ImmutableMap.of(
                checkName,
                new AnalysisAnswerMetricsResult(
                    new Metrics(
                        ImmutableList.of(
                            new ColumnAggregationResult(Aggregation.MAX, columnName, value)),
                        1),
                    AnswerStatus.SUCCESS))));
  }

  @Test
  public void testToAnalysisAnswerMetricsResult() throws IOException {
    String columnName = "col";
    int value = 5;

    Answer testAnswer = new Answer();
    testAnswer.addAnswerElement(
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value)));
    testAnswer.setStatus(AnswerStatus.SUCCESS);
    String rawAnswer = BatfishObjectMapper.writePrettyString(testAnswer);
    List<ColumnAggregation> aggregations =
        ImmutableList.of(new ColumnAggregation(Aggregation.MAX, columnName));

    assertThat(
        _manager.toAnalysisAnswerMetricsResult(rawAnswer, aggregations),
        equalTo(
            new AnalysisAnswerMetricsResult(
                new Metrics(
                    ImmutableList.of(
                        new ColumnAggregationResult(Aggregation.MAX, columnName, value)),
                    1),
                AnswerStatus.SUCCESS)));
  }

  @Test
  public void testComputeColumnAggregations() {
    String columnName = "col";
    int value = 5;

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value));
    List<ColumnAggregation> aggregations =
        ImmutableList.of(new ColumnAggregation(Aggregation.MAX, columnName));

    assertThat(
        _manager.computeColumnAggregations(table, aggregations),
        equalTo(ImmutableList.of(new ColumnAggregationResult(Aggregation.MAX, columnName, value))));
  }

  @Test
  public void testComputeColumnAggregationMax() {
    String columnName = "col";
    int value = 5;

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value));
    ColumnAggregation columnAggregation = new ColumnAggregation(Aggregation.MAX, columnName);

    assertThat(
        _manager.computeColumnAggregation(table, columnAggregation),
        equalTo(new ColumnAggregationResult(Aggregation.MAX, columnName, value)));
  }

  @Test
  public void testComputeColumnMaxOneRowInteger() {
    String columnName = "col";
    int value = 5;

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value));

    assertThat(_manager.computeColumnMax(table, columnName), equalTo(value));
  }

  @Test
  public void testComputeColumnMaxOneRowIssue() {
    String columnName = "col";
    int severity = 5;
    Issue value = new Issue("blah", severity, new Issue.Type("1", "2"));

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.ISSUE, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value));

    assertThat(_manager.computeColumnMax(table, columnName), equalTo(severity));
  }

  @Test
  public void testComputeColumnMaxTwoRows() {
    String columnName = "col";
    int value1 = 5;
    int value2 = 10;

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value1))
            .addRow(Row.of(columnName, value2));

    assertThat(_manager.computeColumnMax(table, columnName), equalTo(value2));
  }

  @Test
  public void testComputeColumnMaxNoRows() {
    String columnName = "col";

    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                new DisplayHints().getTextDesc()));

    assertThat(_manager.computeColumnMax(table, columnName), nullValue());
  }

  @Test
  public void testComputeColumnMaxInvalidColumn() {
    String columnName = "col";
    String invalidColumnName = "invalid";
    int value = 5;

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value));

    assertThat(_manager.computeColumnMax(table, invalidColumnName), nullValue());
  }

  @Test
  public void testComputeColumnMaxInvalidSchema() {
    String columnName = "col";
    String value = "hello";

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.STRING, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value));

    assertThat(_manager.computeColumnMax(table, columnName), nullValue());
  }
}
