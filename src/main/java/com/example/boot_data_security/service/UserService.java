package com.example.boot_data_security.service;

import com.example.boot_data_security.entities.Role;
import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.repos.UserRepo;
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
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SmtpMailSender smtpMailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(userRepo.findByUsername(username).getActivationCode()!=null) {
            throw new UsernameNotFoundException("Користувач не активований");
        }else {
            return userRepo.findByUsername(username);
        }
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

//    private void sendMessage(User user, String text, String link) {
//        if (!StringUtils.isEmpty(user.getEmail())) {
//            String message = String.format("Привіт, %s \n"+ "Щоб підтвердити реєстрацію у грі \"міста\", будь ласка перейди за наступним посиланням: " + "http://localhost:9090/activate_for_update/%s", user.getUsername(), user.getActivationCode());
//            smtpMailSender.send(user.getEmail(), "Код активації", message);
//     }
//    }

    private void sendMessage(User user, String text, String link) {
        if (!StringUtils.isEmpty(user.getEmail())) {
//            String message = String.format("Привіт, %s \n" + "Щоб підтвердити реєстрацію у грі \"міста\""+", будь ласка перейди за наступним посиланням: " +
//                    "http://localhost:9090/activate_for_registration/%s", user.getUsername(), user.getActivationCode());
            String message = String.format("Привіт, %s \n" + text + ", будь ласка перейди за наступним посиланням: " +
                    "http://localhost:9090/" + link, user.getUsername(), user.getActivationCode());
            smtpMailSender.send(user.getEmail(), "Код активації", message);
        }
    }

    private void sendMessage(User user, String text, String link, String newEmail) {
    //    if (!StringUtils.isEmpty(user.getEmail())) {
//            String message = String.format("Привіт, %s \n" + "Щоб підтвердити реєстрацію у грі \"міста\""+", будь ласка перейди за наступним посиланням: " +
//                    "http://localhost:9090/activate_for_registration/%s", user.getUsername(), user.getActivationCode());
            String message = String.format("Привіт, %s \n" + text + ", будь ласка перейди за наступним посиланням: " +
                    "http://localhost:9090/" + link+newEmail, user.getUsername(), user.getActivationCode());
            smtpMailSender.send(newEmail, "Код активації", message);
   //     }
    }

    public boolean userExists(User user) {
        User userFromDbByName = userRepo.findByUsername(user.getUsername());
        User userFromDbEmail = userRepo.findByEmail(user.getEmail());
        return userFromDbByName != null && userFromDbEmail != null;
    }

    public void addUser(User user) {

        user.setActive(false);    // якщо користувача з таки іменем немає, то сетимо йому поля і зберігаємо
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        sendMessage(user, "Щоб підтвердити реєстрацію у грі \"Міста\"", "activate_for_registration/%s");
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


    public boolean activateUser(String code) {
        User user = userRepo.findByActivationCode(code);
        if (user == null) {
            return false;
        }

        user.setActive(true);
       user.setActivationCode(null);  //це закоментовано, бо в UserContoller в методі activate є ці методи
      userRepo.save(user);   //  і не відомо чи якшо вони будуть тут а не там, то чи буде все добре працювати
        return true;
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

    public User findByActivationCode(String code) {
        return userRepo.findByActivationCode(code);
    }

    public boolean isEmailChanged(User user, String newEmail) {
        return !newEmail.isEmpty() && !newEmail.equals(user.getEmail());
    }

    public boolean isUsernameChanged(User user, String newUsername) {
        return !newUsername.isEmpty() && !newUsername.equals(user.getUsername());
    }

    public void updateUsername(User user, String username) {
        user.setUsername(username);
        userRepo.save(user);
    }

    public void updateEmail(User user, String email) {
       // if (isEmailChanged(user, email)) {
            user.setActive(false);
         //   user.setEmail(email);
            //if (!StringUtils.isEmpty(email)) {
                user.setActivationCode(UUID.randomUUID().toString());
                sendMessage(user, "Щоб підтвердити зміну електронної пошти,", "user/activate_for_update/%s?email=", email);
          //  }
            userRepo.save(user);
      //  }
    }

    public boolean isPasswordChanged(User user, String password) {
        return !password.isEmpty()&&!password.equals(user.getPassword());
    }

    public void updatePassword(User user, String password) {
        user.setPassword(password);
        userRepo.save(user);
    }

//    public void updateProfile(User user, String password, String email) {
//        if (isEmailChanged(user, email)) {
//            user.setEmail(email);
//            if (!StringUtils.isEmpty(email)) {
//                user.setActivationCode(UUID.randomUUID().toString());
//                sendMessage(user, "Щоб підтвердити зміну електронної пошти,", "user/activate_for_update/%s");
//            }
//        }
//        if (!StringUtils.isEmpty(password)) {
//            user.setPassword(password);
//        }
//
//      //  if (isEmailChanged) {
//        //    if (!StringUtils.isEmpty(user.getEmail())) {
//          //      sendMessage(user, "Щоб підтвердити зміну електронної пошти,", "user/activate_for_update/%s");
////                String message = String.format("Привіт, %s \n" + "Щоб підтвердити зміну електронної пошти, будь ласка перейди за наступним посиланням: " +
////                        "http://localhost:9090/activate/%s", user.getUsername(), user.getActivationCode());
////                smtpMailSender.send(user.getEmail(), "Код активації", message);
//         //   }
//      //  }
//        userRepo.save(user);
//
//     //   return isEmailChanged;
//    }
}