package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HanLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;
    private Set<String> allDeclaredVariables;

    public void check(AST ast) {
        variableTypes = new HanLinkedList<>();
        allDeclaredVariables = new HashSet<>();

        if (ast == null || ast.root == null) {
            return;
        }

        collectAllDeclaredVariables(ast.root);

        variableTypes.addFirst(new HashMap<>());

        checkStylesheet(ast.root);
    }

    public IHANLinkedList<HashMap<String, ExpressionType>> getVariableTypes() {
        return variableTypes;
    }

    private void collectAllDeclaredVariables(ASTNode node) {
        if (node == null) {
            return;
        }
        if (node instanceof VariableAssignment) {
            VariableAssignment assignment = (VariableAssignment) node;
            if (assignment.name != null && assignment.name.name != null) {
                allDeclaredVariables.add(assignment.name.name);
            }
        }
        for (ASTNode child : node.getChildren()) {
            collectAllDeclaredVariables(child);
        }
    }

    private void checkStylesheet(Stylesheet stylesheet) {
        if (stylesheet.body == null) {
            return;
        }
        for (ASTNode node : stylesheet.body) {
            checkNode(node);
        }
    }

    public void checkNode(ASTNode node) {
        if (node instanceof VariableAssignment) {
            checkVariableAssignment((VariableAssignment) node);
        } else if (node instanceof Stylerule) {
            checkStylerule((Stylerule) node);
        } else if (node instanceof Declaration) {
            checkDeclaration((Declaration) node);
        } else if (node instanceof IfClause) {
            checkIfClause((IfClause) node);
        } else if (node instanceof ElseClause) {
            checkElseClause((ElseClause) node);
        } else if (node instanceof VariableReference) {
            checkVariableReference((VariableReference) node);
        } else if (node instanceof Operation) {
            checkOperation((Operation) node);
        }
    }

    private void checkVariableAssignment(VariableAssignment assignment) {
        ExpressionType type = ExpressionType.UNDEFINED;
        if (assignment.expression != null) {
            type = getExpressionType(assignment.expression);
        }
        if (assignment.name != null && assignment.name.name != null) {
            variableTypes.getFirst().put(assignment.name.name, type);
        }
    }

    private void checkStylerule(Stylerule stylerule) {
        variableTypes.addFirst(new HashMap<>());

        if (stylerule.body != null) {
            for (ASTNode node : stylerule.body) {
                checkNode(node);
            }
        }

        variableTypes.removeFirst();
    }

    private void checkDeclaration(Declaration declaration) {
        if (declaration.property == null || declaration.expression == null) {
            return;
        }

        String prop = declaration.property.name;
        ExpressionType exprType = getExpressionType(declaration.expression);

        if (exprType == ExpressionType.UNDEFINED) {
            return;
        }

        if (prop.equals("color") || prop.equals("background-color")) {
            if (exprType != ExpressionType.COLOR) {
                declaration.setError("Property '" + prop + "' must have a color value, but got " + exprType);
            }
        } else if (prop.equals("width") || prop.equals("height")) {
            if (exprType != ExpressionType.PIXEL && exprType != ExpressionType.PERCENTAGE) {
                declaration.setError("Property '" + prop + "' must have a pixel or percentage value, but got " + exprType);
            }
        } else {
            declaration.setError("Unknown property '" + prop + "'.");
        }
    }

    private void checkIfClause(IfClause ifClause) {
        if (ifClause.conditionalExpression != null) {
            ExpressionType condType = getExpressionType(ifClause.conditionalExpression);
            if (condType != ExpressionType.BOOL) {
                ifClause.setError("Condition in if-statement must be of type boolean, but got " + condType);
            }
        } else {
            ifClause.setError("If-statement missing conditional expression.");
        }

        variableTypes.addFirst(new HashMap<>());
        if (ifClause.body != null) {
            for (ASTNode node : ifClause.body) {
                checkNode(node);
            }
        }
        variableTypes.removeFirst();

        if (ifClause.elseClause != null) {
            checkElseClause(ifClause.elseClause);
        }
    }

    private void checkElseClause(ElseClause elseClause) {
        variableTypes.addFirst(new HashMap<>());
        if (elseClause.body != null) {
            for (ASTNode node : elseClause.body) {
                checkNode(node);
            }
        }
        variableTypes.removeFirst();
    }

    private ExpressionType getExpressionType(Expression expression) {
        if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof VariableReference) {
            return checkVariableReference((VariableReference) expression);
        } else if (expression instanceof Operation) {
            return checkOperation((Operation) expression);
        }
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkVariableReference(VariableReference reference) {
        String name = reference.name;
        if (!isDeclared(name)) {
            if (allDeclaredVariables.contains(name)) {
                reference.setError("Variable '" + name + "' is used outside of its scope.");
            } else {
                reference.setError("Variable '" + name + "' is not declared.");
            }
            return ExpressionType.UNDEFINED;
        }
        return lookupVariableType(name);
    }

    public ExpressionType checkOperation(Operation operation) {
        if (operation.lhs == null || operation.rhs == null) {
            operation.setError("Operation is missing operand.");
            return ExpressionType.UNDEFINED;
        }

        ExpressionType lhsType = getExpressionType(operation.lhs);
        ExpressionType rhsType = getExpressionType(operation.rhs);

        if (lhsType == ExpressionType.UNDEFINED || rhsType == ExpressionType.UNDEFINED) {
            return ExpressionType.UNDEFINED;
        }

        if (lhsType == ExpressionType.COLOR || rhsType == ExpressionType.COLOR) {
            operation.setError("Colors may not be used in operations.");
            return ExpressionType.UNDEFINED;
        }

        if (lhsType == ExpressionType.BOOL || rhsType == ExpressionType.BOOL) {
            operation.setError("Booleans may not be used in arithmetic operations.");
            return ExpressionType.UNDEFINED;
        }

        if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (lhsType != rhsType) {
                operation.setError("Operands of " +
                        (operation instanceof AddOperation ? "addition" : "subtraction") +
                        " must be of the same type, but got " + lhsType + " and " + rhsType);
                return ExpressionType.UNDEFINED;
            }
            return lhsType;
        }

        if (operation instanceof MultiplyOperation) {
            if (lhsType != ExpressionType.SCALAR && rhsType != ExpressionType.SCALAR) {
                operation.setError("At least one operand of multiplication must be a scalar, but got " +
                        lhsType + " and " + rhsType);
                return ExpressionType.UNDEFINED;
            }
            if (lhsType == ExpressionType.SCALAR) {
                return rhsType;
            } else {
                return lhsType;
            }
        }

        return ExpressionType.UNDEFINED;
    }

    private boolean isDeclared(String var) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            if (variableTypes.get(i).containsKey(var)) {
                return true;
            }
        }
        return false;
    }

    private ExpressionType lookupVariableType(String var) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> scope = variableTypes.get(i);
            if (scope.containsKey(var)) {
                return scope.get(var);
            }
        }
        return null;
    }
}
