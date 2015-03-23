package org.batfish.main;

import java.util.Date;

import org.batfish.common.BatfishConstants.TaskkStatus;

public class Task {
   String[] _args;
   Date _obtained;
   TaskkStatus _status;
   Date _terminated;

   public Task(String[] args) {
      this._args = args;
      _obtained = new Date();
      _terminated = null;
      _status = TaskkStatus.Unscheduled;
   }

   public TaskkStatus getStatus() {
      return _status;
   }

   public void setStatus(TaskkStatus status) {
      _status = status;
   }

   public void setTerminated() {
      _terminated = new Date();
   }
}
