package com.example.demo.thread;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;

public class ReflectUtil {
	private static final String DEFAULT_NULL_VALUE = "--";

	
	/**
	 * Java 反射获取对应字段的值
	 * @param <V>
	 * 
	 * @param fund
	 * @param field
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T, V> V getTypeField(T t, String field) {
		if (StringUtils.isNotBlank(field)) {
			Class<?> clazz = t.getClass();
			try {
				Field declaredField = FieldUtils.getDeclaredField(clazz, field, true);
				declaredField.setAccessible(true);
				return (V) declaredField.get(t);
			} catch (Exception e) {
				throw new RuntimeException("反射获取字段值异常");
			}
		}
		return null;
	}

	
	
	/**
	 * 
	 * @param fields 对数组字符串类型的进行批量格式化(并不是每个字段都满足这个格式化的要求， 所有要指定格式化的字段)
	 * @param t
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws Exception
	 */
	public static <T, V> T fixedStringNumProperties(String[] fields, T t) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		for(int i=0; i<fields.length; i++){
			String field = fields[i];
			Class<?> clazz = t.getClass();
			Field fd = clazz.getDeclaredField(field);
			fd.setAccessible(true);
			if(null != fd){
				V v = ReflectUtil.getTypeField(t, field);
				if(null == v || v.equals("") || v.equals("--") || v.equals("0") || v.equals("null")){
					fd.set(t, DEFAULT_NULL_VALUE);
				}else{
					if(v instanceof String){
						String value = (String) v;
						BigDecimal decimal = new BigDecimal(value);
						Double d = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
						DecimalFormat df =new DecimalFormat("#,##0.00");
						fd.set(t, df.format(d));
					}
				}
			}
		}
		return t;
	}

	/**
	 * 将字符串类型的数字保留4位小数
	 * @param fields
	 * @param t
	 * @param <T>
	 * @param <V>
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static <T, V> T fixed4StringNumProperties(String[] fields, T t) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		for(int i=0; i<fields.length; i++){
			String field = fields[i];
			Class<?> clazz = t.getClass();
			Field fd = clazz.getDeclaredField(field);
			fd.setAccessible(true);
			if(null != fd){
				V v = ReflectUtil.getTypeField(t, field);
				if(null == v || v.equals("") || v.equals("--") || v.equals("0") || v.equals("null")){
					fd.set(t, DEFAULT_NULL_VALUE);
				}else{
					if(v instanceof String){
						String value = (String) v;
						BigDecimal decimal = new BigDecimal(value);
						Double d = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
						DecimalFormat df =new DecimalFormat("#,##0.0000");
						fd.set(t, df.format(d));
					}
				}
			}
		}
		return t;
	}


	/**
	 * 针对字符类型的字段格式化
	 * @param fields
	 * @param t
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static <T, V> T fixedStrDateProperties(String[] fields, T t) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		for(int i=0; i<fields.length; i++){
			String field = fields[i];
			Class<?> clazz = t.getClass();
			Field fd= clazz.getDeclaredField(field);
			fd.setAccessible(true);
			if(null != fd){
				V v = ReflectUtil.getTypeField(t, field);
				if(null == v || v.equals("")){
					fd.set(t, DEFAULT_NULL_VALUE);
				}
			}
		}
		return t;
	}
	

}
