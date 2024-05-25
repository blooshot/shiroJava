package com.hap.shiro.users;

import com.hap.shiro.locking.DistributedLocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service
public class UserServiceImp implements UserService, EnvironmentAware {

    private final Logger log = LoggerFactory.getLogger(UserServiceImp.class);

    private static long leaseTime;
    private static long waitTime;


    @Autowired
    UserRepository userRepository;

    @Inject
    DistributedLocker locker;

    @Override
    public UserEntity saveUser(UserEntity user) {
        log.error("Saving Data i Db: ",Thread.activeCount());
        return userRepository.save(user);
    }

    public Iterable<UserEntity> saveUserBulk(List<UserEntity> user) {
//        System.out.println(Thread.currentThread().getName());
//        System.out.println(Thread.activeCount());
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        return userRepository.saveAll(user);
    }

    @Override
    public List<UserEntity> fetchAllUser() {
        log.error("Fetching Data from Database");
        return (List<UserEntity>)userRepository.findAll();
    }

    @Override
    public UserEntity updateUser(UserEntity updateUserData, Long userId) {

        String key = "UPDATE_USER_WITH_ID__"+ String.valueOf(userId);

        boolean isLockedAquired = false;

        try {
            isLockedAquired = locker.acquireLock(key,leaseTime,waitTime);
            System.out.println("lockStatus: "+isLockedAquired);
            if (!isLockedAquired){
                log.error("lock not acquired for: "+key);
            }else {
                log.error("lock aquired on key: "+key);
            }

        }catch (Exception e){

        }
        UserEntity userDataDB = userRepository.findById(userId).get();
        if(!"".equalsIgnoreCase(updateUserData.getUserName()) &&
                !"".equalsIgnoreCase(updateUserData.getUseremail())){
            userDataDB.setUserName(updateUserData.getUserName());
            userDataDB.setUseremail(updateUserData.getUseremail());
        }
        if (Objects.nonNull(updateUserData.getUserphone())) userDataDB.setUserphone(updateUserData.getUserphone());
        return userDataDB;
//        return userRepository.save(userDataDB);
    }



    public List<UserEntity> bulkUpdateUser(List<UserEntity> updateUserData, List<Long> userId) {
        List<UserEntity> userDataDB = new ArrayList<>();

        for (UserEntity user : updateUserData){
            if(checkLock(user.getUserId())){
                System.out.println("UserID locked for 10 sec: "+user.getUserId());
            }else {
                userDataDB.add(userRepository.findById(user.getUserId()).get());
            }
        }



        /*if(!"".equalsIgnoreCase(updateUserData.getUserName()) &&
                !"".equalsIgnoreCase(updateUserData.getUseremail())){
            userDataDB.setUserName(updateUserData.getUserName());
            userDataDB.setUseremail(updateUserData.getUseremail());
        }
        if (Objects.nonNull(updateUserData.getUserphone())) userDataDB.setUserphone(updateUserData.getUserphone());*/
        return userDataDB;
//        return userRepository.save(userDataDB);
    }

    private void relelokc(Long userID){

        String key = "UPDATE_USER_WITH_ID__"+ String.valueOf(userID);
        boolean isLockReleased = locker.releaseLock(key);

        if(isLockReleased){
            log.error("lockRemovedKey: "+key);
        }else {
            log.error("LockNotRemovedKey: "+key);
        }

    }

    private Boolean checkLock(Long userIdList){
        Map<Long,Boolean> userIDLockMap = new HashMap<>();

//        for(long id : userIdList){
           String key = "UPDATE_USER_WITH_ID__"+ String.valueOf(userIdList);
           boolean isLockedAquired = false;

           if(!key.equalsIgnoreCase("UPDATE_USER_WITH_ID__162")){
               try {
                   isLockedAquired = locker.acquireLock(key,leaseTime,waitTime);
                   System.out.println("lockStatus: "+isLockedAquired);
                   if (!isLockedAquired){
                       log.error("lock not acquired for: "+key);
                   }else {
                       log.error("lock aquired on key: "+key);
                   }

               }catch (Exception e){

               }
           }


//       }
       return isLockedAquired;
    }

    @Override
    public void deleteUserPermanenty(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public void setEnvironment(Environment env) {
        leaseTime = Long.parseLong(env.getProperty("app.REDIS_LOCK_LEASE_TIME"));
        waitTime = Long.parseLong(env.getProperty("app.REDIS_LOCK_WAIT_TIME"));
    }
}
