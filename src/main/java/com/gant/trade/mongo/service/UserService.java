package com.gant.trade.mongo.service;

import com.gant.trade.domain.User;
import com.gant.trade.domain.mapper.UserMapper;
import com.gant.trade.mongo.repository.UserRepository;
import com.gant.trade.rest.model.*;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    /*
        @Autowired
        private KeycloakConfig keycloakConfig;
    */
    public UserTO createUser(UserCreateRequest userCreateRequest) {
/*
        Keycloak keycloak = getKeycloak();

        keycloak.tokenManager().getAccessToken();
*/
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setUsername(userCreateRequest.getEmail());
        userRepresentation.setFirstName(userCreateRequest.getName());
        userRepresentation.setLastName(userCreateRequest.getSurname());
        userRepresentation.setEmail(userCreateRequest.getEmail());
/*
        RealmResource realmResource = keycloak.realm(keycloakConfig.getRealm());
        UsersResource usersRessource = realmResource.users();

        try(Response response = usersRessource.create(userRepresentation)) {

            if (response.getStatus() == 201) {

                String userId = CreatedResponseUtil.getCreatedId(response);

                log.info("Created userId {}", userId);

                CredentialRepresentation password = new CredentialRepresentation();
                password.setTemporary(false);
                password.setType(CredentialRepresentation.PASSWORD);
                password.setValue(userCreateRequest.getPassword());

                UserResource userResource = usersRessource.get(userId);
                userResource.resetPassword(password);

                // TODO prendersi il ruolo user da keycloak
                //RoleRepresentation realmRoleUser = realmResource.roles().get("user").toRepresentation();
                //userResource.roles().realmLevel().add(Arrays.asList(realmRoleUser));
*/
        User user = userMapper.convert(userCreateRequest);
        // user.setKeycloakId(userId);
        User userSaved = userRepository.save(user);

        return userMapper.convert(userSaved);
/*
            } else if (response.getStatus() == 409) {
                throw new UserAlreadyExistException();
            } else {
                log.error("{}", response.getStatusInfo().getReasonPhrase());
                return null;
            }
        }
 */
    }

    /*
        private Keycloak getKeycloak() {
            return KeycloakBuilder.builder()
                    .serverUrl(keycloakConfig.getAuthServerUrl())
                    .realm(keycloakConfig.getRealm())
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .clientId(keycloakConfig.getResource())
                    .clientSecret(keycloakConfig.getCredentials().getSecret())
                    .resteasyClient(new ResteasyClientBuilder()
                            .connectionPoolSize(10)
                            .build())
                    .build();
        }
    */
    public UserTO getUserById(Long id) {
        User user = userRepository.findBySeqId(id);
        return userMapper.convert(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public void addUserExchange(Long id, ExchangeConfiguration exchangeConfiguration) {
        User user = userRepository.findBySeqId(id);
        if (user != null) {
            user.getExchangeConfiguration().add(exchangeConfiguration);
            userRepository.save(user);
        }
    }

    public void deleteUserExchange(Long id, String exchange) {
        User user = userRepository.findBySeqId(id);
        Exchange currentExchange = Exchange.valueOf(exchange);
        if (user != null) {
            user.getExchangeConfiguration().removeIf(exchangeConfiguration -> exchangeConfiguration.getExchangeTO().getExchange() == currentExchange);
            userRepository.save(user);
        }
    }

    public UserTO updateUserById(Long id, UserCreateRequest userCreateRequest) {
        User user = userRepository.findBySeqId(id);
        if (user != null) {
            User newUser = userMapper.convert(userCreateRequest);
            //TODO vedere se si puo fare un converter che si prende sia userCreateRequest che
            // cosi da evitare di fare i set sotto
            newUser.setSeqId(user.getSeqId());
            newUser.setKeycloakId(user.getKeycloakId());
            newUser.setExchangeConfiguration(user.getExchangeConfiguration());
            newUser.setId(user.getId());
            newUser.setEmail(user.getEmail());
            User userSaved = userRepository.save(newUser);
/*
            Keycloak keycloak = getKeycloak();
            RealmResource realmResource = keycloak.realm(keycloakConfig.getRealm());
            UsersResource usersRessource = realmResource.users();
            UserResource userResource = usersRessource.get(userSaved.getKeycloakId());
            UserRepresentation userRepresentation = new UserRepresentation();
            userRepresentation.setUsername(userCreateRequest.getEmail());
            userRepresentation.setFirstName(userCreateRequest.getName());
            userRepresentation.setLastName(userCreateRequest.getSurname());
            userRepresentation.setEmail(userCreateRequest.getEmail());

            if(userCreateRequest.getPassword() != null) {
                CredentialRepresentation password = new CredentialRepresentation();
                password.setTemporary(false);
                password.setType(CredentialRepresentation.PASSWORD);
                password.setValue(userCreateRequest.getPassword());

                userResource.resetPassword(password);
            }
            userResource.update(userRepresentation);
*/
            return userMapper.convert(userSaved);
        }
        return null;
    }

    public UserListTO userList(Integer pageSize, Integer pageIndex) {
        if (pageIndex == null) {
            pageIndex = 0;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<User> page = userRepository.findAll(pageable);
        Pagination pagination = new Pagination();
        pagination.setPageSize(page.getSize());
        pagination.setIsLastPage(!page.hasNext());
        pagination.setNextPageIndex(page.hasNext() ? String.valueOf(page.getNumber() + 1) : null);
        pagination.setTotalItems(Math.toIntExact(page.getTotalElements()));
        UserListTO userListTO = new UserListTO();
        userListTO.setPagination(pagination);
        userListTO.setStrategies(userMapper.convertList(page.getContent()));
        return userListTO;
    }

    public UserTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return userMapper.convert(user);
    }
}
