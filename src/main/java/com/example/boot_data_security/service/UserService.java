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

import java.util.List;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username);
    }

    public User findById(int id){
        return userRepo.findById(id);
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
}