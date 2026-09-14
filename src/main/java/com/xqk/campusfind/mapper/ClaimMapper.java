package com.xqk.campusfind.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xqk.campusfind.entity.Claim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ClaimMapper extends BaseMapper<Claim> {

    /**
     * 高级多表联查：获取所有的认领申请，并自动补全物品名称、认领人用户名
     */
    List<Claim> selectClaimWithDetails();

    /**
     * 高级多表联查：获取某位同学自己的认领历史记录，并自动补全物品名称
     */
    List<Claim> selectMyClaimsWithDetails(@Param("userId") Long userId);
}