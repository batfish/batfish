package org.batfish.coordinator;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Comparators.lexicographical;
import static com.google.common.io.MoreFiles.createParentDirectories;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.AnswerRowsOptions;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.ColumnSortOption;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.Container;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;
import org.batfish.common.plugin.AbstractCoordinator;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.NextHopComparator;
import org.batfish.common.util.UnzipUtility;
import org.batfish.common.util.WorkItemBuilder;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.coordinator.WorkQueueMgr.QueueType;
import org.batfish.coordinator.config.Settings;
import org.batfish.coordinator.id.IdManager;
import org.batfish.coordinator.resources.ForkSnapshotBean;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.datamodel.SnapshotMetadataEntry;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.AnswerSummary;
import org.batfish.datamodel.answers.AutoCompleteUtils;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.InputValidationNotes;
import org.batfish.datamodel.answers.InputValidationUtils;
import org.batfish.datamodel.answers.Issue;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.Schema.Type;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.pojo.Topology;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.Variable;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.table.TableView;
import org.batfish.datamodel.table.TableViewRow;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.storage.StorageProvider;
import org.batfish.storage.StoredObjectMetadata;

public class WorkMgr extends AbstractCoordinator {
  private static final Logger LOGGER = LogManager.getLogger(WorkMgr.class);

  private static final Set<String> IGNORED_PATHS =
      ImmutableSet.<String>builder()
          .add(".DS_STORE")
          .add("__MACOSX")
          .add(".git")
          .add(".svn")
          .build();

  private static final Comparator<Node> COMPARATOR_NODE = Comparator.comparing(Node::getName);

  private static final Comparator<Trace> COMPARATOR_TRACE =
      Comparator.comparing(Trace::getDisposition)
          .thenComparing(
              Trace::getHops,
              Comparators.lexicographical(
                  Comparator.comparing(Hop::getNode, Comparator.comparing(Node::getName))
                      .thenComparing(
                          Hop::getSteps,
                          Comparators.lexicographical(
                              Comparator.<Step<?>, String>comparing(
                                      step -> step.getDetail().toString())
                                  .thenComparing(Step::getAction)))));

