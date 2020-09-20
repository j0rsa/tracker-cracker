package com.j0rsa.cracker.tracker.service

import com.j0rsa.cracker.tracker.Config
import com.j0rsa.common.RedisCacheService

object CacheService : RedisCacheService(Config.app.redis.host)