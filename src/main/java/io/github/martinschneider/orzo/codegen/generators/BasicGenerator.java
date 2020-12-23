package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ARRAYLENGTH;
import static io.github.martinschneider.orzo.codegen.OpCodes.WIDE;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.castOps;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.castOps1;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import java.util.Collections;

public class BasicGenerator {
  private CGContext ctx;

  private static final String LOG_NAME = "basic generator";

  public BasicGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public void convert(DynamicByteArray out, String from, String to) {
    addCastingErrors(from, to);
    // TODO: array casts
    if (from != null && to != null) {
      out.write(castOps.getOrDefault(from, Collections.emptyMap()).getOrDefault(to, new byte[0]));
    }
    ctx.opStack.pop();
    ctx.opStack.push(to);
  }

  public void convert1(DynamicByteArray out, String from, String to) {
    addCastingErrors(from, to);
    // TODO: array casts
    if (from != null && to != null) {
      out.write(castOps1.getOrDefault(from, Collections.emptyMap()).getOrDefault(to, new byte[0]));
    }
    ctx.opStack.pop();
    ctx.opStack.push(to);
  }

  private void addCastingErrors(String from, String to) {
    // I have considered allowing these casts by mapping 0 to false and everything else to true.
    // However, I decided to stick with standard Java behaviour and raise an error instead.
    if (from.equals(BOOLEAN)) {
      ctx.errors.addError(LOG_NAME, String.format("cannot cast boolean type to %s", to));
    } else if (to.equals(BOOLEAN)) {
      ctx.errors.addError(LOG_NAME, String.format("cannot cast %s type to boolean", from));
    }
  }

  public void wide(HasOutput out, short idx, byte opCode) {
    if (idx > Byte.MAX_VALUE) {
      out.write(WIDE);
      out.write(opCode);
      out.write(idx);
    } else {
      out.write(opCode);
      out.write((byte) idx);
    }
  }

  public void arrayLength(HasOutput out) {
    out.write(ARRAYLENGTH);
  }
}
