package com.example.arniepanblog.controllers;

import com.example.arniepanblog.config.FileUploadUtil;
import com.example.arniepanblog.config.SeedData;
import com.example.arniepanblog.models.Account;
import com.example.arniepanblog.models.Post;
import com.example.arniepanblog.services.AccountService;
import com.example.arniepanblog.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Controller
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/posts/{id}")
    public String getPost(@PathVariable Long id, Model model) {
        //Find the post by
        Optional<Post> optionalPost = postService.getById(id);

        //If the post exists, then shove it into model
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            if (!getReadPerm(post.getId())) {
                return "forbidden";
            }
            post.setHasEditDeletePermission(editDeletePerm(post.getId()));
            model.addAttribute("post", post);
            model.addAttribute("account", accountService.findByEmail(SeedData.getCurrentUserEmail()).get());
            return "post";
        } else {
            return "404";
        }
    }

    @GetMapping("/posts/new")
    public String createNewPost(Model model) {
        Optional<Account> optionalAccount = accountService.findByEmail(SeedData.getCurrentUserEmail());
        if (optionalAccount.isPresent()) {
            Post post = new Post();
            post.setAccount(optionalAccount.get());
            model.addAttribute("post", post);
            model.addAttribute("account", optionalAccount.get());
            return "post_new";
        } else {
            return "404";
        }
    }

    @PostMapping("posts/new")
    public String saveNewPost(@ModelAttribute Post post, @RequestParam("image") MultipartFile file) throws IOException{
        if (!file.isEmpty()) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            post.setPhotos(fileName);
            Post savedPost = postService.save(post);
            String uploadDir = "post-images/" + savedPost.getId();
            FileUploadUtil.saveFile(uploadDir, fileName, file);
        } else {
            postService.save(post);
        }
        return "redirect:/posts/" + post.getId();
    }

    @GetMapping("/posts/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String getPostForEdit(@PathVariable Long id, Model model) {
        //Find post by id
        Optional<Post> optionalPost = postService.getById(id);
        //If post exist put it in model
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            if (!editDeletePerm(post.getId())) {
                return "forbidden";
            }
            model.addAttribute("post", post);
            model.addAttribute("account", accountService.findByEmail(SeedData.getCurrentUserEmail()).get());
            return "post_edit";
        }
        return "404";
    }

    @PostMapping("posts/{id}")
    @PreAuthorize("isAuthenticated()")
    public String updatePost(@PathVariable Long id, Post post) {
        Optional<Post> optionalPost = postService.getById(id);
        if (optionalPost.isPresent()) {
            Post existingPost = optionalPost.get();
            existingPost.setTitle(post.getTitle());
            existingPost.setBody(post.getBody());
            existingPost.setPublishMode(post.getPublishMode());

            postService.save(existingPost);
        }
        return "redirect:/posts/" + post.getId();
    }

    @GetMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id) {
        Optional<Post> optionalPost = postService.getById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            if (!editDeletePerm(post.getId())) {
                return "forbidden";
            }
            postService.delete(post);
            return "redirect:/";
        }
        return "404";
    }

    public Boolean editDeletePerm(Long id) {
        Post post = new Post();
        Optional<Post> optionalPost = postService.getById(id);
        if (optionalPost.isPresent()) {
            post = optionalPost.get();
        }
        Optional<Account> optionalAccount = accountService.findByEmail(SeedData.getCurrentUserEmail());
        Account activeAccount;
        if (optionalAccount.isPresent()) {
            activeAccount = optionalAccount.get();
        } else {
            return false;
        }
        return activeAccount.getEmail().equals(post.getAccount().getEmail()) || activeAccount.getAuthorities().toString().contains("ROLE_ADMIN");
    }

    public Boolean getReadPerm(Long id) {
        Post post = new Post();
        Optional<Post> optionalPost = postService.getById(id);
        if (optionalPost.isPresent()) {
            post = optionalPost.get();
        }
        Optional<Account> optionalAccount = accountService.findByEmail(SeedData.getCurrentUserEmail());
        Account activeAccount;
        if (optionalAccount.isPresent()) {
            activeAccount = optionalAccount.get();
        } else {
            return false;
        }
        return activeAccount.getEmail().equals(post.getAccount().getEmail()) || activeAccount.getAuthorities().toString().contains("ROLE_ADMIN") || post.getPublishMode().equals("Public");
    }
}
