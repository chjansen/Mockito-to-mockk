// Before transformation - Using Mockito
import org.mockito.Mock;
import org.mockito.InjectMocks;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private UserService userService;
    
    // Test methods would go here
}
