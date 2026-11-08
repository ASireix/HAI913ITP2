package hai913i.tp1.parse;

import hai913i.tp1.utils.NodeUtils;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;

import static hai913i.tp1.utils.NodeUtils.label;

public class JdtVisitor extends ASTVisitor {
    int depth;
    boolean liteMode = false;

    public JdtVisitor(boolean liteMode){
        this.liteMode = liteMode;
    }

    private void printNode(ASTNode node){
        System.out.println("   " + "  ".repeat(depth) + "entre " + NodeUtils.label(node));
    }

    @Override
    public boolean preVisit2(ASTNode node) {
        depth++;
        if (!liteMode){
            printNode(node);
        }

        //Mettre return à false fait arrêter la visite des descendants du noeud sans arrêter le parcours de l'arbre
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        if (liteMode){
            printNode(node);
        }
        return true;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (liteMode){
            printNode(node);
        }
        return true;
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        if (liteMode){
            printNode(node);
        }
        return true;
    }

    @Override
    public boolean visit(SuperConstructorInvocation node) {
        if (liteMode){
            printNode(node);
        }
        return true;
    }

    @Override
    public void postVisit(ASTNode node) {
        depth--;
    }
}
