package com.technology_application.controller;

import com.technology_application.Result;
import com.technology_application.service.IntangibleAssetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jiaqi Lin
 * @date 2022/02/25 18:45
 **/
@RestController
public class IntangibleAssetsController {
    @Autowired
    IntangibleAssetsService service;

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    private Result SelectApplication() throws Exception {
        String url = "S:\\testPath\\财务资产-建筑物.xlsx";
        return Result.success(service.inputData(url));

    }

}
