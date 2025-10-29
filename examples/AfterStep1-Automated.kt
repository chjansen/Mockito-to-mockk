// After Step 1: OpenRewrite recipe applied (automated)
import io.mockk.MockK
import io.mockk.InjectMockKs
import io.mockk.MockKKt.every
import io.mockk.MockKKt.verify
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
        
        // TODO: Manual conversion needed below this line
        `when`(userRepository.save(any())).thenReturn(user)
        `when`(emailService.sendWelcomeEmail(eq("john@example.com"))).thenReturn(true)
        
        userService.createUser("john@example.com")
        
        verify(userRepository).save(any())
        verify(emailService).sendWelcomeEmail(eq("john@example.com"))
    }
}
