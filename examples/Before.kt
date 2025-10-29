// Before transformation - Kotlin test using Mockito
import org.mockito.Mock
import org.mockito.InjectMocks
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.junit.jupiter.api.Test

class UserServiceTest {
    @Mock
    private lateinit var userRepository: UserRepository
    
    @Mock
    private lateinit var emailService: EmailService
    
    @InjectMocks
    private lateinit var userService: UserService
    
    @Test
    fun `test user creation`() {
        val user = User("john@example.com")
        
        `when`(userRepository.save(any())).thenReturn(user)
        `when`(emailService.sendWelcomeEmail(eq("john@example.com"))).thenReturn(true)
        
        userService.createUser("john@example.com")
        
        verify(userRepository).save(any())
        verify(emailService).sendWelcomeEmail(eq("john@example.com"))
    }
}
