package org.batfish.main;

import java.util.Collection;
import java.util.Date;

import org.batfish.common.BatfishConstants.WorkStatus;

public class Work {
   String[] _args;
   Date _obtained;
   Date _terminated;
   WorkStatus _status;

   public Work(String[] args) {
      this._args = args;
      _obtained = new Date();
      _terminated = null;
      _status = WorkStatus.Unscheduled;
   }

   public WorkStatus getStatus() {
      return _status;
   }

   public void setStatus(WorkStatus status) {
      _status = status;
   }
   
   public void setTerminated() {
      _terminated = new Date();
   }
}
