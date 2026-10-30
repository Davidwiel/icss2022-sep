package nl.han.ica.icss.parser;

import java.util.Stack;


import nl.han.ica.datastructures.HanStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

import javax.swing.text.Style;

/**
 - This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.

 */
public class ASTListener extends ICSSBaseListener {

	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HanStack<>();
	}
	public AST getAST() {
		return ast;
	}

	@Override public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet stylesheet = new Stylesheet();
		ast.setRoot(stylesheet);
		currentContainer.push(stylesheet);
	}
	@Override public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterCss_rule(ICSSParser.Css_ruleContext ctx) {
		Stylerule stylerule = new Stylerule();
		currentContainer.peek().addChild(stylerule);
		currentContainer.push(stylerule);
	}
	@Override public void exitCss_rule(ICSSParser.Css_ruleContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterSelector(ICSSParser.SelectorContext ctx) {
		Selector selector;
		String text = ctx.getText();

		if(text.startsWith("#")) {
			selector = new IdSelector(text);
		} else if (text.startsWith(".")) {
			selector = new ClassSelector(text);
		} else {
			selector = new TagSelector(text);
		}

		currentContainer.peek().addChild(selector);
	}

	@Override public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = new Declaration(ctx.LOWER_IDENT().getText());
		currentContainer.peek().addChild(declaration);
		currentContainer.push(declaration);
	}
	@Override public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterValue(ICSSParser.ValueContext ctx) {
		Literal literal = null;

		if (ctx.COLOR() != null) {
			literal = new ColorLiteral(ctx.COLOR().getText());
		} else if (ctx.PIXELSIZE() != null) {
			literal = new PixelLiteral(ctx.PIXELSIZE().getText());
		} else if (ctx.PERCENTAGE() != null) {
			literal = new PercentageLiteral(ctx.PERCENTAGE().getText());
		} else if (ctx.SCALAR() != null) {
			literal = new ScalarLiteral(ctx.SCALAR().getText());
		} else if (ctx.TRUE() != null || ctx.FALSE() != null) {
			literal = new BoolLiteral(ctx.getText());
		}

		if (literal != null) {
			currentContainer.peek().addChild(literal);
		}
	}

	@Override public void enterVariable_reference(ICSSParser.Variable_referenceContext ctx) {
		VariableReference reference = new VariableReference(ctx.getText());
		currentContainer.peek().addChild(reference);
	}

	@Override public void enterStatement(ICSSParser.StatementContext ctx) {
		ASTNode clause;
		if (ctx.conditional().IF() != null) {
			clause = new IfClause();
			currentContainer.peek().addChild(clause);
		} else {
			clause = new ElseClause();
			ASTNode parent = currentContainer.peek();
			java.util.List<ASTNode> children = parent.getChildren();

			// An else clause belongs to the preceding IfClause.
			// If the last added child of the current container is an IfClause, attach it there.
			if (!children.isEmpty() && children.get(children.size() - 1) instanceof IfClause) {
				children.get(children.size() - 1).addChild(clause);
			} else {
				parent.addChild(clause); // Fallback
			}
		}
		currentContainer.push(clause);
	}
	@Override public void exitStatement(ICSSParser.StatementContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterVariable_assignment(ICSSParser.Variable_assignmentContext ctx) {
		VariableAssignment assignment = new VariableAssignment();
		assignment.name = new VariableReference(ctx.CAPITAL_IDENT().getText());
		currentContainer.peek().addChild(assignment);
		currentContainer.push(assignment);
	}
	@Override public void exitVariable_assignment(ICSSParser.Variable_assignmentContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterOperation(ICSSParser.OperationContext ctx) {
		if (ctx.getChildCount() == 3) {
			Operation operation;
			if (ctx.MUL() != null) {
				operation = new MultiplyOperation();
			} else if (ctx.PLUS() != null) {
				operation = new AddOperation();
			} else {
				operation = new SubtractOperation();
			}
			currentContainer.peek().addChild(operation);
			currentContainer.push(operation);
		}
	}
	@Override public void exitOperation(ICSSParser.OperationContext ctx) {
		if (ctx.getChildCount() == 3) {
			currentContainer.pop();
		}
	}

}