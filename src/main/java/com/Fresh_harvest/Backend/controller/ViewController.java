package com.Fresh_harvest.Backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin_dashboard";
    }
    @GetMapping("/seller/dashboard")
    public String sellerDashboard() {
        return "seller_dashboard";
    }
    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "customer_dashboard";
    }
    @GetMapping("/admin/products")
    public String adminProductManagement() {
        return "admin_product_management";
    }

}
