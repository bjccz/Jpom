/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Code Technology Studio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.jpom.service.system;

import cn.hutool.core.util.ReflectUtil;
import io.jpom.model.BaseJsonModel;
import io.jpom.model.data.SystemParametersModel;
import io.jpom.service.h2db.BaseDbService;
import org.springframework.stereotype.Service;

/**
 * @author bwcx_jzy
 * @since 2021/12/2
 */
@Service
public class SystemParametersServer extends BaseDbService<SystemParametersModel> {


	/**
	 * 先尝试更新，更新失败尝试插入
	 *
	 * @param name      参数名称
	 * @param jsonModel 参数值
	 * @param desc      描述
	 */
	public void upsert(String name, BaseJsonModel jsonModel, String desc) {
		SystemParametersModel systemParametersModel = new SystemParametersModel();
		systemParametersModel.setId(name);
		systemParametersModel.setValue(jsonModel.toString());
		systemParametersModel.setDescription(desc);
		super.upsert(systemParametersModel);
	}

	/**
	 * 查询 系统参数 值
	 *
	 * @param name 参数名称
	 * @param cls  类
	 * @param <T>  泛型
	 * @return data
	 */
	public <T> T getConfig(String name, Class<T> cls) {
		SystemParametersModel parametersModel = super.getByKey(name);
		if (parametersModel == null) {
			return null;
		}
		return parametersModel.jsonToBean(cls);
	}

	/**
	 * 查询系统参数值,没有数据创建一个空对象
	 *
	 * @param name 参数名称
	 * @param cls  类
	 * @param <T>  泛型
	 * @return data
	 */
	public <T> T getConfigDefNewInstance(String name, Class<T> cls) {
		T config = this.getConfig(name, cls);
		return config == null ? ReflectUtil.newInstance(cls) : config;
	}
}
