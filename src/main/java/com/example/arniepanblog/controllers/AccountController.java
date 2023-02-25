package com.example.arniepanblog.controllers;

import com.example.arniepanblog.config.SeedData;
import com.example.arniepanblog.models.Account;
import com.example.arniepanblog.models.Post;
import com.example.arniepanblog.services.AccountService;
import com.example.arniepanblog.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AccountController {
    @Autowired
    public PostService postService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/users/{id}")
    public String getUserPost(Model model) {
        //Find the account by email
        Optional<Account> optionalAccount = accountService.findByEmail(SeedData.getCurrentUserEmail());

        //If the account exists, then shove it into model
        if (optionalAccount.isPresent()) {
            List<Post> userPosts = new ArrayList<>(optionalAccount.get().getPosts());
            model.addAttribute("posts", userPosts);
            return "users";
        } else {
            return "404";
        }
    }

    @GetMapping("/reset-password")
    public String getResetPasswordPage(Model model) {
        Account account = new Account();
        model.addAttribute("account", account);
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@ModelAttribute Account account, @RequestParam("newPassword") String confirmPassword) {
        Optional<Account> accountOptional = accountService.findByEmail(account.getEmail());
        if (accountOptional.isPresent()) {
            if (account.getPassword().equals(confirmPassword)) {
                Account existingAccount = accountOptional.get();
                existingAccount.setPassword(account.getPassword());
                accountService.save(existingAccount);
                return "redirect:/login";
            } else {
                return "password_mismatch";
            }
        }
        return "invalid_email";
    }

}