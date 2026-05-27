package org.bot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bot.mapper.UserMapper;
import org.bot.model.domain.User;
import org.bot.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public User queryOrCreateByNickname(String nickname) {
        String normalizedName = nickname.toLowerCase();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getNickname, normalizedName);
        User user = this.getOne(queryWrapper);

        if (user == null) {
            user = new User();
            user.setNickname(normalizedName);
            this.save(user);
        }

        return user;
    }
}
