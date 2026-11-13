package com.urbaniak.evacuation_support_system.services;

import com.urbaniak.evacuation_support_system.models.user.UserModel;
import com.urbaniak.evacuation_support_system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void updateFcmToken(UUID uuid, String token){
        UserModel user = userRepository.findByUuid(uuid)
//                .orElseThrow(() -> new UserNotFoundException(email))// TODO: exception do dorobienia
                  .orElseThrow(() -> new IllegalArgumentException("brak użytkownika o uuid: " + uuid)
                ); // TODO: exception do dorobienia
        user.setFcmToken(token);
        userRepository.save(user);
    }

}
