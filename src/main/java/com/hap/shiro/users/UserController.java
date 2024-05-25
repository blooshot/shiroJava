package com.hap.shiro.users;

import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {
    private final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserServiceImp userService;

    @PostMapping("/adduser")
    public UserEntity addUser(@RequestBody UserEntity userRequestJson){
        return userService.saveUser(userRequestJson);
    }

    @PostMapping("/adduserbulk")
    public Iterable<UserEntity> addUser(@RequestBody List<UserEntity> userRequestJson){
        log.error(Thread.currentThread().getName());
        return userService.saveUserBulk(userRequestJson);
    }

    @GetMapping("/getallusers")
    public List<UserEntity> getAllUsers(){
        return userService.fetchAllUser();
    }

    @PutMapping("/updateuser")
    public UserEntity updateUser(@RequestBody UserEntity user){
        return userService.updateUser(user, user.getUserId());
    }

    @PutMapping("/bulkupdateuser")
    public List<UserEntity> updateUser(@RequestBody List<UserEntity> users){
        List<Long> usersIdList = new ArrayList<>();
        for(UserEntity user : users){
            usersIdList.add(user.getUserId());
        }
       return userService.bulkUpdateUser(users,usersIdList);
    }

    @DeleteMapping("/deleteuser/{id}")
    public String deleteUser(@PathVariable("id") Long userID){
        try {
            userService.deleteUserPermanenty(userID);
        }catch (Exception e){
            return "Errror occured";
        }
        return "User Deleted Successfully";
    }

}
