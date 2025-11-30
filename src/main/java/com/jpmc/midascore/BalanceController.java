package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
public class BalanceController {

    private final UserRepository userRepository;

    public BalanceController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam long userId) {

        UserRecord user = userRepository.findById(userId).orElse(null);

        // If user does not exist → return Balance(0)
        if (user == null) {
            return new Balance(0);
        }

        // Return balance object (ONLY amount)
        return new Balance(user.getBalance());
    }
}
