package com.campus;

import com.campus.mapper.ActivityMapper;
import com.campus.mapper.AppUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitCheck implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitCheck.class);

    private final AppUserMapper appUserMapper;
    private final ActivityMapper activityMapper;

    public DataInitCheck(AppUserMapper appUserMapper, ActivityMapper activityMapper) {
        this.appUserMapper = appUserMapper;
        this.activityMapper = activityMapper;
    }

    @Override
    public void run(String... args) {
        long userCount = appUserMapper.selectCount(null);
        long activityCount = activityMapper.selectCount(null);
        log.info("数据库连接验证成功：app_user 表 {} 条记录，activity 表 {} 条记录", userCount, activityCount);
    }
}
