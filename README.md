# Mockito-to-mockk

This recipe is meant for Kotlin applications where tests are written using Mockito and migrates them to the Kotlin mock framework MockK.

## Overview

This project provides an OpenRewrite recipe that automatically converts test code from using the Mockito mocking framework to MockK, which is better suited for Kotlin projects. **The recipe works with both Java and Kotlin test files.**

## What It Does

The recipe performs the following transformations:

### Annotation Conversions (✅ Fully Automated)

- `@Mock` → `@MockK`
- `@InjectMocks` → `@InjectMockKs`
- `@Spy` → `@SpyK`

### Import Changes (✅ Fully Automated)

- `org.mockito.Mock` → `io.mockk.MockK`
- `org.mockito.InjectMocks` → `io.mockk.InjectMockKs`
- `org.mockito.Spy` → `io.mockk.SpyK`
- `org.mockito.Mockito.when` → `io.mockk.MockKKt.every`
- `org.mockito.Mockito.whenever` → `io.mockk.MockKKt.every`
- `org.mockito.Mockito.verify` → `io.mockk.MockKKt.verify`
- `org.mockito.ArgumentMatchers.*` → `io.mockk.*`
- Removes `org.mockito.junit.MockitoJUnitRunner` (not needed for MockK)

### Dependency Changes (✅ Fully Automated)

- Replaces `org.mockito:mockito-core` with `io.mockk:mockk`
- Replaces `org.mockito:mockito-inline` with `io.mockk:mockk`
- Removes `org.mockito:mockito-junit-jupiter`

### Method Call Transformations (⚠️ Requires Kotlin & Manual Review)

For Kotlin test files, the following transformations need manual conversion after running this recipe:

- `when(mock.method()).thenReturn(value)` → `every { mock.method() } returns value`
- `when(mock.method()).thenThrow(exception)` → `every { mock.method() } throws exception`
- `verify(mock).method()` → `verify { mock.method() }`
- `any()`, `eq()` → MockK equivalents (imports are updated automatically)

The import changes ensure these method calls will resolve to MockK functions after manual syntax updates.

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

The transformation happens in two steps:

### Step 1: Automated (OpenRewrite Recipe)

**Before** (`examples/Before.kt`):
```kotlin
import org.mockito.Mock
import org.mockito.InjectMocks
import org.mockito.Mockito.`when`

class UserServiceTest {
    @Mock
    private lateinit var userRepository: UserRepository
    
    @InjectMocks
    private lateinit var userService: UserService
    
    @Test
    fun `test user creation`() {
        `when`(userRepository.save(any())).thenReturn(user)
        verify(userRepository).save(any())
    }
}
```

**After Step 1** (`examples/AfterStep1-Automated.kt`):
```kotlin
import io.mockk.MockK
import io.mockk.InjectMockKs
import io.mockk.every  // Import updated automatically

class UserServiceTest {
    @MockK  // Annotation converted
    private lateinit var userRepository: UserRepository
    
    @InjectMockKs  // Annotation converted
    private lateinit var userService: UserService
    
    @Test
    fun `test user creation`() {
        // Method calls still need manual conversion
        `when`(userRepository.save(any())).thenReturn(user)
        verify(userRepository).save(any())
    }
}
```

### Step 2: Manual Conversion

**After Step 2** (`examples/AfterStep2-Manual.kt`):
```kotlin
import io.mockk.MockK
import io.mockk.InjectMockKs
import io.mockk.every
import io.mockk.verify

class UserServiceTest {
    @MockK
    private lateinit var userRepository: UserRepository
    
    @InjectMockKs
    private lateinit var userService: UserService
    
    @Test
    fun `test user creation`() {
        // Converted to MockK DSL
        every { userRepository.save(any()) } returns user
        verify { userRepository.save(any()) }
    }
}
```

See the `examples/` directory for complete before/after examples.

## Important Notes

1. **Works with Kotlin Test Code**: This recipe is designed for Kotlin test files (`.kt`) and will automatically convert Mockito annotations and imports to MockK equivalents.

2. **Automated vs. Manual Changes**:
   - ✅ **Automated**: Annotations (`@Mock`, `@InjectMocks`, `@Spy`) and imports are converted automatically
   - ⚠️ **Manual**: Method call syntax transformations require manual conversion:
     - `when().thenReturn()` → `every {} returns`
     - `verify(mock).method()` → `verify { mock.method() }`
     - Argument matchers `any()`, `eq()` are already available through updated imports

3. **Recommended Workflow**: 
   - Step 1: Run this OpenRewrite recipe to convert annotations, imports, and dependencies
   - Step 2: Manually update method call syntax to use MockK's Kotlin DSL
   - Step 3: Run your tests and fix any remaining issues

4. **Why Manual Changes?**: MockK uses Kotlin's DSL features (lambda blocks, infix functions) which require syntax changes beyond simple method renames. OpenRewrite can handle type/import changes, but complex syntax transformations to Kotlin DSL are best done with IDE assistance or manually.

5. **Kotlin-Specific Features**: After migration, consider leveraging MockK's Kotlin-specific features like:
   - Relaxed mocks (`relaxed = true`)
   - Capturing lambdas with `slot()`
   - Extension functions
   - Coroutine support (`coEvery`, `coVerify`)

## Development

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
