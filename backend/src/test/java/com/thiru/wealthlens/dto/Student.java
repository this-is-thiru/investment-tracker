package com.thiru.wealthlens.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Student(String name,LocalDate dob,LocalDateTime createdDate) {

}
