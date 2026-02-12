package com.project.recon.domain.user.repository;

import com.project.recon.domain.user.entity.ProviderType;
import com.project.recon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(ProviderType provider, Long providerId);

    Optional<User> findByEmail(String email);

    boolean existsByNickName(String nickName);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
