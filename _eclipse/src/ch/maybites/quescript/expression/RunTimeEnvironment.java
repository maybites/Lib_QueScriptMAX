/*
 * Copyright 2015 Martin Fröhlich
 * 
 * http://maybites.ch
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

package ch.maybites.quescript.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ch.maybites.quescript.expression.Expression.ExpressionException;

public class RunTimeEnvironment {
	/**
	 * Definition of PI as a constant, can be used in expressions as variable.
	 */

	/**
	 * All defined variables with name and value.
	 */
	protected Map<String, ExpressionVar> publicVars = new HashMap<String, ExpressionVar>();

	/**
	 * All defined variables with name and value.
	 */
	protected Map<String, ExpressionVar> protectedVars = new HashMap<String, ExpressionVar>();

	/**
	 * All defined variables with name and value.
	 */
	protected Map<String, ExpressionVar> privateVars = new HashMap<String, ExpressionVar>();

	/**
	 * All defined operators with name and implementation.
	 */
	protected Map<String, Operator> operators = new HashMap<String, Operator>();

	/**
	 * All defined functions with name and implementation.
	 */
	protected Map<String, Function> functions = new HashMap<String, Function>();

	/**
	 * All defined variables with name and value.
	 */
	protected Map<String, ExpressionVar> staticVars = new HashMap<String, ExpressionVar>();

	public RunTimeEnvironment() {
		addOperator(new Operator("+", 20, true) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return parameters.get(0).add(parameters.get(1));
			}
		});
		addOperator(new Operator("-", 20, true) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(parameters.get(0).getNumberValue() - parameters.get(1).getNumberValue());
			}
		});
		addOperator(new Operator("*", 30, true) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(parameters.get(0).getNumberValue() * parameters.get(1).getNumberValue());
			}
		});
		addOperator(new Operator("/", 30, true) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(parameters.get(0).getNumberValue() / parameters.get(1).getNumberValue());
			}
		});
		addOperator(new Operator("%", 30, true) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(parameters.get(0).getNumberValue() % parameters.get(1).getNumberValue());
			}
		});
		addOperator(new Operator("^", 40, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.pow(parameters.get(0).getNumberValue(), parameters.get(1).getNumberValue()));
			}
		});
		addOperator(new Operator("&&", 4, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				boolean b1 = !parameters.get(0).equals(ExpressionVar.ZERO);
				boolean b2 = !parameters.get(1).equals(ExpressionVar.ZERO);
				return b1 && b2 ? ExpressionVar.ONE : ExpressionVar.ZERO;
			}
		});

		addOperator(new Operator("||", 2, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				boolean b1 = !parameters.get(0).equals(ExpressionVar.ZERO);
				boolean b2 = !parameters.get(1).equals(ExpressionVar.ZERO);
				return b1 || b2 ? ExpressionVar.ONE : ExpressionVar.ZERO;
			}
		});

		addOperator(new Operator(">", 10, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return parameters.get(0).compareTo(parameters.get(1)) == 1 ? ExpressionVar.ONE : ExpressionVar.ZERO;
			}
		});

		addOperator(new Operator("gt", 10, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) throws ExpressionException {
				return operators.get(">").eval(parameters);
			}
		});

		addOperator(new Operator(">=", 10, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return parameters.get(0).compareTo(parameters.get(1)) >= 0 ? ExpressionVar.ONE : ExpressionVar.ZERO;
			}
		});

		addOperator(new Operator("ge", 10, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) throws ExpressionException {
				return operators.get(">=").eval(parameters);
			}
		});

		addOperator(new Operator("<", 10, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return parameters.get(0).compareTo(parameters.get(1)) == -1 ? ExpressionVar.ONE
						: ExpressionVar.ZERO;
			}
		});
		
		addOperator(new Operator("lt", 10, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) throws ExpressionException {
				return operators.get("<").eval(parameters);
			}
		});

		addOperator(new Operator("<=", 10, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return parameters.get(0).compareTo(parameters.get(1)) <= 0 ? ExpressionVar.ONE : ExpressionVar.ZERO;
			}
		});
		
		addOperator(new Operator("le", 10, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) throws ExpressionException{
				return operators.get("<=").eval(parameters);
			}
		});

		addOperator(new Operator("=", 7, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) throws ExpressionException{
				if(parameters.size() == 2 && parameters.get(0).isUsedAsVariable){
					return parameters.get(0).set(parameters.get(1));
				}
				throw new ExpressionException("= can only assign to a variable");
			}
		});

		addOperator(new Operator("==", 7, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) throws ExpressionException {
				return parameters.get(0).compareTo(parameters.get(1)) == 0 ? ExpressionVar.ONE : ExpressionVar.ZERO;
			}
		});

		addOperator(new Operator("!=", 7, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return parameters.get(0).compareTo(parameters.get(1)) != 0 ? ExpressionVar.ONE : ExpressionVar.ZERO;
			}
		});
		addOperator(new Operator("<>", 7, false) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) throws ExpressionException {
				return operators.get("!=").eval(parameters);
			}
		});

		addFunction(new Function("NOT", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				boolean zero = parameters.get(0).compareTo(ExpressionVar.ZERO) == 0;
				return zero ? ExpressionVar.ONE : ExpressionVar.ZERO;
			}
		});

		addFunction(new Function("IF", 3) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return (parameters.get(0).getNumberValue() == 1) ? parameters.get(1) : parameters.get(2);
			}
		});

		addFunction(new Function("RANDOM", 0) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.random());
			}
		});
		addFunction(new Function("SIN", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.sin(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("COS", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.cos(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("TAN", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.tan(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("ASIN", 1) { // added by av
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.asin(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("ACOS", 1) { // added by av
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.acos(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("ATAN", 1) { // added by av
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.atan(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("SINH", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.sinh(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("COSH", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.cosh(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("TANH", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.tanh(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("RAD", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.toRadians(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("DEG", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.toDegrees(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("MAX", -1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) throws ExpressionException {
				if (parameters.size() == 0) {
					throw new ExpressionException("MAX requires at least one parameter");
				}
				ExpressionVar max = null;
				for (ExpressionVar parameter : parameters) {
					if (max == null || parameter.compareTo(max) > 0) {
						max = parameter;
					}
				}
				return max;
			}
		});
		addFunction(new Function("MIN", -1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) throws ExpressionException {
				if (parameters.size() == 0) {
					throw new ExpressionException("MIN requires at least one parameter");
				}
				ExpressionVar min = null;
				for (ExpressionVar parameter : parameters) {
					if (min == null || parameter.compareTo(min) < 0) {
						min = parameter;
					}
				}
				return min;
			}
		});
		addFunction(new Function("ABS", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.abs(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("LOG", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.log(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("LOG10", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.log10(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("ROUND", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.round(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("FLOOR", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return  new ExpressionVar(Math.floor(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("CEILING", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) {
				return new ExpressionVar(Math.ceil(parameters.get(0).getNumberValue()));
			}
		});
		addFunction(new Function("SQRT", 1) {
			@Override
			public ExpressionVar eval(List<ExpressionVar> parameters) throws ExpressionException {
				if (parameters.get(0).compareTo(ExpressionVar.ZERO) == 0) {
					return new ExpressionVar(0);
				}
				if (Math.signum(parameters.get(0).getNumberValue()) < 0) {
					throw new ExpressionException(
							"Argument to SQRT() function must not be negative");
				}

				return new ExpressionVar(Math.sqrt(parameters.get(0).getNumberValue()));
			}
		});

		setStaticVariable("NULL", ExpressionVar.NULL);
		setStaticVariable("PI", ExpressionVar.PI);
		setStaticVariable("TRUE", ExpressionVar.ONE);
		setStaticVariable("FALSE", ExpressionVar.ZERO);
	}

	/**
	 * Adds an operator to the list of supported operators.
	 * 
	 * @param operator
	 *            The operator to add.
	 * @return The previous operator with that name, or <code>null</code> if
	 *         there was none.
	 */
	protected Operator addOperator(Operator operator) {
		return operators.put(operator.getOper(), operator);
	}

	/**
	 * Adds a function to the list of supported functions
	 * 
	 * @param function
	 *            The function to add.
	 * @return The previous operator with that name, or <code>null</code> if
	 *         there was none.
	 */
	protected Function addFunction(Function function) {
		return functions.put(function.getName(), function);
	}

	/**
	 * Returns the Map Collection of all the protected variables
	 * @return
	 */
	public Map<String, ExpressionVar> getProtectedVars(){
		return protectedVars;
	}
	
	/**
	 * Returns the Map Collection of all the Public variables
	 * @return
	 */
	public Map<String, ExpressionVar> getPublicVars(){
		return publicVars;
	}
	
	/**
	 * Returns the Map Collection of all the Public variables
	 * @return
	 */
	public Map<String, ExpressionVar> getPrivateVars(){
		return privateVars;
	}
	
	/**
	 * Replaces the Map collection of all the protected Variables with
	 * @param vars
	 */
	public void setProtectedVars(Map<String, ExpressionVar> vars){
		protectedVars.clear();
		protectedVars = vars;
	}
	
	/**
	 * Replaces the Map collection of all the public Variables with
	 * @param vars
	 */
	public void setPublicVars(Map<String, ExpressionVar> vars){
		publicVars.clear();
		publicVars = vars;
	}

	/**
	 * Replaces the Map collection of all the protected Variables with
	 * @param vars
	 */
	public void setPrivateVars(Map<String, ExpressionVar> vars){
		privateVars.clear();
		privateVars = vars;
	}
	

	/**
	 * Sets a public variable value. if no variable of this name has been set, rt will use
	 * the passed instance as the variable container.
	 * 
	 * @param variable
	 *            The variable name.
	 * @param value
	 *            The variable value.
	 * @return reference to the public variable
	 */
	public ExpressionVar setPublicVariable(String variable, ExpressionVar value) {
		if(publicVars.containsKey(variable)){
			return publicVars.get(variable).set(value);
		} else {
			publicVars.put(variable, value.setUsedAsVariable());
			return value;
		}
	}

	/**
	 * Sets a public variable value.
	 * 
	 * @param variable
	 *            The variable name.
	 * @param value
	 *            The variable value.
	 * @return reference to the public variable
	 */
	public ExpressionVar setPublicVariable(String variable, double value) {
		if(publicVars.containsKey(variable))
			return publicVars.get(variable).setValue(value);
		else {
			ExpressionVar v =  new ExpressionVar(value).setUsedAsVariable();
			publicVars.put(variable, v);
			return v;
		}
	}

	/**
	 * Sets a public variable value.
	 * 
	 * @param variable
	 *            The variable name.
	 * @param value
	 *            The variable value.
	 * @return reference to the public variable
	 */
	public ExpressionVar setPublicVariable(String variable, String value) {
		if(publicVars.containsKey(variable))
			return publicVars.get(variable).setValue(value);
		else {
			ExpressionVar v =  new ExpressionVar(value).setUsedAsVariable();
			publicVars.put(variable, v);
			return v;
		}
	}

	/**
	 * Sets a protected variable value.
	 * 
	 * @param variable
	 *            The variable name.
	 * @param value
	 *            The variable value.
	 * @return reference to the protected variable
	 */
	public ExpressionVar setProtectedVariable(String variable, ExpressionVar value) {
		if(protectedVars.containsKey(variable)){
			return protectedVars.get(variable).set(value);
		} else {
			protectedVars.put(variable, value.setUsedAsVariable());
			return value;
		}
	}

	/**
	 * Sets a protected variable value.
	 * 
	 * @param variable
	 *            The variable name.
	 * @param value
	 *            The variable value.
	 * @return reference to the protected variable
	 */
	public ExpressionVar setProtectedVariable(String variable, double value) {
		if(protectedVars.containsKey(variable)){
			return protectedVars.get(variable).setValue(value);
		}else{
			ExpressionVar v = new ExpressionVar(value).setUsedAsVariable();
			protectedVars.put(variable,v);
			return v;
		}
	}

	/**
	 * Sets a protected variable value.
	 * 
	 * @param variable
	 *            The variable name.
	 * @param value
	 *            The variable value.
	 * @return reference to the protected variable
	 */
	public ExpressionVar setProtectedVariable(String variable, String value) {
		if(protectedVars.containsKey(variable)){
			return protectedVars.get(variable).setValue(value);
		}else{
			ExpressionVar v = new ExpressionVar(value).setUsedAsVariable();
			protectedVars.put(variable,v);
			return v;
		}
	}


	/**
	 * Sets a private variable value.
	 * 
	 * @param variable
	 *            The variable name.
	 * @param value
	 *            The variable value.
	 * @return reference to the protected variable
	 */
	public ExpressionVar setPrivateVariable(String variable, ExpressionVar value) {
		if(privateVars.containsKey(variable)){
			return privateVars.get(variable).set(value);
		} else {
			privateVars.put(variable, value.setUsedAsVariable());
			return value;
		}
	}

	/**
	 * Sets a private variable value.
	 * 
	 * @param variable
	 *            The variable name.
	 * @param value
	 *            The variable value.
	 * @return reference to the protected variable
	 */
	public ExpressionVar setPrivateVariable(String variable, double value) {
		if(privateVars.containsKey(variable)){
			return privateVars.get(variable).setValue(value);
		}else{
			ExpressionVar v = new ExpressionVar(value).setUsedAsVariable();
			privateVars.put(variable,v);
			return v;
		}
	}

	/**
	 * Sets a private variable value.
	 * 
	 * @param variable
	 *            The variable name.
	 * @param value
	 *            The variable value.
	 * @return reference to the protected variable
	 */
	public ExpressionVar setPrivateVariable(String variable, String value) {
		if(privateVars.containsKey(variable)){
			return privateVars.get(variable).setValue(value);
		}else{
			ExpressionVar v = new ExpressionVar(value).setUsedAsVariable();
			privateVars.put(variable,v);
			return v;
		}
	}

	/**
	 * Sets a static variable value.
	 * 
	 * @param variable
	 *            The variable name.
	 * @param value
	 *            The variable value.
	 */
	private void setStaticVariable(String variable, ExpressionVar value) {
		if(staticVars.containsKey(value))
			staticVars.get(value).set(value);
		else
			staticVars.put(variable, value);
	}

	protected abstract class Operation {
		/**
		 * Name of the Operation
		 */
		protected String oper;
		/**
		 * Implementation for an Operation.
		 * 
		 * @param parameters
		 *            Parameters will be passed by the expression evaluator as a
		 *            {@link List} of {@link ExpressionVar} values.
		 * @return The function must return a new {@link ExpressionVar} value as a
		 *         computing result.
		 */
		public abstract ExpressionVar eval(List<ExpressionVar> parameters) throws ExpressionException;		
	}

	/**
	 * Abstract definition of a supported expression function. A function is
	 * defined by a name, the number of parameters and the actual processing
	 * implementation.
	 */
	protected abstract class Function extends Operation{
		/**
		 * Number of parameters expected for this function. 
		 * <code>-1</code> denotes a variable number of parameters.
		 */
		private int numParams;

		/**
		 * Creates a new function with given name and parameter count.
		 * 
		 * @param name
		 *            The name of the function.
		 * @param numParams
		 *            The number of parameters for this function.
		 *            <code>-1</code> denotes a variable number of parameters.
		 */
		public Function(String name, int numParams) {
			this.oper = name.toUpperCase(Locale.ROOT);
			this.numParams = numParams;
		}

		public String getName() {
			return oper;
		}

		public int getNumParams() {
			return numParams;
		}

		public boolean numParamsVaries() {
			return numParams < 0;
		}
	}

	/**
	 * Abstract definition of a supported operator. An operator is defined by
	 * its name (pattern), precedence and if it is left- or right associative.
	 */
	protected abstract class Operator extends Operation{
		/**
		 * Operators precedence.
		 */
		private int precedence;
		/**
		 * Operator is left associative.
		 */
		private boolean leftAssoc;

		/**
		 * Creates a new operator.
		 * 
		 * @param oper
		 *            The operator name (pattern).
		 * @param precedence
		 *            The operators precedence.
		 * @param leftAssoc
		 *            <code>true</code> if the operator is left associative,
		 *            else <code>false</code>.
		 */
		public Operator(String oper, int precedence, boolean leftAssoc) {
			this.oper = oper;
			this.precedence = precedence;
			this.leftAssoc = leftAssoc;
		}

		public String getOper() {
			return oper;
		}

		public int getPrecedence() {
			return precedence;
		}

		public boolean isLeftAssoc() {
			return leftAssoc;
		}
	}

}
