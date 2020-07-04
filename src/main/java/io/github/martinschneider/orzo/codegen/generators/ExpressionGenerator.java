package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.arithmeticOps;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POW;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.ExpressionResult;
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.VariableInfo;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.lexer.tokens.BoolLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Chr;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

public class ExpressionGenerator {
  public CGContext ctx;
  private static final String LOGGER_NAME = "expression code generator";
  private static final List<Operators> INCREMENT_DECREMENT_OPS =
      List.of(POST_INCREMENT, POST_DECREMENT, PRE_INCREMENT, PRE_DECREMENT);

  public ExpressionResult eval(
      DynamicByteArray out, VariableMap variables, String type, Expression expr) {
    return eval(out, variables, type, expr, true);
  }

  public ExpressionResult eval(
      DynamicByteArray out,
      VariableMap variables,
      String type,
      Expression expr,
      boolean pushIfZero) {
    // TODO: support String concatenation
    // TODO: support different types
    // TODO: error handling, e.g. only "+" operator is valid for String concatenation, "%" is not
    // valid for doubles etc.
    if (type == null) {
      type = new NumExprTypeDecider(ctx).getType(variables, expr);
    }
    Object val = null;
    if (expr == null) {
      return null;
    }
    List<Token> tokens = expr.tokens;
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (!type.equals(DOUBLE)
          && ((i + 2 < tokens.size() && tokens.get(i + 2).eq(op(POW)))
              || (i + 1 < tokens.size() && tokens.get(i + 1).eq(op(POW))))) {
        // Calculating powers for integer types uses BigInteger and requires loading the operands in
        // a different order. Therefore, we skip processing them here.
      } else if (token instanceof Identifier) {
        Identifier id = (Identifier) token;
        String varType = variables.get(id).type;
        String arrType = variables.get(id).arrType;
        // look ahead for ++ or -- operators because in that case we do not push the value to the
        // stack
        if (i + 1 == tokens.size()
            || (!tokens.get(i + 1).eq(op(POST_DECREMENT))
                && !tokens.get(i + 1).eq(op(POST_INCREMENT))
                && !tokens.get(i + 1).eq(op(PRE_INCREMENT))
                && !tokens.get(i + 1).eq(op(PRE_DECREMENT)))) {
          VariableInfo varInfo = variables.get(id);
          byte varIdx = varInfo.idx;
          if (id.arrSel != null) {
            // array
            ctx.loadGen.loadValueFromArray(
                out, variables, id.arrSel.exprs, varInfo.arrType, varIdx);
            type = varInfo.arrType;
          } else {
            ctx.loadGen.load(out, varType, varIdx);
          }
        }
        if (!type.equals(varType) && arrType == null) {
          ctx.basicGen.convert(out, varType, type);
        }
      } else if (token instanceof IntLiteral) {
        BigInteger bigInt = (BigInteger) ((IntLiteral) token).val;
        Long intValue = bigInt.longValue();
        // look ahead for <<, >> or >>> operators which require the second argument to be an integer
        if (i + 1 < tokens.size()
            && ((tokens.get(i + 1).eq(op(LSHIFT))
                || (tokens.get(i + 1).eq(op(RSHIFT)))
                || (tokens.get(i + 1).eq(op(RSHIFTU)))))) {
          ctx.pushGen.push(out, INT, intValue.intValue());
        } else if (!type.equals(INT) || intValue != 0 || pushIfZero) {
          ctx.pushGen.push(out, type, intValue.doubleValue());
        }
        val = bigInt;
      } else if (token instanceof BoolLiteral) {
        Boolean bool = (Boolean) ((BoolLiteral) token).val;
        ctx.pushGen.pushBool(out, bool);
        val = bool;
      } else if (token instanceof FPLiteral) {
        BigDecimal bigDec = (BigDecimal) ((FPLiteral) token).val;
        ctx.pushGen.push(out, type, bigDec.doubleValue());
        val = bigDec;
      } else if (token instanceof Str) {
        ctx.loadGen.ldc(out, CONSTANT_STRING, ((Str) token).strValue());
        type = STRING;
      } else if (token instanceof Chr) {
        char chr = (char) ((Chr) token).val;
        ctx.pushGen.push(out, CHAR, chr);
        type = CHAR;
      } else if (token instanceof MethodCall) {
        type = ctx.methodCallGen.generate(out, variables, (MethodCall) token);
      } else if (token instanceof Operator) {
        Operators op = ((Operator) token).opValue();
        if (List.of(POST_INCREMENT, POST_DECREMENT, PRE_INCREMENT, PRE_DECREMENT).contains(op)) {
          byte idx = variables.get(tokens.get(i - 1)).idx;
          ctx.incrGen.inc(out, type, idx, op, false);
        } else if (op.equals(POW)) {
          if (type.equals(DOUBLE)) {
            ctx.constPool.addClass("java/lang/Math");
            ctx.invokeGen.invokeStatic(
                out, new Method("java.lang.Math", "pow", DOUBLE, List.of(DOUBLE, DOUBLE)));
          } else {
            eval(out, variables, LONG, new Expression(List.of(tokens.get(i - 1))));
            ctx.constPool.addClass("java/math/BigInteger");
            ctx.constPool.addMethodRef(
                "java/math/BigInteger", "valueOf", "(J)Ljava/math/BigInteger;");
            ctx.constPool.addMethodRef("java/math/BigInteger", "pow", "(I)Ljava/math/BigInteger;");
            ctx.constPool.addMethodRef("java/math/BigInteger", "longValue", "()J");
            ctx.invokeGen.invokeStatic(
                out,
                new Method(
                    "java.math.BigInteger", "valueOf", "Ljava/math/BigInteger;", List.of(LONG)));
            eval(out, variables, INT, new Expression(List.of(tokens.get(i - 2))));
            ctx.invokeGen.invokeVirtual(
                out,
                new Method("java/math/BigInteger", "pow", "Ljava/math/BigInteger;", List.of(INT)));
            ctx.invokeGen.invokeVirtual(
                out,
                new Method("java/math/BigInteger", "longValue", LONG, Collections.emptyList()));
            type = LONG;
          }
        } else {
          Byte opCode = arithmeticOps.getOrDefault(op, Collections.emptyMap()).get(type);
          if (opCode != null) {
            ctx.opStack.pop2();
            ctx.opStack.push(type);
            out.write(opCode);
          }
        }
      }
    }
    if (expr.cast != null) {
      ctx.basicGen.convert(out, type, expr.cast.name);
      type = expr.cast.name;
    }
    return new ExpressionResult(type, val);
  }
}
