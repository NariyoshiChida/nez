package nez.cc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import nez.Grammar;
import nez.expr.Expression;
import nez.expr.Rule;
import nez.util.FileBuilder;
import nez.util.UList;

public class GrammarGenerator {
	final protected FileBuilder file;
	public GrammarGenerator(String fileName) {
		this.file = new FileBuilder(fileName);
	}
	HashMap<Class<?>, Method> methodMap = new HashMap<Class<?>, Method>();
	public final void visit(Expression p) {
		Method m = lookupMethod("visit", p.getClass());
		if(m != null) {
			try {
				m.invoke(this, p);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		else {
			visitUndefined(p);
		}
	}

	void visitUndefined(Expression p) {
		System.out.println("undefined: " + p.getClass());
	}

	protected final Method lookupMethod(String method, Class<?> c) {
		Method m = this.methodMap.get(c);
		if(m == null) {
			String name = method + c.getSimpleName();
			try {
				m = this.getClass().getMethod(name, c);
			} catch (NoSuchMethodException e) {
				return null;
			} catch (SecurityException e) {
				return null;
			}
			this.methodMap.put(c, m);
		}
		return m;
	}
	
	public void generate(Grammar grammar) {
		makeHeader();
		UList<Rule> list = grammar.getDefinedRuleList();
		for(Rule r: list) {
			visitRule(r);
		}
		makeFooter();
		file.writeNewLine();
		file.flush();
	}

	public void makeHeader() {
		file.write("// Nez File");
	}
	
	public void visitRule(Rule r) {
		file.writeIndent(r.toString());
	}

	public void makeFooter() {

	}

}