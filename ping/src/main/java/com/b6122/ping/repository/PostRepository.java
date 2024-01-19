package com.b6122.ping.repository;

import com.b6122.ping.domain.Post;
import com.b6122.ping.domain.User;
import com.b6122.ping.repository.datajpa.PostDataRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor

public class PostRepository{

    private final EntityManager em;


    @Modifying
    @Query("update Post p set p.viewCount = :viewCount where p.id =:id")
    public int updateViewCount(@Param("viewCount") int viewCount, @Param("id") Long id){
        return 0;
    };

    @Modifying
    @Query("update Post p set p.likeCount = :likeCount where p.id =:pid")
    public int updateLikeCount(@Param("likeCount") int likeCount, @Param("id") Long pid){
        return 0;
    };

    @Modifying
    @Query("insert into Like(user_id, post_id) Values(:uid,:pid)")
    public int createLike(@Param("pid") Long pid, @Param("uid") Long uid){
        return 0;
    }

    public  Post findById(long id){
        return em.createQuery("select P from Post p where p.id = :id", Post.class)
                .setParameter("id", id)
                .getSingleResult();
    }

}
