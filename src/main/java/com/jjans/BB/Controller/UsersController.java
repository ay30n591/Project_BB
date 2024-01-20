package com.jjans.BB.Controller;


import com.jjans.BB.Config.Security.JwtTokenProvider;
import com.jjans.BB.Dto.Response;
import com.jjans.BB.Dto.UserInfoDto;
import com.jjans.BB.Dto.UserRequestDto;
import com.jjans.BB.Service.Helper;
import com.jjans.BB.Service.UsersService;
import io.jsonwebtoken.io.IOException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = {"http://localhost:3000", "http://3.37.110.13:3000"}) // CORS 설정
public class UsersController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UsersService usersService;
    private final Response response;

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return usersService.getAllUsers();
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo() {
        UserInfoDto userInfo = usersService.userInfo();
        return new ResponseEntity<>(userInfo, HttpStatus.OK);
    }

    @GetMapping("/info/{email}")
    public ResponseEntity<?> getUserInfoByEmail(@PathVariable String email) {
        UserInfoDto userInfo = usersService.userInfo(email);
        return new ResponseEntity<>(userInfo, HttpStatus.OK);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/updateImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUserImage(@RequestParam("imageFile") MultipartFile imageFile) {
        return usersService.userImageUpdate(imageFile);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Validated UserRequestDto.SignUp signUp, Errors errors) {
            // validation check
        System.out.println(signUp.getEmail());
        System.out.println(signUp.getPassword());
        System.out.println(signUp.getNickName());

        if (errors.hasErrors()) {
            return response.invalidFields(Helper.refineError(errors));
        }
        return usersService.signUp(signUp);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated UserRequestDto.Login login, Errors errors) {
        // validation check
        System.out.println(login.getEmail());
        System.out.println(login.getPassword());

        if (errors.hasErrors()) {
            return response.invalidFields(Helper.refineError(errors));
        }
        return usersService.login(login);
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestBody @Validated UserRequestDto.Reissue reissue, Errors errors) {
        // validation check
        if (errors.hasErrors()) {
            return response.invalidFields(Helper.refineError(errors));
        }
        return usersService.reissue(reissue);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) @Validated UserRequestDto.Logout logout, Errors errors) {
        // validation check
        if (logout == null) {
            // 요청 바디가 누락된 경우
            return response.fail("요청 바디가 누락되었습니다.", HttpStatus.BAD_REQUEST);
        }

        System.out.println(logout.getAccessToken());
        System.out.println(logout.getRefreshToken());

        if (errors.hasErrors()) {
            return response.invalidFields(Helper.refineError(errors));
        }
        return usersService.logout(logout);
    }

    @GetMapping("/authority")
    public ResponseEntity<?> authority() {
        log.info("ADD ROLE_ADMIN");
        return usersService.authority();
    }

    @GetMapping("/userTest")
    public ResponseEntity<?> userTest() {
        log.info("ROLE_USER TEST");
        return response.success();
    }

    @GetMapping("/adminTest")
    public ResponseEntity<?> adminTest() {
        log.info("ROLE_ADMIN TEST");
        return response.success();
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser() {
        // 현재 사용자의 인증 객체를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // 현재 사용자의 정보를 가져옵니다.
            Object principal = authentication.getPrincipal();

            // principal이 UserDetails를 구현한 경우, 사용자 이름을 가져옵니다.
            String currentUserName = (principal instanceof UserDetails)
                    ? ((UserDetails) principal).getUsername()
                    : principal.toString();

            return ResponseEntity.ok(List.of(currentUserName));
        } else {
            // 인증되지 않은 경우 응답합니다.
            return ResponseEntity.status(401).body(null);
        }
    }
}