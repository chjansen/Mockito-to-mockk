package io.github.chjansen.mockito2mockk;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class MockitoToMockKRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new MockitoToMockKRecipe());
    }

    @Test
    void convertMockAnnotation() {
        rewriteRun(
            java(
                """
                package org.mockito;
                public @interface Mock {}
                """
            ),
            java(
                """
                package io.mockk;
                public @interface MockK {}
                """
            ),
            java(
                """
                import org.mockito.Mock;
                
                class MyService {}
                
                public class MyTest {
                    @Mock
                    private MyService service;
                }
                """,
                """
                import io.mockk.MockK;
                
                class MyService {}
                
                public class MyTest {
                    @MockK
                    private MyService service;
                }
                """
            )
        );
    }

    @Test
    void convertInjectMocksAnnotation() {
        rewriteRun(
            java(
                """
                package org.mockito;
                public @interface InjectMocks {}
                """
            ),
            java(
                """
                package io.mockk;
                public @interface InjectMockKs {}
                """
            ),
            java(
                """
                import org.mockito.InjectMocks;
                
                class MyService {}
                
                public class MyTest {
                    @InjectMocks
                    private MyService service;
                }
                """,
                """
                import io.mockk.InjectMockKs;
                
                class MyService {}
                
                public class MyTest {
                    @InjectMockKs
                    private MyService service;
                }
                """
            )
        );
    }

    @Test
    void convertSpyAnnotation() {
        rewriteRun(
            java(
                """
                package org.mockito;
                public @interface Spy {}
                """
            ),
            java(
                """
                package io.mockk;
                public @interface SpyK {}
                """
            ),
            java(
                """
                import org.mockito.Spy;
                
                class MyService {}
                
                public class MyTest {
                    @Spy
                    private MyService service;
                }
                """,
                """
                import io.mockk.SpyK;
                
                class MyService {}
                
                public class MyTest {
                    @SpyK
                    private MyService service;
                }
                """
            )
        );
    }

    @Test
    void convertMultipleAnnotations() {
        rewriteRun(
            java(
                """
                package org.mockito;
                public @interface Mock {}
                """
            ),
            java(
                """
                package org.mockito;
                public @interface InjectMocks {}
                """
            ),
            java(
                """
                package io.mockk;
                public @interface MockK {}
                """
            ),
            java(
                """
                package io.mockk;
                public @interface InjectMockKs {}
                """
            ),
            java(
                """
                import org.mockito.Mock;
                import org.mockito.InjectMocks;
                
                class MyRepository {}
                class MyService {}
                
                public class MyTest {
                    @Mock
                    private MyRepository repository;
                    
                    @InjectMocks
                    private MyService service;
                }
                """,
                """
                import io.mockk.InjectMockKs;
                import io.mockk.MockK;
                
                class MyRepository {}
                class MyService {}
                
                public class MyTest {
                    @MockK
                    private MyRepository repository;
                    
                    @InjectMockKs
                    private MyService service;
                }
                """
            )
        );
    }
}
