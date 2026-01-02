package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BfConsts.TaskStatus;

public final class Task {

  public static class Batch {
    private static final String PROP_COMPLETED = "completed";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_SIZE = "size";
    private static final String PROP_START_DATE = "startDate";

    private AtomicInteger _completed;

    private String _description;

    private int _size;

    private Date _startDate;

    @JsonCreator
    private Batch() {
      _completed = new AtomicInteger();
    }

    @JsonProperty(PROP_COMPLETED)
    public AtomicInteger getCompleted() {
      return _completed;
    }

    @JsonProperty(PROP_DESCRIPTION)
    public String getDescription() {
      return _description;
    }

    @JsonProperty(PROP_SIZE)
    public int getSize() {
      return _size;
    }

    @JsonProperty(PROP_START_DATE)
    public Date getStartDate() {
      return _startDate;
    }

    @JsonProperty(PROP_COMPLETED)
    @Deprecated // only for Jackson
    private void setCompleted(int completed) {
      _completed = new AtomicInteger(completed);
    }

    @JsonProperty(PROP_DESCRIPTION)
    public void setDescription(String description) {
      _description = description;
    }

    @JsonProperty(PROP_SIZE)
    public void setSize(int size) {
      _size = size;
    }

    @JsonProperty(PROP_START_DATE)
    public void setStartDate(Date startDate) {
      _startDate = startDate;
    }

    @Override
    public String toString() {
      if (_size > 0) {
        return String.format("%s: %s: %s/%d", _startDate, _description, _completed, _size);
      } else {
        return String.format("%s: %s", _startDate, _description);
      }
    }
  }

  private static final String PROP_ARGS = "args";
  private static final String PROP_BATCHES = "batches";
  private static final String PROP_ERR_MESSAGE = "errMessage";
  private static final String PROP_OBTAINED = "obtained";
  private static final String PROP_STATUS = "status";
  private static final String PROP_TERMINATED = "terminated";

  private final String[] _args;

  private final List<Batch> _batches;

  private String _errMessage;

  private final Date _obtained;

  private TaskStatus _status;

  private Date _terminated;

  @JsonCreator
  public Task(
      @JsonProperty(PROP_ARGS) String[] args,
      @JsonProperty(PROP_OBTAINED) Date obtained,
      @JsonProperty(PROP_STATUS) TaskStatus status,
      @JsonProperty(PROP_TERMINATED) Date terminated,
      @JsonProperty(PROP_BATCHES) List<Batch> batches,
      @JsonProperty(PROP_ERR_MESSAGE) String errMessage) {
    _args = args;
    _obtained = obtained;
    _status = status;
    _terminated = terminated;
    _batches = batches;
    _errMessage = errMessage;
  }

  public Task(@Nullable String[] args) {
    this(args, new Date(), TaskStatus.Unscheduled, null, new ArrayList<>(), null);
  }

  public Task(TaskStatus status) {
    this(null, new Date(), status, null, new ArrayList<>(), null);
    if (status == TaskStatus.TerminatedNormally || status == TaskStatus.TerminatedAbnormally) {
      _terminated = new Date();
    }
  }

  public Task(TaskStatus status, String description) {
    this(status);
    newBatch(description);
  }

  @JsonProperty(PROP_ARGS)
  public String[] getArgs() {
    return _args;
  }

  public List<Batch> getBatches() {
    return _batches;
  }

  @JsonProperty(PROP_ERR_MESSAGE)
  public String getErrMessage() {
    return _errMessage;
  }

  @JsonProperty(PROP_OBTAINED)
  public Date getObtained() {
    return _obtained;
  }

  @JsonProperty(PROP_STATUS)
  public TaskStatus getStatus() {
    return _status;
  }

  @JsonProperty(PROP_TERMINATED)
  public Date getTerminated() {
    return _terminated;
  }

  public Batch newBatch(String description) {
    Batch batch = new Batch();
    batch.setDescription(description);
    Date date = new Date();
    batch.setStartDate(date);
    _batches.add(batch);
    return batch;
  }

  @JsonProperty(PROP_ERR_MESSAGE)
  public void setErrMessage(String msg) {
    _errMessage = msg;
  }

  public void setStatus(TaskStatus status) {
    _status = status;
  }

  public void setTerminated(Date terminated) {
    _terminated = terminated;
  }

  @Override
  public String toString() {
    return String.format(
        "[Status: %s LastBatch: %s]",
        _status, _batches.isEmpty() ? "None" : _batches.get(_batches.size() - 1));
  }

  public static @Nonnull Task unknown() {
    return UNKNOWN;
  }

  private static final Task UNKNOWN = new Task(TaskStatus.Unknown);
}
