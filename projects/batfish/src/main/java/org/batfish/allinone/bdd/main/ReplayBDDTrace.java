package org.batfish.allinone.bdd.main;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.sf.javabdd.JFactory;
import org.batfish.common.bdd.BDDPacket;

/** Replays a BDD trace. */
public final class ReplayBDDTrace {
  public static void main(String[] args) throws IOException, ClassNotFoundException {
    checkArgument(args.length == 1, "Expected arguments: <trace file>");
    Path inputPath = Paths.get(args[0]);

    // Bazel: resolve relative to current working directory. No-op if paths are already absolute.
    String wd = System.getenv("BUILD_WORKING_DIRECTORY");
    if (wd != null) {
      inputPath = Paths.get(wd).resolve(inputPath);
    }

    JFactory factory = BDDPacket.defaultJFactory(false);
    factory.replayTrace(inputPath);
  }
}
