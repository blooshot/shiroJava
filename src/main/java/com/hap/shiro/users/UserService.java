package com.hap.shiro.users;

import java.util.List;

public interface UserService {

    UserEntity saveUser(UserEntity user);

    List<UserEntity> fetchAllUser();

    UserEntity updateUser(UserEntity user, Long userId);

    void deleteUserPermanenty(Long userId);


}

