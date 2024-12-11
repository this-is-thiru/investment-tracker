package com.thiru.investment_tracker.auth.service;

import com.thiru.investment_tracker.auth.entity.UserDetail;
import com.thiru.investment_tracker.auth.repository.UserDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDetailsRepository userDetailsRepo;

    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                Optional<UserDetail> optionalUserDetails = userDetailsRepo.findByEmail(username);
                return optionalUserDetails.map(UserDetailsImpl::new)
                        .orElseThrow(() -> new UsernameNotFoundException("user not found " + username));
            }
        };
    }
}