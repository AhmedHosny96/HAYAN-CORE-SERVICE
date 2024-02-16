package com.hayaan.auth.repo;

import com.hayaan.auth.object.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email OR u.phoneNumber = :phoneNumber")
    Optional<User> findByUsernameOrEmailOrPhoneNumber(@Param("username") String username, @Param("email") String email, @Param("phoneNumber") String phoneNumber);

}
