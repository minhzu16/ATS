package fptu.sba301.ats.security;

import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    /**
     * Resolves the currently authenticated user from SecurityContextHolder.
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        String username = auth.getName();
        return userRepository.findByEmailAndDeletedFalse(username)
                .orElseThrow(() -> new BusinessException("Authenticated user not found", HttpStatus.UNAUTHORIZED));
    }

    /**
     * Resolves user from UserDetails principal.
     */
    public User resolveUser(UserDetails principal) {
        if (principal == null || principal.getUsername() == null) {
            return getCurrentUser();
        }
        return userRepository.findByEmailAndDeletedFalse(principal.getUsername())
                .orElseThrow(() -> new BusinessException("Authenticated user not found", HttpStatus.UNAUTHORIZED));
    }
}
