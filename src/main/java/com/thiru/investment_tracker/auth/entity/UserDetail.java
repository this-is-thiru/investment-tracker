package com.thiru.investment_tracker.auth.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.ToString;

@Document(value = "user_details")
@Data
@ToString
public class UserDetail {
    @JsonIgnore
    @MongoId
    private String email;
    private String password;
    private String roles;
}