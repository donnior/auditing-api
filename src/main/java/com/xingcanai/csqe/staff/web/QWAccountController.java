package com.xingcanai.csqe.staff.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xingcanai.csqe.common.BaseController;
import com.xingcanai.csqe.staff.entity.QWAccount;
import com.xingcanai.csqe.staff.entity.QWAccountRepository;

@RestController
public class QWAccountController implements BaseController {

    @Autowired
    private QWAccountRepository qwAccountRepository;

    // @GetMapping("/qwaccounts")
    // public Page<QWAccount> listQWAccounts(XCPageRequest pageRequest) {
    //     PageRequest req = pageRequest.toPageRequest();
    //     return this.qwAccountRepository.findAll(req.withSort(Sort.by("createTime").descending()));
    // }

    @GetMapping("/qwaccounts")
    public Page<QWAccount> listQWAccounts(@RequestParam(name = "page", required = false, defaultValue = DEFAULT_PAGE) Integer page,
                                          @RequestParam(name = "page_size", required = false, defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize) {
        PageRequest req = PageRequest.of(page-1, pageSize);
        return this.qwAccountRepository.findAll(req.withSort(Sort.by("createTime").descending()));
    }

}
