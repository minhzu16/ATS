package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.LoginRequest;
import fptu.sba301.ats.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}
