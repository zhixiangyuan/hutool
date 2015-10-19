package com.xiaoleilu.hutool.db;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.xiaoleilu.hutool.CollectionUtil;
import com.xiaoleilu.hutool.StrUtil;
import com.xiaoleilu.hutool.db.handler.EntityListHandler;
import com.xiaoleilu.hutool.db.handler.EntityHandler;

/**
 * 数据访问层模板<br>
 * 此模板用于简化对指定表的操作，简化的操作如下：<br>
 * 1、在初始化时指定了表名，CRUD操作时便不需要表名<br>
 * 2、在初始化时指定了主键，某些需要主键的操作便不需要指定主键类型
 * @author Looly
 *
 */
public class DaoTemplate {
	protected SqlRunner runner;
	
	/** 表名 */
	protected String tableName;
	/** 本表的主键字段，请在子类中覆盖或构造方法中指定，默认为id */
	protected String primaryKeyField = "id";
	
	//--------------------------------------------------------------- Constructor start
	public DaoTemplate(String tableName) {
		this.tableName = tableName;
	}
	
	public DaoTemplate(String tableName, String primaryKeyField) {
		this.tableName = tableName;
		this.primaryKeyField = primaryKeyField;
	}
	
	public DaoTemplate(String tableName, String primaryKeyField, DataSource ds) {
		this(tableName, primaryKeyField, DbUtil.newSqlRunner(ds));
	}
	
	public DaoTemplate(String tableName, String primaryKeyField, SqlRunner runner) {
		this.tableName = tableName;
		this.primaryKeyField = primaryKeyField;
		this.runner = runner;
	}
	//--------------------------------------------------------------- Constructor end
	
	//------------------------------------------------------------- Add start
	/**
	 * 添加
	 * @param entity 实体对象
	 * @return 插入行数
	 * @throws SQLException
	 */
	public int add(Entity entity) throws SQLException {
		if(StrUtil.isBlank(entity.getTableName())){
			entity.setTableName(tableName);
		}
		return runner.insert(entity);
	}
	
	/**
	 * 添加
	 * @param entity 实体对象
	 * @return 主键列表
	 * @throws SQLException
	 */
	public List<Object> addForGeneratedKeys(Entity entity) throws SQLException {
		if(StrUtil.isBlank(entity.getTableName())){
			entity.setTableName(tableName);
		}
		return runner.insertForGeneratedKeys(entity);
	}
	
	/**
	 * 添加
	 * @param entity 实体对象
	 * @return 自增主键
	 * @throws SQLException
	 */
	public Long addForGeneratedKey(Entity entity) throws SQLException {
		if(StrUtil.isBlank(entity.getTableName())){
			entity.setTableName(tableName);
		}
		return runner.insertForGeneratedKey(entity);
	}
	//------------------------------------------------------------- Add end
	
	//------------------------------------------------------------- Delete start
	/**
	 * 删除
	 * @param <T> 主键类型
	 * 
	 * @param pk 主键
	 * @return 删除行数
	 * @throws SQLException
	 */
	public <T> int del(T pk) throws SQLException {
		if (pk == null) {
			return 0;
		}

		return this.del(Entity.create(tableName).set(primaryKeyField, pk));
	}
	
	/**
	 * 删除
	 * @param <T> 主键类型
	 * 
	 * @param field 字段名
	 * @param value 字段值
	 * @return 删除行数
	 * @throws SQLException
	 */
	public <T> int del(String field, T value) throws SQLException {
		if (StrUtil.isBlank(field)) {
			return 0;
		}

		return this.del(Entity.create(tableName).set(field, value));
	}
	
	/**
	 * 删除
	 * @param <T> 主键类型
	 * 
	 * @param where 删除条件，当条件为空时，返回0（防止误删全表）
	 * @return 删除行数
	 * @throws SQLException
	 */
	public <T> int del(Entity where) throws SQLException {
		if (CollectionUtil.isEmpty(where)) {
			return 0;
		}
		if(StrUtil.isBlank(where.getTableName())){
			where.setTableName(tableName);
		}

		return runner.del(where);
	}
	//------------------------------------------------------------- Delete end
	
