package com.hap.shiro.users;

import com.hap.shiro.JSRunner.JsRunner;
import com.hap.shiro.JSRunner.Multiplier;
import com.hap.shiro.common.util.RhinoSandbox.RhinoSandbox;
import com.hap.shiro.common.util.RhinoSandbox.RhinoSandboxes;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.apache.catalina.User;
//import org.graalvm.polyglot.*;
//import org.graalvm.polyglot.Context;
//import org.graalvm.polyglot.Value;
import org.mozilla.javascript.NativeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api")
public class UserController {
    private final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserServiceImp userService;

    @Autowired
    JsRunner jsRunner;

    @Autowired
    Multiplier multiplier;

    @GetMapping("/runjs")
    public ResponseEntity runjs(){
        String reponse1 = "var be=5+50;";

        /*----------------- GRALLVM  --------------------*/

        /*Context context = Context.newBuilder()
                .allowAllAccess(true)  // Adjust access based on your needs (cautiously!)
                .build();*/
    /*    Context context = Context.create();
        context.create("js");
        Value jsBingings = context.getBindings("js");
//        jsBingings.putMember("be", "java");
        Value result = context.eval("js",reponse1);
        jsBingings.getMember("be");
        System.out.println(result);*/

       /* try (Context context = Context.create()) {
            // Set member 'console' to be the actual console object
            context.put("console", System.out);

            // Evaluate the JavaScript code with console.log
            String jsCode = "console.log('Hello from JavaScript using GraalVM!');";
            context.eval("js", jsCode);
        }*/

        /*------- Rhino  ---------*/

        NativeObject returnedObject = jsRunner.getExecutor(multiplier.stringScript());
        Object cid = returnedObject.get("companyId",returnedObject );
        return new ResponseEntity<>(cid.toString(), HttpStatus.OK);
    }

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
    @RateLimiter(name = "userRateLimiter", fallbackMethod="rateLimitingUser")
    public List<UserEntity> getAllUsers(){
        log.error(Thread.currentThread().getName());
        return userService.fetchAllUser();
    }

    public ResponseEntity<UserEntity> rateLimitingUser(){
        UserEntity user = UserEntity.builder()
                .useremail("LuchBhiHo")
                .userName("sajfdlk@fads.ca")
                .userphone(234567890)
                .build();

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/getalluserrate/{id}")
    public ResponseEntity getAllUsersRate(@PathVariable("id") Long id){
        log.error(Thread.currentThread().getName());
        String response = null;
        try {
            response = userService.fetchUserRate(id);
        }catch (Exception e){
            response = "BewajahAccessMat: "+e.getMessage();
            return new ResponseEntity<>(response,HttpStatus.LOCKED);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
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
