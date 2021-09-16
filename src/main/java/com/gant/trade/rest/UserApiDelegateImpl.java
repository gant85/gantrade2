package com.gant.trade.rest;


import com.gant.trade.mongo.service.UserService;
import com.gant.trade.rest.model.UserCreateRequest;
import com.gant.trade.rest.model.UserCreateResponse;
import com.gant.trade.rest.model.UserListTO;
import com.gant.trade.rest.model.UserTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserApiDelegateImpl implements UserApiDelegate {

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<UserCreateResponse> createUser(UserCreateRequest userCreateRequest) {
        log.info("Starting interaction: createUser");
        UserTO userTO = userService.createUser(userCreateRequest);
        UserCreateResponse userCreateResponse = new UserCreateResponse();
        userCreateResponse.setId(userTO.getSeqId());
        log.info("End interaction: createUser");
        return ResponseEntity.ok(userCreateResponse);
    }

    @Override
    public ResponseEntity<Void> deleteUserById(Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserTO> getUserById(Long id)  {
        log.info("Starting interaction: getUserById");
        UserTO user = userService.getUserById(id);
        log.info("End interaction: getUserById");
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<UserTO> getUserByEmail(String emailAddress)  {
        log.info("Starting interaction: getUserByEmail");
        UserTO user = userService.getUserByEmail(emailAddress);
        log.info("End interaction: getUserByEmail");
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<UserListTO> userList(Integer pageSize, Integer pageIndex) {
        log.info("Starting interaction: userList");
        UserListTO userListTO = userService.userList(pageSize, pageIndex);
        log.info("End interaction: userList");
        return ResponseEntity.ok(userListTO);
    }

    @Override
    public ResponseEntity<UserTO> updateUserById(Long id, UserCreateRequest userCreateRequest) {
        log.info("Starting interaction: updateUser");
        UserTO savedUser = userService.updateUserById(id, userCreateRequest);
        log.info("End interaction: updateUser");
        return ResponseEntity.ok(savedUser);
    }
}