  private static SortedSet<Path> getEntries(Path directory) {
    SortedSet<Path> entries = new TreeSet<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
      for (Path entry : stream) {
        entries.add(entry);
      }
    } catch (IOException | DirectoryIteratorException e) {
      throw new BatfishException("Error listing directory '" + directory + "'", e);
    }
    return entries;
  }

  private static Path createTempDirectory(String prefix, FileAttribute<?>... attrs) {
    try {
      Path tempDir = Files.createTempDirectory(prefix, attrs);
      tempDir.toFile().deleteOnExit();
      return tempDir;
    } catch (IOException e) {
      throw new BatfishException("Failed to create temporary directory", e);
    }
  }

  static final class AssignWorkTask implements Runnable {
    @Override
    public void run() {
      Main.getWorkMgr().checkTasks();
      Main.getWorkMgr().assignWork();
    }
  }

  private static final Set<String> WELL_KNOWN_NETWORK_FILENAMES =
      ImmutableSet.of(BfConsts.RELPATH_REFERENCE_LIBRARY_PATH, BfConsts.RELPATH_NODE_ROLES_PATH);

  private static final String SNAPSHOT_PACKAGING_INSTRUCTIONS_URL =
      "https://batfish.readthedocs.io/en/latest/notebooks/interacting.html#Packaging-snapshot-data";

  private final IdManager _idManager;
  private final BatfishLogger _logger;
  private final SnapshotMetadataMgr _snapshotMetadataManager;
  private WorkQueueMgr _workQueueMgr;
  private final StorageProvider _storage;
  private final ExecutorService _gcExecutor;

  public WorkMgr(
      Settings settings,
      BatfishLogger logger,
      @Nonnull IdManager idManager,
      @Nonnull StorageProvider storage,
      @Nonnull WorkExecutorCreator workExecutorCreator) {
    _idManager = idManager;
    _storage = storage;
    _snapshotMetadataManager = new SnapshotMetadataMgr(_storage);
    _logger = logger;
    _workQueueMgr = new WorkQueueMgr(logger, _snapshotMetadataManager);
    // Can only run one GC task at a time, and only have one queued. If one is queued and another is
    // submitted, the older one in the queue is discarded.
    _gcExecutor =
        new ThreadPoolExecutor(
            0, 1, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new DiscardOldestPolicy());
    _workExecutor = workExecutorCreator.apply(logger, settings);
  }

  @VisibleForTesting
  public @Nonnull StorageProvider getStorage() {
    return _storage;
  }

  private void assignWork() {

    try {
      QueuedWork work = _workQueueMgr.getWorkForAssignment();

      // get out if no work was found
      if (work == null) {
        // _logger.info("WM:AssignWork: No unassigned work\n");
        return;
      }
      SubmissionResult result = _workExecutor.submit(work);
      switch (result.getType()) {
        case ERROR:
          _logger.errorf("Error submitting work: %s\n", result.getMessage());
          _workQueueMgr.markAssignmentError(work);
          break;
        case SUCCESS:
          _logger.infof("Work submitted with ID: %s\n", work.getId());
          TaskHandle handle = result.getTaskHandle();
          _workQueueMgr.markAssignmentSuccess(work, handle);
          break;
        case BUSY:
          _logger.warnf("Work with ID: %s requeued because worker is busy\n", work.getId());
          _workQueueMgr.markAssignmentFailure(work);
          break;
        default:
          throw new IllegalArgumentException(
              String.format("Invalid SubmissionResult.Type: %s", result.getType()));
      }
    } catch (Exception e) {
      _logger.errorf("Got exception in assignWork: %s\n", Throwables.getStackTraceAsString(e));
    }
  }

  private void checkTasks() {
    try {
      List<QueuedWork> workToCheck = _workQueueMgr.getWorkForChecking();
      for (QueuedWork work : workToCheck) {
        TaskHandle assignedHandle = work.getAssignedHandle();
        if (assignedHandle == null) {
          _logger.errorf("WM:CheckWork no assigned handle for %s\n", work);
          _workQueueMgr.makeWorkUnassigned(work);
          continue;
        }
        Task task = assignedHandle.checkTask();
        try {
          _workQueueMgr.processTaskCheckResult(work, task);
        } catch (Exception e) {
          _logger.errorf("exception: %s\n", Throwables.getStackTraceAsString(e));
        }
      }
    } catch (Exception e) {
      _logger.errorf("Got exception in checkTasks: %s\n", Throwables.getStackTraceAsString(e));
    }
  }

  private @Nullable CompletionMetadata getCompletionMetadata(
      String network, @Nullable String snapshot) throws IOException {
    checkArgument(!isNullOrEmpty(network), "Network name should be supplied");
    if (snapshot == null) {
      return null;
    }

    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return null;
    }
    SnapshotId snapshotId = snapshotIdOpt.get();
    return _storage.loadCompletionMetadata(networkId, snapshotId);
  }

  public @Nullable List<AutocompleteSuggestion> autoComplete(
      String network,
      @Nullable String snapshot,
      Variable.Type completionType,
      String query,
      int maxSuggestions)
      throws IOException {

    return AutoCompleteUtils.autoComplete(
        network,
        snapshot,
        completionType,
        query,
        maxSuggestions,
        getCompletionMetadata(network, snapshot),
        getNetworkNodeRoles(network),
        getReferenceLibrary(network),
        true);
  }

  WorkDetails computeWorkDetails(WorkItem workItem) throws IOException {
    String referenceSnapshotName = WorkItemBuilder.getReferenceSnapshotName(workItem);
    String questionName = WorkItemBuilder.getQuestionName(workItem);

    WorkType workType = WorkType.UNKNOWN;

    if (WorkItemBuilder.isParsingWorkItem(workItem)) {
      workType = WorkType.PARSING;
    }

    if (WorkItemBuilder.isDataplaningWorkItem(workItem)) {
      if (workType != WorkType.UNKNOWN) {
        throw new BatfishException("Cannot do composite work. Separate PARSING and DATAPLANING.");
      }
      workType = WorkType.DATAPLANING;
    }

    if (WorkItemBuilder.isAnsweringWorkItem(workItem)) {
      if (workType != WorkType.UNKNOWN) {
        throw new BatfishException("Cannot do composite work. Separate ANSWER from other work.");
      }
      Question question = Question.parseQuestion(getQuestion(workItem.getNetwork(), questionName));
      workType =
          question.getIndependent()
              ? WorkType.INDEPENDENT_ANSWERING
              : question.getDataPlane()
                  ? WorkType.DATAPLANE_DEPENDENT_ANSWERING
                  : WorkType.PARSING_DEPENDENT_ANSWERING;
    }

    // TODO: grab IDs once, and earlier; validate resolvable names
    NetworkId networkId = _idManager.getNetworkId(workItem.getNetwork()).get();
    WorkDetails.Builder builder =
        WorkDetails.builder()
            .setNetworkId(networkId)
            .setSnapshotId(_idManager.getSnapshotId(workItem.getSnapshot(), networkId).get())
            .setWorkType(workType)
            .setIsDifferential(WorkItemBuilder.isDifferential(workItem));
    if (referenceSnapshotName != null) {
      builder.setReferenceSnapshotId(
          _idManager.getSnapshotId(referenceSnapshotName, networkId).get());
    }
    if (questionName != null) {
      builder.setQuestionId(_idManager.getQuestionId(questionName, networkId).get());
    }
    return builder.build();
  }

  /**
   * Delete the specified network. Returns {@code true} if deletion is successful. Returns {@code
   * false} if network does not exist.
   */
  public boolean delNetwork(@Nonnull String network) {
    boolean result = _idManager.deleteNetwork(network);
    if (result) {
      triggerGarbageCollection();
    }
    return result;
  }

  /**
   * Delete the specified snapshot under the specified network. Returns {@code true} if deletion is
   * successful. Returns {@code false} if either network or snapshot does not exist.
   */
  public boolean delSnapshot(@Nonnull String network, @Nonnull String snapshot) {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return false;
    }
    NetworkId networkId = networkIdOpt.get();
    boolean result = _idManager.deleteSnapshot(snapshot, networkId);
    if (result) {
      triggerGarbageCollection();
    }
    return result;
  }

  /** Queues garbage collection */
  void triggerGarbageCollection() {
    try {
      _gcExecutor.submit(
          () -> {
            try {
              _storage.runGarbageCollection();
            } catch (Exception e) {
              _logger.errorf("ERROR WorkMgr GC: %s", Throwables.getStackTraceAsString(e));
            }
          });
    } catch (RejectedExecutionException e) {
      // can ignore, since handled by rejection policy
    }
  }

  /**
   * Delete the specified question under the specified network. Returns {@code true} if deletion is
   * successful. Returns {@code false} if network or question does not exist.
   */
  public boolean delQuestion(@Nonnull String network, @Nonnull String question) {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return false;
    }
    NetworkId networkId = networkIdOpt.get();
    return _idManager.deleteQuestion(question, networkId);
  }

  /**
   * Get the answer for the specified question.
   *
   * @throws IllegalArgumentException if the network, question, or snapshots cannot be found
   * @throws IOException if there are any other errors
   */
  public @Nullable Answer getAnswer(
      String network, String snapshot, String question, @Nullable String referenceSnapshot)
      throws IOException {
    String ansString = loadAnswer(network, snapshot, question, referenceSnapshot);
    return ansString == null
        ? null
        : BatfishObjectMapper.mapper().readValue(ansString, Answer.class);
  }

  /** Get the answer string for the specified question. */
  public @Nonnull String getAnswerString(
      String network, String snapshot, String question, @Nullable String referenceSnapshot)
      throws JsonProcessingException {
    try {
      String answer = loadAnswer(network, snapshot, question, referenceSnapshot);
      if (answer == null) {
        Answer ans = Answer.failureAnswer("Not answered", null);
        ans.setStatus(AnswerStatus.NOTFOUND);
        return BatfishObjectMapper.writeString(ans);
      }
      return answer;
    } catch (IOException e) {
      String message =
          String.format(
              "Could not get answer: network=%s, snapshot=%s, question=%s, referenceSnapshot=%s:"
                  + " %s",
              network, snapshot, question, referenceSnapshot, Throwables.getStackTraceAsString(e));
      Answer ans = Answer.failureAnswer(message, null);
      ans.setStatus(AnswerStatus.FAILURE);
      return BatfishObjectMapper.writeString(ans);
    }
  }

  /**
   * Get the answer string for the specified question. Returns {@code null} if the question is not
   * answered.
   */
  private @Nullable String loadAnswer(
      String network, String snapshot, String question, @Nullable String referenceSnapshot)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    checkArgument(networkIdOpt.isPresent(), "Missing network: '%s'", network);
    NetworkId networkId = networkIdOpt.get();
    Optional<QuestionId> questionIdOpt = _idManager.getQuestionId(question, networkId);
    checkArgument(
        questionIdOpt.isPresent(), "Missing question '%s' for network '%s'", question, network);
    QuestionId questionId = questionIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
    checkArgument(
        snapshotIdOpt.isPresent(), "Missing snapshot '%s' for network '%s'", snapshot, network);
    SnapshotId snapshotId = snapshotIdOpt.get();
    SnapshotId referenceSnapshotId = null;
    if (referenceSnapshot != null) {
      Optional<SnapshotId> referenceSnapshotIdOpt =
          _idManager.getSnapshotId(referenceSnapshot, networkId);
      checkArgument(
          referenceSnapshotIdOpt.isPresent(),
          "Missing snapshot '%s' for network '%s'",
          referenceSnapshot,
          network);
      referenceSnapshotId = referenceSnapshotIdOpt.get();
    }
    NodeRolesId networkNodeRolesId = getOrDefaultNodeRolesId(networkId);
    AnswerId answerId =
        _idManager.getAnswerId(
            networkId, snapshotId, questionId, networkNodeRolesId, referenceSnapshotId);
    // No metadata means the question has not been answered
    if (!_storage.hasAnswerMetadata(networkId, snapshotId, answerId)) {
      return null;
    }
    return _storage.loadAnswer(networkId, snapshotId, answerId);
  }

  /**
   * Get all completed work for the specified network and snapshot.
   *
   * @param networkName name of the network to get completed work for.
   * @param snapshotName name of the snapshot to get completed work for.
   * @return {@link List} of completed {@link QueuedWork}.
   */
  public List<QueuedWork> getCompletedWork(String networkName, String snapshotName) {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(networkName);
    checkArgument(networkIdOpt.isPresent(), "Missing network '%s'", networkName);
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshotName, networkId);
    checkArgument(
        snapshotIdOpt.isPresent(),
        "Missing snapshot '%s' for network '%s'",
        snapshotName,
        networkName);
    return _workQueueMgr.getCompletedWork(networkId, snapshotIdOpt.get());
  }

  public @Nonnull AnswerMetadata getAnswerMetadata(
      @Nonnull String network,
      @Nonnull String snapshot,
      @Nonnull String question,
      @Nullable String referenceSnapshot) {
    try {
      Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
      checkArgument(networkIdOpt.isPresent(), "Missing network: '%s'", network);
      NetworkId networkId = networkIdOpt.get();
      Optional<QuestionId> questionIdOpt = _idManager.getQuestionId(question, networkId);
      checkArgument(
          questionIdOpt.isPresent(), "Missing question '%s' for network '%s'", question, network);
      QuestionId questionId = questionIdOpt.get();
      Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
      checkArgument(
          snapshotIdOpt.isPresent(), "Missing snapshot '%s' for network '%s'", snapshot, network);
      SnapshotId snapshotId = snapshotIdOpt.get();
      SnapshotId referenceSnapshotId = null;
      if (referenceSnapshot != null) {
        Optional<SnapshotId> referenceSnapshotIdOpt =
            _idManager.getSnapshotId(referenceSnapshot, networkId);
        checkArgument(
            referenceSnapshotIdOpt.isPresent(),
            "Missing snapshot '%s' for network '%s'",
            referenceSnapshot,
            network);
        referenceSnapshotId = referenceSnapshotIdOpt.get();
      }
      NodeRolesId networkNodeRolesId = getOrDefaultNodeRolesId(networkId);
      AnswerId answerId =
          _idManager.getAnswerId(
              networkId, snapshotId, questionId, networkNodeRolesId, referenceSnapshotId);
      if (!_storage.hasAnswerMetadata(networkId, snapshotId, answerId)) {
        return AnswerMetadata.forStatus(AnswerStatus.NOTFOUND);
      }
      return _storage.loadAnswerMetadata(networkId, snapshotId, answerId);
    } catch (IOException e) {
      _logger.errorf(
          "Could not get answer metadata: network=%s, snapshot=%s, question=%s,"
              + " referenceSnapshot=%s: %s",
          network, snapshot, question, referenceSnapshot, Throwables.getStackTraceAsString(e));
      return AnswerMetadata.forStatus(AnswerStatus.FAILURE);
    }
  }

  private @Nonnull NodeRolesId getOrDefaultNodeRolesId(NetworkId networkId) {
    return _idManager
        .getNetworkNodeRolesId(networkId)
        .orElse(NodeRolesId.DEFAULT_NETWORK_NODE_ROLES_ID);
  }

  /** Return a {@link Container container} contains all snapshots directories inside it. */
  public Container getContainer(String networkName) {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(networkName);
    checkArgument(networkIdOpt.isPresent(), "Network '%s' does not exist", networkName);
    SortedSet<String> testrigs =
        ImmutableSortedSet.copyOf(_idManager.listSnapshots(networkIdOpt.get()));
    return Container.of(networkName, testrigs);
  }

  @Override
  public BatfishLogger getLogger() {
    return _logger;
  }

  @Override
  public Set<String> getNetworkNames() {
    return _idManager.listNetworks();
  }

  /**
   * Returns the latest snapshot in the network.
   *
   * @return An {@link Optional} object with the latest snapshot or empty if no snapshots exist
   */
  public @Nonnull Optional<String> getLatestSnapshot(String network) {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    checkArgument(networkIdOpt.isPresent(), "Missing network '%s'", network);
    NetworkId networkId = networkIdOpt.get();
    Function<String, Instant> toSnapshotTimestamp =
        snapshot -> {
          Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
          checkArgument(
              snapshotIdOpt.isPresent(),
              "Missing snapshot '%s' for network '%s'",
              snapshot,
              network);
          return _snapshotMetadataManager.getSnapshotCreationTimeOrMin(
              networkId, snapshotIdOpt.get());
        };
    return listSnapshots(network).stream()
        .max(
            Comparator.comparing(
                toSnapshotTimestamp, Comparator.nullsFirst(Comparator.naturalOrder())));
  }

  /**
   * Gets the set of nodes in this snapshot. Extracts the set based on the topology file that is
   * generated as part of the testrig initialization. Returns {@code null} if the network or the
   * snapshot does not exist.
   *
   * @throws IOException If the contents of the topology file cannot be mapped to the topology
   *     object
   */
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  public @Nullable Set<String> getNodes(String network, String snapshot) throws IOException {
    Topology topology = getPojoTopology(network, snapshot);
    if (topology == null) {
      return null;
    }
    return topology.getNodes().stream().map(Node::getName).collect(Collectors.toSet());
  }

  /**
   * Returns the pojo topology for the given network and snapshot, or {@code null} if either does
   * not exist.
   */
  public @Nullable Topology getPojoTopology(@Nonnull String network, @Nonnull String snapshot)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return null;
    }
    SnapshotId snapshotId = snapshotIdOpt.get();
    return BatfishObjectMapper.mapper()
        .readValue(_storage.loadPojoTopology(networkId, snapshotId), Topology.class);
  }

  /**
   * Gets the {@link ReferenceLibrary} for the {@code network}. Returns an empty {@link
   * ReferenceLibrary} if one does not exist for that network. Returns {@code null} if the network
   * does not exist.
   *
   * @throws IOException if there is an error loading the {@link ReferenceLibrary}
   */
  public ReferenceLibrary getReferenceLibrary(String network) throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    return _storage.loadReferenceLibrary(networkId).orElse(new ReferenceLibrary(null));
  }

  /** Checks if the specified snapshot exists. */
  public boolean checkSnapshotExists(String network, String snapshot) {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return false;
    }
    NetworkId networkId = networkIdOpt.get();
    return _idManager.hasSnapshotId(snapshot, networkId);
  }

  /** Checks if the specified question exists. */
  public boolean checkQuestionExists(String network, String question) {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return false;
    }
    NetworkId networkId = networkIdOpt.get();
    return _idManager.hasQuestionId(question, networkId);
  }

  /**
   * Get content of given ad-hoc question under network. Returns {@code null} if network or question
   * does not exist.
   *
   * @throws IOException if there is an error reading the question
   */
  public @Nullable String getQuestion(String network, String question) throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<QuestionId> questionIdOpt = _idManager.getQuestionId(question, networkId);
    if (!questionIdOpt.isPresent()) {
      return null;
    }
    return _storage.loadQuestion(networkId, questionIdOpt.get());
  }

  public QueuedWork getMatchingWork(WorkItem workItem, QueueType qType) {
    return _workQueueMgr.getMatchingWork(workItem, qType);
  }

  public QueuedWork getWork(UUID workItemId) {
    return _workQueueMgr.getWork(workItemId);
  }

  /**
   * Load and return the log file for a given work item ID in a given snapshot.
   *
   * @return Content of the log file as a string; {@code null} if the network, snapshot or log file
   *     is not available
   * @throws IOException if the log could not be read successfully.
   */
  public @Nullable String getWorkLog(String networkName, String snapshotName, String workId)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(networkName);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshotName, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return null;
    }
    try {
      return _storage.loadWorkLog(networkId, snapshotIdOpt.get(), workId);
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  /**
   * Load and return the answer JSON file for a given work item ID in a given snapshot.
   *
   * @return Content of the JSON file as a string; {@code null} if the network, snapshot or log file
   *     is not available
   * @throws IOException if the JSON could not be read successfully.
   */
  public @Nullable String getWorkJson(String networkName, String snapshotName, String workId)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(networkName);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshotName, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return null;
    }
    try {
      return _storage.loadWorkJson(networkId, snapshotIdOpt.get(), workId);
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  public String initNetwork(@Nullable String network, @Nullable String networkPrefix) {
    String newNetworkName =
        isNullOrEmpty(network) ? networkPrefix + "_" + UUID.randomUUID() : network;
    if (checkNetworkExists(newNetworkName)) {
      throw new BatfishException(String.format("Network '%s' already exists!", newNetworkName));
    }
    NetworkId networkId = _idManager.generateNetworkId();
    _storage.initNetwork(networkId);
    _idManager.assignNetwork(newNetworkName, networkId);
    _logger.infof("Initialized network: %s\n", newNetworkName);
    return newNetworkName;
  }

  @Override
  public void initSnapshot(
      String networkName, String snapshotName, Path srcDir, Instant creationTime) {
    initSnapshot(networkName, snapshotName, srcDir, creationTime, null);
  }

  /** Move runtime_data.json under batfish/ if it's at the top level. */
  private void moveRuntimeDataFile(Path dir) {
    File runtimeDataNewLoc =
        dir.resolve(BfConsts.RELPATH_BATFISH).resolve(BfConsts.RELPATH_RUNTIME_DATA_FILE).toFile();
    File runtimeDataOldLoc = dir.resolve(BfConsts.RELPATH_RUNTIME_DATA_FILE).toFile();
    if (runtimeDataNewLoc.exists()) {
      // The runtime data file already exists under batfish subdirectory. Delete the one at the root
      // directory (if it exists).
      FileUtils.deleteQuietly(runtimeDataOldLoc);
      return;
    }

    // Move runtime data file at top-level to batfish/ subfolder
    if (runtimeDataOldLoc.exists()) {
      File batfishDir = dir.resolve(BfConsts.RELPATH_BATFISH).toFile();
      try {
        FileUtils.forceMkdir(batfishDir);
        FileUtils.copyFileToDirectory(runtimeDataOldLoc, batfishDir);
        FileUtils.deleteQuietly(runtimeDataOldLoc);
      } catch (IOException e) {
        _logger.warn("Failed to move runtime data file into batfish/ folder");
      }
    }
  }

  public void initSnapshot(
      String networkName,
      String snapshotName,
      Path srcDir,
      Instant creationTime,
      @Nullable SnapshotId parentSnapshotId) {
    Path subDir = getSnapshotSubdir(srcDir);
    validateSnapshotDir(subDir);

    moveRuntimeDataFile(subDir);

    // If interface blacklist was provided, delete it and copy contents into runtime data
    List<NodeInterfacePair> ifaceBlacklist =
        deserializeAndDeleteInterfaceBlacklist(
            subDir.resolve(BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE));
    updateRuntimeData(
        subDir.resolve(BfConsts.RELPATH_BATFISH).resolve(BfConsts.RELPATH_RUNTIME_DATA_FILE),
        ifaceBlacklist,
        ImmutableList.of());

    SortedSet<Path> subFileList = getEntries(subDir);

    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(networkName);
    checkArgument(networkIdOpt.isPresent(), "Missing network '%s'", networkName);
    NetworkId networkId = networkIdOpt.get();
    SnapshotId snapshotId = _idManager.generateSnapshotId();

    // Now that the directory exists, we must also create the metadata.
    try {
      _snapshotMetadataManager.writeMetadata(
          new SnapshotMetadata(creationTime, parentSnapshotId), networkId, snapshotId);
    } catch (Exception e) {
      throw new BatfishException("Could not write testrigMetadata", e);
    }

    // things look ok, now make the move
    boolean bgpTables = false;
    boolean roleData = false;
    boolean referenceLibraryData = false;
    for (Path subFile : subFileList) {
      String name = subFile.getFileName().toString();
      if (name.equals(BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES)) {
        bgpTables = true;
      } else if (isWellKnownNetworkFile(subFile)) {
        if (name.equals(BfConsts.RELPATH_REFERENCE_LIBRARY_PATH)) {
          referenceLibraryData = true;
          try {
            ReferenceLibrary testrigData =
                BatfishObjectMapper.mapper()
                    .readValue(CommonUtil.readFile(subFile), ReferenceLibrary.class);
            ReferenceLibrary mergedLibrary =
                getReferenceLibrary(networkName)
                    .mergeReferenceBooks(testrigData.getReferenceBooks());
            _storage.storeReferenceLibrary(mergedLibrary, networkId);
          } catch (IOException e) {
            // lets not stop the upload because that file is busted.
            // TODO: figure out a way to surface this error to the user
            _logger.errorf("Could not process reference library data: %s", e);
          }
        }
      }
      // Copy everything over
      try {
        if (Files.isDirectory(subFile)) {
          Files.walk(subFile)
              .filter(Files::isRegularFile)
              .forEach(
                  deepFile -> {
                    try (InputStream srcFileStream = Files.newInputStream(deepFile)) {
                      _storage.storeSnapshotInputObject(
                          srcFileStream,
                          subDir.relativize(deepFile).toString(),
                          new NetworkSnapshot(networkId, snapshotId));
                    } catch (IOException e) {
                      throw new UncheckedIOException(
                          String.format("Failed to copy: '%s'", subFile), e);
                    }
                  });
        } else {
          try (InputStream srcFileStream = Files.newInputStream(subFile)) {
            _storage.storeSnapshotInputObject(
                srcFileStream,
                subFile.getFileName().toString(),
                new NetworkSnapshot(networkId, snapshotId));
          }
        }
      } catch (IOException e) {
        throw new UncheckedIOException(String.format("Failed to copy: '%s'", subFile), e);
      }
    }
    _logger.infof(
        "Environment data for snapshot:%s; bgpTables:%s, nodeRoles:%s referenceBooks:%s\n",
        snapshotName, bgpTables, roleData, referenceLibraryData);
    _idManager.assignSnapshot(snapshotName, networkId, snapshotId);
  }

  /**
   * Helper function to assert that the specified dir contains configs
   *
   * @throws BatfishException when specified dir does not contain network configs dir, AWS configs
   *     dir, or a hosts dir
   */
  private static void validateSnapshotDir(Path subDir) {
    // Confirm that at least one of the config subfolders is present
    List<Path> configPaths =
        ImmutableList.of(
            subDir.resolve(BfConsts.RELPATH_HOST_CONFIGS_DIR),
            subDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR),
            subDir.resolve(BfConsts.RELPATH_AWS_CONFIGS_DIR),
            subDir.resolve(BfConsts.RELPATH_SONIC_CONFIGS_DIR),
            subDir.resolve(BfConsts.RELPATH_AZURE_CONFIGS_DIR));
    if (configPaths.stream().noneMatch(Files::exists)) {
      Path srcDir = subDir.getParent();
      throw new BatfishException(
          String.format(
              "Unexpected packaging of snapshot. At least one of these directories must exist: %s. "
                  + "See %s for instructions on how to package your snapshot for analysis.",
              configPaths.stream().map(srcDir::relativize).collect(ImmutableList.toImmutableList()),
              SNAPSHOT_PACKAGING_INSTRUCTIONS_URL));
    }
  }

  /**
   * Helper function to assert there is only one subdir in the specified snapshot dir and return
   * that subdir
   */
  @VisibleForTesting
  static Path getSnapshotSubdir(Path srcDir) {
    SortedSet<Path> srcDirEntries =
        getEntries(srcDir).stream()
            .filter(path -> !IGNORED_PATHS.contains(path.getFileName().toString()))
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
    /*
     * Sanity check what we got:
     *    There should be just one top-level folder.
     */
    if (srcDirEntries.size() != 1 || !Files.isDirectory(srcDirEntries.iterator().next())) {
      throw new BatfishException(
          String.format(
              "Unexpected packaging of snapshot. There should be just one top-level folder.  See"
                  + " %s for more details on how to package your snapshot for analysis.",
              SNAPSHOT_PACKAGING_INSTRUCTIONS_URL));
    }
    return srcDirEntries.iterator().next();
  }

  private static final int STREAMED_FILE_BUFFER_SIZE = 1024;

  private void writeStreamToFile(InputStream inputStream, Path outputFile) throws IOException {
    createParentDirectories(outputFile);
    try (OutputStream fileOutputStream = Files.newOutputStream(outputFile)) {
      int read = 0;
      byte[] bytes = new byte[STREAMED_FILE_BUFFER_SIZE];
      while ((read = inputStream.read(bytes)) != -1) {
        fileOutputStream.write(bytes, 0, read);
      }
    }
  }

  /**
   * Copy a snapshot and make modifications to the copy.
   *
   * @param networkName Name of the network containing the original snapshot
   * @param forkSnapshotBean {@link ForkSnapshotBean} containing parameters used to create the fork
   * @throws IllegalArgumentException If the new snapshot name conflicts with an existing snapshot;
   *     or if item to restore had not been deactivated; or if network does not exist
   * @throws FileNotFoundException if base snapshot does not exist
   * @throws IOException If there is an error reading or writing snapshot files.
   */
  public void forkSnapshot(String networkName, ForkSnapshotBean forkSnapshotBean)
      throws IllegalArgumentException, IOException {
    String baseSnapshotName = forkSnapshotBean.baseSnapshot;
    String snapshotName = forkSnapshotBean.newSnapshot;
    LOGGER.info(
        "Beginning fork snapshot from {}/{} to {}", networkName, baseSnapshotName, snapshotName);

    // Fail early if the new snapshot already exists or the base snapshot does not
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(networkName);
    checkArgument(networkIdOpt.isPresent(), "Network '%s' does not exist", networkName);
    NetworkId networkId = networkIdOpt.get();
    checkArgument(
        !_idManager.hasSnapshotId(snapshotName, networkId),
        "Snapshot with name: '%s' already exists",
        snapshotName);
    Optional<SnapshotId> baseSnapshotIdOpt = _idManager.getSnapshotId(baseSnapshotName, networkId);
    if (!baseSnapshotIdOpt.isPresent()) {
      throw new FileNotFoundException(
          String.format("Base snapshot with name: '%s' does not exist", baseSnapshotName));
    }
    SnapshotId baseSnapshotId = baseSnapshotIdOpt.get();

    // Save user input for troubleshooting
    Instant creationTime = Instant.now();
    String forkSnapshotKey = generateFileDateString(snapshotName);
    _storage.storeForkSnapshotRequest(
        BatfishObjectMapper.writeString(forkSnapshotBean), forkSnapshotKey, networkId);

    // Copy baseSnapshot so initSnapshot will see a properly formatted upload
    Path newSnapshotInputsDir =
        createTempDirectory("files_to_add").resolve(Paths.get(BfConsts.RELPATH_INPUT));
    if (!newSnapshotInputsDir.toFile().mkdirs()) {
      throw new BatfishException("Failed to create directory: '" + newSnapshotInputsDir + "'");
    }

    try (Stream<String> baseInputObjectKeys =
        _storage.listSnapshotInputObjectKeys(new NetworkSnapshot(networkId, baseSnapshotId))) {
      List<String> allKeys = baseInputObjectKeys.collect(Collectors.toList());
      allKeys.parallelStream()
          .forEach(
              key -> {
                try (InputStream baseObjectStream =
                    _storage.loadSnapshotInputObject(networkId, baseSnapshotId, key)) {
                  writeStreamToFile(baseObjectStream, newSnapshotInputsDir.resolve(key));
                } catch (IOException e) {
                  throw new UncheckedIOException(
                      String.format("Unable to copy base snapshot input object with key: %s", key),
                      e);
                }
              });
    }
    // Write user-specified files to the forked snapshot input dir, overwriting existing ones
    if (forkSnapshotBean.zipFile != null) {
      Path unzipDir = createTempDirectory("upload");
      UnzipUtility.unzip(new ByteArrayInputStream(forkSnapshotBean.zipFile), unzipDir);

      // Preserve proper snapshot dir formatting (single top-level dir), so copy new files directly
      // into existing top-level dir
      FileUtils.copyDirectory(getSnapshotSubdir(unzipDir).toFile(), newSnapshotInputsDir.toFile());

      // do not need this directory anymore
      FileUtils.deleteDirectory(unzipDir.toFile());
    }

    moveRuntimeDataFile(newSnapshotInputsDir);

    // Update line-up/line-down interface statuses
    Set<NodeInterfacePair> deactivate = new HashSet<>();
    if (forkSnapshotBean.deactivateInterfaces != null) {
      deactivate.addAll(forkSnapshotBean.deactivateInterfaces);
    }
    // Deactivate any interfaces in interface blacklist and delete blacklist if present
    deactivate.addAll(
        deserializeAndDeleteInterfaceBlacklist(
            newSnapshotInputsDir.resolve(BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE)));
    List<NodeInterfacePair> restore =
        firstNonNull(forkSnapshotBean.restoreInterfaces, ImmutableList.of());
    updateRuntimeData(
        newSnapshotInputsDir
            .resolve(BfConsts.RELPATH_BATFISH)
            .resolve(BfConsts.RELPATH_RUNTIME_DATA_FILE),
        deactivate,
        restore);

    // Add user-specified failures to new blacklists
    addToSerializedList(
        newSnapshotInputsDir.resolve(BfConsts.RELPATH_EDGE_BLACKLIST_FILE),
        forkSnapshotBean.deactivateLinks,
        new TypeReference<List<Edge>>() {});
    addToSerializedList(
        newSnapshotInputsDir.resolve(BfConsts.RELPATH_NODE_BLACKLIST_FILE),
        forkSnapshotBean.deactivateNodes,
        new TypeReference<List<String>>() {});

    // Remove user-specified items from blacklists
    removeFromSerializedList(
        newSnapshotInputsDir.resolve(BfConsts.RELPATH_EDGE_BLACKLIST_FILE),
        forkSnapshotBean.restoreLinks,
        new TypeReference<List<Edge>>() {});
    removeFromSerializedList(
        newSnapshotInputsDir.resolve(BfConsts.RELPATH_NODE_BLACKLIST_FILE),
        forkSnapshotBean.restoreNodes,
        new TypeReference<List<String>>() {});

    // Use initSnapshot to handle creating metadata, etc.
    try {
      initSnapshot(
          networkName,
          snapshotName,
          newSnapshotInputsDir.getParent(),
          creationTime,
          baseSnapshotId);
    } finally {
      FileUtils.deleteDirectory(newSnapshotInputsDir.toFile());
    }
  }

  /**
   * Creates or updates {@link SnapshotRuntimeData} at the given {@code runtimeDataPath} by setting
   * the given {@code deactivateIfaces} to line down and the given {@code activateIfaces} to line
   * up. Interfaces in both collections will be marked line up.
   */
  @VisibleForTesting
  static void updateRuntimeData(
      Path runtimeDataPath,
      @Nonnull Collection<NodeInterfacePair> deactivateIfaces,
      @Nonnull Collection<NodeInterfacePair> restoreIfaces) {
    if (deactivateIfaces.isEmpty() && restoreIfaces.isEmpty()) {
      return;
    }
    SnapshotRuntimeData runtimeData = SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA;
    if (runtimeDataPath.toFile().exists()) {
      try {
        runtimeData =
            firstNonNull(
                BatfishObjectMapper.mapper()
                    .readValue(CommonUtil.readFile(runtimeDataPath), SnapshotRuntimeData.class),
                SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA);
      } catch (IOException e) {
        // fine, existing runtime data is unreadable. Don't care, replace it. TODO warn?
      }
    }
    SnapshotRuntimeData updatedRuntimeData =
        runtimeData.toBuilder()
            .setInterfacesLineDown(deactivateIfaces)
            .setInterfacesLineUp(restoreIfaces)
            .build();
    try {
      runtimeDataPath.getParent().toFile().mkdir();
      CommonUtil.writeFile(runtimeDataPath, BatfishObjectMapper.writeString(updatedRuntimeData));
    } catch (JsonProcessingException e) {
      // TODO Warn here?
    }
  }

  /**
   * If interface blacklist is present at the given path, deserializes it and deletes file. Returns
   * an empty list if file did not exist or could not be deserialized. File is deleted regardless of
   * whether deserialization was successful.
   *
   * <p>TODO Delete method when {@link BfConsts#RELPATH_INTERFACE_BLACKLIST_FILE} is removed.
   */
  @VisibleForTesting
  static @Nonnull List<NodeInterfacePair> deserializeAndDeleteInterfaceBlacklist(
      Path blacklistPath) {
    if (!blacklistPath.toFile().exists()) {
      return ImmutableList.of();
    }
    try {
      return BatfishObjectMapper.mapper()
          .readValue(
              CommonUtil.readFile(blacklistPath), new TypeReference<List<NodeInterfacePair>>() {});
    } catch (IOException e) {
      // Blacklist could not be deserialized as List<NodeInterfacePair>
      return ImmutableList.of();
    } finally {
      CommonUtil.delete(blacklistPath);
    }
  }

  @VisibleForTesting
  /* Helper method to add the specified collection to the serialized list at the specified path. */
  static <T> void addToSerializedList(
      Path serializedObjectPath, @Nullable Collection<T> addition, TypeReference<List<T>> type)
      throws IOException {
    if (addition == null || addition.isEmpty()) {
      return;
    }

    List<T> baseList;
    if (serializedObjectPath.toFile().exists()) {
      baseList =
          BatfishObjectMapper.mapper().readValue(CommonUtil.readFile(serializedObjectPath), type);
      baseList.addAll(addition);
    } else {
      baseList = ImmutableList.copyOf(addition);
    }
    CommonUtil.writeFile(serializedObjectPath, BatfishObjectMapper.writeString(baseList));
  }

  @VisibleForTesting
  /*
   * Helper method to remove the specified collection from the serialized list at the specified
   * path.
   */
  static <T> void removeFromSerializedList(
      Path serializedObjectPath, @Nullable Collection<T> subtraction, TypeReference<List<T>> type)
      throws IOException {
    if (subtraction == null || subtraction.isEmpty()) {
      return;
    }

    List<T> baseList;
    if (serializedObjectPath.toFile().exists()) {
      baseList =
          BatfishObjectMapper.mapper().readValue(CommonUtil.readFile(serializedObjectPath), type);
    } else {
      throw new IllegalArgumentException("Cannot remove element(s) from non-existent blacklist.");
    }

    List<T> missing =
        subtraction.stream()
            .filter(s -> !baseList.contains(s))
            .collect(ImmutableList.toImmutableList());
    checkArgument(
        missing.isEmpty(),
        "Existing blacklist does not contain element(s) specified for removal: '%s'",
        missing);

    baseList.removeAll(subtraction);
    CommonUtil.writeFile(serializedObjectPath, BatfishObjectMapper.writeString(baseList));
  }

  private static boolean isWellKnownNetworkFile(Path path) {
    return WELL_KNOWN_NETWORK_FILENAMES.contains(path.getFileName().toString());
  }

  public SortedSet<String> listNetworks(String apiKey) {
    Path networksDir = Main.getSettings().getContainersLocation();
    if (!Files.exists(networksDir)) {
      networksDir.toFile().mkdirs();
    }
    SortedSet<String> authorizedNetworks =
        _idManager.listNetworks().stream()
            .filter(network -> Main.getAuthorizer().isAccessibleNetwork(apiKey, network, false))
            .collect(toCollection(TreeSet::new));
    return authorizedNetworks;
  }

  public List<Container> getContainers(@Nullable String apiKey) {
    return listNetworks(apiKey).stream().map(this::getContainer).collect(Collectors.toList());
  }

  public List<QueuedWork> listIncompleteWork(
      String network, @Nullable String snapshot, @Nullable WorkType workType) {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    checkArgument(networkIdOpt.isPresent(), "Missing network: '%s'", network);
    NetworkId networkId = networkIdOpt.get();
    SnapshotId snapshotId = null;
    if (snapshot != null) {
      Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
      checkArgument(
          snapshotIdOpt.isPresent(), "Missing snapshot '%s' for network: '%s'", snapshot, network);
      snapshotId = snapshotIdOpt.get();
    }
    return _workQueueMgr.listIncompleteWork(networkId, snapshotId, workType);
  }

  /**
   * List questions for the given network. If {@code verbose} is {@code true}, include hidden
   * questions. Returns list of questions if successful, or {@code null} if network does not exist.
   */
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  public @Nullable SortedSet<String> listQuestions(String network, boolean verbose) {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Set<String> questions = _idManager.listQuestions(networkId);
    if (!verbose) {
      questions =
          questions.stream()
              .filter(name -> !name.startsWith("__"))
              .collect(ImmutableSet.toImmutableSet());
    }
    return ImmutableSortedSet.copyOf(questions);
  }

  /** Returns list of snapshots for given network, or {@code null} if network does not exist. */
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  public @Nullable List<String> listSnapshots(@Nonnull String network) {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    List<String> testrigs =
        _idManager.listSnapshots(networkId).stream()
            // Pair with resolved snapshot IDs so we can filter snapshot names on those that are
            // still present
            .map(
                snapshot ->
                    Maps.immutableEntry(snapshot, _idManager.getSnapshotId(snapshot, networkId)))
            .filter(e -> e.getValue().isPresent())
            .sorted(
                (t1, t2) -> { // reverse sorting by creation-time, name
                  SnapshotId snapshotId1 = t1.getValue().get();
                  SnapshotId snapshotId2 = t2.getValue().get();
                  String key1 =
                      _snapshotMetadataManager.getSnapshotCreationTimeOrMin(networkId, snapshotId1)
                          + t1.getKey();
                  String key2 =
                      _snapshotMetadataManager.getSnapshotCreationTimeOrMin(networkId, snapshotId2)
                          + t2.getKey();
                  return key2.compareTo(key1);
                })
            .map(Entry::getKey)
            .collect(Collectors.toList());
    return testrigs;
  }

  /**
   * Returns list of snapshots for given network along with their metadata, or {@code null} if
   * network does not exist.
   *
   * @throws IOException if there is an error reading metadata for any snapshot
   */
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  public @Nullable List<SnapshotMetadataEntry> listSnapshotsWithMetadata(@Nonnull String network)
      throws IOException {
    if (!_idManager.hasNetworkId(network)) {
      return null;
    }
    ImmutableList.Builder<SnapshotMetadataEntry> snapshotMetadataList = ImmutableList.builder();
    for (String snapshot : listSnapshots(network)) {
      snapshotMetadataList.add(
          new SnapshotMetadataEntry(snapshot, getSnapshotMetadata(network, snapshot)));
    }
    return snapshotMetadataList.build();
  }

  /**
   * Write the reference library for the given network.
   *
   * @throws IOException if there is an error
   */
  public void putReferenceLibrary(ReferenceLibrary referenceLibrary, String network)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    checkArgument(networkIdOpt.isPresent(), "Invalid network: %s", network);
    NetworkId networkId = networkIdOpt.get();
    _storage.storeReferenceLibrary(referenceLibrary, networkId);
  }

  public boolean queueWork(WorkItem workItem) {
    String network = requireNonNull(workItem.getNetwork());
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    checkArgument(networkIdOpt.isPresent(), "Missing network: '%s'", network);
    NetworkId networkId = networkIdOpt.get();
    boolean success;
    try {
      WorkDetails workDetails = computeWorkDetails(workItem);
      _snapshotMetadataManager.getInitializationMetadata(networkId, workDetails.getSnapshotId());
      if (workDetails.isDifferential()) {
        assert workDetails.getReferenceSnapshotId() != null;
        _snapshotMetadataManager.getInitializationMetadata(
            networkId, workDetails.getReferenceSnapshotId());
      }
      success = _workQueueMgr.queueUnassignedWork(new QueuedWork(workItem, workDetails));
    } catch (Exception e) {
      throw new BatfishException(String.format("Failed to queue work: %s", e.getMessage()), e);
    }
    // as an optimization trigger AssignWork to see if we can schedule this (or another) work
    if (success) {
      Thread thread = new Thread(this::assignWork);
      thread.start();
    }
    return success;
  }

  public void startWorkManager() {
    loadPlugins();

    Executors.newScheduledThreadPool(1)
        .scheduleAtFixedRate(
            new AssignWorkTask(),
            0,
            Main.getSettings().getPeriodAssignWorkMs(),
            TimeUnit.MILLISECONDS);
  }

  /**
   * Uploads the given ad-hoc question to the given network. Returns {@code true} if successful.
   * Returns {@code false} if network does not exist.
   *
   * @throws IOException if there as an error saving the question
   */
  public boolean uploadQuestion(String network, String question, String questionJson)
      throws IOException {
    return uploadQuestion(network, question, questionJson, true);
  }

  /**
   * Uploads the given ad-hoc question to the given network. Returns {@code true} if successful.
   * Returns {@code false} if network does not exist.
   *
   * @throws IOException if there as an error saving the question
   */
  @VisibleForTesting
  boolean uploadQuestion(String network, String question, String questionJson, boolean validate)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return false;
    }
    NetworkId networkId = networkIdOpt.get();
    if (validate) {
      // Validate the question before saving it to disk.
      try {
        Question.parseQuestion(questionJson);
      } catch (Exception e) {
        throw new BatfishException(
            String.format("Invalid question %s/%s: %s", network, question, e.getMessage()), e);
      }
    }
    QuestionId questionId = _idManager.generateQuestionId();
    _storage.storeQuestion(questionJson, networkId, questionId);
    _idManager.assignQuestion(question, networkId, questionId);
    return true;
  }

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss.SSS")
          .withLocale(Locale.getDefault())
          .withZone(ZoneOffset.UTC);

  static String generateFileDateString(String base, Instant instant) {
    return base + "_" + FORMATTER.format(instant);
  }

  private static String generateFileDateString(String base) {
    return generateFileDateString(base, Instant.now());
  }

  /**
   * Upload a new snapshot to the specified network.
   *
   * @param networkName Name of the network to upload the snapshot to.
   * @param snapshotName Name of the new snapshot.
   * @param fileStream {@link InputStream} of the snapshot zip.
   * @return {@code true} if successful, or {@code false} if snapshot already exists.
   */
  public boolean uploadSnapshot(String networkName, String snapshotName, InputStream fileStream) {
    LOGGER.info("Beginning snapshot upload to {}/{}", networkName, snapshotName);
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(networkName);
    checkArgument(networkIdOpt.isPresent(), "Missing network: '%s'", networkName);
    NetworkId networkId = networkIdOpt.get();

    // Fail early if the snapshot already exists
    if (_idManager.hasSnapshotId(snapshotName, networkId)) {
      return false;
    }

    // Save uploaded zip for troubleshooting
    Instant creationTime = Instant.now();
    String uploadZipKey = generateFileDateString(snapshotName);
    try {
      _storage.storeUploadSnapshotZip(fileStream, uploadZipKey, networkId);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    Path unzipDir = createTempDirectory("tr");
    try (InputStream zipStream = _storage.loadUploadSnapshotZip(uploadZipKey, networkId)) {
      UnzipUtility.unzip(zipStream, unzipDir);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to extract uploaded zip", e);
    }

    try {
      initSnapshot(networkName, snapshotName, unzipDir, creationTime);
    } catch (Exception e) {
      throw new BatfishException(
          String.format("Error initializing snapshot: %s", e.getMessage()), e);
    } finally {
      CommonUtil.deleteDirectory(unzipDir);
    }
    // Trigger GC since uploading initial snapshot can change expungeBeforeDate
    triggerGarbageCollection();
    return true;
  }

  public boolean checkNetworkExists(String networkName) {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(networkName);
    if (!networkIdOpt.isPresent()) {
      return false;
    }
    return _storage.checkNetworkExists(networkIdOpt.get());
  }

  @VisibleForTesting
  @Nonnull
  Answer processAnswerRows(String rawAnswerStr, AnswerRowsOptions options) {
    if (rawAnswerStr == null) {
      Answer answer = Answer.failureAnswer("Not found", null);
      answer.setStatus(AnswerStatus.NOTFOUND);
      return answer;
    }
    try {
      Answer rawAnswer =
          BatfishObjectMapper.mapper().readValue(rawAnswerStr, new TypeReference<Answer>() {});
      // If the AnswerStatus is not SUCCESS, the answer cannot have any AnswerElements related to
      // actual answers (but, e.g., it might have a BatfishStackTrace). Return that as-is.
      if (rawAnswer.getStatus() != AnswerStatus.SUCCESS) {
        return rawAnswer;
      }
      TableAnswerElement rawTable = (TableAnswerElement) rawAnswer.getAnswerElements().get(0);
      Answer answer = new Answer();
      answer.setStatus(rawAnswer.getStatus());
      answer.addAnswerElement(processAnswerTable(rawTable, options));
      return answer;
    } catch (Exception e) {
      _logger.errorf("Failed to convert answer string to Answer: %s", e.getMessage());
      return Answer.failureAnswer(e.getMessage(), null);
    }
  }

  @VisibleForTesting
  @Nonnull
  Answer processAnswerRows2(String rawAnswerStr, AnswerRowsOptions options) {
    if (rawAnswerStr == null) {
      Answer answer = Answer.failureAnswer("Not found", null);
      answer.setStatus(AnswerStatus.NOTFOUND);
      return answer;
    }
    try {
      Answer rawAnswer =
          BatfishObjectMapper.mapper().readValue(rawAnswerStr, new TypeReference<Answer>() {});
      // If the AnswerStatus is not SUCCESS, the answer cannot have any AnswerElements related to
      // actual answers (but, e.g., it might have a BatfishStackTrace). Return that as-is.
      if (rawAnswer.getStatus() != AnswerStatus.SUCCESS) {
        return rawAnswer;
      }
      return filterAnswer(rawAnswer, options);
    } catch (Exception e) {
      _logger.errorf(
          "Failed to convert answer string to Answer: %s\n", Throwables.getStackTraceAsString(e));
      return Answer.failureAnswer(e.getMessage(), null);
    }
  }

  /** Filter the supplied rawAnswer based on the options provided */
  public Answer filterAnswer(Answer rawAnswer, AnswerRowsOptions options) {
    AnswerElement answerElement = rawAnswer.getAnswerElements().get(0);
    if (!(answerElement instanceof TableAnswerElement)) {
      return rawAnswer;
    }
    TableAnswerElement rawTable = (TableAnswerElement) answerElement;
    Answer answer = new Answer();
    answer.setStatus(rawAnswer.getStatus());
    answer.addAnswerElement(processAnswerTable2(rawTable, options));
    return answer;
  }

  @VisibleForTesting
  @Nonnull
  TableAnswerElement processAnswerTable(TableAnswerElement rawTable, AnswerRowsOptions options) {
    Map<String, ColumnMetadata> rawColumnMap = rawTable.getMetadata().toColumnMap();
    List<Row> filteredRows =
        rawTable.getRowsList().stream()
            .filter(row -> options.getFilters().stream().allMatch(filter -> filter.matches(row)))
            .collect(ImmutableList.toImmutableList());

    Stream<Row> rowStream = filteredRows.stream();
    if (!options.getSortOrder().isEmpty()) {
      // sort using specified sort order
      rowStream = rowStream.sorted(buildComparator(rawColumnMap, options.getSortOrder()));
    }
    TableAnswerElement table;
    if (options.getColumns().isEmpty()) {
      table = new TableAnswerElement(rawTable.getMetadata());
    } else {
      // project to desired columns
      rowStream =
          rowStream.map(rawRow -> Row.builder().putAll(rawRow, options.getColumns()).build());
      Map<String, ColumnMetadata> columnMap = new LinkedHashMap<>(rawColumnMap);
      columnMap.keySet().retainAll(options.getColumns());
      List<ColumnMetadata> columnMetadata =
          columnMap.values().stream().collect(ImmutableList.toImmutableList());
      table =
          new TableAnswerElement(
              new TableMetadata(columnMetadata, rawTable.getMetadata().getTextDesc()));
    }
    if (options.getUniqueRows()) {
      // uniquify if desired
      rowStream = rowStream.distinct();
    }
    // offset, truncate, and add to table
    rowStream.skip(options.getRowOffset()).limit(options.getMaxRows()).forEach(table::addRow);
    table.setSummary(rawTable.getSummary() != null ? rawTable.getSummary() : new AnswerSummary());
    table.getSummary().setNumResults(filteredRows.size());
    return table;
  }

  @VisibleForTesting
  @Nonnull
  TableView processAnswerTable2(TableAnswerElement rawTable, AnswerRowsOptions options) {
    Map<Row, Integer> rowIds = Maps.newIdentityHashMap();
    CommonUtil.forEachWithIndex(rawTable.getRowsList(), (i, row) -> rowIds.put(row, i));
    Map<String, ColumnMetadata> rawColumnMap = rawTable.getMetadata().toColumnMap();

    for (String c : options.getColumns()) {
      if (!rawColumnMap.containsKey(c)) {
        Collection<String> sortedColumnNames = new TreeSet<>(rawColumnMap.keySet());
        throw new IllegalArgumentException(
            String.format("Column %s is not in the answer: %s", c, sortedColumnNames));
      }
    }

    List<Row> filteredRows =
        rawTable.getRowsList().stream()
            .filter(row -> options.getFilters().stream().allMatch(filter -> filter.matches(row)))
            .collect(ImmutableList.toImmutableList());

    Stream<Row> rowStream = filteredRows.stream();
    if (!options.getSortOrder().isEmpty()) {
      // sort using specified sort order
      rowStream = rowStream.sorted(buildComparator(rawColumnMap, options.getSortOrder()));
    }
    TableMetadata tableMetadata;
    if (options.getColumns().isEmpty()) {
      tableMetadata = rawTable.getMetadata();
    } else {
      // project to desired columns
      rowStream =
          rowStream.map(
              rawRow -> {
                Row row = Row.builder().putAll(rawRow, options.getColumns()).build();
                rowIds.put(row, rowIds.get(rawRow));
                return row;
              });
      // TableMetadata requires at least one key. For simplicity, make them all keys.
      Map<String, ColumnMetadata> columnMap =
          options.getColumns().stream()
              .collect(
                  ImmutableMap.toImmutableMap(
                      Function.identity(),
                      col -> {
                        ColumnMetadata colMetadata = rawColumnMap.get(col);
                        return new ColumnMetadata(
                            colMetadata.getName(),
                            colMetadata.getSchema(),
                            colMetadata.getDescription(),
                            true, // isKey
                            false // isValue
                            );
                      }));
      List<ColumnMetadata> columnMetadata =
          columnMap.values().stream().collect(ImmutableList.toImmutableList());
      tableMetadata = new TableMetadata(columnMetadata, rawTable.getMetadata().getTextDesc());
    }
    if (options.getUniqueRows()) {
      // uniquify if desired
      rowStream = rowStream.distinct();
    }
    // offset, truncate, and add to table
    TableView tableView =
        new TableView(
            options,
            rowStream
                .skip(options.getRowOffset())
                .limit(options.getMaxRows())
                .map(row -> new TableViewRow(rowIds.get(row), row))
                .collect(ImmutableList.toImmutableList()),
            tableMetadata,
            rawTable.getWarnings());
    tableView.setSummary(
        rawTable.getSummary() != null ? rawTable.getSummary() : new AnswerSummary());
    tableView.getSummary().setNumResults(filteredRows.size());
    return tableView;
  }

  @VisibleForTesting
  @Nonnull
  Comparator<Row> buildComparator(
      Map<String, ColumnMetadata> rawColumnMap, List<ColumnSortOption> sortOrder) {
    ColumnSortOption firstColumnSortOption = sortOrder.get(0);
    ColumnMetadata firstMetadata = rawColumnMap.get(firstColumnSortOption.getColumn());
    Comparator<Row> comparator = columnComparator(firstMetadata);
    if (firstColumnSortOption.getReversed()) {
      comparator = comparator.reversed();
    }
    for (int i = 1; i < sortOrder.size(); i++) {
      ColumnSortOption columnSortOption = sortOrder.get(i);
      Comparator<Row> nextComparator =
          columnComparator(rawColumnMap.get(columnSortOption.getColumn()));
      if (columnSortOption.getReversed()) {
        nextComparator = nextComparator.reversed();
      }
      comparator = comparator.thenComparing(nextComparator);
    }
    return comparator;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @VisibleForTesting
  @Nonnull
  Comparator<Row> columnComparator(ColumnMetadata columnMetadata) {
    Schema schema = columnMetadata.getSchema();
    Comparator schemaComparator = schemaComparator(schema);
    Comparator comparator =
        comparing((Row r) -> r.get(columnMetadata.getName(), schema), nullsFirst(schemaComparator));
    return comparator;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private @Nonnull Comparator<?> schemaComparator(Schema schema) {
    if (schema.equals(Schema.BOOLEAN)) {
      return naturalOrder();
    } else if (schema.equals(Schema.DOUBLE)) {
      return naturalOrder();
    } else if (schema.equals(Schema.FLOW)) {
      return naturalOrder();
    } else if (schema.equals(Schema.INTEGER)) {
      return naturalOrder();
    } else if (schema.equals(Schema.INTERFACE)) {
      return naturalOrder();
    } else if (schema.equals(Schema.IP)) {
      return naturalOrder();
    } else if (schema.equals(Schema.ISSUE)) {
      return comparing(Issue::getSeverity);
    } else if (schema.getType() == Type.LIST) {
      Comparator schemaComparator = schemaComparator(schema.getInnerSchema());
      return lexicographical(nullsFirst(schemaComparator));
    } else if (schema.equals(Schema.LONG)) {
      return naturalOrder();
    } else if (schema.equals(Schema.NEXT_HOP)) {
      return NextHopComparator.instance();
    } else if (schema.equals(Schema.NODE)) {
      return COMPARATOR_NODE;
    } else if (schema.equals(Schema.PREFIX)) {
      return naturalOrder();
    } else if (schema.getType() == Type.SET) {
      Comparator schemaComparator = schemaComparator(schema.getInnerSchema());
      return lexicographical(nullsFirst(schemaComparator));
    } else if (schema.equals(Schema.STRING)) {
      return naturalOrder();
    } else if (schema.equals(Schema.TRACE)) {
      return COMPARATOR_TRACE;
    } else {
      return comparing(Object::toString);
    }
  }

  @VisibleForTesting
  public IdManager getIdManager() {
    return _idManager;
  }

  @VisibleForTesting
  public SnapshotMetadataMgr getSnapshotMetadataManager() {
    return _snapshotMetadataManager;
  }

  /** Fetch metadata for snapshot. Returns {@code null} if network or snapshot does not exist. */
  public @Nullable SnapshotMetadata getSnapshotMetadata(String network, String snapshot)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return null;
    }
    return _snapshotMetadataManager.readMetadata(networkId, snapshotIdOpt.get());
  }

  /**
   * Promote snapshot's inferred node roles to network-wide node roles if latter does not yet exist
   *
   * @throws IOException if there is an error retrieving snapshot's inferred node roles
   */
  public void tryPromoteSnapshotNodeRoles(
      @Nonnull NetworkId networkId, @Nonnull SnapshotId snapshotId) throws IOException {
    if (!_idManager.hasNetworkNodeRolesId(networkId)) {
      putNetworkNodeRoles(
          getSnapshotNodeRoles(networkId, snapshotId),
          networkId,
          _idManager.getSnapshotNodeRolesId(networkId, snapshotId));
    }
  }

  /**
   * Reads the {@link NodeRolesData} object for the provided {@code network}. If none previously set
   * for this {@code network}, returns empty {@link NodeRolesData}. If {@code network} does not
   * exist, returns {@code null}.
   *
   * @throws IOException If there is an error
   */
  public @Nullable NodeRolesData getNetworkNodeRoles(@Nonnull String network) throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<NodeRolesId> networkNodeRolesIdOpt = _idManager.getNetworkNodeRolesId(networkId);
    if (!networkNodeRolesIdOpt.isPresent()) {
      return NodeRolesData.builder().build();
    }
    NodeRolesId networkNodeRolesId = networkNodeRolesIdOpt.get();
    try {
      return BatfishObjectMapper.mapper()
          .readValue(_storage.loadNodeRoles(networkId, networkNodeRolesId), NodeRolesData.class);
    } catch (IOException e) {
      throw new IOException("Failed to read network node roles", e);
    }
  }

  /**
   * Returns the {@link NodeRolesData} object containing only inferred roles for the provided
   * network and snapshot, or {@code null} if either the network or snapshot does not exist.
   *
   * @throws IOException If there is an error
   */
  public @Nullable NodeRolesData getSnapshotNodeRoles(
      @Nonnull String network, @Nonnull String snapshot) throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return null;
    }
    return getSnapshotNodeRoles(networkId, snapshotIdOpt.get());
  }

  /**
   * Returns the {@link NodeRolesData} object containing only inferred roles for the provided
   * networkId and snapshotId.
   *
   * @throws IOException If there is an error
   */
  private @Nonnull NodeRolesData getSnapshotNodeRoles(
      @Nonnull NetworkId networkId, @Nonnull SnapshotId snapshotId) throws IOException {
    NodeRolesId snapshotNodeRolesId = _idManager.getSnapshotNodeRolesId(networkId, snapshotId);
    return BatfishObjectMapper.mapper()
        .readValue(_storage.loadNodeRoles(networkId, snapshotNodeRolesId), NodeRolesData.class);
  }

  /**
   * Provides a stream from which the extended object with the given {@code key} for the given
   * {@code network} may be read. Returns {@code null} if the object cannot be found.
   *
   * @throws IOException if there is an error reading the object
   */
  @MustBeClosed
  public @Nullable InputStream getNetworkObject(@Nonnull String network, @Nonnull String key)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    checkArgument(networkIdOpt.isPresent(), "Missing network '%s'", network);
    NetworkId networkId = networkIdOpt.get();
    try {
      return _storage.loadNetworkObject(networkId, key);
    } catch (FileNotFoundException e) {
      return null;
    } catch (IOException e) {
      throw new IOException(
          String.format("Could not read extended object for network '%s', key '%s'", network, key),
          e);
    }
  }

  /**
   * Writes an extended object from the provided {@code inputStream} with the given {@code key} for
   * the given {@code network}. Returns {@code true} if the object was written, or {@code false} if
   * the network does not exist.
   *
   * @throws IOException if there is an error writing the object
   */
  public boolean putNetworkObject(
      @Nonnull InputStream inputStream, @Nonnull String network, @Nonnull String key)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return false;
    }
    NetworkId networkId = networkIdOpt.get();
    try {
      _storage.storeNetworkObject(inputStream, networkId, key);
    } catch (IOException e) {
      throw new IOException(
          String.format("Could not write extended object for network '%s', key '%s'", network, key),
          e);
    }
    return true;
  }

  /**
   * Deletes the extended object with the given {@code key} for the given {@code network}. Returns
   * {@code true} if deletion is successful, or {@code false} if the object or network does not
   * exist.
   *
   * @throws IOException if there is an error deleting the object
   */
  public boolean deleteNetworkObject(@Nonnull String network, @Nonnull String key)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return false;
    }
    NetworkId networkId = networkIdOpt.get();
    try {
      _storage.deleteNetworkObject(networkId, key);
    } catch (FileNotFoundException e) {
      return false;
    } catch (IOException e) {
      throw new IOException(
          String.format(
              "Could not delete extended object for network '%s' at key '%s'", network, key),
          e);
    }
    return true;
  }

  /**
   * Provides a stream from which the extended object with the given {@code key} for the given
   * {@code network} and {@code snapshot} may be read. Returns {@code null} if the object cannot be
   * found.
   *
   * @throws IOException if there is an error reading the object
   */
  @MustBeClosed
  public @Nullable InputStream getSnapshotObject(
      @Nonnull String network, @Nonnull String snapshot, @Nonnull String key) throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return null;
    }
    try {
      return _storage.loadSnapshotObject(networkId, snapshotIdOpt.get(), key);
    } catch (FileNotFoundException e) {
      return null;
    } catch (IOException e) {
      throw new IOException(
          String.format(
              "Could not read extended object for network '%s', snapshot '%s', key '%s'",
              network, snapshot, key),
          e);
    }
  }

  /**
   * Writes an extended object from the provided {@code inputStream} with the given {@code key} for
   * the given {@code network} and {@code snapshot}. Returns {@code true} if the object was written,
   * or {@code false} if the network or snapshot does not exist.
   *
   * @throws IOException if there is an error writing the object
   */
  public boolean putSnapshotExtendedObject(
      @Nonnull InputStream inputStream,
      @Nonnull String network,
      @Nonnull String snapshot,
      @Nonnull String key)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return false;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return false;
    }
    try {
      _storage.storeSnapshotObject(inputStream, networkId, snapshotIdOpt.get(), key);
    } catch (IOException e) {
      throw new IOException(
          String.format(
              "Could not write extended object for network '%s', snapshot '%s', key '%s'",
              network, snapshot, key),
          e);
    }
    return true;
  }

  /**
   * Deletes the extended object with the given {@code key} for the given {@code network} and {@code
   * snapshot}. Returns {@code true} if deletion is successful, or {@code false} if the object,
   * network, or snapshot does not exist.
   *
   * @throws IOException if there is an error deleting the object
   */
  public boolean deleteSnapshotObject(
      @Nonnull String network, @Nonnull String snapshot, @Nonnull String key) throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return false;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return false;
    }
    try {
      _storage.deleteSnapshotObject(networkId, snapshotIdOpt.get(), key);
    } catch (FileNotFoundException e) {
      return false;
    } catch (IOException e) {
      throw new IOException(
          String.format(
              "Could not delete extended object for network '%s', snapshot '%s', key '%s'",
              network, snapshot, key),
          e);
    }
    return true;
  }

  /**
   * Provides a stream from which the input object with the given {@code key} for the given {@code
   * network} and {@code snapshot} may be read. Returns {@code null} if the object cannot be found.
   *
   * @throws IOException if there is an error reading the object
   */
  @MustBeClosed
  public InputStream getSnapshotInputObject(String network, String snapshot, String key)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return null;
    }
    try {
      return _storage.loadSnapshotInputObject(networkId, snapshotIdOpt.get(), key);
    } catch (FileNotFoundException e) {
      return null;
    } catch (IOException e) {
      throw new IOException(
          String.format(
              "Could not read input object for network '%s', snapshot '%s', key '%s'",
              network, snapshot, key),
          e);
    }
  }

  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  public @Nullable List<StoredObjectMetadata> getSnapshotInputObjectsMetadata(
      String network, String snapshot) throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return null;
    }
    try {
      return _storage.getSnapshotInputObjectsMetadata(networkId, snapshotIdOpt.get());
    } catch (IOException e) {
      throw new IOException(
          String.format(
              "Could not fetch input keys for network '%s', snapshot '%s'", network, snapshot),
          e);
    }
  }

  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  public @Nullable List<StoredObjectMetadata> getSnapshotExtendedObjectsMetadata(
      String network, String snapshot) throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return null;
    }
    try {
      return _storage.getSnapshotExtendedObjectsMetadata(networkId, snapshotIdOpt.get());
    } catch (IOException e) {
      throw new IOException(
          String.format(
              "Could not fetch extended objects metadata for network '%s', snapshot '%s'",
              network, snapshot),
          e);
    }
  }

  /**
   * Returns the env topology for the given network and snapshot, or {@code null} if either does not
   * exist.
   */
  public @Nullable org.batfish.datamodel.Topology getTopology(String network, String snapshot)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    NetworkId networkId = networkIdOpt.get();
    Optional<SnapshotId> snapshotIdOpt = _idManager.getSnapshotId(snapshot, networkId);
    if (!snapshotIdOpt.isPresent()) {
      return null;
    }
    String topologyStr = _storage.loadInitialTopology(networkId, snapshotIdOpt.get());
    return BatfishObjectMapper.mapper()
        .readValue(topologyStr, org.batfish.datamodel.Topology.class);
  }

  /**
   * Writes the {@code nodeRolesData} for the given {@code network}. Returns {@code true} if
   * successful. Returns {@code false} if {@code network} does not exist.
   *
   * @throws IOException if there is an error
   */
  public boolean putNetworkNodeRoles(@Nonnull NodeRolesData nodeRolesData, @Nonnull String network)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return false;
    }
    NetworkId networkId = networkIdOpt.get();
    NodeRolesId networkNodeRolesId = _idManager.generateNetworkNodeRolesId();
    putNetworkNodeRoles(nodeRolesData, networkId, networkNodeRolesId);
    return true;
  }

  /**
   * Writes the {@code nodeRolesData} for the given {@code networkId} and assigns the given {@code
   * nodeRolesId}.
   *
   * @throws IOException if there is an error
   */
  private void putNetworkNodeRoles(
      @Nonnull NodeRolesData nodeRolesData,
      @Nonnull NetworkId networkId,
      @Nonnull NodeRolesId nodeRolesId)
      throws IOException {
    _storage.storeNodeRoles(networkId, nodeRolesData, nodeRolesId);
    _idManager.assignNetworkNodeRolesId(networkId, nodeRolesId);
  }

  /** Provides the results of validating the user supplied input */
  public @Nullable InputValidationNotes validateInput(
      String network, @Nullable String snapshot, Variable.Type varType, String query)
      throws IOException {
    Optional<NetworkId> networkIdOpt = _idManager.getNetworkId(network);
    if (!networkIdOpt.isPresent()) {
      return null;
    }
    if (snapshot != null && !_idManager.hasSnapshotId(snapshot, networkIdOpt.get())) {
      return null;
    }
    return InputValidationUtils.validate(
        varType,
        query,
        firstNonNull(
            getCompletionMetadata(network, snapshot), CompletionMetadata.builder().build()),
        firstNonNull(getNetworkNodeRoles(network), NodeRolesData.builder().build()),
        firstNonNull(getReferenceLibrary(network), new ReferenceLibrary(null)));
  }

  private final WorkExecutor _workExecutor;
}
