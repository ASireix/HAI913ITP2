package hai913i.tp1.parse;

import hai913i.tp1.model.AnalysisFacts.*;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.*;

/** Extrait les membres directs de chaque type, sans effectuer d'affichage. */
public class JdtVisitorClasses extends ASTVisitor {
    private final String fileName;
    private final List<TypeFact> types = new ArrayList<>();

    public JdtVisitorClasses(String fileName) { this.fileName = fileName; }
    public List<TypeFact> types() { return List.copyOf(types); }

    @Override public boolean visit(TypeDeclaration node) {
        return collectType(node, node.isInterface() ? "Interface" : "Classe");
    }
    @Override public boolean visit(EnumDeclaration node) { return collectType(node, "Enumeration"); }
    @Override public boolean visit(RecordDeclaration node) { return collectType(node, "Record"); }
    @Override public boolean visit(AnonymousClassDeclaration node) { return false; }
    @Override public boolean visit(AnnotationTypeDeclaration node) { return false; }

    private boolean collectType(AbstractTypeDeclaration node, String kind) {
        if (!(node.getParent() instanceof CompilationUnit)
                && !(node.getParent() instanceof AbstractTypeDeclaration)) return false;
        CompilationUnit unit = (CompilationUnit) node.getRoot();
        String pkg = unit.getPackage() == null ? "(défaut)" : unit.getPackage().getName().getFullyQualifiedName();
        ITypeBinding binding = node.resolveBinding();
        String name = binding == null ? node.getName().getIdentifier() : binding.getQualifiedName();
        List<String> parents = new ArrayList<>();
        List<String> interfaces = new ArrayList<>();
        if (binding != null) {
            for (ITypeBinding parent = binding.getSuperclass(); parent != null
                    && !parent.getQualifiedName().equals("java.lang.Object"); parent = parent.getSuperclass()) {
                parents.add(parent.getQualifiedName());
            }
            for (ITypeBinding iface : binding.getInterfaces()) interfaces.add(iface.getQualifiedName());
        }
        List<FieldFact> fields = new ArrayList<>();
        List<MethodFact> methods = new ArrayList<>();
        for (Object declaration : node.bodyDeclarations()) {
            if (declaration instanceof FieldDeclaration field) {
                String visibility = visibility(field.getModifiers(), kind.equals("Interface"));
                for (Object fragment : field.fragments()) {
                    VariableDeclarationFragment variable = (VariableDeclarationFragment) fragment;
                    fields.add(new FieldFact(variable.getName().getIdentifier(),
                            field.getType() + "[]".repeat(variable.extraDimensions().size()), visibility));
                }
            } else if (declaration instanceof MethodDeclaration method) {
                List<String> parameterTypes = MethodIdentity.parameterTypes(method);
                String id = method.resolveBinding() == null
                        ? MethodIdentity.id(name, method.isConstructor() ? "<init>" : method.getName().getIdentifier(), parameterTypes)
                        : MethodIdentity.id(method.resolveBinding());
                JdtVisitorMethods visitor = new JdtVisitorMethods(id);
                if (method.getBody() != null) method.getBody().accept(visitor);
                Block body = method.getBody();
                int bodyLoc = body == null ? 0 : unit.getLineNumber(body.getStartPosition() + body.getLength() - 1)
                        - unit.getLineNumber(body.getStartPosition()) + 1;
                methods.add(new MethodFact(method.getName().getIdentifier(), parameterTypes,
                        method.isConstructor(), visitor.calls(), id, bodyLoc));
            }
        }
        types.add(new TypeFact(fileName, name, kind, pkg, parents, interfaces, fields, methods));
        return true;
    }

    private String visibility(int modifiers, boolean inInterface) {
        if (Modifier.isPublic(modifiers)) return "public";
        if (Modifier.isProtected(modifiers)) return "protected";
        if (Modifier.isPrivate(modifiers)) return "private";
        return inInterface ? "public implicite" : "package";
    }
}
