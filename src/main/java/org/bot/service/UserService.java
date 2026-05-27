package org.bot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.bot.model.domain.User;

public interface UserService extends IService<User> {

    User queryOrCreateByNickname(String nickname);
}
