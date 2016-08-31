package org.batfish.plugin;

import org.batfish.datamodel.answers.Answer;
import org.batfish.main.Batfish;

public abstract class TaskPlugin {

   protected final Batfish _batfish;

   public TaskPlugin(Batfish batfish) {
      _batfish = batfish;
   }

   public abstract Answer run();

}
