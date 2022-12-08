package io.juneqqq.service.common;

import io.juneqqq.dao.entity.UserMoment;

import java.util.List;
import java.util.Set;

public interface UserMomentService {
    void addUserMoments(UserMoment userMoment);
    List<UserMoment> getUserMoments(Long userId);
    Set<UserMoment> getUserSubscribedMoments(Long userId);
}
