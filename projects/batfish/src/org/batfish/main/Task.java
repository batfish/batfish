package org.batfish.main;

import java.util.Date;

import org.batfish.common.BfConsts.TaskStatus;

public class Task {
   String[] _args;
   Date _obtained;
   Date _terminated;
   TaskStatus _status;

   public Task(String[] args) {
      this._args = args;
      _obtained = new Date();
      _terminated = null;
      _status = TaskStatus.Unscheduled;
   }

   public TaskStatus getStatus() {
      return _status;
   }

   public void setStatus(TaskStatus status) {
      _status = status;
   }
   
   public void setTerminated() {
      _terminated = new Date();
   }
}
