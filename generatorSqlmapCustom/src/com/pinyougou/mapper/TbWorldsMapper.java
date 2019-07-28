package com.pinyougou.mapper;

import com.pinyougou.pojo.TbWorlds;
import com.pinyougou.pojo.TbWorldsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TbWorldsMapper {
    int countByExample(TbWorldsExample example);

    int deleteByExample(TbWorldsExample example);

    int deleteByPrimaryKey(Long wordId);

    int insert(TbWorlds record);

    int insertSelective(TbWorlds record);

    List<TbWorlds> selectByExample(TbWorldsExample example);

    TbWorlds selectByPrimaryKey(Long wordId);

    int updateByExampleSelective(@Param("record") TbWorlds record, @Param("example") TbWorldsExample example);

    int updateByExample(@Param("record") TbWorlds record, @Param("example") TbWorldsExample example);

    int updateByPrimaryKeySelective(TbWorlds record);

    int updateByPrimaryKey(TbWorlds record);
}