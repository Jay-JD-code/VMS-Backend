package com.vms.controller;




import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vms.controller.dto.ChangePasswordRequest;
import com.vms.dto.ForgotPasswordRequest;
import com.vms.dto.LoginRequest;
import com.vms.dto.LoginResponse;
import com.vms.dto.RefreshTokenRequest;
import com.vms.dto.RegisterRequest;
import com.vms.dto.RegisterResponse;
import com.vms.dto.ResetPasswordRequest;
import com.vms.dto.TokenResponse;
import com.vms.dto.VerifyOtpRequest;
import com.vms.entity.UserEntity;
import com.vms.repository.UserRepository;
import com.vms.service.AuthService;
import com.vms.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

	private final AuthService service;
	
	@PostMapping("/register")
	public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
	    service.register(request);
	    return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse("User registered successfully"));
	}

	/** Staff self-registration — PROCUREMENT or FINANCE only. */
	@PostMapping("/register-staff")
	public ResponseEntity<RegisterResponse> registerStaff(@RequestBody RegisterRequest request) {
	    service.registerStaff(request);
	    return ResponseEntity.status(HttpStatus.CREATED)
	            .body(new RegisterResponse("Account created. Check your email for login credentials."));
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
	    return ResponseEntity.ok(service.login(request));
	}
	
	@PostMapping("/logout")
	public ResponseEntity<String> logout() {

	    return ResponseEntity.ok("Logged out successfully");
	}
	
	@PostMapping("/refresh")
	public ResponseEntity<?> refreshToken(
	        @RequestBody RefreshTokenRequest request) {

	    String refreshToken = request.getRefreshToken();

	    if (!jwtUtil.validateToken(refreshToken)) {
	        return ResponseEntity
	                .status(HttpStatus.UNAUTHORIZED)
	                .body("Invalid refresh token");
	    }

	    String username = jwtUtil.extractUsername(refreshToken);
	    String role = jwtUtil.extractRole(refreshToken);

	    String accessToken = jwtUtil.generateAccessToken(username, role);

	    return ResponseEntity.ok(new TokenResponse(accessToken));
	}
	
	
			@PostMapping("/forgot-password")
			public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {

			    service.forgotPassword(request);

			    return ResponseEntity.ok("OTP sent to email");
			}
			
			@PostMapping("/verify-otp")
			public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {

			    String resetToken = service.verifyOtp(request);

			    return ResponseEntity.ok(Map.of("resetToken", resetToken));
			}
			
					@PostMapping("/reset-password")
					public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {

					    service.resetPassword(request);

					    return ResponseEntity.ok("Password updated successfully");
					}
					
					@PostMapping("/change-password")
					public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req) {
					    service.changePassword(req);
					    return ResponseEntity.ok("Password updated");
					}
			
	
					@PostMapping("/create-vendor-user")
					public ResponseEntity<?> createVendorUser(@RequestBody Map<String, String> req) {

					    service.createVendorUser(req.get("email"));

					    return ResponseEntity.ok().build();
					}
}
