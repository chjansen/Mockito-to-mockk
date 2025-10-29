package io.github.chjansen.mockito2mockk;

import org.openrewrite.*;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.TypeUtils;

import java.util.List;

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
        return "Converts Mockito usage to MockK in Kotlin test files.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new FindSourceFiles("**/*.kt"),
                new JavaIsoVisitor<ExecutionContext>() {

                    // Method matchers for Mockito methods
                    private final MethodMatcher mockitoVerify = new MethodMatcher("org.mockito.Mockito.verify(..)");
                    private final MethodMatcher mockitoEq = new MethodMatcher("org.mockito.ArgumentMatchers.eq(..)");
                    private final MethodMatcher mockitoAny = new MethodMatcher("org.mockito.ArgumentMatchers.any(..)");
                    private final MethodMatcher mockitoWhen = new MethodMatcher("org.mockito.Mockito.when(..)");

                    @Override
                    public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
                        // Remove Mockito imports and add MockK imports
                        doAfterVisit(new RemoveImport<>("org.mockito.*", false));
                        doAfterVisit(new RemoveImport<>("org.mockito.kotlin.*", false));
                        doAfterVisit(new RemoveImport<>("org.mockito.junit.jupiter.*", false));

                        doAfterVisit(new AddImport<>("io.mockk.*", null, false));
                        doAfterVisit(new AddImport<>("io.mockk.junit5.MockKExtension", null, false));

                        return super.visitCompilationUnit(cu, ctx);
                    }

                    @Override
                    public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
                        J.Annotation a = super.visitAnnotation(annotation, ctx);

                        if (TypeUtils.isOfClassType(a.getType(), "org.mockito.Mock")) {
                            // Remove @Mock annotation - MockK uses different approach
                            return null;
                        }

                        if (TypeUtils.isOfClassType(a.getType(), "org.mockito.InjectMocks")) {
                            // Remove @InjectMocks annotation - MockK uses different approach
                            return null;
                        }

                        if (TypeUtils.isOfClassType(a.getType(), "org.junit.jupiter.api.extension.ExtendWith")) {
                            // Change MockitoExtension to MockKExtension
                            if (a.getArguments() != null) {
                                for (Expression arg : a.getArguments()) {
                                    if (arg instanceof J.FieldAccess) {
                                        J.FieldAccess fa = (J.FieldAccess) arg;
                                        if ("MockitoExtension".equals(fa.getSimpleName()) &&
                                                fa.getTarget() instanceof J.Identifier &&
                                                "class".equals(((J.Identifier) fa.getTarget()).getSimpleName())) {
                                            JavaTemplate template = JavaTemplate.builder("MockKExtension::class")
                                                    .imports("io.mockk.junit5.MockKExtension")
                                                    .build();

                                            return template.apply(getCursor(), a.getCoordinates().replace());
                                        }
                                    }
                                }
                            }
                        }

                        return a;
                    }

                    @Override
                    public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, ExecutionContext ctx) {
                        J.VariableDeclarations mv = super.visitVariableDeclarations(multiVariable, ctx);

                        // Convert @Mock annotated fields to MockK equivalents
                        if (mv.getLeadingAnnotations().stream().anyMatch(a ->
                                TypeUtils.isOfClassType(a.getType(), "org.mockito.Mock")
                        )) {
                            // Remove @Mock annotation and initialize with mockk()
                            J.VariableDeclarations.NamedVariable variable = mv.getVariables().get(0);

                            JavaTemplate template = JavaTemplate.builder("private val #{} = mockk<#{}>()")
                                    .imports("io.mockk.mockk")
                                    .build();

                            return template.apply(
                                    getCursor(),
                                    mv.getCoordinates().replace(),
                                    variable.getSimpleName(),
                                    mv.getTypeExpression() != null ? mv.getTypeExpression() : "Any"
                            );
                        }

                        return mv;
                    }

                    @Override
                    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                        J.MethodInvocation m = super.visitMethodInvocation(method, ctx);

                        // Convert verify() calls
                        if (mockitoVerify.matches(m)) {
                            JavaTemplate template = JavaTemplate.builder("verify { #{} }")
                                    .imports("io.mockk.verify")
                                    .build();
                            return template.apply(getCursor(), m.getCoordinates().replace(), m.getArguments().get(0));
                        }

                        // Convert eq() calls
                        if (mockitoEq.matches(m)) {
                            JavaTemplate template = JavaTemplate.builder("eq(#{})")
                                    .imports("io.mockk.eq")
                                    .build();
                            return template.apply(getCursor(), m.getCoordinates().replace(), m.getArguments().get(0));
                        }

                        // Convert any() calls
                        if (mockitoAny.matches(m)) {
                            JavaTemplate template = JavaTemplate.builder("any()")
                                    .imports("io.mockk.any")
                                    .build();
                            return template.apply(getCursor(), m.getCoordinates().replace());
                        }

                        // Convert when() calls to every {} returns
                        if (mockitoWhen.matches(m)) {
                            JavaTemplate template = JavaTemplate.builder("every { #{} }")
                                    .imports("io.mockk.every")
                                    .build();
                            return template.apply(getCursor(), m.getCoordinates().replace(), m.getArguments().get(0));
                        }

                        // Convert .thenReturn() to returns
                        if (m.getSelect() != null && "thenReturn".equals(m.getSimpleName())) {
                            JavaTemplate template = JavaTemplate.builder("returns #{}")
                                    .build();
                            return template.apply(getCursor(), m.getCoordinates().replace(), m.getArguments().get(0));
                        }

                        return m;
                    }
                }
        );
    }
}