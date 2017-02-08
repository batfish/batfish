package org.batfish.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Task {

   public static class Batch {

      private static final String COMPLETED_VAR = "completed";

      private static final String DESCRIPTION_VAR = "description";

      private static final String SIZE_VAR = "size";

      private static final String START_DATE_VAR = "startDate";

      private AtomicInteger _completed;

      private String _description;

      private int _size;

      private Date _startDate;

      @JsonCreator
      private Batch() {
         _completed = new AtomicInteger();
      }

      @JsonProperty(COMPLETED_VAR)
      public AtomicInteger getCompleted() {
         return _completed;
      }

      @JsonProperty(DESCRIPTION_VAR)
      public String getDescription() {
         return _description;
      }

      @JsonProperty(SIZE_VAR)
      public int getSize() {
         return _size;
      }

      @JsonProperty(START_DATE_VAR)
      public Date getStartDate() {
         return _startDate;
      }

      @JsonProperty(COMPLETED_VAR)
      private void setCompleted(AtomicInteger completed) {
         _completed = completed;
      }

      @JsonProperty(DESCRIPTION_VAR)
      public void setDescription(String description) {
         _description = description;
      }

      @JsonProperty(SIZE_VAR)
      public void setSize(int size) {
         _size = size;
      }

      @JsonProperty(START_DATE_VAR)
      public void setStartDate(Date startDate) {
         _startDate = startDate;
      }

      @Override
      public String toString() {
         return String.format("%s: %s: %s/%d", _startDate.toString(),
               _description, _completed.toString(), _size);
      }

   }

   private static final String ARGS_VAR = "args";

   private static final String OBTAINED_VAR = "obtained";

   private static final String STATUS_VAR = "status";

   private static final String TERMINATED_VAR = "terminated";

   private String[] _args;

   private List<Batch> _batches;

   private Date _obtained;

   private TaskStatus _status;

   private Date _terminated;

   private volatile boolean _terminationRequested;

   @JsonCreator
   public Task() {
   }

   public Task(String[] args) {
      this._args = args;
      _batches = new ArrayList<>();
      _obtained = new Date();
      _terminated = null;
      _status = TaskStatus.Unscheduled;
   }

   @JsonProperty(ARGS_VAR)
   public String[] getArgs() {
      return _args;
   }

   public List<Batch> getBatches() {
      return _batches;
   }

   @JsonProperty(OBTAINED_VAR)
   public Date getObtained() {
      return _obtained;
   }

   @JsonProperty(STATUS_VAR)
   public TaskStatus getStatus() {
      return _status;
   }

   @JsonProperty(TERMINATED_VAR)
   public Date getTerminated() {
      return _terminated;
   }

   @JsonIgnore
   public boolean getTerminationRequested() {
      return _terminationRequested;
   }

   public Batch newBatch(String description) {
      Batch batch = new Batch();
      batch.setDescription(description);
      Date date = new Date();
      batch.setStartDate(date);
      _batches.add(batch);
      return batch;
   }

   @JsonIgnore
   public void requestTermination() {
      _terminationRequested = true;
   }

   @JsonProperty(ARGS_VAR)
   public void setArgs(String[] args) {
      _args = args;
   }

   public void setBatches(List<Batch> batches) {
      _batches = batches;
   }

   @JsonProperty(OBTAINED_VAR)
   private void setObtained(Date obtained) {
      _obtained = obtained;
   }

   @JsonProperty(STATUS_VAR)
   public void setStatus(TaskStatus status) {
      _status = status;
   }

   public void setTerminated() {
      _terminated = new Date();
   }

   @JsonProperty(TERMINATED_VAR)
   public void setTerminated(Date terminated) {
      _terminated = terminated;
   }

   public synchronized String updateAndWrite() throws JsonProcessingException {
      _obtained = new Date();
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }
}
