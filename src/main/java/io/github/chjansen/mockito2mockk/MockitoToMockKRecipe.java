package io.github.chjansen.mockito2mockk;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.ChangeType;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

/**
 * Main recipe to convert Mockito mocking to MockK for Kotlin tests.
 */
public class MockitoToMockKRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Convert Mockito to MockK";
    }

    @Override
    public String getDescription() {
        return "Converts Mockito mocking framework usage to MockK for Kotlin tests.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MockitoToMockKVisitor();
    }

    private static class MockitoToMockKVisitor extends JavaIsoVisitor<ExecutionContext> {

        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
            // Visit imports first - change types
            cu = (J.CompilationUnit) new ChangeImportsVisitor().visitNonNull(cu, ctx);
            
            return cu;
        }
    }

    /**
     * Visitor to change Mockito imports to MockK imports
     */
    private static class ChangeImportsVisitor extends JavaIsoVisitor<ExecutionContext> {
        
        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
            // Apply multiple ChangeType recipes
            cu = (J.CompilationUnit) new ChangeType("org.mockito.Mock", "io.mockk.MockK", true)
                .getVisitor().visitNonNull(cu, ctx);
            cu = (J.CompilationUnit) new ChangeType("org.mockito.InjectMocks", "io.mockk.InjectMockKs", true)
                .getVisitor().visitNonNull(cu, ctx);
            cu = (J.CompilationUnit) new ChangeType("org.mockito.Spy", "io.mockk.SpyK", true)
                .getVisitor().visitNonNull(cu, ctx);
            
            return cu;
        }
        
        @Override
        public J.Import visitImport(J.Import _import, ExecutionContext ctx) {
            J.Import imp = super.visitImport(_import, ctx);
            
            String importStr = imp.getQualid().printTrimmed(getCursor());
            boolean isStatic = imp.isStatic();
            
            // Handle static imports for Mockito methods
            if (isStatic && importStr.equals("org.mockito.Mockito.mock")) {
                maybeRemoveImport("org.mockito.Mockito.mock");
                maybeAddImport("io.mockk.MockKKt", "mockk", false);
                return null;
            } else if (isStatic && importStr.equals("org.mockito.Mockito.verify")) {
                maybeRemoveImport("org.mockito.Mockito.verify");
                maybeAddImport("io.mockk.MockKKt", "verify", false);
                return null;
            } else if (isStatic && importStr.equals("org.mockito.Mockito.when")) {
                maybeRemoveImport("org.mockito.Mockito.when");
                maybeAddImport("io.mockk.MockKKt", "every", false);
                return null;
            } else if (isStatic && importStr.equals("org.mockito.Mockito.*")) {
                maybeRemoveImport("org.mockito.Mockito");
                maybeAddImport("io.mockk.MockKKt", "*", false);
                return null;
            } else if (!isStatic && importStr.equals("org.mockito.Mockito")) {
                maybeRemoveImport(importStr);
                maybeAddImport("io.mockk.MockKAnnotations");
                return null;
            }
            // ArgumentMatchers
            else if (isStatic && importStr.startsWith("org.mockito.ArgumentMatchers")) {
                String memberName = "*";
                if (importStr.contains(".") && !importStr.endsWith("*")) {
                    memberName = importStr.substring(importStr.lastIndexOf(".") + 1);
                }
                maybeRemoveImport(importStr);
                maybeAddImport("io.mockk.MockKKt", memberName, false);
                return null;
            }
            // JUnit runner
            else if (importStr.equals("org.mockito.junit.MockitoJUnitRunner")) {
                maybeRemoveImport(importStr);
                return null;
            }
            
            return imp;
        }
    }
}
