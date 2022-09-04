package com.example.boot_data_security.service;

import com.example.boot_data_security.entities.Role;
import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SmtpMailSender smtpMailSender;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username);
    }

    public User findById(Long id) {
        return userRepo.findById(id).get();
    }

    public void setRecord(int count) {
        // org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User)
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepo.findByUsername(user.getUsername());
        if (count > currentUser.getRecord()) {
            userRepo.setRecord(count, currentUser.getUsername());
        }
    }

    public List<User> allUsers() {
        return userRepo.findAll();
    }

    public boolean addUser(User user) {
        User userFromDbByName = userRepo.findByUsername(user.getUsername());
        User userFromDbEmail = userRepo.findByEmail(user.getEmail());

        if (userFromDbByName != null||userFromDbEmail !=null) {
            return false;
        }

        user.setActive(true);    // якщо користувача з таки іменем немає, то сетимо йому поля і зберігаємо
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        userRepo.save(user);
        if(!StringUtils.isEmpty(user.getEmail())){
            String message = String.format("Привіт, %s \n"+ "Щоб підтвердити реєстрацію у грі \"міста\", будь ласка перейди за наступним посиланням: " +
                    "http://localhost:9090/activate/%s", user.getUsername(), user.getActivationCode());
            smtpMailSender.send(user.getEmail(), "Код активації", message);
        }
        return true;
    }

    public void save(User user) {
        userRepo.save(user);
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public Role[] getRoles() {
        return Role.values();
    }

    public void update(User updatedUser, Long id, Map<String, String> form) {
        User userToBeUpdated = userRepo.findById(id).get();
        userToBeUpdated.setPassword(updatedUser.getPassword());
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

    public boolean activateUser(String code){
        User user = userRepo.findByActivationCode(code);
        if(user==null){
            return  false;
        }
        user.setActivationCode(null);
        userRepo.save(user);
        return  true;
    }
}