	//------------------------------------------------------------- Update start
	/**
	 * 更新职位信息
	 * 
	 * @param entity 实体对象，必须包含主键
	 * @return 更新行数
	 * @throws SQLException
	 */
	public int update(Entity entity) throws SQLException {
		entity.setTableName(tableName);
		Object pk = entity.get(primaryKeyField);
		if (null == pk) {
			throw new SQLException(StrUtil.format("Please determine `{}` for update", primaryKeyField));
		}

		Entity where = Entity.create(tableName).set(primaryKeyField, pk);
		Entity record = (Entity) entity.clone();
		record.remove(primaryKeyField);

		return runner.update(record, where);
	}
	
	/**
	 * 增加或者更新实体
	 * @param entity 实体，当包含主键时更新，否则新增
	 * @return 新增或更新条数
	 * @throws SQLException
	 */
	public int addOrUpdate(Entity entity) throws SQLException {
		if(entity.get(primaryKeyField) == null) {
			entity.set(primaryKeyField, add(entity));
			return 1;
		}else {
			 return update(entity);
		}
	}
	//------------------------------------------------------------- Update end
	
	//------------------------------------------------------------- Get start
	/**
	 * 根据主键获取单个记录
	 * @param <T>
	 * 
	 * @param pk 主键值
	 * @return 记录
	 * @throws SQLException
	 */
	public <T> Entity get(T pk) throws SQLException {
		return this.get(primaryKeyField, pk);
	}
	
	/**
	 * 根据某个字段（最好是唯一字段）查询单个记录<br>
	 * 当有多条返回时，只显示查询到的第一条
	 * @param <T>
	 * 
	 * @param field 字段名
	 * @param value 字段值
	 * @return 记录
	 * @throws SQLException
	 */
	public <T> Entity get(String field, T value) throws SQLException {
		return this.get(Entity.create(tableName).set(field, value));
	}
	
	/**
	 * 根据条件实体查询单个记录，当有多条返回时，只显示查询到的第一条
	 * 
	 * @param where 条件
	 * @return 记录
	 * @throws SQLException
	 */
	public Entity get(Entity where) throws SQLException {
		if(null == where){
			where = Entity.create(tableName);
		}
		
		if(StrUtil.isBlank(where.getTableName())){
			where.setTableName(tableName);
		}
		return runner.find(null, where, new EntityHandler());
	}
	//------------------------------------------------------------- Get end
	
	//------------------------------------------------------------- Find start
	/**
	 * 根据某个字段值查询结果
	 * @param <T>
	 * 
	 * @param field 字段名
	 * @param value 字段值
	 * @return 记录
	 * @throws SQLException
	 */
	public <T> List<Entity> find(String field, T value) throws SQLException {
		return this.find(Entity.create(tableName).set(field, value));
	}
	
	/**
	 * 查询当前表的所有记录
	 * @return 记录
	 * @throws SQLException
	 */
	public List<Entity> findAll() throws SQLException {
		return this.find(Entity.create(tableName));
	}
	
	/**
	 * 根据某个字段值查询结果
	 * 
	 * @param where 查询条件
	 * @return 记录
	 * @throws SQLException
	 */
	public List<Entity> find(Entity where) throws SQLException {
		return runner.find(null, where, new EntityListHandler());
	}
	
	/**
	 * 满足条件的数据条目数量
	 * @param where 条件
	 * @return 数量
	 * @throws SQLException
	 */
	public int count(Entity where) throws SQLException{
		return runner.count(where);
	}
	
	/**
	 * 指定条件的数据是否存在
	 * @param where 条件
	 * @return 是否存在
	 * @throws SQLException
	 */
	public boolean exist(Entity where) throws SQLException{
		return this.count(where) > 0;
	}
	//------------------------------------------------------------- Find end
}
