package com.b6122.ping.domain;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

//JPA Entity 클래스가 TimeEntity를 상속할 경우 이 클래스의 변수를 Column으로 인식
@Getter@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) //시간 값을 자동으로 할당
public class TimeEntity {

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime modifiedDate;
}
