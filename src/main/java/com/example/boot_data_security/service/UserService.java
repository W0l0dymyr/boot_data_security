package com.example.boot_data_security.service;

import com.example.boot_data_security.entities.City;
import com.example.boot_data_security.entities.Role;
import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.repos.UserRepo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private static final Logger LOGGER = LogManager.getLogger(UserService.class);
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SmtpMailSender smtpMailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user.getActivationCode() != null) {
            LOGGER.debug("User is not activated");
            throw new UsernameNotFoundException("Користувач не активований");
        } else {
            LOGGER.info("Loading user called " + username);
            return user;
        }
    }

    public void makeUsedCity(User user, City city){
        user.getUsedCities().add(city);
    }
    public User findById(Long id) {
        LOGGER.info("Finding the user by id");
        return userRepo.findById(id).get();
    }

    public void setRecord(int count) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepo.findByUsername(user.getUsername());
        if (count > currentUser.getRecord()) {
            LOGGER.info("Setting the new record");
            userRepo.setRecord(count, currentUser.getUsername());
        }
    }

    public List<User> allUsers() {
        return userRepo.findAll();
    }

    private void sendMessage(User user, String text, String link) {
        LOGGER.info("Sending message to the user's email for registration");
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format("Привіт, %s \n" + text + ", будь ласка перейди за наступним посиланням: " +
                    "http://localhost:9090/" + link, user.getUsername(), user.getActivationCode());
            smtpMailSender.send(user.getEmail(), "Код активації", message);
        }
    }

    private void sendMessage(User user, String text, String link, String newEmail) {
        LOGGER.info("Sending message to the user's email for updating email");
        String message = String.format("Привіт, %s \n" + text + ", будь ласка перейди за наступним посиланням: " +
                "http://localhost:9090/" + link + newEmail, user.getUsername(), user.getActivationCode());
        smtpMailSender.send(newEmail, "Код активації", message);
        //     }
    }

    public boolean userExists(User user) {
        LOGGER.info("Checking if a user exists");
        User userFromDbByName = userRepo.findByUsername(user.getUsername());
        User userFromDbEmail = userRepo.findByEmail(user.getEmail());
        return userFromDbByName != null && userFromDbEmail != null;
    }

    public boolean addUser(User user) {
        LOGGER.info("Adding a new user");
        User userFromDbByName = userRepo.findByUsername(user.getUsername());
        User userFromDbByEmail = userRepo.findByEmail(user.getEmail());
        if(userFromDbByName!=null||userFromDbByEmail!=null){
            return false;
        }
        user.setActive(false);    // якщо користувача з таки іменем немає, то сетимо йому поля і зберігаємо
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        sendMessage(user, "Щоб підтвердити реєстрацію у грі \"Міста\"", "activate_for_registration/%s");
        return true;
    }

    public void save(User user) {
        LOGGER.info("User is saved");
        user.setActivationCode(null);
        userRepo.save(user);
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public Role[] getRoles() {
        return Role.values();
    }


    public boolean activateUser(String code) {
        LOGGER.info("User activation");
        User user = userRepo.findByActivationCode(code);
        if (user == null) {
            LOGGER.debug("User with activation code " + code + " not found");
            return false;
        }

        user.setActive(true);
        user.setActivationCode(null);  //це закоментовано, бо в UserContoller в методі activate є ці методи
        userRepo.save(user);   //  і не відомо чи якшо вони будуть тут а не там, то чи буде все добре працювати
        return true;
    }

    public void update(User updatedUser, Long id, Map<String, String> form) {
        LOGGER.info("Updating user data");
        User userToBeUpdated = userRepo.findById(id).get();

        userToBeUpdated.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        userToBeUpdated.setUsername(updatedUser.getUsername());
        Set<String> roles = Arrays.stream(Role.values()).map(Role::name).collect(Collectors.toSet());
        userToBeUpdated.getRoles().clear();
        for (String key : form.keySet()) {
            if (roles.contains(key)) {
                userToBeUpdated.getRoles().add(Role.valueOf(key));
            }
        }
        userRepo.save(userToBeUpdated);
    }

    public User findByActivationCode(String code) {
        return userRepo.findByActivationCode(code);
    }

    public boolean isEmailChanged(User user, String newEmail) {
        LOGGER.info("Checking if is email changed");
        return !newEmail.isEmpty() && !newEmail.equals(user.getEmail());
    }

    public boolean isUsernameChanged(User user, String newUsername) {
        LOGGER.info("Checking if is username changed");
        return !newUsername.isEmpty() && !newUsername.equals(user.getUsername());
    }

    public void updateUsername(User user, String username) {
        LOGGER.info("Updating username");
        user.setUsername(username);
        userRepo.save(user);
    }

    public void updateEmail(User user, String email) {
        LOGGER.info("Updating email");
        user.setActive(false);
        user.setActivationCode(UUID.randomUUID().toString());
        sendMessage(user, "Щоб підтвердити зміну електронної пошти,", "user/activate_for_update/%s?email=", email);
        userRepo.save(user);
    }

    public boolean isPasswordChanged(User user, String password) {
        LOGGER.info("Checking if is password changed");
        return !password.isEmpty() && !password.equals(user.getPassword());
    }

    public void updatePassword(User user, String password) {
        LOGGER.info("Updating password");
        user.setPassword(passwordEncoder.encode(password));
        userRepo.save(user);
    }
}