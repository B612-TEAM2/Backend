package com.b6122.ping.repository.datajpa;

import com.b6122.ping.domain.Friendship;
import com.b6122.ping.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface UserDataRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
