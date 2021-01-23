package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static io.github.martinschneider.orzo.parser.TestHelper.varInfo;

import io.github.martinschneider.orzo.codegen.generators.IfGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.IfParser;
import io.github.martinschneider.orzo.parser.ParserContext;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.provider.Arguments;

@TestInstance(Lifecycle.PER_CLASS)
public class IfGeneratorTest extends StatementGeneratorTest<IfStatement> {

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args(
            "if (x==0) { x=1; }",
            list(varInfo("x", "int", 2)),
            list("iload_2", "ifne 5", "iconst_1", "istore_2")),
        args(
            "if (x==0) { x=1; } else { x=2; }",
            list(varInfo("x", "int", 2)),
            list("iload_2", "ifne 8", "iconst_1", "istore_2", "goto 5", "iconst_2", "istore_2")),
        args(
            "if (x==0) { x=1; } else if (x==1) { x=2; }",
            list(varInfo("x", "int", 2)),
            list(
                "iload_2",
                "ifne 8",
                "iconst_1",
                "istore_2",
                "goto 10",
                "iload_2",
                "iconst_1",
                "if_icmpne 5",
                "iconst_2",
                "istore_2")),
        args(
            "if (x==0) { x=1; } else if (x==1) { x=2; } else { x=3; }",
            list(varInfo("x", "int", 2)),
            list(
                "iload_2",
                "ifne 8",
                "iconst_1",
                "istore_2",
                "goto 15",
                "iload_2",
                "iconst_1",
                "if_icmpne 8",
                "iconst_2",
                "istore_2",
                "goto 5",
                "iconst_3",
                "istore_2")));
  }

  @BeforeAll
  public void init() {
    super.init();
    target = new IfGenerator(ctx);
    parser = new IfParser(ParserContext.build(new CompilerErrors()));
  }
}