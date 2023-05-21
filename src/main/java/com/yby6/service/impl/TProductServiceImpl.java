package com.yby6.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yby6.mapper.TProductMapper;
import com.yby6.domain.TProduct;
import com.yby6.service.TProductService;

@Service
public class TProductServiceImpl extends ServiceImpl<TProductMapper, TProduct> implements TProductService {

}

