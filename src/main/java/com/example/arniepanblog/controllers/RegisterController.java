package com.example.arniepanblog.controllers;

import com.example.arniepanblog.models.Account;
import com.example.arniepanblog.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class RegisterController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        Account account = new Account();
        model.addAttribute("account", account);
        return "register";
    }

    @PostMapping("/register")
    public String registerNewUser(@ModelAttribute Account account, @RequestParam("confirmPassword") String confirmPassword) {
        Optional<Account> accountOptional = accountService.findByEmail(account.getEmail());
        if (accountOptional.isPresent()) {
//            return "redirect:/register?error=Email already exists";
            return "email_exist";
        }
        if (!account.getPassword().equals(confirmPassword)) {
            return "password_not_match";
        }
        accountService.save(account);
        return "redirect:/";
    }
}
