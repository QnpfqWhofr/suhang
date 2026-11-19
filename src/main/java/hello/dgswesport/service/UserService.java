package hello.dgswesport.service;

import hello.dgswesport.dto.LoginRequest;
import hello.dgswesport.dto.UserCreateRequest;
import hello.dgswesport.dto.UserResponse;
import hello.dgswesport.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse login(LoginRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getOneUser(Long id);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);
}
