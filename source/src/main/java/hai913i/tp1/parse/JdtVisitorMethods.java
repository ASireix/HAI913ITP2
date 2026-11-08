package hai913i.tp1.parse;

import hai913i.tp1.model.AnalysisFacts.CallFact;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.core.dom.*;

public class JdtVisitorMethods extends ASTVisitor {
    private final List<CallFact> calls = new ArrayList<>();

    private final String callerId;

    public JdtVisitorMethods(String callerId) { this.callerId = callerId; }

    public List<CallFact> calls() { return List.copyOf(calls); }

    @Override
    public boolean visit(MethodInvocation node) {
        IMethodBinding methodBinding = node.resolveMethodBinding();
        ITypeBinding receiver = findReceiver(node, methodBinding);
        collectCall(node, node.getName().getIdentifier(), methodBinding, receiver);
        return true;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        ITypeBinding enclosingType = node.getQualifier() == null
                ? findEnclosingType(node)
                : node.getQualifier().resolveTypeBinding();
        ITypeBinding receiver = enclosingType == null ? null : enclosingType.getSuperclass();
        collectCall(node, node.getName().getIdentifier(), node.resolveMethodBinding(), receiver);
        return true;
    }

    // Un receveur explicite donne son propre type, indépendamment de la cible.
    private ITypeBinding findReceiver(MethodInvocation node, IMethodBinding methodBinding) {
        Expression expression = node.getExpression();
        if (expression != null) {
            return expression.resolveTypeBinding();
        }
        if (methodBinding == null) {
            return null;
        }
        if (Modifier.isStatic(methodBinding.getModifiers())) {
            return methodBinding.getDeclaringClass();
        }
        return findEnclosingType(node);
    }

    private ITypeBinding findEnclosingType(ASTNode node) {
        for (ASTNode current = node.getParent(); current != null; current = current.getParent()) {
            if (current instanceof AbstractTypeDeclaration type) {
                return type.resolveBinding();
            }
            if (current instanceof AnonymousClassDeclaration anonymousType) {
                return anonymousType.resolveBinding();
            }
        }
        return null;
    }

    private String typeName(ITypeBinding binding) {
        if (binding == null) {
            return "non résolue";
        }
        return binding.getErasure().getQualifiedName();
    }

    private String targetName(IMethodBinding binding) {
        if (binding == null) {
            return "non résolue";
        }
        IMethodBinding declaration = binding.getMethodDeclaration();
        if (declaration.getDeclaringClass() == null) {
            return "non résolue";
        }
        return declaration.getDeclaringClass().getQualifiedName() + "#" + declaration.getName() + "()";
    }

    private void collectCall(ASTNode node, String name, IMethodBinding target, ITypeBinding receiver) {
        CompilationUnit unit = (CompilationUnit) node.getRoot();
        int lineNumber = unit.getLineNumber(node.getStartPosition());
        boolean resolved = target != null && !target.isRecovered() && target.getMethodDeclaration().getDeclaringClass() != null;
        boolean internal = resolved && target.getMethodDeclaration().getDeclaringClass().isFromSource();
        calls.add(new CallFact(name, lineNumber, targetName(target), typeName(receiver),
                resolved, internal, node instanceof SuperMethodInvocation, callerId,
                resolved ? Optional.of(MethodIdentity.id(target)) : Optional.empty()));
    }
}
