package com.noteder.be.controller;

import com.noteder.be.dto.UserDto;
import com.noteder.be.entity.User;
import com.noteder.be.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Operations related to user management")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Operation(
            summary = "Get all users with pagination",
            description = "Retrieve a paginated list of all registered users.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful retrieval of user list",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
                    )
            }
    )
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @Parameter(description = "Pagination information") Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }
}
