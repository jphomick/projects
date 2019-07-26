package com.joseph.petfound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

@Controller
public class HomeController {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    IssueRepository issueRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    CloudinaryConfig cloudc;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String processRegistrationPage(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        model.addAttribute("user", user);
        if (result.hasErrors()){
            return "registration";
        } else {
            user.setEnabled(true);
            user.setRoles(Arrays.asList(roleRepository.findByRole("USER")));
            userRepository.save(user);
            model.addAttribute("created",  true);
        }
        return "login";
    }

    @RequestMapping("/")
    public String homePage(Principal principal, Model model) {
        model.addAttribute("issues", issueRepository.findAll());
        User user = ((CustomUserDetails)((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser();
        model.addAttribute("user", user);
        Role role1 = roleRepository.findByRole("ADMIN");
        for (User check : role1.getUsers()) {
            if (check.getId() == user.getId()) {
                return "issue";
            }
        }
        return "issue";
    }

    @RequestMapping("/depot")
    public String depotPage(Principal principal, Model model) {
        model.addAttribute("projects", projectRepository.findAll());
        User user = ((CustomUserDetails)((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser();
        model.addAttribute("user", user);
        return "project";
    }

    @GetMapping("/newProject")
    public String newProject(Principal principal, Model model) {
        model.addAttribute("project", new Project());
        User user = ((CustomUserDetails)((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser();
        model.addAttribute("user", user);
        return "new";
    }

    @PostMapping("/sendProject")
    public String saveProject(@Valid Project project, BindingResult result, Principal principal, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("project", new Project());
            User user = ((CustomUserDetails)((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser();
            model.addAttribute("user", user);
            return "new";
        }
        projectRepository.save(project);
        return "redirect:/depot";
    }

    @GetMapping("/newIssue")
    public String newIssue(Principal principal, Model model) {
        model.addAttribute("issue", new Issue());
        model.addAttribute("projects", projectRepository.findAll());
        User user = ((CustomUserDetails)((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser();
        model.addAttribute("user", user);
        return "add";
    }

    @PostMapping("/sendIssue")
    public String saveIssue(@Valid Issue issue, BindingResult result, Principal principal, Model model) {
        User user = ((CustomUserDetails)((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser();
        issue.setSubmitBy(user.getUsername());
        issue.setStatus("Open");
        if (result.hasErrors()) {
            model.addAttribute("issue", new Issue());
            model.addAttribute("projects", projectRepository.findAll());
            model.addAttribute("user", user);
            return "add";
        }
        issueRepository.save(issue);
        return "redirect:/";
    }

    @RequestMapping("/seeIssues")
    public String seeIssue(Model model) {
        model.addAttribute("issues", issueRepository.findAll());
        model.addAttribute("projects", projectRepository.findAll());
        return "see_issue";
    }

    @RequestMapping("/complete/{id}")
    public String completeTask(@PathVariable("id") long id) {
        Issue issue = issueRepository.findById(id).get();
        issue.setStatus("Closed");
        issueRepository.save(issue);
        return "redirect:/";
    }

    @RequestMapping("/open/{id}")
    public String openTask(@PathVariable("id") long id) {
        Issue issue = issueRepository.findById(id).get();
        issue.setStatus("Open");
        issueRepository.save(issue);
        return "redirect:/";
    }

    @RequestMapping("/delete/{id}")
    public String deleteTask(@PathVariable("id") long id) {
        Issue issue = issueRepository.findById(id).get();
        Project project = issue.getProject();
        Set<Issue> issues = project.getIssues();
        issues.remove(issue);
        project.setIssues(issues);
        projectRepository.save(project);
        issueRepository.deleteById(id);
        return "redirect:/";
    }

    @RequestMapping("/deleteProject/{id}")
    public String deleteProject(@PathVariable("id") long id) {
        projectRepository.deleteById(id);
        return "redirect:/depot";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }
}
