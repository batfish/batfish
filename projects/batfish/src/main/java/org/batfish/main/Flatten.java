package org.batfish.main;

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.batfish.common.BatfishLogger;
import org.batfish.config.Settings;

public final class Flatten {

  public static void main(String[] args) {
    checkArgument(args.length == 2, "Expected arguments: <input_dir> <output_dir>");
    Path inputPath = Paths.get(args[0]);
    Path outputPath = Paths.get(args[1]);
    Settings settings = new Settings(new String[] {"-storagebase", "/"});
    settings.setLogger(new BatfishLogger(BatfishLogger.LEVELSTR_WARN, false, System.out));

    new Batfish(settings, null, null, null, null, null, null, null, null)
        .flatten(inputPath, outputPath);
  }
}
