package hai913i.tp1.parse;

import java.util.Arrays;
import java.util.List;
import org.eclipse.jdt.core.dom.*;

/** Une même convention pour les déclarations et les cibles des appels. */
public final class MethodIdentity {
    private MethodIdentity() {}
    public static List<String> parameterTypes(IMethodBinding binding) {
        return Arrays.stream(binding.getMethodDeclaration().getParameterTypes())
                .map(t -> t.getErasure().getQualifiedName()).toList();
    }
    public static String id(IMethodBinding binding) {
        IMethodBinding method = binding.getMethodDeclaration();
        return id(method.getDeclaringClass().getErasure().getQualifiedName(),
                method.isConstructor() ? "<init>" : method.getName(), parameterTypes(method));
    }
    public static String id(String owner, String name, List<String> parameters) {
        return owner + "#" + name + "(" + String.join(",", parameters) + ")";
    }
    public static List<String> parameterTypes(MethodDeclaration node) {
        if (node.resolveBinding() != null) return parameterTypes(node.resolveBinding());
        return ((List<?>) node.parameters()).stream().map(p -> {
            SingleVariableDeclaration parameter = (SingleVariableDeclaration) p;
            return parameter.getType().toString() + "[]".repeat(parameter.extraDimensions().size()
                    + (parameter.isVarargs() ? 1 : 0));
        }).toList();
    }
}
