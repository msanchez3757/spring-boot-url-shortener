package com.msanchez.url_shortener.web.controllers;

import com.msanchez.url_shortener.ApplicationProperties;
import com.msanchez.url_shortener.domain.entities.ShortURL;
import com.msanchez.url_shortener.domain.entities.User;
import com.msanchez.url_shortener.domain.models.CreateShortUrlCmd;
import com.msanchez.url_shortener.domain.models.PagedResult;
import com.msanchez.url_shortener.domain.models.ShortUrlDTO;
import com.msanchez.url_shortener.domain.services.ShortUrlService;
import com.msanchez.url_shortener.exceptions.ShortUrlNotFoundException;
import com.msanchez.url_shortener.web.controllers.dtos.CreateShortUrlForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final ShortUrlService shortUrlService;
    private final ApplicationProperties applicationProperties;
    private final SecurityUtils securityUtils;

    public HomeController(ShortUrlService shortUrlService, ApplicationProperties applicationProperties,
                          SecurityUtils securityUtils) {
        this.shortUrlService = shortUrlService;
        this.applicationProperties = applicationProperties;
        this.securityUtils = securityUtils;
    }



    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "1") Integer page,
            Model model){
        this.addShortUrlsDataToModel(model,page);
        model.addAttribute("paginationUrl", "/");
        model.addAttribute("createShortUrlForm", new CreateShortUrlForm("", false, null));
        return "index";
    }

    private void addShortUrlsDataToModel(Model model, int pageNo) {
        PagedResult<ShortUrlDTO> shortUrls = shortUrlService.findAllPublicShortUrls(pageNo, applicationProperties.pageSize());
        model.addAttribute("shortUrls", shortUrls);
        model.addAttribute("baseUrl", applicationProperties.baseUrl());
    }

    @PostMapping("/short-urls")
    String createShortUrl(@ModelAttribute("createShortUrlForm") @Valid CreateShortUrlForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model
                                 ){
        if(bindingResult.hasErrors()){
            this.addShortUrlsDataToModel(model,1);
            return "index";
        }
        try{
            Long userId = securityUtils.getCurrentUserId();
            CreateShortUrlCmd cmd = new CreateShortUrlCmd(form.originalUrl(),
                    form.isPrivate(),
                    form.expirationInDays(),
                    userId);
            var shortUrlDTO = shortUrlService.createShortUrl(cmd);

            redirectAttributes.addFlashAttribute("successMessage", "Short URL Created Successfully " +
                    applicationProperties.baseUrl() + "/s/" + shortUrlDTO.shortKey());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load short URL");

        }

        return "redirect:/";
    }

    @GetMapping("/s/{shortKey}")
    String redirectToOriginalUrl(@PathVariable String shortKey){
        Long userId = securityUtils.getCurrentUserId();
        Optional<ShortUrlDTO> shortUrlDTOOptional = shortUrlService.accessShortUrl(shortKey, userId);
        if(shortUrlDTOOptional.isEmpty()){
            throw new ShortUrlNotFoundException("Invalid short key");
        }
        ShortUrlDTO shortUrlDTO = shortUrlDTOOptional.get();
        return "redirect:"+shortUrlDTO.originalUrl();
    }

    @GetMapping("/login")
    String loginForm() {
        return "login";
    }

    @GetMapping("/my-urls")
    public String showUserUrls(
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        var currentUserId = securityUtils.getCurrentUserId();
        PagedResult<ShortUrlDTO> myUrls =
                shortUrlService.getUserShortUrls(currentUserId, page, applicationProperties.pageSize());
        model.addAttribute("shortUrls", myUrls);
        model.addAttribute("baseUrl", applicationProperties.baseUrl());
        model.addAttribute("paginationUrl", "/my-urls");
        model.addAttribute("paginationUrl", "/my-urls");
        return "my-urls";
    }

    @PostMapping("/delete-urls")
    public String deleteUrls(
            @RequestParam(value = "ids", required = false) List<Long> ids,
            RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "No URLs selected for deletion");
            return "redirect:/my-urls";
        }
        try {
            var currentUserId = securityUtils.getCurrentUserId();
            shortUrlService.deleteUserShortUrls(ids, currentUserId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Selected URLs have been deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deleting URLs: " + e.getMessage());
        }
        return "redirect:/my-urls";
    }
}
