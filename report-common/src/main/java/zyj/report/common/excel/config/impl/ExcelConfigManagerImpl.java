package zyj.report.common.excel.config.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import zyj.report.common.excel.config.ConfigConstant;
import zyj.report.common.excel.config.ExcelConfigManager;
import zyj.report.common.excel.config.RuturnConfig;
import zyj.report.common.excel.entity.RuturnPropertyParam;
import zyj.report.common.excel.event.Event;
import zyj.report.common.excel.util.ExcelUtil;
import zyj.report.common.excel.util.ValueWidget;

/**
 * 
 * @author
 *
 */
public class ExcelConfigManagerImpl implements ExcelConfigManager {

	private String configName ;
	private SAXReader saxReader;
	private Document doc;
	private Element root;
	private Map<String,Element> modelMap;
	private Map<String,RuturnConfig> modelConfigMap;
	
	public ExcelConfigManagerImpl(String config) {
		configName = config;
		modelConfigMap = new HashMap<String,RuturnConfig>();
		InputStream in = null;
		saxReader = new SAXReader();
		try {
			in = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(configName);
			if(in == null){
				in = ExcelConfigManagerImpl.class.getClassLoader().getResourceAsStream(configName);
			}
			if(in == null){
				in = ExcelConfigManagerImpl.class.getResourceAsStream(configName);
			}
			if(in == null){
				in = new PathMatchingResourcePatternResolver().getResource("classpath:"+configName).getInputStream();
			}
			doc = saxReader.read(in);
			root = doc.getRootElement();
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(in != null){
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/****
	 * read ImportExcelToModel.xml
	 */
	public ExcelConfigManagerImpl() {
		this("ExcelModeMapping.xml");
		
	}
	
	private void init(){
		modelMap = new HashMap<String,Element>();
		List<Element> list = root.elements();
		Element model = null;
		for (Iterator<Element> it = list.iterator(); it.hasNext();) {
			model = it.next();
			String modelId = model.attributeValue("id");
			if(modelId != null && !"".equals(modelId)){
				modelMap.put(modelId, model);
			}
		}
	}

	/***
	 * get node through class name
	 */
	public Element getModelElement(String modelName) {
		return modelMap.get(modelName);
	}

	public RuturnConfig getModel(String modelName) {
		RuturnConfig config = modelConfigMap.get(modelName);
		if(config == null){
			Element model = this.getModelElement(modelName);
			if (model != null) {
				config = new RuturnConfig();
				config.setClassName(model
						.attributeValue(ConfigConstant.MODEL_CLASS));
				config.setMapModel(model
						.attributeValue(ConfigConstant.MODEL_MAP_MODEL));
				config.setPropertyMap(this.getPropertyMap(model,config));
				String event = model.attributeValue(ConfigConstant.MODEL_EVENT_PROPERTY);
				try {
					if(event != null && !"".equals(event)){
						config.setEvent((Event)Class.forName(event).newInstance());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				String enable = model.attributeValue(ConfigConstant.MODEL_ENABLE_PROPERTY);
				Set<Integer> enableSet = new HashSet<Integer>();
				if(enable != null && !"".equals(enable)){
					if(enable.startsWith("column:")){
						enable = enable.substring(7);
						for(String str : enable.split(",")){
							enableSet.add(Integer.parseInt(str)-1);
						}
					}else{
						for(String str : enable.split(",")){
							for(Entry<String, RuturnPropertyParam> entry : ((Map<String, RuturnPropertyParam>)config.getPropertyMap()).entrySet()){
								RuturnPropertyParam property = entry.getValue();
								if(str.equals(property.getName())){
									enableSet.add(Integer.parseInt(property.getColumn())-1);
									break;
								}
							}
						}
					}
				}
				config.setEnableColumn(enableSet);
				modelConfigMap.put(modelName, config);
			}
		}
		return config;
	}
	
	public boolean setConvertMap(String modelName,int column,Map<String,String> m){
		boolean result = false;
		RuturnConfig config = getModel(modelName);
		if(config != null){
			Map<String, RuturnPropertyParam> propertyMap = config.getPropertyMap();
			for(Entry<String, RuturnPropertyParam> entry : propertyMap.entrySet()){
				RuturnPropertyParam property = entry.getValue();
				if(Integer.parseInt(property.getColumn()) == column){
					property.setConvertable(true);
					property.setConvertMap(m);
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 按照名称解析
	 * @param model
	 * @return
	 */
	private Map<String, RuturnPropertyParam> getPropertyMap(Element model, RuturnConfig config) {
		Map<String, RuturnPropertyParam> propertyMap = new HashMap<String, RuturnPropertyParam>();
		List list = model.elements();// An attribute of a class
		Element property = null;
		for (Iterator it = list.iterator(); it.hasNext();) {
			property = (Element) it.next();
			RuturnPropertyParam modelProperty = new RuturnPropertyParam();
			modelProperty.setName(property
					.attributeValue(ConfigConstant.PROPERTY_NAME));//property name in java bean
			if("Camel-Case".equals(config.getMapModel())){
				modelProperty.setNameForMap(ExcelUtil.parseCamelCase(modelProperty.getName()));
			}
			modelProperty.setColumn(property
					.attributeValue(ConfigConstant.PROPERTY_CLOUMN));//sequeence in excel
			modelProperty.setExcelTitleName(property
					.attributeValue(ConfigConstant.PROPERTY_EXCEL_TITLE_NAME));//table title in excel
			
			modelProperty.setNotNull(Boolean.parseBoolean(property.attributeValue(ConfigConstant.PROPERTY_NOT_NULL)));
			
			modelProperty.setDataType(property
					.attributeValue(ConfigConstant.PROPERTY_DATA_TYPE));//data type:[String,Date]
			
			modelProperty.setMaxLength(property
					.attributeValue(ConfigConstant.PROPERTY_MAX_LENGTH));
			String viewLength = property.attributeValue(ConfigConstant.PROPERTY_VIEW_LENGTH);
			if(viewLength != null && !"".equals(viewLength)){
				modelProperty.setViewLength(Integer.valueOf(viewLength));
			}
			modelProperty.setColumn(property.attributeValue(ConfigConstant.PROPERTY_CLOUMN));
			
			modelProperty.setDateFormat(property.attributeValue(ConfigConstant.PROPERTY_FORMAT));
			String isConvertableStr = property
					.attributeValue(ConfigConstant.PROPERTY_ISCONVERTABLE);
			boolean isConvertable = Boolean.parseBoolean(isConvertableStr);
			modelProperty.setConvertable(isConvertable);
			modelProperty.setDictypecode(property.attributeValue(ConfigConstant.PROPERTY_DICTYPECODE));
			modelProperty.setDiccode(property.attributeValue(ConfigConstant.PROPERTY_DICCODE));
			if (isConvertable) {
				List map_list = property.elements();
				Element property_tmp = null;
				for (Iterator it2 = map_list.iterator(); it2.hasNext();) {
					property_tmp = (Element) it2.next();
					if (property_tmp != null) {
						if (property_tmp.getName().equals("map")) {
							Map<String, String> map_tmp = new HashMap<String, String>();
							List entities = property_tmp.elements();
							for (int i = 0; i < entities.size(); i++) {
								Element entity = (Element) entities.get(i);
								String key2 = entity.attributeValue(ExcelUtil.MAP_KEY);
								String value2 = entity.attributeValue(ExcelUtil.MAP_VALUE);
								if (ValueWidget.isHasValue(key2)) {
									map_tmp.put(key2, value2);
								}
							}
							modelProperty.setConvertMap(map_tmp);
						}
					}
				}
				if(modelProperty.getConvertMap() == null){
					modelProperty.setConvertMap(new HashMap<String, String>());
				}
				Map<String, String> map_tmp = new HashMap<String, String>();
				List<String> converValueList = new LinkedList<String>();
				for(Entry<String,String> e : modelProperty.getConvertMap().entrySet()){
					map_tmp.put(e.getValue(), e.getKey());
					converValueList.add(e.getValue());
				}
				modelProperty.setConvertMap2(map_tmp);
				modelProperty.setConverValueList(converValueList);
			}

			modelProperty.setDefaultValue(property
					.attributeValue(ConfigConstant.PROPERTY_DEFAULT));
			
			//
			//System.out.println("assist.set" + modelProperty.getName().substring(0, 1).toUpperCase()
			//		+ modelProperty.getName().substring(1) + "(newData.get" + modelProperty.getName().substring(0, 1).toUpperCase() 
			//		+ modelProperty.getName().substring(1) + "());//"+modelProperty.getExcelTitleName());
			//
			propertyMap.put(modelProperty.getExcelTitleName(), modelProperty);

		}
		return propertyMap;
	}
}
