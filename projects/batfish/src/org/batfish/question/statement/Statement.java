package org.batfish.question.statement;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;

public interface Statement {

   void execute(Environment environment, BatfishLogger logger, Settings settings);

}
