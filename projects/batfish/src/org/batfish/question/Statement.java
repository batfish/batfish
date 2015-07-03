package org.batfish.question;

import org.batfish.main.BatfishLogger;
import org.batfish.main.Settings;

public interface Statement {

   void execute(Environment environment, BatfishLogger logger, Settings settings);

}
