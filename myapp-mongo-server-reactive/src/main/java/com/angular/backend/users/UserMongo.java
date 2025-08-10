package com.angular.backend.users;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "users")
public class UserMongo {

    @Id
    public String id;
    public String firstName;
    public String lastName;
    public String username;
    public String age;
    public String email;

}
