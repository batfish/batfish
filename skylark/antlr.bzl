"""Rules for generating ANTLR parsers from grammar files."""

def _antlr_grammar_impl(ctx):
    """Implementation function for antlr_grammar rule."""
    srcs = ctx.files.srcs
    package = ctx.attr.package

    # Lexer and Parser files to pass to ANTLR (not all .g4 files)
    lexer_parser_files = [f for f in srcs if f.basename.endswith("Lexer.g4") or f.basename.endswith("Parser.g4")]

    # Determine output files based on lexer/parser files
    outs = []
    for src in lexer_parser_files:
        base_name = src.basename.removesuffix(".g4")
        if base_name.endswith("Lexer"):
            outs.append(ctx.actions.declare_file(base_name + ".java"))
            outs.append(ctx.actions.declare_file(base_name + ".tokens"))
        elif base_name.endswith("Parser"):
            outs.append(ctx.actions.declare_file(base_name + ".java"))
            outs.append(ctx.actions.declare_file(base_name + "BaseListener.java"))
            outs.append(ctx.actions.declare_file(base_name + "Listener.java"))

    # Get ANTLR tool
    antlr_jar = ctx.file.antlr_tool

    # Build ANTLR command
    cmd = "java -cp %s org.antlr.v4.Tool -Xexact-output-dir %s -package %s -encoding UTF-8 -Werror -o %s" % (
        antlr_jar.path,
        " ".join([f.path for f in lexer_parser_files]),
        package,
        outs[0].dirname,
    )

    # Run ANTLR (all srcs are inputs for dependencies, but only lexer/parser are in command line)
    ctx.actions.run_shell(
        command = cmd,
        inputs = depset(direct = srcs + [antlr_jar]),
        outputs = outs,
        mnemonic = "AntlrGeneration",
        progress_message = "Generating ANTLR parser for %s" % package,
    )

    return [DefaultInfo(files = depset(outs))]

antlr_grammar = rule(
    implementation = _antlr_grammar_impl,
    attrs = {
        "srcs": attr.label_list(
            allow_files = [".g4"],
            mandatory = True,
            doc = "ANTLR grammar files (.g4) - files ending in Lexer.g4 or Parser.g4 will be passed to ANTLR, others are dependencies",
        ),
        "package": attr.string(
            mandatory = True,
            doc = "Java package name for generated files",
        ),
        "antlr_tool": attr.label(
            allow_single_file = True,
            default = "@antlr4_tool//jar",
            doc = "ANTLR tool JAR",
        ),
    },
    doc = "Generates Java parser files from ANTLR grammar files.",
)
