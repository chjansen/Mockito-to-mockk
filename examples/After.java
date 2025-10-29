// After transformation - Using MockK
import io.mockk.MockK;
import io.mockk.InjectMockKs;

public class UserServiceTest {
    @MockK
    private UserRepository userRepository;
    
    @MockK
    private EmailService emailService;
    
    @InjectMockKs
    private UserService userService;
    
    // Test methods would go here
}
