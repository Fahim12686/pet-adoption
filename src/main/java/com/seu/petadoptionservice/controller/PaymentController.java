package com.seu.petadoptionservice.controller;

import com.seu.petadoptionservice.dto.PaymentRequest;
import com.seu.petadoptionservice.entity.AdoptionApplication;
import com.seu.petadoptionservice.entity.Payment;
import com.seu.petadoptionservice.entity.Pet;
import com.seu.petadoptionservice.entity.User;
import com.seu.petadoptionservice.repository.AdoptionApplicationRepository;
import com.seu.petadoptionservice.repository.PaymentRepository;
import com.seu.petadoptionservice.repository.UserRepository;
import com.seu.petadoptionservice.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final AdoptionApplicationRepository applicationRepository;
    private final PaymentRepository paymentRepository;
    private final PetService petService;
    private final UserRepository userRepository;

    @GetMapping("/{appId}")
    public String paymentForm(@PathVariable Long appId, Principal principal,
                               RedirectAttributes redirectAttributes, Model model) {
        Optional<AdoptionApplication> appOpt = applicationRepository.findById(appId);
        if (appOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("dashboardError", "That adoption application no longer exists.");
            return "redirect:/dashboard";
        }
        AdoptionApplication app = appOpt.get();

        String deny = validateAccessAndState(app, principal, false);
        if (deny != null) {
            redirectAttributes.addFlashAttribute("dashboardError", deny);
            return "redirect:/dashboard";
        }

        PaymentRequest request = new PaymentRequest();
        request.setApplicationId(app.getId());

        model.addAttribute("application", app);
        model.addAttribute("paymentRequest", request);
        return "payment";
    }

    @PostMapping("/process")
    public String processPayment(@Valid @ModelAttribute PaymentRequest request, BindingResult result,
                                  Principal principal, RedirectAttributes redirectAttributes, Model model) {
        if (request.getApplicationId() == null) {
            redirectAttributes.addFlashAttribute("dashboardError", "We couldn't find that adoption application.");
            return "redirect:/dashboard";
        }

        Optional<AdoptionApplication> appOpt = applicationRepository.findById(request.getApplicationId());
        if (appOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("dashboardError", "That adoption application no longer exists.");
            return "redirect:/dashboard";
        }
        AdoptionApplication app = appOpt.get();

        String deny = validateAccessAndState(app, principal, true);
        if (deny != null) {
            redirectAttributes.addFlashAttribute("dashboardError", deny);
            return "redirect:/dashboard";
        }

        if (result.hasErrors()) {
            model.addAttribute("application", app);
            return "payment";
        }

        // Defensive: validation guarantees 16 digits, but never trust client input
        // for an operation this consequential (also protects against a future caller
        // that skips validation).
        String cardNumber = request.getCardNumber();
        if (cardNumber == null || cardNumber.length() != 16) {
            model.addAttribute("application", app);
            model.addAttribute("error", "Card number must be exactly 16 digits.");
            return "payment";
        }

        // Mock 95% success rate logic
        boolean isSuccess = Math.random() > 0.05;

        Payment payment = Payment.builder()
                .application(app)
                .amount(app.getPet().getAdoptionFee())
                .cardLast4(cardNumber.substring(12))
                .transactionId(UUID.randomUUID().toString())
                .status(isSuccess ? "SUCCESS" : "FAILED")
                .build();

        paymentRepository.save(payment);

        if (isSuccess) {
            app.setStatus("COMPLETED");
            applicationRepository.save(app);

            Pet pet = app.getPet();
            pet.setStatus("ADOPTED");
            petService.savePet(pet);

            return "redirect:/payment/success?tx=" + payment.getTransactionId();
        } else {
            model.addAttribute("error", "Payment declined. Please try again.");
            model.addAttribute("application", app);
            return "payment";
        }
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam("tx") String transactionId, Model model) {
        model.addAttribute("transactionId", transactionId);
        return "payment-success";
    }

    /**
     * Confirms the application belongs to the signed-in user (unless they're staff/admin)
     * and is in a payable state. Returns a user-facing error message if access should be
     * denied, or null if the request may proceed. Centralizing this avoids duplicating the
     * same checks (and same edge-case bugs) across the GET form and POST process endpoints.
     */
    private String validateAccessAndState(AdoptionApplication app, Principal principal, boolean strict) {
        if (principal == null) {
            return "Please log in to continue.";
        }
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "We couldn't verify your account. Please log in again.";
        }

        boolean isStaffOrAdmin = false;
        if (principal instanceof Authentication auth) {
            for (GrantedAuthority ga : auth.getAuthorities()) {
                if (ga.getAuthority().equals("ROLE_ADMIN") || ga.getAuthority().equals("ROLE_STAFF")) {
                    isStaffOrAdmin = true;
                    break;
                }
            }
        }

        if (!isStaffOrAdmin && (app.getUser() == null || !app.getUser().getId().equals(user.getId()))) {
            return "That adoption application doesn't belong to your account.";
        }

        if ("COMPLETED".equals(app.getStatus())) {
            return "This adoption fee has already been paid.";
        }
        if (!"APPROVED".equals(app.getStatus())) {
            return "This application isn't approved for payment yet.";
        }

        return null;
    }
}