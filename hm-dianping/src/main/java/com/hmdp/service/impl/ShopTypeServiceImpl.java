package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.management.Query;
import java.util.List;
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
@Slf4j
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ShopType> queryTypeList() {
        //1.查询redis缓存
        String key = RedisConstants.CACHE_SHOP_TYPE_KEY;
        String shopTypeJson = stringRedisTemplate.opsForValue().get(key);

        if(StrUtil.isNotBlank(shopTypeJson)){
            //返回list对象
            List<ShopType> typeList = JSONUtil.toList(shopTypeJson, ShopType.class);
            return typeList;
        }

        log.info("缓存命中失败，查询数据库");
        List<ShopType> typeList = query().orderByAsc("sort").list();

        //在数据库中未查询到数据
        if (typeList == null && typeList.isEmpty()) {
            //返回空列表
            log.warn("数据库中没有商户类型数据");
            return typeList;
        }

        //将数据库数据添加到缓存中，返回列表数据
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(typeList),7, TimeUnit.DAYS);
        log.info("已将店铺类型存到redis当中");
        return typeList;
    }
}
