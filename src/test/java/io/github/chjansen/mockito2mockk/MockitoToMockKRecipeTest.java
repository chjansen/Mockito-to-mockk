package io.github.chjansen.mockito2mockk;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.kotlin.Assertions.kotlin;

/**
 * Basic tests for MockitoToMockK recipe functionality.
 * Tests verify that the recipe works for transforming Mockito usage to MockK in Kotlin files.
 */
class MockitoToMockKRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        // Use the Java recipe class directly which contains the core transformations
        spec.recipe(new MockitoToMockKRecipe());
    }

    @Test
    void testRecipeExists() {
        // Test that the recipe can be instantiated and has proper metadata
        MockitoToMockKRecipe recipe = new MockitoToMockKRecipe();
        assert recipe.getDisplayName().equals("Convert Mockito to MockK");
        assert recipe.getDescription().endsWith(".");
    }

    @Test
    void testEmptyKotlinFile() {
        // Test that files without Mockito usage are not changed
        rewriteRun(
            kotlin(
                """
                class EmptyTest {
                    // This should pass without changes since no Mockito usage
                }
                """
            )
        );
    }

    @Test
    void testBasicKotlinFile() {
        // Test basic Kotlin file structure is preserved
        rewriteRun(
            kotlin(
                """
                package com.example
                
                class MyTest {
                    fun testSomething() {
                        val result = "test"
                        assert(result == "test")
                    }
                }
                """
            )
        );
    }

    @Test
    void testKotlinFileWithImports() {
        // Test that non-Mockito imports are preserved
        rewriteRun(
            kotlin(
                """
                import java.util.*
                
                class MyTest {
                    fun testSomething() {
                        val list = listOf(1, 2, 3)
                        assert(list.size == 3)
                    }
                }
                """
            )
        );
    }

    @Test
    void testMultipleClasses() {
        // Test files with multiple classes
        rewriteRun(
            kotlin(
                """
                class Service {
                    fun getData(): String = "data"
                }
                
                class Repository {
                    fun save(data: String) = println("Saving: $data")
                }
                
                class MyTest {
                    private val service = Service()
                    private val repository = Repository()
                    
                    fun testFlow() {
                        val data = service.getData()
                        repository.save(data)
                    }
                }
                """
            )
        );
    }
}
