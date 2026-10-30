package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HanLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;



public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HanLinkedList<>();

        variableTypes.addFirst(new HashMap<>());

        checkNode(ast.root);
    }

    public void checkNode(ASTNode node) {
        if (node instanceof VariableReference) {
            VariableReference reference = (VariableReference) node;
            String variableName = reference.name;

            if (!isDeclared(variableName)) {
                reference.setError("Variable '" + variableName + "' not declared.");
            }
        } else if (node instanceof VariableAssignment) {


        } else if (node instanceof Operation) {
            Operation operation = (Operation) node;
            if (!checkOperation(operation)) {
                operation.setError("Value " +
                        "" +
                        "")
            }


        }
    }

    private Boolean isDeclared(String var){
        for (int i = 0; i < variableTypes.getSize(); i++) {
            if (variableTypes.get(i).containsKey(var)) {
                return true;
            }
        }

        return false;
    }

    public Boolean checkOperation(Operation operation) {
        ArrayList<ASTNode> arrayList = operation.getChildren();
        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i) instanceof ScalarLiteral || arrayList.get(i) instanceof PercentageLiteral) {
                return true;
            }
        }
        return false;
    }
}
