package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;

public class Generator {

    public String generate(AST ast) {
        if (ast == null || ast.root == null || ast.root.body == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (ASTNode node : ast.root.body) {
            if (node instanceof Stylerule) {
                generateStylerule((Stylerule) node, sb);
            }
        }

        return sb.toString();
    }

    private void generateStylerule(Stylerule stylerule, StringBuilder sb) {
        if (stylerule.selectors != null) {
            for (int i = 0; i < stylerule.selectors.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(stylerule.selectors.get(i).toString());
            }
        }
        sb.append(" {\n");

        if (stylerule.body != null) {
            for (ASTNode node : stylerule.body) {
                if (node instanceof Declaration) {
                    Declaration declaration = (Declaration) node;
                    sb.append("  ")
                      .append(declaration.property.name)
                      .append(": ")
                      .append(expressionToString(declaration.expression))
                      .append(";\n");
                }
            }
        }

        sb.append("}\n");
    }

    private String expressionToString(Expression expression) {
        if (expression instanceof PixelLiteral) {
            return ((PixelLiteral) expression).value + "px";
        } else if (expression instanceof PercentageLiteral) {
            return ((PercentageLiteral) expression).value + "%";
        } else if (expression instanceof ColorLiteral) {
            return ((ColorLiteral) expression).value;
        } else if (expression instanceof ScalarLiteral) {
            return String.valueOf(((ScalarLiteral) expression).value);
        } else if (expression instanceof BoolLiteral) {
            return ((BoolLiteral) expression).value ? "TRUE" : "FALSE";
        } else if (expression != null) {
            return expression.getNodeLabel();
        }
        return "";
    }
}
