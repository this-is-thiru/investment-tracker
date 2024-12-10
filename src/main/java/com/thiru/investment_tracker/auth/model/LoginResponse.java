package com.thiru.investment_tracker.auth.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.util.collection.TCommonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor(staticName = "from")
public class LoginResponse {
	private String access_token;
	private int expires_in;
	private final String tokenType = "Bearer";
}
