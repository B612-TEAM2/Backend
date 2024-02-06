package com.b6122.ping.init;

import com.b6122.ping.domain.User;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.parser.Entity;

@Component
@Transactional
@RequiredArgsConstructor
public class InitService {

    private final EntityManager em;

    @Value("${profile.image.upload-path}")
    private String profileImagePath;

    public void dbInit() {
        User user1 = User.createUser();
        user1.updateDummyProfile("nickname1",profileImagePath + "\\" + "dummy1.png");
        em.persist(user1);

        User user2 = User.createUser();
        user2.updateDummyProfile("nickname2",profileImagePath + "\\" + "dummy2.png");
        em.persist(user2);

        User user3 = User.createUser();
        user3.updateDummyProfile("nickname3",profileImagePath + "\\" + "dummy3.png");
        em.persist(user3);
    }
}
