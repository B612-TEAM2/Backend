package com.b6122.ping.repository;

import com.b6122.ping.domain.Like;
import com.b6122.ping.domain.Post;
import com.b6122.ping.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LikeRepository {

    private final EntityManager em;


    //특정post에 좋아요를 눌렀는지 확인
    public boolean checkMyLike(@Param("pid") long pid, @Param("uid") long uid) {
        TypedQuery<Like> query = em.createQuery("SELECT l FROM Like l WHERE l.pid = :pid AND l.uid = :uid", Like.class);
        query.setParameter("pid", pid);
        query.setParameter("uid", uid);

        List<Like> result = query.getResultList();

        return !result.isEmpty(); // If the result list is not empty, return true; otherwise, return false
    }
}
