package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY+id;
        //1.redis中查缓存,返回shop对象
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2.查到，返回数据
        if(StrUtil.isNotBlank(shopJson)){
            //将key反序列化为对象
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        // 判断是否缓存了空值
        if (shopJson != null) {
            // 缓存null值，设置过期时间，防止缓存穿透
            stringRedisTemplate.opsForValue().set(key, "null", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.fail("店铺不存在！");
        }

        //3.redis没查到，根据id查询数据库(mybatisplus 自带的)
        Shop shop = getById(id);
        //4.数据库中没查到，存null到缓存中，返回404
        if(shop == null){
            return Result.fail("店铺不存在！");
        }
        //5.数据库查到，添加到缓存,设置过期时间
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //返回店铺信息
     return Result.ok(shop);
    }
}
