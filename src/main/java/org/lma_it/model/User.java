package org.lma_it.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String name;

    @Column(name = "user_second_name", nullable = false)
    private String secondName;

    @Column(name = "user_last_name")
    private String lastName;

    @Column(name = "user_birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "user_gender", nullable = false)
    private String gender;

    @Lob
    @Column(name = "user_image", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] profileImage;

    public User(){}

}
