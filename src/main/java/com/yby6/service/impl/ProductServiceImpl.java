// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.service.impl;

import com.yby6.entity.Product;
import com.yby6.mapper.ProductMapper;
import com.yby6.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

}
