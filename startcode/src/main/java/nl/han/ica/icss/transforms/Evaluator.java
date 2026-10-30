package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HanLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HanLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new HanLinkedList<>();
        variableValues.addFirst(new HashMap<>());

        if (ast == null || ast.root == null) {
            return;
        }

        ast.root.body = transformBody(ast.root.body);
    }

    private ArrayList<ASTNode> transformBody(ArrayList<ASTNode> nodes) {
        ArrayList<ASTNode> result = new ArrayList<>();
        if (nodes == null) {
            return result;
        }

        for (ASTNode node : nodes) {
            if (node instanceof VariableAssignment) {
                VariableAssignment assignment = (VariableAssignment) node;
                Literal literal = evaluateExpression(assignment.expression);
                assignment.expression = literal;
                setVariable(assignment.name.name, literal);
                result.add(assignment);
            } else if (node instanceof Stylerule) {
                Stylerule stylerule = (Stylerule) node;
                variableValues.addFirst(new HashMap<>());
                stylerule.body = transformBody(stylerule.body);
                variableValues.removeFirst();
                result.add(stylerule);
            } else if (node instanceof Declaration) {
                Declaration declaration = (Declaration) node;
                declaration.expression = evaluateExpression(declaration.expression);
                result.add(declaration);
            } else if (node instanceof IfClause) {
                IfClause ifClause = (IfClause) node;
                Literal condLiteral = evaluateExpression(ifClause.conditionalExpression);
                boolean condition = false;
                if (condLiteral instanceof BoolLiteral) {
                    condition = ((BoolLiteral) condLiteral).value;
                }

                ArrayList<ASTNode> branch;
                if (condition) {
                    branch = ifClause.body;
                } else if (ifClause.elseClause != null) {
                    branch = ifClause.elseClause.body;
                } else {
                    branch = new ArrayList<>();
                }

                ArrayList<ASTNode> inlined = transformBody(branch);
                result.addAll(inlined);
            } else {
                result.add(node);
            }
        }

        return result;
    }

    private Literal evaluateExpression(Expression expression) {
        if (expression instanceof Literal) {
            return (Literal) expression;
        } else if (expression instanceof VariableReference) {
            String name = ((VariableReference) expression).name;
            return getVariable(name);
        } else if (expression instanceof Operation) {
            return evaluateOperation((Operation) expression);
        }
        return null;
    }

    private Literal evaluateOperation(Operation operation) {
        Literal left = evaluateExpression(operation.lhs);
        Literal right = evaluateExpression(operation.rhs);

        if (left == null || right == null) {
            return null;
        }

        if (operation instanceof AddOperation) {
            if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
                return new PixelLiteral(((PixelLiteral) left).value + ((PixelLiteral) right).value);
            } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
                return new PercentageLiteral(((PercentageLiteral) left).value + ((PercentageLiteral) right).value);
            } else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
                return new ScalarLiteral(((ScalarLiteral) left).value + ((ScalarLiteral) right).value);
            }
        } else if (operation instanceof SubtractOperation) {
            if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
                return new PixelLiteral(((PixelLiteral) left).value - ((PixelLiteral) right).value);
            } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
                return new PercentageLiteral(((PercentageLiteral) left).value - ((PercentageLiteral) right).value);
            } else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
                return new ScalarLiteral(((ScalarLiteral) left).value - ((ScalarLiteral) right).value);
            }
        } else if (operation instanceof MultiplyOperation) {
            if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
                return new ScalarLiteral(((ScalarLiteral) left).value * ((ScalarLiteral) right).value);
            } else if (left instanceof ScalarLiteral && right instanceof PixelLiteral) {
                return new PixelLiteral(((ScalarLiteral) left).value * ((PixelLiteral) right).value);
            } else if (left instanceof PixelLiteral && right instanceof ScalarLiteral) {
                return new PixelLiteral(((PixelLiteral) left).value * ((ScalarLiteral) right).value);
            } else if (left instanceof ScalarLiteral && right instanceof PercentageLiteral) {
                return new PercentageLiteral(((ScalarLiteral) left).value * ((PercentageLiteral) right).value);
            } else if (left instanceof PercentageLiteral && right instanceof ScalarLiteral) {
                return new PercentageLiteral(((PercentageLiteral) left).value * ((ScalarLiteral) right).value);
            }
        }

        return null;
    }

    private void setVariable(String name, Literal literal) {
        for (int i = 0; i < variableValues.getSize(); i++) {
            HashMap<String, Literal> scope = variableValues.get(i);
            if (scope.containsKey(name)) {
                scope.put(name, literal);
                return;
            }
        }
        variableValues.getFirst().put(name, literal);
    }

    private Literal getVariable(String name) {
        for (int i = 0; i < variableValues.getSize(); i++) {
            HashMap<String, Literal> scope = variableValues.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }
}
