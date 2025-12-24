package com.example.chatbot.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.chatbot.dto.request.AuthenticationRequest;
import com.example.chatbot.dto.request.IntrospectRequest;
import com.example.chatbot.dto.response.AuthenticationResponse;
import com.example.chatbot.dto.response.IntrospectResponse;
import com.example.chatbot.entity.InvalidatedToken;
import com.example.chatbot.entity.User;
import com.example.chatbot.enums.ErrorCode;
import com.example.chatbot.exception.AppException;
import com.example.chatbot.repository.InvalidatedTokenRepository;
import com.example.chatbot.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthenticationService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    private String SIGN_KEY;

    public AuthenticationResponse authenticated(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean authencated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authencated) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }
        String token = generatedToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .Authencationed(authencated)
                .build();
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifySignedJWT(token);
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();

    }

    public void logout(String token) throws JOSEException, ParseException {
        var signToken = verifySignedJWT(token);
        var jwtID = signToken.getJWTClaimsSet().getJWTID();
        var expriryDate = signToken.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jwtID)
                .expiryTime(expriryDate)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
    }

    public SignedJWT verifySignedJWT(String token) throws JOSEException, ParseException {
        JWSVerifier jwsVerifier = new MACVerifier(SIGN_KEY);
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expriryDate = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(jwsVerifier);
        if (!verified || expriryDate.before(new Date())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    private String generatedToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())  // ✅ Subject = userId
                .issuer("chatbot-app")             // ✅ Issuer = tên app
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli()))
                .jwtID(java.util.UUID.randomUUID().toString())  // ✅ JWT ID unique cho mỗi token
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            jwsObject.sign(new MACSigner(SIGN_KEY));
        } catch (Exception e) {
            log.debug("Không thể tạo token với lỗi = " + e.getMessage());
            throw new RuntimeException(e);
        }
        return jwsObject.serialize();
    }
}
