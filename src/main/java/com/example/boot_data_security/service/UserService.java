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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username);
    }

    public User findById(Long id){
        return userRepo.findById(id).get();
    }

    public void setRecord(int count) {
       // org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User)
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepo.findByUsername(user.getUsername());
        if (count > currentUser.getRecord()) {
            userRepo.setRecord(count, currentUser.getUsername());
        }
    }

    public List<User> allUsers(){
        return userRepo.findAll();
    }

    public void save(User user){
        userRepo.save(user);
    }

    public User findByUsername(String username){
        return userRepo.findByUsername(username);
    }

    public Role[] getRoles(){
        return Role.values();
    }

    public void update(User updatedUser, Long id, Map<String, String> form){
        User userToBeUpdated = userRepo.findById(id).get();
        userToBeUpdated.setPassword(updatedUser.getPassword());
        userToBeUpdated.setUsername(updatedUser.getUsername());
      Set<String> roles = Arrays.stream(Role.values()).map(Role::name).collect(Collectors.toSet());
      userToBeUpdated.getRoles().clear();
      for(String key:form.keySet()){
          if(roles.contains(key)){
              userToBeUpdated.getRoles().add(Role.valueOf(key));
          }
      }
        userRepo.save(userToBeUpdated);
    }
}