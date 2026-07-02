package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.service.impl.SearchService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    @Resource private SearchService searchService;

    /** 搜索联想 */
    @GetMapping("/suggest")
    public R suggest(@RequestParam String keyword) {
        return R.ok(searchService.suggest(keyword));
    }

    /** 搜索商品 */
    @GetMapping
    public R search(@RequestParam String keyword) {
        return R.ok(searchService.search(keyword));
    }

    /** 热门搜索词 */
    @GetMapping("/hot")
    public R hotWords() {
        return R.ok(searchService.hotWords());
    }
}
