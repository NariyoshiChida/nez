package org.peg4d.expression;

import java.util.TreeMap;

import nez.expr.Typestate;
import nez.util.UList;
import nez.util.UMap;

import org.peg4d.ParsingContext;
import org.peg4d.pegcode.GrammarVisitor;

public class ParsingAnd extends ParsingUnary {
	ParsingAnd(ParsingExpression e) {
		super(e);
	}
	@Override
	public String getInterningKey() { 
		return "&";
	}
	@Override
	public boolean checkAlwaysConsumed(String startNonTerminal, UList<String> stack) {
		return false;
	}
	@Override
	public int inferNodeTransition(UMap<String> visited) {
		int t = this.inner.inferNodeTransition(visited);
		if(t == Typestate.ObjectType) {  // typeCheck needs to report error
			return Typestate.BooleanType;
		}
		return t;
	}
	@Override
	public ParsingExpression checkNodeTransition(Typestate c) {
		if(c.required == Typestate.ObjectType) {
			c.required = Typestate.BooleanType;
			this.inner = this.inner.checkNodeTransition(c);
			c.required = Typestate.ObjectType;
		}
		else {
			this.inner = this.inner.checkNodeTransition(c);
		}
		return this;
	}
	@Override
	public ParsingExpression norm(boolean lexOnly, TreeMap<String,String> undefedFlags) {
		ParsingExpression e = inner.norm(lexOnly, undefedFlags);
		if(e == inner) {
			return this;
		}
		return ParsingExpression.newAnd(e);
	}

	@Override
	public void visit(GrammarVisitor visitor) {
		visitor.visitAnd(this);
	}
	@Override
	public boolean match(ParsingContext context) {
		long pos = context.getPosition();
		this.inner.matcher.match(context);
		context.rollback(pos);
		return !context.isFailure();
	}
	
	@Override
	public short acceptByte(int ch) {
		short r = this.inner.acceptByte(ch);
		if(r == Accept || r == Unconsumed) {
			return Unconsumed;
		}
		return r;
	}
	
}