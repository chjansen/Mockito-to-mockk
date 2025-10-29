# Mockito-to-mockk

This recipe is meant for Kotlin applications where tests are written using Mockito and moves them to the Kotlin mock framework MockK.

## Overview

This project provides an OpenRewrite recipe that automatically converts test code from using the Mockito mocking framework to MockK, which is better suited for Kotlin projects.

## What It Does

The recipe performs the following transformations:

### Annotation Conversions

- `@Mock` → `@MockK`
- `@InjectMocks` → `@InjectMockKs`
- `@Spy` → `@SpyK`

### Import Changes

- `org.mockito.Mock` → `io.mockk.MockK`
- `org.mockito.InjectMocks` → `io.mockk.InjectMockKs`
- `org.mockito.Spy` → `io.mockk.SpyK`
- Removes `org.mockito.junit.MockitoJUnitRunner` (not needed for MockK)

### Dependency Changes

- Replaces `org.mockito:mockito-core` with `io.mockk:mockk`
- Replaces `org.mockito:mockito-inline` with `io.mockk:mockk`
- Removes `org.mockito:mockito-junit-jupiter`

## Usage

### Building the Recipe

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Using the Recipe

This recipe can be applied to your project using OpenRewrite's Gradle or Maven plugins.

#### Gradle

Add to your `build.gradle`:

```groovy
plugins {
    id("org.openrewrite.rewrite") version "6.5.0"
}

rewrite {
    activeRecipe("io.github.chjansen.mockito2mockk.MockitoToMockK")
}

dependencies {
    rewrite("io.github.chjansen:mockito-to-mockk:1.0.0")
}
```

Then run:

```bash
./gradlew rewriteRun
```

#### Maven

Add to your `pom.xml`:

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>5.5.0</version>
    <configuration>
        <activeRecipes>
            <recipe>io.github.chjansen.mockito2mockk.MockitoToMockK</recipe>
        </activeRecipes>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>io.github.chjansen</groupId>
            <artifactId>mockito-to-mockk</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</plugin>
```

Then run:

```bash
mvn rewrite:run
```

## Example Transformation

### Before

```java
import org.mockito.Mock;
import org.mockito.InjectMocks;

public class MyServiceTest {
    @Mock
    private MyRepository repository;
    
    @InjectMocks
    private MyService service;
}
```

### After

```java
import io.mockk.MockK;
import io.mockk.InjectMockKs;

public class MyServiceTest {
    @MockK
    private MyRepository repository;
    
    @InjectMockKs
    private MyService service;
}
```

## Important Notes

1. **Manual Review Required**: After running this recipe, you should review the changes as MockK has different semantics than Mockito for some operations.

2. **Method Call Conversions**: This recipe currently focuses on annotations, imports, and dependencies. Method calls require manual conversion or conversion to Kotlin first:
   - `when()` / `whenever()` → `every {}` - Requires Kotlin DSL syntax
   - `thenReturn()` → `returns` - Requires Kotlin infix function syntax  
   - `thenThrow()` → `throws` - Requires Kotlin infix function syntax
   - `any()`, `eq()` → MockK equivalents - Can be used with same imports after switching to `io.mockk.*`

3. **Recommended Approach**: 
   - First, run this recipe to convert annotations and imports
   - Then, convert your test files to Kotlin (.java → .kt)
   - Finally, manually update method calls to use MockK's Kotlin DSL

4. **Kotlin-Specific Features**: Consider leveraging MockK's Kotlin-specific features like relaxed mocks, capturing lambdas, and extension functions after migration.

##Development

### Project Structure

```
mockito-to-mockk/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── io/github/chjansen/mockito2mockk/
│   │   │       └── MockitoToMockKRecipe.java
│   │   └── resources/
│   │       └── META-INF/rewrite/
│   │           └── mockito-to-mockk.yml
│   └── test/
│       └── java/
│           └── io/github/chjansen/mockito2mockk/
│               └── MockitoToMockKRecipeTest.java
├── build.gradle.kts
└── README.md
```

### Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source and available under the [MIT License](LICENSE).

## Resources

- [OpenRewrite Documentation](https://docs.openrewrite.org/)
- [MockK Documentation](https://mockk.io/)
- [Mockito Documentation](https://site.mockito.org/)
