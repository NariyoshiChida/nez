package nez.expr;

import nez.SourceContext;
import nez.ast.Node;
import nez.ast.SourcePosition;
import nez.util.UList;
import nez.util.UMap;
import nez.vm.FailPush;
import nez.vm.Instruction;
import nez.vm.Optimizer;

public class Repetition extends Unary {
	Repetition(SourcePosition s, Expression e) {
		super(s, e);
	}
	@Override
	Expression dupUnary(Expression e) {
		return (this.inner != e) ? Factory.newRepetition(this.s, e) : this;
	}
	@Override
	public String getPredicate() { 
		return "*";
	}
	@Override
	public String getInterningKey() { 
		return "*";
	}
	@Override
	public boolean checkAlwaysConsumed(ExpressionChecker checker, String startNonTerminal, UList<String> stack) {
		return false;
	}
	@Override
	public int inferNodeTransition(UMap<String> visited) {
		int t = this.inner.inferNodeTransition(visited);
		if(t == NodeTransition.ObjectType) {
			return NodeTransition.BooleanType;
		}
		return t;
	}
	@Override
	public Expression checkNodeTransition(ExpressionChecker checker, NodeTransition c) {
		int required = c.required;
		if(!this.inner.isAlwaysConsumed()) {
			checker.reportWarning(s, "empty repetition");
		}
		Expression inn = this.inner.checkNodeTransition(checker, c);
		if(required != NodeTransition.OperationType && c.required == NodeTransition.OperationType) {
			checker.reportWarning(s, "unable to create objects in repetition => removed!!");
			this.inner = inn.removeNodeOperator();
			c.required = required;
		}
		else {
			this.inner = inn;
		}
		return this;
	}
	@Override public short acceptByte(int ch) {
		short r = this.inner.acceptByte(ch);
		if(r == Accept) {
			return Accept;
		}
		return Unconsumed;
	}
	@Override
	public boolean match(SourceContext context) {
		long ppos = -1;
		long pos = context.getPosition();
//		long f = context.rememberFailure();
		while(ppos < pos) {
			Node left = context.left;
			if(!this.inner.matcher.match(context)) {
				context.left = left;
				left = null;
				break;
			}
			ppos = pos;
			pos = context.getPosition();
			left = null;
		}
//		context.forgetFailure(f);
		return true;
	}
	
	@Override
	public Instruction encode(Optimizer optimizer, Instruction next) {
		return new FailPush(optimizer, this, next);
	}


}