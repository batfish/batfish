package org.batfish.question;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;

public interface Statement {

   void execute(Environment environment, BatfishLogger logger, Settings settings);

}
