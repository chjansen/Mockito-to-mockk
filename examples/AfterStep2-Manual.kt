// After Step 2: Manual method call conversion (final result)
import io.mockk.MockK
import io.mockk.InjectMockKs
import io.mockk.every
import io.mockk.verify
import io.mockk.any
import io.mockk.eq
import org.junit.jupiter.api.Test

class UserServiceTest {
    @MockK
    private lateinit var userRepository: UserRepository
    
    @MockK
    private lateinit var emailService: EmailService
    
    @InjectMockKs
    private lateinit var userService: UserService
    
    @Test
    fun `test user creation`() {
        val user = User("john@example.com")
        
        // Converted to MockK DSL syntax
        every { userRepository.save(any()) } returns user
        every { emailService.sendWelcomeEmail(eq("john@example.com")) } returns true
        
        userService.createUser("john@example.com")
        
        verify { userRepository.save(any()) }
        verify { emailService.sendWelcomeEmail(eq("john@example.com")) }
    }
}
