package com.urbaniak.evacuation_support_system.security;

import com.urbaniak.evacuation_support_system.models.user.UserEvacuationStatus;
import com.urbaniak.evacuation_support_system.models.user.Role;
import com.urbaniak.evacuation_support_system.models.user.UserModel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserPrincipal implements UserDetails {
    private final UserModel user;

    public UserPrincipal(UserModel user) {
        this.user = user;
    }

    @Override
    public String getUsername(){
        return user.getUuid().toString();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword(){
        return "";
    }

    public UUID getUuid(){
        return user.getUuid();
    }

    public Role getRole(){return user.getRole();}

    public UserEvacuationStatus getEvacuationStatus(){return user.getUserEvacuationStatus();}

    public String getRoomId(){ return user.getRoomId();}


}
