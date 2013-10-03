package com.linkedin.oneclick.utils;


import java.util.HashMap;
import java.util.Map;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.UnifiedJEXL;


/**
 * Allow customization of commands by substituting variables using
 * <a href="http://commons.apache.org/proper/commons-jexl/">JEXL</a>.
 *
 * The substitution is done in 2 phases:
 * <ol>
 *   <li>Before breaking into command-line tokens</li>
 *   <li>After breaking into command-line, just before the command gets executed.
 *   To be substituted at this phase variables needs to be marked with <code>@{variable-name}</code> instead of
 *   <code>${variable-name}</code></li>
 * </ol>
 */
public class Customizer
{
  JexlEngine engine;
  UnifiedJEXL interpreter;
  ExpressionContext context;

  public Customizer()
  {
    engine= new JexlEngine();
    engine.setLenient(false);
    interpreter= new UnifiedJEXL(engine);
    context=new ExpressionContext(interpreter);
  }

  public void set(String name, String value)
  {
    context.set(name, value);
  }

  static class ExpressionContext implements JexlContext
  {
    UnifiedJEXL interpreter;
    Map<String, UnifiedJEXL.Expression> variables= new HashMap<String, UnifiedJEXL.Expression>();

    public ExpressionContext(UnifiedJEXL interpreter)
    {
      this.interpreter= interpreter;
    }

    @Override public Object get(String name)
    {
      UnifiedJEXL.Expression found= variables.get(name);
      if (found==null)
        return null;
      return found.evaluate(this);
    }

    @Override public void set(String name, Object value)
    {
      variables.put(name, interpreter.parse(value.toString()));
    }

    @Override public boolean has(String name)
    {
      return variables.containsKey(name);
    }
  }

  /** expressions.map(evaluate) */
  public String[] evaluate(String[] expressions)
  {
    String[] evaluated= new String[expressions.length];
    for(int i= 0; i < expressions.length; i++)
      evaluated[i]= evaluate(expressions[i]);
    return evaluated;
  }

  public String evaluate(String expression)
  {
    return interpreter.parse(expression).evaluate(context).toString();
  }

  public String[] twoPhaseEvaluate(String expression)
  {
    String phase1= interpreter.parse(expression).evaluate(context).toString();
    String[] phase2= phase1.replace("@{", "${").split("\\s+");
    return evaluate(phase2);
  }

}
