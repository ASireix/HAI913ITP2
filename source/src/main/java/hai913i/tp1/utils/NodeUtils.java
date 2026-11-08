package hai913i.tp1.utils;

import org.eclipse.jdt.core.dom.*;

public class NodeUtils {
    public static String label(ASTNode node) {
        String result = node.getClass().getSimpleName();
        if (node instanceof MethodDeclaration method) {
            result += " : " + method.getName() + (method.isConstructor() ? " (constructor)" : "");
        }
        return result;
    }
    public static String stringByDepth(int depth, String content) {
        return "    ".repeat(depth) + content;
    }
}
