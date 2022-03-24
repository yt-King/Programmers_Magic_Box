package com.technology_application.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.technology_application.dao.IntangibleAssetsMapper;
import com.technology_application.model.IntangibleAssets;
import com.technology_application.utils.ReadExcel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Jiaqi Lin
 * @date 2022/02/25 18:41
 *
 **/
@Service
public class IntangibleAssetsService {
    @Autowired
    IntangibleAssetsMapper mapper;

    public int inputData(String url) throws Exception {
        List<List<String>> dataList = ReadExcel.GetList(url);
        for (List<String> i : dataList) {
            IntangibleAssets entity = new IntangibleAssets();
            entity.setId(i.get(0));
            entity.setAssetsName(i.get(1));
            entity.setAssetsType(Double.valueOf(i.get(2)));
            QueryWrapper<IntangibleAssets> wrapper = new QueryWrapper<>();
            wrapper.ge("id", i.get(0));
            if (mapper.selectCount(wrapper) == 0) {
                int num = mapper.insert(entity);
                return num;
            } else {
                throw new Exception("编号错误");
            }
        }
        return 0;
    }

}
